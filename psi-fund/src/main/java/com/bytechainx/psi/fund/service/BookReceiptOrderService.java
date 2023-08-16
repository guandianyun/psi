package com.bytechainx.psi.fund.service;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.bytechainx.psi.common.EnumConstant.AuditStatusEnum;
import com.bytechainx.psi.common.EnumConstant.CheckingRefOrderTypeEnum;
import com.bytechainx.psi.common.EnumConstant.ExportStatusEnum;
import com.bytechainx.psi.common.EnumConstant.FundFlowEnum;
import com.bytechainx.psi.common.EnumConstant.FundTypeEnum;
import com.bytechainx.psi.common.EnumConstant.MsgDataTypeEnum;
import com.bytechainx.psi.common.EnumConstant.OrderPayStatusEnum;
import com.bytechainx.psi.common.EnumConstant.OrderStatusEnum;
import com.bytechainx.psi.common.EnumConstant.RefOrderTypeEnum;
import com.bytechainx.psi.common.EnumConstant.TenantConfigEnum;
import com.bytechainx.psi.common.kit.DateUtil;
import com.bytechainx.psi.common.kit.excel.XlsKit;
import com.bytechainx.psi.common.model.CustomerInfo;
import com.bytechainx.psi.common.model.SaleOrder;
import com.bytechainx.psi.common.model.SaleOrderFund;
import com.bytechainx.psi.common.model.TenantConfig;
import com.bytechainx.psi.common.model.TenantExportLog;
import com.bytechainx.psi.common.model.TraderBalanceAccount;
import com.bytechainx.psi.common.model.TraderBookAccount;
import com.bytechainx.psi.common.model.TraderBookAccountLogs;
import com.bytechainx.psi.common.model.TraderCustomerReceivable;
import com.bytechainx.psi.common.model.TraderFundOrder;
import com.bytechainx.psi.common.model.TraderReceiptOrder;
import com.bytechainx.psi.common.model.TraderReceiptOrderFund;
import com.bytechainx.psi.common.model.TraderReceiptOrderRef;
import com.bytechainx.psi.common.service.base.CommonService;
import com.google.common.collect.Lists;
import com.jfinal.kit.Kv;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;


/**
* 收款单
*/
public class BookReceiptOrderService extends CommonService {

	/**
	* 分页列表
	*/
	public Page<TraderReceiptOrder> paginate(String startDay, String endDay, Kv conditionColumns, Integer pageNumber, int pageSize) {
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

		return TraderReceiptOrder.dao.paginate(pageNumber, pageSize, "select * ", "from trader_receipt_order "+where.toString()+" order by id desc", params.toArray());
	}

