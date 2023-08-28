package com.bytechainx.psi.sale.service;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import com.bytechainx.psi.common.model.CustomerInfo;
import com.bytechainx.psi.common.model.GoodsAttribute;
import com.bytechainx.psi.common.model.GoodsAttributeRef;
import com.bytechainx.psi.common.model.GoodsInfo;
import com.bytechainx.psi.common.model.GoodsPrice;
import com.bytechainx.psi.common.model.GoodsUnit;
import com.bytechainx.psi.common.model.InventoryStock;
import com.bytechainx.psi.common.model.InventoryStockLog;
import com.bytechainx.psi.common.model.SaleOrder;
import com.bytechainx.psi.common.model.SaleOrderCost;
import com.bytechainx.psi.common.model.SaleOrderFee;
import com.bytechainx.psi.common.model.SaleOrderFund;
import com.bytechainx.psi.common.model.SaleOrderGoods;
import com.bytechainx.psi.common.model.SaleOrderLog;
import com.bytechainx.psi.common.model.TenantAdmin;
import com.bytechainx.psi.common.model.TenantConfig;
import com.bytechainx.psi.common.model.TenantExportLog;
import com.bytechainx.psi.common.model.TraderBalanceAccount;
import com.bytechainx.psi.common.model.TraderBookAccount;
import com.bytechainx.psi.common.model.TraderBookAccountLogs;
import com.bytechainx.psi.common.model.TraderCustomerReceivable;
import com.bytechainx.psi.common.model.TraderFundOrder;
import com.bytechainx.psi.common.model.TraderFundType;
import com.bytechainx.psi.common.model.TraderReceiptOrder;
import com.bytechainx.psi.common.model.TraderReceiptOrderRef;
import com.bytechainx.psi.common.service.base.CommonService;
import com.google.common.collect.Lists;
import com.jfinal.kit.Kv;
import com.jfinal.kit.Ret;
import com.jfinal.kit.ThreadPoolKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;


/**
* 销售单
*/
public class SaleOrderService extends CommonService {

	/**
	* 分页列表
	*/
	public Page<SaleOrder> paginate(String startDay, String endDay, Kv conditionColumns, Integer pageNumber, int pageSize) {
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

		return SaleOrder.dao.paginate(pageNumber, pageSize, "select * ", "from sale_order "+where.toString()+" order by id desc", params.toArray());
	}
	
