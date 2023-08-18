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
import com.bytechainx.psi.common.model.PurchaseOrder;
import com.bytechainx.psi.common.model.PurchaseOrderFund;
import com.bytechainx.psi.common.model.SupplierInfo;
import com.bytechainx.psi.common.model.TenantConfig;
import com.bytechainx.psi.common.model.TenantExportLog;
import com.bytechainx.psi.common.model.TraderBalanceAccount;
import com.bytechainx.psi.common.model.TraderBookAccount;
import com.bytechainx.psi.common.model.TraderBookAccountLogs;
import com.bytechainx.psi.common.model.TraderFundOrder;
import com.bytechainx.psi.common.model.TraderPayOrder;
import com.bytechainx.psi.common.model.TraderPayOrderFund;
import com.bytechainx.psi.common.model.TraderPayOrderRef;
import com.bytechainx.psi.common.model.TraderSupplierPayable;
import com.bytechainx.psi.common.service.base.CommonService;
import com.google.common.collect.Lists;
import com.jfinal.kit.Kv;
import com.jfinal.kit.Ret;
import com.jfinal.kit.ThreadPoolKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;


/**
* 付款单
*/
public class BookPayOrderService extends CommonService {

	/**
	 * 分页列表
	 * @param tenantOrgId
	 * @param conditionColumns 表字段条件，只限于主表
	 * @param pageNumber
	 * @param pageSize
	 * @return
	 */
	public Page<TraderPayOrder> paginate(String startDay, String endDay, Kv conditionColumns, Integer pageNumber, int pageSize) {
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
		
		return TraderPayOrder.dao.paginate(pageNumber, pageSize, "select * ", "from trader_pay_order "+where.toString()+" order by id desc", params.toArray());
	}


	/**
	* 新增
	*/
	public Ret create(TraderPayOrder order, List<TraderPayOrderFund> orderFundList, List<TraderPayOrderRef> orderRefList) {
		if( order == null) {
			return Ret.fail("参数错误");
		}
		if(order.getSupplierInfoId() == null || order.getSupplierInfoId() <= 0) {
			return Ret.fail("供应商不能为空");
		}
		if(order.getAmount() == null || order.getAmount().doubleValue() <= 0) {
			return Ret.fail("付款金额不能为空");
		}
		if(order.getOrderTime() == null) {
			return Ret.fail("单据时间不能为空");
		}
		if(orderFundList == null || orderFundList.isEmpty()) {
			return Ret.fail("付款金额不能为空"); 
		}
		if(order.getCheckAmount() != null && order.getCheckAmount().compareTo(order.getAmount()) > 0) {
			return Ret.fail("实际核销金额 不能大于 付款合计金额"); 
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
		// 核销的进货单
		createCheckingOrder(order, orderRefList);
		// 记录对账单
		updateSupplierPayable(order);
		
		return Ret.ok("新增付款单成功").set("targetId", order.getId());
	}
	
	/**
	* 修改
	*/
	public Ret update(TraderPayOrder order, List<TraderPayOrderFund> orderFundList, List<TraderPayOrderRef> orderRefList) {
		if(order == null || order.getId() == null || order.getId() <= 0) {
			return Ret.fail("参数错误");
		}
		if(order.getSupplierInfoId() == null || order.getSupplierInfoId() <= 0) {
			return Ret.fail("供应商不能为空");
		}
		if(order.getAmount() == null || order.getAmount().doubleValue() <= 0) {
			return Ret.fail("付款金额不能为空");
		}
		if(order.getOrderTime() == null) {
			return Ret.fail("单据时间不能为空");
		}
		if(orderFundList == null || orderFundList.isEmpty()) {
			return Ret.fail("付款金额不能为空"); 
		}
		if(order.getCheckAmount() != null && order.getCheckAmount().compareTo(order.getAmount()) > 0) {
			return Ret.fail("实际核销金额 不能大于 付款合计金额"); 
		}
		
		TraderPayOrder _order = TraderPayOrder.dao.findById(order.getId());
		if(_order == null) {
			return Ret.fail("付款单不存在，无法修改");
		}
		if(_order.getOrderStatus() == OrderStatusEnum.normal.getValue() && order.getOrderStatus() != OrderStatusEnum.normal.getValue()) {
			return Ret.fail("单据状态异常，无法修改"); 
		}
		// 审核状态要根据是否开启审核开关来设置，默认无需审核
		if(_order.getOrderStatus() == OrderStatusEnum.draft.getValue()) { // 如果之前是草稿单，则处理审核状态
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
		// 更新订单付款资金
		updateOrderFund(order, orderFundList, changeCheckAmount);
		// 记录对账单
		updateSupplierPayable(order);
		// 处理核销的进货单
		updateCheckingOrder(order, orderRefList, false);
		
		return Ret.ok("修改付款单成功").set("targetId", order.getId());
	}


	/**
	* 作废
	*/
	public Ret disable(List<Integer> ids) {
		if(ids == null || ids.isEmpty()) {
			return Ret.fail("参数错误");
		}
		for (Integer id : ids) {
			TraderPayOrder order = TraderPayOrder.dao.findById(id);
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
			for (TraderPayOrderFund e : order.getOrderFundList()) {
				undoOrderFund(order, e);
			}
			// 删除交易流水
			Db.delete("delete from trader_fund_order where tenant_store_id = ? and ref_order_type = ? and ref_order_id = ?", RefOrderTypeEnum.trader_pay_order.getValue(), order.getId());
			
			// 处理核销的进货单
			for (TraderPayOrderRef ref : order.getOrderRefList()) {
				undoPurchaseOrderChecking(order, ref);
			}
			// 记录对账单
			updateSupplierPayable(order);
			
		}
		return Ret.ok("作废付款单成功");
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
			TraderPayOrder order = TraderPayOrder.dao.findById(id);
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
				// 处理核销的进货单
				updateCheckingOrder(order, order.getOrderRefList(), true);
			}
			// 记录对账单
			updateSupplierPayable(order);
			// 发送审核结果消息
			sendAuditNoticeMsg(order);
			
		}
		return Ret.ok("审核付款单成功");
	}
	
