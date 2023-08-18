package com.bytechainx.psi.common.model;

import java.util.ArrayList;
import java.util.List;

import com.bytechainx.psi.common.EnumConstant.FlagEnum;
import com.bytechainx.psi.common.EnumConstant.PrintTemplateOrderTypeEnum;
import com.bytechainx.psi.common.model.base.BaseTenantPrintTemplate;
import com.jfinal.kit.Kv;

/**
 * 打印模板
 */
@SuppressWarnings("serial")
public class TenantPrintTemplate extends BaseTenantPrintTemplate<TenantPrintTemplate> {
	
	public static final TenantPrintTemplate dao = new TenantPrintTemplate().dao();
	
	public TenantPrintTemplate findById(Integer id) {
		return TenantPrintTemplate.dao.findFirst("select * from tenant_print_template where id = ? limit 1", id);
	}
	
	public TenantPrintTemplate findDefault(Integer orderType) {
		return TenantPrintTemplate.dao.findFirst("select * from tenant_print_template where order_type = ? and default_flag = ? limit 1", orderType, FlagEnum.YES.getValue());
	}
	
	public List<TenantPrintTemplate> findAll() {
		return TenantPrintTemplate.dao.find("select * from tenant_print_template ");
	}
	
	/**
	 * 页眉
	 * @return
	 */
	public List<Kv> getPageTop() {
		List<Kv> colList = new ArrayList<>();
		Integer orderType = getOrderType();
		if(orderType == PrintTemplateOrderTypeEnum.goods_tag.getValue()) {
			
		} else if(orderType == PrintTemplateOrderTypeEnum.sale_order.getValue()) {
			
		} else if(orderType == PrintTemplateOrderTypeEnum.sale_reject_order.getValue()) {
			
		} else if(orderType == PrintTemplateOrderTypeEnum.purchase_order.getValue()) {
			
		} else if(orderType == PrintTemplateOrderTypeEnum.purchase_reject_order.getValue()) {
	        
		} else if(orderType == PrintTemplateOrderTypeEnum.inventory_checking.getValue()) {
			
		} else if(orderType == PrintTemplateOrderTypeEnum.trader_receipt_order.getValue()) {
			
		} else if(orderType == PrintTemplateOrderTypeEnum.trader_pay_order.getValue()) {
			
		}
		return colList;
	}
	
