package com.bytechainx.psi.common.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.bytechainx.psi.common.CommonConstant;
import com.bytechainx.psi.common.EnumConstant.DiscountTypeEnum;
import com.bytechainx.psi.common.EnumConstant.LogisticsServiceEnum;
import com.bytechainx.psi.common.EnumConstant.LogisticsStatusEnum;
import com.bytechainx.psi.common.EnumConstant.OrderSourceEnum;
import com.bytechainx.psi.common.EnumConstant.PrintTemplateOrderTypeEnum;
import com.bytechainx.psi.common.EnumConstant.SalePayTypeEnum;
import com.bytechainx.psi.common.EnumConstant.TenantConfigEnum;
import com.bytechainx.psi.common.kit.DateUtil;
import com.bytechainx.psi.common.kit.NumberToCN;
import com.bytechainx.psi.common.kit.OrderCodeBuilder;
import com.bytechainx.psi.common.model.base.BaseSaleOrder;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.redis.Redis;

/**
 * 销售单
 */
@SuppressWarnings("serial")
public class SaleOrder extends BaseSaleOrder<SaleOrder> {
	
	public static final SaleOrder dao = new SaleOrder().dao();
	private List<SaleOrderGoods> orderGoodsList;
	
	public SaleOrder findById(Integer id) {
		return SaleOrder.dao.findFirst("select * from sale_order where id = ? limit 1", id);
	}
	
	public List<SaleOrderFund> getOrderFundList() {
		return SaleOrderFund.dao.find("select * from sale_order_fund where sale_order_id = ?", getId());
	}
	/**
	 * 单据变更日志，最新30条
	 * @return
	 */
	public List<SaleOrderLog> getOrderLogList() {
		return SaleOrderLog.dao.find("select * from sale_order_log where sale_order_id = ? order by id desc limit 30", getId());
	}
	
	/**
	 * 销售单其他费用，要客户支付
	 * @return
	 */
	public List<SaleOrderFee> getOrderFeeList() {
		return SaleOrderFee.dao.find("select * from sale_order_fee where sale_order_id = ?", getId());
	}
	/**
	 * 销售单其他成本，不用客户支付
	 * @return
	 */
	public List<SaleOrderCost> getOrderCostList() {
		return SaleOrderCost.dao.find("select * from sale_order_cost where sale_order_id = ?", getId());
	}
	
	/**
	 * 进货单开支项目对应的其他费用
	 * @param fundTypeId
	 * @return
	 */
	public BigDecimal getOrderFee(Integer fundTypeId) {
		SaleOrderFee fee = SaleOrderFee.dao.findFirst("select * from sale_order_fee where sale_order_id = ? and trader_fund_type = ? limit 1", getId(), fundTypeId);
		if(fee == null) {
			return BigDecimal.ZERO;
		}
		return fee.getAmount();
	}
	/**
	 * 进货单开支项目对应的成本支出
	 * @param fundTypeId
	 * @return
	 */
	public SaleOrderCost getOrderCost(Integer fundTypeId) {
		return SaleOrderCost.dao.findFirst("select * from sale_order_cost where sale_order_id = ? and trader_fund_type = ? limit 1", getId(), fundTypeId);
	}
	
	
	/**
	 * 获取单据关联的商品
	 * @return
	 */
	public List<SaleOrderGoods> getOrderGoodsList() {
		if(orderGoodsList != null) {
			return orderGoodsList;
		}
		return SaleOrderGoods.dao.find("select * from sale_order_goods where sale_order_id = ?", getId());
	}
	/**
	 * 商品名称列表
	 * @return
	 */
	public String getOrderGoodsStr() {
		StringBuffer sb = new StringBuffer();
		List<SaleOrderGoods> goodsList = getOrderGoodsList();
		for (SaleOrderGoods saleOrderGoods : goodsList) {
			sb.append(saleOrderGoods.getGoodsInfo().getName()+"."+saleOrderGoods.getBuyNumber()+"/");
		}
		return sb.substring(0, sb.length()-1).toString();
	}
	
	// 单据编号缓存redis key
	private static final String REDIS_ORDER_CODE_LIST_KEY = "order.code.sale.order.tenant.id.";

	/**
	 * 生成单据编号，从redis队列中获取
	 * 
	 * @return
	 */
	public String generateOrderCode() {
		String code = Redis.use().lpop(REDIS_ORDER_CODE_LIST_KEY );
		if(code == null) {
			return null;
		}
		return "XSD"+code;
	}

