package com.bytechainx.psi.fund.service;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.bytechainx.psi.common.EnumConstant.CheckingRefOrderTypeEnum;
import com.bytechainx.psi.common.EnumConstant.DataStatusEnum;
import com.bytechainx.psi.common.EnumConstant.ExportStatusEnum;
import com.bytechainx.psi.common.kit.DateUtil;
import com.bytechainx.psi.common.kit.excel.XlsKit;
import com.bytechainx.psi.common.model.GoodsInfo;
import com.bytechainx.psi.common.model.PurchaseOrderGoods;
import com.bytechainx.psi.common.model.PurchaseRejectOrderGoods;
import com.bytechainx.psi.common.model.SupplierInfo;
import com.bytechainx.psi.common.model.TenantExportLog;
import com.bytechainx.psi.common.model.TraderBookAccount;
import com.bytechainx.psi.common.model.TraderSupplierPayable;
import com.bytechainx.psi.common.service.base.CommonService;
import com.google.common.collect.Lists;
import com.jfinal.kit.Kv;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;


/**
* 供应商对账
*/
public class BookSupplierBillService extends CommonService {


	/**
	* 分页列表
	*/
	public Page<TraderSupplierPayable> paginate(String startDay, String endDay, Kv conditionColumns, Integer pageNumber, int pageSize) {
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
		
		Page<TraderSupplierPayable> page = TraderSupplierPayable.dao.paginate(pageNumber, pageSize, "select supplier_info_id, sum(new_amount) as new_amount, sum(take_amount) as take_amount, sum(discount_amount) as discount_amount ", "from trader_supplier_payable "+where.toString()+" group by supplier_info_id order by id desc", params.toArray());
		for (TraderSupplierPayable e : page.getList()) {
			BigDecimal openBalance = getOpenBalance(e.getSupplierInfoId(), startDay);
			e.put("open_balance", openBalance);
		}
		return page;
	}
	
	/**
	* 所有供应商统计
	*/
	public TraderSupplierPayable sumAmount(String startDay, String endDay, Kv conditionColumns, Integer pageNumber, int pageSize) {
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
		
		TraderSupplierPayable payable = TraderSupplierPayable.dao.findFirst("select sum(new_amount) as new_amount, sum(take_amount) as take_amount, sum(discount_amount) as discount_amount from trader_supplier_payable "+where.toString()+" order by id", params.toArray());
		BigDecimal openBalance = getOpenBalance(conditionColumns.getInt("supplier_info_id"), startDay);
		payable.put("open_balance", openBalance);
		return payable;
	}

	/**
	* 对账单列表明细
	*/
	public Page<TraderSupplierPayable> paginateByList(Integer tenantStoreId, Integer supplierInfoId, String startDay, String endDay, Integer pageNumber, int pageSize) {
		StringBuffer where = new StringBuffer();
		List<Object> params = new ArrayList<Object>();
		where.append("where 1 = 1 ");
		if(tenantStoreId != null && tenantStoreId > 0) {
			where.append(" and tenant_store_id = ?");
			params.add(tenantStoreId);
		}
		if(supplierInfoId != null && supplierInfoId > 0) {
			where.append(" and supplier_info_id = ?");
			params.add(supplierInfoId);
		}
		if(StringUtils.isNotEmpty(startDay)) {
			where.append(" and order_time  >= ?");
			params.add(startDay);
		}
		if(StringUtils.isNotEmpty(endDay)) {
			where.append(" and order_time  <= ?");
			params.add(endDay);
		}

		return TraderSupplierPayable.dao.paginate(pageNumber, pageSize, "select * ", "from trader_supplier_payable "+where.toString()+" order by order_time", params.toArray());
	}
	
