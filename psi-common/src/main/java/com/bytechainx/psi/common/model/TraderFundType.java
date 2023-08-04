package com.bytechainx.psi.common.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.bytechainx.psi.common.CommonConstant;
import com.bytechainx.psi.common.EnumConstant.TenantConfigEnum;
import com.bytechainx.psi.common.model.base.BaseTraderFundType;

/**
 * 收支项目类型
 */
@SuppressWarnings("serial")
public class TraderFundType extends BaseTraderFundType<TraderFundType> {
	
	public static final TraderFundType dao = new TraderFundType().dao();
	
	public TraderFundType findById(Integer id) {
		return TraderFundType.dao.findFirst("select * from trader_fund_type where id = ? limit 1", id);
	}
	
	/**
	 * 单据成本支出和其他费用配置
	 * @return
	 */
	public List<TraderFundType> findFundTypeConfig(TenantConfigEnum tenantConfig) {
		TenantConfig config = TenantConfig.dao.findByKeyCache(tenantConfig);
		String[] fundTypeIds = StringUtils.split(config.getAttrValue(), ",");
		if(fundTypeIds == null || fundTypeIds.length <= 0) {
			return null;
		}
		List<TraderFundType> fundTypeList = new ArrayList<>();
		for(String id : fundTypeIds) {
			TraderFundType fundType = TraderFundType.dao.findFirstByCache(CommonConstant.CACHE_NAME_ONE_MINUTE_STORE, "trader.fund.type.id."+id, "select * from trader_fund_type where id = ?", Integer.parseInt(id));
			if(fundType == null) {
				continue;
			}
			fundTypeList.add(fundType);
		}
		return fundTypeList;
	}
	
	
}