	/**
	 * 预生成单据编号，存入redis队列
	 * 
	 * @param tenantOrgId
	 */
	public void buildOrderCodes() {
		OrderCodeBuilder.build(REDIS_ORDER_CODE_LIST_KEY, "sale_order");
	}
	/**
	 * 客户
	 * @return
	 */
	public CustomerInfo getCustomerInfo() {
		return CustomerInfo.dao.findFirstByCache(CommonConstant.CACHE_NAME_ONE_MINUTE_STORE, "customer.info.id."+getCustomerInfoId(), "select * from customer_info where id = ?", getCustomerInfoId());
	}
	/**
	 * 业务员
	 * @return
	 */
	public TenantAdmin getHandler() {
		return TenantAdmin.dao.findFirstByCache(CommonConstant.CACHE_NAME_ONE_MINUTE_STORE, "tenant.admin.id."+getHandlerId(), "select * from tenant_admin where id = ?", getHandlerId());
	}
	/**
	 * 制单人
	 * @return
	 */
	public TenantAdmin getMakeMan() {
		return TenantAdmin.dao.findFirstByCache(CommonConstant.CACHE_NAME_ONE_MINUTE_STORE, "tenant.admin.id."+getMakeManId(), "select * from tenant_admin where id = ?", getMakeManId());
	}
	/**
	 * 审核人
	 * @return
	 */
	public TenantAdmin getAuditor() {
		return TenantAdmin.dao.findFirstByCache(CommonConstant.CACHE_NAME_ONE_MINUTE_STORE, "tenant.admin.id."+getAuditorId(), "select * from tenant_admin where id = ?", getAuditorId());
	}
	/**
	 * 单据修改记录
	 * @param oldOrder
	 * @param goodsModifyFlag
	 * @param fundModifyFlag
	 * @return
	 */
	public String getChangeLog(SaleOrder oldOrder, boolean goodsModifyFlag, boolean fundModifyFlag) {
		StringBuffer operDesc = new StringBuffer();
		if(getHandlerId().intValue() != oldOrder.getHandlerId().intValue()) {
			operDesc.append("业务员:");
			operDesc.append("\""+oldOrder.getHandler().getRealName()+"\"");
			operDesc.append("修改为->");
			operDesc.append("\""+getHandler().getRealName()+"\"");
			operDesc.append(",");
		}
		if(getCustomerInfoId().intValue() != oldOrder.getCustomerInfoId().intValue()) {
			operDesc.append("客户:");
			operDesc.append("\""+oldOrder.getCustomerInfo().getName()+"\"");
			operDesc.append("修改为->");
			operDesc.append("\""+getCustomerInfo().getName()+"\"");
			operDesc.append(",");
		}
		if(getDiscountType() != oldOrder.getDiscountType()) {
			operDesc.append("优惠类型:");
			operDesc.append("\""+DiscountTypeEnum.getEnum(oldOrder.getDiscountType()).getName()+"\"");
			operDesc.append("修改为->");
			operDesc.append("\""+DiscountTypeEnum.getEnum(getDiscountType()).getName()+"\"");
			operDesc.append(",");
		}
		if(getDiscount().compareTo(oldOrder.getDiscount()) != 0) {
			operDesc.append("折扣:");
			operDesc.append("\""+getDiscount()+"\"");
			operDesc.append("修改为->");
			operDesc.append("\""+oldOrder.getDiscount()+"\"");
			operDesc.append(",");
		}
		if(getOddAmount().compareTo(oldOrder.getOddAmount()) != 0) {
			operDesc.append("抹零金额:");
			operDesc.append("\""+oldOrder.getOddAmount()+"\"");
			operDesc.append("修改为->");
			operDesc.append("\""+getOddAmount()+"\"");
			operDesc.append(",");
		}
		BigDecimal otherAmount =  getOtherAmount() == null ? BigDecimal.ZERO : getOtherAmount();
		BigDecimal oldOtherAmount =  oldOrder.getOtherAmount() == null ? BigDecimal.ZERO : oldOrder.getOtherAmount();
		if(otherAmount.compareTo(oldOtherAmount) != 0) {
			operDesc.append("其他费用:");
			operDesc.append("\""+oldOtherAmount+"\"");
			operDesc.append("修改为->");
			operDesc.append("\""+otherAmount+"\"");
			operDesc.append(",");
		}
		
		BigDecimal otherCostAmount =  getOtherCostAmount() == null ? BigDecimal.ZERO : getOtherCostAmount();
		BigDecimal oldOtherCostAmount =  oldOrder.getOtherCostAmount() == null ? BigDecimal.ZERO : oldOrder.getOtherCostAmount();
		if(otherAmount.compareTo(oldOtherCostAmount) != 0) {
			operDesc.append("成本支出:");
			operDesc.append("\""+oldOtherCostAmount+"\"");
			operDesc.append("修改为->");
			operDesc.append("\""+otherCostAmount+"\"");
			operDesc.append(",");
		}
		if(getPayType() != null && getPayType() != oldOrder.getPayType()) {
			operDesc.append("付款类型:");
			operDesc.append("\""+SalePayTypeEnum.getEnum(oldOrder.getPayType()).getName()+"\"");
			operDesc.append("修改为->");
			operDesc.append("\""+SalePayTypeEnum.getEnum(getPayType()).getName()+"\"");
			operDesc.append(",");
		}
		if(getLogisticsService() != null && getLogisticsService() != oldOrder.getLogisticsService()) {
			operDesc.append("物流配送:");
			operDesc.append("\""+LogisticsServiceEnum.getEnum(oldOrder.getLogisticsService()).getName()+"\"");
			operDesc.append("修改为->");
			operDesc.append("\""+LogisticsServiceEnum.getEnum(getLogisticsService()).getName());
			operDesc.append(",");
		}
		if(StringUtils.equals(getDeliveryDistance(), oldOrder.getDeliveryDistance())) {
			operDesc.append("配送距离:");
			operDesc.append("\""+oldOrder.getDeliveryDistance()+"\"");
			operDesc.append("修改为->");
			operDesc.append(getDeliveryDistance()+"\"");
			operDesc.append(",");
		}
		if(getDeliveryFee() != null && getDeliveryFee().compareTo(oldOrder.getDeliveryFee()) != 0) {
			operDesc.append("送货费:");
			operDesc.append("\""+oldOrder.getDeliveryFee()+"\"");
			operDesc.append("修改为->");
			operDesc.append("\""+getDeliveryFee()+"\"");
			operDesc.append(",");
		}
		if(getGoodsUnit() != null && getGoodsUnit().compareTo(oldOrder.getGoodsUnit()) != 0) {
			operDesc.append("总件数:");
			operDesc.append("\""+oldOrder.getGoodsUnit()+"\"");
			operDesc.append("修改为->");
			operDesc.append("\""+getGoodsUnit()+"\"");
			operDesc.append(",");
		}
		if(!StringUtils.equals(getOrderImg(), oldOrder.getOrderImg())) {
			operDesc.append("单据图片发生变更");
			operDesc.append(",");
		}
		if(goodsModifyFlag) {
			operDesc.append("单据货品发生变更");
			operDesc.append(",");
		}
		if(fundModifyFlag) {
			operDesc.append("收付款发生变更");
			operDesc.append(",");
		}
		if(operDesc.length() <= 0) {
			return "数据无变化";
		}
		return StringUtils.substring(operDesc.toString(), 0, operDesc.length()-1);
	}
	