	/**
	* 对账单期初计算
	*/
	public BigDecimal getOpenBalance(Integer supplierInfoId, String startDay) {
		StringBuffer where = new StringBuffer();
		List<Object> params = new ArrayList<Object>();
		where.append("where 1 = 1 ");
		if(supplierInfoId != null && supplierInfoId > 0) {
			where.append(" and supplier_info_id = ?");
			params.add(supplierInfoId);
		}
		if(StringUtils.isNotEmpty(startDay)) {
			where.append(" and order_time  < ?");
			params.add(startDay);
		}
		TraderSupplierPayable payable = TraderSupplierPayable.dao.findFirst("select sum(new_amount) as new_amount, sum(take_amount) as take_amount, sum(discount_amount) as discount_amount from trader_supplier_payable "+where.toString(), params.toArray());
		// 计算期初，也就是起始时间之前的期末
		if(payable.getNewAmount() == null) {
			payable.setNewAmount(BigDecimal.ZERO);
		}
		if(payable.getTakeAmount() == null) {
			payable.setTakeAmount(BigDecimal.ZERO);
		}
		if(payable.getDiscountAmount() == null) {
			payable.setDiscountAmount(BigDecimal.ZERO);
		}
		// 计算期初，也就是起始时间之前的期末
		BigDecimal openBalance = BigDecimal.ZERO;
		if (supplierInfoId != null && supplierInfoId > 0) {
			SupplierInfo supplierInfo = SupplierInfo.dao.findById(supplierInfoId);
			openBalance = payable.getNewAmount().subtract(payable.getTakeAmount()).subtract(payable.getDiscountAmount());
			openBalance = openBalance.add(supplierInfo.getTraderBookAccount().getOpenBalance()); // 加上供应商初始期初
		} else {
			TraderBookAccount sumOpenBalance = TraderBookAccount.dao.findFirst("select sum(open_balance) as open_balance from trader_book_account where tenant_org_id = " 
					+ " and id in (select trader_book_account_id from supplier_info where tenant_org_id = "  + " and data_status = " + DataStatusEnum.enable.getValue() + ")");
			openBalance = payable.getNewAmount().subtract(payable.getTakeAmount()).subtract(payable.getDiscountAmount());
			openBalance = openBalance.add(sumOpenBalance.getOpenBalance()); // 加上供应商初始期初
		}
		return openBalance;
	}


