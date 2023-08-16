package com.bytechainx.psi.fund.service;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.bytechainx.psi.common.EnumConstant.AuditStatusEnum;
import com.bytechainx.psi.common.EnumConstant.FeePayAccountEnum;
import com.bytechainx.psi.common.EnumConstant.FundFlowEnum;
import com.bytechainx.psi.common.EnumConstant.OrderStatusEnum;
import com.bytechainx.psi.common.EnumConstant.RefOrderTypeEnum;
import com.bytechainx.psi.common.EnumConstant.TenantConfigEnum;
import com.bytechainx.psi.common.model.TenantConfig;
import com.bytechainx.psi.common.model.TraderBalanceAccount;
import com.bytechainx.psi.common.model.TraderFundOrder;
import com.bytechainx.psi.common.model.TraderTransferOrder;
import com.bytechainx.psi.common.service.base.CommonService;
import com.jfinal.kit.Kv;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;


/**
* 账户互转
*/
public class AccountTransferService extends CommonService {
	

	/**
	* 分页列表
	*/
	public Page<TraderTransferOrder> paginate(String startDay, String endDay, Kv conditionColumns, Integer pageNumber, int pageSize) {
		StringBuffer where = new StringBuffer();
		List<Object> params = new ArrayList<Object>();
		where.append("where 1 = 1 ");
		if(StringUtils.isNotEmpty(startDay)) {
			where.append(" and out_time  >= ?");
			params.add(startDay);
		}
		if(StringUtils.isNotEmpty(endDay)) {
			where.append(" and out_time  <= ?");
			params.add(endDay);
		}

		conditionFilter(conditionColumns, where, params);

		return TraderTransferOrder.dao.paginate(pageNumber, pageSize, "select * ", "from trader_transfer_order "+where.toString()+" order by id desc", params.toArray());
	}

	/**
	* 新增
	*/
	public Ret create(TraderTransferOrder order) {
		if( order == null) {
			return Ret.fail("参数错误");
		}
		if(order.getOutAccountId() == null || order.getOutAccountId() <= 0) {
			return Ret.fail("转出帐户不能为空");
		}
		if(order.getInAccountId() == null || order.getInAccountId() <= 0) {
			return Ret.fail("转入帐户不能为空");
		}
		if(order.getOutAccountId().intValue() == order.getInAccountId().intValue()) {
			return Ret.fail("转入转出账户不能相同");
		}
		if(order.getOutTime() == null) {
			return Ret.fail("转出时间不能为空");
		}
		if(order.getInTime() == null) {
			return Ret.fail("到帐时间不能为空");
		}
		if(order.getAmount() == null || order.getAmount().doubleValue() <= 0) {
			return Ret.fail("转账金额不能为空");
		}
		// 设置审核状态
		AuditStatusEnum auditStatus = getAuditStatus(order);
		order.setAuditStatus(auditStatus.getValue());
		
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
		
		// 账户付款
		paymentOrderFund(order);
		
		return Ret.ok("新增转账单成功").set("targetId", order.getId());
	}


	/**
	* 修改
	*/
	public Ret update(TraderTransferOrder order) {
		if(order == null || order.getId() == null || order.getId() <= 0) {
			return Ret.fail("参数错误");
		}
		if(order.getOutAccountId() == null || order.getOutAccountId() <= 0) {
			return Ret.fail("转出帐户不能为空");
		}
		if(order.getInAccountId() == null || order.getInAccountId() <= 0) {
			return Ret.fail("转入帐户不能为空");
		}
		if(order.getOutAccountId().intValue() == order.getInAccountId().intValue()) {
			return Ret.fail("转入转出账户不能相同");
		}
		if(order.getOutTime() == null) {
			return Ret.fail("转出时间不能为空");
		}
		if(order.getInTime() == null) {
			return Ret.fail("到帐时间不能为空");
		}
		if(order.getAmount() == null || order.getAmount().doubleValue() <= 0) {
			return Ret.fail("转账金额不能为空");
		}
		
		TraderTransferOrder _order = TraderTransferOrder.dao.findById(order.getId());
		if(_order == null) {
			return Ret.fail("互转单不存在，无法修改");
		}
		if(_order.getOrderStatus() == OrderStatusEnum.normal.getValue() && order.getOrderStatus() != OrderStatusEnum.normal.getValue()) {
			return Ret.fail("单据状态异常，无法修改"); 
		}
		// 审核状态要根据是否开启审核开关来设置，默认无需审核
		if (_order.getOrderStatus() == OrderStatusEnum.draft.getValue()) { // 如果之前是草稿单，则处理审核状态
			AuditStatusEnum auditStatus = getAuditStatus(order);
			order.setAuditStatus(auditStatus.getValue());
		} else {
			order.setAuditStatus(_order.getAuditStatus());
		}
		order.setUpdatedAt(new Date());
		order.update();
		
		// 更新订单付款资金
		paymentOrderFund(order);
		
		return Ret.ok("修改转账单成功");
	}


