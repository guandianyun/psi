package com.bytechainx.psi.purchase.service;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.bytechainx.psi.common.EnumConstant.AuditStatusEnum;
import com.bytechainx.psi.common.EnumConstant.CheckingRefOrderTypeEnum;
import com.bytechainx.psi.common.EnumConstant.DataStatusEnum;
import com.bytechainx.psi.common.EnumConstant.ExportStatusEnum;
import com.bytechainx.psi.common.EnumConstant.FundFlowEnum;
import com.bytechainx.psi.common.EnumConstant.FundTypeEnum;
import com.bytechainx.psi.common.EnumConstant.LogisticsStatusInEnum;
import com.bytechainx.psi.common.EnumConstant.MsgDataTypeEnum;
import com.bytechainx.psi.common.EnumConstant.OrderPayStatusEnum;
import com.bytechainx.psi.common.EnumConstant.OrderStatusEnum;
import com.bytechainx.psi.common.EnumConstant.RefOrderTypeEnum;
import com.bytechainx.psi.common.EnumConstant.RejectTypeEnum;
import com.bytechainx.psi.common.EnumConstant.StockInOutOrderTypeEnum;
import com.bytechainx.psi.common.EnumConstant.StockIoTypeEnum;
import com.bytechainx.psi.common.EnumConstant.StockWarnTypeEnum;
import com.bytechainx.psi.common.EnumConstant.TenantConfigEnum;
import com.bytechainx.psi.common.kit.DateUtil;
import com.bytechainx.psi.common.kit.excel.XlsKit;
import com.bytechainx.psi.common.model.GoodsInfo;
import com.bytechainx.psi.common.model.GoodsPrice;
import com.bytechainx.psi.common.model.GoodsUnit;
import com.bytechainx.psi.common.model.InventoryStock;
import com.bytechainx.psi.common.model.InventoryStockLog;
import com.bytechainx.psi.common.model.PurchaseOrder;
import com.bytechainx.psi.common.model.PurchaseOrderCost;
import com.bytechainx.psi.common.model.PurchaseOrderFee;
import com.bytechainx.psi.common.model.PurchaseOrderFund;
import com.bytechainx.psi.common.model.PurchaseOrderGoods;
import com.bytechainx.psi.common.model.PurchaseOrderLog;
import com.bytechainx.psi.common.model.SupplierInfo;
import com.bytechainx.psi.common.model.TenantAdmin;
import com.bytechainx.psi.common.model.TenantConfig;
import com.bytechainx.psi.common.model.TenantExportLog;
import com.bytechainx.psi.common.model.TraderBalanceAccount;
import com.bytechainx.psi.common.model.TraderBookAccount;
import com.bytechainx.psi.common.model.TraderBookAccountLogs;
import com.bytechainx.psi.common.model.TraderFundOrder;
import com.bytechainx.psi.common.model.TraderFundType;
import com.bytechainx.psi.common.model.TraderPayOrderRef;
import com.bytechainx.psi.common.model.TraderReceiptOrder;
import com.bytechainx.psi.common.model.TraderSupplierPayable;
import com.bytechainx.psi.common.service.base.CommonService;
import com.google.common.collect.Lists;
import com.jfinal.kit.Kv;
import com.jfinal.kit.Ret;
import com.jfinal.kit.ThreadPoolKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;


/**
* 进货单
*/
public class PurchaseOrderService extends CommonService {
	
	/**
	* 分页列表
	*/
	public Page<PurchaseOrder> paginate(String startDay, String endDay, Kv conditionColumns, Integer pageNumber, int pageSize) {
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

		return PurchaseOrder.dao.paginate(pageNumber, pageSize, "select * ", "from purchase_order "+where.toString()+" order by id desc", params.toArray());
	}


	/**
	* 新增
	*/
	public Ret create(PurchaseOrder order, List<PurchaseOrderGoods> orderGoodList, List<PurchaseOrderFund> orderFundList, List<PurchaseOrderFee> orderFeeList, List<PurchaseOrderCost> orderCostList) {
		if( order == null) {
			return Ret.fail("参数错误");
		}
		if(order.getSupplierInfoId() == null || order.getSupplierInfoId() <= 0) {
			return Ret.fail("供应商不能为空");
		}
		if(order.getHandlerId() == null || order.getHandlerId() <= 0) {
			return Ret.fail("进货员不能为空");
		}
		if(order.getAmount() == null) {
			return Ret.fail("应付金额不能为空");
		}
		if(order.getDiscountAmount() == null) {
			return Ret.fail("折后应付不能为空");
		}
		if(order.getOrderTime() == null) {
			return Ret.fail("单据时间不能为空");
		}
		if(orderGoodList == null || orderGoodList.isEmpty()) {
			return Ret.fail("订单商品不能为空"); 
		}
		BigDecimal paidAmount = BigDecimal.ZERO; // 实付已付金额
		BigDecimal balanceAmount = BigDecimal.ZERO; // 余额支付的总金额
		if(orderFundList != null && !orderFundList.isEmpty()) {
			for (PurchaseOrderFund e : orderFundList) {
				paidAmount = paidAmount.add(e.getAmount());
				if((e.getId() == null || e.getId().intValue() <= 0) && e.getPayType() == FundTypeEnum.balance.getValue()) {
					balanceAmount = balanceAmount.add(e.getAmount());
				}
			}
		}
		if(paidAmount.compareTo(order.getAmount()) > 0) {
			return Ret.fail("实付不能大于应付金额"); 
		}
		if(balanceAmount.compareTo(BigDecimal.ZERO) > 0) { // 存在余额支付，则判断下余额支付的金额是否大于可用资金
			SupplierInfo supplierInfo = SupplierInfo.dao.findById(order.getSupplierInfoId());
			if(balanceAmount.compareTo(supplierInfo.getAvailableAmount()) > 0) {
				return Ret.fail("余额扣款的金额不足"); 
			}
		}
		
		// 设置审核状态
		AuditStatusEnum auditStatus = getAuditStatus(order);
		order.setAuditStatus(auditStatus.getValue());
		// 设置出入库状态
		LogisticsStatusInEnum logisticsStatus = getLogisticsStatus(order);
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
		
		// 保存商品
		for (PurchaseOrderGoods e : orderGoodList) {
			if(e.getGoodsInfoId() == null || e.getGoodsInfoId() <= 0 || e.getBuyNumber().doubleValue() <= 0) {
				continue;
			}
			e.setSupplierInfoId(order.getSupplierInfoId());
			e.setCreatedAt(new Date());
			e.setUpdatedAt(new Date());
			e.setPurchaseOrderId(order.getId());
			e.save();
		}
		
		// 更新商品库存
		updateInventoryStock(order, orderGoodList);
		// 更新商品成本均价
		updateGoodsPrice(order, orderGoodList);
		
		// 订单支付资金
		if(orderFundList != null && !orderFundList.isEmpty()) {
			for (PurchaseOrderFund e : orderFundList) {
				paymentOrderFund(order, e, false);
			}
		}
		// 货款计入收入
		goodsInTraderBookAccount(order, order.getAmount());
		// 记录对账单
		updateSupplierPayable(order);
		// 记录其他费用，需要支付给供应商的
		updateOrderFee(order, orderFeeList);
		// 记录支出成本，自己本身的支出
		updateOrderCost(order, orderCostList);
		// 更新货品的告警状态，FIXME 要不要判断状态，减少无效操作
		updateStockWarn(order.getOrderGoodsList());
		
		// 记录订单操作
		writeLog(order.getId(), order.getLastManId(), "创建进货单");
		
		return Ret.ok("新增进货单成功").set("targetId", order.getId());
	}