	/**
	 * 导出数据xls
	 */
	public Ret export(Integer handlerId, String startDay, String endDay, Kv condKv) {
		String fileName = "供应商对账-"+DateUtil.getSecondNumber(new Date());
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
					List<TraderSupplierPayable> orderList = Lists.newArrayList();
					int pageNumber = 1;
					Page<TraderSupplierPayable> page = paginate(startDay, endDay, condKv, pageNumber, 500);
					while (!page.getList().isEmpty() && orderList.size() < 10000) { // 最大不能超过10000
						orderList.addAll(page.getList());
						pageNumber += 1;
						page = paginate(startDay, endDay, condKv, pageNumber, 200);
					}
					StringBuffer headersBuffer = new StringBuffer();
					StringBuffer columnsBuffer = new StringBuffer();
					
					headersBuffer.append("供应商名称,供应商分类,期初应付款,增加应付款,已付应付款,优惠金额,期末应付款");
					columnsBuffer.append("supplier_info_name,supplier_category_name,open_balance,new_amount,take_amount,discount_amount,close_balance");
					
					String[] headers = headersBuffer.toString().split(",");
					String[] columns = columnsBuffer.toString().split(",");
					
					for (TraderSupplierPayable order : orderList) {
						SupplierInfo supplierInfo = order.getSupplierInfo();
						order.put("supplier_info_name", supplierInfo.getName());
						order.put("supplier_category_name", supplierInfo.getSupplierCategory().getName());
						order.put("close_balance", order.getBigDecimal("open_balance").add(order.getNewAmount().subtract(order.getTakeAmount()).subtract(order.getDiscountAmount())));
					}
					
					String _startDay = DateUtil.getDayStr(DateUtil.getSecondDate(startDay));
					String _endDay = DateUtil.getDayStr(DateUtil.getSecondDate(endDay));
					String filePath = XlsKit.genOrderXls("供应商对账"+_startDay+"~"+_endDay, fileName, headers, orderList, columns, null);
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
	 * @return
	 */
	public Ret exportList(Integer handlerId, Integer tenantStoreId, Integer supplierInfoId, String startDay, String endDay) {
		SupplierInfo supplierInfo = SupplierInfo.dao.findById(supplierInfoId);
		String fileName = supplierInfo.getName() +"-供应商对账明细-"+DateUtil.getSecondNumber(new Date());
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
					List<TraderSupplierPayable> orderList = Lists.newArrayList();
					BigDecimal openBalance = getOpenBalance(supplierInfoId, startDay);
					TraderSupplierPayable openBalancePayable = new TraderSupplierPayable();
					openBalancePayable.setOrderTime(null);
					openBalancePayable.put("order_time_day", "");
					openBalancePayable.setRefOrderCode("");
					openBalancePayable.put("order_type_name", "期初应付款");
					openBalancePayable.put("close_balance", openBalance);
					orderList.add(openBalancePayable);
					
					int pageNumber = 1;
					Page<TraderSupplierPayable> page = paginateByList(tenantStoreId, supplierInfoId, startDay, endDay, pageNumber, 500);
					while (!page.getList().isEmpty() && orderList.size() < 10000) { // 最大不能超过10000
						orderList.addAll(page.getList());
						pageNumber += 1;
						page = paginateByList(tenantStoreId, supplierInfoId, startDay, endDay, pageNumber, 200);
					}
					StringBuffer headersBuffer = new StringBuffer();
					StringBuffer columnsBuffer = new StringBuffer();
					
					headersBuffer.append("单据日期,单据编号,单据类型,商品/数量/金额,增加应付款,已付应付款,优惠金额,剩余应付款,备注");
					columnsBuffer.append("order_time_day,ref_order_code,order_type_name,list,new_amount,take_amount,discount_amount,close_balance,remark");
					
					String[] headers = headersBuffer.toString().split(",");
					String[] columns = columnsBuffer.toString().split(",");
					String[] listColumns = new String[] {"goods_string"};
					
					for (TraderSupplierPayable order : orderList) {
						if(order.getOrderTime() == null) {
							continue;
						}
						Record refOrder = order.getRefOrder();
						if(order.getRefOrderType() == CheckingRefOrderTypeEnum.purchase_order.getValue()) {
							List<PurchaseOrderGoods> goodsList = PurchaseOrderGoods.dao.find("select * from purchase_order_goods where purchase_order_id = ?", order.getRefOrderId());
							order.put("list", goodsList);
							for (PurchaseOrderGoods orderGoods : goodsList) {
								GoodsInfo goodsInfo = orderGoods.getGoodsInfo();
								String goodsSpecNames = orderGoods.getGoodsSpecNames();
								if(StringUtils.isNotEmpty(goodsSpecNames)) {
									orderGoods.put("goods_string", goodsInfo.getName() + "(" + goodsSpecNames +") / "+ orderGoods.getBuyNumber().stripTrailingZeros().toPlainString() + orderGoods.getGoodsUnit().getName() + " / " + orderGoods.getAmount().stripTrailingZeros().toPlainString());
								} else {
									orderGoods.put("goods_string", goodsInfo.getName() + " / "+ orderGoods.getBuyNumber().stripTrailingZeros().toPlainString() + orderGoods.getGoodsUnit().getName() + " / " + orderGoods.getAmount().stripTrailingZeros().toPlainString());
								}
							}
							
						} else if(order.getRefOrderType() == CheckingRefOrderTypeEnum.purchase_reject_order.getValue()) {
							List<PurchaseRejectOrderGoods> goodsList = PurchaseRejectOrderGoods.dao.find("select * from purchase_reject_order_goods where purchase_reject_order_id = ?", order.getRefOrderId());
							order.put("list", goodsList);
							for (PurchaseRejectOrderGoods orderGoods : goodsList) {
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
					String filePath = XlsKit.genOrderXls("供应商对账明细"+_startDay+"~"+_endDay, fileName, headers, orderList, columns, listColumns);
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