	/**
	* 新增
	*/
	public Ret create(SaleOrder order, List<SaleOrderGoods> orderGoodList, List<SaleOrderFund> orderFundList, List<SaleOrderFee> orderFeeList, List<SaleOrderCost> orderCostList) {
		if( order == null) {
			return Ret.fail("参数错误");
		}
		if(order.getCustomerInfoId() == null || order.getCustomerInfoId() <= 0) {
			return Ret.fail("客户不能为空");
		}
		if(order.getHandlerId() == null || order.getHandlerId() <= 0) {
			return Ret.fail("进货员不能为空");
		}
		if(order.getAmount() == null || order.getAmount().doubleValue() <= 0) {
			return Ret.fail("应收金额不能为空");
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
			for (SaleOrderFund e : orderFundList) {
				paidAmount = paidAmount.add(e.getAmount());
				if(e.getReceiptType()== FundTypeEnum.balance.getValue()) {
					balanceAmount = balanceAmount.add(e.getAmount());
				}
			}
		}
		if(paidAmount.compareTo(order.getAmount()) > 0) {
			return Ret.fail("实收金额不能大于应收金额"); 
		}
		if(balanceAmount.compareTo(BigDecimal.ZERO) > 0) { // 存在余额支付，则判断下余额支付的金额是否大于可用资金
			CustomerInfo customerInfo = CustomerInfo.dao.findById(order.getCustomerInfoId());
			if(balanceAmount.compareTo(customerInfo.getAvailableAmount()) > 0) {
				return Ret.fail("余额扣款的金额不足"); 
			}
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
		CustomerInfo customerInfo = CustomerInfo.dao.findById(order.getCustomerInfoId());
		
		sendOrderNoticeMsg(order, customerInfo);
		
		order.setPayType(customerInfo.getPayType());
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
		BigDecimal totalCostAmount = BigDecimal.ZERO;
		for (SaleOrderGoods e : orderGoodList) {
			if(e.getGoodsInfoId() == null || e.getGoodsInfoId() <= 0 || e.getBuyNumber().doubleValue() <= 0) {
				continue;
			}
			GoodsPrice goodsPrice = GoodsPrice.dao.findBySpec(e.getGoodsInfoId(), e.getSpec1Id(), e.getSpecOption1Id(), e.getSpec2Id(), e.getSpecOption2Id(), e.getSpec3Id(), e.getSpecOption3Id(), e.getUnitId());
			if(goodsPrice != null) {
				e.setCostPrice(goodsPrice.getAvgCostPrice());
				e.setCostAmount(e.getCostPrice().multiply(e.getBuyNumber()));
				totalCostAmount = totalCostAmount.add(e.getCostAmount());
			}
			e.setCustomerInfoId(order.getCustomerInfoId());
			e.setCreatedAt(new Date());
			e.setUpdatedAt(new Date());
			e.setSaleOrderId(order.getId());
			e.save();
			
		}
		order.setGoodsCostAmount(totalCostAmount);
		order.update();
		
		// 更新商品库存
		updateInventoryStock(order, orderGoodList);
		
		// 订单支付资金
		if(orderFundList != null && !orderFundList.isEmpty()) {
			for (SaleOrderFund e : orderFundList) {
				paymentOrderFund(order, e, false);
			}
		}
		// 货款计入收入
		goodsOutTraderBookAccount(order, order.getAmount());
		// 记录对账单
		updateCustomerReceivable(order);
		// 记录其他费用，需要支付给客户的
		updateOrderFee(order, orderFeeList);
		// 记录支出成本，自己本身的支出
		updateOrderCost(order, orderCostList);
		// 更新货品的告警状态，FIXME 要不要判断状态，减少无效操作
		updateStockWarn(order.getOrderGoodsList());
		
		// 记录订单操作
		writeLog(order.getId(), order.getLastManId(), "创建销售单");
		
		sendSaleOrderNotice(order);
		
		return Ret.ok("新增销售单成功").set("targetId", order.getId());
	}


	/**
	* 修改
	*/
	public Ret update(SaleOrder order, List<SaleOrderGoods> orderGoodList, List<SaleOrderFund> orderFundList, List<SaleOrderFee> orderFeeList, List<SaleOrderCost> orderCostList) {
		if(order == null || order.getId() == null || order.getId() <= 0) {
			return Ret.fail("参数错误");
		}
		if(order.getCustomerInfoId() == null || order.getCustomerInfoId() <= 0) {
			return Ret.fail("客户不能为空");
		}
		if(order.getHandlerId() == null || order.getHandlerId() <= 0) {
			return Ret.fail("进货员不能为空");
		}
		if(order.getAmount() == null || order.getAmount().doubleValue() <= 0) {
			return Ret.fail("应收金额不能为空");
		}
		if(order.getDiscountAmount() == null || order.getDiscountAmount().doubleValue() <= 0) {
			return Ret.fail("折后应收不能为空");
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
			for (SaleOrderFund e : orderFundList) {
				paidAmount = paidAmount.add(e.getAmount());
				if(e.getReceiptType()== FundTypeEnum.balance.getValue()) {
					balanceAmount = balanceAmount.add(e.getAmount());
				}
			}
		}
		if(paidAmount.compareTo(order.getAmount()) > 0) {
			return Ret.fail("实收金额不能大于应收金额"); 
		}
		if(balanceAmount.compareTo(BigDecimal.ZERO) > 0) { // 存在余额支付，则判断下余额支付的金额是否大于可用资金
			CustomerInfo customerInfo = CustomerInfo.dao.findById(order.getCustomerInfoId());
			if(balanceAmount.compareTo(customerInfo.getAvailableAmount()) > 0) {
				return Ret.fail("余额扣款的金额不足"); 
			}
		}
		
		SaleOrder _order = SaleOrder.dao.findById(order.getId());
		if(_order == null) {
			return Ret.fail("销售单不存在，无法修改");
		}
		if(_order.getRejectType() != RejectTypeEnum.no.getValue()) {
			return Ret.fail("销售单已产生退货，无法修改");
		}
		if(_order.getOrderStatus() == OrderStatusEnum.normal.getValue() && order.getOrderStatus() != OrderStatusEnum.normal.getValue()) {
			return Ret.fail("单据状态异常，无法修改"); 
		}
		BigDecimal changeAmount = order.getAmount().subtract(_order.getAmount()); // 应收金额发生变更
		
		if (_order.getOrderStatus() == OrderStatusEnum.draft.getValue()) { // 如果之前是草稿单，则处理审核状态
			// 设置审核状态
			AuditStatusEnum auditStatus = getAuditStatus(order);
			order.setAuditStatus(auditStatus.getValue());
			// 设置出入库状态
			LogisticsStatusEnum logisticsStatus = getLogisticsStatus(order);
			order.setLogisticsStatus(logisticsStatus.getValue());
			
			sendOrderNoticeMsg(order, order.getCustomerInfo());
			
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
		// 更新商品库存
		updateInventoryStock(order, orderGoodList);
		// 更新订单付款资金
		boolean fundModifyFlag = updateOrderFund(order, orderFundList, false);
		// 货款计入收入
		goodsOutTraderBookAccount(order, changeAmount);
		// 记录对账单
		updateCustomerReceivable(order);
		// 记录其他费用，需要支付给客户的
		updateOrderFee(order, orderFeeList);
		// 记录支出成本，自己本身的支出
		updateOrderCost(order, orderCostList);
		// 更新货品的告警状态，FIXME 要不要判断状态，减少无效操作
		updateStockWarn(order.getOrderGoodsList());

		// 记录订单操作
		writeLog(order.getId(), order.getLastManId(), "修改单据:"+order.getChangeLog(_order, goodsModifyFlag, fundModifyFlag));
			
		return Ret.ok("修改销售单成功").set("targetId", order.getId());
	}

	


	/**
	* 作废
	*/
	public Ret disable(List<Integer> ids) {
		if(ids == null || ids.isEmpty()) {
			return Ret.fail("参数错误");
		}
		for (Integer id : ids) {
			SaleOrder order = SaleOrder.dao.findById(id);
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
				order.setLogisticsStatus(LogisticsStatusEnum.waiting.getValue());
				order.setAuditStatus(AuditStatusEnum.waiting.getValue());
				order.setOrderStatus(OrderStatusEnum.disable.getValue());
				order.setUpdatedAt(new Date());
				order.update();
				
				continue;
			}
			// 处理收款单核销
			for (TraderReceiptOrderRef ref : order.getReceiptOrderRefList()) {
				// 回退优惠金额
				order.setAmount(order.getAmount().add(ref.getDiscountAmount()));
				order.setOddAmount(order.getOddAmount().subtract(ref.getDiscountAmount()));
				order.setPaidAmount(order.getPaidAmount().subtract(ref.getAmount()));
				if(order.getPaidAmount().compareTo(BigDecimal.ZERO) <= 0) { // 未付
					order.setPayStatus(OrderPayStatusEnum.no.getValue());
					
				} else if(order.getPaidAmount().compareTo(order.getAmount()) < 0) { // 部分付
					order.setPayStatus(OrderPayStatusEnum.part.getValue());
				}
				order.setUpdatedAt(new Date());
				order.update();
				
				// 删除销售单核销的资金明细
				Db.delete("delete from sale_order_fund where sale_order_id = ? and receipt_type = ? and balance_account_id = ?", order.getId(), FundTypeEnum.checking.getValue(), ref.getTraderReceiptOrderId());
				
				TraderReceiptOrder receiptOrder = ref.getTraderReceiptOrder();
				receiptOrder.setDiscountAmount(receiptOrder.getDiscountAmount().subtract(ref.getDiscountAmount()));
				receiptOrder.setOrderAmount(receiptOrder.getOrderAmount().subtract(ref.getAmount()));
				receiptOrder.setCheckAmount(receiptOrder.getOrderAmount().subtract(receiptOrder.getDiscountAmount()));
				receiptOrder.update();
				
				// 回退往来帐户支付金额
				fundPayTraderBookAccount(order, BigDecimal.ZERO.subtract(ref.getAmount()));
				
				ref.delete();
			}
			// 订单资金回退
			for (SaleOrderFund e : order.getOrderFundList()) {
				// 根据支付方式，回退资金
				undoOrderFund(order, e);
			}
			
			// 删除交易流水
			Db.delete("delete from trader_fund_order where ref_order_type = ? and ref_order_id = ?", RefOrderTypeEnum.sale_order.getValue(), order.getId());
			// 货款收入回退
			goodsOutTraderBookAccount(order, BigDecimal.ZERO.subtract(order.getAmount()));
			// 记录对账单
			updateCustomerReceivable(order);
			
			order.setLogisticsStatus(LogisticsStatusEnum.waiting.getValue());
			order.setAuditStatus(AuditStatusEnum.waiting.getValue());
			order.setOrderStatus(OrderStatusEnum.disable.getValue());
			order.setUpdatedAt(new Date());
			order.update();
			
			// 更新货品的告警状态，FIXME 要不要判断状态，减少无效操作
			updateStockWarn(order.getOrderGoodsList());
			
			// 记录订单操作
			writeLog(order.getId(), order.getLastManId(), "作废销售单");
		}
		return Ret.ok("作废销售单成功");
	}
	
	
	/**
	 * 销售单付款
	 * @param 
	 * @param orderId
	 * @param orderFundList
	 * @return
	 */
	public Ret payment(Integer orderId, List<SaleOrderFund> orderFundList) {
		if(orderId == null || orderId <= 0 || orderFundList == null || orderFundList.isEmpty()) {
			return Ret.fail("参数错误");
		}
		SaleOrder order = SaleOrder.dao.findById(orderId);
		if(order == null) {
			return Ret.fail("销售单不存在，无法修改");
		}
		if(order.getAuditStatus() != AuditStatusEnum.pass.getValue()) {
			return Ret.fail("销售单未通过审核，无法付款");
		}
		if(order.getOrderStatus() != OrderStatusEnum.normal.getValue()) {
			return Ret.fail("非正常销售单，无法付款");
		}
		if(order.getPayStatus() == OrderPayStatusEnum.finish.getValue()) {
			return Ret.fail("销售单已付清，无需付款");
		}
		// 账户付款，更新结算帐户资金
		for (SaleOrderFund e : orderFundList) {
			// 支付资金，生成记录
			paymentOrderFund(order, e, false);
		}
		
		return Ret.ok("销售单付款成功");
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
			SaleOrder order = SaleOrder.dao.findById(id);
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
			LogisticsStatusEnum logisticsStatus = getLogisticsStatus(order);
			order.setLogisticsStatus(logisticsStatus.getValue());

			order.setUpdatedAt(new Date());
			order.update();
			
			if(auditStatus.getValue() == AuditStatusEnum.pass.getValue()) { // 审核通过
				
				// 商品出库，库存增加，如果开通了单独出出库，审核通过也不会增加库存
				// 更新商品库存
				updateInventoryStock(order, order.getOrderGoodsList());
				// 审核处理资金，可能存在多次审核, 审核拒绝无需处理资金
				updateOrderFund(order, order.getOrderFundList(), true);
				// 往来账户
				goodsOutTraderBookAccount(order, order.getAmount());
				// 记录对账单
				updateCustomerReceivable(order);
			}
			// 更新货品的告警状态，FIXME 要不要判断状态，减少无效操作
			updateStockWarn(order.getOrderGoodsList());
			
			// 写入操作记录
			writeLog(order.getId(), order.getLastManId(), "审核进货订单:"+auditStatus.getName());
			// 发送审核结果通知
			sendAuditNoticeMsg(order);
			
			sendSaleOrderNotice(order);
		}
		return Ret.ok("审核销售单成功");
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
		SaleOrder order = SaleOrder.dao.findById(id);
		if(order == null) {
			return Ret.fail("销售单不存在");
		}
		if(order.getOrderStatus() != OrderStatusEnum.draft.getValue()) { // 非草稿单不能删除
			return Ret.fail("单据不是草稿单，不能删除");
		}
		order.delete();
		
		return Ret.ok("删除单据成功");
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
		SaleOrderLog log = new SaleOrderLog();
		log.setOperAdminId(operAdminId);
		if(operAdminId != null && operAdminId > 0) {
			TenantAdmin admin = TenantAdmin.dao.findById(operAdminId);
			log.setOperAdminName(admin.getRealName());
		} else {
			log.setOperAdminName("系统后台");
		}
		log.setOperDesc(operDesc);
		log.setOperTime(new Date());
		log.setSaleOrderId(orderId);
		log.save();
		
		return Ret.ok();
	}

