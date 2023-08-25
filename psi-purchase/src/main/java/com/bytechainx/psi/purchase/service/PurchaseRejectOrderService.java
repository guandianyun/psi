package com.bytechainx.psi.purchase.service;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.bytechainx.psi.common.BizException;
import com.bytechainx.psi.common.EnumConstant.AuditStatusEnum;
import com.bytechainx.psi.common.EnumConstant.CheckingRefOrderTypeEnum;
import com.bytechainx.psi.common.EnumConstant.ExportStatusEnum;
import com.bytechainx.psi.common.EnumConstant.FundFlowEnum;
import com.bytechainx.psi.common.EnumConstant.FundTypeEnum;
import com.bytechainx.psi.common.EnumConstant.LogisticsStatusEnum;
import com.bytechainx.psi.common.EnumConstant.LogisticsStatusInEnum;
import com.bytechainx.psi.common.EnumConstant.MsgDataTypeEnum;
import com.bytechainx.psi.common.EnumConstant.OrderPayStatusEnum;
import com.bytechainx.psi.common.EnumConstant.OrderStatusEnum;
import com.bytechainx.psi.common.EnumConstant.RefOrderTypeEnum;
import com.bytechainx.psi.common.EnumConstant.RejectTypeEnum;
import com.bytechainx.psi.common.EnumConstant.StockInOutOrderTypeEnum;
import com.bytechainx.psi.common.EnumConstant.StockIoTypeEnum;
import com.bytechainx.psi.common.EnumConstant.TenantConfigEnum;
import com.bytechainx.psi.common.kit.DateUtil;
import com.bytechainx.psi.common.kit.excel.XlsKit;
import com.bytechainx.psi.common.model.GoodsInfo;
import com.bytechainx.psi.common.model.GoodsUnit;
import com.bytechainx.psi.common.model.InventoryStock;
import com.bytechainx.psi.common.model.InventoryStockLog;
import com.bytechainx.psi.common.model.PurchaseOrder;
import com.bytechainx.psi.common.model.PurchaseOrderGoods;
import com.bytechainx.psi.common.model.PurchaseRejectOrder;
import com.bytechainx.psi.common.model.PurchaseRejectOrderCost;
import com.bytechainx.psi.common.model.PurchaseRejectOrderFee;
import com.bytechainx.psi.common.model.PurchaseRejectOrderFund;
import com.bytechainx.psi.common.model.PurchaseRejectOrderGoods;
import com.bytechainx.psi.common.model.PurchaseRejectOrderLog;
import com.bytechainx.psi.common.model.SupplierInfo;
import com.bytechainx.psi.common.model.TenantAdmin;
import com.bytechainx.psi.common.model.TenantConfig;
import com.bytechainx.psi.common.model.TenantExportLog;
import com.bytechainx.psi.common.model.TraderBalanceAccount;
import com.bytechainx.psi.common.model.TraderBookAccount;
import com.bytechainx.psi.common.model.TraderBookAccountLogs;
import com.bytechainx.psi.common.model.TraderFundOrder;
import com.bytechainx.psi.common.model.TraderFundType;
import com.bytechainx.psi.common.model.TraderSupplierPayable;
import com.bytechainx.psi.common.service.base.CommonService;
import com.google.common.collect.Lists;
import com.jfinal.aop.Inject;
import com.jfinal.kit.Kv;
import com.jfinal.kit.Ret;
import com.jfinal.kit.ThreadPoolKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;


/**
* 进货退货退货单
*/
public class PurchaseRejectOrderService extends CommonService {
	
	@Inject
	private PurchaseOrderService purchaseOrderService;

	/**
	* 分页列表
	*/
	public Page<PurchaseRejectOrder> paginate(String startDay, String endDay, Kv conditionColumns, Integer pageNumber, int pageSize) {
		StringBuffer where = new StringBuffer();
		List<Object> params = new ArrayList<Object>();
		where.append("where 1 = 1 ");
		if(StringUtils.isNotEmpty(startDay)) {
			where.append(" and order_time  >= ?");
			params.add(startDay);
		}
		if(StringUtils.isNotEmpty(endDay)) {
			where.append(" and order_time  <= ?");
			params.add(endDay);
		}

		conditionFilter(conditionColumns, where, params);

		return PurchaseRejectOrder.dao.paginate(pageNumber, pageSize, "select * ", "from purchase_reject_order "+where.toString()+" order by id desc", params.toArray());
	}

	/**
	* 新增
	 * @param orderCostList 
	 * @param orderFeeList 
	*/
	public Ret create(PurchaseRejectOrder order, List<PurchaseRejectOrderGoods> orderGoodList, List<PurchaseRejectOrderFund> orderFundList, List<PurchaseRejectOrderFee> orderFeeList, List<PurchaseRejectOrderCost> orderCostList) {
		if( order == null) {
			return Ret.fail("参数错误");
		}
		if(order.getSupplierInfoId() == null || order.getSupplierInfoId() <= 0) {
			return Ret.fail("供应商不能为空");
		}
		if(order.getHandlerId() == null || order.getHandlerId() <= 0) {
			return Ret.fail("进货退货员不能为空");
		}
		if(order.getAmount() == null) {
			return Ret.fail("应退金额不能为空");
		}
		if(order.getDiscountAmount() == null) {
			return Ret.fail("折后应退不能为空");
		}
		if(order.getOrderTime() == null) {
			return Ret.fail("单据时间不能为空");
		}
		if(orderGoodList == null || orderGoodList.isEmpty()) {
			return Ret.fail("订单商品不能为空"); 
		}
		BigDecimal paidAmount = BigDecimal.ZERO; // 实付已付金额
		if(orderFundList != null && !orderFundList.isEmpty()) {
			for (PurchaseRejectOrderFund e : orderFundList) {
				paidAmount = paidAmount.add(e.getAmount());
			}
		}
		if(paidAmount.compareTo(order.getAmount()) > 0) {
			return Ret.fail("订金不能大于应收金额"); 
		}
		// 设置审核状态
		AuditStatusEnum auditStatus = getAuditStatus(order);
		order.setAuditStatus(auditStatus.getValue());
		// 设置出入库状态
		LogisticsStatusEnum logisticsStatus = getLogisticsStatus(order);
		order.setLogisticsStatus(logisticsStatus.getValue());
		
		order.setPaidAmount(paidAmount);
		if(order.getPaidAmount().compareTo(order.getAmount()) >= 0) {
			order.setPayStatus(OrderPayStatusEnum.finish.getValue());
		} else if(order.getPaidAmount().compareTo(BigDecimal.ZERO) > 0) {
			order.setPayStatus(OrderPayStatusEnum.part.getValue());
		}
		// 发送消息给审核人
	    sendOrderNoticeMsg(order);
	    
	    String orderCode = order.generateOrderCode(); //  获取订单号，单号存在redis上。
		if(StringUtils.isEmpty(orderCode)) {
			orderCode = order.generateOrderCode();
			if(StringUtils.isEmpty(orderCode)) {
				return Ret.fail("创建订单异常，请稍候重试");
			}
		}
		order.setOrderCode(orderCode);
		order.setCreatedAt(new Date());
		order.setUpdatedAt(new Date());
		order.save();
		
		Set<Integer> orderIdList = new HashSet<>(); // 关联转换的订单列表
		// 保存商品
		for (PurchaseRejectOrderGoods e : orderGoodList) {
			if(e.getGoodsInfoId() == null || e.getGoodsInfoId() <= 0 || e.getBuyNumber().doubleValue() <= 0) {
				continue;
			}
			e.setSupplierInfoId(order.getSupplierInfoId());
			e.setCreatedAt(new Date());
			e.setUpdatedAt(new Date());
			e.setPurchaseRejectOrderId(order.getId());
			e.save();
			
			// 进货单退货处理
			orderReject(order, e, null, orderIdList);
		}
		if(!orderIdList.isEmpty()) {
			order.setPurchaseOrderId(","+StringUtils.join(orderIdList, ",")+",");
			order.update();
		}
		
		// 库存退回给供应商
		updateInventoryStock(order, orderGoodList);
		
		// 订单支付资金
		if(orderFundList != null && !orderFundList.isEmpty()) {
			for (PurchaseRejectOrderFund e : orderFundList) {
				paymentOrderFund(order, e, false);
			}
		}
		// 货款计入收入
		goodsExpensesTraderBookAccount(order, order.getAmount());
		// 记录对账单
		updateSupplierPayable(order);
		// 记录其他费用，需要支付给供应商的
		updateOrderFee(order, orderFeeList);
		// 记录支出成本，自己本身的支出
		updateOrderCost(order, orderCostList);
		// 更新货品的告警状态，FIXME 要不要判断状态，减少无效操作
		updateStockWarn(order.getOrderGoodsList());
				
		// 记录订单操作
		String operDesc = "创建退货单";
		if(StringUtils.isNotEmpty(order.getPurchaseOrderId())) {
			operDesc = "进货单退货";
		}
		writeLog(order.getId(), order.getLastManId(), operDesc);
		
		return Ret.ok("新增进货退货单成功").set("targetId", order.getId());
	}