	/**
	 * 页头
	 * @return
	 */
	public List<Kv> getPageHeader() {
		List<Kv> colList = new ArrayList<>();
		Integer orderType = getOrderType();
		if(orderType == PrintTemplateOrderTypeEnum.goods_tag.getValue()) {
			colList.add(getColMeta("goods_bar_code", "条码", "string", "single-item"));
	        colList.add(getColMeta("goods_name", "品名", "string", "item"));
	        colList.add(getColMeta("goods_spec", "规格", "string", "item"));
//	        colList.add(getColMeta("goods_attribute", "属性", "string", "item"));
	        colList.add(getColMeta("unit_name", "单位", "string", "item"));
	        colList.add(getColMeta("price", "销售价", "double", "item"));
	        
		} else if(orderType == PrintTemplateOrderTypeEnum.sale_order.getValue()) {
			colList.add(getColMeta("order_code", "销售单号", "string", "item"));
	        colList.add(getColMeta("order_bar_code", "销售单号(条码)", "string", "single-item"));
	        colList.add(getColMeta("book_order_code", "订单号", "string", "item"));
	        colList.add(getColMeta("customer_name", "客户姓名", "string", "item"));
	        colList.add(getColMeta("customer_mobile", "客户电话", "string", "item"));
	        colList.add(getColMeta("customer_address", "客户地址", "string", "item"));
	        colList.add(getColMeta("customer_remark", "客户备注", "string", "item"));
	        colList.add(getColMeta("order_time", "销售日期", "date", "item"));
	        colList.add(getColMeta("company_full_name", "公司全称", "string", "item"));
            colList.add(getColMeta("company_name", "公司名称", "string", "item"));
            colList.add(getColMeta("company_mobile", "公司电话", "string", "item"));
            colList.add(getColMeta("handler_name", "业务员", "string", "item"));
            colList.add(getColMeta("handler_mobile", "业务员电话", "string", "item"));
	        
		} else if(orderType == PrintTemplateOrderTypeEnum.sale_reject_order.getValue()) {
			colList.add(getColMeta("order_code", "退货单号", "string", "item"));
	        colList.add(getColMeta("order_bar_code", "退货单号(条码)", "string", "single-item"));
	        colList.add(getColMeta("sale_order_code", "销售单号", "string", "item"));
	        colList.add(getColMeta("customer_name", "客户姓名", "string", "item"));
	        colList.add(getColMeta("customer_mobile", "客户电话", "string", "item"));
	        colList.add(getColMeta("customer_address", "客户地址", "string", "item"));
	        colList.add(getColMeta("customer_remark", "客户备注", "string", "item"));
	        colList.add(getColMeta("reject_reason", "退货原因", "string", "item"));
	        colList.add(getColMeta("order_time", "退货日期", "date", "item"));
	        colList.add(getColMeta("company_full_name", "公司全称", "string", "item"));
            colList.add(getColMeta("company_name", "公司名称", "string", "item"));
            colList.add(getColMeta("company_mobile", "公司电话", "string", "item"));
            colList.add(getColMeta("handler_name", "退货员", "string", "item"));
            colList.add(getColMeta("handler_mobile", "退货员电话", "string", "item"));
            
		} else if(orderType == PrintTemplateOrderTypeEnum.purchase_order.getValue()) {
			colList.add(getColMeta("order_code", "进货单号", "string", "item"));
	        colList.add(getColMeta("order_bar_code", "进货单号(条码)", "string", "single-item"));
	        colList.add(getColMeta("book_order_code", "订单号", "string", "item"));
	        colList.add(getColMeta("order_time", "到货日期", "date", "item"));
	        colList.add(getColMeta("supplier_name", "供应商名称", "string", "item"));
	        colList.add(getColMeta("supplier_mobile", "供应商电话", "string", "item"));
	        colList.add(getColMeta("supplier_address", "供应商地址", "string", "item"));
	        colList.add(getColMeta("company_full_name", "公司全称", "string", "item"));
            colList.add(getColMeta("company_name", "公司名称", "string", "item"));
            colList.add(getColMeta("company_mobile", "公司电话", "string", "item"));
            colList.add(getColMeta("handler_name", "进货员", "string", "item"));
            colList.add(getColMeta("handler_mobile", "进货员电话", "string", "item"));
	        
		} else if(orderType == PrintTemplateOrderTypeEnum.purchase_reject_order.getValue()) {
			colList.add(getColMeta("order_code", "退货单号", "string", "item"));
	        colList.add(getColMeta("order_bar_code", "退货单号(条码)", "string", "single-item"));
	        colList.add(getColMeta("sale_order_code", "进货单号", "string", "item"));
	        colList.add(getColMeta("order_time", "退货日期", "date", "item"));
	        colList.add(getColMeta("supplier_name", "供应商名称", "string", "item"));
	        colList.add(getColMeta("supplier_mobile", "供应商电话", "string", "item"));
	        colList.add(getColMeta("supplier_address", "供应商地址", "string", "item"));
	        colList.add(getColMeta("company_full_name", "公司全称", "string", "item"));
            colList.add(getColMeta("company_name", "公司名称", "string", "item"));
            colList.add(getColMeta("company_mobile", "公司电话", "string", "item"));
            colList.add(getColMeta("handler_name", "退货员", "string", "item"));
            colList.add(getColMeta("handler_mobile", "退货员电话", "string", "item"));
            
		} else if(orderType == PrintTemplateOrderTypeEnum.inventory_checking.getValue()) {
			colList.add(getColMeta("order_code", "盘点单号", "string", "item"));
	        colList.add(getColMeta("order_bar_code", "盘点单号(条码)", "string", "single-item"));
	        colList.add(getColMeta("order_time", "盘点日期", "date", "item"));
	        colList.add(getColMeta("company_full_name", "公司全称", "string", "item"));
            colList.add(getColMeta("company_name", "公司名称", "string", "item"));
            colList.add(getColMeta("company_mobile", "公司电话", "string", "item"));
            colList.add(getColMeta("handler_name", "盘点员", "string", "item"));
            colList.add(getColMeta("handler_mobile", "盘点员电话", "string", "item"));
			
		} else if(orderType == PrintTemplateOrderTypeEnum.trader_receipt_order.getValue()) {
			colList.add(getColMeta("order_code", "收款单号", "string", "item"));
	        colList.add(getColMeta("order_bar_code", "收款单号(条码)", "string", "single-item"));
	        colList.add(getColMeta("customer_name", "客户姓名", "string", "item"));
	        colList.add(getColMeta("customer_mobile", "客户电话", "string", "item"));
	        colList.add(getColMeta("customer_address", "客户地址", "string", "item"));
	        colList.add(getColMeta("customer_remark", "客户备注", "string", "item"));
	        colList.add(getColMeta("order_time", "单据日期", "date", "item"));
	        colList.add(getColMeta("company_full_name", "公司全称", "string", "item"));
            colList.add(getColMeta("company_name", "公司名称", "string", "item"));
            colList.add(getColMeta("company_mobile", "公司电话", "string", "item"));
            colList.add(getColMeta("handler_name", "经办人", "string", "item"));
            colList.add(getColMeta("handler_mobile", "经办人电话", "string", "item"));
            
		} else if(orderType == PrintTemplateOrderTypeEnum.trader_pay_order.getValue()) {
			colList.add(getColMeta("order_code", "付款单号", "string", "item"));
	        colList.add(getColMeta("order_bar_code", "付款单号(条码)", "string", "single-item"));
	        colList.add(getColMeta("supplier_name", "供应商名称", "string", "item"));
	        colList.add(getColMeta("supplier_mobile", "供应商电话", "string", "item"));
	        colList.add(getColMeta("supplier_address", "供应商地址", "string", "item"));
	        colList.add(getColMeta("order_time", "单据日期", "date", "item"));
	        colList.add(getColMeta("company_full_name", "公司全称", "string", "item"));
            colList.add(getColMeta("company_name", "公司名称", "string", "item"));
            colList.add(getColMeta("company_mobile", "公司电话", "string", "item"));
            colList.add(getColMeta("handler_name", "经办人", "string", "item"));
            colList.add(getColMeta("handler_mobile", "经办人电话", "string", "item"));
		}
		return colList;
	}
	
