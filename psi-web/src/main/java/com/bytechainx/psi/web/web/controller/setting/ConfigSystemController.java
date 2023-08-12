package com.bytechainx.psi.web.web.controller.setting;


import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.bytechainx.psi.common.EnumConstant.FundFlowEnum;
import com.bytechainx.psi.common.EnumConstant.TenantConfigEnum;
import com.bytechainx.psi.common.Permissions;
import com.bytechainx.psi.common.annotation.Permission;
import com.bytechainx.psi.common.kit.StrUtil;
import com.bytechainx.psi.common.model.TenantAdmin;
import com.bytechainx.psi.common.model.TenantConfig;
import com.bytechainx.psi.common.model.TraderFundType;
import com.bytechainx.psi.fund.service.AccountFundTypeService;
import com.bytechainx.psi.web.web.controller.base.BaseController;
import com.jfinal.aop.Inject;
import com.jfinal.core.Path;
import com.jfinal.kit.Kv;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Page;


/**
* 系统设置
*/
@Path("/setting/config/system")
public class ConfigSystemController extends BaseController {

	@Inject
	private AccountFundTypeService accountFundTypeService;
	
	/**
	* 首页
	*/
	@Permission(Permissions.setting_config_system)
	public void index() {

	}

	/**
	* 列表
	*/
	@Permission(Permissions.setting_config_system)
	public void list() {
		Kv tenantConfigKv = Kv.create();
		List<TenantConfig> configList = TenantConfig.dao.find("select * from tenant_config ");
		for (TenantConfig tenantConfig : configList) {
			if(StringUtils.equals(tenantConfig.getAttrKey(), TenantConfigEnum.common_order_code_rule.name())) {
				if(StringUtils.isEmpty(tenantConfig.getAttrValue())) {
					tenantConfig.setAttrValue(TenantConfigEnum.common_order_code_rule.getValue());
				}
				JSONObject json = JSONObject.parseObject(tenantConfig.getAttrValue());
				tenantConfigKv.set("common_order_code_rule_type", json.getInteger("type"));
				tenantConfigKv.set("common_order_code_rule_seq", json.getInteger("seq"));
			} else {
				tenantConfigKv.set(tenantConfig.getAttrKey(), tenantConfig.getAttrValue());
			}
		}
		Kv incomeFundTypeCondKv = Kv.create();
		incomeFundTypeCondKv.set("fund_flow", FundFlowEnum.income.getValue());
		Page<TraderFundType> incomeFundTypePage = accountFundTypeService.paginate(incomeFundTypeCondKv, 1, maxPageSize);
		
		Kv expensesFundTypeCondKv = Kv.create();
		expensesFundTypeCondKv.set("fund_flow", FundFlowEnum.expenses.getValue());
		Page<TraderFundType> expensesFundTypePage = accountFundTypeService.paginate(expensesFundTypeCondKv, 1, maxPageSize);
		
		TenantConfig feeSaleOrderConfig = TenantConfig.dao.findByKeyCache(TenantConfigEnum.fee_sale_order);
		TenantConfig costSaleOrderConfig = TenantConfig.dao.findByKeyCache(TenantConfigEnum.cost_sale_order);
		TenantConfig feeSaleRejectOrderConfig = TenantConfig.dao.findByKeyCache(TenantConfigEnum.fee_sale_reject_order);
		TenantConfig costSaleRejectOrderConfig = TenantConfig.dao.findByKeyCache(TenantConfigEnum.cost_sale_reject_order);
		
		TenantConfig feePurchaseOrderConfig = TenantConfig.dao.findByKeyCache(TenantConfigEnum.fee_purchase_order);
		TenantConfig costPurchaseOrderConfig = TenantConfig.dao.findByKeyCache(TenantConfigEnum.cost_purchase_order);
		TenantConfig feePurchaseRejectOrderConfig = TenantConfig.dao.findByKeyCache(TenantConfigEnum.fee_purchase_reject_order);
		TenantConfig costPurchaseRejectOrderConfig = TenantConfig.dao.findByKeyCache(TenantConfigEnum.cost_purchase_reject_order);
		
		JSONObject oancConfig = TenantConfig.dao.findJsonByKeyCahce(TenantConfigEnum.order_audit_notice_config);
		Integer auditorId = oancConfig.getInteger("purchaseOrder_auditor_id");
		if(auditorId != null && auditorId > 0) {
			TenantAdmin tenantAdmin = TenantAdmin.dao.findById(auditorId);
			oancConfig.put("purchaseOrder_auditor_name", tenantAdmin.getRealName());
		}
		auditorId = oancConfig.getInteger("saleOrder_auditor_id");
		if(auditorId != null && auditorId > 0) {
			TenantAdmin tenantAdmin = TenantAdmin.dao.findById(auditorId);
			oancConfig.put("saleOrder_auditor_name", tenantAdmin.getRealName());
		}
		auditorId = oancConfig.getInteger("fundOrder_auditor_id");
		if(auditorId != null && auditorId > 0) {
			TenantAdmin tenantAdmin = TenantAdmin.dao.findById(auditorId);
			oancConfig.put("fundOrder_auditor_name", tenantAdmin.getRealName());
		}
		
		setAttr("feeSaleOrderFields", StrUtil.beforeAfterAppendComma(feeSaleOrderConfig.getAttrValue()));
		setAttr("costSaleOrderFields", StrUtil.beforeAfterAppendComma(costSaleOrderConfig.getAttrValue()));
		setAttr("feeSaleRejectOrderFields", StrUtil.beforeAfterAppendComma(feeSaleRejectOrderConfig.getAttrValue()));
		setAttr("costSaleRejectOrderFields", StrUtil.beforeAfterAppendComma(costSaleRejectOrderConfig.getAttrValue()));
		
		setAttr("feePurchaseOrderFields", StrUtil.beforeAfterAppendComma(feePurchaseOrderConfig.getAttrValue()));
		setAttr("costPurchaseOrderFields", StrUtil.beforeAfterAppendComma(costPurchaseOrderConfig.getAttrValue()));
		setAttr("feePurchaseRejectOrderFields", StrUtil.beforeAfterAppendComma(feePurchaseRejectOrderConfig.getAttrValue()));
		setAttr("costPurchaseRejectOrderFields", StrUtil.beforeAfterAppendComma(costPurchaseRejectOrderConfig.getAttrValue()));
		
		setAttr("incomeFundTypePage", incomeFundTypePage);
		setAttr("expensesFundTypePage", expensesFundTypePage);
		setAttr("tenantConfig", tenantConfigKv);
		setAttr("oancConfig", oancConfig);
	}