	/**
	* 修改
	 * @param orderCostList 
	 * @param orderFeeList 
	*/
	public Ret update(PurchaseRejectOrder order, List<PurchaseRejectOrderGoods> orderGoodList, List<PurchaseRejectOrderFund> orderFundList, List<PurchaseRejectOrderFee> orderFeeList, List<PurchaseRejectOrderCost> orderCostList) {
		if(order == null || order.getId() == null || order.getId() <= 0) {
			return Ret.fail("参数错误");
		}
		if(order.getSupplierInfoId() == null || order.getSupplierInfoId() <= 0) {
			return Ret.fail("供应商不能为空");
		}
		if(order.getHandlerId() == null || order.getHandlerId() <= 0) {
			return Ret.fail("进货退货员不能为空");
		}
		if(order.getAmount() == null) {
			return Ret.fail("应退金额不能为空");
		}
		if(order.getDiscountAmount() == null) {
			return Ret.fail("折后应退不能为空");
		}
		if(order.getOrderTime() == null) {
			return Ret.fail("单据时间不能为空");
		}
		if(orderGoodList == null || orderGoodList.isEmpty()) {
			return Ret.fail("订单商品不能为空"); 
		}
		BigDecimal paidAmount = BigDecimal.ZERO; // 实付已付金额
		if(orderFundList != null && !orderFundList.isEmpty()) {
			for (PurchaseRejectOrderFund e : orderFundList) {
				paidAmount = paidAmount.add(e.getAmount());
			}
		}
		if(paidAmount.compareTo(order.getAmount()) > 0) {
			return Ret.fail("实收不能大于应收金额"); 
		}
		
		PurchaseRejectOrder _order = PurchaseRejectOrder.dao.findById(order.getId());
		if(_order == null) {
			return Ret.fail("进货退货单不存在，无法修改");
		}
		if(_order.getOrderStatus() == OrderStatusEnum.normal.getValue() && order.getOrderStatus() != OrderStatusEnum.normal.getValue()) {
			return Ret.fail("单据状态异常，无法修改"); 
		}
		BigDecimal changeAmount = order.getAmount().subtract(_order.getAmount()); // 应收金额发生变更
		// 审核状态要根据是否开启审核开关来设置，默认无需审核
		if (_order.getOrderStatus() == OrderStatusEnum.draft.getValue()) { // 如果之前是草稿单，则处理审核状态
			AuditStatusEnum auditStatus = getAuditStatus(order);
			order.setAuditStatus(auditStatus.getValue());
			// 设置出入库状态
			LogisticsStatusEnum logisticsStatus = getLogisticsStatus(order);
			order.setLogisticsStatus(logisticsStatus.getValue());
			
			// 发送消息给审核人
		    sendOrderNoticeMsg(order);
		    
		} else if(_order.getAuditStatus() == AuditStatusEnum.reject.getValue()) {
			order.setLogisticsStatus(LogisticsStatusInEnum.waiting.getValue());
			order.setAuditStatus(AuditStatusEnum.waiting.getValue());
		} else {
			order.setLogisticsStatus(_order.getLogisticsStatus());
			order.setAuditStatus(_order.getAuditStatus());
		}
		order.setPaidAmount(paidAmount);
		if(order.getPaidAmount().compareTo(order.getAmount()) >= 0) {
			order.setPayStatus(OrderPayStatusEnum.finish.getValue());
		} else if(order.getPaidAmount().compareTo(BigDecimal.ZERO) > 0) {
			order.setPayStatus(OrderPayStatusEnum.part.getValue());
		}
		order.setUpdatedAt(new Date());
		order.update();
		
		// 更新商品数据
		boolean goodsModifyFlag = updateOrderGoods(order, orderGoodList);
		// 库存退回给供应商
		updateInventoryStock(order, orderGoodList);
		// 更新订单付款资金
		boolean fundModifyFlag = updateOrderFund(order, orderFundList, false);
		
		// 货款计入收入
		goodsExpensesTraderBookAccount(order, changeAmount);
		// 记录对账单
		updateSupplierPayable(order);
		// 记录其他费用，需要支付给供应商的
		updateOrderFee(order, orderFeeList);
		// 记录支出成本，自己本身的支出
		updateOrderCost(order, orderCostList);
		// 更新货品的告警状态，FIXME 要不要判断状态，减少无效操作
		updateStockWarn(order.getOrderGoodsList());

		// 记录订单操作
		writeLog(order.getId(), order.getLastManId(), "修改单据:"+order.getChangeLog(_order, goodsModifyFlag, fundModifyFlag));
			
		return Ret.ok("修改进货退货单成功").set("targetId", order.getId());
	}

	/**
	* 作废
	*/
	public Ret disable(List<Integer> ids) {
		if(ids == null || ids.isEmpty()) {
			return Ret.fail("参数错误");
		}
		for (Integer id : ids) {
			PurchaseRejectOrder order = PurchaseRejectOrder.dao.findById(id);
			if(order == null) {
				continue;
			}
			if(order.getOrderStatus() == OrderStatusEnum.disable.getValue()) { // 已作废的单据不能重复作废
				continue;
			}
			
			// 商品库存还原
			undoInventoryStock(order, order.getOrderGoodsList());

			if(order.getAuditStatus() != AuditStatusEnum.pass.getValue()) { // 非已审核的不执行以下资金回退操作
				order.setLogisticsStatus(LogisticsStatusEnum.waiting.getValue());
				order.setAuditStatus(AuditStatusEnum.waiting.getValue());
				order.setOrderStatus(OrderStatusEnum.disable.getValue());
				order.setUpdatedAt(new Date());
				order.update();
				
				continue;
			}
			// 进货单退货回退
			for (PurchaseRejectOrderGoods e : order.getOrderGoodsList()) {
				undoOrderReject(order, e);
			}
			
			// 订单资金回退
			for (PurchaseRejectOrderFund e : order.getOrderFundList()) {
				// 根据支付方式，回退资金
				undoOrderFund(order, e);
			}
			// 删除交易流水
			Db.delete("delete from trader_fund_order where tenant_store_id = ? and ref_order_type = ? and ref_order_id = ?", RefOrderTypeEnum.purchase_reject_order.getValue(), order.getId());
			// 货款收入回退
			goodsExpensesTraderBookAccount(order, BigDecimal.ZERO.subtract(order.getAmount()));
			// 记录对账单
			updateSupplierPayable(order);
			
			order.setLogisticsStatus(LogisticsStatusEnum.waiting.getValue());
			order.setAuditStatus(AuditStatusEnum.waiting.getValue());
			order.setOrderStatus(OrderStatusEnum.disable.getValue());
			order.setUpdatedAt(new Date());
			order.update();
			
			// 更新货品的告警状态，FIXME 要不要判断状态，减少无效操作
			updateStockWarn(order.getOrderGoodsList());
			
			// 记录订单操作
			writeLog(order.getId(), order.getLastManId(), "作废进货退货单");
		}
		return Ret.ok("作废进货退货单成功");
	}
	
	
	/**
	 * 进货退货单付款
	 * @param 
	 * @param orderId
	 * @param orderFundList
	 * @return
	 */
	public Ret payment(Integer orderId, List<PurchaseRejectOrderFund> orderFundList) {
		if(orderId == null || orderId <= 0 || orderFundList == null || orderFundList.isEmpty()) {
			return Ret.fail("参数错误");
		}
		PurchaseRejectOrder order = PurchaseRejectOrder.dao.findById(orderId);
		if(order == null) {
			return Ret.fail("进货退货单不存在，无法修改");
		}
		if(order.getAuditStatus() != AuditStatusEnum.pass.getValue()) {
			return Ret.fail("进货退货单未通过审核，无法付款");
		}
		if(order.getOrderStatus() != OrderStatusEnum.normal.getValue()) {
			return Ret.fail("非正常进货退货单，无法付款");
		}
		if(order.getPayStatus() == OrderPayStatusEnum.finish.getValue()) {
			return Ret.fail("进货退货单已付清，无需付款");
		}
		// 账户付款，更新结算帐户资金
		for (PurchaseRejectOrderFund e : orderFundList) {
			// 支付资金，生成记录
			paymentOrderFund(order, e, false);
		}
		
		return Ret.ok("进货退货单付款成功");
	}


