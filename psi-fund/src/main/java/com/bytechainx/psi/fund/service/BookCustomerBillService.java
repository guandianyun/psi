package com.bytechainx.psi.fund.service;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.bytechainx.psi.common.EnumConstant.CheckingRefOrderTypeEnum;
import com.bytechainx.psi.common.EnumConstant.DataStatusEnum;
import com.bytechainx.psi.common.EnumConstant.ExportStatusEnum;
import com.bytechainx.psi.common.dto.ConditionFilter;
import com.bytechainx.psi.common.kit.DateUtil;
import com.bytechainx.psi.common.kit.excel.XlsKit;
import com.bytechainx.psi.common.model.CustomerInfo;
import com.bytechainx.psi.common.model.GoodsInfo;
import com.bytechainx.psi.common.model.SaleOrderGoods;
import com.bytechainx.psi.common.model.SaleRejectOrderGoods;
import com.bytechainx.psi.common.model.TenantExportLog;
import com.bytechainx.psi.common.model.TraderBookAccount;
import com.bytechainx.psi.common.model.TraderCustomerReceivable;
import com.bytechainx.psi.common.service.base.CommonService;
import com.google.common.collect.Lists;
import com.jfinal.kit.Kv;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;


/**
* 客户对账
*/
public class BookCustomerBillService extends CommonService {


	/**
	* 分页列表
	*/
	public Page<TraderCustomerReceivable> paginate(String startDay, String endDay, Kv conditionColumns, Integer pageNumber, int pageSize) {
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
		
		Page<TraderCustomerReceivable> page = TraderCustomerReceivable.dao.paginate(pageNumber, pageSize, "select customer_info_id, sum(new_amount) as new_amount, sum(take_amount) as take_amount, sum(discount_amount) as discount_amount ", "from trader_customer_receivable "+where.toString()+" group by customer_info_id order by id desc", params.toArray());
		// 计算期初金额
		for (TraderCustomerReceivable e : page.getList()) {
			BigDecimal openBalance = getOpenBalance(e.getCustomerInfoId()+"", startDay);
			e.put("open_balance", openBalance);
		}
		return page;
	}
	

	/**
	* 所有客户统计
	*/
	public TraderCustomerReceivable sumAmount(String startDay, String endDay, Kv conditionColumns, Integer pageNumber, int pageSize) {
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
		
		TraderCustomerReceivable traderCustomerReceivable = TraderCustomerReceivable.dao.findFirst("select sum(new_amount) as new_amount, sum(take_amount) as take_amount, sum(discount_amount) as discount_amount from trader_customer_receivable "+where.toString()+" order by id", params.toArray());
	
		// 查询的客户ID
		Object customerInfoIdValue = conditionColumns.get("customer_info_id");
		String customerInfoIds = "";
		if(customerInfoIdValue instanceof Integer) {
			customerInfoIds = (Integer)customerInfoIdValue+"";
		} else if(customerInfoIdValue instanceof ConditionFilter) {
			ConditionFilter filter = (ConditionFilter) customerInfoIdValue;
			customerInfoIds = (String)filter.getValue();
		}
		BigDecimal openBalance = getOpenBalance(customerInfoIds, startDay);
		traderCustomerReceivable.put("open_balance", openBalance);
		return traderCustomerReceivable;
	}

	/**
	* 对账单列表明细
	 * @param customerInfoId2 
	*/
	public Page<TraderCustomerReceivable> paginateByList(Integer tenantStoreId, Integer customerInfoId, String startDay, String endDay, Integer pageNumber, int pageSize) {
		StringBuffer where = new StringBuffer();
		List<Object> params = new ArrayList<Object>();
		where.append("where 1 = 1 ");
		if(customerInfoId != null && customerInfoId > 0) {
			where.append(" and customer_info_id = ?");
			params.add(customerInfoId);
		}
		if(StringUtils.isNotEmpty(startDay)) {
			where.append(" and order_time  >= ?");
			params.add(startDay);
		}
		if(StringUtils.isNotEmpty(endDay)) {
			where.append(" and order_time  <= ?");
			params.add(endDay);
		}

		return TraderCustomerReceivable.dao.paginate(pageNumber, pageSize, "select * ", "from trader_customer_receivable "+where.toString()+" order by order_time ", params.toArray());
	}
	