	/**
	* 修改
	*/
	@Permission(Permissions.setting_config_system_update)
	public void update() {
		Integer commonOrderCodeRuleType = getInt("common_order_code_rule_type");
		Integer commonOrderCodeRuleSeq = getInt("common_order_code_rule_seq");
		TenantConfig orderCodeRuleConfig = TenantConfig.dao.findByKeyCache(TenantConfigEnum.common_order_code_rule);
		JSONObject orderCodeRuleJson = new JSONObject();
		orderCodeRuleJson.put("type", commonOrderCodeRuleType);
		orderCodeRuleJson.put("seq", commonOrderCodeRuleSeq);
		orderCodeRuleConfig.setAttrValue(orderCodeRuleJson.toJSONString());
		updateConfig(orderCodeRuleConfig);
		
		String[] feeSaleOrderFields = getParaValues("fee_sale_order_field");
		String[] costSaleOrderFields = getParaValues("cost_sale_order_field");
		TenantConfig feeSaleOrderConfig = TenantConfig.dao.findByKeyCache(TenantConfigEnum.fee_sale_order);
		feeSaleOrderConfig.setAttrValue(StringUtils.join(feeSaleOrderFields, ","));
		updateConfig(feeSaleOrderConfig);
		
		TenantConfig costSaleOrderConfig = TenantConfig.dao.findByKeyCache(TenantConfigEnum.cost_sale_order);
		costSaleOrderConfig.setAttrValue(StringUtils.join(costSaleOrderFields, ","));
		updateConfig(costSaleOrderConfig);
		
		String[] feeSaleRejectOrderFields = getParaValues("fee_sale_reject_order_field");
		String[] costSaleRejectOrderFields = getParaValues("cost_sale_reject_order_field");
		TenantConfig feeSaleRejectOrderConfig = TenantConfig.dao.findByKeyCache(TenantConfigEnum.fee_sale_reject_order);
		feeSaleRejectOrderConfig.setAttrValue(StringUtils.join(feeSaleRejectOrderFields, ","));
		updateConfig(feeSaleRejectOrderConfig);
		
		TenantConfig costSaleRejectOrderConfig = TenantConfig.dao.findByKeyCache(TenantConfigEnum.cost_sale_reject_order);
		costSaleRejectOrderConfig.setAttrValue(StringUtils.join(costSaleRejectOrderFields, ","));
		updateConfig(costSaleRejectOrderConfig);
		
		String[] feePurchaseOrderFields = getParaValues("fee_purchase_order_field");
		String[] costPurchaseOrderFields = getParaValues("cost_purchase_order_field");
		TenantConfig feePurchaseOrderConfig = TenantConfig.dao.findByKeyCache(TenantConfigEnum.fee_purchase_order);
		feePurchaseOrderConfig.setAttrValue(StringUtils.join(feePurchaseOrderFields, ","));
		updateConfig(feePurchaseOrderConfig);
		
		TenantConfig costPurchaseOrderConfig = TenantConfig.dao.findByKeyCache(TenantConfigEnum.cost_purchase_order);
		costPurchaseOrderConfig.setAttrValue(StringUtils.join(costPurchaseOrderFields, ","));
		updateConfig(costPurchaseOrderConfig);
		
		String[] feePurchaseRejectOrderFields = getParaValues("fee_purchase_reject_order_field");
		String[] costPurchaseRejectOrderFields = getParaValues("cost_purchase_reject_order_field");
		TenantConfig feePurchaseRejectOrderConfig = TenantConfig.dao.findByKeyCache(TenantConfigEnum.fee_purchase_reject_order);
		feePurchaseRejectOrderConfig.setAttrValue(StringUtils.join(feePurchaseRejectOrderFields, ","));
		updateConfig(feePurchaseRejectOrderConfig);
		
		TenantConfig costPurchaseRejectOrderConfig = TenantConfig.dao.findByKeyCache(TenantConfigEnum.cost_purchase_reject_order);
		costPurchaseRejectOrderConfig.setAttrValue(StringUtils.join(costPurchaseRejectOrderFields, ","));
		updateConfig(costPurchaseRejectOrderConfig);
		
		TenantConfig oancConfig = TenantConfig.dao.findByKeyCache(TenantConfigEnum.order_audit_notice_config);
		TenantConfigEnum tenantConfigEnum = TenantConfigEnum.getEnum(oancConfig.getAttrKey());
		JSONObject configValue = JSONObject.parseObject(tenantConfigEnum.getValue());
		for (String key : configValue.keySet()) {
			configValue.put(key, get(key)); // 字段名就是KEY
		}
		configValue.put("config_open", true);
		oancConfig.setAttrValue(configValue.toJSONString());
		
		updateConfig(oancConfig);
		
		
		Enumeration<String> paraNames = getParaNames();
		while(paraNames.hasMoreElements()) {
			String paraName = paraNames.nextElement();
			String paraValue = get(paraName);
			TenantConfig config = TenantConfig.dao.findByKeyCache(TenantConfigEnum.getEnum(paraName));
			if(config == null) {
				continue;
			}
			if(StringUtils.isEmpty(paraValue)) {
				paraValue = "false";
			}
			config.setAttrValue(paraValue);
			
			updateConfig(config);
		}
		renderJson(Ret.ok());
	}

	
	/**更新或者创建配置
	 * @param config
	 */
	private void updateConfig(TenantConfig config) {
		if(config.getId() == null || config.getId() <= 0) {
			config.setCreatedAt(new Date());
			config.setUpdatedAt(new Date());
			config.save();
		} else {
			config.setUpdatedAt(new Date());
			config.update();
		}
	}

}