	/**
	* 作废
	*/
	public Ret disable(List<Integer> ids) {
		if(ids == null || ids.isEmpty()) {
			return Ret.fail("参数错误");
		}
		for (Integer id : ids) {
			TraderTransferOrder order = TraderTransferOrder.dao.findById(id);
			if(order == null) {
				continue;
			}
			order.setOrderStatus(OrderStatusEnum.disable.getValue());
			order.setUpdatedAt(new Date());
			order.update();
			
			if(order.getAuditStatus() != AuditStatusEnum.pass.getValue()) { // 不是已审核状态，不处理资金
				continue;
			}
			// 资金回退
			refundOrderFund(order);
		}
		
		return Ret.ok("作废转账单成功");
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
			TraderTransferOrder order = TraderTransferOrder.dao.findById(id);
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
			order.setUpdatedAt(new Date());
			order.update();
			
			// 审核处理资金，可能存在多次审核, 审核拒绝无需处理资金
			if(auditStatus.getValue() == AuditStatusEnum.pass.getValue()) { // 审核通过
				paymentOrderFund(order);
			}
		}
		return Ret.ok("审核转账单成功");
	}
	
	
	/**
	 * 回退资金到帐户
	 * @param tenantOrgId
	 * @param order
	 * @param e
	 */
	private void refundOrderFund(TraderTransferOrder order) {
		if(order.getAuditStatus() != AuditStatusEnum.pass.getValue()) { // 非审核不处理资金
			return;
		}
		TraderBalanceAccount inBalanceAccount = TraderBalanceAccount.dao.findById(order.getInAccountId());
		BigDecimal inAmount = order.getAmount();
		if (order.getFeePayAccount() == FeePayAccountEnum.in.getValue() && order.getFee() != null) { // 转入账户方支付手续费
			inAmount = inAmount.subtract(order.getFee());
		}
		inBalanceAccount.setBalance(inBalanceAccount.getBalance().subtract(inAmount)); // 回退转入的金额
		inBalanceAccount.setUpdatedAt(new Date());
		inBalanceAccount.update();
		
		TraderBalanceAccount outBalanceAccount = TraderBalanceAccount.dao.findById(order.getOutAccountId());
		BigDecimal outAmount = order.getAmount();
		if (order.getFeePayAccount() == FeePayAccountEnum.out.getValue() && order.getFee() != null) { // 转出账户方支付手续费
			outAmount = outAmount.add(order.getFee());
		}
		outBalanceAccount.setBalance(outBalanceAccount.getBalance().add(outAmount)); // 回退转出的金额
		outBalanceAccount.setUpdatedAt(new Date());
		outBalanceAccount.update();
		
		// 删除交易流水
		Db.delete("delete from trader_fund_order where ref_order_type = ? and ref_order_id = ?", RefOrderTypeEnum.trader_transfer_order.getValue(), order.getId());

	}
	
	/**
	 * 帐户资金支付处理
	 * @param tenantOrgId
	 * @param order
	 * @param orderFund
	 */
	private void paymentOrderFund(TraderTransferOrder order) {
		if(order.getAuditStatus() != AuditStatusEnum.pass.getValue()) { // 不是已审核状态，不处理资金
			return;
		}
		// 手续费支付方处理
		// 结算帐户余额计算
		TraderBalanceAccount inBalanceAccount = TraderBalanceAccount.dao.findById(order.getInAccountId());
		BigDecimal inAmount = order.getAmount();
		if (order.getFeePayAccount() == FeePayAccountEnum.in.getValue() && order.getFee() != null) { // 转入账户方支付手续费
			inAmount = inAmount.subtract(order.getFee());
		}
		inBalanceAccount.setBalance(inBalanceAccount.getBalance().add(inAmount)); // 转入的金额
		inBalanceAccount.setUpdatedAt(new Date());
		inBalanceAccount.update();
		
		TraderBalanceAccount outBalanceAccount = TraderBalanceAccount.dao.findById(order.getOutAccountId());
		BigDecimal outAmount = order.getAmount();
		if (order.getFeePayAccount() == FeePayAccountEnum.out.getValue() && order.getFee() != null) { // 转出账户方支付手续费
			outAmount = outAmount.add(order.getFee());
		}
		outBalanceAccount.setBalance(outBalanceAccount.getBalance().subtract(outAmount)); // 转出的金额
		outBalanceAccount.setUpdatedAt(new Date());
		outBalanceAccount.update();
		
		// 生成交易流水
		TraderFundOrder inFundOrder = TraderFundOrder.dao.findByOrderId(order.getInAccountId(), RefOrderTypeEnum.trader_transfer_order, order.getId());
		if(inFundOrder != null) { // 存在，则更新
			inFundOrder.setAmount(inAmount);
			inFundOrder.setOrderTime(new Date());
			inFundOrder.setUpdatedAt(new Date());
			inFundOrder.update();
			
		} else { // 不存在，则新增
			inFundOrder = new TraderFundOrder();
			inFundOrder.setAmount(inAmount);
			inFundOrder.setFundFlow(FundFlowEnum.income.getValue());
			inFundOrder.setOrderTime(new Date());
			inFundOrder.setRefOrderCode(order.getOrderCode());
			inFundOrder.setRefOrderId(order.getId());
			inFundOrder.setRefOrderType(RefOrderTypeEnum.trader_transfer_order.getValue());
			inFundOrder.setTraderBalanceAccountId(order.getInAccountId());
			inFundOrder.setCreatedAt(new Date());
			inFundOrder.setUpdatedAt(new Date());
			inFundOrder.save();
		}
		TraderFundOrder outFundOrder = TraderFundOrder.dao.findByOrderId(order.getOutAccountId(), RefOrderTypeEnum.trader_transfer_order, order.getId());
		if(outFundOrder != null) { // 存在，则更新
			outFundOrder.setAmount(outAmount);
			outFundOrder.setOrderTime(new Date());
			outFundOrder.setUpdatedAt(new Date());
			outFundOrder.update();
			
		} else { // 不存在，则新增
			outFundOrder = new TraderFundOrder();
			outFundOrder.setAmount(outAmount);
			outFundOrder.setFundFlow(FundFlowEnum.expenses.getValue());
			outFundOrder.setOrderTime(new Date());
			outFundOrder.setRefOrderCode(order.getOrderCode());
			outFundOrder.setRefOrderId(order.getId());
			outFundOrder.setRefOrderType(RefOrderTypeEnum.trader_transfer_order.getValue());
			outFundOrder.setTraderBalanceAccountId(order.getOutAccountId());
			outFundOrder.setCreatedAt(new Date());
			outFundOrder.setUpdatedAt(new Date());
			outFundOrder.save();
		}
	}
	
	/**
	 * 设置单据审核状态
	 * @param tenantOrgId
	 * @param order
	 */
	private AuditStatusEnum getAuditStatus(TraderTransferOrder order) {
		TenantConfig auditConfig = TenantConfig.dao.findByKeyCache(TenantConfigEnum.audit_trader_transfer_order);
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
	
	
}