	/**
	* 审核
	*/
	public Ret audit(List<Integer> ids, AuditStatusEnum auditStatus, String auditDesc, Integer auditorId) {
		if(ids == null || ids.isEmpty()) {
			return Ret.fail("参数错误");
		}
		if(auditStatus == null) {
			return Ret.fail("审核状态错误");
		}
		if(auditorId == null || auditorId <= 0) {
			return Ret.fail("审核人不能为空");
		}
		for (Integer id : ids) {
			PurchaseRejectOrder order = PurchaseRejectOrder.dao.findById(id);
			if(order == null) {
				continue;
			}
			if(order.getAuditStatus() != AuditStatusEnum.waiting.getValue()) { // 已审核的订单不能重复审核
				continue;
			}
			// 设置出入库状态
			LogisticsStatusEnum logisticsStatus = getLogisticsStatus(order);
			order.setLogisticsStatus(logisticsStatus.getValue());

			order.setAuditorId(auditorId);
			order.setAuditDesc(auditDesc);
			order.setAuditStatus(auditStatus.getValue());
			order.setAuditTime(new Date());
			order.setUpdatedAt(new Date());
			order.update();
			
			if(auditStatus.getValue() == AuditStatusEnum.pass.getValue()) { // 审核通过
				Set<Integer> orderIdList = new HashSet<>(); // 关联转换的订单列表
				for (PurchaseRejectOrderGoods e : order.getOrderGoodsList()) {
					orderReject(order, e, null, orderIdList);
				}
				if(!orderIdList.isEmpty()) {
					order.setPurchaseOrderId(","+StringUtils.join(orderIdList, ",")+",");
					order.update();
				}
				// 更新商品库存
				updateInventoryStock(order, order.getOrderGoodsList());
				// 审核处理资金，可能存在多次审核, 审核拒绝无需处理资金
				updateOrderFund(order, order.getOrderFundList(), true);
				// 往来账户
				goodsExpensesTraderBookAccount(order, order.getAmount());
				// 记录对账单
				updateSupplierPayable(order);
			}
			// 更新货品的告警状态，FIXME 要不要判断状态，减少无效操作
			updateStockWarn(order.getOrderGoodsList());
			
			// 写入操作记录
			writeLog(order.getId(), order.getLastManId(), "审核进货退货订单:"+auditStatus.getName());
			// 发送审核结果通知
			sendAuditNoticeMsg(order);
		}
		return Ret.ok("审核进货退货单成功");
	}
	
	/**
	 * 记录单据其他支出成本，不用供应商支付
	 * @param 
	 * @param order
	 * @param orderCostList
	 */
	private void updateOrderCost(PurchaseRejectOrder order, List<PurchaseRejectOrderCost> orderCostList) {
		if(orderCostList == null) {
			orderCostList = new ArrayList<>();
		}
		List<PurchaseRejectOrderCost> oldOrderFeeList = PurchaseRejectOrderCost.dao.find("select * from purchase_reject_order_cost where purchase_reject_order_id = ?", order.getId());
		// 先过滤出已删除的记录
		List<PurchaseRejectOrderCost> deleteFeeList = new ArrayList<>();
		for(PurchaseRejectOrderCost oldFee : oldOrderFeeList) {
			boolean isExist = false;
			for (PurchaseRejectOrderCost fee : orderCostList) {
				if(fee.getId() != null && fee.getId().intValue() == oldFee.getId().intValue()) {
					isExist = true;
					break;
				}
				if(fee.getTraderFundType().intValue() == oldFee.getTraderFundType().intValue()) { // 同一个结算帐户
					isExist = true;
					break;
				}
			}
			if(!isExist) { // 需要删除的记录
				deleteFeeList.add(oldFee);
			}
		}
		
		for (PurchaseRejectOrderCost e : deleteFeeList) {
			// 删除资金记录
			e.delete();
		}
		BigDecimal totalOtherCostAmount = BigDecimal.ZERO;
		for (PurchaseRejectOrderCost orderFee : orderCostList) {
			if(orderFee.getAmount() == null || orderFee.getAmount().compareTo(BigDecimal.ZERO) == 0 || orderFee.getTraderFundType() == null || orderFee.getTraderFundType() <= 0) {
				continue;
			}
			PurchaseRejectOrderCost _orderFee = PurchaseRejectOrderCost.dao.findFirst("select * from purchase_reject_order_cost where purchase_reject_order_id = ? and trader_fund_type = ? limit 1", order.getId(), orderFee.getTraderFundType());
			if(_orderFee == null) {
				orderFee.setPurchaseRejectOrderId(order.getId());
				orderFee.save();
			} else {
				_orderFee.setAmount(orderFee.getAmount());
				_orderFee.update();
			}
			totalOtherCostAmount = totalOtherCostAmount.add(orderFee.getAmount());
		}
		order.setOtherCostAmount(totalOtherCostAmount);
		order.update();
	}


	/**
	 * 记录其他费用，需要供应商支付
	 * @param 
	 * @param order
	 * @param orderFeeList
	 */
	private void updateOrderFee(PurchaseRejectOrder order, List<PurchaseRejectOrderFee> orderFeeList) {
		if(orderFeeList == null) {
			orderFeeList = new ArrayList<>();
		}
		List<PurchaseRejectOrderFee> oldOrderFeeList = PurchaseRejectOrderFee.dao.find("select * from purchase_reject_order_fee where purchase_reject_order_id = ?", order.getId());
		// 先过滤出已删除的记录
		List<PurchaseRejectOrderFee> deleteFeeList = new ArrayList<>();
		for(PurchaseRejectOrderFee oldFee : oldOrderFeeList) {
			boolean isExist = false;
			for (PurchaseRejectOrderFee fee : orderFeeList) {
				if(fee.getId() != null && fee.getId().intValue() == oldFee.getId().intValue()) {
					isExist = true;
					break;
				}
				if(fee.getTraderFundType().intValue() == oldFee.getTraderFundType().intValue()) { // 同一个结算帐户
					isExist = true;
					break;
				}
			}
			if(!isExist) { // 需要删除的记录
				deleteFeeList.add(oldFee);
			}
		}
		
		for (PurchaseRejectOrderFee e : deleteFeeList) {
			// 删除资金记录
			e.delete();
		}
		BigDecimal totalOtherAmount = BigDecimal.ZERO;
		for (PurchaseRejectOrderFee orderFee : orderFeeList) {
			if(orderFee.getAmount() == null || orderFee.getAmount().compareTo(BigDecimal.ZERO) == 0 || orderFee.getTraderFundType() == null || orderFee.getTraderFundType() <= 0) {
				continue;
			}
			PurchaseRejectOrderFee _orderFee = PurchaseRejectOrderFee.dao.findFirst("select * from purchase_reject_order_fee where purchase_reject_order_id = ? and trader_fund_type = ? limit 1", order.getId(), orderFee.getTraderFundType());
			if(_orderFee == null) {
				orderFee.setPurchaseRejectOrderId(order.getId());
				orderFee.save();
			} else {
				_orderFee.setAmount(orderFee.getAmount());
				_orderFee.update();
			}
			totalOtherAmount = totalOtherAmount.add(orderFee.getAmount());
		}
		order.setOtherAmount(totalOtherAmount);
		order.update();
	}

	/**
	 * 记录订单操作日志
	 * @param 
	 * @param orderId
	 * @param operAdminId
	 * @param operDesc
	 * @return
	 */
	public Ret writeLog(Integer orderId, Integer operAdminId, String operDesc) {
		if(StringUtils.contains(operDesc, "数据无变化")) {
			return Ret.ok();
		}
		PurchaseRejectOrderLog log = new PurchaseRejectOrderLog();
		log.setOperAdminId(operAdminId);
		if(operAdminId != null && operAdminId > 0) {
			TenantAdmin admin = TenantAdmin.dao.findById(operAdminId);
			log.setOperAdminName(admin.getRealName());
		} else {
			log.setOperAdminName("系统后台");
		}
		log.setOperDesc(operDesc);
		log.setOperTime(new Date());
		log.setPurchaseRejectOrderId(orderId);
		log.save();
		
		return Ret.ok();
	}