	/**
	 * 表格明细
	 * @return
	 */
	public List<Kv> getPageTable() {
		List<Kv> colList = new ArrayList<>();
		Integer orderType = getOrderType();
		if(orderType == PrintTemplateOrderTypeEnum.goods_tag.getValue()) {
			
		} else if(orderType == PrintTemplateOrderTypeEnum.sale_order.getValue()) {
			colList.add(getColMeta("goods_name", "品名", "string", "item"));
	        colList.add(getColMeta("goods_spec", "规格", "string", "item"));
	        colList.add(getColMeta("goods_bar_code", "条码", "string", "item"));
	        colList.add(getColMeta("goods_unit_name", "单位", "string", "item"));
	        colList.add(getColMeta("goods_buy_number", "数量", "number", "item"));
	        colList.add(getColMeta("goods_price", "单价", "double", "item"));
	        colList.add(getColMeta("goods_discount", "折扣", "double", "item"));
	        colList.add(getColMeta("goods_discount_amount", "折后单价", "double", "item"));//折扣后的单价
	        colList.add(getColMeta("goods_discount_total", "优惠", "double", "item"));
	        colList.add(getColMeta("goods_total_amount", "金额", "double", "item"));
	        colList.add(getColMeta("goods_remark", "备注", "string", "item"));
	        // 商品属性
	        List<GoodsAttribute> attributeList = GoodsAttribute.dao.findAll();
	        for (GoodsAttribute goodsAttribute : attributeList) {
	        	colList.add(getColMeta("goods_attribute_"+goodsAttribute.getId(), goodsAttribute.getName(), "string", "item"));
			}
	        
		} else if(orderType == PrintTemplateOrderTypeEnum.sale_reject_order.getValue()) {
			colList.add(getColMeta("goods_name", "品名", "string", "item"));
	        colList.add(getColMeta("goods_spec", "规格", "string", "item"));
	        colList.add(getColMeta("goods_bar_code", "条码", "string", "item"));
	        colList.add(getColMeta("goods_reject_deal", "处理方式", "string", "item"));
	        colList.add(getColMeta("goods_unit_name", "单位", "string", "item"));
	        colList.add(getColMeta("goods_buy_number", "数量", "number", "item"));
	        colList.add(getColMeta("goods_price", "单价", "double", "item"));
	        colList.add(getColMeta("goods_discount", "折扣", "double", "item"));
	        colList.add(getColMeta("goods_discount_amount", "折后单价", "double", "item"));//折扣后的单价
	        colList.add(getColMeta("goods_discount_total", "优惠", "double", "item"));
	        colList.add(getColMeta("goods_total_amount", "金额", "double", "item"));
	        colList.add(getColMeta("goods_remark", "备注", "string", "item"));
	        // 商品属性
	        List<GoodsAttribute> attributeList = GoodsAttribute.dao.findAll();
	        for (GoodsAttribute goodsAttribute : attributeList) {
	        	colList.add(getColMeta("goods_attribute_"+goodsAttribute.getId(), goodsAttribute.getName(), "string", "item"));
			}
	        
		} else if(orderType == PrintTemplateOrderTypeEnum.purchase_order.getValue()) {
			colList.add(getColMeta("goods_name", "品名", "string", "item"));
	        colList.add(getColMeta("goods_spec", "规格", "string", "item"));
	        colList.add(getColMeta("goods_bar_code", "条码", "string", "item"));
	        colList.add(getColMeta("goods_unit_name", "单位", "string", "item"));
	        colList.add(getColMeta("goods_buy_number", "数量", "number", "item"));
	        colList.add(getColMeta("goods_price", "单价", "double", "item"));
	        colList.add(getColMeta("goods_discount", "折扣", "double", "item"));
	        colList.add(getColMeta("goods_discount_amount", "折后单价", "double", "item"));//折扣后的单价
	        colList.add(getColMeta("goods_discount_total", "优惠", "double", "item"));
	        colList.add(getColMeta("goods_total_amount", "金额", "double", "item"));
	        colList.add(getColMeta("goods_remark", "备注", "string", "item"));
	        // 商品属性
	        List<GoodsAttribute> attributeList = GoodsAttribute.dao.findAll();
	        for (GoodsAttribute goodsAttribute : attributeList) {
	        	colList.add(getColMeta("goods_attribute_"+goodsAttribute.getId(), goodsAttribute.getName(), "string", "item"));
			}
	        
	        
		} else if(orderType == PrintTemplateOrderTypeEnum.purchase_reject_order.getValue()) {
			colList.add(getColMeta("goods_name", "品名", "string", "item"));
	        colList.add(getColMeta("goods_spec", "规格", "string", "item"));
	        colList.add(getColMeta("goods_bar_code", "条码", "string", "item"));
	        colList.add(getColMeta("goods_unit_name", "单位", "string", "item"));
	        colList.add(getColMeta("goods_buy_number", "数量", "number", "item"));
	        colList.add(getColMeta("goods_price", "单价", "double", "item"));
	        colList.add(getColMeta("goods_discount", "折扣", "double", "item"));
	        colList.add(getColMeta("goods_discount_amount", "折后单价", "double", "item"));//折扣后的单价
	        colList.add(getColMeta("goods_discount_total", "优惠", "double", "item"));
	        colList.add(getColMeta("goods_total_amount", "金额", "double", "item"));
	        colList.add(getColMeta("goods_remark", "备注", "string", "item"));
	        // 商品属性
	        List<GoodsAttribute> attributeList = GoodsAttribute.dao.findAll();
	        for (GoodsAttribute goodsAttribute : attributeList) {
	        	colList.add(getColMeta("goods_attribute_"+goodsAttribute.getId(), goodsAttribute.getName(), "string", "item"));
			}
	        
		} else if(orderType == PrintTemplateOrderTypeEnum.inventory_checking.getValue()) {
			colList.add(getColMeta("goods_name", "品名", "string", "item"));
	        colList.add(getColMeta("goods_spec", "规格", "string", "item"));
	        colList.add(getColMeta("goods_bar_code", "条码", "string", "item"));
	        colList.add(getColMeta("goods_unit_name", "单位", "string", "item"));
	        colList.add(getColMeta("goods_check_number", "盘点数量", "number", "item"));
	        colList.add(getColMeta("goods_profit_loss_number", "盈亏数量", "double", "item"));
	        colList.add(getColMeta("goods_profit_loss", "盈亏金额", "double", "item"));
	        colList.add(getColMeta("goods_remark", "备注", "string", "item"));
	        
		} else if(orderType == PrintTemplateOrderTypeEnum.trader_receipt_order.getValue()) {
			colList.add(getColMeta("balance_account_name", "收款账户", "string", "item"));
	        colList.add(getColMeta("receipt_amount", "收款金额", "number", "item"));
	        colList.add(getColMeta("fund_time", "收款时间", "date", "item"));
	        
		} else if(orderType == PrintTemplateOrderTypeEnum.trader_pay_order.getValue()) {
			colList.add(getColMeta("balance_account_name", "付款账户", "string", "item"));
	        colList.add(getColMeta("pay_amount", "付款金额", "number", "item"));
	        colList.add(getColMeta("fund_time", "付款时间", "date", "item"));
		}
		return colList;
	}
	