	/**
	* 修改
	*/
	public Ret update(PurchaseOrder order, List<PurchaseOrderGoods> orderGoodList, List<PurchaseOrderFund> orderFundList, List<PurchaseOrderFee> orderFeeList, List<PurchaseOrderCost> orderCostList) {
		if(order == null || order.getId() == null || order.getId() <= 0) {
			return Ret.fail("参数错误");
		}
		if(order.getSupplierInfoId() == null || order.getSupplierInfoId() <= 0) {
			return Ret.fail("供应商不能为空");
		}
		if(order.getHandlerId() == null || order.getHandlerId() <= 0) {
			return Ret.fail("进货员不能为空");
		}
		if(order.getAmount() == null || order.getAmount().doubleValue() <= 0) {
			return Ret.fail("应付金额不能为空");
		}
		if(order.getDiscountAmount() == null || order.getDiscountAmount().doubleValue() <= 0) {
			return Ret.fail("折后应付不能为空");
		}
		if(order.getOrderTime() == null) {
			return Ret.fail("单据时间不能为空");
		}
		if(orderGoodList == null || orderGoodList.isEmpty()) {
			return Ret.fail("订单商品不能为空"); 
		}
		
		BigDecimal paidAmount = BigDecimal.ZERO; // 实付已付金额
		BigDecimal balanceAmount = BigDecimal.ZERO; // 余额支付的总金额
		if(orderFundList != null && !orderFundList.isEmpty()) {
			for (PurchaseOrderFund e : orderFundList) {
				paidAmount = paidAmount.add(e.getAmount());
				if((e.getId() == null || e.getId().intValue() <= 0) && e.getPayType() == FundTypeEnum.balance.getValue()) {
					balanceAmount = balanceAmount.add(e.getAmount());
				}
			}
		}
		if(paidAmount.compareTo(order.getAmount()) > 0) {
			return Ret.fail("实付不能大于应付金额"); 
		}
		if(balanceAmount.compareTo(BigDecimal.ZERO) > 0) { // 存在余额支付，则判断下余额支付的金额是否大于可用资金
			SupplierInfo supplierInfo = SupplierInfo.dao.findById(order.getSupplierInfoId());
			if(balanceAmount.compareTo(supplierInfo.getAvailableAmount()) > 0) {
				return Ret.fail("余额扣款的金额不足"); 
			}
		}
		
		PurchaseOrder _order = PurchaseOrder.dao.findById(order.getId());
		if(_order == null) {
			return Ret.fail("进货单不存在，无法修改");
		}
		if(_order.getRejectType() != RejectTypeEnum.no.getValue()) {
			return Ret.fail("进货单已产生退货，无法修改");
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
			LogisticsStatusInEnum logisticsStatus = getLogisticsStatus(order);
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
		// 更新订单付款资金
		boolean fundModifyFlag = updateOrderFund(order, orderFundList, false);
		// 货款计入收入
		goodsInTraderBookAccount(order, changeAmount);
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
			
		return Ret.ok("修改进货单成功").set("targetId", order.getId());
	}

	/**
	* 作废
	*/
	public Ret disable(List<Integer> ids) {
		if(ids == null || ids.isEmpty()) {
			return Ret.fail("参数错误");
		}
		for (Integer id : ids) {
			PurchaseOrder order = PurchaseOrder.dao.findById(id);
			if(order == null) {
				continue;
			}
			if(order.getOrderStatus() == OrderStatusEnum.disable.getValue()) { // 已作废的单据不能重复作废
				continue;
			}
			if(order.getRejectType() != RejectTypeEnum.no.getValue()) { // 有退货，则不能作废
				continue;
			}
			
			// 商品库存回退
			undoInventoryStock(order, order.getOrderGoodsList());
			
			if(order.getAuditStatus() != AuditStatusEnum.pass.getValue()) { // 非已审核的不执行以下资金回退操作
				order.setLogisticsStatus(LogisticsStatusInEnum.waiting.getValue());
				order.setAuditStatus(AuditStatusEnum.waiting.getValue());
				order.setOrderStatus(OrderStatusEnum.disable.getValue());
				order.setUpdatedAt(new Date());
				order.update();
				
				continue;
			}
			// 处理付款单核销
			for (TraderPayOrderRef ref : order.getPayOrderRefList()) {
				// 回退优惠金额
				order.setAmount(order.getAmount().add(ref.getDiscountAmount()));
				order.setOddAmount(order.getOddAmount().subtract(ref.getDiscountAmount()));
				order.setPaidAmount(order.getPaidAmount().subtract(ref.getAmount()));
				if (order.getPaidAmount().compareTo(BigDecimal.ZERO) <= 0) { // 未付
					order.setPayStatus(OrderPayStatusEnum.no.getValue());

				} else if (order.getPaidAmount().compareTo(order.getAmount()) < 0) { // 部分付
					order.setPayStatus(OrderPayStatusEnum.part.getValue());
				}
				order.setUpdatedAt(new Date());
				order.update();

				// 删除销售单核销的资金明细
				Db.delete("delete from purchase_order_fund where purchase_order_id = ? and receipt_type = ? and balance_account_id = ?", order.getId(), FundTypeEnum.checking.getValue(),
						ref.getTraderPayOrderId());

				TraderReceiptOrder receiptOrder = ref.getTraderPayOrder();
				receiptOrder.setDiscountAmount(receiptOrder.getDiscountAmount().subtract(ref.getDiscountAmount()));
				receiptOrder.setOrderAmount(receiptOrder.getOrderAmount().subtract(ref.getAmount()));
				receiptOrder.setCheckAmount(receiptOrder.getOrderAmount().subtract(receiptOrder.getDiscountAmount()));
				receiptOrder.update();

				// 回退往来帐户支付金额
				fundPayTraderBookAccount(order, BigDecimal.ZERO.subtract(ref.getAmount()));

				ref.delete();
			}
			// 订单资金回退
			for (PurchaseOrderFund e : order.getOrderFundList()) {
				// 根据支付方式，回退资金
				undoOrderFund(order, e);
			}
			// 删除交易流水
			Db.delete("delete from trader_fund_order where tenant_store_id = ? and ref_order_type = ? and ref_order_id = ?", RefOrderTypeEnum.purchase_order.getValue(), order.getId());
			// 货款收入回退
			goodsInTraderBookAccount(order, BigDecimal.ZERO.subtract(order.getAmount()));
			// 记录对账单
			updateSupplierPayable(order);
			
			order.setLogisticsStatus(LogisticsStatusInEnum.waiting.getValue());
			order.setAuditStatus(AuditStatusEnum.waiting.getValue());
			order.setOrderStatus(OrderStatusEnum.disable.getValue());
			order.setUpdatedAt(new Date());
			order.update();
			
			// 更新货品的告警状态，FIXME 要不要判断状态，减少无效操作
			updateStockWarn(order.getOrderGoodsList());
			
			// 记录订单操作
			writeLog(order.getId(), order.getLastManId(), "作废进货单");
		}
		return Ret.ok("作废进货单成功");
	}
	
	
	/**
	 * 进货单付款
	 * @param 
	 * @param orderId
	 * @param orderFundList
	 * @return
	 */
	public Ret payment(Integer orderId, List<PurchaseOrderFund> orderFundList) {
		if(orderId == null || orderId <= 0 || orderFundList == null || orderFundList.isEmpty()) {
			return Ret.fail("参数错误");
		}
		PurchaseOrder order = PurchaseOrder.dao.findById(orderId);
		if(order == null) {
			return Ret.fail("进货单不存在，无法修改");
		}
		if(order.getAuditStatus() != AuditStatusEnum.pass.getValue()) {
			return Ret.fail("进货单未通过审核，无法付款");
		}
		if(order.getOrderStatus() != OrderStatusEnum.normal.getValue()) {
			return Ret.fail("非正常进货单，无法付款");
		}
		if(order.getPayStatus() == OrderPayStatusEnum.finish.getValue()) {
			return Ret.fail("进货单已付清，无需付款");
		}
		// 账户付款，更新结算帐户资金
		for (PurchaseOrderFund e : orderFundList) {
			// 支付资金，生成记录
			paymentOrderFund(order, e, false);
		}
		
		return Ret.ok("进货单付款成功");
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
			PurchaseOrder order = PurchaseOrder.dao.findById(id);
			if(order == null) {
				continue;
			}
			if(order.getAuditStatus() != AuditStatusEnum.waiting.getValue()) { // 已审核的订单不能重复审核
				continue;
			}
			order.setAuditorId(auditorId);
			order.setAuditDesc(auditDesc);
			order.setAuditStatus(auditStatus.getValue());
			order.setAuditTime(new Date());
			
			// 设置出入库状态
			LogisticsStatusInEnum logisticsStatus = getLogisticsStatus(order);
			order.setLogisticsStatus(logisticsStatus.getValue());
			
			order.setUpdatedAt(new Date());
			order.update();
			
			if(auditStatus.getValue() == AuditStatusEnum.pass.getValue()) { // 审核通过
				// 商品入库，库存增加，如果开通了单独出入库，审核通过也不会增加库存
				List<PurchaseOrderGoods> orderGoodList = order.getOrderGoodsList();
				// 更新商品库存
				updateInventoryStock(order, orderGoodList);
				// 更新商品成本均价
				updateGoodsPrice(order, orderGoodList);
				// 审核处理资金，可能存在多次审核, 审核拒绝无需处理资金
				updateOrderFund(order, order.getOrderFundList(), true);
				// 往来账户
				goodsInTraderBookAccount(order, order.getAmount());
				// 记录对账单
				updateSupplierPayable(order);
			}
			
			// 更新货品的告警状态，FIXME 要不要判断状态，减少无效操作
			updateStockWarn(order.getOrderGoodsList());
			
			// 写入操作记录
			writeLog(order.getId(), order.getLastManId(), "审核进货单:"+auditStatus.getName());
			// 发送审核结果通知
			sendAuditNoticeMsg(order);
		}
		return Ret.ok("进货单审核成功");
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
		PurchaseOrder order = PurchaseOrder.dao.findById(id);
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
	 * 供应商应付对账
	 * @param 
	 * @param order
	 */
	private void updateSupplierPayable(PurchaseOrder order) {
		if(order.getAuditStatus() != AuditStatusEnum.pass.getValue()) { // 非审核不处理资金
			return;
		}
		TraderSupplierPayable supplierPayable = TraderSupplierPayable.dao.findByOrderId(CheckingRefOrderTypeEnum.purchase_order, order.getId());
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
			supplierPayable.setRefOrderType(CheckingRefOrderTypeEnum.purchase_order.getValue());
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


	/**
	 * 记录单据其他支出成本，不用支付给供应商
	 * @param 
	 * @param order
	 * @param orderCostList
	 */
	private void updateOrderCost(PurchaseOrder order, List<PurchaseOrderCost> orderCostList) {
		if(orderCostList == null) {
			orderCostList = new ArrayList<>();
		}
		List<PurchaseOrderCost> oldOrderFeeList = PurchaseOrderCost.dao.find("select * from purchase_order_cost where purchase_order_id = ?", order.getId());
		// 先过滤出已删除的记录
		List<PurchaseOrderCost> deleteFeeList = new ArrayList<>();
		for(PurchaseOrderCost oldFee : oldOrderFeeList) {
			boolean isExist = false;
			for (PurchaseOrderCost fee : orderCostList) {
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
		
		for (PurchaseOrderCost e : deleteFeeList) {
			// 删除资金记录
			e.delete();
		}
		BigDecimal totalOtherCostAmount = BigDecimal.ZERO;
		for (PurchaseOrderCost orderFee : orderCostList) {
			if(orderFee.getAmount() == null || orderFee.getAmount().compareTo(BigDecimal.ZERO) == 0 || orderFee.getTraderFundType() == null || orderFee.getTraderFundType() <= 0) {
				continue;
			}
			PurchaseOrderCost _orderFee = PurchaseOrderCost.dao.findFirst("select * from purchase_order_cost where purchase_order_id = ? and trader_fund_type = ? limit 1", order.getId(), orderFee.getTraderFundType());
			if(_orderFee == null) {
				orderFee.setPurchaseOrderId(order.getId());
				orderFee.save();
			} else {
				orderFee.update();
			}
			totalOtherCostAmount = totalOtherCostAmount.add(orderFee.getAmount());
		}
		order.setOtherCostAmount(totalOtherCostAmount);
		order.update();
	}


	/**
	 * 记录其他费用，需要支付给供应商
	 * @param 
	 * @param order
	 * @param orderFeeList
	 */
	private void updateOrderFee(PurchaseOrder order, List<PurchaseOrderFee> orderFeeList) {
		if(orderFeeList == null) {
			orderFeeList = new ArrayList<>();
		}
		List<PurchaseOrderFee> oldOrderFeeList = PurchaseOrderFee.dao.find("select * from purchase_order_fee where purchase_order_id = ?", order.getId());
		// 先过滤出已删除的记录
		List<PurchaseOrderFee> deleteFeeList = new ArrayList<>();
		for(PurchaseOrderFee oldFee : oldOrderFeeList) {
			boolean isExist = false;
			for (PurchaseOrderFee fee : orderFeeList) {
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
		
		for (PurchaseOrderFee e : deleteFeeList) {
			// 删除资金记录
			e.delete();
		}
		BigDecimal totalOtherAmount = BigDecimal.ZERO;
		for (PurchaseOrderFee orderFee : orderFeeList) {
			if(orderFee.getAmount() == null || orderFee.getAmount().compareTo(BigDecimal.ZERO) == 0 || orderFee.getTraderFundType() == null || orderFee.getTraderFundType() <= 0) {
				continue;
			}
			PurchaseOrderFee _orderFee = PurchaseOrderFee.dao.findFirst("select * from purchase_order_fee where purchase_order_id = ? and trader_fund_type = ? limit 1", order.getId(), orderFee.getTraderFundType());
			if(_orderFee == null) {
				orderFee.setPurchaseOrderId(order.getId());
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
		PurchaseOrderLog log = new PurchaseOrderLog();
		log.setOperAdminId(operAdminId);
		if(operAdminId != null && operAdminId > 0) {
			TenantAdmin admin = TenantAdmin.dao.findById(operAdminId);
			log.setOperAdminName(admin.getRealName());
		} else {
			log.setOperAdminName("系统后台");
		}
		log.setOperDesc(operDesc);
		log.setOperTime(new Date());
		log.setPurchaseOrderId(orderId);
		log.save();
		
		return Ret.ok();
	}
	
	/**
	 * 导出数据xls
	 */
	public Ret export(Integer handlerId, String startDay, String endDay, Kv condKv) {
		String fileName = "进货单-"+DateUtil.getSecondNumber(new Date());
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
					List<PurchaseOrder> orderList = Lists.newArrayList();
					int pageNumber = 1;
					Page<PurchaseOrder> page = paginate(startDay, endDay, condKv, pageNumber, 500);
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
					
					headersBuffer.append(",供应商名称,入库仓库,商品名称,商品条码,规格,单位");
					columnsBuffer.append(",supplier_info_nam,list,discount");

					headersBuffer.append(",数量,单价,商品折扣(%),折后单价,金额,备注,整单折扣率(%)");
					
					List<TraderFundType> feeList = PurchaseOrder.dao.findFeeConfig(); // 其他费用配置
					if(feeList != null) {
						for (TraderFundType fee : feeList) {
							headersBuffer.append("," + fee.getName());
							columnsBuffer.append(",fee_amount_" + fee.getId());
						}
					}
					headersBuffer.append(",应付金额,已付金额");
					columnsBuffer.append(",amount,paid_amount");
					
					List<TraderFundType> costList = PurchaseOrder.dao.findCostConfig(); // 成本支出配置
					if(costList != null) {
						for (TraderFundType cost : costList) {
							headersBuffer.append("," + cost.getName());
							columnsBuffer.append(",cost_amount_" + cost.getId());
						}
					}
					headersBuffer.append(",单据备注,进货员");
					columnsBuffer.append(",remark,handler_name");
					
					listColumnsBuffer.append(",buy_number,price,discount,discount_amount,amount,remark");
					
					String[] headers = headersBuffer.toString().split(",");
					String[] columns = columnsBuffer.toString().split(",");
					String[] listColumns = listColumnsBuffer.toString().split(",");
							
					for (PurchaseOrder order : orderList) {
						order.put("order_time_day", DateUtil.getDayStr(order.getOrderTime()));
						
						List<PurchaseOrderGoods> goodsList = order.getOrderGoodsList();
						order.put("list", goodsList);
						for (PurchaseOrderGoods orderGoods : goodsList) {
							GoodsInfo goodsInfo = orderGoods.getGoodsInfo();
							orderGoods.put("goods_name", goodsInfo.getName());
							orderGoods.put("bar_code", goodsInfo.getBarCode());
							orderGoods.put("spec_name", orderGoods.getGoodsSpecNames());
							orderGoods.put("unit_name", orderGoods.getGoodsUnit().getName());
						}
						List<PurchaseOrderFee> orderFeeList = order.getOrderFeeList();
						for(PurchaseOrderFee fee: orderFeeList) {
							order.put("fee_amount_"+fee.getTraderFundType(), fee.getAmount());
						}
						List<PurchaseOrderCost> orderCostList = order.getOrderCostList();
						for(PurchaseOrderCost cost: orderCostList) {
							order.put("cost_amount_"+cost.getTraderFundType(), cost.getAmount());
						}
						
						order.put("handler_name", order.getHandler().getRealName());
					}
					
					String filePath = XlsKit.genOrderXls("进货单", fileName, headers, orderList, columns, listColumns);
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
	 * @param auditFlag 是否审核操作，审核通过不能按修改的行为处理。
	 * @return 
	 */
	private boolean updateOrderFund(PurchaseOrder order, List<PurchaseOrderFund> orderFundList, boolean auditFlag) {
		boolean modifyFlag = false; // 是否存在数据修改
		List<PurchaseOrderFund> oldOrderFundList = PurchaseOrderFund.dao.find("select * from purchase_order_fund where purchase_order_id = ?", order.getId());
		// 先过滤出已删除的记录
		List<PurchaseOrderFund> deleteFundList = new ArrayList<>();
		for(PurchaseOrderFund oldFund : oldOrderFundList) {
			boolean isExist = false;
			for (PurchaseOrderFund fund : orderFundList) {
				if(fund.getId() != null && fund.getId().intValue() == oldFund.getId().intValue()) {
					isExist = true;
					break;
				}
			}
			if(!isExist) { // 需要删除的记录
				deleteFundList.add(oldFund);
			}
		}
		
		for (PurchaseOrderFund e : deleteFundList) {
			// 回退资金到帐户
			undoOrderFund(order, e);
			// 删除资金记录
			e.delete();
			modifyFlag = true;
		}
		
		// 账户付款，更新结算帐户资金
		for (PurchaseOrderFund e : orderFundList) {
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
	private void undoOrderFund(PurchaseOrder order, PurchaseOrderFund orderFund) {
		if(order.getAuditStatus() != AuditStatusEnum.pass.getValue()) { // 非审核不处理资金
			return;
		}
		if(orderFund.getPayType() == FundTypeEnum.cash.getValue()) { // 本单支付，退回到结算帐户
			TraderBalanceAccount balanceAccount = TraderBalanceAccount.dao.findById(orderFund.getBalanceAccountId());
			balanceAccount.setBalance(balanceAccount.getBalance().add(orderFund.getAmount())); // 回退支付的金额
			balanceAccount.setUpdatedAt(new Date());
			balanceAccount.update();
			
			BigDecimal changeAmount = BigDecimal.ZERO.subtract(orderFund.getAmount()); // 回退支付的金额，支付的时候是增加，退款的时候是减少
			fundOutTraderBookAccount(order, changeAmount);
			
		} else if(orderFund.getPayType() == FundTypeEnum.balance.getValue()) { // 余额支付，退回到往来帐户
			BigDecimal changeAmount = BigDecimal.ZERO.subtract(orderFund.getAmount()); // 支付的时候是增加，退款的时候减少
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
	 * @return
	 */
	private boolean paymentOrderFund(PurchaseOrder order, PurchaseOrderFund orderFund, boolean auditFlag) {
		boolean modifyFlag = false; // 是否存在数据修改
		if(orderFund.getBalanceAccountId() == null || orderFund.getBalanceAccountId() <= 0 || orderFund.getAmount() == null || orderFund.getAmount().doubleValue() <= 0) {
			return modifyFlag;
		}
		BigDecimal changeFundAmount = orderFund.getAmount(); // 变更的金额
		PurchaseOrderFund _orderFund = PurchaseOrderFund.dao.findFirst("select * from purchase_order_fund where purchase_order_id = ? and id = ?", order.getId(), orderFund.getId());
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
				
				modifyFlag = true;
			}
			
		} else {
			orderFund.setPurchaseOrderId(order.getId());
			orderFund.setUpdatedAt(new Date());
			orderFund.setCreatedAt(new Date());
			orderFund.save();
			
			modifyFlag = true;
		}
		if(order.getAuditStatus() != AuditStatusEnum.pass.getValue()) { // 不是已审核状态，不处理资金
			return modifyFlag;
		}
		if(auditFlag) { // 审核操作，变更的金额即为原来支付的金额，因为审核时，金额不会发生变化
			changeFundAmount = orderFund.getAmount(); // 变更的金额
		}
		if(orderFund.getPayType() == FundTypeEnum.cash.getValue()) { // 本单支付，从结算帐户支付
			TraderBalanceAccount balanceAccount = TraderBalanceAccount.dao.findById(orderFund.getBalanceAccountId());
			balanceAccount.setBalance(balanceAccount.getBalance().subtract(changeFundAmount)); // 减去支付的金额
			balanceAccount.setUpdatedAt(new Date());
			balanceAccount.update();
			
			// 更新往来帐户资金
			fundOutTraderBookAccount(order, changeFundAmount);
			
		} else if(orderFund.getPayType() == FundTypeEnum.balance.getValue()) { // 余额支付，从往来帐户支付
			fundPayTraderBookAccount(order, changeFundAmount);
		}
		
		// 生成交易流水
		TraderFundOrder fundOrder = TraderFundOrder.dao.findByOrderId(orderFund.getBalanceAccountId(), RefOrderTypeEnum.purchase_order, order.getId());
		if(fundOrder != null) { // 存在，则更新
			fundOrder.setAmount(orderFund.getAmount());
			fundOrder.setOrderTime(new Date());
			fundOrder.setUpdatedAt(new Date());
			fundOrder.update();
			
		} else { // 不存在，则新增
			fundOrder = new TraderFundOrder();
			fundOrder.setAmount(orderFund.getAmount());
			fundOrder.setFundFlow(FundFlowEnum.expenses.getValue());
			fundOrder.setOrderTime(new Date());
			fundOrder.setRefOrderCode(order.getOrderCode());
			fundOrder.setRefOrderId(order.getId());
			fundOrder.setRefOrderType(RefOrderTypeEnum.purchase_order.getValue());
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
	 * 更新往来帐户资金：给供应商支付资金
	 * @param 
	 * @param order
	 * @param changeAmount
	 */
	private void fundOutTraderBookAccount(PurchaseOrder order, BigDecimal changeAmount) {
		if(order.getAuditStatus() != AuditStatusEnum.pass.getValue()) { // 非审核不处理资金
			return;
		}
		if(changeAmount.compareTo(BigDecimal.ZERO) == 0) {
			return;
		}
		// 更新往来账户金额
		SupplierInfo supplierInfo = SupplierInfo.dao.findById(order.getSupplierInfoId());
		TraderBookAccount bookAccount = supplierInfo.getTraderBookAccount();
		BigDecimal outAmount = bookAccount.getOutAmount().add(changeAmount);
		bookAccount.setOutAmount(outAmount);
		BigDecimal payAmount = bookAccount.getPayAmount().add(changeAmount);
		bookAccount.setPayAmount(payAmount);
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
	 * 更新往来帐户资金：供应商平账资金
	 * @param 
	 * @param order
	 * @param changeAmount
	 */
	private void fundPayTraderBookAccount(PurchaseOrder order, BigDecimal changeAmount) {
		if(order.getAuditStatus() != AuditStatusEnum.pass.getValue()) { // 非审核不处理资金
			return;
		}
		if(changeAmount.compareTo(BigDecimal.ZERO) == 0) {
			return;
		}
		// 更新往来账户金额
		SupplierInfo supplierInfo = SupplierInfo.dao.findById(order.getSupplierInfoId());
		TraderBookAccount bookAccount = supplierInfo.getTraderBookAccount();
		BigDecimal payAmount = bookAccount.getPayAmount().add(changeAmount);
		bookAccount.setPayAmount(payAmount);
		bookAccount.setUpdatedAt(new Date());
		bookAccount.update();
	}
	
	
	
	/**
	 * 更新往来帐户资金：供应商商品货款收入
	 * @param 
	 * @param order
	 * @param changeAmount
	 */
	private void goodsInTraderBookAccount(PurchaseOrder order, BigDecimal changeAmount) {
		if(order.getAuditStatus() != AuditStatusEnum.pass.getValue()) { // 非审核不处理资金
			return;
		}
		if(changeAmount.compareTo(BigDecimal.ZERO) == 0) {
			return;
		}
		// 更新供应商开单时间
		SupplierInfo supplierInfo = SupplierInfo.dao.findById(order.getSupplierInfoId());
		if(supplierInfo.getFirstOrderTime() == null) {
			supplierInfo.setFirstOrderTime(new Date());
		}
		supplierInfo.setLastOrderTime(new Date());
		supplierInfo.setUpdatedAt(new Date());
		supplierInfo.update();
		
		// 更新往来账户金额
		TraderBookAccount bookAccount = supplierInfo.getTraderBookAccount();
		BigDecimal inAmount = bookAccount.getInAmount().add(changeAmount);
		bookAccount.setInAmount(inAmount);
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
	 * 修改进货单时，更新商品数据
	 * @param 
	 * @param order
	 * @param orderGoodList
	 * @return 
	 */
	private boolean updateOrderGoods(PurchaseOrder order, List<PurchaseOrderGoods> orderGoodList) {
		boolean modifyFlag = false; // 是否存在数据修改
		List<PurchaseOrderGoods> oldOrderGoodList = PurchaseOrderGoods.dao.find("select * from purchase_order_goods where purchase_order_id = ?", order.getId());
		// 先过滤出已删除的记录
		List<PurchaseOrderGoods> deleteGoodsList = new ArrayList<>();
		for(PurchaseOrderGoods oldGoods : oldOrderGoodList) {
			boolean isExist = false;
			for (PurchaseOrderGoods goods : orderGoodList) {
				if(goods.getId() != null && goods.getId().intValue() == oldGoods.getId().intValue()) {
					isExist = true;
					break;
				}
			}
			if(!isExist) { // 需要删除的记录
				deleteGoodsList.add(oldGoods);
			}
		}
		// 库存释放
		undoInventoryStock(order, deleteGoodsList);
		
		// 删除商品数据
		for (PurchaseOrderGoods e : deleteGoodsList) {
			e.delete();
			modifyFlag = true;
		}
		
		// 保存商品
		for (PurchaseOrderGoods e : orderGoodList) {
			if (e.getGoodsInfoId() == null || e.getGoodsInfoId() <= 0 || e.getBuyNumber().doubleValue() <= 0) {
				continue;
			}
			PurchaseOrderGoods _e = PurchaseOrderGoods.dao.findById(e.getId());
			BigDecimal changeNumber = e.getBuyNumber(); // 发生变更的库存数量
			if(_e != null) {
				changeNumber = e.getBuyNumber().subtract(_e.getBuyNumber());
				if(e.getBuyNumber().compareTo(_e.getBuyNumber()) != 0 || e.getDiscountAmount().compareTo(_e.getDiscountAmount()) != 0 || 
						e.getPrice().compareTo(_e.getPrice()) != 0 || e.getAmount().compareTo(_e.getAmount()) != 0 ) {
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
				e.setPurchaseOrderId(order.getId());
				e.setUpdatedAt(new Date());
				e.setCreatedAt(new Date());
				e.save();
				
				modifyFlag = true;
			}
			e.put("changeNumber", changeNumber);
		}
		
		// 更新商品库存
		updateInventoryStock(order, orderGoodList);
		// 更新商品成本均价
		updateGoodsPrice(order, orderGoodList);
		
		return modifyFlag;
	}


	/**
	 * 商品入库，修改库存
	 * @param 
	 * @param order
	 * @param orderGoods
	 * @param changeNumber
	 */
	private void updateInventoryStock(PurchaseOrder order, List<PurchaseOrderGoods> orderGoodsList) {
		if(order.getLogisticsStatus() != LogisticsStatusInEnum.stockin.getValue()) {
			return;
		}
		for (PurchaseOrderGoods orderGoods : orderGoodsList) {
			BigDecimal changeNumber = orderGoods.getBigDecimal("changeNumber"); // 发生变更的数量
			if(changeNumber == null) {
				changeNumber = orderGoods.getBuyNumber();
			}
			if(changeNumber.compareTo(BigDecimal.ZERO) == 0) {
				continue;
			}
			GoodsInfo goodsInfo = GoodsInfo.dao.findById(orderGoods.getGoodsInfoId());
			GoodsUnit stockUnit = goodsInfo.getStockMainUnit(); // 通过库存单位查询库存
			BigDecimal changeStock = changeNumber;
			
			// 普通商品
			BigDecimal goodsStock = orderGoods.getBuyNumber();
			
			InventoryStock inventoryStock = InventoryStock.dao.findByWarehouse(orderGoods.getGoodsInfoId(), orderGoods.getSpec1Id(), orderGoods.getSpecOption1Id(), orderGoods.getSpec2Id(), orderGoods.getSpecOption2Id(), orderGoods.getSpec3Id(), orderGoods.getSpecOption3Id(), stockUnit.getId());
			if(inventoryStock == null) {
				inventoryStock = createInventoryStock(order, orderGoods, goodsStock, stockUnit);
			} else {
				inventoryStock.setStock(inventoryStock.getStock().add(changeStock)); // 增加库存
				inventoryStock.setUpdatedAt(new Date());
				inventoryStock.update();
			}
			// 库存流水
			InventoryStockLog stockLog = InventoryStockLog.dao.findByOrderId(order.getId(), StockInOutOrderTypeEnum.purchase_order.getValue(), orderGoods.getSpec1Id(), orderGoods.getSpecOption1Id(), orderGoods.getSpec2Id(), orderGoods.getSpecOption2Id(), orderGoods.getSpec3Id(), orderGoods.getSpecOption3Id(), orderGoods.getUnitId());
			if(stockLog != null) {
				stockLog.setChangeNumber(orderGoods.getBuyNumber());
				stockLog.setRemainNumber(inventoryStock.getStock());
				stockLog.update();
			} else {
				stockLog = new InventoryStockLog();
				stockLog.setChangeNumber(orderGoods.getBuyNumber());
				stockLog.setCreatedAt(new Date());
				stockLog.setGoodsInfoId(orderGoods.getGoodsInfoId());
				stockLog.setIoType(StockIoTypeEnum.in.getValue());
				stockLog.setOrderCode(order.getOrderCode());
				stockLog.setOrderId(order.getId());
				stockLog.setOrderType(StockInOutOrderTypeEnum.purchase_order.getValue());
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
	 * 创建商品库存
	 * @param 
	 * @param order
	 * @param orderGoods
	 * @param goodsStock 
	 * @param stockUnit 
	 * @return
	 */
	private InventoryStock createInventoryStock(PurchaseOrder order, PurchaseOrderGoods orderGoods, BigDecimal goodsStock, GoodsUnit stockUnit) {
		InventoryStock outInventoryStock = new InventoryStock();
		outInventoryStock.setCreatedAt(new Date());
		outInventoryStock.setDataStatus(DataStatusEnum.enable.getValue());
		outInventoryStock.setGoodsInfoId(orderGoods.getGoodsInfoId());
		outInventoryStock.setInitStock(BigDecimal.ZERO);
		outInventoryStock.setLockStock(BigDecimal.ZERO);
		outInventoryStock.setReserveStock(BigDecimal.ZERO);
		outInventoryStock.setSpec1Id(orderGoods.getSpec1Id());
		outInventoryStock.setSpecOption1Id(orderGoods.getSpecOption1Id());
		outInventoryStock.setSpec2Id(orderGoods.getSpec2Id());
		outInventoryStock.setSpecOption2Id(orderGoods.getSpecOption2Id());
		outInventoryStock.setSpec3Id(orderGoods.getSpec3Id());
		outInventoryStock.setSpecOption3Id(orderGoods.getSpecOption3Id());
		outInventoryStock.setUnitId(stockUnit.getId());
		outInventoryStock.setStock(goodsStock);
		outInventoryStock.setUpdatedAt(new Date());
		outInventoryStock.setWarnType(StockWarnTypeEnum.ok.getValue());
		outInventoryStock.save();
		return outInventoryStock;
	}

	/**
	 * 商品库存释放
	 * @param 
	 * @param order
	 * @param orderGoods
	 */
	private void undoInventoryStock(PurchaseOrder order, List<PurchaseOrderGoods> orderGoodsList) {
		if(order.getLogisticsStatus() != LogisticsStatusInEnum.stockin.getValue()) {
			return;
		}
		for (PurchaseOrderGoods orderGoods : orderGoodsList) {
			GoodsInfo goodsInfo = GoodsInfo.dao.findById(orderGoods.getGoodsInfoId());
			GoodsUnit stockUnit = goodsInfo.getStockMainUnit(); // 通过库存单位查询库存
			
			BigDecimal goodsStock = orderGoods.getBuyNumber();
			// 普通商品
			InventoryStock inventoryStock = InventoryStock.dao.findByWarehouse(orderGoods.getGoodsInfoId(), orderGoods.getSpec1Id(), orderGoods.getSpecOption1Id(), orderGoods.getSpec2Id(), orderGoods.getSpecOption2Id(), orderGoods.getSpec3Id(), orderGoods.getSpecOption3Id(), stockUnit.getId());
			inventoryStock.setStock(inventoryStock.getStock().subtract(goodsStock)); // 减库存
			inventoryStock.setUpdatedAt(new Date());
			inventoryStock.update();
			
			// 删除流水
			InventoryStockLog stockLog = InventoryStockLog.dao.findByOrderId(order.getId(), StockInOutOrderTypeEnum.purchase_order.getValue(), orderGoods.getSpec1Id(), orderGoods.getSpecOption1Id(), orderGoods.getSpec2Id(), orderGoods.getSpecOption2Id(), orderGoods.getSpec3Id(), orderGoods.getSpecOption3Id(), orderGoods.getUnitId());
			if(stockLog != null) {
				stockLog.delete();
			}
		}
	}
	
	/**
	 * 设置单据审核状态
	 * @param 
	 * @param order
	 */
	private AuditStatusEnum getAuditStatus(PurchaseOrder order) {
		TenantConfig auditConfig = TenantConfig.dao.findByKeyCache(TenantConfigEnum.audit_purchase_order);
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
	 * 更新商品成本价
	 * @param 
	 * @param order
	 * @param orderGoods
	 * @return
	 */
	public void updateGoodsPrice(PurchaseOrder order, List<PurchaseOrderGoods> orderGoodsList) {
		List<PurchaseOrderGoods> _orderGoodsList = new ArrayList<>();
		// 重组商品信息，一个单据可能存在多个相同的商品
		for (PurchaseOrderGoods orderGoods : orderGoodsList) {
			GoodsInfo goodsInfo = orderGoods.getGoodsInfo();
			BigDecimal goodStock = orderGoods.getBuyNumber();
			if(_orderGoodsList.isEmpty()) {
				orderGoods.setUnitId(goodsInfo.getStockMainUnit().getId());
				orderGoods.setBuyNumber(goodStock);
				_orderGoodsList.add(orderGoods);
				continue;
			}
			for (int i = 0; i < _orderGoodsList.size(); i++) {
				PurchaseOrderGoods _orderGoods = _orderGoodsList.get(i);
				if(orderGoods.getGoodsInfoId().intValue() == _orderGoods.getGoodsInfoId().intValue() && orderGoods.getSpec1Id().intValue() == _orderGoods.getSpec1Id().intValue() 
						&& orderGoods.getSpecOption1Id().intValue() == _orderGoods.getSpecOption1Id().intValue() 
						&& orderGoods.getSpec2Id().intValue() == _orderGoods.getSpec2Id().intValue() && orderGoods.getSpecOption2Id().intValue() == _orderGoods.getSpecOption2Id().intValue() 
						&& orderGoods.getSpec3Id().intValue() == _orderGoods.getSpec3Id().intValue() && orderGoods.getSpecOption3Id().intValue() == _orderGoods.getSpecOption3Id().intValue()) {
					_orderGoods.setBuyNumber(_orderGoods.getBuyNumber().add(goodStock));
				} else {
					orderGoods.setBuyNumber(goodStock);
					orderGoods.setUnitId(goodsInfo.getStockMainUnit().getId());
					_orderGoodsList.add(orderGoods);
				}
			}
		}
		for (PurchaseOrderGoods orderGoods : _orderGoodsList) {
			if(order.getAuditStatus() != AuditStatusEnum.pass.getValue()) { // 非审核不处理
				return;
			}
			GoodsPrice goodsPrice = GoodsPrice.dao.findBySpec(orderGoods.getGoodsInfoId(), orderGoods.getSpec1Id(), orderGoods.getSpecOption1Id(), orderGoods.getSpec2Id(), orderGoods.getSpecOption2Id(), orderGoods.getSpec3Id(), orderGoods.getSpecOption3Id(), orderGoods.getUnitId());
			if(goodsPrice != null) {
				// 计算均价
				GoodsInfo goodsInfo = orderGoods.getGoodsInfo();
				InventoryStock inventoryStock = InventoryStock.dao.findSumStock(orderGoods.getGoodsInfoId(), orderGoods.getSpec1Id(), orderGoods.getSpecOption1Id(), orderGoods.getSpec2Id(), orderGoods.getSpecOption2Id(), orderGoods.getSpec3Id(), orderGoods.getSpecOption3Id(), goodsInfo.getStockMainUnit().getId());
				BigDecimal stock = inventoryStock.getBigDecimal("stock");
				BigDecimal goodStock = orderGoods.getBuyNumber();
				// ((均价x当前库存数量)+(商品单价x商品数量))/总商品数量
				BigDecimal avgCostPrice = goodsPrice.getAvgCostPrice().multiply(stock.subtract(goodStock)).add(orderGoods.getDiscountAmount().multiply(goodStock)).divide(stock, 2, BigDecimal.ROUND_HALF_UP);
				goodsPrice.setCostPrice(orderGoods.getDiscountAmount()); // 保存最近成本价，折扣单价
				goodsPrice.setAvgCostPrice(avgCostPrice); // 更新均价
				goodsPrice.setUpdatedAt(new Date());
				goodsPrice.update();
			} else {
				goodsPrice = new GoodsPrice();
				goodsPrice.setGoodsInfoId(orderGoods.getGoodsInfoId());
				goodsPrice.setBarCode("");
				goodsPrice.setSpec1Id(orderGoods.getSpec1Id());
				goodsPrice.setSpecOption1Id(orderGoods.getSpecOption1Id());
				goodsPrice.setSpec2Id(orderGoods.getSpec2Id());
				goodsPrice.setSpecOption2Id(orderGoods.getSpecOption2Id());
				goodsPrice.setSpec3Id(orderGoods.getSpec2Id());
				goodsPrice.setSpecOption3Id(orderGoods.getSpecOption3Id());
				goodsPrice.setUnitId(orderGoods.getUnitId());
				goodsPrice.setCostPrice(orderGoods.getDiscountAmount());
				goodsPrice.setAvgCostPrice(orderGoods.getDiscountAmount());
				goodsPrice.setInitPrice(orderGoods.getDiscountAmount());
				goodsPrice.setDataStatus(DataStatusEnum.enable.getValue());
				goodsPrice.setCreatedAt(new Date());
				goodsPrice.save();
			}
		}
	}
	
	
	/**
	 * 获取单据物流状态
	 * @param 
	 * @param order
	 * @return
	 */
	private LogisticsStatusInEnum getLogisticsStatus(PurchaseOrder order) {
		if(order.getAuditStatus() == AuditStatusEnum.pass.getValue()) { // 已审核单即入库了
			return LogisticsStatusInEnum.stockin;
		} else {
			return LogisticsStatusInEnum.waiting;
		}
	}
	
	/**
	 * 发送审核结果通知
	 * @param 
	 * @param order
	 */
	private void sendAuditNoticeMsg(PurchaseOrder order) {
		SupplierInfo supplierInfo = order.getSupplierInfo();
		String title = "供应商“" + supplierInfo.getName() + "”进货单审核通过";
		String content = title;
		if (order.getAuditStatus() == AuditStatusEnum.reject.getValue()) {
			title = "供应商“" + supplierInfo.getName() + "”进货单审核拒绝";
			content = title;
		}
		Boolean smsFlag = PurchaseOrder.dao.findAuditHandlerSmsNoticeConfig();// 发送短信
		Boolean sysFlag = PurchaseOrder.dao.findAuditHandlerSysNoticeConfig();// 发送系统消息
		sendNoticeMsg(MsgDataTypeEnum.purchase_order_audit, title, content, order.getHandlerId(), smsFlag, sysFlag);

		smsFlag = PurchaseOrder.dao.findAuditMakeManSmsNoticeConfig();// 发送短信
		sysFlag = PurchaseOrder.dao.findAuditMakeManSysNoticeConfig();// 发送系统消息
		sendNoticeMsg(MsgDataTypeEnum.purchase_order_audit, title, content, order.getMakeManId(), smsFlag, sysFlag);
	}

	/**
	 * 发送消息给审核人
	 * @param 
	 * @param order
	 */
	private void sendOrderNoticeMsg(PurchaseOrder order) {
		if(order.getAuditStatus() != AuditStatusEnum.waiting.getValue() || order.getOrderStatus() != OrderStatusEnum.normal.getValue()) {
			return;
		}
		Integer	auditorId = PurchaseOrder.dao.findAuditorIdConfig();
		order.setAuditorId(auditorId); // 设置审核人
		Boolean smsFlag = PurchaseOrder.dao.findAuditSmsNoticeConfig();// 发送短信
		Boolean sysFlag = PurchaseOrder.dao.findAuditSysNoticeConfig();// 发送系统消息
		SupplierInfo supplierInfo = order.getSupplierInfo();
		String title="供应商“"+supplierInfo.getName()+"”进货单审核通知";
		String content = title;
		sendNoticeMsg(MsgDataTypeEnum.purchase_order_audit, title, content, order.getAuditorId(), smsFlag, sysFlag);
	}
	
}