	/**
	* 新增
	*/
	public Ret create(TraderReceiptOrder order, List<TraderReceiptOrderFund> orderFundList, List<TraderReceiptOrderRef> orderRefList) {
		if( order == null) {
			return Ret.fail("参数错误");
		}
		if(order.getCustomerInfoId() == null || order.getCustomerInfoId() <= 0) {
			return Ret.fail("客户不能为空");
		}
		if(order.getAmount() == null || order.getAmount().doubleValue() <= 0) {
			return Ret.fail("收款金额不能为空");
		}
		if(order.getOrderTime() == null) {
			return Ret.fail("单据时间不能为空");
		}
		if(orderFundList == null || orderFundList.isEmpty()) {
			return Ret.fail("收款金额不能为空"); 
		}
		if(order.getCheckAmount() != null && order.getCheckAmount().compareTo(order.getAmount()) > 0) {
			return Ret.fail("实际核销金额 不能大于 收款合计金额"); 
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
		
		// 账户收款
		createOrderFund(order, orderFundList);
		// 核销的销售单
		createCheckingOrder(order, orderRefList);
		// 记录对账单
		updateCustomerReceivable(order);
		return Ret.ok("新增收款单成功").set("targetId", order.getId());
	}

	/**
	* 修改
	*/
	public Ret update(TraderReceiptOrder order, List<TraderReceiptOrderFund> orderFundList, List<TraderReceiptOrderRef> orderRefList) {
		if(order == null || order.getId() == null || order.getId() <= 0) {
			return Ret.fail("参数错误");
		}
		if(order.getCustomerInfoId() == null || order.getCustomerInfoId() <= 0) {
			return Ret.fail("客户不能为空");
		}
		if(order.getAmount() == null || order.getAmount().doubleValue() <= 0) {
			return Ret.fail("收款金额不能为空");
		}
		if(order.getOrderTime() == null) {
			return Ret.fail("单据时间不能为空");
		}
		if(orderFundList == null || orderFundList.isEmpty()) {
			return Ret.fail("收款金额不能为空"); 
		}
		if(order.getCheckAmount() != null && order.getCheckAmount().compareTo(order.getAmount()) > 0) {
			return Ret.fail("实际核销金额 不能大于 收款合计金额"); 
		}
		
		TraderReceiptOrder _order = TraderReceiptOrder.dao.findById(order.getId());
		if(_order == null) {
			return Ret.fail("收款单不存在，无法修改");
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
		
		BigDecimal changeCheckAmount = BigDecimal.ZERO;
		if(order.getCheckAmount() != null) {
			changeCheckAmount = order.getCheckAmount().subtract(_order.getCheckAmount());
		}
		// 更新订单收款资金
		updateOrderFund(order, orderFundList, changeCheckAmount);
		// 记录对账单
		updateCustomerReceivable(order);
		// 处理核销的销售单
		updateCheckingOrder(order, orderRefList, false);
		
		return Ret.ok("修改收款单成功").set("targetId", order.getId());
	}


	/**
	* 作废
	*/
	public Ret disable(List<Integer> ids) {
		if(ids == null || ids.isEmpty()) {
			return Ret.fail("参数错误");
		}
		for (Integer id : ids) {
			TraderReceiptOrder order = TraderReceiptOrder.dao.findById(id);
			if(order == null) {
				continue;
			}
			if(order.getOrderStatus() == OrderStatusEnum.disable.getValue()) { // 已作废的单据不能重复作废
				continue;
			}
			order.setOrderStatus(OrderStatusEnum.disable.getValue());
			order.setUpdatedAt(new Date());
			order.update();
			
			if(order.getAuditStatus() != AuditStatusEnum.pass.getValue()) { // 不是已审核状态，不处理资金
				continue;
			}
			// 回退往来帐户支付金额
			updateTraderBookAccountPayAmount(order, BigDecimal.ZERO.subtract(order.getCheckAmount()));
			
			// 资金回退
			for (TraderReceiptOrderFund e : order.getOrderFundList()) {
				undoOrderFund(order, e);
			}
			// 删除交易流水
			Db.delete("delete from trader_fund_order where tenant_store_id = ? and ref_order_type = ? and ref_order_id = ?", RefOrderTypeEnum.trader_receipt_order.getValue(), order.getId());
			
			// 处理核销的销售单
			for (TraderReceiptOrderRef ref : order.getOrderRefList()) {
				undoSaleOrderChecking(order, ref);
			}
			// 记录对账单
			updateCustomerReceivable(order);
			
		}
		return Ret.ok("作废收款单成功");
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
			TraderReceiptOrder order = TraderReceiptOrder.dao.findById(id);
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
				updateOrderFund(order, order.getOrderFundList(), order.getCheckAmount());
				updateCheckingOrder(order, order.getOrderRefList(), true);
			}
			// 记录对账单
			updateCustomerReceivable(order);
			// 发送审核结果通知
			sendAuditNoticeMsg(order);
			
		}
		return Ret.ok("审核收款单成功");
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
		TraderReceiptOrder order = TraderReceiptOrder.dao.findById(id);
		if(order == null) {
			return Ret.fail("收款单不存在");
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
		String fileName = "收款单-"+DateUtil.getSecondNumber(new Date());
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
					List<TraderReceiptOrder> orderList = Lists.newArrayList();
					int pageNumber = 1;
					Page<TraderReceiptOrder> page = paginate(startDay, endDay, condKv, pageNumber, 500);
					while (!page.getList().isEmpty() && orderList.size() < 10000) { // 最大不能超过10000
						orderList.addAll(page.getList());
						pageNumber += 1;
						page = paginate(startDay, endDay, condKv, pageNumber, 200);
					}
					StringBuffer headersBuffer = new StringBuffer();
					headersBuffer.append("单据日期,单据编号");
					StringBuffer columnsBuffer = new StringBuffer();
					columnsBuffer.append("order_time_day,order_code");
					
					headersBuffer.append(",客户名称,收款账户,收款金额,收款日期,收款合计,核销金额,优惠金额,核销单据,单据备注,经办人");
					columnsBuffer.append(",customer_info_name,list,amount,order_amount,discount_amount,order_list,remark,handler_name");
					
					String[] headers = headersBuffer.toString().split(",");
					String[] columns = columnsBuffer.toString().split(",");
					
					String[] listColumns = new String[] {"trader_balance_account_name","amount","fund_time"};
					for (TraderReceiptOrder order : orderList) {
						order.put("order_time_day", DateUtil.getDayStr(order.getOrderTime()));
						order.put("customer_info_name", order.getCustomerInfo().getName());
						
						List<TraderReceiptOrderFund> fundList = order.getOrderFundList();
						order.put("list", fundList);
						for (TraderReceiptOrderFund fund : fundList) {
							fund.put("trader_balance_account_name", fund.getBalanceAccount().getName());
							fund.put("fund_time", DateUtil.getDayStr(fund.getFundTime()));
						}
						StringBuffer orderListBuffer = new StringBuffer();
						List<TraderReceiptOrderRef> payOrderRefList = order.getOrderRefList();
						for (TraderReceiptOrderRef ref : payOrderRefList) {
							orderListBuffer.append(ref.getSaleOrder().getOrderCode()+":"+ref.getAmount().stripTrailingZeros().toPlainString()+"\r\n");
						}
						if(orderListBuffer.length() > 0) {
							order.put("order_list", orderListBuffer.substring(0, orderListBuffer.length() - 1).toString());
						}
						
						order.put("handler_name", order.getHandler().getRealName());
					}
					
					String filePath = XlsKit.genOrderXls("收款单", fileName, headers, orderList, columns, listColumns);
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
	 * 核销销售单处理
	 * @param 
	 * @param order
	 * @param orderRefList
	 */
	private void createCheckingOrder(TraderReceiptOrder order, List<TraderReceiptOrderRef> orderRefList) {
		if(orderRefList == null || orderRefList.isEmpty()) {
			return;
		}
		for (TraderReceiptOrderRef ref : orderRefList) {
			if(ref.getAmount() == null || ref.getAmount().doubleValue() <= 0) {
				continue;
			}
			if(ref.getSaleOrderId() == null || ref.getSaleOrderId() <= 0) {
				continue;
			}
			
			SaleOrder saleOrder = SaleOrder.dao.findById(ref.getSaleOrderId());
			if(saleOrder == null) { // 销售单不存在
				continue;
			}
			if(ref.getDiscountAmount() == null) {
				ref.setDiscountAmount(BigDecimal.ZERO);
			}
			ref.setTraderReceiptOrderId(order.getId());
			ref.save();
			
			if(order.getAuditStatus() != AuditStatusEnum.pass.getValue()) {
				continue;
			}
			// 处理销售单核销
			updateSaleOrderChecking(order, ref, saleOrder, ref.getAmount(), ref.getDiscountAmount());
		}
	}


	/**
	 * 处理收款资金，记录交易
	 * @param 
	 * @param order
	 * @param orderFundList
	 */
	private void createOrderFund(TraderReceiptOrder order, List<TraderReceiptOrderFund> orderFundList) {
		// 更新来往账户支付的金额
		updateTraderBookAccountPayAmount(order, order.getCheckAmount());
		
		if(orderFundList == null || orderFundList.isEmpty()) {
			return;
		}
		// 账户收款
		for (TraderReceiptOrderFund e : orderFundList) {
			if (e.getTraderBalanceAccountId() == null || e.getTraderBalanceAccountId() <= 0) {
				continue;
			}
			if (e.getAmount() == null || e.getAmount().doubleValue() <= 0) {
				continue;
			}
			paymentOrderFund(order, e);
		}
	}
	
	/**
	 * 处理核销的销售单
	 * @param 
	 * @param order
	 * @param orderRefList
	 */
	private void updateCheckingOrder(TraderReceiptOrder order, List<TraderReceiptOrderRef> orderRefList, boolean auditFlag) {
		if (orderRefList == null || orderRefList.isEmpty()) {
			orderRefList = new ArrayList<>();
		}
		List<TraderReceiptOrderRef> oldRefList = TraderReceiptOrderRef.dao.find("select * from trader_receipt_order_ref where trader_receipt_order_id = ?", order.getId());
		// 先过滤出已删除的记录
		List<TraderReceiptOrderRef> deleteRefList = new ArrayList<>();
		for(TraderReceiptOrderRef oldRef : oldRefList) {
			boolean isExist = false;
			for (TraderReceiptOrderRef ref : orderRefList) {
				if(ref.getId() != null && ref.getId().intValue() == oldRef.getId().intValue()) {
					isExist = true;
					break;
				}
				if(ref.getSaleOrderId() != null && ref.getSaleOrderId().intValue() == oldRef.getSaleOrderId().intValue()) {
					isExist = true;
					break;
				}
			}
			if(!isExist) { // 需要删除的记录
				deleteRefList.add(oldRef);
			}
		}
		// 处理需要删除的核销单
		for (TraderReceiptOrderRef ref : deleteRefList) {
			undoSaleOrderChecking(order, ref);
			ref.delete();
		}
		
		// 处理核销的销售单
		for (TraderReceiptOrderRef ref : orderRefList) {
			if (ref.getAmount() == null || ref.getAmount().doubleValue() <= 0) {
				continue;
			}
			if (ref.getSaleOrderId() == null || ref.getSaleOrderId() <= 0) {
				continue;
			}
			SaleOrder saleOrder = SaleOrder.dao.findById(ref.getSaleOrderId());
			if(saleOrder == null) { // 销售单不存在
				continue;
			}
			
			BigDecimal changeAmount = ref.getAmount();
			BigDecimal changeDiscountAmount = ref.getDiscountAmount();
			TraderReceiptOrderRef _ref = TraderReceiptOrderRef.dao.findFirst("select * from trader_receipt_order_ref where trader_receipt_order_id = ? and sale_order_id = ? ", order.getId(), ref.getSaleOrderId());
			if(_ref != null) {
				if(!auditFlag) { // 是否审核操作，审核操作不要计算差值
					changeAmount = ref.getAmount().subtract(_ref.getAmount());
					changeDiscountAmount = ref.getDiscountAmount().subtract(_ref.getDiscountAmount());
				}
				if(ref.getDiscountAmount() == null) {
					_ref.setDiscountAmount(BigDecimal.ZERO);
				} else {
					_ref.setDiscountAmount(ref.getDiscountAmount());
				}
				_ref.setAmount(ref.getAmount());
				_ref.update();
				
			} else {
				if(ref.getDiscountAmount() == null) {
					ref.setDiscountAmount(BigDecimal.ZERO);
				}
				ref.setTraderReceiptOrderId(order.getId());
				ref.save();
			}
			// 处理销售单核销
			updateSaleOrderChecking(order, ref, saleOrder, changeAmount, changeDiscountAmount);
		}
	}

	/**
	 * 处理销售单核销
	 * @param 
	 * @param order
	 * @param ref
	 * @param saleOrder
	 * @param changeAmount
	 * @param changeDiscountAmount
	 */
	private void updateSaleOrderChecking(TraderReceiptOrder order, TraderReceiptOrderRef ref, SaleOrder saleOrder, BigDecimal changeAmount, BigDecimal changeDiscountAmount) {
		if(order.getAuditStatus() != AuditStatusEnum.pass.getValue()) {
			return;
		}
		// 处理销售单已付金额，更新销售单状态
		saleOrder.setOddAmount(saleOrder.getOddAmount().add(changeDiscountAmount));
		saleOrder.setAmount(saleOrder.getAmount().subtract(changeDiscountAmount)); // 减去核销优惠金额

		saleOrder.setPaidAmount(saleOrder.getPaidAmount().add(changeAmount));
		if (saleOrder.getPaidAmount().compareTo(saleOrder.getAmount()) >= 0) { // 已付清
			saleOrder.setPayStatus(OrderPayStatusEnum.finish.getValue());
		} else { // 部分付
			saleOrder.setPayStatus(OrderPayStatusEnum.part.getValue());
		}
		saleOrder.setUpdatedAt(new Date());
		saleOrder.update();

		// 生成销售单收款明细
		SaleOrderFund saleOrderFund = SaleOrderFund.dao.findFirst("select * from sale_order_fund where sale_order_id = ? and balance_account_id = ? and receipt_type = ? limit 1", saleOrder.getId(),
				order.getId(), FundTypeEnum.checking.getValue());
		if (saleOrderFund != null) {
			saleOrderFund.setAmount(ref.getAmount());
			saleOrderFund.setReceiptTime((order.getOrderTime()));
			saleOrderFund.setUpdatedAt(new Date());
			saleOrderFund.update();

		} else {
			saleOrderFund = new SaleOrderFund();
			saleOrderFund.setAmount(ref.getAmount());
			saleOrderFund.setCreatedAt(new Date());
			saleOrderFund.setReceiptTime(new Date());
			saleOrderFund.setReceiptType((FundTypeEnum.checking.getValue()));
			saleOrderFund.setSaleOrderId(saleOrder.getId());
			saleOrderFund.setBalanceAccountId(order.getId());
			saleOrderFund.setUpdatedAt(new Date());
			saleOrderFund.save();
		}
	}
	
	/**
	 * 回退销售单核销
	 * @param 
	 * @param order
	 * @param ref
	 */
	private void undoSaleOrderChecking(TraderReceiptOrder order, TraderReceiptOrderRef ref) {
		if(order.getAuditStatus() != AuditStatusEnum.pass.getValue()) {
			return;
		}
		SaleOrder saleOrder = SaleOrder.dao.findById(ref.getSaleOrderId());
		if(saleOrder == null) { // 销售单不存在
			return;
		}
		// 处理销售单已付金额，更新销售单状态
		saleOrder.setOddAmount(saleOrder.getOddAmount().subtract(ref.getDiscountAmount()));
		saleOrder.setAmount(saleOrder.getAmount().add(ref.getDiscountAmount())); // 回退核销优惠金额
		
		saleOrder.setPaidAmount(saleOrder.getPaidAmount().subtract(ref.getAmount()));
		if(saleOrder.getPaidAmount().compareTo(BigDecimal.ZERO) <= 0) { // 未付
			saleOrder.setPayStatus(OrderPayStatusEnum.no.getValue());
			
		} else if(saleOrder.getPaidAmount().compareTo(saleOrder.getAmount()) < 0) { // 部分付
			saleOrder.setPayStatus(OrderPayStatusEnum.part.getValue());
		}
		saleOrder.setUpdatedAt(new Date());
		saleOrder.update();
		
		// 删除核销的资金明细
		Db.delete("delete from sale_order_fund where sale_order_id = ? and balance_account_id = ? and receipt_type = ? limit 1", ref.getSaleOrderId(), order.getId(), FundTypeEnum.checking.getValue());
	}

	/**
	 * 更新收款资金
	 * @param 
	 * @param order
	 * @param orderFundList
	 * @param changeCheckAmount 
	 * @param changeAmount
	 */
	private void updateOrderFund(TraderReceiptOrder order, List<TraderReceiptOrderFund> orderFundList, BigDecimal changeCheckAmount) {
		// 更新来往账户支付的金额
		updateTraderBookAccountPayAmount(order, changeCheckAmount);
				
		if(orderFundList == null) {
			orderFundList = new ArrayList<>();
		}
		List<TraderReceiptOrderFund> oldOrderFundList = TraderReceiptOrderFund.dao.find("select * from trader_receipt_order_fund where trader_receipt_order_id = ?", order.getId());
		// 先过滤出已删除的记录
		List<TraderReceiptOrderFund> deleteFundList = new ArrayList<>();
		for(TraderReceiptOrderFund oldFund : oldOrderFundList) {
			boolean isExist = false;
			for (TraderReceiptOrderFund fund : orderFundList) {
				if(fund.getId() != null && fund.getId().intValue() == oldFund.getId().intValue()) {
					isExist = true;
					break;
				}
				if(fund.getTraderBalanceAccountId().intValue() == oldFund.getTraderBalanceAccountId().intValue()) { // 同一个结算帐户
					isExist = true;
					break;
				}
			}
			if(!isExist) { // 需要删除的记录
				deleteFundList.add(oldFund);
			}
		}
		
		for (TraderReceiptOrderFund e : deleteFundList) {
			// 结算帐户余额计算
			undoOrderFund(order, e);
			// 删除资金记录
			e.delete();
		}
		// 账户收款，更新结算帐户资金
		for (TraderReceiptOrderFund e : orderFundList) {
			paymentOrderFund(order, e);
		}
		
	}

	/**
	 * 回退资金到帐户
	 * @param 
	 * @param order
	 * @param e
	 */
	private void undoOrderFund(TraderReceiptOrder order, TraderReceiptOrderFund orderFund) {
		if(order.getAuditStatus() != AuditStatusEnum.pass.getValue()) { // 非审核不处理资金
			return;
		}
		TraderBalanceAccount balanceAccount = TraderBalanceAccount.dao.findById(orderFund.getTraderBalanceAccountId());
		balanceAccount.setBalance(balanceAccount.getBalance().subtract(orderFund.getAmount())); // 回退收款的金额
		balanceAccount.setUpdatedAt(new Date());
		balanceAccount.update();
		
		BigDecimal changeAmount = BigDecimal.ZERO.subtract(orderFund.getAmount()); // 回退收款的金额，收款的时候是增加，退款的时候是减少
		updateTraderBookAccount(order, changeAmount);
	}
	
	/**
	 * 帐户资金收款处理
	 * @param 
	 * @param order
	 * @param orderFund
	 */
	private void paymentOrderFund(TraderReceiptOrder order, TraderReceiptOrderFund orderFund) {
		if(orderFund.getTraderBalanceAccountId() == null || orderFund.getTraderBalanceAccountId() <= 0 || orderFund.getAmount() == null || orderFund.getAmount().doubleValue() <= 0) {
			return;
		}
		BigDecimal changeFundAmount = orderFund.getAmount(); // 变更的金额
		TraderReceiptOrderFund _orderFund = TraderReceiptOrderFund.dao.findFirst("select * from trader_receipt_order_fund where trader_receipt_order_id = ? and id = ?", order.getId(), orderFund.getId());
		if(_orderFund != null) {
			changeFundAmount = orderFund.getAmount().subtract(_orderFund.getAmount()); // 计算差额
			_orderFund.setAmount(orderFund.getAmount());
			_orderFund.setFundTime(orderFund.getFundTime());
			_orderFund.setUpdatedAt(new Date());
			_orderFund.update();
			
		} else {
			orderFund.setTraderReceiptOrderId(order.getId());
			orderFund.setUpdatedAt(new Date());
			orderFund.setCreatedAt(new Date());
			orderFund.save();
		}
		if(order.getAuditStatus() != AuditStatusEnum.pass.getValue()) { // 不是已审核状态，不处理资金
			return;
		}
		// 结算帐户余额计算
		TraderBalanceAccount balanceAccount = TraderBalanceAccount.dao.findById(orderFund.getTraderBalanceAccountId());
		balanceAccount.setBalance(balanceAccount.getBalance().add(changeFundAmount)); // 增加收款的金额
		balanceAccount.setUpdatedAt(new Date());
		balanceAccount.update();
		
		// 更新往来帐户资金
		updateTraderBookAccount(order, changeFundAmount);
		
		// 生成交易流水
		TraderFundOrder fundOrder = TraderFundOrder.dao.findByOrderId(orderFund.getTraderBalanceAccountId(), RefOrderTypeEnum.trader_receipt_order, order.getId());
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
			fundOrder.setRefOrderType(RefOrderTypeEnum.trader_receipt_order.getValue());
			fundOrder.setTraderBalanceAccountId(orderFund.getTraderBalanceAccountId());
			fundOrder.setCreatedAt(new Date());
			fundOrder.setUpdatedAt(new Date());
			fundOrder.save();
		}
	}

	/**
	 * 更新往来帐户资金
	 * @param 
	 * @param order
	 * @param changeAmount
	 */
	private void updateTraderBookAccount(TraderReceiptOrder order, BigDecimal changeAmount) {
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
	 * 更新往来帐户资金，支付核销出去的金额
	 * @param 
	 * @param order
	 * @param changeAmount
	 */
	private void updateTraderBookAccountPayAmount(TraderReceiptOrder order, BigDecimal changeAmount) {
		if(order.getAuditStatus() != AuditStatusEnum.pass.getValue()) { // 非审核不处理资金
			return;
		}
		if(changeAmount == null || changeAmount.compareTo(BigDecimal.ZERO) == 0) {
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
	 * 客户应付对账
	 * @param 
	 * @param order
	 */
	private void updateCustomerReceivable(TraderReceiptOrder order) {
		if(order.getAuditStatus() != AuditStatusEnum.pass.getValue()) { // 非审核不处理资金
			return;
		}
		TraderCustomerReceivable customerReceivable = TraderCustomerReceivable.dao.findByOrderId(CheckingRefOrderTypeEnum.trader_receipt_order, order.getId());
		if(customerReceivable != null) {
			if(order.getOrderStatus() == OrderStatusEnum.disable.getValue()) {
				customerReceivable.delete();
				return;
			}
			customerReceivable.setNewAmount(order.getAmount());
			customerReceivable.setTakeAmount(order.getAmount());
			customerReceivable.setDiscountAmount(order.getDiscountAmount());
			customerReceivable.setAdjustAmount(BigDecimal.ZERO);
			customerReceivable.setOrderTime(order.getOrderTime());
			customerReceivable.setUpdatedAt(new Date());
			customerReceivable.update();
			
		} else {
			CustomerInfo customerInfo = CustomerInfo.dao.findById(order.getCustomerInfoId());
			customerReceivable = new TraderCustomerReceivable();
			customerReceivable.setRefOrderCode(order.getOrderCode());
			customerReceivable.setRefOrderId(order.getId());
			customerReceivable.setRefOrderType(CheckingRefOrderTypeEnum.trader_receipt_order.getValue());
			customerReceivable.setCustomerInfoId(order.getCustomerInfoId());
			customerReceivable.setTraderBookAccountId(customerInfo.getTraderBookAccountId());
			customerReceivable.setDiscountAmount(order.getDiscountAmount());
			customerReceivable.setNewAmount(order.getAmount());
			customerReceivable.setTakeAmount(order.getAmount());
			customerReceivable.setAdjustAmount(BigDecimal.ZERO);
			customerReceivable.setOrderTime(order.getOrderTime());
			customerReceivable.setCreatedAt(new Date());
			customerReceivable.setUpdatedAt(new Date());
			customerReceivable.save();
		}
	}

	
	/**
	 * 设置单据审核状态
	 * @param 
	 * @param order
	 */
	private AuditStatusEnum getAuditStatus(TraderReceiptOrder order) {
		TenantConfig auditConfig = TenantConfig.dao.findByKeyCache(TenantConfigEnum.audit_trader_receipt_order);
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
	private void sendAuditNoticeMsg(TraderReceiptOrder order) {
		CustomerInfo customerInfo = order.getCustomerInfo();
		String title = "客户“" + customerInfo.getName() + "”收款单审核通过";
		String content = title;
		if (order.getAuditStatus() == AuditStatusEnum.reject.getValue()) {
			title = "客户“" + customerInfo.getName() + "”收款单审核拒绝";
			content = title;
		}
		Boolean smsFlag = TraderReceiptOrder.dao.findAuditHandlerSmsNoticeConfig();// 发送短信
		Boolean sysFlag = TraderReceiptOrder.dao.findAuditHandlerSysNoticeConfig();// 发送系统消息
		sendNoticeMsg(MsgDataTypeEnum.recepit_order_audit, title, content, order.getHandlerId(), smsFlag, sysFlag);

		smsFlag = TraderReceiptOrder.dao.findAuditMakeManSmsNoticeConfig();// 发送短信
		sysFlag = TraderReceiptOrder.dao.findAuditMakeManSysNoticeConfig();// 发送系统消息
		sendNoticeMsg(MsgDataTypeEnum.recepit_order_audit, title, content, order.getMakeManId(), smsFlag, sysFlag);
	}
	
	/**
	 * 发送消息给审核人
	 * @param 
	 * @param order
	 */
	private void sendOrderNoticeMsg(TraderReceiptOrder order) {
		if(order.getAuditStatus() != AuditStatusEnum.waiting.getValue() || order.getOrderStatus() != OrderStatusEnum.normal.getValue()) {
			return;
		}
		Integer	auditorId = TraderReceiptOrder.dao.findAuditorIdConfig();
		order.setAuditorId(auditorId); // 设置审核人
		Boolean smsFlag = TraderReceiptOrder.dao.findAuditSmsNoticeConfig();// 发送短信
		Boolean sysFlag = TraderReceiptOrder.dao.findAuditSysNoticeConfig();// 发送系统消息
		CustomerInfo customerInfo = order.getCustomerInfo();
		String title="客户“"+customerInfo.getName()+"”收款单审核通知";
		String content = title;
		sendNoticeMsg(MsgDataTypeEnum.recepit_order_audit, title, content, order.getAuditorId(), smsFlag, sysFlag);
	}
	
	
}