	/**
	 * 导出数据xls
	 */
	public Ret export(Integer handlerId, String startDay, String endDay, Kv condKv) {
		String fileName = "销售单-"+DateUtil.getSecondNumber(new Date());
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
					List<SaleOrder> orderList = Lists.newArrayList();
					int pageNumber = 1;
					Page<SaleOrder> page = paginate(startDay, endDay, condKv, pageNumber, 500);
					while (!page.getList().isEmpty() && orderList.size() < 10000) { // 最大不能超过10000
						orderList.addAll(page.getList());
						pageNumber += 1;
						page = paginate(startDay, endDay, condKv, pageNumber, 200);
					}
					StringBuffer headersBuffer = new StringBuffer();
					headersBuffer.append("单据日期,单据编号");
					StringBuffer columnsBuffer = new StringBuffer();
					columnsBuffer.append("order_time_day,order_code");
					
					headersBuffer.append(",客户名称,商品名称,商品条码,规格,单位,数量,单价,商品折扣(%),折后单价,金额,备注,整单折扣率(%)");
					columnsBuffer.append(",customer_info_name,list,discount");
					
					List<TraderFundType> feeList = SaleOrder.dao.findFeeConfig(); // 其他费用配置
					if(feeList != null) {
						for (TraderFundType fee : feeList) {
							headersBuffer.append("," + fee.getName());
							columnsBuffer.append(",fee_amount_" + fee.getId());
						}
					}
					headersBuffer.append(",应收金额,已收金额");
					columnsBuffer.append(",amount,paid_amount");
					
					List<TraderFundType> costList = SaleOrder.dao.findCostConfig(); // 成本支出配置
					if(costList != null) {
						for (TraderFundType cost : costList) {
							headersBuffer.append("," + cost.getName());
							columnsBuffer.append(",cost_amount_" + cost.getId());
						}
					}
					headersBuffer.append(",单据备注,业务员");
					columnsBuffer.append(",remark,handler_name");
					
					String[] headers = headersBuffer.toString().split(",");
					String[] columns = columnsBuffer.toString().split(",");
					
					String[] listColumns = new String[] {"goods_name","bar_code","spec_name","unit_name","buy_number","price","discount","discount_amount","amount","remark"};
					for (SaleOrder order : orderList) {
						order.put("order_time_day", DateUtil.getDayStr(order.getOrderTime()));
						order.put("customer_info_name", order.getCustomerInfo().getName());
						
						List<SaleOrderGoods> goodsList = order.getOrderGoodsList();
						order.put("list", goodsList);
						for (SaleOrderGoods orderGoods : goodsList) {
							GoodsInfo goodsInfo = orderGoods.getGoodsInfo();
							orderGoods.put("goods_name", goodsInfo.getName());
							orderGoods.put("bar_code", goodsInfo.getBarCode());
							orderGoods.put("spec_name", orderGoods.getGoodsSpecNames());
							orderGoods.put("unit_name", orderGoods.getGoodsUnit().getName());
						}
						List<SaleOrderFee> orderFeeList = order.getOrderFeeList();
						for(SaleOrderFee fee: orderFeeList) {
							order.put("fee_amount_"+fee.getTraderFundType(), fee.getAmount());
						}
						List<SaleOrderCost> orderCostList = order.getOrderCostList();
						for(SaleOrderCost cost: orderCostList) {
							order.put("cost_amount_"+cost.getTraderFundType(), cost.getAmount());
						}
						
						order.put("handler_name", order.getHandler().getRealName());
					}
					
					String filePath = XlsKit.genOrderXls("销售单", fileName, headers, orderList, columns, listColumns);
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
	 * 导出单个销售单表格
	 * @param 
	 * @param adminId
	 * @param id
	 * @return
	 */
	public Ret exportOrder(Integer handlerId, Integer id) {
		SaleOrder saleOrder = SaleOrder.dao.findById(id);
		if(saleOrder == null) {
			return Ret.fail("销售单不存在");
		}

		String fileName = "销售单详情-"+DateUtil.getSecondNumber(new Date());
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
					saleOrder.put("list", saleOrder.getOrderGoodsList());
					StringBuffer headersBuffer = new StringBuffer();
					StringBuffer headerColsBuffer = new StringBuffer();
					StringBuffer listHeadersBuffer = new StringBuffer();
					StringBuffer listColumnsBuffer = new StringBuffer();
					StringBuffer footersBuffer = new StringBuffer();
					StringBuffer footerColsBuffer = new StringBuffer();
					
					headersBuffer.append("单据编号,单据日期");
					headerColsBuffer.append("order_code,order_time_day");
					
					headersBuffer.append(",客户名称");
					headerColsBuffer.append(",customer_info_name");
					
					
					footersBuffer.append("折扣率");
					footerColsBuffer.append("discount_name");
					List<TraderFundType> feeList = SaleOrder.dao.findFeeConfig(); // 其他费用配置
					if(feeList != null) {
						for (TraderFundType fee : feeList) {
							headersBuffer.append("," + fee.getName());
							headerColsBuffer.append(",fee_amount_" + fee.getId());
						}
					}
					footersBuffer.append(",应收金额,已收金额");
					footerColsBuffer.append(",amount,paid_amount");
					
					List<TraderFundType> costList = SaleOrder.dao.findCostConfig(); // 成本支出配置
					if(costList != null) {
						for (TraderFundType cost : costList) {
							footersBuffer.append("," + cost.getName());
							footerColsBuffer.append(",cost_amount_" + cost.getId());
						}
					}
					footersBuffer.append(",单据备注,业务员");
					footerColsBuffer.append(",remark,handler_name");
					
					listHeadersBuffer.append("商品名称,商品条码,规格,单位,数量,单价,商品折扣(%),折后单价,金额");
					listColumnsBuffer.append("goods_name,bar_code,spec_name,unit_name,buy_number,price,discount,discount_amount,amount");
					
					listHeadersBuffer.append(",备注");
					listColumnsBuffer.append(",remark");
					
					List<GoodsAttribute> attributeList = GoodsAttribute.dao.findAll();
			        for (GoodsAttribute attribute : attributeList) {
			        	listHeadersBuffer.append(","+attribute.getName());
						listColumnsBuffer.append(",attribute_"+attribute.getId());
					}
			            
			        saleOrder.put("discount_name", saleOrder.getDiscount().stripTrailingZeros().toPlainString()+"%");
					saleOrder.put("order_time_day", DateUtil.getDayStr(saleOrder.getOrderTime()));
					saleOrder.put("customer_info_name", saleOrder.getCustomerInfo().getName());
					
					List<SaleOrderGoods> goodsList = saleOrder.getOrderGoodsList();
					List<SaleOrderGoods> _newGoodsList = new ArrayList<>();
					for (SaleOrderGoods orderGoods : goodsList) {
						GoodsInfo goodsInfo = orderGoods.getGoodsInfo();
						orderGoods.put("goods_name", goodsInfo.getName());
						orderGoods.put("bar_code", goodsInfo.getBarCode());
						orderGoods.put("spec_name", orderGoods.getGoodsSpecNames());
						orderGoods.put("unit_name", orderGoods.getGoodsUnit().getName());
						List<GoodsAttributeRef> goodsAttributeRefList = goodsInfo.getGoodsAttributeRefList();
				        for (GoodsAttributeRef goodsAttributeRef : goodsAttributeRefList) {
				        	orderGoods.put("attribute_"+goodsAttributeRef.getGoodsAttributeId(), goodsAttributeRef.getAttrValue());
						}
				        _newGoodsList.add(orderGoods);
					}
					saleOrder.put("list", _newGoodsList);
					
					List<SaleOrderFee> orderFeeList = saleOrder.getOrderFeeList();
					for(SaleOrderFee fee: orderFeeList) {
						saleOrder.put("fee_amount_"+fee.getTraderFundType(), fee.getAmount());
					}
					List<SaleOrderCost> orderCostList = saleOrder.getOrderCostList();
					for(SaleOrderCost cost: orderCostList) {
						saleOrder.put("cost_amount_"+cost.getTraderFundType(), cost.getAmount());
					}
					
					saleOrder.put("handler_name", saleOrder.getHandler().getRealName());
					
					String[] headers = headersBuffer.toString().split(","); 
					String[] headerCols = headerColsBuffer.toString().split(",");
					String[] listHeaders = listHeadersBuffer.toString().split(",");
					String[] listColumns = listColumnsBuffer.toString().split(",");
					String[] footers = footersBuffer.toString().split(",");
					String[] footerCols = footerColsBuffer.toString().split(",");
					
					String filePath = XlsKit.genOrderDetailXls("销售单详情", fileName, headers, headerCols, listHeaders, listColumns, footers, footerCols, saleOrder.toMap());
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
	private boolean updateOrderFund(SaleOrder order, List<SaleOrderFund> orderFundList, boolean auditFlag) {
		boolean modifyFlag = false; // 是否存在数据修改
		List<SaleOrderFund> oldOrderFundList = SaleOrderFund.dao.find("select * from sale_order_fund where sale_order_id = ?", order.getId());
		// 先过滤出已删除的记录
		List<SaleOrderFund> deleteFundList = new ArrayList<>();
		for(SaleOrderFund oldFund : oldOrderFundList) {
			boolean isExist = false;
			for (SaleOrderFund fund : orderFundList) {
				if(fund.getId() != null && fund.getId().intValue() == oldFund.getId().intValue()) {
					isExist = true;
					break;
				}
			}
			if(!isExist) { // 需要删除的记录
				deleteFundList.add(oldFund);
			}
		}
		
		for (SaleOrderFund e : deleteFundList) {
			// 回退资金到帐户
			undoOrderFund(order, e);
			// 删除资金记录
			e.delete();
			modifyFlag = true;
		}
		
		// 账户付款，更新结算帐户资金
		for (SaleOrderFund e : orderFundList) {
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
	private void undoOrderFund(SaleOrder order, SaleOrderFund orderFund) {
		if(order.getAuditStatus() != AuditStatusEnum.pass.getValue()) { // 非审核不处理资金
			return;
		}
		if(orderFund.getReceiptType() == FundTypeEnum.cash.getValue()) { // 本单支付，退回到结算帐户
			TraderBalanceAccount balanceAccount = TraderBalanceAccount.dao.findById(orderFund.getBalanceAccountId());
			balanceAccount.setBalance(balanceAccount.getBalance().subtract(orderFund.getAmount())); // 回退支付的金额
			balanceAccount.setUpdatedAt(new Date());
			balanceAccount.update();
			
			BigDecimal changeAmount = BigDecimal.ZERO.subtract(orderFund.getAmount()); // 回退支付的金额，支付的时候是增加，退款的时候是减少
			fundInTraderBookAccount(order, changeAmount);
			
		} else if(orderFund.getReceiptType() == FundTypeEnum.balance.getValue()) { // 余额支付，退回到往来帐户
			BigDecimal changeAmount = BigDecimal.ZERO.subtract(orderFund.getAmount()); // 回退支付的金额，支付的时候是增加，退款的时候是减少
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
	private boolean paymentOrderFund(SaleOrder order, SaleOrderFund orderFund, boolean auditFlag) {
		boolean modifyFlag = false; // 是否存在数据修改
		if(orderFund.getBalanceAccountId() == null || orderFund.getBalanceAccountId() <= 0 || orderFund.getAmount() == null || orderFund.getAmount().doubleValue() <= 0) {
			return modifyFlag;
		}
		BigDecimal changeFundAmount = orderFund.getAmount(); // 变更的金额
		SaleOrderFund _orderFund = SaleOrderFund.dao.findFirst("select * from sale_order_fund where sale_order_id = ? and id = ?", order.getId(), orderFund.getId());
		if(_orderFund != null) {
			changeFundAmount = orderFund.getAmount().subtract(_orderFund.getAmount()); // 计算差额
			if(_orderFund.getBalanceAccountId().intValue() != orderFund.getBalanceAccountId().intValue() 
					|| (_orderFund.getBalanceAccountId().intValue() == orderFund.getBalanceAccountId().intValue() && _orderFund.getReceiptType() != orderFund.getReceiptType()) 
					|| _orderFund.getAmount().compareTo(orderFund.getAmount()) != 0 || _orderFund.getReceiptTime().getTime() != orderFund.getReceiptTime().getTime()) {
				if(orderFund.getReceiptType() != _orderFund.getReceiptType()) { // 支付方式改变了,则要处理老的资金
					changeFundAmount = orderFund.getAmount();
					
					if(_orderFund.getReceiptType() == FundTypeEnum.cash.getValue()) { // 本单支付，从结算帐户支付
						fundInTraderBookAccount(order, BigDecimal.ZERO.subtract(_orderFund.getAmount()));
					} else if(_orderFund.getReceiptType() == FundTypeEnum.balance.getValue()) { // 余额支付，从往来帐户支付
						fundPayTraderBookAccount(order, BigDecimal.ZERO.subtract(_orderFund.getAmount()));
					}
				}
				_orderFund.setAmount(orderFund.getAmount());
				_orderFund.setBalanceAccountId(orderFund.getBalanceAccountId());
				_orderFund.setReceiptTime(orderFund.getReceiptTime());
				_orderFund.setReceiptType(orderFund.getReceiptType());
				_orderFund.setUpdatedAt(new Date());
				_orderFund.update();
				
				modifyFlag = true;
			}
			
		} else {
			orderFund.setSaleOrderId(order.getId());
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
		if(orderFund.getReceiptType() == FundTypeEnum.cash.getValue()) { // 本单支付，从结算帐户支付
			TraderBalanceAccount balanceAccount = TraderBalanceAccount.dao.findById(orderFund.getBalanceAccountId());
			balanceAccount.setBalance(balanceAccount.getBalance().add(changeFundAmount)); // 减去支付的金额
			balanceAccount.setUpdatedAt(new Date());
			balanceAccount.update();
			
			// 更新往来帐户资金
			fundInTraderBookAccount(order, changeFundAmount);
			
		} else if(orderFund.getReceiptType() == FundTypeEnum.balance.getValue()) { // 余额支付，从往来帐户支付
			fundPayTraderBookAccount(order, changeFundAmount);
		}
		
		// 生成交易流水
		TraderFundOrder fundOrder = TraderFundOrder.dao.findByOrderId(orderFund.getBalanceAccountId(), RefOrderTypeEnum.sale_order, order.getId());
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
			fundOrder.setRefOrderType(RefOrderTypeEnum.sale_order.getValue());
			if(orderFund.getReceiptType() == FundTypeEnum.balance.getValue()) {
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
	 * 更新往来帐户资金：客户支付资金，收取客户欠款
	 * @param 
	 * @param order
	 * @param changeAmount
	 */
	private void fundInTraderBookAccount(SaleOrder order, BigDecimal changeAmount) {
		if(order.getAuditStatus() != AuditStatusEnum.pass.getValue()) { // 非审核不处理资金
			return;
		}
		if(changeAmount.compareTo(BigDecimal.ZERO) == 0) {
			return;
		}
		// 更新往来账户金额
		CustomerInfo customerInfo = CustomerInfo.dao.findById(order.getCustomerInfoId());
		TraderBookAccount bookAccount = customerInfo.getTraderBookAccount();
		BigDecimal inAmount = bookAccount.getInAmount().add(changeAmount);
		bookAccount.setInAmount(inAmount);
		BigDecimal payAmount = bookAccount.getPayAmount().add(changeAmount);
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
	 * 更新往来帐户资金：客户平账资金
	 * @param 
	 * @param order
	 * @param changeAmount
	 */
	private void fundPayTraderBookAccount(SaleOrder order, BigDecimal changeAmount) {
		if(order.getAuditStatus() != AuditStatusEnum.pass.getValue()) { // 非审核不处理资金
			return;
		}
		if(changeAmount.compareTo(BigDecimal.ZERO) == 0) {
			return;
		}
		// 更新往来账户金额
		CustomerInfo customerInfo = CustomerInfo.dao.findById(order.getCustomerInfoId());
		TraderBookAccount bookAccount = customerInfo.getTraderBookAccount();
		BigDecimal payAmount = bookAccount.getPayAmount().add(changeAmount);
		bookAccount.setPayAmount(payAmount);
		bookAccount.setUpdatedAt(new Date());
		bookAccount.update();
	}
	
	
	/**
	 * 更新往来帐户资金：给客户货品即总货款
	 * @param 
	 * @param order
	 * @param changeAmount
	 */
	private void goodsOutTraderBookAccount(SaleOrder order, BigDecimal changeAmount) {
		if(order.getAuditStatus() != AuditStatusEnum.pass.getValue()) { // 非审核不处理资金
			return;
		}
		if(changeAmount.compareTo(BigDecimal.ZERO) == 0) {
			return;
		}
		// 更新客户开单时间
		CustomerInfo customerInfo = CustomerInfo.dao.findById(order.getCustomerInfoId());
		if(customerInfo.getFirstOrderTime() == null) {
			customerInfo.setFirstOrderTime(new Date());
		}
		customerInfo.setLastOrderTime(new Date());
		customerInfo.setUpdatedAt(new Date());
		customerInfo.update();
		
		// 更新往来账户金额
		TraderBookAccount bookAccount = customerInfo.getTraderBookAccount();
		BigDecimal outAmount = bookAccount.getOutAmount().add(changeAmount);
		bookAccount.setOutAmount(outAmount);
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
	 * 修改销售单时，更新商品数据
	 * @param 
	 * @param order
	 * @param orderGoodList
	 */
	private boolean updateOrderGoods(SaleOrder order, List<SaleOrderGoods> orderGoodList) {
		boolean modifyFlag = false; // 是否存在数据修改
		List<SaleOrderGoods> oldOrderGoodList = SaleOrderGoods.dao.find("select * from sale_order_goods where sale_order_id = ?", order.getId());
		// 先过滤出已删除的记录
		List<SaleOrderGoods> deleteGoodsList = new ArrayList<>();
		for(SaleOrderGoods oldGoods : oldOrderGoodList) {
			boolean isExist = false;
			for (SaleOrderGoods goods : orderGoodList) {
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
		for (SaleOrderGoods e : deleteGoodsList) {
			e.delete();
			modifyFlag = true;
		}
		
		// 保存商品
		BigDecimal totalCostAmount = BigDecimal.ZERO;
		for (SaleOrderGoods e : orderGoodList) {
			if (e.getGoodsInfoId() == null || e.getGoodsInfoId() <= 0 || e.getBuyNumber().doubleValue() <= 0) {
				continue;
			}
			SaleOrderGoods _e = SaleOrderGoods.dao.findById(e.getId());
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
					_e.setCustomerInfoId(order.getCustomerInfoId());
					_e.setUpdatedAt(new Date());
					_e.update();
					
					modifyFlag = true;
				}
			} else {
				e.setCustomerInfoId(order.getCustomerInfoId());
				e.setSaleOrderId(order.getId());
				e.setUpdatedAt(new Date());
				e.setCreatedAt(new Date());
				e.save();
				
				modifyFlag = true;
			}
			e.put("changeNumber", changeNumber);
			
			GoodsPrice goodsPrice = GoodsPrice.dao.findBySpec(e.getGoodsInfoId(), e.getSpec1Id(), e.getSpecOption1Id(), e.getSpec2Id(), e.getSpecOption2Id(), e.getSpec3Id(), e.getSpecOption3Id(), e.getUnitId());
			if(goodsPrice != null) {
				e.setCostPrice(goodsPrice.getAvgCostPrice());
				e.setCostAmount(e.getCostPrice().multiply(e.getBuyNumber()));
				totalCostAmount = totalCostAmount.add(e.getCostAmount());
			}
			
		}
		if(order.getGoodsCostAmount() == null || order.getGoodsCostAmount().compareTo(totalCostAmount) != 0) {
			order.setGoodsCostAmount(totalCostAmount);
			order.update();
		}
		
		return modifyFlag;
	}


	/**
	 * 商品出库，修改库存
	 * @param 
	 * @param order
	 * @param orderGoods
	 * @param changeNumber
	 * @throws BizException 
	 */
	private void updateInventoryStock(SaleOrder order, List<SaleOrderGoods> orderGoodsList) {
		/**
		if(order.getLogisticsStatus() != LogisticsStatusEnum.stockout.getValue() && order.getOrderStatus() != OrderStatusEnum.normal.getValue()) {
			return;
		}
		**/
		TenantConfig saleNegativeStockConfig = TenantConfig.dao.findByKeyCache(TenantConfigEnum.sale_negative_stock);
		Boolean saleNegativeStockFlag = Boolean.parseBoolean(saleNegativeStockConfig.getAttrValue()); // 是否允许负库存销售
		
		for (SaleOrderGoods orderGoods : orderGoodsList) {
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
			InventoryStock inventoryStock = InventoryStock.dao.findByWarehouse(orderGoods.getGoodsInfoId(), orderGoods.getSpec1Id(), orderGoods.getSpecOption1Id(), orderGoods.getSpec2Id(), orderGoods.getSpecOption2Id(), orderGoods.getSpec3Id(), orderGoods.getSpecOption3Id(), stockUnit.getId());
			if(inventoryStock == null) {
				throw new BizException("库存异常，请确认“"+goodsInfo.getName()+"”是否存在库存");
			}
			if(!saleNegativeStockFlag) { // 不允许负库存
				if(inventoryStock.getAvailableStock().compareTo(changeStock) < 0) {
					throw new BizException(goodsInfo.getName()+"库存不足");
				}
			}
			if(order.getOrderStatus() == null || order.getOrderStatus() == OrderStatusEnum.normal.getValue()) { // 锁定库存状态
				inventoryStock.setLockStock(inventoryStock.getLockStock().add(changeStock)); // 锁定库存Reserve
				inventoryStock.setReserveStock(inventoryStock.getReserveStock().add(changeStock)); // 锁定已订库存
			}
			
			if(order.getLogisticsStatus() == LogisticsStatusEnum.stockout.getValue()) { // 已出库，再次修改库存，直接减库存
				inventoryStock.setLockStock(inventoryStock.getLockStock().subtract(changeStock)); // 解锁库存
				inventoryStock.setReserveStock(inventoryStock.getReserveStock().subtract(changeStock)); // 解锁已订库存
				inventoryStock.setStock(inventoryStock.getStock().subtract(changeStock)); // 减去使用库存
			}
			inventoryStock.setUpdatedAt(new Date());
			inventoryStock.update();
			
			if(order.getLogisticsStatus() != LogisticsStatusEnum.stockout.getValue()) { // 非出库状态，不执行以下操作
				continue;
			}
			
			// 库存流水
			InventoryStockLog stockLog = InventoryStockLog.dao.findByOrderId(order.getId(), StockInOutOrderTypeEnum.sale_order.getValue(), orderGoods.getSpec1Id(), orderGoods.getSpecOption1Id(), orderGoods.getSpec2Id(), orderGoods.getSpecOption2Id(), orderGoods.getSpec3Id(), orderGoods.getSpecOption3Id(), orderGoods.getUnitId());
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
				stockLog.setOrderType(StockInOutOrderTypeEnum.sale_order.getValue());
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
	 * 商品库存释放
	 * @param 
	 * @param order
	 * @param orderGoods
	 */
	private void undoInventoryStock(SaleOrder order, List<SaleOrderGoods> orderGoodsList) {
		if(order.getLogisticsStatus() != LogisticsStatusEnum.stockout.getValue() && order.getOrderStatus() != OrderStatusEnum.normal.getValue()) {
			return;
		}
		for (SaleOrderGoods orderGoods : orderGoodsList) {
			GoodsInfo goodsInfo = GoodsInfo.dao.findById(orderGoods.getGoodsInfoId());
			GoodsUnit stockUnit = goodsInfo.getStockMainUnit(); // 通过库存单位查询库存
			BigDecimal goodsStock = orderGoods.getBuyNumber(); // 换算后的库存
			
			InventoryStock inventoryStock = InventoryStock.dao.findByWarehouse(orderGoods.getGoodsInfoId(), orderGoods.getSpec1Id(), orderGoods.getSpecOption1Id(), orderGoods.getSpec2Id(), orderGoods.getSpecOption2Id(), orderGoods.getSpec3Id(), orderGoods.getSpecOption3Id(), stockUnit.getId());
			if(inventoryStock == null) {
				throw new BizException("库存异常，请确认“"+goodsInfo.getName()+"”是否存在库存");
			}
			
			if(order.getLogisticsStatus() == LogisticsStatusEnum.stockout.getValue()) {
				inventoryStock.setStock(inventoryStock.getStock().add(goodsStock)); // 退回库存
				
			} else if(order.getOrderStatus() == null || order.getOrderStatus() == OrderStatusEnum.normal.getValue()) {
				inventoryStock.setLockStock(inventoryStock.getLockStock().subtract(goodsStock)); // 解锁库存
				inventoryStock.setReserveStock(inventoryStock.getReserveStock().subtract(goodsStock)); // 解锁已订库存
			}
			inventoryStock.setUpdatedAt(new Date());
			inventoryStock.update();
			// 删除流水
			InventoryStockLog stockLog = InventoryStockLog.dao.findByOrderId(order.getId(), StockInOutOrderTypeEnum.sale_order.getValue(), orderGoods.getSpec1Id(), orderGoods.getSpecOption1Id(), orderGoods.getSpec2Id(), orderGoods.getSpecOption2Id(), orderGoods.getSpec3Id(), orderGoods.getSpecOption3Id(), orderGoods.getUnitId());
			if(stockLog != null) {
				stockLog.delete();
			}
		}
	}
	

	/**
	 * 客户应付对账
	 * @param 
	 * @param order
	 */
	private void updateCustomerReceivable(SaleOrder order) {
		if(order.getAuditStatus() != AuditStatusEnum.pass.getValue()) { // 非审核不处理资金
			return;
		}
		TraderCustomerReceivable customerReceivable = TraderCustomerReceivable.dao.findByOrderId(CheckingRefOrderTypeEnum.sale_order, order.getId());
		if(customerReceivable != null) {
			if(order.getOrderStatus() == OrderStatusEnum.disable.getValue()) {
				customerReceivable.delete();
				return;
			}
			BigDecimal discountAmount = order.getGoodsAmount().subtract(order.getDiscountAmount()).add(order.getOddAmount()); // 计算优惠金额
			customerReceivable.setNewAmount(order.getAmount().add(discountAmount));
			customerReceivable.setTakeAmount(order.getPaidAmount());
			customerReceivable.setDiscountAmount(discountAmount);
			customerReceivable.setAdjustAmount(BigDecimal.ZERO);
			customerReceivable.setOrderTime(order.getOrderTime());
			customerReceivable.setUpdatedAt(new Date());
			customerReceivable.update();
			
		} else {
			BigDecimal discountAmount = order.getGoodsAmount().subtract(order.getDiscountAmount()).add(order.getOddAmount()); // 计算优惠金额
			CustomerInfo customerInfo = CustomerInfo.dao.findById(order.getCustomerInfoId());
			customerReceivable = new TraderCustomerReceivable();
			customerReceivable.setRefOrderCode(order.getOrderCode());
			customerReceivable.setRefOrderId(order.getId());
			customerReceivable.setRefOrderType(CheckingRefOrderTypeEnum.sale_order.getValue());
			customerReceivable.setCustomerInfoId(order.getCustomerInfoId());
			customerReceivable.setTraderBookAccountId(customerInfo.getTraderBookAccountId());
			customerReceivable.setDiscountAmount(order.getDiscountAmount());
			customerReceivable.setNewAmount(order.getAmount().add(discountAmount));
			customerReceivable.setTakeAmount(order.getPaidAmount());
			customerReceivable.setDiscountAmount(discountAmount);
			customerReceivable.setAdjustAmount(BigDecimal.ZERO);
			customerReceivable.setOrderTime(order.getOrderTime());
			customerReceivable.setCreatedAt(new Date());
			customerReceivable.setUpdatedAt(new Date());
			customerReceivable.save();
		}
	}


	/**
	 * 记录单据其他支出成本，不用支付给客户
	 * @param 
	 * @param order
	 * @param orderCostList
	 */
	private void updateOrderCost(SaleOrder order, List<SaleOrderCost> orderCostList) {
		if(orderCostList == null) {
			orderCostList = new ArrayList<>();
		}
		List<SaleOrderCost> oldOrderFeeList = SaleOrderCost.dao.find("select * from sale_order_cost where sale_order_id = ?", order.getId());
		// 先过滤出已删除的记录
		List<SaleOrderCost> deleteFeeList = new ArrayList<>();
		for(SaleOrderCost oldCost : oldOrderFeeList) {
			boolean isExist = false;
			for (SaleOrderCost cost : orderCostList) {
				if(cost.getId() != null && cost.getId().intValue() == oldCost.getId().intValue()) {
					isExist = true;
					break;
				}
				if(cost.getTraderFundType().intValue() == oldCost.getTraderFundType().intValue()) { // 同一个结算帐户
					isExist = true;
					break;
				}
			}
			if(!isExist) { // 需要删除的记录
				deleteFeeList.add(oldCost);
			}
		}
		
		for (SaleOrderCost e : deleteFeeList) {
			// 删除资金记录
			e.delete();
		}
		BigDecimal totalOtherCostAmount = BigDecimal.ZERO;
		for (SaleOrderCost orderCost : orderCostList) {
			if(orderCost.getAmount() == null || orderCost.getAmount().compareTo(BigDecimal.ZERO) == 0 || orderCost.getTraderFundType() == null || orderCost.getTraderFundType() <= 0) {
				continue;
			}
			SaleOrderCost _orderCost = SaleOrderCost.dao.findFirst("select * from sale_order_cost where sale_order_id = ? and trader_fund_type = ? limit 1", order.getId(), orderCost.getTraderFundType());
			if(_orderCost == null) {
				orderCost.setSaleOrderId(order.getId());
				orderCost.save();
			} else {
				orderCost.update();
			}
			totalOtherCostAmount = totalOtherCostAmount.add(orderCost.getAmount());
		}
		order.setOtherCostAmount(totalOtherCostAmount);
		order.update();
	}


	/**
	 * 记录其他费用，需要收取客户
	 * @param 
	 * @param order
	 * @param orderFeeList
	 */
	private void updateOrderFee(SaleOrder order, List<SaleOrderFee> orderFeeList) {
		if(orderFeeList == null) {
			orderFeeList = new ArrayList<>();
		}
		List<SaleOrderFee> oldOrderFeeList = SaleOrderFee.dao.find("select * from sale_order_fee where sale_order_id = ?", order.getId());
		// 先过滤出已删除的记录
		List<SaleOrderFee> deleteFeeList = new ArrayList<>();
		for(SaleOrderFee oldFee : oldOrderFeeList) {
			boolean isExist = false;
			for (SaleOrderFee fee : orderFeeList) {
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
		
		for (SaleOrderFee e : deleteFeeList) {
			// 删除资金记录
			e.delete();
		}
		BigDecimal totalOtherAmount = BigDecimal.ZERO;
		for (SaleOrderFee orderFee : orderFeeList) {
			if(orderFee.getAmount() == null || orderFee.getAmount().compareTo(BigDecimal.ZERO) == 0 || orderFee.getTraderFundType() == null || orderFee.getTraderFundType() <= 0) {
				continue;
			}
			SaleOrderFee _orderFee = SaleOrderFee.dao.findFirst("select * from sale_order_fee where sale_order_id = ? and trader_fund_type = ? limit 1", order.getId(), orderFee.getTraderFundType());
			if(_orderFee == null) {
				orderFee.setSaleOrderId(order.getId());
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
	 * 设置单据审核状态
	 * @param 
	 * @param order
	 */
	private AuditStatusEnum getAuditStatus(SaleOrder order) {
		TenantConfig auditConfig = TenantConfig.dao.findByKeyCache(TenantConfigEnum.audit_sale_order);
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
	private LogisticsStatusEnum getLogisticsStatus(SaleOrder order) {
		if(order.getAuditStatus() == AuditStatusEnum.pass.getValue()) { // 已审核单即出库了
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
	private void sendAuditNoticeMsg(SaleOrder order) {
		CustomerInfo customerInfo = order.getCustomerInfo();
		String title = "客户“" + customerInfo.getName() + "”销售单审核通过";
		String content = title;
		if (order.getAuditStatus() == AuditStatusEnum.reject.getValue()) {
			title = "客户“" + customerInfo.getName() + "”销售单审核拒绝";
			content = title;
		}
		
		Boolean smsFlag = SaleOrder.dao.findAuditHandlerSmsNoticeConfig();// 发送短信
		Boolean sysFlag = SaleOrder.dao.findAuditHandlerSysNoticeConfig();// 发送系统消息
		sendNoticeMsg(MsgDataTypeEnum.sale_order_audit, title, content, order.getHandlerId(), smsFlag, sysFlag);

		smsFlag = SaleOrder.dao.findAuditMakeManSmsNoticeConfig();// 发送短信
		sysFlag = SaleOrder.dao.findAuditMakeManSysNoticeConfig();// 发送系统消息
		sendNoticeMsg(MsgDataTypeEnum.sale_order_audit, title, content, order.getMakeManId(), smsFlag, sysFlag);
	}
	
	
	/**
	 * 发送消息给审核人
	 * @param 
	 * @param order
	 * @param customerInfo
	 */
	private void sendOrderNoticeMsg(SaleOrder order, CustomerInfo customerInfo) {
		if(order.getAuditStatus() != AuditStatusEnum.waiting.getValue() || order.getOrderStatus() != OrderStatusEnum.normal.getValue()) {
			return;
		}
		Integer	auditorId = SaleOrder.dao.findAuditorIdConfig();
		order.setAuditorId(auditorId); // 设置审核人
		Boolean smsFlag = SaleOrder.dao.findAuditSmsNoticeConfig();// 发送短信
		Boolean sysFlag = SaleOrder.dao.findAuditSysNoticeConfig();// 发送系统消息
		String title="客户“"+customerInfo.getName()+"”销售单审核通知";
		String content = title;
		sendNoticeMsg(MsgDataTypeEnum.sale_order_audit, title, content, order.getAuditorId(), smsFlag, sysFlag);
	}


}