	/**
	 * 页尾
	 * @return
	 */
	public List<Kv> getPageFooter() {
		List<Kv> colList = new ArrayList<>();
		Integer orderType = getOrderType();
		if(orderType == PrintTemplateOrderTypeEnum.goods_tag.getValue()) {
			
		} else if(orderType == PrintTemplateOrderTypeEnum.sale_order.getValue()) {
			colList.add(getColMeta("amount", "金额合计", "double", "item"));
            colList.add(getColMeta("amount_cn", "金额合计(大写)", "double", "item"));
            colList.add(getColMeta("discount", "折扣", "double", "item"));
            colList.add(getColMeta("promotion_amount", "优惠金额", "double", "item"));
            colList.add(getColMeta("discount_amount", "应收金额", "double", "item"));
            colList.add(getColMeta("discount_amount_cn", "应收金额(大写)", "string", "item"));
            colList.add(getColMeta("paid_amount", "已收金额", "double", "item"));
            colList.add(getColMeta("remain_amount", "未收金额", "double", "item"));
            colList.add(getColMeta("fund_list", "收款明细", "string", "item"));
            colList.add(getColMeta("total_number", "商品总数", "number", "item"));
            colList.add(getColMeta("goods_amount", "商品金额合计", "double", "item"));
            colList.add(getColMeta("goods_amount_cn", "商品金额合计(大写)", "string", "item"));
            List<TraderFundType> feeList = SaleOrder.dao.findFeeConfig();
            if(feeList != null) {
            	for (TraderFundType fund : feeList) {
                	colList.add(getColMeta("fee_amount_"+fund.getId(), fund.getName(), "double", "item"));
    			}
            }
            colList.add(getColMeta("express_order", "快递单号", "string", "item"));
            colList.add(getColMeta("express_name", "快递公司", "string", "item"));
            colList.add(getColMeta("remark", "备注", "string", "item"));
            
		} else if(orderType == PrintTemplateOrderTypeEnum.sale_reject_order.getValue()) {
			colList.add(getColMeta("amount", "金额合计", "double", "item"));
            colList.add(getColMeta("amount_cn", "金额合计(大写)", "double", "item"));
            colList.add(getColMeta("discount", "折扣", "double", "item"));
            colList.add(getColMeta("promotion_amount", "优惠金额", "double", "item"));
            colList.add(getColMeta("discount_amount", "应退金额", "double", "item"));
            colList.add(getColMeta("discount_amount_cn", "应退金额(大写)", "string", "item"));
            colList.add(getColMeta("paid_amount", "已退金额", "double", "item"));
            colList.add(getColMeta("remain_amount", "未退金额", "double", "item"));
            colList.add(getColMeta("fund_list", "退款明细", "string", "item"));
            colList.add(getColMeta("total_number", "商品总数", "number", "item"));
            colList.add(getColMeta("goods_amount", "商品金额合计", "double", "item"));
            colList.add(getColMeta("goods_amount_cn", "商品金额合计(大写)", "string", "item"));
            List<TraderFundType> feeList = SaleRejectOrder.dao.findFeeConfig();
            if(feeList != null) {
            	for (TraderFundType fund : feeList) {
                	colList.add(getColMeta("fee_amount_"+fund.getId(), fund.getName(), "double", "item"));
    			}
            }
            colList.add(getColMeta("remark", "备注", "string", "item"));
            
		} else if(orderType == PrintTemplateOrderTypeEnum.purchase_order.getValue()) {
			colList.add(getColMeta("amount", "金额合计", "double", "item"));
            colList.add(getColMeta("amount_cn", "金额合计(大写)", "double", "item"));
            colList.add(getColMeta("discount", "折扣", "double", "item"));
            colList.add(getColMeta("promotion_amount", "优惠金额", "double", "item"));
            colList.add(getColMeta("discount_amount", "应付金额", "double", "item"));
            colList.add(getColMeta("discount_amount_cn", "应付金额(大写)", "string", "item"));
            colList.add(getColMeta("paid_amount", "已付金额", "double", "item"));
            colList.add(getColMeta("remain_amount", "未付金额", "double", "item"));
            colList.add(getColMeta("fund_list", "付款明细", "string", "item"));
            colList.add(getColMeta("total_number", "商品总数", "number", "item"));
            colList.add(getColMeta("goods_amount", "商品金额合计", "double", "item"));
            colList.add(getColMeta("goods_amount_cn", "商品金额合计(大写)", "string", "item"));
            List<TraderFundType> feeList = PurchaseOrder.dao.findFeeConfig();
            if(feeList != null) {
            	for (TraderFundType fund : feeList) {
                	colList.add(getColMeta("fee_amount_"+fund.getId(), fund.getName(), "double", "item"));
    			}
            }
            colList.add(getColMeta("remark", "备注", "string", "item"));
	        
		} else if(orderType == PrintTemplateOrderTypeEnum.purchase_reject_order.getValue()) {
			colList.add(getColMeta("amount", "金额合计", "double", "item"));
            colList.add(getColMeta("amount_cn", "金额合计(大写)", "double", "item"));
            colList.add(getColMeta("discount", "折扣", "double", "item"));
            colList.add(getColMeta("promotion_amount", "优惠金额", "double", "item"));
            colList.add(getColMeta("discount_amount", "应退金额", "double", "item"));
            colList.add(getColMeta("discount_amount_cn", "应退金额(大写)", "string", "item"));
            colList.add(getColMeta("paid_amount", "已退金额", "double", "item"));
            colList.add(getColMeta("remain_amount", "未退金额", "double", "item"));
            colList.add(getColMeta("fund_list", "退款明细", "string", "item"));
            colList.add(getColMeta("total_number", "商品总数", "number", "item"));
            colList.add(getColMeta("goods_amount", "商品金额合计", "double", "item"));
            colList.add(getColMeta("goods_amount_cn", "商品金额合计(大写)", "string", "item"));
            List<TraderFundType> feeList = PurchaseRejectOrder.dao.findFeeConfig();
            if(feeList != null) {
            	for (TraderFundType fund : feeList) {
                	colList.add(getColMeta("fee_amount_"+fund.getId(), fund.getName(), "double", "item"));
    			}
            }
            colList.add(getColMeta("remark", "备注", "string", "item"));
	        
		} else if(orderType == PrintTemplateOrderTypeEnum.inventory_checking.getValue()) {
			colList.add(getColMeta("remark", "备注", "string", "item"));
			
		} else if(orderType == PrintTemplateOrderTypeEnum.trader_receipt_order.getValue()) {
			colList.add(getColMeta("amount", "收款金额", "double", "item"));
			colList.add(getColMeta("amount_cn", "收款合计(大写)", "double", "item"));
			colList.add(getColMeta("order_amount", "核销金额", "double", "item"));
            colList.add(getColMeta("discount_amount", "优惠金额", "double", "item"));
            colList.add(getColMeta("check_amount", "实际核销", "double", "item"));
            colList.add(getColMeta("remain_amount", "剩余金额", "double", "item"));
            colList.add(getColMeta("remark", "备注", "string", "item"));
			
		} else if(orderType == PrintTemplateOrderTypeEnum.trader_pay_order.getValue()) {
			colList.add(getColMeta("amount", "付款金额", "double", "item"));
			colList.add(getColMeta("amount_cn", "付款合计(大写)", "double", "item"));
			colList.add(getColMeta("order_amount", "核销金额", "double", "item"));
            colList.add(getColMeta("discount_amount", "优惠金额", "double", "item"));
            colList.add(getColMeta("check_amount", "实际核销", "double", "item"));
            colList.add(getColMeta("remain_amount", "剩余金额", "double", "item"));
            colList.add(getColMeta("remark", "备注", "string", "item"));
		}
		return colList;
	}
	
