package com.bytechainx.psi.fund.service;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.bytechainx.psi.common.EnumConstant.AuditStatusEnum;
import com.bytechainx.psi.common.EnumConstant.ExportStatusEnum;
import com.bytechainx.psi.common.EnumConstant.FundFlowEnum;
import com.bytechainx.psi.common.EnumConstant.MsgDataTypeEnum;
import com.bytechainx.psi.common.EnumConstant.OrderStatusEnum;
import com.bytechainx.psi.common.EnumConstant.RefOrderTypeEnum;
import com.bytechainx.psi.common.EnumConstant.TenantConfigEnum;
import com.bytechainx.psi.common.kit.DateUtil;
import com.bytechainx.psi.common.kit.excel.XlsKit;
import com.bytechainx.psi.common.model.TenantConfig;
import com.bytechainx.psi.common.model.TenantExportLog;
import com.bytechainx.psi.common.model.TraderBalanceAccount;
import com.bytechainx.psi.common.model.TraderFundOrder;
import com.bytechainx.psi.common.model.TraderIncomeExpenses;
import com.bytechainx.psi.common.model.TraderIncomeExpensesFund;
import com.bytechainx.psi.common.model.TraderPayOrder;
import com.bytechainx.psi.common.service.base.CommonService;
import com.google.common.collect.Lists;
import com.jfinal.kit.Kv;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;


/**
* 支出单据
*/
public class FlowExpensesService extends CommonService {
	
	/**
	* 分页列表
	*/
	public Page<TraderIncomeExpenses> paginate(String startDay, String endDay, Kv conditionColumns, Integer pageNumber, int pageSize) {
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

		return TraderIncomeExpenses.dao.paginate(pageNumber, pageSize, "select * ", "from trader_income_expenses "+where.toString()+" order by id desc", params.toArray());
	}

	/**
	* 新增
	*/
	public Ret create(TraderIncomeExpenses order, List<TraderIncomeExpensesFund> orderFundList) {
		if( order == null) {
			return Ret.fail("参数错误");
		}
		if(orderFundList == null || orderFundList.isEmpty()) {
			return Ret.fail("收支明细不能为空");
		}
		if(order.getFundTypeId() == null || order.getFundTypeId() <= 0) {
			return Ret.fail("收支项目不能为空");
		}
		if(order.getOrderTime() == null) {
			return Ret.fail("单据时间不能为空");
		}
		// 设置审核状态
		AuditStatusEnum auditStatus = getAuditStatus(order);
		order.setAuditStatus(auditStatus.getValue());
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
		
		// 账户付款
		createOrderFund(order, orderFundList);
		
		return Ret.ok("新增支出单据成功").set("targetId", order.getId());
	}

	/**
	* 修改
	*/
	public Ret update(TraderIncomeExpenses order, List<TraderIncomeExpensesFund> orderFundList) {
		if(order == null || order.getId() == null || order.getId() <= 0) {
			return Ret.fail("参数错误");
		}
		if(orderFundList == null || orderFundList.isEmpty()) {
			return Ret.fail("收支明细不能为空");
		}
		if(order.getFundTypeId() == null || order.getFundTypeId() <= 0) {
			return Ret.fail("收支项目不能为空");
		}
		if(order.getOrderTime() == null) {
			return Ret.fail("单据时间不能为空");
		}
		
		TraderIncomeExpenses _order = TraderIncomeExpenses.dao.findById(order.getId());
		if(_order == null) {
			return Ret.fail("收支单据不存在，无法修改");
		}
		if(_order.getOrderStatus() == OrderStatusEnum.normal.getValue() && order.getOrderStatus() != OrderStatusEnum.normal.getValue()) {
			return Ret.fail("单据状态异常，无法修改"); 
		}
		// 审核状态要根据是否开启审核开关来设置，默认无需审核
		if (_order.getOrderStatus() == OrderStatusEnum.draft.getValue()) { // 如果之前是草稿单，则处理审核状态
			AuditStatusEnum auditStatus = getAuditStatus(order);
			order.setAuditStatus(auditStatus.getValue());
			// 发送消息给审核人
		    sendOrderNoticeMsg(order);
		    
		} else if(_order.getAuditStatus() == AuditStatusEnum.reject.getValue()) {
			order.setAuditStatus(AuditStatusEnum.waiting.getValue());
		} else {
			order.setAuditStatus(_order.getAuditStatus());
		}
		order.setUpdatedAt(new Date());
		order.update();
		
		// 更新订单付款资金
		updateOrderFund(order, orderFundList);
		
		return Ret.ok("修改支出单据成功").set("targetId", order.getId());
	}


