package com.bytechainx.psi.common.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.bytechainx.psi.common.CommonConstant;
import com.bytechainx.psi.common.EnumConstant.PrintTemplateOrderTypeEnum;
import com.bytechainx.psi.common.EnumConstant.TenantConfigEnum;
import com.bytechainx.psi.common.kit.DateUtil;
import com.bytechainx.psi.common.kit.NumberToCN;
import com.bytechainx.psi.common.kit.OrderCodeBuilder;
import com.bytechainx.psi.common.model.base.BaseTraderPayOrder;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.redis.Redis;

/**
 * 付款单
 */
@SuppressWarnings("serial")
public class TraderPayOrder extends BaseTraderPayOrder<TraderPayOrder> {
	
	public static final TraderPayOrder dao = new TraderPayOrder().dao();
	
	public TraderPayOrder findById(Integer id) {
		return TraderPayOrder.dao.findFirst("select * from trader_pay_order where id = ? limit 1", id);
	}
	
	public List<TraderPayOrderFund> getOrderFundList() {
		return TraderPayOrderFund.dao.find("select * from trader_pay_order_fund where trader_pay_order_id = ?", getId());
	}
	
	public List<TraderPayOrderRef> getOrderRefList() {
		return TraderPayOrderRef.dao.find("select * from trader_pay_order_ref where trader_pay_order_id = ?", getId());
	}
	
	public TraderPayOrderRef getOrderRef(Integer purchaseOrderId) {
		return TraderPayOrderRef.dao.findFirst("select * from trader_pay_order_ref where trader_pay_order_id = ? and purchase_order_id = ? limit 1", getId(), purchaseOrderId);
	}
	
	// 单据编号缓存redis key
	private static final String REDIS_ORDER_CODE_LIST_KEY = "order.code.trader.pay.order.tenant.id.";

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
		return "FKD"+code;
	}

	/**
	 * 预生成单据编号，存入redis队列
	 * 
	 * @param tenantOrgId
	 */
	public void buildOrderCodes() {
		OrderCodeBuilder.build(REDIS_ORDER_CODE_LIST_KEY, "trader_pay_order");
	}
	
	public List<String> getOrderImgList() {
		return Arrays.asList(StringUtils.split(getOrderImg() == null ? "" : getOrderImg(), ","));
	}
	
	/**
	 * 供应商
	 * @return
	 */
	public SupplierInfo getSupplierInfo() {
		return SupplierInfo.dao.findFirstByCache(CommonConstant.CACHE_NAME_ONE_MINUTE_STORE, "supplier.info.id."+getSupplierInfoId(), "select * from supplier_info where id = ?", getSupplierInfoId());
	}
	/**
	 * 经办人
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
	 * 审核人
	 * @return
	 */
	public Integer findAuditorIdConfig() {
		TenantConfig config = TenantConfig.dao.findByKeyCache(TenantConfigEnum.order_audit_notice_config);
		JSONObject json = JSONObject.parseObject(config.getAttrValue());
		return json.getInteger("fundOrder_auditor_id");
	}
	
	/**
	 * 审核短信通知
	 * @return
	 */
	public Boolean findAuditSmsNoticeConfig() {
		TenantConfig config = TenantConfig.dao.findByKeyCache(TenantConfigEnum.order_audit_notice_config);
		JSONObject json = JSONObject.parseObject(config.getAttrValue());
		return json.getBoolean("fundOrder_audit_sms_notice");
	}
	/**
	 * 审核系统通知
	 * @return
	 */
	public Boolean findAuditSysNoticeConfig() {
		TenantConfig config = TenantConfig.dao.findByKeyCache(TenantConfigEnum.order_audit_notice_config);
		JSONObject json = JSONObject.parseObject(config.getAttrValue());
		return json.getBoolean("fundOrder_audit_system_notice");
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
	 * 审核设置，单据是否需要审核
	 * @return
	 */
	public boolean getAuditConfigFlag() {
		return findAuditConfig();
	}
	
	public boolean findAuditConfig() {
		TenantConfig config = TenantConfig.dao.findByKeyCache(TenantConfigEnum.audit_trader_pay_order);
		return Boolean.parseBoolean(config.getAttrValue());
	}
	
	/**
	 * 打印数据
	 * @return
	 */
	public Kv getPrintData() {
		Kv kv = Kv.create();
		kv.set("order_code", getOrderCode());
        kv.set("order_bar_code", "");//订单号(条码)
        SupplierInfo supplierInfo = getSupplierInfo();
        kv.set("supplier_name", supplierInfo.getName());
        kv.set("supplier_mobile", supplierInfo.getMobile());
        kv.set("supplier_address", supplierInfo.getAddress());
        kv.set("supplier_remark", supplierInfo.getRemark());
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
        kv.set("discount_amount", getDiscountAmount());
        kv.set("order_amount", getOrderAmount());
        kv.set("check_amount", getCheckAmount());
        kv.set("remain_amount", getAmount().subtract(getCheckAmount()));
        
        kv.set("remark", getRemark());
        
        List<Kv> fundKvs = new ArrayList<>();
        List<TraderPayOrderFund> fundList = getOrderFundList();
        BigDecimal goodsCount = BigDecimal.ZERO;
        for (int index = 0; index < fundList.size(); index++) {
        	TraderPayOrderFund fund = fundList.get(index);
        	Kv fundKv = Kv.create();
        	fundKv.set("index", index+1);
            fundKv.set("balance_account_name", fund.getBalanceAccount().getName());
            fundKv.set("pay_amount", fund.getAmount().stripTrailingZeros().toPlainString());
            fundKv.set("fund_time", DateUtil.getDayStr(fund.getFundTime()));
            
            fundKvs.add(fundKv);
		}
        
        kv.set("total_number", goodsCount.stripTrailingZeros().toPlainString());
        TenantAdmin makeMan = getMakeMan();
        kv.set("make_man", makeMan.getRealName());
        kv.set("make_man_mobile", makeMan.getMobile());
        
        kv.set("list", fundKvs);
        return kv;
	}
	
	/**
	 * 获取默认打印模板
	 * @return
	 */
	public TenantPrintTemplate getPrintDefaultTemplate() {
		TenantPrintTemplate printTemplate = TenantPrintTemplate.dao.findDefault(PrintTemplateOrderTypeEnum.trader_pay_order.getValue());
        if (printTemplate == null) {
        	printTemplate = TenantPrintTemplate.dao.findDefault( PrintTemplateOrderTypeEnum.trader_pay_order.getValue());
        }
        return printTemplate;
	}

	/**
	 * 打印模板列表
	 * @return
	 */
	public List<TenantPrintTemplate> getPrintTemplateList() {
		return TenantPrintTemplate.dao.find("select * from tenant_print_template where order_type = ?", PrintTemplateOrderTypeEnum.trader_pay_order.getValue());
	}
}

