package com.bytechainx.psi.common.model;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.bytechainx.psi.common.CommonConstant;
import com.bytechainx.psi.common.EnumConstant.TenantConfigEnum;
import com.bytechainx.psi.common.model.base.BaseTenantConfig;

/**
 * 租户配置
 */
@SuppressWarnings("serial")
public class TenantConfig extends BaseTenantConfig<TenantConfig> {
	
	public static final TenantConfig dao = new TenantConfig().dao();
	
	public TenantConfig findById(Integer id) {
		return TenantConfig.dao.findFirst("select * from tenant_config where id = ? limit 1", id);
	}

	/**
	 * 查询配置
	 * @param tenantOrgId
	 * @param tenantConfigEnum
	 * @return
	 */
	public TenantConfig findByKeyCache(TenantConfigEnum tenantConfigEnum) {
		if(tenantConfigEnum == null) {
			return null;
		}
		TenantConfig config = TenantConfig.dao.findFirstByCache(CommonConstant.CACHE_NAME_ONE_MINUTE_STORE, "tenant.config.key."+tenantConfigEnum.name(), "select * from tenant_config where attr_key = ? limit 1", tenantConfigEnum.name());
		if(config == null) { // 数据库中没有，则获取默认值
			config = new TenantConfig();
			config.setAttrKey(tenantConfigEnum.name());
			config.setAttrValue(tenantConfigEnum.getValue());
		} else if(StringUtils.isEmpty(config.getAttrValue())) {
			config.setAttrValue(tenantConfigEnum.getValue());
		}
		return config;
	}
	
	/**
	 * 查询配置，返回JSON
	 * @param tenantOrgId
	 * @param tenantConfigEnum
	 * @return
	 */
	public JSONObject findJsonByKeyCahce(TenantConfigEnum tenantConfigEnum) {
		TenantConfig config = findByKeyCache(tenantConfigEnum);
		return JSONObject.parseObject(config.getAttrValue());
	}
	
	/**
	 * 查询配置
	 * @param tenantOrgId
	 * @param tenantConfigEnum
	 * @return
	 */
	public TenantConfig findByKey(TenantConfigEnum tenantConfigEnum) {
		if(tenantConfigEnum == null) {
			return null;
		}
		TenantConfig config = TenantConfig.dao.findFirst("select * from tenant_config where attr_key = ? limit 1", tenantConfigEnum.name());
		if(config == null) { // 数据库中没有，则获取默认值
			config = new TenantConfig();
			config.setAttrKey(tenantConfigEnum.name());
			config.setAttrValue(tenantConfigEnum.getValue());
		}
		return config;
	}
	
	/**
	 * 查询配置，返回JSON
	 * @param tenantOrgId
	 * @param tenantConfigEnum
	 * @return
	 */
	public JSONObject findJsonByKey(TenantConfigEnum tenantConfigEnum) {
		TenantConfig config = findByKey(tenantConfigEnum);
		return JSONObject.parseObject(config.getAttrValue());
	}
	
}