	/**
	 * 页脚
	 * @return
	 */
	public List<Kv> getPageBottom() {
		List<Kv> colList = new ArrayList<>();
		Integer orderType = getOrderType();
		if(orderType == PrintTemplateOrderTypeEnum.goods_tag.getValue()) {
			
		} else if(orderType == PrintTemplateOrderTypeEnum.sale_order.getValue()) {
			colList.add(getColMeta("make_man", "制单人", "string", "item"));
	        colList.add(getColMeta("make_man_mobile", "制单人电话", "string", "item"));
	        colList.add(getColMeta("page_number", "页数", "string", "single-item"));
	        colList.add(getColMeta("page_count", "共#页", "string", "single-item"));
	        colList.add(getColMeta("page_current", "第#页", "string", "single-item"));
	        
		} else if(orderType == PrintTemplateOrderTypeEnum.sale_reject_order.getValue()) {
			colList.add(getColMeta("make_man", "制单人", "string", "item"));
	        colList.add(getColMeta("make_man_mobile", "制单人电话", "string", "item"));
	        colList.add(getColMeta("page_number", "页数", "string", "single-item"));
	        colList.add(getColMeta("page_count", "共#页", "string", "single-item"));
	        colList.add(getColMeta("page_current", "第#页", "string", "single-item"));
			
		} else if(orderType == PrintTemplateOrderTypeEnum.purchase_order.getValue()) {
			colList.add(getColMeta("make_man", "制单人", "string", "item"));
	        colList.add(getColMeta("make_man_mobile", "制单人电话", "string", "item"));
	        colList.add(getColMeta("page_number", "页数", "string", "single-item"));
	        colList.add(getColMeta("page_count", "共#页", "string", "single-item"));
	        colList.add(getColMeta("page_current", "第#页", "string", "single-item"));
	        
		} else if(orderType == PrintTemplateOrderTypeEnum.purchase_reject_order.getValue()) {
			colList.add(getColMeta("make_man", "制单人", "string", "item"));
	        colList.add(getColMeta("make_man_mobile", "制单人电话", "string", "item"));
	        colList.add(getColMeta("page_number", "页数", "string", "single-item"));
	        colList.add(getColMeta("page_count", "共#页", "string", "single-item"));
	        colList.add(getColMeta("page_current", "第#页", "string", "single-item"));
	        
		} else if(orderType == PrintTemplateOrderTypeEnum.inventory_checking.getValue()) {
			colList.add(getColMeta("make_man", "制单人", "string", "item"));
	        colList.add(getColMeta("make_man_mobile", "制单人电话", "string", "item"));
	        colList.add(getColMeta("page_number", "页数", "string", "single-item"));
	        colList.add(getColMeta("page_count", "共#页", "string", "single-item"));
	        colList.add(getColMeta("page_current", "第#页", "string", "single-item"));
	        
		} else if(orderType == PrintTemplateOrderTypeEnum.trader_receipt_order.getValue()) {
			colList.add(getColMeta("make_man", "制单人", "string", "item"));
	        colList.add(getColMeta("make_man_mobile", "制单人电话", "string", "item"));
	        colList.add(getColMeta("page_number", "页数", "string", "single-item"));
	        colList.add(getColMeta("page_count", "共#页", "string", "single-item"));
	        colList.add(getColMeta("page_current", "第#页", "string", "single-item"));
	        
		} else if(orderType == PrintTemplateOrderTypeEnum.trader_pay_order.getValue()) {
			colList.add(getColMeta("make_man", "制单人", "string", "item"));
	        colList.add(getColMeta("make_man_mobile", "制单人电话", "string", "item"));
	        colList.add(getColMeta("page_number", "页数", "string", "single-item"));
	        colList.add(getColMeta("page_count", "共#页", "string", "single-item"));
	        colList.add(getColMeta("page_current", "第#页", "string", "single-item"));
	        
		}
		return colList;
	}
	
	 private Kv getColMeta(String id, String name, String dataType, String type) {
	        Kv kv = Kv.create();
	        kv.set("id", id);
	        kv.set("name", name);
	        kv.set("dataType", dataType);
	        kv.set("type", type);
	        return kv;
	    }
}