	public List<String> getOrderImgList() {
		return Arrays.asList(StringUtils.split(getOrderImg() == null ? "" : getOrderImg(), ","));
	}
	
	
	/**
	 * 其他费用配置
	 * @return
	 */
	public List<TraderFundType> findFeeConfig() {
		return TraderFundType.dao.findFundTypeConfig(TenantConfigEnum.fee_sale_order);
	}
	
	/**
	 * 支出成本配置
	 * @return
	 */
	public List<TraderFundType> findCostConfig() {
		return TraderFundType.dao.findFundTypeConfig(TenantConfigEnum.cost_sale_order);
	}
	
	/**
	 * 审核设置，单据是否需要审核
	 * @return
	 */
	public boolean getAuditConfigFlag() {
		return findAuditConfig();
	}
	
	public boolean findAuditConfig() {
		TenantConfig config = TenantConfig.dao.findByKeyCache(TenantConfigEnum.audit_sale_order);
		return Boolean.parseBoolean(config.getAttrValue());
	}

	public Integer findRowsConfig() {
		TenantConfig config = TenantConfig.dao.findByKeyCache(TenantConfigEnum.rows_sale_order);
		return Integer.parseInt(config.getAttrValue());
	}
	
	/**
	 * 审核人
	 * @return
	 */
	public Integer findAuditorIdConfig() {
		TenantConfig config = TenantConfig.dao.findByKeyCache(TenantConfigEnum.order_audit_notice_config);
		JSONObject json = JSONObject.parseObject(config.getAttrValue());
		return json.getInteger("saleOrder_auditor_id");
	}
	