	/**
	 * 删除单据
	 * @param tenantOrgId
	 * @param ids
	 * @return
	 */
	public Ret delete(Integer id) {
		if(id == null) {
			return Ret.fail("参数错误");
		}
		TraderPayOrder order = TraderPayOrder.dao.findById(id);
		if(order == null) {
			return Ret.fail("付款单不存在");
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
		String fileName = "付款单-"+DateUtil.getSecondNumber(new Date());
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
					List<TraderPayOrder> orderList = Lists.newArrayList();
					int pageNumber = 1;
					Page<TraderPayOrder> page = paginate(startDay, endDay, condKv, pageNumber, 500);
					while (!page.getList().isEmpty() && orderList.size() < 10000) { // 最大不能超过10000
						orderList.addAll(page.getList());
						pageNumber += 1;
						page = paginate(startDay, endDay, condKv, pageNumber, 200);
					}
					StringBuffer headersBuffer = new StringBuffer();
					headersBuffer.append("单据日期,单据编号");
					StringBuffer columnsBuffer = new StringBuffer();
					columnsBuffer.append("order_time_day,order_code");
					
					headersBuffer.append(",供应商名称,付款账户,付款金额,付款日期,付款合计,核销金额,优惠金额,核销单据,单据备注,经办人");
					columnsBuffer.append(",supplier_info_name,list,amount,order_amount,discount_amount,order_list,remark,handler_name");
					
					String[] headers = headersBuffer.toString().split(",");
					String[] columns = columnsBuffer.toString().split(",");
					
					String[] listColumns = new String[] {"trader_balance_account_name","amount","fund_time"};
					for (TraderPayOrder order : orderList) {
						order.put("order_time_day", DateUtil.getDayStr(order.getOrderTime()));
						order.put("supplier_info_name", order.getSupplierInfo().getName());
						
						List<TraderPayOrderFund> fundList = order.getOrderFundList();
						order.put("list", fundList);
						for (TraderPayOrderFund fund : fundList) {
							fund.put("trader_balance_account_name", fund.getBalanceAccount().getName());
							fund.put("fund_time", DateUtil.getDayStr(fund.getFundTime()));
						}
						StringBuffer orderListBuffer = new StringBuffer();
						List<TraderPayOrderRef> payOrderRefList = order.getOrderRefList();
						for (TraderPayOrderRef ref : payOrderRefList) {
							orderListBuffer.append(ref.getPurchaseOrder().getOrderCode()+":"+ref.getAmount().stripTrailingZeros().toPlainString()+"\r\n");
						}
						if(orderListBuffer.length() > 0) {
							order.put("order_list", orderListBuffer.substring(0, orderListBuffer.length() - 1).toString());
						}
						
						order.put("handler_name", order.getHandler().getRealName());
					}
					
					String filePath = XlsKit.genOrderXls("付款单", fileName, headers, orderList, columns, listColumns);
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
	 * 核销进货单处理
	 * @param tenantOrgId
	 * @param order
	 * @param orderRefList
	 */
	private void createCheckingOrder(TraderPayOrder order, List<TraderPayOrderRef> orderRefList) {
		if(orderRefList == null || orderRefList.isEmpty()) {
			return;
		}
		for (TraderPayOrderRef ref : orderRefList) {
			if(ref.getAmount() == null || ref.getAmount().doubleValue() <= 0) {
				continue;
			}
			if(ref.getPurchaseOrderId() == null || ref.getPurchaseOrderId() <= 0) {
				continue;
			}
			PurchaseOrder purchaseOrder = PurchaseOrder.dao.findById(ref.getPurchaseOrderId());
			if(purchaseOrder == null) { // 进货单不存在
				continue;
			}
			if(ref.getDiscountAmount() == null) {
				ref.setDiscountAmount(BigDecimal.ZERO);
			}
			
			ref.setTraderPayOrderId(order.getId());
			ref.save();
			
			// 处理进货单核销
			updatePurchaseOrderChecking(order, ref, purchaseOrder, ref.getAmount(), ref.getDiscountAmount());
		}
	}


	/**
	 * 处理付款资金，记录交易
	 * @param tenantOrgId
	 * @param order
	 * @param orderFundList
	 */
	private void createOrderFund(TraderPayOrder order, List<TraderPayOrderFund> orderFundList) {
		// 更新来往账户支付的金额
		updateTraderBookAccountPayAmount(order, order.getCheckAmount());
				
		if(orderFundList == null || orderFundList.isEmpty()) {
			return;
		}
		// 账户付款
		for (TraderPayOrderFund e : orderFundList) {
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
	 * 处理核销的进货单
	 * @param tenantOrgId
	 * @param order
	 * @param orderRefList
	 */
	private void updateCheckingOrder(TraderPayOrder order, List<TraderPayOrderRef> orderRefList, boolean auditFlag) {
		if (orderRefList == null || orderRefList.isEmpty()) {
			orderRefList = new ArrayList<>();
		}
		List<TraderPayOrderRef> oldRefList = TraderPayOrderRef.dao.find("select * from trader_pay_order_ref where trader_pay_order_id = ?", order.getId());
		// 先过滤出已删除的记录
		List<TraderPayOrderRef> deleteRefList = new ArrayList<>();
		for(TraderPayOrderRef oldRef : oldRefList) {
			boolean isExist = false;
			for (TraderPayOrderRef ref : orderRefList) {
				if(ref.getId() != null && ref.getId().intValue() == oldRef.getId().intValue()) {
					isExist = true;
					break;
				}
				if(ref.getPurchaseOrderId().intValue() == oldRef.getPurchaseOrderId().intValue()) { // 同一个进货单
					isExist = true;
					break;
				}
			}
			if(!isExist) { // 需要删除的记录
				deleteRefList.add(oldRef);
			}
		}
		// 处理需要删除的核销单
		for (TraderPayOrderRef ref : deleteRefList) {
			undoPurchaseOrderChecking(order, ref);
			ref.delete();
		}
		
		// 处理核销的进货单
		for (TraderPayOrderRef ref : orderRefList) {
			if (ref.getAmount() == null || ref.getAmount().doubleValue() <= 0) {
				continue;
			}
			if (ref.getPurchaseOrderId() == null || ref.getPurchaseOrderId() <= 0) {
				continue;
			}
			PurchaseOrder purchaseOrder = PurchaseOrder.dao.findById(ref.getPurchaseOrderId());
			if(purchaseOrder == null) { // 进货单不存在
				continue;
			}
			BigDecimal changeAmount = ref.getAmount();
			BigDecimal changeDiscountAmount = ref.getDiscountAmount();
			TraderPayOrderRef _ref = TraderPayOrderRef.dao.findFirst("select * from trader_pay_order_ref where trader_pay_order_id = ? and purchase_order_id = ? ", order.getId(), ref.getPurchaseOrderId());
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
				ref.setTraderPayOrderId(order.getId());
				ref.save();
			}
			// 处理更新进货单核销
			updatePurchaseOrderChecking(order, ref, purchaseOrder, changeAmount, changeDiscountAmount);
		}
	}


	/**
	 * 更新处理进货单核销
	 * @param tenantOrgId
	 * @param order
	 * @param ref
	 * @param purchaseOrder
	 * @param changeAmount
	 * @param changeDiscountAmount
	 */
	private void updatePurchaseOrderChecking(TraderPayOrder order, TraderPayOrderRef ref, PurchaseOrder purchaseOrder, BigDecimal changeAmount, BigDecimal changeDiscountAmount) {
		if(order.getAuditStatus() != AuditStatusEnum.pass.getValue()) {
			return;
		}
		// 处理进货单已付金额，更新进货单状态
		purchaseOrder.setOddAmount(purchaseOrder.getOddAmount().add(changeDiscountAmount));
		purchaseOrder.setAmount(purchaseOrder.getAmount().subtract(changeDiscountAmount)); // 减去核销优惠金额
		
		purchaseOrder.setPaidAmount(purchaseOrder.getPaidAmount().add(changeAmount));
		if(purchaseOrder.getPaidAmount().compareTo(purchaseOrder.getAmount()) >= 0) { // 已付清
			purchaseOrder.setPayStatus(OrderPayStatusEnum.finish.getValue());
		} else { // 部分付
			purchaseOrder.setPayStatus(OrderPayStatusEnum.part.getValue());
		}
		purchaseOrder.setUpdatedAt(new Date());
		purchaseOrder.update();
		
		// 生成进货单付款明细
		PurchaseOrderFund purchaseOrderFund = PurchaseOrderFund.dao.findFirst("select * from purchase_order_fund where purchase_order_id = ? and balance_account_id = ? and pay_type = ? limit 1", purchaseOrder.getId(), order.getId(), FundTypeEnum.checking.getValue());
		if(purchaseOrderFund != null) {
			purchaseOrderFund.setAmount(ref.getAmount());
			purchaseOrderFund.setPayTime(order.getOrderTime());
			purchaseOrderFund.setUpdatedAt(new Date());
			purchaseOrderFund.update();
			
		} else {
			purchaseOrderFund = new PurchaseOrderFund();
			purchaseOrderFund.setAmount(ref.getAmount());
			purchaseOrderFund.setCreatedAt(new Date());
			purchaseOrderFund.setPayTime(new Date());
			purchaseOrderFund.setPayType(FundTypeEnum.checking.getValue());
			purchaseOrderFund.setPurchaseOrderId(purchaseOrder.getId());
			purchaseOrderFund.setBalanceAccountId(order.getId());
			purchaseOrderFund.setUpdatedAt(new Date());
			purchaseOrderFund.save();
		}
	}
	
	/**
	 * 回退进货单核销
	 * @param tenantOrgId
	 * @param order
	 * @param ref
	 */
	private void undoPurchaseOrderChecking(TraderPayOrder order, TraderPayOrderRef ref) {
		if(order.getAuditStatus() != AuditStatusEnum.pass.getValue()) {
			return;
		}
		PurchaseOrder purchaseOrder = PurchaseOrder.dao.findById(ref.getPurchaseOrderId());
		if(purchaseOrder == null) { // 进货单不存在
			return;
		}
		// 处理进货单已付金额，更新进货单状态
		purchaseOrder.setOddAmount(purchaseOrder.getOddAmount().subtract(ref.getDiscountAmount()));
		purchaseOrder.setAmount(purchaseOrder.getAmount().add(ref.getDiscountAmount())); // 回退核销优惠金额
		
		purchaseOrder.setPaidAmount(purchaseOrder.getPaidAmount().subtract(ref.getAmount()));
		if(purchaseOrder.getPaidAmount().compareTo(BigDecimal.ZERO) <= 0) { // 未付
			purchaseOrder.setPayStatus(OrderPayStatusEnum.no.getValue());
			
		} else if(purchaseOrder.getPaidAmount().compareTo(purchaseOrder.getAmount()) < 0) { // 部分付
			purchaseOrder.setPayStatus(OrderPayStatusEnum.part.getValue());
		}
		purchaseOrder.setUpdatedAt(new Date());
		purchaseOrder.update();
		
		// 删除核销的资金明细
		Db.delete("delete from purchase_order_fund where purchase_order_id = ? and balance_account_id = ? and pay_type = ? limit 1", ref.getPurchaseOrderId(), order.getId(), FundTypeEnum.checking.getValue());
	}

	/**
	 * 更新付款资金
	 * @param tenantOrgId
	 * @param order
	 * @param orderFundList
	 * @param changeAmount
	 */
	private void updateOrderFund(TraderPayOrder order, List<TraderPayOrderFund> orderFundList, BigDecimal changeCheckAmount) {
		// 更新来往账户支付的金额
		updateTraderBookAccountPayAmount(order, changeCheckAmount);
				
		if(orderFundList == null) {
			orderFundList = new ArrayList<>();
		}
		List<TraderPayOrderFund> oldOrderFundList = TraderPayOrderFund.dao.find("select * from trader_pay_order_fund where trader_pay_order_id = ?", order.getId());
		// 先过滤出已删除的记录
		List<TraderPayOrderFund> deleteFundList = new ArrayList<>();
		for(TraderPayOrderFund oldFund : oldOrderFundList) {
			boolean isExist = false;
			for (TraderPayOrderFund fund : orderFundList) {
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
		
		for (TraderPayOrderFund e : deleteFundList) {
			// 结算帐户余额计算
			undoOrderFund(order, e);
			// 删除资金记录
			e.delete();
		}
		// 账户付款，更新结算帐户资金
		for (TraderPayOrderFund e : orderFundList) {
			paymentOrderFund(order, e);
		}
		
	}

	/**
	 * 回退资金到帐户
	 * @param tenantOrgId
	 * @param order
	 * @param e
	 */
	private void undoOrderFund(TraderPayOrder order, TraderPayOrderFund orderFund) {
		if(order.getAuditStatus() != AuditStatusEnum.pass.getValue()) { // 非审核不处理资金
			return;
		}
		TraderBalanceAccount balanceAccount = TraderBalanceAccount.dao.findById(orderFund.getTraderBalanceAccountId());
		balanceAccount.setBalance(balanceAccount.getBalance().add(orderFund.getAmount())); // 回退支付的金额
		balanceAccount.setUpdatedAt(new Date());
		balanceAccount.update();
		
		// 更新结算账户总余额
		
		BigDecimal changeAmount = BigDecimal.ZERO.subtract(orderFund.getAmount()); // 回退支付的金额，支付的时候是增加，退款的时候是减少
		updateTraderBookAccount(order, changeAmount);
	}
	
	/**
	 * 帐户资金支付处理
	 * @param tenantOrgId
	 * @param order
	 * @param orderFund
	 */
	private void paymentOrderFund(TraderPayOrder order, TraderPayOrderFund orderFund) {
		if(orderFund.getTraderBalanceAccountId() == null || orderFund.getTraderBalanceAccountId() <= 0 || orderFund.getAmount() == null || orderFund.getAmount().doubleValue() <= 0) {
			return;
		}
		BigDecimal changeFundAmount = orderFund.getAmount(); // 变更的金额
		TraderPayOrderFund _orderFund = TraderPayOrderFund.dao.findFirst("select * from trader_pay_order_fund where trader_pay_order_id = ? and id = ?", order.getId(), orderFund.getId());
		if(_orderFund != null) {
			changeFundAmount = orderFund.getAmount().subtract(_orderFund.getAmount()); // 计算差额
			_orderFund.setAmount(orderFund.getAmount());
			_orderFund.setFundTime(orderFund.getFundTime());
			_orderFund.setUpdatedAt(new Date());
			_orderFund.update();
			
		} else {
			orderFund.setTraderPayOrderId(order.getId());
			orderFund.setUpdatedAt(new Date());
			orderFund.setCreatedAt(new Date());
			orderFund.save();
		}
		if(order.getAuditStatus() != AuditStatusEnum.pass.getValue()) { // 不是已审核状态，不处理资金
			return;
		}
		// 结算帐户余额计算
		TraderBalanceAccount balanceAccount = TraderBalanceAccount.dao.findById(orderFund.getTraderBalanceAccountId());
		balanceAccount.setBalance(balanceAccount.getBalance().subtract(changeFundAmount)); // 减去支付的金额
		balanceAccount.setUpdatedAt(new Date());
		balanceAccount.update();
		
		// 更新往来帐户资金
		updateTraderBookAccount(order, changeFundAmount);
		
		// 生成交易流水
		TraderFundOrder fundOrder = TraderFundOrder.dao.findByOrderId(orderFund.getTraderBalanceAccountId(), RefOrderTypeEnum.trader_pay_order, order.getId());
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
			fundOrder.setRefOrderType(RefOrderTypeEnum.trader_pay_order.getValue());
			fundOrder.setTraderBalanceAccountId(orderFund.getTraderBalanceAccountId());
			fundOrder.setCreatedAt(new Date());
			fundOrder.setUpdatedAt(new Date());
			fundOrder.save();
		}
	}

	/**
	 * 更新往来帐户资金
	 * @param tenantOrgId
	 * @param order
	 * @param changeAmount
	 */
	private void updateTraderBookAccount(TraderPayOrder order, BigDecimal changeAmount) {
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
	 * 更新往来帐户资金，支付核销出去的金额
	 * @param tenantOrgId
	 * @param order
	 * @param changeAmount
	 */
	private void updateTraderBookAccountPayAmount(TraderPayOrder order, BigDecimal changeAmount) {
		if(order.getAuditStatus() != AuditStatusEnum.pass.getValue()) { // 非审核不处理资金
			return;
		}
		if(changeAmount == null || changeAmount.compareTo(BigDecimal.ZERO) == 0) {
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
	 * 供应商应付对账
	 * @param tenantOrgId
	 * @param order
	 */
	private void updateSupplierPayable(TraderPayOrder order) {
		if(order.getAuditStatus() != AuditStatusEnum.pass.getValue()) { // 非审核不处理资金
			return;
		}
		TraderSupplierPayable supplierPayable = TraderSupplierPayable.dao.findByOrderId(CheckingRefOrderTypeEnum.trader_pay_order, order.getId());
		if(supplierPayable != null) {
			if(order.getOrderStatus() == OrderStatusEnum.disable.getValue()) {
				supplierPayable.delete();
				return;
			}
			supplierPayable.setNewAmount(order.getAmount());
			supplierPayable.setTakeAmount(order.getAmount());
			supplierPayable.setDiscountAmount(order.getDiscountAmount());
			supplierPayable.setAdjustAmount(BigDecimal.ZERO);
			supplierPayable.setOrderTime(order.getOrderTime());
			supplierPayable.setUpdatedAt(new Date());
			supplierPayable.update();
			
		} else {
			SupplierInfo supplierInfo = SupplierInfo.dao.findById(order.getSupplierInfoId());
			supplierPayable = new TraderSupplierPayable();
			supplierPayable.setRefOrderCode(order.getOrderCode());
			supplierPayable.setRefOrderId(order.getId());
			supplierPayable.setRefOrderType(CheckingRefOrderTypeEnum.trader_pay_order.getValue());
			supplierPayable.setSupplierInfoId(order.getSupplierInfoId());
			supplierPayable.setTraderBookAccountId(supplierInfo.getTraderBookAccountId());
			supplierPayable.setDiscountAmount(order.getDiscountAmount());
			supplierPayable.setNewAmount(order.getAmount());
			supplierPayable.setTakeAmount(order.getAmount());
			supplierPayable.setAdjustAmount(BigDecimal.ZERO);
			supplierPayable.setOrderTime(order.getOrderTime());
			supplierPayable.setCreatedAt(new Date());
			supplierPayable.setUpdatedAt(new Date());
			supplierPayable.save();
		}
	}
	
	/**
	 * 设置单据审核状态
	 * @param tenantOrgId
	 * @param order
	 */
	private AuditStatusEnum getAuditStatus(TraderPayOrder order) {
		TenantConfig auditConfig = TenantConfig.dao.findByKeyCache(TenantConfigEnum.audit_trader_pay_order);
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
	 * @param tenantOrgId
	 * @param order
	 */
	private void sendAuditNoticeMsg(TraderPayOrder order) {
		SupplierInfo supplierInfo = order.getSupplierInfo();
		String title = "供应商“" + supplierInfo.getName() + "”付款单审核通过";
		String content = title;
		if (order.getAuditStatus() == AuditStatusEnum.reject.getValue()) {
			title = "供应商“" + supplierInfo.getName() + "”付款单审核拒绝";
			content = title;
		}
		Boolean smsFlag = TraderPayOrder.dao.findAuditHandlerSmsNoticeConfig();// 发送短信
		Boolean sysFlag = TraderPayOrder.dao.findAuditHandlerSysNoticeConfig();// 发送系统消息
		sendNoticeMsg(MsgDataTypeEnum.pay_order_audit, title, content, order.getHandlerId(), smsFlag, sysFlag);

		smsFlag = TraderPayOrder.dao.findAuditMakeManSmsNoticeConfig();// 发送短信
		sysFlag = TraderPayOrder.dao.findAuditMakeManSysNoticeConfig();// 发送系统消息
		sendNoticeMsg(MsgDataTypeEnum.pay_order_audit, title, content, order.getMakeManId(), smsFlag, sysFlag);
	}
	
	/** 
	 * 发送消息给审核人
	 * @param tenantOrgId
	 * @param order
	 */
	private void sendOrderNoticeMsg(TraderPayOrder order) {
		if(order.getAuditStatus() != AuditStatusEnum.waiting.getValue() || order.getOrderStatus() != OrderStatusEnum.normal.getValue()) {
			return;
		}
		Integer	auditorId = TraderPayOrder.dao.findAuditorIdConfig();
		order.setAuditorId(auditorId); // 设置审核人
		Boolean smsFlag = TraderPayOrder.dao.findAuditSmsNoticeConfig();// 发送短信
		Boolean sysFlag = TraderPayOrder.dao.findAuditSysNoticeConfig();// 发送系统消息
		SupplierInfo supplierInfo = order.getSupplierInfo();
		String title="供应商“"+supplierInfo.getName()+"”付款单审核通知";
		String content = title;
		sendNoticeMsg(MsgDataTypeEnum.pay_order_audit, title, content, order.getAuditorId(), smsFlag, sysFlag);
	}

}