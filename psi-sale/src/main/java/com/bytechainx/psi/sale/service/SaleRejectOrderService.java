package com.bytechainx.psi.sale.service;


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
import com.bytechainx.psi.common.EnumConstant.LogisticsStatusInEnum;
import com.bytechainx.psi.common.EnumConstant.MsgDataTypeEnum;
import com.bytechainx.psi.common.EnumConstant.OrderPayStatusEnum;
import com.bytechainx.psi.common.EnumConstant.OrderStatusEnum;
import com.bytechainx.psi.common.EnumConstant.RefOrderTypeEnum;
import com.bytechainx.psi.common.EnumConstant.RejectDealTypeEnum;
import com.bytechainx.psi.common.EnumConstant.RejectTypeEnum;
import com.bytechainx.psi.common.EnumConstant.StockInOutOrderTypeEnum;
import com.bytechainx.psi.common.EnumConstant.StockIoTypeEnum;
import com.bytechainx.psi.common.EnumConstant.TenantConfigEnum;
import com.bytechainx.psi.common.kit.DateUtil;
import com.bytechainx.psi.common.kit.excel.XlsKit;
import com.bytechainx.psi.common.model.CustomerInfo;
import com.bytechainx.psi.common.model.GoodsInfo;
import com.bytechainx.psi.common.model.GoodsPrice;
import com.bytechainx.psi.common.model.GoodsUnit;
import com.bytechainx.psi.common.model.InventoryStock;
import com.bytechainx.psi.common.model.InventoryStockLog;
import com.bytechainx.psi.common.model.SaleOrder;
import com.bytechainx.psi.common.model.SaleOrderGoods;
import com.bytechainx.psi.common.model.SaleRejectOrder;
import com.bytechainx.psi.common.model.SaleRejectOrderCost;
import com.bytechainx.psi.common.model.SaleRejectOrderFee;
import com.bytechainx.psi.common.model.SaleRejectOrderFund;
import com.bytechainx.psi.common.model.SaleRejectOrderGoods;
import com.bytechainx.psi.common.model.SaleRejectOrderLog;
import com.bytechainx.psi.common.model.TenantAdmin;
import com.bytechainx.psi.common.model.TenantConfig;
import com.bytechainx.psi.common.model.TenantExportLog;
import com.bytechainx.psi.common.model.TraderBalanceAccount;
import com.bytechainx.psi.common.model.TraderBookAccount;
import com.bytechainx.psi.common.model.TraderBookAccountLogs;
import com.bytechainx.psi.common.model.TraderCustomerReceivable;
import com.bytechainx.psi.common.model.TraderFundOrder;
import com.bytechainx.psi.common.model.TraderFundType;
import com.bytechainx.psi.common.service.base.CommonService;
import com.google.common.collect.Lists;
import com.jfinal.aop.Inject;
import com.jfinal.kit.Kv;
import com.jfinal.kit.Ret;
import com.jfinal.kit.ThreadPoolKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;


/**
* 销售退货单
*/
public class SaleRejectOrderService extends CommonService {
	
	@Inject
	private SaleOrderService saleOrderService;

	/**
	* 分页列表
	*/
	public Page<SaleRejectOrder> paginate(String startDay, String endDay, Kv conditionColumns, Integer pageNumber, int pageSize) {
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

		return SaleRejectOrder.dao.paginate(pageNumber, pageSize, "select * ", "from sale_reject_order "+where.toString()+" order by id desc", params.toArray());
	}