	/**
	 * 删除单据
	 * @param 
	 * @param ids
	 * @return
	 */
	public Ret delete(Integer id) {
		if(id == null) {
			return Ret.fail("参数错误");
		}
		PurchaseRejectOrder order = PurchaseRejectOrder.dao.findById(id);
		if(order == null) {
			return Ret.fail("进货单不存在");
		}
		if(order.getOrderStatus() != OrderStatusEnum.draft.getValue()) { // 非草稿单不能删除
			return Ret.fail("单据不是草稿单，不能删除");
		}
		order.delete();
		
		return Ret.ok("删除单据成功");
	}
	
	/**
	 * 导出数据xls
	 */
	public Ret export(Integer handlerId, String startDay, String endDay, Kv condKv) {
		String fileName = "进货退货单-"+DateUtil.getSecondNumber(new Date());
		TenantExportLog exportLog = new TenantExportLog();
		exportLog.setCreatedAt(new Date());
		exportLog.setFileName(fileName);
		exportLog.setErrorDesc("");
		exportLog.setExportStatus(ExportStatusEnum.ing.getValue());
		exportLog.setFilePath("");
		exportLog.setHandlerId(handlerId);
		exportLog.save();
		
		ThreadPoolKit.execute(() -> {
				try {
					List<PurchaseRejectOrder> orderList = Lists.newArrayList();
					int pageNumber = 1;
					Page<PurchaseRejectOrder> page = paginate(startDay, endDay, condKv, pageNumber, 500);
					while (!page.getList().isEmpty() && orderList.size() < 10000) { // 最大不能超过10000
						orderList.addAll(page.getList());
						pageNumber += 1;
						page = paginate(startDay, endDay, condKv, pageNumber, 200);
					}
					StringBuffer headersBuffer = new StringBuffer();
					headersBuffer.append("单据日期,单据编号");
					StringBuffer columnsBuffer = new StringBuffer();
					columnsBuffer.append("order_time_day,order_code");
					StringBuffer listColumnsBuffer = new StringBuffer();
					listColumnsBuffer.append("goods_name,bar_code,spec_name,unit_name");
					
					headersBuffer.append(",供应商名称,退货仓库,商品名称,商品条码,规格,单位");
					columnsBuffer.append(",supplier_info_name,list,discount");
					
					headersBuffer.append(",数量,单价,商品折扣(%),折后单价,金额,备注,整单折扣率(%)");
					
					List<TraderFundType> feeList = PurchaseRejectOrder.dao.findFeeConfig(); // 其他费用配置
					if(feeList != null) {
						for (TraderFundType fee : feeList) {
							headersBuffer.append("," + fee.getName());
							columnsBuffer.append(",fee_amount_" + fee.getId());
						}
					}
					headersBuffer.append(",应退金额,已退金额");
					columnsBuffer.append(",amount,paid_amount");
					
					List<TraderFundType> costList = PurchaseRejectOrder.dao.findCostConfig(); // 成本支出配置
					if(costList != null) {
						for (TraderFundType cost : costList) {
							headersBuffer.append("," + cost.getName());
							columnsBuffer.append(",cost_amount_" + cost.getId());
						}
					}
					headersBuffer.append(",单据备注,退货员");
					columnsBuffer.append(",remark,handler_name");
					
					listColumnsBuffer.append(",buy_number,price,discount,discount_amount,amount,remark");
					
					String[] headers = headersBuffer.toString().split(",");
					String[] columns = columnsBuffer.toString().split(",");
					String[] listColumns = listColumnsBuffer.toString().split(",");
							
					for (PurchaseRejectOrder order : orderList) {
						order.put("order_time_day", DateUtil.getDayStr(order.getOrderTime()));
						
						List<PurchaseRejectOrderGoods> goodsList = order.getOrderGoodsList();
						order.put("list", goodsList);
						for (PurchaseRejectOrderGoods orderGoods : goodsList) {
							GoodsInfo goodsInfo = orderGoods.getGoodsInfo();
							orderGoods.put("goods_name", goodsInfo.getName());
							orderGoods.put("bar_code", goodsInfo.getBarCode());
							orderGoods.put("spec_name", orderGoods.getGoodsSpecNames());
							orderGoods.put("unit_name", orderGoods.getGoodsUnit().getName());
						}
						List<PurchaseRejectOrderFee> orderFeeList = order.getOrderFeeList();
						for(PurchaseRejectOrderFee fee: orderFeeList) {
							order.put("fee_amount_"+fee.getTraderFundType(), fee.getAmount());
						}
						List<PurchaseRejectOrderCost> orderCostList = order.getOrderCostList();
						for(PurchaseRejectOrderCost cost: orderCostList) {
							order.put("cost_amount_"+cost.getTraderFundType(), cost.getAmount());
						}
						
						order.put("handler_name", order.getHandler().getRealName());
					}
					
					String filePath = XlsKit.genOrderXls("进货退货单", fileName, headers, orderList, columns, listColumns);
					exportLog.setFilePath(filePath);
					exportLog.setExportStatus(ExportStatusEnum.finish.getValue());
					exportLog.setUpdatedAt(new Date());
					exportLog.update();
					
				} catch (Exception e) {
					exportLog.setExportStatus(ExportStatusEnum.fail.getValue());
					exportLog.setErrorDesc(e.getMessage());
					exportLog.setUpdatedAt(new Date());
					exportLog.update();
					
					e.printStackTrace();
				}
			});
		
		return Ret.ok().set("targetId", exportLog.getId());
	}
	
	
	/**
	 * 更新订单付款资金
	 * @param 
	 * @param order
	 * @param orderFundList
	 * @param auditFlag 否审核操作，审核通过不能按修改的行为处理。
	 * @param changeAmount
	 * @return 
	 */
	private boolean updateOrderFund(PurchaseRejectOrder order, List<PurchaseRejectOrderFund> orderFundList, boolean auditFlag) {
		boolean modifyFlag = false; // 是否存在数据修改
		List<PurchaseRejectOrderFund> oldOrderFundList = PurchaseRejectOrderFund.dao.find("select * from purchase_reject_order_fund where purchase_reject_order_id = ?", order.getId());
		// 先过滤出已删除的记录
		List<PurchaseRejectOrderFund> deleteFundList = new ArrayList<>();
		for(PurchaseRejectOrderFund oldFund : oldOrderFundList) {
			boolean isExist = false;
			for (PurchaseRejectOrderFund fund : orderFundList) {
				if(fund.getId() != null && fund.getId().intValue() == oldFund.getId().intValue()) {
					isExist = true;
					break;
				}
			}
			if(!isExist) { // 需要删除的记录
				deleteFundList.add(oldFund);
			}
		}
		
		for (PurchaseRejectOrderFund e : deleteFundList) {
			// 回退资金到帐户
			undoOrderFund(order, e);
			// 删除资金记录
			e.delete();
			modifyFlag = true;
		}
		
		// 账户付款，更新结算帐户资金
		for (PurchaseRejectOrderFund e : orderFundList) {
			// 支付资金，生成记录
			boolean _modifyFlag = paymentOrderFund(order, e, auditFlag);
			if(_modifyFlag) {
				modifyFlag = _modifyFlag;
			}
		}
		return modifyFlag;
	}

	/**
	 * 回退资金到帐户
	 * @param 
	 * @param order
	 * @param orderFund
	 */
	private void undoOrderFund(PurchaseRejectOrder order, PurchaseRejectOrderFund orderFund) {
		if(order.getAuditStatus() != AuditStatusEnum.pass.getValue()) { // 非审核不处理资金
			return;
		}
		if(orderFund.getPayType() == FundTypeEnum.cash.getValue()) { // 本单支付，退回到结算帐户
			TraderBalanceAccount balanceAccount = TraderBalanceAccount.dao.findById(orderFund.getBalanceAccountId());
			balanceAccount.setBalance(balanceAccount.getBalance().subtract(orderFund.getAmount())); // 回退支付的金额
			balanceAccount.setUpdatedAt(new Date());
			balanceAccount.update();
			
			BigDecimal changeAmount = BigDecimal.ZERO.subtract(orderFund.getAmount()); // 回退支付的金额，支付的时候是减少，回退的时候是增加
			fundOutTraderBookAccount(order, changeAmount);
			
		} else if(orderFund.getPayType() == FundTypeEnum.balance.getValue()) { // 余额支付，退回到往来帐户
			BigDecimal changeAmount = BigDecimal.ZERO.subtract(orderFund.getAmount()); // 回退支付的金额，支付的时候是减少，回退的时候是增加
			fundPayTraderBookAccount(order, changeAmount);
		}
	}
	
