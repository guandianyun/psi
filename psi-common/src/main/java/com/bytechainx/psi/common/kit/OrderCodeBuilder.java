/**
 * 
 */
package com.bytechainx.psi.common.kit;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.bytechainx.psi.common.EnumConstant.OrderCodeRuleTypeEnum;
import com.bytechainx.psi.common.EnumConstant.TenantConfigEnum;
import com.bytechainx.psi.common.model.TenantConfig;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.redis.Cache;
import com.jfinal.plugin.redis.Redis;

/**
 * 单据编号生成
 * @author defier
 *
 */
public class OrderCodeBuilder {
	
	/**
	 * @param tenantOrgId
	 * @param tenantKey
	 */
	@SuppressWarnings("unchecked")
	public static void build(String tenantKey, String orderTable) {
		Cache redis = Redis.use();
		long nextNumber = 1;
		JSONObject orderCodeConfig = TenantConfig.dao.findJsonByKeyCahce(TenantConfigEnum.common_order_code_rule);
		int ruleType = orderCodeConfig.getIntValue("type");
		int ruleSeq = orderCodeConfig.getIntValue("seq"); // 序号数量
		if(ruleType == 0) {
			ruleType = OrderCodeRuleTypeEnum.yymm.getValue();
			ruleSeq = 4;
		}
		
		String currentDate = DateUtil.getDayNumberStr(new Date());
		if(ruleType == OrderCodeRuleTypeEnum.yy.getValue()) { // 两位年份
			currentDate = currentDate.substring(2,4);
			
		} else if(ruleType == OrderCodeRuleTypeEnum.yymm.getValue()) { // 两位年份+两位月份
			currentDate = currentDate.substring(2,6);
			
		} else if(ruleType == OrderCodeRuleTypeEnum.yymmdd.getValue()) { // 两位年份+四位日期
			currentDate = currentDate.substring(2,8);
		}
		
		long orderCodeCount = redis.llen(tenantKey); // 已生成的数量
		List<String> orderCodeList = redis.lrange(tenantKey, -1, -1);
		if(orderCodeCount > 0) {
			String lastOrderCode = orderCodeList.get(0); // 最后生成的单据编号
			String lastDate = lastOrderCode.substring(0, currentDate.length());
			if(StringUtils.equals(lastDate, currentDate) && currentDate.length() + 3 <= lastOrderCode.length()) { // 同一时段
				nextNumber = Integer.parseInt(lastOrderCode.substring(currentDate.length()))+1;//编号前四位是日期
			} else {
				redis.del(tenantKey);// 不在同一个时段,把序列clear,重新放入
			}
		} else {
			String nowDate = DateUtil.getDayNumberStr(new Date());
			String dateFormat = "%Y%m%d";
			if(ruleType == OrderCodeRuleTypeEnum.yy.getValue()) {
				dateFormat = "%Y";
				nowDate = nowDate.substring(0,4);
			} else if(ruleType == OrderCodeRuleTypeEnum.yymm.getValue()) {
				dateFormat = "%Y%m";
				nowDate = nowDate.substring(0,6);
			} else if(ruleType == OrderCodeRuleTypeEnum.yymmdd.getValue()) {
				dateFormat = "%Y%m%d";
				nowDate = nowDate.substring(0,8);
			} 
			nextNumber = Db.queryLong("select count(id) from "+orderTable+" where DATE_FORMAT(created_at, '"+dateFormat+"') = ?", nowDate);
		}
		if(nextNumber <= 0) {
			nextNumber = 1; // 从1开始起步计算
		}
		while(100 - orderCodeCount > 0) { // 未达到最大生成数量
			String orderCode = currentDate+String.format("%0"+ruleSeq+"d", nextNumber++);
			Record record = Db.findFirst("select * from "+orderTable+" where order_code = ? limit 1", orderCode);
			if(record == null) { // 单号不存在，则保存
				redis.rpush(tenantKey, orderCode);
				orderCodeCount++;
			}
		}
	}

}