	/**
	* 对账单期初计算
	*/
	public BigDecimal getOpenBalance(String customerInfoIds, String startDay) {
		StringBuffer where = new StringBuffer();
		List<Object> params = new ArrayList<Object>();
		where.append("where 1 = 1 ");
		if(StringUtils.isNotEmpty(customerInfoIds)) {
			where.append(" and customer_info_id in (?)");
			params.add(customerInfoIds);
		}
		if(StringUtils.isNotEmpty(startDay)) {
			where.append(" and order_time  < ?");
			params.add(startDay);
		}
		TraderCustomerReceivable receivable = TraderCustomerReceivable.dao.findFirst("select sum(new_amount) as new_amount, sum(take_amount) as take_amount, sum(discount_amount) as discount_amount from trader_customer_receivable "+where.toString(), params.toArray());
		// 计算期初，也就是起始时间之前的期末
		if(receivable.getNewAmount() == null) {
			receivable.setNewAmount(BigDecimal.ZERO);
		}
		if(receivable.getTakeAmount() == null) {
			receivable.setTakeAmount(BigDecimal.ZERO);
		}
		if(receivable.getDiscountAmount() == null) {
			receivable.setDiscountAmount(BigDecimal.ZERO);
		}
		// 计算期初，也就是起始时间之前的期末
		BigDecimal openBalance = BigDecimal.ZERO;
		if(StringUtils.isNotEmpty(customerInfoIds)) {
			TraderBookAccount sumOpenBalance = TraderBookAccount.dao.findFirst("select sum(open_balance) as open_balance from trader_book_account where 1 = 1 " + " and id in (select trader_book_account_id from customer_info where 1 = 1 " +" and data_status = "+DataStatusEnum.enable.getValue()+" and id in ("+customerInfoIds+"))");
			openBalance = receivable.getNewAmount().subtract(receivable.getTakeAmount()).subtract(receivable.getDiscountAmount());
			openBalance = openBalance.add(sumOpenBalance.getOpenBalance()); // 加上客户初始期初
		} else {
			TraderBookAccount sumOpenBalance = TraderBookAccount.dao.findFirst("select sum(open_balance) as open_balance from trader_book_account where 1 = 1 " + " and id in (select trader_book_account_id from customer_info where 1 = 1 " +" and data_status = "+DataStatusEnum.enable.getValue()+")");
			openBalance = receivable.getNewAmount().subtract(receivable.getTakeAmount()).subtract(receivable.getDiscountAmount());
			openBalance = openBalance.add(sumOpenBalance.getOpenBalance()); // 加上客户初始期初
		}
		return openBalance;
	}
	
	
	/**
	 * 导出数据xls
	 */
	public Ret export(Integer handlerId, String startDay, String endDay, Kv condKv) {
		String fileName = "客户对账-"+DateUtil.getSecondNumber(new Date());
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
					List<TraderCustomerReceivable> orderList = Lists.newArrayList();
					int pageNumber = 1;
					Page<TraderCustomerReceivable> page = paginate(startDay, endDay, condKv, pageNumber, 500);
					while (!page.getList().isEmpty() && orderList.size() < 10000) { // 最大不能超过10000
						orderList.addAll(page.getList());
						pageNumber += 1;
						page = paginate(startDay, endDay, condKv, pageNumber, 200);
					}
					StringBuffer headersBuffer = new StringBuffer();
					StringBuffer columnsBuffer = new StringBuffer();
					headersBuffer.append("客户名称,客户分类,结算账期,期初应收款,增加应收款,收回应收款,优惠金额,期末应收款");
					columnsBuffer.append("customer_info_name,customer_category_name,pay_type_name,open_balance,new_amount,take_amount,discount_amount,close_balance");
					
					String[] headers = headersBuffer.toString().split(",");
					String[] columns = columnsBuffer.toString().split(",");
					
					for (TraderCustomerReceivable order : orderList) {
						CustomerInfo customerInfo = order.getCustomerInfo();
						order.put("customer_info_name", customerInfo.getName());
						String payTypeName = customerInfo.getPayTypeName();
						if(customerInfo.getPayDay() != null && customerInfo.getPayDay() > 0) {
							payTypeName += "(" + customerInfo.getPayDay() + ")";
						}
						order.put("customer_category_name", customerInfo.getCustomerCategory().getName());
						order.put("pay_type_name", payTypeName);
						order.put("close_balance", order.getBigDecimal("open_balance").add(order.getNewAmount().subtract(order.getTakeAmount()).subtract(order.getDiscountAmount())));
					}
					String _startDay = DateUtil.getDayStr(DateUtil.getSecondDate(startDay));
					String _endDay = DateUtil.getDayStr(DateUtil.getSecondDate(endDay));
					String filePath = XlsKit.genOrderXls("客户对账"+_startDay+"~"+_endDay, fileName, headers, orderList, columns, null);
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
	 * 导出明细
	 * @param tenantOrgId
	 * @param adminId
	 * @param tenantStoreId
	 * @param customerInfoId
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public Ret exportList(Integer handlerId, Integer tenantStoreId, Integer customerInfoId, String startDay, String endDay) {
		CustomerInfo customerInfo = CustomerInfo.dao.findById(customerInfoId);
		String fileName = customerInfo.getName() +"-客户对账明细-"+DateUtil.getSecondNumber(new Date());
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
					List<TraderCustomerReceivable> orderList = Lists.newArrayList();
					BigDecimal openBalance = getOpenBalance(customerInfoId+"", startDay);
					TraderCustomerReceivable openBalanceReceivable = new TraderCustomerReceivable();
					openBalanceReceivable.setOrderTime(null);
					openBalanceReceivable.put("order_time_day", "");
					openBalanceReceivable.setRefOrderCode("");
					openBalanceReceivable.put("order_type_name", "期初应收款");
					openBalanceReceivable.put("close_balance", openBalance);
					orderList.add(openBalanceReceivable);
					
					int pageNumber = 1;
					Page<TraderCustomerReceivable> page = paginateByList(tenantStoreId, customerInfoId, startDay, endDay, pageNumber, 500);
					while (!page.getList().isEmpty() && orderList.size() < 10000) { // 最大不能超过10000
						orderList.addAll(page.getList());
						pageNumber += 1;
						page = paginateByList(tenantStoreId, customerInfoId, startDay, endDay, pageNumber, 200);
					}
					StringBuffer headersBuffer = new StringBuffer();
					StringBuffer columnsBuffer = new StringBuffer();
					
					headersBuffer.append("单据日期,单据编号,单据类型,商品/数量/金额,增加应收款,收回应收款,优惠金额,剩余应收款,备注");
					columnsBuffer.append("order_time_day,ref_order_code,order_type_name,list,new_amount,take_amount,discount_amount,close_balance,remark");
					
					String[] headers = headersBuffer.toString().split(",");
					String[] columns = columnsBuffer.toString().split(",");
					String[] listColumns = new String[] {"goods_string"};
					
					for (TraderCustomerReceivable order : orderList) {
						if(order.getOrderTime() == null) {
							continue;
						}
						Record refOrder = order.getRefOrder();
						
						if(order.getRefOrderType() == CheckingRefOrderTypeEnum.sale_order.getValue()) {
							List<SaleOrderGoods> goodsList = SaleOrderGoods.dao.find("select * from sale_order_goods where sale_order_id = ?", order.getRefOrderId());
							order.put("list", goodsList);
							for (SaleOrderGoods orderGoods : goodsList) {
								GoodsInfo goodsInfo = orderGoods.getGoodsInfo();
								String goodsSpecNames = orderGoods.getGoodsSpecNames();
								if(StringUtils.isNotEmpty(goodsSpecNames)) {
									orderGoods.put("goods_string", goodsInfo.getName() + "(" + goodsSpecNames +") / "+ orderGoods.getBuyNumber().stripTrailingZeros().toPlainString() + orderGoods.getGoodsUnit().getName() + " / " + orderGoods.getAmount().stripTrailingZeros().toPlainString());
								} else {
									orderGoods.put("goods_string", goodsInfo.getName() + " / "+ orderGoods.getBuyNumber().stripTrailingZeros().toPlainString() + orderGoods.getGoodsUnit().getName() + " / " + orderGoods.getAmount().stripTrailingZeros().toPlainString());
								}
							}
							
						} else if(order.getRefOrderType() == CheckingRefOrderTypeEnum.sale_reject_order.getValue()) {
							List<SaleRejectOrderGoods> goodsList = SaleRejectOrderGoods.dao.find("select * from sale_reject_order_goods where sale_reject_order_id = ?", order.getRefOrderId());
							order.put("list", goodsList);
							for (SaleRejectOrderGoods orderGoods : goodsList) {
								GoodsInfo goodsInfo = orderGoods.getGoodsInfo();
								String goodsSpecNames = orderGoods.getGoodsSpecNames();
								if(StringUtils.isNotEmpty(goodsSpecNames)) {
									orderGoods.put("goods_string", goodsInfo.getName() + "(" + goodsSpecNames +") / "+ orderGoods.getBuyNumber().stripTrailingZeros().toPlainString() + orderGoods.getGoodsUnit().getName() + " / " + orderGoods.getAmount().stripTrailingZeros().toPlainString());
								} else {
									orderGoods.put("goods_string", goodsInfo.getName() + " / "+ orderGoods.getBuyNumber().stripTrailingZeros().toPlainString() + orderGoods.getGoodsUnit().getName() + " / " + orderGoods.getAmount().stripTrailingZeros().toPlainString());
								}
							}
						}
						
						order.put("order_time_day", DateUtil.getDayStr(refOrder.getDate("order_time")));
						order.put("order_type_name", order.getRefOrderTypeName());
						order.put("close_balance", order.getNewAmount().subtract(order.getTakeAmount()).subtract(order.getDiscountAmount()));
						order.put("remark", refOrder.getStr("remark"));
					}
					String _startDay = DateUtil.getDayStr(DateUtil.getSecondDate(startDay));
					String _endDay = DateUtil.getDayStr(DateUtil.getSecondDate(endDay));
					String filePath = XlsKit.genOrderXls("客户对账明细"+_startDay+"~"+_endDay, fileName, headers, orderList, columns, listColumns);
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

}