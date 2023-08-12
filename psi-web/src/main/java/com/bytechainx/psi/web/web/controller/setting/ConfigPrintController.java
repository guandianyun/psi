package com.bytechainx.psi.web.web.controller.setting;


import java.util.Arrays;
import java.util.Date;

import com.alibaba.fastjson.JSONObject;
import com.bytechainx.psi.common.EnumConstant.TenantConfigEnum;
import com.bytechainx.psi.common.Permissions;
import com.bytechainx.psi.common.annotation.Permission;
import com.bytechainx.psi.common.model.TenantConfig;
import com.bytechainx.psi.common.model.TenantPrintTemplate;
import com.bytechainx.psi.common.service.setting.ConfigPrintService;
import com.bytechainx.psi.web.web.controller.base.BaseController;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.core.Path;
import com.jfinal.kit.Kv;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;


/**
* 打印设置
*/
@Path("/setting/config/print")
public class ConfigPrintController extends BaseController {

	@Inject
	private ConfigPrintService configPrintService;

	/**
	* 首页
	*/
	@Permission(Permissions.setting_config_print)
	public void index() {
		
	}

	/**
	* 列表
	*/
	@Permission(Permissions.setting_config_print)
	public void list() {
		int pageNumber = getInt("pageNumber", 1);
		Kv condKv = Kv.create();
		conditionFilterStore(condKv, null); // 添加门店过滤条件
		Page<TenantPrintTemplate> page = configPrintService.paginate(condKv, pageNumber, maxPageSize);
		
		JSONObject orderPrintConfig = TenantConfig.dao.findJsonByKeyCahce(TenantConfigEnum.create_order_print_confirm);
		
		setAttr("page", page);
		setAttr("orderPrintConfig", orderPrintConfig);
		
	}
	
	/**
	* 查询
	*/
	public void showJson() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderError(404);
			return;
		}
		TenantPrintTemplate printTemplate = TenantPrintTemplate.dao.findById(id);
		if(printTemplate == null) {
			renderError(404);
			return;
		}
		renderJson(Ret.ok().set("data", printTemplate));
	}
	
	/**
	 * 导入打印模板
	 */
	@Permission(Permissions.setting_config_print_tplSetting)
	public void importTplList() {
		int pageNumber = getInt("pageNumber", 1);
		Kv condKv = Kv.create();
		conditionFilterStore(condKv, null); // 添加门店过滤条件
		Page<TenantPrintTemplate> page = configPrintService.paginate(condKv, pageNumber, maxPageSize);
		
		setAttr("page", page);
	}
	
	/**
	* 添加
	*/
	@Permission(Permissions.setting_config_print_tplSetting)
	public void add() {
		Integer orderType = getInt("order_type"); // 单据类型
		
		TenantPrintTemplate printTemplate = TenantPrintTemplate.dao.findDefault(orderType);
        if (printTemplate == null) {
        	printTemplate = TenantPrintTemplate.dao.findDefault( orderType);
        }
        printTemplate.remove("id");
        
        setAttr("printTemplate", printTemplate);
	}

	/**
	* 修改
	*/
	@Permission(Permissions.setting_config_print_tplSetting)
	public void create() {
		TenantPrintTemplate printTemplate = getModel(TenantPrintTemplate.class, "", true);
		Ret ret = configPrintService.create(printTemplate);
		renderJson(ret);
	}

	/**
	* 编辑
	*/
	@Permission(Permissions.setting_config_print_tplSetting)
	public void edit() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderError(404);
			return;
		}
		TenantPrintTemplate printTemplate = TenantPrintTemplate.dao.findById(id);
		if(printTemplate == null) {
			renderError(404);
			return;
		}
		
		setAttr("printTemplate", printTemplate);

	}

	/**
	* 修改
	*/
	@Permission(Permissions.setting_config_print_tplSetting)
	public void update() {
		TenantPrintTemplate printTemplate = getModel(TenantPrintTemplate.class, "", true);
		Ret ret = configPrintService.update(printTemplate);
		renderJson(ret);
	}
	
	
	/**
	* 删除
	*/
	@Permission(Permissions.setting_config_print_tplSetting)
	@Before(Tx.class)
	public void delete() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("ID不能为空"));
			return;
		}
		Ret ret = configPrintService.delete(Arrays.asList(id));

		renderJson(ret);
	}

	/**
	 * 重置模板
	 */
	@Permission(Permissions.setting_config_print_tplSetting)
	public void reset() {
		Integer id = getInt("id");
		if(id != null && id > 0) {
			TenantPrintTemplate printTemplate = TenantPrintTemplate.dao.findById(id);
			
			renderJson(Ret.ok().set("data", printTemplate));
			return;
		}
		
		Integer orderType = getInt("order_type"); // 单据类型
		TenantPrintTemplate printTemplate = TenantPrintTemplate.dao.findDefault(orderType);
        if (printTemplate == null) {
        	printTemplate = TenantPrintTemplate.dao.findDefault( orderType);
        }
        printTemplate.remove("id");
        
        renderJson(Ret.ok().set("data", printTemplate));
	}
	
	/**
	 * 单据打印开关配置
	 */
	@Permission(Permissions.setting_config_print_orderSetting)
	public void orderPrintConfigUpdate() {
		TenantConfig orderPrintConfig = TenantConfig.dao.findByKeyCache(TenantConfigEnum.create_order_print_confirm);
		updateConfig(orderPrintConfig);
		
		renderJson(Ret.ok());
	}
	
	/**
	 * 更新通知配置
	 * @param sencOpen
	 * @param sencConfig
	 */
	@Permission(Permissions.setting_config_print_orderSetting)
	private void updateConfig(TenantConfig config) {
		TenantConfigEnum tenantConfigEnum = TenantConfigEnum.getEnum(config.getAttrKey());
		JSONObject configValue = JSONObject.parseObject(tenantConfigEnum.getValue());
		for (String key : configValue.keySet()) {
			configValue.put(key, get(key)); // 字段名就是KEY
		}
		config.setAttrValue(configValue.toJSONString());
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