	/**
	 * 帐户支付资金处理
	 * @param 
	 * @param order
	 * @param orderFund
	 * @param auditFlag 
	 * @return
	 */
	private boolean paymentOrderFund(PurchaseRejectOrder order, PurchaseRejectOrderFund orderFund, boolean auditFlag) {
		boolean modifyFlag = false; // 是否存在数据修改
		if(orderFund.getBalanceAccountId() == null || orderFund.getBalanceAccountId() <= 0 || orderFund.getAmount() == null || orderFund.getAmount().doubleValue() <= 0) {
			return modifyFlag;
		}
		BigDecimal changeFundAmount = orderFund.getAmount(); // 变更的金额
		PurchaseRejectOrderFund _orderFund = PurchaseRejectOrderFund.dao.findFirst("select * from purchase_reject_order_fund where purchase_reject_order_id = ? and id = ?", order.getId(), orderFund.getId());
		if(_orderFund != null) {
			changeFundAmount = orderFund.getAmount().subtract(_orderFund.getAmount()); // 计算差额
			if(_orderFund.getBalanceAccountId().intValue() != orderFund.getBalanceAccountId().intValue() 
					|| (_orderFund.getBalanceAccountId().intValue() == orderFund.getBalanceAccountId().intValue() && _orderFund.getPayType() != orderFund.getPayType()) 
					|| _orderFund.getAmount().compareTo(orderFund.getAmount()) != 0 || _orderFund.getPayTime().getTime() != orderFund.getPayTime().getTime()) {
				if(orderFund.getPayType() != _orderFund.getPayType()) { // 支付方式改变了,则要处理老的资金
					changeFundAmount = orderFund.getAmount();
					
					if(_orderFund.getPayType() == FundTypeEnum.cash.getValue()) { // 本单支付，从结算帐户支付
						fundOutTraderBookAccount(order, BigDecimal.ZERO.subtract(_orderFund.getAmount()));
					} else if(_orderFund.getPayType() == FundTypeEnum.balance.getValue()) { // 余额支付，从往来帐户支付
						fundPayTraderBookAccount(order, BigDecimal.ZERO.subtract(_orderFund.getAmount()));
					}
				}
				_orderFund.setAmount(orderFund.getAmount());
				_orderFund.setBalanceAccountId(orderFund.getBalanceAccountId());
				_orderFund.setPayTime(orderFund.getPayTime());
				_orderFund.setPayType(orderFund.getPayType());
				_orderFund.setUpdatedAt(new Date());
				_orderFund.update();
			}
			modifyFlag = true;
			
		} else {
			orderFund.setPurchaseRejectOrderId(order.getId());
			orderFund.setUpdatedAt(new Date());
			orderFund.setCreatedAt(new Date());
			orderFund.save();
			
			modifyFlag = true;
		}
		if(order.getAuditStatus() != AuditStatusEnum.pass.getValue()) { // 不是已审核状态，不处理资金
			return modifyFlag;
		}
		if(auditFlag) {
			changeFundAmount = orderFund.getAmount(); // 审核操作，变更的金额就是原金额
		}
		if(orderFund.getPayType() == FundTypeEnum.cash.getValue()) { // 本单支付，从结算帐户支付
			TraderBalanceAccount balanceAccount = TraderBalanceAccount.dao.findById(orderFund.getBalanceAccountId());
			balanceAccount.setBalance(balanceAccount.getBalance().add(changeFundAmount)); // 退款，结算帐户增加支付的金额
			balanceAccount.setUpdatedAt(new Date());
			balanceAccount.update();
			
			// 更新往来帐户资金
			fundOutTraderBookAccount(order, changeFundAmount);
			
		} else if(orderFund.getPayType() == FundTypeEnum.balance.getValue()) { // 余额支付，从往来帐户支付
			fundPayTraderBookAccount(order, changeFundAmount);
		}
		
		// 生成交易流水
		TraderFundOrder fundOrder = TraderFundOrder.dao.findByOrderId(orderFund.getBalanceAccountId(), RefOrderTypeEnum.purchase_reject_order, order.getId());
		if(fundOrder != null) { // 存在，则更新
			fundOrder.setAmount(orderFund.getAmount());
			fundOrder.setOrderTime(new Date());
			fundOrder.setUpdatedAt(new Date());
			fundOrder.update();
			
		} else { // 不存在，则新增
			fundOrder = new TraderFundOrder();
			fundOrder.setAmount(orderFund.getAmount());
			fundOrder.setFundFlow(FundFlowEnum.income.getValue());
			fundOrder.setOrderTime(new Date());
			fundOrder.setRefOrderCode(order.getOrderCode());
			fundOrder.setRefOrderId(order.getId());
			fundOrder.setRefOrderType(RefOrderTypeEnum.purchase_reject_order.getValue());
			if(orderFund.getPayType() == FundTypeEnum.balance.getValue()) {
				fundOrder.setTraderBalanceAccountId(0);
			} else {
				fundOrder.setTraderBalanceAccountId(orderFund.getBalanceAccountId());
			}
			fundOrder.setCreatedAt(new Date());
			fundOrder.setUpdatedAt(new Date());
			fundOrder.save();
		}
		return modifyFlag;
	}

	/**
	 * 更新往来帐户资金：供应商退钱，给供应商的支出就减少
	 * @param 
	 * @param order
	 * @param changeAmount
	 */
	private void fundOutTraderBookAccount(PurchaseRejectOrder order, BigDecimal changeAmount) {
		if(order.getAuditStatus() != AuditStatusEnum.pass.getValue()) { // 非审核不处理资金
			return;
		}
		if(changeAmount.compareTo(BigDecimal.ZERO) == 0) {
			return;
		}
		// 更新往来账户金额
		SupplierInfo supplierInfo = SupplierInfo.dao.findById(order.getSupplierInfoId());
		TraderBookAccount bookAccount = supplierInfo.getTraderBookAccount();
		BigDecimal outAmount = bookAccount.getOutAmount().subtract(changeAmount);
		bookAccount.setOutAmount(outAmount);
		BigDecimal payAmount = bookAccount.getPayAmount().subtract(changeAmount);
		bookAccount.setPayAmount(payAmount);
		bookAccount.setUpdatedAt(new Date());
		bookAccount.update();

		// 记录往来帐户出入金日志表
		TraderBookAccountLogs bookAccountLogs = new TraderBookAccountLogs();
		bookAccountLogs.setAmount(changeAmount);
		bookAccountLogs.setBalance(bookAccount.getInAmount().subtract(bookAccount.getOutAmount()));
		bookAccountLogs.setCreatedAt(new Date());
		bookAccountLogs.setFundFlow(FundFlowEnum.income.getValue());
		bookAccountLogs.setTraderBookAccountId(bookAccount.getId());
		bookAccountLogs.save();
	}
	
	/**
	 * 更新往来帐户资金：供应商平账资金
	 * @param 
	 * @param order
	 * @param changeAmount
	 */
	private void fundPayTraderBookAccount(PurchaseRejectOrder order, BigDecimal changeAmount) {
		if(order.getAuditStatus() != AuditStatusEnum.pass.getValue()) { // 非审核不处理资金
			return;
		}
		if(changeAmount.compareTo(BigDecimal.ZERO) == 0) {
			return;
		}
		// 更新往来账户金额
		SupplierInfo supplierInfo = SupplierInfo.dao.findById(order.getSupplierInfoId());
		TraderBookAccount bookAccount = supplierInfo.getTraderBookAccount();
		BigDecimal payAmount = bookAccount.getPayAmount().subtract(changeAmount);
		bookAccount.setPayAmount(payAmount);
		bookAccount.setUpdatedAt(new Date());
		bookAccount.update();
	}
	
	/**
	 * 更新往来帐户资金：给供应商退货，欠供应商货品(货款)就减少
	 * @param 
	 * @param order
	 * @param changeAmount
	 */
	private void goodsExpensesTraderBookAccount(PurchaseRejectOrder order, BigDecimal changeAmount) {
		if(order.getAuditStatus() != AuditStatusEnum.pass.getValue()) { // 非审核不处理资金
			return;
		}
		if(changeAmount.compareTo(BigDecimal.ZERO) == 0) {
			return;
		}
		// 更新往来账户金额
		SupplierInfo supplierInfo = SupplierInfo.dao.findById(order.getSupplierInfoId());
		TraderBookAccount bookAccount = supplierInfo.getTraderBookAccount();
		BigDecimal inAmount = bookAccount.getInAmount().subtract(changeAmount);
		bookAccount.setInAmount(inAmount);
		bookAccount.setUpdatedAt(new Date());
		bookAccount.update();

		// 记录往来帐户出入金日志表
		TraderBookAccountLogs bookAccountLogs = new TraderBookAccountLogs();
		bookAccountLogs.setAmount(changeAmount);
		bookAccountLogs.setBalance(bookAccount.getInAmount().subtract(bookAccount.getOutAmount()));
		bookAccountLogs.setCreatedAt(new Date());
		bookAccountLogs.setFundFlow(FundFlowEnum.expenses.getValue());
		bookAccountLogs.setTraderBookAccountId(bookAccount.getId());
		bookAccountLogs.save();
	}
	
