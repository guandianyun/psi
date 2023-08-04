package com.bytechainx.psi.common.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.bytechainx.psi.common.CommonConstant;
import com.bytechainx.psi.common.EnumConstant.DiscountTypeEnum;
import com.bytechainx.psi.common.EnumConstant.LogisticsStatusInEnum;
import com.bytechainx.psi.common.EnumConstant.PrintTemplateOrderTypeEnum;
import com.bytechainx.psi.common.EnumConstant.RejectReasonTypeEnum;
import com.bytechainx.psi.common.EnumConstant.TenantConfigEnum;
import com.bytechainx.psi.common.kit.DateUtil;
import com.bytechainx.psi.common.kit.NumberToCN;
import com.bytechainx.psi.common.kit.OrderCodeBuilder;
import com.bytechainx.psi.common.model.base.BaseSaleRejectOrder;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.redis.Redis;

/**
 * 销售退货单
 */
@SuppressWarnings("serial")
public class SaleRejectOrder extends BaseSaleRejectOrder<SaleRejectOrder> {
	
	public static final SaleRejectOrder dao = new SaleRejectOrder().dao();
	
	private List<SaleRejectOrderGoods> orderGoodsList; // 商品明细
	
	public SaleRejectOrder findById(Integer id) {
		return SaleRejectOrder.dao.findFirst("select * from sale_reject_order where id = ? limit 1", id);
	}
	
	public List<SaleRejectOrderFund> getOrderFundList() {
		return SaleRejectOrderFund.dao.find("select * from sale_reject_order_fund where sale_reject_order_id = ?", getId());
	}
	/**
	 * 单据变更日志，最新30条
	 * @return
	 */
	public List<SaleRejectOrderLog> getOrderLogList() {
		return SaleRejectOrderLog.dao.find("select * from sale_reject_order_log where sale_reject_order_id = ? order by id desc limit 30", getId());
	}
	/**
	 * 销售退货单其他费用，要客户支付
	 * @return
	 */
	public List<SaleRejectOrderFee> getOrderFeeList() {
		return SaleRejectOrderFee.dao.find("select * from sale_reject_order_fee where sale_reject_order_id = ?", getId());
	}
	/**
	 * 销售退货单其他成本，不用客户支付
	 * @return
	 */
	public List<SaleRejectOrderCost> getOrderCostList() {
		return SaleRejectOrderCost.dao.find("select * from sale_reject_order_cost where sale_reject_order_id = ?", getId());
	}
	
	/**
	 * 获取单据关联的商品
	 * @return
	 */
	public List<SaleRejectOrderGoods> getOrderGoodsList() {
		if(orderGoodsList != null) {
			return orderGoodsList;
		}
		return SaleRejectOrderGoods.dao.find("select * from sale_reject_order_goods where sale_reject_order_id = ?", getId());
	}
	