	/**
	* 作废
	*/
	public Ret disable(List<Integer> ids) {
		if(ids == null || ids.isEmpty()) {
			return Ret.fail("参数错误");
		}
		for (Integer id : ids) {
			TraderIncomeExpenses order = TraderIncomeExpenses.dao.findById(id);
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
			for (TraderIncomeExpensesFund e : order.getOrderFundList()) {
				undoOrderFund(order, e);
			}
			// 删除交易流水
			Db.delete("delete from trader_fund_order where tenant_store_id = ? and ref_order_type = ? and ref_order_id = ?", RefOrderTypeEnum.trader_income_expenses.getValue(), order.getId());
			
		}
		
		return Ret.ok("作废支出单据成功");
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
			TraderIncomeExpenses order = TraderIncomeExpenses.dao.findById(id);
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
				updateOrderFund(order, order.getOrderFundList());
			}
			// 审核消息通知
			sendAuditNoticeMsg(order);
		}
		
		return Ret.ok("审核支出单据成功");
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
		TraderIncomeExpenses order = TraderIncomeExpenses.dao.findById(id);
		if(order == null) {
			return Ret.fail("单据不存在");
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
		String fileName = "收支单-"+DateUtil.getSecondNumber(new Date());
		TenantExportLog exportLog = new TenantExportLog();
		exportLog.setCreatedAt(new Date());
		exportLog.setFileName(fileName);
		exportLog.setErrorDesc("");
		exportLog.setExportStatus(ExportStatusEnum.ing.getValue());
		exportLog.setFilePath("");
		exportLog.setHandlerId(handlerId);
		exportLog.save();
		
		Thread thread = new Thread() {
			@Override
			public void run() {
				try {
					List<TraderIncomeExpenses> orderList = Lists.newArrayList();
					int pageNumber = 1;
					Page<TraderIncomeExpenses> page = paginate(startDay, endDay, condKv, pageNumber, 500);
					while (!page.getList().isEmpty() && orderList.size() < 10000) { // 最大不能超过10000
						orderList.addAll(page.getList());
						pageNumber += 1;
						page = paginate(startDay, endDay, condKv, pageNumber, 200);
					}
					StringBuffer headersBuffer = new StringBuffer();
					headersBuffer.append("单据日期,单据编号");
					StringBuffer columnsBuffer = new StringBuffer();
					columnsBuffer.append("order_time_day,order_code");
					
					headersBuffer.append(",收支项目,收支账户,收支金额,收支日期,收支合计,单据备注,经办人");
					columnsBuffer.append(",fund_type_name,list,amount,remark,handler_name");
					
					String[] headers = headersBuffer.toString().split(",");
					String[] columns = columnsBuffer.toString().split(",");
					
					String[] listColumns = new String[] {"trader_balance_account_name","amount","fund_time"};
					for (TraderIncomeExpenses order : orderList) {
						order.put("order_time_day", DateUtil.getDayStr(order.getOrderTime()));
						order.put("fund_type_name", order.getFundType().getName());
						
						List<TraderIncomeExpensesFund> fundList = order.getOrderFundList();
						order.put("list", fundList);
						for (TraderIncomeExpensesFund fund : fundList) {
							fund.put("trader_balance_account_name", fund.getBalanceAccount().getName());
							fund.put("fund_time", DateUtil.getDayStr(fund.getFundTime()));
						}
						order.put("handler_name", order.getHandler().getRealName());
					}
					
					String filePath = XlsKit.genOrderXls("收支单", fileName, headers, orderList, columns, listColumns);
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
			}
		};
		thread.start();
		
		return Ret.ok().set("targetId", exportLog.getId());
	}
	
	/**
	 * 处理付款资金，记录交易
	 * @param 
	 * @param order
	 * @param orderFundList
	 */
	private void createOrderFund(TraderIncomeExpenses order, List<TraderIncomeExpensesFund> orderFundList) {
		if(orderFundList == null || orderFundList.isEmpty()) {
			return;
		}
		// 账户付款
		for (TraderIncomeExpensesFund e : orderFundList) {
			if (e.getTraderBalanceAccountId() == null || e.getTraderBalanceAccountId() <= 0) {
				continue;
			}
			if (e.getAmount() == null || e.getAmount().doubleValue() <= 0) {
				continue;
			}
			e.setTraderIncomeExpensesId(order.getId());
			e.setCreatedAt(new Date());
			e.setUpdatedAt(new Date());
			e.save();
			
			paymentOrderFund(order, e);
		}
	}
	
	/**
	 * 帐户资金支付处理
	 * @param 
	 * @param order
	 * @param orderFund
	 */
	private void paymentOrderFund(TraderIncomeExpenses order, TraderIncomeExpensesFund orderFund) {
		if(orderFund.getTraderBalanceAccountId() == null || orderFund.getTraderBalanceAccountId() <= 0 || orderFund.getAmount() == null || orderFund.getAmount().doubleValue() <= 0) {
			return;
		}
		TraderIncomeExpensesFund _orderFund = TraderIncomeExpensesFund.dao.findFirst("select * from trader_income_expenses_fund where id = ?", orderFund.getId());
		if(_orderFund != null) {
			_orderFund.setAmount(orderFund.getAmount());
			_orderFund.setTraderBalanceAccountId(orderFund.getTraderBalanceAccountId());
			_orderFund.setFundTime(orderFund.getFundTime());
			_orderFund.setUpdatedAt(new Date());
			_orderFund.update();
			
		} else {
			orderFund.setTraderIncomeExpensesId(order.getId());
			orderFund.setUpdatedAt(new Date());
			orderFund.setCreatedAt(new Date());
			orderFund.save();
		}
		if(order.getAuditStatus() != AuditStatusEnum.pass.getValue()) { // 不是已审核状态，不处理资金
			return;
		}
		// 结算帐户余额计算
		TraderBalanceAccount balanceAccount = TraderBalanceAccount.dao.findById(orderFund.getTraderBalanceAccountId());
		BigDecimal amount = BigDecimal.ZERO;
		if(order.getFundFlow() == FundFlowEnum.income.getValue()) { // 收入
			amount = balanceAccount.getBalance().add(orderFund.getAmount()); // 收入的金额
			
		} else if(order.getFundFlow() == FundFlowEnum.expenses.getValue()) { // 支出
			amount = balanceAccount.getBalance().subtract(orderFund.getAmount()); // 支付的金额
		}
		balanceAccount.setBalance(amount);
		balanceAccount.setUpdatedAt(new Date());
		balanceAccount.update();
		
		// 生成交易流水
		TraderFundOrder fundOrder = TraderFundOrder.dao.findByOrderId(orderFund.getTraderBalanceAccountId(), RefOrderTypeEnum.trader_income_expenses, order.getId());
		if(fundOrder != null) { // 存在，则更新
			fundOrder.setAmount(orderFund.getAmount());
			fundOrder.setOrderTime(new Date());
			fundOrder.setUpdatedAt(new Date());
			fundOrder.update();
			
		} else { // 不存在，则新增
			fundOrder = new TraderFundOrder();
			fundOrder.setAmount(orderFund.getAmount());
			fundOrder.setFundFlow(order.getFundFlow());
			fundOrder.setOrderTime(new Date());
			fundOrder.setRefOrderCode(order.getOrderCode());
			fundOrder.setRefOrderId(order.getId());
			fundOrder.setRefOrderType(RefOrderTypeEnum.trader_income_expenses.getValue());
			fundOrder.setTraderBalanceAccountId(orderFund.getTraderBalanceAccountId());
			fundOrder.setCreatedAt(new Date());
			fundOrder.setUpdatedAt(new Date());
			fundOrder.save();
		}
	}
	
	/**
	 * 更新付款资金
	 * @param 
	 * @param order
	 * @param orderFundList
	 * @param changeAmount
	 */
	private void updateOrderFund(TraderIncomeExpenses order, List<TraderIncomeExpensesFund> orderFundList) {
		if(orderFundList == null) {
			orderFundList = new ArrayList<>();
		}
		List<TraderIncomeExpensesFund> oldOrderFundList = TraderIncomeExpensesFund.dao.find("select * from trader_income_expenses_fund where trader_income_expenses_id = ?", order.getId());
		// 先过滤出已删除的记录
		List<TraderIncomeExpensesFund> deleteFundList = new ArrayList<>();
		for(TraderIncomeExpensesFund oldFund : oldOrderFundList) {
			boolean isExist = false;
			for (TraderIncomeExpensesFund fund : orderFundList) {
				if(fund.getId() != null && fund.getId().intValue() == oldFund.getId().intValue()) {
					isExist = true;
					break;
				}
			}
			if(!isExist) { // 需要删除的记录
				deleteFundList.add(oldFund);
			}
		}
		
		for (TraderIncomeExpensesFund e : deleteFundList) {
			// 结算帐户余额计算
			undoOrderFund(order, e);
			// 删除资金记录
			e.delete();
		}
		// 账户付款，更新结算帐户资金
		for (TraderIncomeExpensesFund e : orderFundList) {
			paymentOrderFund(order, e);
		}
		
	}
	
	/**
	 * 回退资金到帐户
	 * @param 
	 * @param order
	 * @param e
	 */
	private void undoOrderFund(TraderIncomeExpenses order, TraderIncomeExpensesFund orderFund) {
		if(order.getAuditStatus() != AuditStatusEnum.pass.getValue()) { // 非审核不处理资金
			return;
		}
		TraderBalanceAccount balanceAccount = TraderBalanceAccount.dao.findById(orderFund.getTraderBalanceAccountId());
		BigDecimal amount = BigDecimal.ZERO;
		if(order.getFundFlow() == FundFlowEnum.income.getValue()) { // 收入
			amount = balanceAccount.getBalance().subtract(orderFund.getAmount()); // 回退支付的金额
			
		} else if(order.getFundFlow() == FundFlowEnum.expenses.getValue()) { // 支出
			amount = balanceAccount.getBalance().add(orderFund.getAmount()); // 回退支付的金额
		}
		balanceAccount.setBalance(amount);
		balanceAccount.setUpdatedAt(new Date());
		balanceAccount.update();
		
	}
	
	/**
	 * 设置单据审核状态
	 * @param 
	 * @param order
	 */
	private AuditStatusEnum getAuditStatus(TraderIncomeExpenses order) {
		TenantConfig auditConfig = TenantConfig.dao.findByKeyCache(TenantConfigEnum.audit_trader_income_expenses);
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
	 * 发送审核结果通知
	 * @param 
	 * @param order
	 */
	private void sendAuditNoticeMsg(TraderIncomeExpenses order) {
		if(order.getFundFlow() == FundFlowEnum.income.getValue()) {
			String title = "日常收入单审核通过";
			String content = title;
			if (order.getAuditStatus() == AuditStatusEnum.reject.getValue()) {
				title = "日常收入单审核拒绝";
				content = title;
			}
			Boolean smsFlag = TraderIncomeExpenses.dao.findAuditHandlerSmsNoticeConfig();// 发送短信
			Boolean sysFlag = TraderIncomeExpenses.dao.findAuditHandlerSysNoticeConfig();// 发送系统消息
			sendNoticeMsg(MsgDataTypeEnum.income_order_audit, title, content, order.getHandlerId(), smsFlag, sysFlag);
			
			smsFlag = TraderIncomeExpenses.dao.findAuditMakeManSmsNoticeConfig();// 发送短信
			sysFlag = TraderIncomeExpenses.dao.findAuditMakeManSysNoticeConfig();// 发送系统消息
			sendNoticeMsg(MsgDataTypeEnum.income_order_audit, title, content, order.getMakeManId(), smsFlag, sysFlag);
			
		} else if(order.getFundFlow() == FundFlowEnum.expenses.getValue()) {
			String title = "日常支出单审核通过";
			String content = title;
			if (order.getAuditStatus() == AuditStatusEnum.reject.getValue()) {
				title = "日常支出单审核拒绝";
				content = title;
			}
			Boolean smsFlag = TraderIncomeExpenses.dao.findAuditHandlerSmsNoticeConfig();// 发送短信
			Boolean sysFlag = TraderIncomeExpenses.dao.findAuditHandlerSysNoticeConfig();// 发送系统消息
			sendNoticeMsg(MsgDataTypeEnum.expenses_order_audit, title, content, order.getHandlerId(), smsFlag, sysFlag);
			
			smsFlag = TraderIncomeExpenses.dao.findAuditMakeManSmsNoticeConfig();// 发送短信
			sysFlag = TraderIncomeExpenses.dao.findAuditMakeManSysNoticeConfig();// 发送系统消息
			sendNoticeMsg(MsgDataTypeEnum.expenses_order_audit, title, content, order.getMakeManId(), smsFlag, sysFlag);
		}
	}
	
	/**
	 * 发送消息给审核人
	 * @param 
	 * @param order
	 */
	private void sendOrderNoticeMsg(TraderIncomeExpenses order) {
		if(order.getAuditStatus() != AuditStatusEnum.waiting.getValue() || order.getOrderStatus() != OrderStatusEnum.normal.getValue()) {
			return;
		}
		Integer	auditorId = TraderPayOrder.dao.findAuditorIdConfig();
		order.setAuditorId(auditorId); // 设置审核人
		Boolean smsFlag = TraderIncomeExpenses.dao.findAuditSmsNoticeConfig();// 发送短信
		Boolean sysFlag = TraderIncomeExpenses.dao.findAuditSysNoticeConfig();// 发送系统消息
		if(order.getFundFlow() == FundFlowEnum.income.getValue()) {
			String title="日常收入单审核通知";
			String content = title;
			sendNoticeMsg(MsgDataTypeEnum.income_order_audit, title, content, order.getAuditorId(), smsFlag, sysFlag);
			
		} else if(order.getFundFlow() == FundFlowEnum.expenses.getValue()) {
			String title="日常支出单审核通知";
			String content = title;
			sendNoticeMsg(MsgDataTypeEnum.expenses_order_audit, title, content, order.getAuditorId(), smsFlag, sysFlag);
		}
	}

}