	/**
	 * 审核短信通知
	 * @return
	 */
	public Boolean findAuditSmsNoticeConfig() {
		TenantConfig config = TenantConfig.dao.findByKeyCache(TenantConfigEnum.order_audit_notice_config);
		JSONObject json = JSONObject.parseObject(config.getAttrValue());
		return json.getBoolean("saleOrder_audit_sms_notice");
	}
	/**
	 * 审核系统通知
	 * @return
	 */
	public Boolean findAuditSysNoticeConfig() {
		TenantConfig config = TenantConfig.dao.findByKeyCache(TenantConfigEnum.order_audit_notice_config);
		JSONObject json = JSONObject.parseObject(config.getAttrValue());
		return json.getBoolean("saleOrder_audit_system_notice");
	}
	
	/**
	 * 审核后通知给业务员：短信
	 * @return
	 */
	public Boolean findAuditHandlerSmsNoticeConfig() {
		TenantConfig config = TenantConfig.dao.findByKeyCache(TenantConfigEnum.order_audit_notice_config);
		JSONObject json = JSONObject.parseObject(config.getAttrValue());
		return json.getBoolean("audit_handler_sms_notice");
	}

	/**
	 * 审核后通知给业务员：系统通知
	 * @return
	 */
	public Boolean findAuditHandlerSysNoticeConfig() {
		TenantConfig config = TenantConfig.dao.findByKeyCache(TenantConfigEnum.order_audit_notice_config);
		JSONObject json = JSONObject.parseObject(config.getAttrValue());
		return json.getBoolean("audit_handler_system_notice");
	}
	/**
	 * 审核后通知给业务员：短信
	 * @return
	 */
	public Boolean findAuditMakeManSmsNoticeConfig() {
		TenantConfig config = TenantConfig.dao.findByKeyCache(TenantConfigEnum.order_audit_notice_config);
		JSONObject json = JSONObject.parseObject(config.getAttrValue());
		return json.getBoolean("audit_makeMan_sms_notice");
	}
	/**
	 * 审核后通知给业务员：系统通知
	 * @return
	 */
	public Boolean findAuditMakeManSysNoticeConfig() {
		TenantConfig config = TenantConfig.dao.findByKeyCache(TenantConfigEnum.order_audit_notice_config);
		JSONObject json = JSONObject.parseObject(config.getAttrValue());
		return json.getBoolean("audit_makeMan_system_notice");
	}
	
	public void setOrderGoodsList(List<SaleOrderGoods> orderGoodsList) {
		this.orderGoodsList = orderGoodsList;
	}
	
	public String getLogisticsStatusName() {
		return LogisticsStatusEnum.getEnum(getLogisticsStatus()).getName();
	}
	
	/**
	 * 获取销售单对应的收款单关联
	 * @return
	 */
	public List<TraderReceiptOrderRef> getReceiptOrderRefList() {
		return TraderReceiptOrderRef.dao.find("select * from trader_receipt_order_ref where sale_order_id = ?", getId());
	}
	
	public String getPayTypeName() {
		if(getPayType() == null) {
			return null;
		}
		return SalePayTypeEnum.getEnum(getPayType()).getName();
	}
	
	public String getOrderSourceName() {
		return OrderSourceEnum.getEnum(getOrderSource()).getName();
	}
	