	// 单据编号缓存redis key
	private static final String REDIS_ORDER_CODE_LIST_KEY = "order.code.sale.reject.order.tenant.id.";

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
		return "XSTD"+code;
	}

	/**
	 * 预生成单据编号，存入redis队列
	 * 
	 * @param tenantOrgId
	 */
	public void buildOrderCodes() {
		OrderCodeBuilder.build(REDIS_ORDER_CODE_LIST_KEY, "sale_reject_order");
	}

	/**
	 * 单据修改记录
	 * @param oldOrder
	 * @param goodsModifyFlag
	 * @param fundModifyFlag
	 * @return
	 */
	public String getChangeLog(SaleRejectOrder oldOrder, boolean goodsModifyFlag, boolean fundModifyFlag) {
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
		if(getGoodsUnit() != null && getGoodsUnit().compareTo(oldOrder.getGoodsUnit()) != 0) {
			operDesc.append("总件数:");
			operDesc.append("\""+oldOrder.getGoodsUnit()+"\"");
			operDesc.append("修改为->");
			operDesc.append("\""+getGoodsUnit()+"\"");
			operDesc.append(",");
		}
		if(getLogisticsAmount() != null && getLogisticsAmount().compareTo(oldOrder.getLogisticsAmount()) != 0) {
			operDesc.append("运费:");
			operDesc.append("\""+oldOrder.getLogisticsAmount()+"\"");
			operDesc.append("修改为->");
			operDesc.append("\""+getLogisticsAmount()+"\"");
			operDesc.append(",");
		}
		if(getRejectReasonType() != oldOrder.getRejectReasonType()) {
			operDesc.append("退货原因:");
			operDesc.append("\""+RejectReasonTypeEnum.getEnum(oldOrder.getRejectReasonType()).getName()+"\"");
			operDesc.append("修改为->");
			operDesc.append("\""+RejectReasonTypeEnum.getEnum(getRejectReasonType()).getName()+"\"");
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
	 * 审核设置，单据是否需要审核
	 * @return
	 */
	public boolean getAuditConfigFlag() {
		return findAuditConfig();
	}
	
	public boolean findAuditConfig() {
		TenantConfig config = TenantConfig.dao.findByKeyCache(TenantConfigEnum.audit_sale_reject_order);
		return Boolean.parseBoolean(config.getAttrValue());
	}

	public Integer findRowsConfig() {
		TenantConfig config = TenantConfig.dao.findByKeyCache(TenantConfigEnum.rows_sale_reject_order);
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
	
	/**
	 * 其他费用配置
	 * @return
	 */
	public List<TraderFundType> findFeeConfig() {
		return TraderFundType.dao.findFundTypeConfig(TenantConfigEnum.fee_sale_reject_order);
	}
	
	/**
	 * 支出成本配置
	 * @return
	 */
	public List<TraderFundType> findCostConfig() {
		return TraderFundType.dao.findFundTypeConfig(TenantConfigEnum.cost_sale_reject_order);
	}
	
	public void setOrderGoodsList(List<SaleRejectOrderGoods> orderGoodsList) {
		this.orderGoodsList = orderGoodsList;
	}
	
	public String getRejectReasonName() {
		if(getRejectReasonType() != null) {
			return RejectReasonTypeEnum.getEnum(getRejectReasonType()).getName();
		}
		return "";
	}
	
	public String getLogisticsStatusName() {
		if(getLogisticsStatus() != null) {
			return LogisticsStatusInEnum.getEnum(getLogisticsStatus()).getName();
		}
		return "";
	}
	
	
	/**
	 * 打印数据
	 * @return
	 */
	public Kv getPrintData() {
		Kv kv = Kv.create();
		kv.set("order_code", getOrderCode());
        kv.set("order_bar_code", "");//订单号(条码)
        
        StringBuffer orderCodeSB = new StringBuffer();
        String[] orderCodes = StringUtils.split(getSaleOrderId(), ",");
        if(orderCodes != null) {
        	for (int index = 0; index < orderCodes.length; index++) {
            	if(StringUtils.isEmpty(orderCodes[index])) {
            		continue;
            	}
            	SaleOrder order = SaleOrder.dao.findById(Integer.parseInt(orderCodes[index]));
            	if(orderCodeSB.length() > 0) {
            		orderCodeSB.append(order.getOrderCode()+",");
            	}
            	orderCodeSB.append(order.getOrderCode());
    		}
        }
        kv.set("sale_order_code", orderCodeSB.toString());
        
        CustomerInfo customerInfo = getCustomerInfo();
        kv.set("customer_name", customerInfo.getName());
        kv.set("customer_mobile", customerInfo.getMobile());
        kv.set("customer_address", customerInfo.getAddress());
        kv.set("customer_remark", customerInfo.getRemark());
        kv.set("order_time", DateUtil.getDayStr(getOrderTime()));
        kv.set("reject_reason", getRejectReasonName());
        
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
        List<SaleRejectOrderFund> fundList = getOrderFundList();
        StringBuffer fundListString = new StringBuffer();
        for(SaleRejectOrderFund fund : fundList) {
        	fundListString.append(fund.getAccountName()+"退"+fund.getAmount().stripTrailingZeros().toPlainString()+";");
        }
        
        kv.set("fund_list", fundListString);
        kv.set("goods_amount", getGoodsAmount());
        kv.set("goods_amount_cn", NumberToCN.number2CNMontrayUnit(getGoodsAmount()));
        kv.set("remark", getRemark());
        
        List<TraderFundType> feeList = SaleRejectOrder.dao.findFeeConfig();
        if(feeList != null) {
        	for (TraderFundType fund : feeList) {
            	SaleRejectOrderFee orderFee = SaleRejectOrderFee.dao.findByFundType(getId(), fund.getId());
            	if(orderFee != null) {
            		kv.set("fee_amount_"+fund.getId(), orderFee.getAmount().stripTrailingZeros().toPlainString());
            	} else {
            		kv.set("fee_amount_"+fund.getId(), BigDecimal.ZERO.toPlainString());
            	}
    		}
        }
        
        List<Kv> goodsKvs = new ArrayList<>();
        List<SaleRejectOrderGoods> goodsList = getOrderGoodsList();
        BigDecimal goodsCount = BigDecimal.ZERO;
        for (int index = 0; index < goodsList.size(); index++) {
        	SaleRejectOrderGoods orderGoods = goodsList.get(index);
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
		TenantPrintTemplate printTemplate = TenantPrintTemplate.dao.findDefault(PrintTemplateOrderTypeEnum.sale_reject_order.getValue());
        if (printTemplate == null) {
        	printTemplate = TenantPrintTemplate.dao.findDefault( PrintTemplateOrderTypeEnum.sale_reject_order.getValue());
        }
        return printTemplate;
	}

	/**
	 * 打印模板列表
	 * @return
	 */
	public List<TenantPrintTemplate> getPrintTemplateList() {
		return TenantPrintTemplate.dao.find("select * from tenant_print_template where order_type = ?", PrintTemplateOrderTypeEnum.sale_reject_order.getValue());
	}
	
	
}