	/**
	 * 修改进货退货单时，更新商品数据
	 * @param 
	 * @param order
	 * @param orderGoodList
	 * @return 
	 */
	private boolean updateOrderGoods(PurchaseRejectOrder order, List<PurchaseRejectOrderGoods> orderGoodList) {
		boolean modifyFlag = false; // 是否存在数据修改
		List<PurchaseRejectOrderGoods> oldOrderGoodList = PurchaseRejectOrderGoods.dao.find("select * from purchase_reject_order_goods where purchase_reject_order_id = ?", order.getId());
		// 先过滤出已删除的记录
		List<PurchaseRejectOrderGoods> deleteGoodsList = new ArrayList<>();
		for(PurchaseRejectOrderGoods oldGoods : oldOrderGoodList) {
			boolean isExist = false;
			for (PurchaseRejectOrderGoods goods : orderGoodList) {
				if(goods.getId() != null && goods.getId().intValue() == oldGoods.getId().intValue()) {
					isExist = true;
					break;
				}
			}
			if(!isExist) { // 需要删除的记录
				deleteGoodsList.add(oldGoods);
			}
		}
		// 库存还原
		undoInventoryStock(order, deleteGoodsList);
		// 删除商品数据
		for (PurchaseRejectOrderGoods e : deleteGoodsList) {
			// 释放进货单退货
			undoOrderReject(order, e);
						
			e.delete();
			modifyFlag = true;
		}
		
		Set<Integer> orderIdList = new HashSet<>(); // 关联转换的订单列表
		// 保存商品
		for (PurchaseRejectOrderGoods e : orderGoodList) {
			if (e.getGoodsInfoId() == null || e.getGoodsInfoId() <= 0 || e.getBuyNumber().doubleValue() <= 0) {
				continue;
			}
			PurchaseRejectOrderGoods _e = PurchaseRejectOrderGoods.dao.findById(e.getId());
			BigDecimal changeNumber = e.getBuyNumber(); // 发生变更的库存数量
			if(_e != null) {
				changeNumber = e.getBuyNumber().subtract(_e.getBuyNumber());
				if(e.getBuyNumber().compareTo(_e.getBuyNumber()) != 0 || e.getDiscountAmount().compareTo(_e.getDiscountAmount()) != 0 || 
						e.getPrice().compareTo(_e.getPrice()) != 0 || e.getAmount().compareTo(_e.getAmount()) != 0) {
					_e.setBuyNumber(e.getBuyNumber());
					_e.setDiscount(e.getDiscount());
					_e.setDiscountAmount(e.getDiscountAmount());
					_e.setPrice(e.getPrice());
					_e.setAmount(e.getAmount());
					_e.setUpdatedAt(new Date());
					_e.setSupplierInfoId(order.getSupplierInfoId());
					_e.update();
					
					modifyFlag = true;
				}
			} else {
				e.setSupplierInfoId(order.getSupplierInfoId());
				e.setPurchaseRejectOrderId(order.getId());
				e.setUpdatedAt(new Date());
				e.setCreatedAt(new Date());
				e.save();
				
				modifyFlag = true;
			}
			e.put("changeNumber", changeNumber);
			
			// 进货单退货
			orderReject(order, e, changeNumber, orderIdList);
		}
		if(!orderIdList.isEmpty()) {
			order.setPurchaseOrderId(","+StringUtils.join(orderIdList, ",")+",");
			order.update();
		}
		
		return modifyFlag;
	}
	
	/**
	 * 商品库存取消退货
	 * @param 
	 * @param order
	 * @param orderGoods
	 */
	private void undoInventoryStock(PurchaseRejectOrder order, List<PurchaseRejectOrderGoods> orderGoodsList) {
		if(order.getLogisticsStatus() != LogisticsStatusEnum.stockout.getValue() && order.getOrderStatus() != OrderStatusEnum.normal.getValue()) {
			return;
		}
		for (PurchaseRejectOrderGoods orderGoods : orderGoodsList) {
			GoodsInfo goodsInfo = GoodsInfo.dao.findById(orderGoods.getGoodsInfoId());
			GoodsUnit stockUnit = goodsInfo.getStockMainUnit(); // 通过库存单位查询库存
			BigDecimal goodsStock = orderGoods.getBuyNumber();
			// 普通商品
			InventoryStock inventoryStock = InventoryStock.dao.findByWarehouse(orderGoods.getGoodsInfoId(), orderGoods.getSpec1Id(), orderGoods.getSpecOption1Id(), orderGoods.getSpec2Id(), orderGoods.getSpecOption2Id(), orderGoods.getSpec3Id(), orderGoods.getSpecOption3Id(), stockUnit.getId());
			if(order.getLogisticsStatus() == LogisticsStatusEnum.stockout.getValue()) {
				inventoryStock.setStock(inventoryStock.getStock().add(goodsStock)); // 退回库存
				
			} else if(order.getOrderStatus() == null || order.getOrderStatus() == OrderStatusEnum.normal.getValue()) {
				inventoryStock.setLockStock(inventoryStock.getLockStock().subtract(goodsStock)); // 解锁库存
			}
			inventoryStock.setUpdatedAt(new Date());
			inventoryStock.update();
			// 删除流水
			InventoryStockLog stockLog = InventoryStockLog.dao.findByOrderId(order.getId(), StockInOutOrderTypeEnum.purchase_reject_order.getValue(), orderGoods.getSpec1Id(), orderGoods.getSpecOption1Id(), orderGoods.getSpec2Id(), orderGoods.getSpecOption2Id(), orderGoods.getSpec3Id(), orderGoods.getSpecOption3Id(), orderGoods.getUnitId());
			if(stockLog != null) {
				stockLog.delete();
			}
		}
	}
	
	/**
	 * 商品退货，修改库存
	 * @param 
	 * @param order
	 * @param orderGoods
	 * @param changeNumber
	 */
	private void updateInventoryStock(PurchaseRejectOrder order, List<PurchaseRejectOrderGoods> orderGoodsList) {
		if(order.getLogisticsStatus() != LogisticsStatusEnum.stockout.getValue() && order.getOrderStatus() != OrderStatusEnum.normal.getValue()) {
			return;
		}
		
		for (PurchaseRejectOrderGoods orderGoods : orderGoodsList) {
			BigDecimal changeNumber = orderGoods.getBigDecimal("changeNumber"); // 发生变更的数量
			if(changeNumber == null) {
				changeNumber = orderGoods.getBuyNumber();
			}
			if(changeNumber.compareTo(BigDecimal.ZERO) == 0) {
				continue;
			}
			GoodsInfo goodsInfo = GoodsInfo.dao.findById(orderGoods.getGoodsInfoId());
			GoodsUnit stockUnit = goodsInfo.getStockMainUnit(); // 通过库存单位查询库存
			BigDecimal changeStock = changeNumber; // 换算后的库存单位的数量
						
			InventoryStock inventoryStock = InventoryStock.dao.findByWarehouse(orderGoods.getGoodsInfoId(), orderGoods.getSpec1Id(), orderGoods.getSpecOption1Id(), orderGoods.getSpec2Id(), orderGoods.getSpecOption2Id(), orderGoods.getSpec3Id(), orderGoods.getSpecOption3Id(), stockUnit.getId());
			if(inventoryStock == null) {
				throw new BizException("库存异常，请确认“"+goodsInfo.getName()+"”是否存在库存");
			}
			if(order.getOrderStatus() == null || order.getOrderStatus() == OrderStatusEnum.normal.getValue()) { // 锁定库存状态
				if(inventoryStock.getLockStock() == null) {
					inventoryStock.setLockStock(BigDecimal.ZERO);
				}
				inventoryStock.setLockStock(inventoryStock.getLockStock().add(changeStock)); // 锁定库存
			}
			
			if(order.getLogisticsStatus() == LogisticsStatusEnum.stockout.getValue()) { // 已出库，再次修改库存，直接减库存
				inventoryStock.setLockStock(inventoryStock.getLockStock().subtract(changeStock)); // 解锁库存
				inventoryStock.setStock(inventoryStock.getStock().subtract(changeStock)); // 减去使用库存
			}
			inventoryStock.setUpdatedAt(new Date());
			inventoryStock.update();
			
			// 库存流水
			InventoryStockLog stockLog = InventoryStockLog.dao.findByOrderId(order.getId(), StockInOutOrderTypeEnum.purchase_reject_order.getValue(), orderGoods.getSpec1Id(), orderGoods.getSpecOption1Id(), orderGoods.getSpec2Id(), orderGoods.getSpecOption2Id(), orderGoods.getSpec3Id(), orderGoods.getSpecOption3Id(), orderGoods.getUnitId());
			if(stockLog != null) {
				stockLog.setChangeNumber(orderGoods.getBuyNumber());
				stockLog.setRemainNumber(inventoryStock.getStock());
				stockLog.update();
			} else {
				stockLog = new InventoryStockLog();
				stockLog.setChangeNumber(orderGoods.getBuyNumber());
				stockLog.setCreatedAt(new Date());
				stockLog.setGoodsInfoId(orderGoods.getGoodsInfoId());
				stockLog.setIoType(StockIoTypeEnum.out.getValue());
				stockLog.setOrderCode(order.getOrderCode());
				stockLog.setOrderId(order.getId());
				stockLog.setOrderType(StockInOutOrderTypeEnum.purchase_reject_order.getValue());
				stockLog.setRemainNumber(inventoryStock.getStock());
				stockLog.setSpec1Id(orderGoods.getSpec1Id());
				stockLog.setSpecOption1Id(orderGoods.getSpecOption1Id());
				stockLog.setSpec2Id(orderGoods.getSpec2Id());
				stockLog.setSpecOption2Id(orderGoods.getSpecOption2Id());
				stockLog.setSpec3Id(orderGoods.getSpec3Id());
				stockLog.setSpecOption3Id(orderGoods.getSpecOption3Id());
				stockLog.setUnitId(orderGoods.getUnitId());
				stockLog.save();
			}
		}
	}