	/**
	 * 打印数据
	 * @return
	 */
	public Kv getPrintData() {
		Kv kv = Kv.create();
		kv.set("order_code", getOrderCode());
        kv.set("order_bar_code", "");//订单号(条码)
        
        CustomerInfo customerInfo = getCustomerInfo();
        kv.set("customer_name", customerInfo.getName());
        kv.set("customer_mobile", customerInfo.getMobile());
        kv.set("customer_address", customerInfo.getAddress());
        kv.set("customer_remark", customerInfo.getRemark());
        kv.set("order_time", DateUtil.getDayStr(getOrderTime()));
        TenantOrg tenantOrg = TenantOrg.dao.findCacheById();
        kv.set("company_full_name", tenantOrg.getFullName());
        kv.set("company_name", tenantOrg.getName());
        kv.set("company_mobile", tenantOrg.getMobile());
        TenantAdmin admin = TenantAdmin.dao.findById(getHandlerId());
        kv.set("handler_name", admin.getRealName());
        kv.set("handler_mobile", admin.getMobile());
        kv.set("amount", getAmount());
        kv.set("amount_cn", NumberToCN.number2CNMontrayUnit(getAmount()));
        kv.set("discount", getDiscount().stripTrailingZeros().toPlainString()+"%");
        kv.set("promotion_amount", getGoodsAmount().subtract(getDiscountAmount()).add(getOddAmount()));
        kv.set("discount_amount", getDiscountAmount());
        kv.set("discount_amount_cn", NumberToCN.number2CNMontrayUnit(getDiscountAmount()));
        kv.set("paid_amount", getPaidAmount());
        kv.set("remain_amount", getAmount().subtract(getPaidAmount()));
        List<SaleOrderFund> fundList = getOrderFundList();
        StringBuffer fundListString = new StringBuffer();
        for(SaleOrderFund fund : fundList) {
        	fundListString.append(fund.getAccountName()+"收"+fund.getAmount().stripTrailingZeros().toPlainString()+";");
        }
        kv.set("fund_list", fundListString);
        kv.set("goods_amount", getGoodsAmount());
        kv.set("goods_amount_cn", NumberToCN.number2CNMontrayUnit(getGoodsAmount()));
        kv.set("remark", getRemark());
        
        List<TraderFundType> feeList = SaleOrder.dao.findFeeConfig();
        if(feeList != null) {
        	for (TraderFundType fund : feeList) {
            	SaleOrderFee orderFee = SaleOrderFee.dao.findByFundType(getId(), fund.getId());
            	if(orderFee != null) {
            		kv.set("fee_amount_"+fund.getId(), orderFee.getAmount().stripTrailingZeros().toPlainString());
            	} else {
            		kv.set("fee_amount_"+fund.getId(), BigDecimal.ZERO.toPlainString());
            	}
    		}
        }
        
        kv.set("express_order", getExpressOrder());
        kv.set("express_name", getExpressName());
        
        
        List<Kv> goodsKvs = new ArrayList<>();
        List<SaleOrderGoods> goodsList = getOrderGoodsList();
        BigDecimal goodsCount = BigDecimal.ZERO;
        for (int index = 0; index < goodsList.size(); index++) {
        	SaleOrderGoods orderGoods = goodsList.get(index);
        	Kv goodsKv = Kv.create();
        	goodsKv.set("index", index+1);
        	GoodsInfo goodsInfo = orderGoods.getGoodsInfo();
        	goodsKv.set("goods_name", goodsInfo.getName());
        	goodsKv.set("goods_spec", orderGoods.getGoodsSpecNames());
            goodsKv.set("goods_bar_code", goodsInfo.getBarCode());//条码
            
            goodsKv.set("goods_unit_name", orderGoods.getGoodsUnit().getName());
            goodsKv.set("goods_buy_number", orderGoods.getBuyNumber().stripTrailingZeros().toPlainString());
            goodsKv.set("goods_price", orderGoods.getPrice());
            goodsKv.set("goods_discount", orderGoods.getDiscount().stripTrailingZeros().toPlainString()+"%");
            goodsKv.set("goods_discount_amount", orderGoods.getDiscountAmount());//折扣后的单价
            goodsKv.set("goods_discount_total", orderGoods.getBuyNumber().multiply(orderGoods.getPrice()).subtract(orderGoods.getAmount()));
            goodsKv.set("goods_total_amount", orderGoods.getAmount());
            goodsKv.set("goods_remark", orderGoods.getRemark());
            
            List<GoodsAttributeRef> goodsAttributeRefList = goodsInfo.getGoodsAttributeRefList();
            for (GoodsAttributeRef goodsAttributeRef : goodsAttributeRefList) {
            	goodsKv.set("goods_attribute_"+goodsAttributeRef.getGoodsAttributeId(), goodsAttributeRef.getAttrValue());
			}
            
            goodsKvs.add(goodsKv);
            
            goodsCount = goodsCount.add(orderGoods.getBuyNumber());
		}
        
        kv.set("total_number", goodsCount.stripTrailingZeros().toPlainString());
        TenantAdmin makeMan = getMakeMan();
        kv.set("make_man", makeMan.getRealName());
        kv.set("make_man_mobile", makeMan.getMobile());
        
        kv.set("list", goodsKvs);
        return kv;
	}
	
	/**
	 * 获取默认打印模板
	 * @return
	 */
	public TenantPrintTemplate getPrintDefaultTemplate() {
		TenantPrintTemplate printTemplate = TenantPrintTemplate.dao.findDefault(PrintTemplateOrderTypeEnum.sale_order.getValue());
        if (printTemplate == null) {
        	printTemplate = TenantPrintTemplate.dao.findDefault( PrintTemplateOrderTypeEnum.sale_order.getValue());
        }
        return printTemplate;
	}

	/**
	 * 打印模板列表
	 * @return
	 */
	public List<TenantPrintTemplate> getPrintTemplateList() {
		return TenantPrintTemplate.dao.find("select * from tenant_print_template where order_type = ?", PrintTemplateOrderTypeEnum.sale_order.getValue());
	}
	
}