	/**
	* 新增
	*/
	public Ret create(SaleRejectOrder order, List<SaleRejectOrderGoods> orderGoodList, List<SaleRejectOrderFund> orderFundList, List<SaleRejectOrderFee> orderFeeList, List<SaleRejectOrderCost> orderCostList) {
		if( order == null) {
			return Ret.fail("参数错误");
		}
		if(order.getCustomerInfoId() == null || order.getCustomerInfoId() <= 0) {
			return Ret.fail("客户不能为空");
		}
		if(order.getHandlerId() == null || order.getHandlerId() <= 0) {
			return Ret.fail("销售退货员不能为空");
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
			for (SaleRejectOrderFund e : orderFundList) {
				paidAmount = paidAmount.add(e.getAmount());
			}
		}
		if(paidAmount.compareTo(order.getAmount()) > 0) {
			return Ret.fail("实付不能大于应付金额"); 
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
		// 发送审核消息
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
		for (SaleRejectOrderGoods e : orderGoodList) {
			if(e.getGoodsInfoId() == null || e.getGoodsInfoId() <= 0 || e.getBuyNumber().doubleValue() <= 0) {
				continue;
			}
			if(e.getCostPrice() == null || e.getCostPrice().compareTo(BigDecimal.ZERO) <= 0) {
				GoodsPrice goodsPrice = GoodsPrice.dao.findBySpec(e.getGoodsInfoId(), e.getSpec1Id(), e.getSpecOption1Id(), e.getSpec2Id(), e.getSpecOption2Id(), e.getSpec3Id(), e.getSpecOption3Id(), e.getUnitId());
				if(goodsPrice != null) {
					e.setCostPrice(goodsPrice.getAvgCostPrice());
					e.setCostAmount(e.getCostPrice().multiply(e.getBuyNumber()));
				}
			}
			e.setCustomerInfoId(order.getCustomerInfoId());
			e.setCreatedAt(new Date());
			e.setUpdatedAt(new Date());
			e.setSaleRejectOrderId(order.getId());
			e.save();
			
			// 销售单退货处理
			orderReject(order, e, null, orderIdList);
		}
		
		if(!orderIdList.isEmpty()) {
			order.setSaleOrderId(","+StringUtils.join(orderIdList, ",")+",");
			order.update();
		}
		
		// 更新库存，客户退货到仓库
		updateInventoryStock(order, orderGoodList);
		
		// 订单支付资金
		if(orderFundList != null && !orderFundList.isEmpty()) {
			for (SaleRejectOrderFund e : orderFundList) {
				paymentOrderFund(order, e, false);
			}
		}
		// 货款计入收入
		goodsOutTraderBookAccount(order, order.getAmount());
		// 记录对账单
		updateCustomerReceivable(order);
		
		// 记录其他费用，需要客户支付
		updateOrderFee(order, orderFeeList);
		// 记录支出成本，自己本身的支出
		updateOrderCost(order, orderCostList);
		// 更新货品的告警状态，FIXME 要不要判断状态，减少无效操作
		updateStockWarn(order.getOrderGoodsList());
		
		// 记录订单操作
		writeLog(order.getId(), order.getLastManId(), "创建销售退货单");
		
		return Ret.ok("新增销售退货单成功").set("targetId", order.getId());
	}


	/**
	* 修改
	*/
	public Ret update(SaleRejectOrder order, List<SaleRejectOrderGoods> orderGoodList, List<SaleRejectOrderFund> orderFundList, List<SaleRejectOrderFee> orderFeeList, List<SaleRejectOrderCost> orderCostList) {
		if(order == null || order.getId() == null || order.getId() <= 0) {
			return Ret.fail("参数错误");
		}
		if(order.getCustomerInfoId() == null || order.getCustomerInfoId() <= 0) {
			return Ret.fail("客户不能为空");
		}
		if(order.getHandlerId() == null || order.getHandlerId() <= 0) {
			return Ret.fail("销售退货员不能为空");
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
			for (SaleRejectOrderFund e : orderFundList) {
				paidAmount = paidAmount.add(e.getAmount());
			}
		}
		if(paidAmount.compareTo(order.getAmount()) > 0) {
			return Ret.fail("实付不能大于应付金额"); 
		}
		
		SaleRejectOrder _order = SaleRejectOrder.dao.findById(order.getId());
		if(_order == null) {
			return Ret.fail("销售退货单不存在，无法修改");
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
			
			// 发送审核消息
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
		// 库存更新
		updateInventoryStock(order, orderGoodList);
		// 更新订单付款资金
		boolean fundModifyFlag = updateOrderFund(order, orderFundList, false);
		
		// 货款计入收入
		goodsOutTraderBookAccount(order, changeAmount);
		// 记录对账单
		updateCustomerReceivable(order);
		// 记录其他费用，需要客户支付
		updateOrderFee(order, orderFeeList);
		// 记录支出成本，自己本身的支出
		updateOrderCost(order, orderCostList);
		// 更新货品的告警状态，FIXME 要不要判断状态，减少无效操作
		updateStockWarn(order.getOrderGoodsList());

		// 记录订单操作
		writeLog(order.getId(), order.getLastManId(), "修改单据:"+order.getChangeLog(_order, goodsModifyFlag, fundModifyFlag));
			
		return Ret.ok("修改销售退货单成功").set("targetId", order.getId());
	}

	/**
	* 作废
	*/
	public Ret disable(List<Integer> ids) {
		if(ids == null || ids.isEmpty()) {
			return Ret.fail("参数错误");
		}
		for (Integer id : ids) {
			SaleRejectOrder order = SaleRejectOrder.dao.findById(id);
			if(order == null) {
				continue;
			}
			if(order.getOrderStatus() == OrderStatusEnum.disable.getValue()) { // 已作废的单据不能重复作废
				continue;
			}
			
			// 商品库存还原
			undoInventoryStock(order, order.getOrderGoodsList());

			if(order.getAuditStatus() != AuditStatusEnum.pass.getValue()) { // 非已审核的不执行以下资金回退操作
				order.setLogisticsStatus(LogisticsStatusInEnum.waiting.getValue());
				order.setAuditStatus(AuditStatusEnum.waiting.getValue());
				order.setOrderStatus(OrderStatusEnum.disable.getValue());
				order.setUpdatedAt(new Date());
				order.update();
				
				continue;
			}
			
			// 销售单退货回退
			for (SaleRejectOrderGoods e : order.getOrderGoodsList()) {
				undoOrderReject(order, e);
			}

			// 订单资金回退
			for (SaleRejectOrderFund e : order.getOrderFundList()) {
				// 根据支付方式，回退资金
				undoOrderFund(order, e);
			}
			// 删除交易流水
			Db.delete("delete from trader_fund_order where ref_order_type = ? and ref_order_id = ?", RefOrderTypeEnum.sale_reject_order.getValue(), order.getId());
			// 货款收入回退
			goodsOutTraderBookAccount(order, BigDecimal.ZERO.subtract(order.getAmount()));
			// 记录对账单
			updateCustomerReceivable(order);
			
			order.setLogisticsStatus(LogisticsStatusInEnum.waiting.getValue());
			order.setAuditStatus(AuditStatusEnum.waiting.getValue());
			order.setOrderStatus(OrderStatusEnum.disable.getValue());
			order.setUpdatedAt(new Date());
			order.update();
			
			// 更新货品的告警状态，FIXME 要不要判断状态，减少无效操作
			updateStockWarn(order.getOrderGoodsList());
			
			// 记录订单操作
			writeLog(order.getId(), order.getLastManId(), "作废销售退货单");
		}
		return Ret.ok("作废销售退货单成功");
	}
	
	
	/**
	 * 销售退货单付款
	 * @param 
	 * @param orderId
	 * @param orderFundList
	 * @return
	 */
	public Ret payment(Integer orderId, List<SaleRejectOrderFund> orderFundList) {
		if(orderId == null || orderId <= 0 || orderFundList == null || orderFundList.isEmpty()) {
			return Ret.fail("参数错误");
		}
		SaleRejectOrder order = SaleRejectOrder.dao.findById(orderId);
		if(order == null) {
			return Ret.fail("销售退货单不存在，无法修改");
		}
		if(order.getAuditStatus() != AuditStatusEnum.pass.getValue()) {
			return Ret.fail("销售退货单未通过审核，无法付款");
		}
		if(order.getOrderStatus() != OrderStatusEnum.normal.getValue()) {
			return Ret.fail("非正常销售退货单，无法付款");
		}
		if(order.getPayStatus() == OrderPayStatusEnum.finish.getValue()) {
			return Ret.fail("销售退货单已付清，无需付款");
		}
		// 账户付款，更新结算帐户资金
		for (SaleRejectOrderFund e : orderFundList) {
			// 支付资金，生成记录
			paymentOrderFund(order, e, false);
		}
		
		return Ret.ok("销售退货单付款成功");
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
			SaleRejectOrder order = SaleRejectOrder.dao.findById(id);
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
				Set<Integer> orderIdList = new HashSet<>(); // 关联的销售单列表
				for (SaleRejectOrderGoods e : order.getOrderGoodsList()) {
					orderReject(order, e, null, orderIdList);
				}
				if(!orderIdList.isEmpty()) {
					order.setSaleOrderId(","+StringUtils.join(orderIdList, ",")+",");
					order.update();
				}
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
			writeLog(order.getId(), order.getLastManId(), "审核销售退货订单:"+auditStatus.getName());
			// 发送审核结果通知
			sendAuditNoticeMsg(order);
		}
		return Ret.ok("审核销售退货单成功");
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
		SaleRejectOrder order = SaleRejectOrder.dao.findById(id);
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
		SaleRejectOrderLog log = new SaleRejectOrderLog();
		log.setOperAdminId(operAdminId);
		if(operAdminId != null && operAdminId > 0) {
			TenantAdmin admin = TenantAdmin.dao.findById(operAdminId);
			log.setOperAdminName(admin.getRealName());
		} else {
			log.setOperAdminName("系统后台");
		}
		log.setOperDesc(operDesc);
		log.setOperTime(new Date());
		log.setSaleRejectOrderId(orderId);
		log.save();
		
		return Ret.ok();
	}
	
	/**
	 * 导出数据xls
	 */
	public Ret export(Integer handlerId, String startDay, String endDay, Kv condKv) {
		String fileName = "销售退货单-"+DateUtil.getSecondNumber(new Date());
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
					List<SaleRejectOrder> orderList = Lists.newArrayList();
					int pageNumber = 1;
					Page<SaleRejectOrder> page = paginate(startDay, endDay, condKv, pageNumber, 500);
					while (!page.getList().isEmpty() && orderList.size() < 10000) { // 最大不能超过10000
						orderList.addAll(page.getList());
						pageNumber += 1;
						page = paginate(startDay, endDay, condKv, pageNumber, 200);
					}
					StringBuffer headersBuffer = new StringBuffer();
					headersBuffer.append("单据日期,单据编号");
					StringBuffer columnsBuffer = new StringBuffer();
					columnsBuffer.append("order_time_day,order_code");
					
					headersBuffer.append(",客户名称,退货原因,商品名称,商品条码,规格,单位,处理方式,数量,单价,商品折扣(%),折后单价,金额,备注,整单折扣率(%)");
					columnsBuffer.append(",customer_info_name,reject_reason_type_name,list,discount");
					
					List<TraderFundType> feeList = SaleRejectOrder.dao.findFeeConfig(); // 其他费用配置
					if(feeList != null) {
						for (TraderFundType fee : feeList) {
							headersBuffer.append("," + fee.getName());
							columnsBuffer.append(",fee_amount_" + fee.getId());
						}
					}
					
					headersBuffer.append(",应退金额,已退金额");
					columnsBuffer.append(",amount,paid_amount");
					
					List<TraderFundType> costList = SaleRejectOrder.dao.findCostConfig(); // 成本支出配置
					if(costList != null) {
						for (TraderFundType cost : costList) {
							headersBuffer.append("," + cost.getName());
							columnsBuffer.append(",cost_amount_" + cost.getId());
						}
					}
					headersBuffer.append(",单据备注,退货员");
					columnsBuffer.append(",remark,handler_name");
					
					String[] headers = headersBuffer.toString().split(",");
					String[] columns = columnsBuffer.toString().split(",");
					
					String[] listColumns = new String[] {"goods_name","bar_code","spec_name","unit_name","reject_deal_type_name","buy_number","price","discount","discount_amount","amount","remark"};
					for (SaleRejectOrder order : orderList) {
						order.put("order_time_day", DateUtil.getDayStr(order.getOrderTime()));
						order.put("customer_info_name", order.getCustomerInfo().getName());
						order.put("reject_reason_type_name", order.getRejectReasonName());
						
						List<SaleRejectOrderGoods> goodsList = order.getOrderGoodsList();
						order.put("list", goodsList);
						for (SaleRejectOrderGoods orderGoods : goodsList) {
							GoodsInfo goodsInfo = orderGoods.getGoodsInfo();
							orderGoods.put("goods_name", goodsInfo.getName());
							orderGoods.put("bar_code", goodsInfo.getBarCode());
							orderGoods.put("spec_name", orderGoods.getGoodsSpecNames());
							orderGoods.put("unit_name", orderGoods.getGoodsUnit().getName());
							orderGoods.put("reject_deal_type_name", orderGoods.getRejectDealName());
						}
						List<SaleRejectOrderFee> orderFeeList = order.getOrderFeeList();
						for(SaleRejectOrderFee fee: orderFeeList) {
							order.put("fee_amount_"+fee.getTraderFundType(), fee.getAmount());
						}
						List<SaleRejectOrderCost> orderCostList = order.getOrderCostList();
						for(SaleRejectOrderCost cost: orderCostList) {
							order.put("cost_amount_"+cost.getTraderFundType(), cost.getAmount());
						}
						
						order.put("handler_name", order.getHandler().getRealName());
					}
					
					String filePath = XlsKit.genOrderXls("销售退货单", fileName, headers, orderList, columns, listColumns);
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
	 */
	private boolean updateOrderFund(SaleRejectOrder order, List<SaleRejectOrderFund> orderFundList, boolean auditFlag) {
		boolean modifyFlag = false; // 是否存在数据修改
		List<SaleRejectOrderFund> oldOrderFundList = SaleRejectOrderFund.dao.find("select * from sale_reject_order_fund where sale_reject_order_id = ?", order.getId());
		// 先过滤出已删除的记录
		List<SaleRejectOrderFund> deleteFundList = new ArrayList<>();
		for(SaleRejectOrderFund oldFund : oldOrderFundList) {
			boolean isExist = false;
			for (SaleRejectOrderFund fund : orderFundList) {
				if(fund.getId() != null && fund.getId().intValue() == oldFund.getId().intValue()) {
					isExist = true;
					break;
				}
			}
			if(!isExist) { // 需要删除的记录
				deleteFundList.add(oldFund);
			}
		}
		
		for (SaleRejectOrderFund e : deleteFundList) {
			// 回退资金到帐户
			undoOrderFund(order, e);
			// 删除资金记录
			e.delete();
			modifyFlag = true;
		}
		
		// 账户付款，更新结算帐户资金
		for (SaleRejectOrderFund e : orderFundList) {
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
	private void undoOrderFund(SaleRejectOrder order, SaleRejectOrderFund orderFund) {
		if(order.getAuditStatus() != AuditStatusEnum.pass.getValue()) { // 非审核不处理资金
			return;
		}
		if(orderFund.getReceiptType() == FundTypeEnum.cash.getValue()) { // 本单支付，退回到结算帐户
			TraderBalanceAccount balanceAccount = TraderBalanceAccount.dao.findById(orderFund.getBalanceAccountId());
			balanceAccount.setBalance(balanceAccount.getBalance().add(orderFund.getAmount())); // 回退支付的金额
			balanceAccount.setUpdatedAt(new Date());
			balanceAccount.update();
			
			BigDecimal changeAmount = BigDecimal.ZERO.subtract(orderFund.getAmount()); // 回退支付退款的金额，支付的时候是减少，退款的时候是增加
			fundInTraderBookAccount(order, changeAmount);
			
		} else if(orderFund.getReceiptType() == FundTypeEnum.balance.getValue()) { // 余额支付，退回到往来帐户
			BigDecimal changeAmount = BigDecimal.ZERO.subtract(orderFund.getAmount()); // 回退支付退款的金额，支付的时候是减少，退款的时候是增加
			fundPayTraderBookAccount(order, changeAmount);
		}
	}
	/**
	 * 记录单据其他支出成本，不用供应商支付
	 * @param 
	 * @param order
	 * @param orderCostList
	 */
	private void updateOrderCost(SaleRejectOrder order, List<SaleRejectOrderCost> orderCostList) {
		if(orderCostList == null) {
			orderCostList = new ArrayList<>();
		}
		List<SaleRejectOrderCost> oldOrderFeeList = SaleRejectOrderCost.dao.find("select * from sale_reject_order_cost where sale_reject_order_id = ?", order.getId());
		// 先过滤出已删除的记录
		List<SaleRejectOrderCost> deleteFeeList = new ArrayList<>();
		for(SaleRejectOrderCost oldFee : oldOrderFeeList) {
			boolean isExist = false;
			for (SaleRejectOrderCost fee : orderCostList) {
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
		
		for (SaleRejectOrderCost e : deleteFeeList) {
			// 删除资金记录
			e.delete();
		}
		BigDecimal totalOtherCostAmount = BigDecimal.ZERO;
		for (SaleRejectOrderCost orderFee : orderCostList) {
			if(orderFee.getAmount() == null || orderFee.getAmount().compareTo(BigDecimal.ZERO) == 0 || orderFee.getTraderFundType() == null || orderFee.getTraderFundType() <= 0) {
				continue;
			}
			SaleRejectOrderCost _orderFee = SaleRejectOrderCost.dao.findFirst("select * from sale_reject_order_cost where sale_reject_order_id = ? and trader_fund_type = ? limit 1", order.getId(), orderFee.getTraderFundType());
			if(_orderFee == null) {
				orderFee.setSaleRejectOrderId(order.getId());
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
	private void updateOrderFee(SaleRejectOrder order, List<SaleRejectOrderFee> orderFeeList) {
		if(orderFeeList == null) {
			orderFeeList = new ArrayList<>();
		}
		List<SaleRejectOrderFee> oldOrderFeeList = SaleRejectOrderFee.dao.find("select * from sale_reject_order_fee where sale_reject_order_id = ?", order.getId());
		// 先过滤出已删除的记录
		List<SaleRejectOrderFee> deleteFeeList = new ArrayList<>();
		for(SaleRejectOrderFee oldFee : oldOrderFeeList) {
			boolean isExist = false;
			for (SaleRejectOrderFee fee : orderFeeList) {
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
		
		for (SaleRejectOrderFee e : deleteFeeList) {
			// 删除资金记录
			e.delete();
		}
		BigDecimal totalOtherAmount = BigDecimal.ZERO;
		for (SaleRejectOrderFee orderFee : orderFeeList) {
			if(orderFee.getAmount() == null || orderFee.getAmount().compareTo(BigDecimal.ZERO) == 0 || orderFee.getTraderFundType() == null || orderFee.getTraderFundType() <= 0) {
				continue;
			}
			SaleRejectOrderFee _orderFee = SaleRejectOrderFee.dao.findFirst("select * from sale_reject_order_fee where sale_reject_order_id = ? and trader_fund_type = ? limit 1", order.getId(), orderFee.getTraderFundType());
			if(_orderFee == null) {
				orderFee.setSaleRejectOrderId(order.getId());
				orderFee.save();
			} else {
				_orderFee.setAmount(orderFee.getAmount());
				_orderFee.update();
			}
			totalOtherAmount = totalOtherAmount.add(orderFee.getAmount());
			order.setOtherAmount(totalOtherAmount);
			order.update();
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
	private boolean paymentOrderFund(SaleRejectOrder order, SaleRejectOrderFund orderFund, boolean auditFlag) {
		boolean modifyFlag = false; // 是否存在数据修改
		if(orderFund.getBalanceAccountId() == null || orderFund.getBalanceAccountId() <= 0 || orderFund.getAmount() == null || orderFund.getAmount().doubleValue() <= 0) {
			return modifyFlag;
		}
		BigDecimal changeFundAmount = orderFund.getAmount(); // 变更的金额
		SaleRejectOrderFund _orderFund = SaleRejectOrderFund.dao.findFirst("select * from sale_reject_order_fund where sale_reject_order_id = ? and id = ?", order.getId(), orderFund.getId());
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
			orderFund.setSaleRejectOrderId(order.getId());
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
			balanceAccount.setBalance(balanceAccount.getBalance().subtract(changeFundAmount)); // 退款，结算帐户增加支付的金额
			balanceAccount.setUpdatedAt(new Date());
			balanceAccount.update();
			
			// 更新往来帐户资金
			fundInTraderBookAccount(order, changeFundAmount);
			
		} else if(orderFund.getReceiptType() == FundTypeEnum.balance.getValue()) { // 余额支付，从往来帐户支付
			fundPayTraderBookAccount(order, changeFundAmount);
		}
		
		// 生成交易流水
		TraderFundOrder fundOrder = TraderFundOrder.dao.findByOrderId(orderFund.getBalanceAccountId(), RefOrderTypeEnum.sale_reject_order, order.getId());
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
			fundOrder.setRefOrderType(RefOrderTypeEnum.sale_reject_order.getValue());
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
	 * 更新往来帐户资金：付款，支付货款给客户，从总收入里扣除
	 * @param 
	 * @param order
	 * @param changeAmount
	 */
	private void fundInTraderBookAccount(SaleRejectOrder order, BigDecimal changeAmount) {
		if(order.getAuditStatus() != AuditStatusEnum.pass.getValue()) { // 非审核不处理资金
			return;
		}
		if(changeAmount.compareTo(BigDecimal.ZERO) == 0) {
			return;
		}
		// 更新往来账户金额
		CustomerInfo customerInfo = CustomerInfo.dao.findById(order.getCustomerInfoId());
		TraderBookAccount bookAccount = customerInfo.getTraderBookAccount();
		BigDecimal inAmount = bookAccount.getInAmount().subtract(changeAmount);
		bookAccount.setInAmount(inAmount);
		BigDecimal payAmount = bookAccount.getPayAmount().subtract(changeAmount);
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
	 * 更新往来帐户资金：客户平账资金
	 * @param 
	 * @param order
	 * @param changeAmount
	 */
	private void fundPayTraderBookAccount(SaleRejectOrder order, BigDecimal changeAmount) {
		if(order.getAuditStatus() != AuditStatusEnum.pass.getValue()) { // 非审核不处理资金
			return;
		}
		if(changeAmount.compareTo(BigDecimal.ZERO) == 0) {
			return;
		}
		// 更新往来账户金额
		CustomerInfo customerInfo = CustomerInfo.dao.findById(order.getCustomerInfoId());
		TraderBookAccount bookAccount = customerInfo.getTraderBookAccount();
		BigDecimal payAmount = bookAccount.getPayAmount().subtract(changeAmount);
		bookAccount.setPayAmount(payAmount);
		bookAccount.setUpdatedAt(new Date());
		bookAccount.update();
	}
	
	/**
	 * 更新往来帐户资金：退货，减少货款
	 * @param 
	 * @param order
	 * @param changeAmount
	 */
	private void goodsOutTraderBookAccount(SaleRejectOrder order, BigDecimal changeAmount) {
		if(order.getAuditStatus() != AuditStatusEnum.pass.getValue()) { // 非审核不处理资金
			return;
		}
		if(changeAmount.compareTo(BigDecimal.ZERO) == 0) {
			return;
		}
		// 更新往来账户金额
		CustomerInfo customerInfo = CustomerInfo.dao.findById(order.getCustomerInfoId());
		TraderBookAccount bookAccount = customerInfo.getTraderBookAccount();
		BigDecimal outAmount = bookAccount.getOutAmount().subtract(changeAmount);
		bookAccount.setOutAmount(outAmount);
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
	 * 修改销售退货单时，更新商品数据
	 * @param 
	 * @param order
	 * @param orderGoodList
	 */
	private boolean updateOrderGoods(SaleRejectOrder order, List<SaleRejectOrderGoods> orderGoodList) {
		boolean modifyFlag = false; // 是否存在数据修改
		List<SaleRejectOrderGoods> oldOrderGoodList = SaleRejectOrderGoods.dao.find("select * from sale_reject_order_goods where sale_reject_order_id = ?", order.getId());
		// 先过滤出已删除的记录
		List<SaleRejectOrderGoods> deleteGoodsList = new ArrayList<>();
		for(SaleRejectOrderGoods oldGoods : oldOrderGoodList) {
			boolean isExist = false;
			for (SaleRejectOrderGoods goods : orderGoodList) {
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
		for (SaleRejectOrderGoods e : deleteGoodsList) {
			// 释放销售单退货
			undoOrderReject(order, e);
						
			e.delete();
			modifyFlag = true;
		}
		
		Set<Integer> orderIdList = new HashSet<>(); // 关联退货的单据列表
		// 保存商品
		for (SaleRejectOrderGoods e : orderGoodList) {
			if (e.getGoodsInfoId() == null || e.getGoodsInfoId() <= 0 || e.getBuyNumber().doubleValue() <= 0) {
				continue;
			}
			SaleRejectOrderGoods _e = SaleRejectOrderGoods.dao.findById(e.getId());
			BigDecimal changeNumber = e.getBuyNumber(); // 发生变更的库存数量
			if(_e != null) {
				changeNumber = e.getBuyNumber().subtract(_e.getBuyNumber());
				if(e.getBuyNumber().compareTo(_e.getBuyNumber()) != 0 || e.getDiscountAmount().compareTo(_e.getDiscountAmount()) != 0 || 
						e.getPrice().compareTo(_e.getPrice()) != 0 || e.getAmount().compareTo(_e.getAmount()) != 0 
						|| _e.getRejectDealType() != e.getRejectDealType()
						|| !StringUtils.equals(_e.getRemark(), e.getRemark())) {
					_e.setBuyNumber(e.getBuyNumber());
					_e.setDiscount(e.getDiscount());
					_e.setDiscountAmount(e.getDiscountAmount());
					_e.setPrice(e.getPrice());
					_e.setAmount(e.getAmount());
					_e.setCustomerInfoId(order.getCustomerInfoId());
					_e.setUpdatedAt(new Date());
					_e.setRemark(e.getRemark());
					_e.setRejectDealType(e.getRejectDealType());
					_e.update();
					modifyFlag = true;
				}
			} else {
				e.setCustomerInfoId(order.getCustomerInfoId());
				e.setSaleRejectOrderId(order.getId());
				e.setUpdatedAt(new Date());
				e.setCreatedAt(new Date());
				e.save();
				modifyFlag = true;
			}
			e.put("changeNumber", changeNumber);
			// 销售单退货
			orderReject(order, e, changeNumber, orderIdList);
		}
		if(!orderIdList.isEmpty()) {
			order.setSaleOrderId(","+StringUtils.join(orderIdList, ",")+",");
			order.update();
		}
		
		return modifyFlag;
	}
	
	/**
	 * 商品退货，修改库存
	 * @param tenantOrgId
	 * @param order
	 * @param orderGoods
	 */
	private void updateInventoryStock(SaleRejectOrder order, List<SaleRejectOrderGoods> orderGoodsList) {
		for (SaleRejectOrderGoods orderGoods : orderGoodsList) {
			if(orderGoods.getRejectDealType() == RejectDealTypeEnum.scrap.getValue()) { // 报废，不处理库存
				continue;
			}
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
			if(order.getLogisticsStatus() == LogisticsStatusInEnum.stockin.getValue()) { // 已入库，再次修改库存
				inventoryStock.setStock(inventoryStock.getStock().add(changeStock)); // 减去使用库存
				inventoryStock.setUpdatedAt(new Date());
				inventoryStock.update();
			}
			
			// 库存流水
			InventoryStockLog stockLog = InventoryStockLog.dao.findByOrderId(order.getId(), StockInOutOrderTypeEnum.sale_reject_order.getValue(), orderGoods.getSpec1Id(), orderGoods.getSpecOption1Id(), orderGoods.getSpec2Id(), orderGoods.getSpecOption2Id(), orderGoods.getSpec3Id(), orderGoods.getSpecOption3Id(), orderGoods.getUnitId());
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
				stockLog.setOrderType(StockInOutOrderTypeEnum.sale_reject_order.getValue());
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
	 * 商品库存取消退货
	 * @param 
	 * @param order
	 * @param orderGoods
	 */
	private void undoInventoryStock(SaleRejectOrder order, List<SaleRejectOrderGoods> orderGoodsList) {
		if(order.getLogisticsStatus() != LogisticsStatusInEnum.stockin.getValue()) {
			return;
		}
		for (SaleRejectOrderGoods orderGoods : orderGoodsList) {
			GoodsInfo goodsInfo = GoodsInfo.dao.findById(orderGoods.getGoodsInfoId());
			GoodsUnit stockUnit = goodsInfo.getStockMainUnit(); // 通过库存单位查询库存
			
			BigDecimal goodsStock = orderGoods.getBuyNumber(); // 换算后的库存
			InventoryStock inventoryStock = InventoryStock.dao.findByWarehouse(orderGoods.getGoodsInfoId(), orderGoods.getSpec1Id(), orderGoods.getSpecOption1Id(), orderGoods.getSpec2Id(), orderGoods.getSpecOption2Id(), orderGoods.getSpec3Id(), orderGoods.getSpecOption3Id(), stockUnit.getId());
			inventoryStock.setStock(inventoryStock.getStock().subtract(goodsStock)); // 取消退货
			
			inventoryStock.setUpdatedAt(new Date());
			inventoryStock.update();
			// 删除流水
			InventoryStockLog stockLog = InventoryStockLog.dao.findByOrderId(order.getId(), StockInOutOrderTypeEnum.sale_reject_order.getValue(), orderGoods.getSpec1Id(), orderGoods.getSpecOption1Id(), orderGoods.getSpec2Id(), orderGoods.getSpecOption2Id(), orderGoods.getSpec3Id(), orderGoods.getSpecOption3Id(), orderGoods.getUnitId());
			if(stockLog != null) {
				stockLog.delete();
			}
		}
	}

	/**
	 * 销售单退货
	 * @param 
	 * @param order
	 * @param orderIdList
	 * @param changeGoodsNumber 发生变更的数量
	 * @param _e 
	 * @return
	 */
	private void orderReject(SaleRejectOrder rejectOrder, SaleRejectOrderGoods orderGoods, BigDecimal subtractNumber, Set<Integer> orderIdList) {
		if(rejectOrder.getAuditStatus() != AuditStatusEnum.pass.getValue()) { // 非审核不处理
			return;
		}
		String[] orderIds = StringUtils.split(rejectOrder.getSaleOrderId(), ",");
		if(orderIds == null || orderIds.length <= 0) {
			return;
		}
		for (int i = 0; i < orderIds.length; i++) {
			if(StringUtils.isEmpty(orderIds[i])) {
				continue;
			}
			Integer orderId = Integer.parseInt(orderIds[i]);
			List<SaleOrderGoods> orderSameGoodsList = SaleOrderGoods.dao.findOrderGoods(orderId, orderGoods.getGoodsInfoId(), orderGoods.getSpec1Id(), orderGoods.getSpecOption1Id(), orderGoods.getSpec2Id(), orderGoods.getSpecOption2Id(), orderGoods.getSpec3Id(), orderGoods.getSpecOption3Id(), orderGoods.getUnitId());
			if(orderSameGoodsList == null || orderSameGoodsList.isEmpty()) {
				continue;
			}
			BigDecimal buyNumber =  orderGoods.getBuyNumber();
			BigDecimal totalRejectAmount = orderGoods.getAmount();
			for (SaleOrderGoods orderSameGoods : orderSameGoodsList) {
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
			
			SaleOrder order = SaleOrder.dao.findById(orderId);
			List<SaleOrderGoods> orderGoodsList = order.getOrderGoodsList();
			BigDecimal countBuyNumber = BigDecimal.ZERO;
			BigDecimal countRejectNumber = BigDecimal.ZERO;
			BigDecimal countRejectAmount = BigDecimal.ZERO; // 退货总金额
			for (SaleOrderGoods e : orderGoodsList) {
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
			String operDesc = "全部商品转销售单";
			if(order.getRejectType() ==  RejectTypeEnum.part.getValue()) {
				operDesc = "部分商品转销售单";
			}
			saleOrderService.writeLog(orderId, order.getLastManId(), operDesc);
		}
	}
	
	/**
	 * 回退销售单退货
	 * @param 
	 * @param order
	 * @param orderGoods
	 */
	private void undoOrderReject(SaleRejectOrder rejectOrder, SaleRejectOrderGoods orderGoods) {
		String[] orderIds = StringUtils.split(rejectOrder.getSaleOrderId(), ",");
		if(orderIds == null || orderIds.length <= 0) {
			return;
		}
		for (int i = 0; i < orderIds.length; i++) {
			if(StringUtils.isEmpty(orderIds[i])) {
				continue;
			}
			Integer orderId = Integer.parseInt(orderIds[i]);
			List<SaleOrderGoods> oderGoodsList = SaleOrderGoods.dao.findOrderGoods(orderId, orderGoods.getGoodsInfoId(), orderGoods.getSpec1Id(), orderGoods.getSpecOption1Id(), orderGoods.getSpec2Id(), orderGoods.getSpecOption2Id(), orderGoods.getSpec3Id(), orderGoods.getSpecOption3Id(), orderGoods.getUnitId());
			if(oderGoodsList == null || oderGoodsList.isEmpty()) {
				continue;
			}
			BigDecimal buyNumber =  orderGoods.getBuyNumber();
			BigDecimal totalRejectAmount = orderGoods.getAmount();
			for (SaleOrderGoods orderSameGoods : oderGoodsList) {
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
			
			SaleOrder order = SaleOrder.dao.findById(orderId);
			List<SaleOrderGoods> orderGoodsList = order.getOrderGoodsList();
			BigDecimal countBuyNumber = BigDecimal.ZERO;
			BigDecimal countRejectNumber = BigDecimal.ZERO;
			BigDecimal countRejectAmount = BigDecimal.ZERO; // 退货总金额
			for (SaleOrderGoods e : orderGoodsList) {
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
			String operDesc = "全部取消转销售单";
			if(order.getRejectType() ==  RejectTypeEnum.part.getValue()) {
				operDesc = "部分商品取消转销售单";
			}
			saleOrderService.writeLog(orderId, order.getLastManId(), operDesc);
		}
	}
	
	/**
	 * 设置单据审核状态
	 * @param 
	 * @param order
	 */
	private AuditStatusEnum getAuditStatus(SaleRejectOrder order) {
		TenantConfig auditConfig = TenantConfig.dao.findByKeyCache(TenantConfigEnum.audit_sale_reject_order);
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
	private LogisticsStatusInEnum getLogisticsStatus(SaleRejectOrder order) {
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
	private void sendAuditNoticeMsg(SaleRejectOrder order) {
		CustomerInfo customerInfo = order.getCustomerInfo();
		String title = "客户“" + customerInfo.getName() + "”销售退货单审核通过";
		String content = title;
		if (order.getAuditStatus() == AuditStatusEnum.reject.getValue()) {
			title = "客户“" + customerInfo.getName() + "”销售退货单审核拒绝";
			content = title;
		}
		Boolean smsFlag = SaleRejectOrder.dao.findAuditHandlerSmsNoticeConfig();// 发送短信
		Boolean sysFlag = SaleRejectOrder.dao.findAuditHandlerSysNoticeConfig();// 发送系统消息
		sendNoticeMsg(MsgDataTypeEnum.sale_reject_order_audit, title, content, order.getHandlerId(), smsFlag, sysFlag);

		smsFlag = SaleRejectOrder.dao.findAuditMakeManSmsNoticeConfig();// 发送短信
		sysFlag = SaleRejectOrder.dao.findAuditMakeManSysNoticeConfig();// 发送系统消息
		sendNoticeMsg(MsgDataTypeEnum.sale_reject_order_audit, title, content, order.getMakeManId(), smsFlag, sysFlag);
	}
	
	/**
	 * 发送消息给审核人
	 * @param 
	 * @param order
	 */
	private void sendOrderNoticeMsg(SaleRejectOrder order) {
		if(order.getAuditStatus() != AuditStatusEnum.waiting.getValue() || order.getOrderStatus() != OrderStatusEnum.normal.getValue()) {
			return;
		}
		Integer	auditorId = SaleRejectOrder.dao.findAuditorIdConfig();
		order.setAuditorId(auditorId); // 设置审核人
		Boolean smsFlag = SaleRejectOrder.dao.findAuditSmsNoticeConfig();// 发送短信
		Boolean sysFlag = SaleRejectOrder.dao.findAuditSysNoticeConfig();// 发送系统消息
		CustomerInfo customerInfo = order.getCustomerInfo();
		String title="客户“"+customerInfo.getName()+"”销售退货单审核通知";
		String content = title;
		sendNoticeMsg(MsgDataTypeEnum.sale_reject_order_audit, title, content, order.getAuditorId(), smsFlag, sysFlag);
	}

	/**
	 * 客户应付对账
	 * @param 
	 * @param order
	 */
	private void updateCustomerReceivable(SaleRejectOrder order) {
		if(order.getAuditStatus() != AuditStatusEnum.pass.getValue()) { // 非审核不处理资金
			return;
		}
		TraderCustomerReceivable customerReceivable = TraderCustomerReceivable.dao.findByOrderId(CheckingRefOrderTypeEnum.sale_reject_order, order.getId());
		if(customerReceivable != null) {
			if(order.getOrderStatus() == OrderStatusEnum.disable.getValue()) {
				customerReceivable.delete();
				return;
			}
			BigDecimal discountAmount = order.getGoodsAmount().subtract(order.getDiscountAmount()).add(order.getOddAmount()); // 计算优惠金额
			customerReceivable.setNewAmount(BigDecimal.ZERO.subtract(order.getAmount().add(discountAmount)));
			customerReceivable.setTakeAmount(BigDecimal.ZERO.subtract(order.getPaidAmount()));
			customerReceivable.setDiscountAmount(BigDecimal.ZERO.subtract(discountAmount));
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
			customerReceivable.setRefOrderType(CheckingRefOrderTypeEnum.sale_reject_order.getValue());
			customerReceivable.setCustomerInfoId(order.getCustomerInfoId());
			customerReceivable.setTraderBookAccountId(customerInfo.getTraderBookAccountId());
			customerReceivable.setDiscountAmount(order.getDiscountAmount());
			customerReceivable.setNewAmount(BigDecimal.ZERO.subtract(order.getAmount().add(discountAmount)));
			customerReceivable.setTakeAmount(BigDecimal.ZERO.subtract(order.getPaidAmount()));
			customerReceivable.setDiscountAmount(BigDecimal.ZERO.subtract(discountAmount));
			customerReceivable.setAdjustAmount(BigDecimal.ZERO);
			customerReceivable.setOrderTime(order.getOrderTime());
			customerReceivable.setCreatedAt(new Date());
			customerReceivable.setUpdatedAt(new Date());
			customerReceivable.save();
		}
	}

	

}