	/**
	 * 进货单退货
	 * @param 
	 * @param order
	 * @param orderIdList
	 * @param changeGoodsNumber 发生变更的数量
	 * @param _e 
	 * @return
	 */
	private void orderReject(PurchaseRejectOrder rejectOrder, PurchaseRejectOrderGoods orderGoods, BigDecimal subtractNumber, Set<Integer> orderIdList) {
		if(rejectOrder.getAuditStatus() != AuditStatusEnum.pass.getValue()) { // 非审核不处理
			return;
		}
		String[] orderIds = StringUtils.split(rejectOrder.getPurchaseOrderId(), ",");
		if(orderIds == null || orderIds.length <= 0) {
			return;
		}
		for (int i = 0; i < orderIds.length; i++) {
			if(StringUtils.isEmpty(orderIds[i])) {
				continue;
			}
			Integer orderId = Integer.parseInt(orderIds[i]);
			List<PurchaseOrderGoods> oderGoodsList = PurchaseOrderGoods.dao.findOrderGoods(orderId, orderGoods.getGoodsInfoId(), orderGoods.getSpec1Id(), orderGoods.getSpecOption1Id(), orderGoods.getSpec2Id(), orderGoods.getSpecOption2Id(), orderGoods.getSpec3Id(), orderGoods.getSpecOption3Id(), orderGoods.getUnitId());
			if(oderGoodsList == null || oderGoodsList.isEmpty()) {
				continue;
			}
			BigDecimal buyNumber =  orderGoods.getBuyNumber();
			BigDecimal totalRejectAmount = orderGoods.getAmount(); // 退货总金额
			for (PurchaseOrderGoods orderSameGoods : oderGoodsList) {
				if(buyNumber.compareTo(BigDecimal.ZERO) <= 0) {
					break;
				}
				BigDecimal changeNumber = orderSameGoods.getBuyNumber().subtract(orderSameGoods.getRejectNumber());
				if(changeNumber.compareTo(BigDecimal.ZERO) <= 0) { // 已转完成
					if(subtractNumber == null) {
						continue;
					} else {
						 // 老商品，比较下当前数量是不是小于之前的转换数量
						if(subtractNumber.compareTo(BigDecimal.ZERO) == 0) {
							continue;
						}
					}
				}
				if(subtractNumber == null) { // 新商品
					if(changeNumber.compareTo(buyNumber) > 0) { // 订单货品剩余可转数量和当前商品的数量比较，如果当前商品的数量大于剩余可转数量，则取剩余数量，否则取当前商品数量
						changeNumber = buyNumber;
					}
				} else {
					if(changeNumber.compareTo(subtractNumber) > 0) { // 剩余待转数量大于变更差额，
						changeNumber = subtractNumber;
					}
					subtractNumber = subtractNumber.subtract(changeNumber); // 差额要减少，知道差额为0，消化完毕
				}
				orderSameGoods.setRejectNumber(orderSameGoods.getRejectNumber().add(changeNumber));
				BigDecimal rejectAmount = orderSameGoods.getRejectNumber().multiply(orderSameGoods.getPrice());
				if(rejectAmount.compareTo(totalRejectAmount) < 0) {
					orderSameGoods.setRejectAmount(rejectAmount);
					totalRejectAmount = totalRejectAmount.subtract(rejectAmount); // 剩余的金额
					
				} else {
					orderSameGoods.setRejectAmount(totalRejectAmount);
					totalRejectAmount = BigDecimal.ZERO; // 剩余的金额 
				}
				orderSameGoods.update();
				
				buyNumber = buyNumber.subtract(changeNumber); // 剩余的数量
			}
			
			PurchaseOrder order = PurchaseOrder.dao.findById(orderId);
			List<PurchaseOrderGoods> orderGoodsList = order.getOrderGoodsList();
			BigDecimal countBuyNumber = BigDecimal.ZERO;
			BigDecimal countRejectNumber = BigDecimal.ZERO;
			BigDecimal countRejectAmount = BigDecimal.ZERO; // 退货总金额
			for (PurchaseOrderGoods e : orderGoodsList) {
				countBuyNumber = countBuyNumber.add(e.getBuyNumber());
				countRejectNumber = countRejectNumber.add(e.getRejectNumber());
				countRejectAmount = countRejectAmount.add(e.getRejectAmount());
			}
			if(countRejectNumber.compareTo(countBuyNumber) >= 0) {
				order.setRejectType(RejectTypeEnum.all.getValue());
			} else if(countRejectNumber.compareTo(BigDecimal.ZERO) > 0) {
				order.setRejectType(RejectTypeEnum.part.getValue());
			} else if(countRejectNumber.compareTo(BigDecimal.ZERO) == 0) {
				order.setRejectType(RejectTypeEnum.no.getValue());
			}
			order.setRejectAmount(countRejectAmount);
			order.setUpdatedAt(new Date());
			order.update();
			
			orderIdList.add(orderId);
			
			if(order.getRejectType() ==  RejectTypeEnum.no.getValue()) {
				return;
			}
			String operDesc = "全部商品退货";
			if(order.getRejectType() ==  RejectTypeEnum.part.getValue()) {
				operDesc = "部分商品退货";
			}
			purchaseOrderService.writeLog(orderId, order.getLastManId(), operDesc);
		}
	}
	
	/**
	 * 回退进货单退货
	 * @param 
	 * @param order
	 * @param orderGoods
	 */
	private void undoOrderReject(PurchaseRejectOrder rejectOrder, PurchaseRejectOrderGoods orderGoods) {
		String[] orderIds = StringUtils.split(rejectOrder.getPurchaseOrderId(), ",");
		if(orderIds == null || orderIds.length <= 0) {
			return;
		}
		for (int i = 0; i < orderIds.length; i++) {
			if(StringUtils.isEmpty(orderIds[i])) {
				continue;
			}
			Integer orderId = Integer.parseInt(orderIds[i]);
			List<PurchaseOrderGoods> orderSameGoodsList = PurchaseOrderGoods.dao.findOrderGoods(orderId, orderGoods.getGoodsInfoId(), orderGoods.getSpec1Id(), orderGoods.getSpecOption1Id(), orderGoods.getSpec2Id(), orderGoods.getSpecOption2Id(), orderGoods.getSpec3Id(), orderGoods.getSpecOption3Id(), orderGoods.getUnitId());
			if(orderSameGoodsList == null || orderSameGoodsList.isEmpty()) {
				continue;
			}
			BigDecimal buyNumber =  orderGoods.getBuyNumber();
			BigDecimal totalRejectAmount = orderGoods.getAmount();
			for (PurchaseOrderGoods orderSameGoods : orderSameGoodsList) {
				if(buyNumber.compareTo(BigDecimal.ZERO) <= 0) {
					break;
				}
				BigDecimal changeNumber = orderSameGoods.getRejectNumber();
				if(changeNumber.compareTo(buyNumber) > 0) {
					changeNumber = buyNumber;
				}
				orderSameGoods.setRejectNumber(orderSameGoods.getRejectNumber().subtract(changeNumber));
				BigDecimal rejectAmount = orderSameGoods.getRejectAmount();
				if(rejectAmount.compareTo(totalRejectAmount) < 0) {
					orderSameGoods.setRejectAmount(BigDecimal.ZERO);
					totalRejectAmount = totalRejectAmount.subtract(rejectAmount); // 剩余的金额
					
				} else {
					orderSameGoods.setRejectAmount(rejectAmount.subtract(totalRejectAmount));
					totalRejectAmount = BigDecimal.ZERO; // 剩余的金额 
				}
				orderSameGoods.update();
				
				buyNumber = buyNumber.subtract(changeNumber); // 剩余的数量
			}
			
			PurchaseOrder order = PurchaseOrder.dao.findById(orderId);
			List<PurchaseOrderGoods> orderGoodsList = order.getOrderGoodsList();
			BigDecimal countBuyNumber = BigDecimal.ZERO;
			BigDecimal countRejectNumber = BigDecimal.ZERO;
			BigDecimal countRejectAmount = BigDecimal.ZERO; // 退货总金额
			for (PurchaseOrderGoods e : orderGoodsList) {
				countBuyNumber = countBuyNumber.add(e.getBuyNumber());
				countRejectNumber = countRejectNumber.add(e.getRejectNumber());
				countRejectAmount = countRejectAmount.add(e.getRejectAmount());
			}
			if(countRejectNumber.compareTo(countBuyNumber) >= 0) {
				order.setRejectType(RejectTypeEnum.all.getValue());
			} else if(countRejectNumber.compareTo(BigDecimal.ZERO) > 0) {
				order.setRejectType(RejectTypeEnum.part.getValue());
			} else if(countRejectNumber.compareTo(BigDecimal.ZERO) == 0) {
				order.setRejectType(RejectTypeEnum.no.getValue());
			}
			order.setRejectAmount(countRejectAmount);
			order.setUpdatedAt(new Date());
			order.update();
			
			if(order.getRejectType() ==  RejectTypeEnum.all.getValue()) {
				return;
			}
			String operDesc = "全部取消退货";
			if(order.getRejectType() ==  RejectTypeEnum.part.getValue()) {
				operDesc = "部分商品取消退货";
			}
			purchaseOrderService.writeLog(orderId, order.getLastManId(), operDesc);
		}
	}
	
	/**
	 * 设置单据审核状态
	 * @param 
	 * @param order
	 */
	private AuditStatusEnum getAuditStatus(PurchaseRejectOrder order) {
		TenantConfig auditConfig = TenantConfig.dao.findByKeyCache(TenantConfigEnum.audit_purchase_reject_order);
		if (Boolean.parseBoolean(auditConfig.getAttrValue())) { // 需要审核
			 return AuditStatusEnum.waiting;
		} else {
			if (order.getOrderStatus() == null || order.getOrderStatus() == OrderStatusEnum.normal.getValue()) { // 草稿不能自动审核
				return AuditStatusEnum.pass;
			} else {
				return AuditStatusEnum.waiting;
			}
		}
	}
	
	/**
	 * 获取单据物流状态
	 * @param 
	 * @param order
	 * @return
	 */
	private LogisticsStatusEnum getLogisticsStatus(PurchaseRejectOrder order) {
		if(order.getAuditStatus() == AuditStatusEnum.pass.getValue()) { // 已审核单即入库了
			return LogisticsStatusEnum.stockout;
		} else {
			return LogisticsStatusEnum.waiting;
		}
	}

	/**
	 * 发送审核结果通知
	 * @param 
	 * @param order
	 */
	private void sendAuditNoticeMsg(PurchaseRejectOrder order) {
		SupplierInfo supplierInfo = order.getSupplierInfo();
		String title = "供应商“" + supplierInfo.getName() + "”进货退货单审核通过";
		String content = title;
		if (order.getAuditStatus() == AuditStatusEnum.reject.getValue()) {
			title = "供应商“" + supplierInfo.getName() + "”进货退货单审核拒绝";
			content = title;
		}
		Boolean smsFlag = PurchaseRejectOrder.dao.findAuditHandlerSmsNoticeConfig();// 发送短信
		Boolean sysFlag = PurchaseRejectOrder.dao.findAuditHandlerSysNoticeConfig();// 发送系统消息
		sendNoticeMsg(MsgDataTypeEnum.purchase_reject_order_audit, title, content, order.getHandlerId(), smsFlag, sysFlag);

		smsFlag = PurchaseRejectOrder.dao.findAuditMakeManSmsNoticeConfig();// 发送短信
		sysFlag = PurchaseRejectOrder.dao.findAuditMakeManSysNoticeConfig();// 发送系统消息
		sendNoticeMsg(MsgDataTypeEnum.purchase_reject_order_audit, title, content, order.getMakeManId(), smsFlag, sysFlag);
	}
	
	/**
	 * 发送消息给审核人
	 * @param 
	 * @param order
	 */
	private void sendOrderNoticeMsg(PurchaseRejectOrder order) {
		if(order.getAuditStatus() != AuditStatusEnum.waiting.getValue() || order.getOrderStatus() != OrderStatusEnum.normal.getValue()) {
			return;
		}
		Integer	auditorId = PurchaseRejectOrder.dao.findAuditorIdConfig();
		order.setAuditorId(auditorId); // 设置审核人
		Boolean smsFlag = PurchaseRejectOrder.dao.findAuditSmsNoticeConfig();// 发送短信
		Boolean sysFlag = PurchaseRejectOrder.dao.findAuditSysNoticeConfig();// 发送系统消息
		SupplierInfo supplierInfo = order.getSupplierInfo();
		String title="供应商“"+supplierInfo.getName()+"”进货退货单审核通知";
		String content = title;
		sendNoticeMsg(MsgDataTypeEnum.purchase_reject_order_audit, title, content, order.getAuditorId(), smsFlag, sysFlag);
	}

	/**
	 * 供应商应收对账
	 * @param 
	 * @param order
	 */
	private void updateSupplierPayable(PurchaseRejectOrder order) {
		if(order.getAuditStatus() != AuditStatusEnum.pass.getValue()) { // 非审核不处理资金
			return;
		}
		TraderSupplierPayable supplierPayable = TraderSupplierPayable.dao.findByOrderId(CheckingRefOrderTypeEnum.purchase_reject_order, order.getId());
		if(supplierPayable != null) {
			if(order.getOrderStatus() == OrderStatusEnum.disable.getValue()) {
				supplierPayable.delete();
				return;
			}
			BigDecimal discountAmount = order.getGoodsAmount().subtract(order.getDiscountAmount()).add(order.getOddAmount()); // 计算优惠金额
			supplierPayable.setNewAmount(order.getAmount().add(discountAmount));
			supplierPayable.setTakeAmount(order.getPaidAmount());
			supplierPayable.setDiscountAmount(discountAmount);
			supplierPayable.setAdjustAmount(BigDecimal.ZERO);
			supplierPayable.setOrderTime(order.getOrderTime());
			supplierPayable.setUpdatedAt(new Date());
			supplierPayable.update();
			
		} else {
			BigDecimal discountAmount = order.getGoodsAmount().subtract(order.getDiscountAmount()).add(order.getOddAmount()); // 计算优惠金额
			SupplierInfo supplierInfo = SupplierInfo.dao.findById(order.getSupplierInfoId());
			supplierPayable = new TraderSupplierPayable();
			supplierPayable.setRefOrderCode(order.getOrderCode());
			supplierPayable.setRefOrderId(order.getId());
			supplierPayable.setRefOrderType(CheckingRefOrderTypeEnum.purchase_reject_order.getValue());
			supplierPayable.setSupplierInfoId(order.getSupplierInfoId());
			supplierPayable.setTraderBookAccountId(supplierInfo.getTraderBookAccountId());
			supplierPayable.setDiscountAmount(order.getDiscountAmount());
			supplierPayable.setNewAmount(order.getAmount().add(discountAmount));
			supplierPayable.setTakeAmount(order.getPaidAmount());
			supplierPayable.setDiscountAmount(discountAmount);
			supplierPayable.setAdjustAmount(BigDecimal.ZERO);
			supplierPayable.setOrderTime(order.getOrderTime());
			supplierPayable.setCreatedAt(new Date());
			supplierPayable.setUpdatedAt(new Date());
			supplierPayable.save();
		}
	}



}