package com.bytechainx.psi.common.service.setting;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.bytechainx.psi.common.EnumConstant.FlagEnum;
import com.bytechainx.psi.common.model.TenantPrintTemplate;
import com.bytechainx.psi.common.service.base.CommonService;
import com.jfinal.kit.Kv;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Page;


/**
* 打印设置
*/
public class ConfigPrintService extends CommonService {

	/**
	* 分页列表
	*/
	public Page<TenantPrintTemplate> paginate(Kv condKv, Integer pageNumber, int pageSize) {
		StringBuffer where = new StringBuffer();
		List<Object> params = new ArrayList<Object>();
		where.append("where 1 = 1 ");
		conditionFilter(condKv, where, params);
		return TenantPrintTemplate.dao.paginate(pageNumber, pageSize, "select * ", "from tenant_print_template "+where.toString()+" order by id ", params.toArray());
	}


	/**
	* 修改
	*/
	public Ret create(TenantPrintTemplate printTemplate) {
		if(printTemplate == null) {
			return Ret.fail("参数错误");
		}
		if(StringUtils.isEmpty(printTemplate.getName())) {
			return Ret.fail("模板名称不能为空");
		}
		if(StringUtils.isEmpty(printTemplate.getContent())) {
			return Ret.fail("模板内容不能为空");
		}
		if(printTemplate.getOrderType() == null) {
			return Ret.fail("单据类型不能为空");
		}
		TenantPrintTemplate _printTemplate = TenantPrintTemplate.dao.findFirst("select * from tenant_print_template where order_type = ? and name = ? limit 1", printTemplate.getOrderType(), printTemplate.getName());
		if(_printTemplate != null) {
			return Ret.fail("打印模板已存在");
		}
		if(printTemplate.getDefaultFlag()) { // 如果是默认模板，则之前的默认要去除
			TenantPrintTemplate defaultTemplate = TenantPrintTemplate.dao.findDefault(printTemplate.getOrderType());
			if(defaultTemplate != null) {
				defaultTemplate.setDefaultFlag(FlagEnum.NO.getValue());
				defaultTemplate.update();
			}
		}
		printTemplate.setUpdatedAt(new Date());
		printTemplate.setCreatedAt(new Date());
		printTemplate.save();
		
		return Ret.ok();
	}
	
	/**
	* 修改
	*/
	public Ret update(TenantPrintTemplate printTemplate) {
		if(printTemplate == null || printTemplate.getId() == null || printTemplate.getId() <= 0) {
			return Ret.fail("参数错误");
		}
		if(StringUtils.isEmpty(printTemplate.getName())) {
			return Ret.fail("模板名称不能为空");
		}
		if(StringUtils.isEmpty(printTemplate.getContent())) {
			return Ret.fail("模板内容不能为空");
		}
		if(printTemplate.getOrderType() == null) {
			return Ret.fail("单据类型不能为空");
		}
		TenantPrintTemplate _printTemplate = TenantPrintTemplate.dao.findFirst("select * from tenant_print_template where order_type = ? and name = ? limit 1", printTemplate.getOrderType(), printTemplate.getName());
		if(_printTemplate != null && _printTemplate.getId().intValue() != printTemplate.getId().intValue()) {
			return Ret.fail("打印模板已存在");
		}
		_printTemplate = TenantPrintTemplate.dao.findById(printTemplate.getId());
		if(_printTemplate == null) {
			return Ret.fail("打印模板不存在，无法修改");
		}
		
		TenantPrintTemplate defaultTemplate = TenantPrintTemplate.dao.findDefault(printTemplate.getOrderType());
		if(printTemplate.getDefaultFlag()) { // 如果是默认模板，则之前的默认要去除
			if(defaultTemplate != null && defaultTemplate.getId().intValue() != printTemplate.getId().intValue()) {
				defaultTemplate.setDefaultFlag(FlagEnum.NO.getValue());
				defaultTemplate.update();
			}
		} else {
			if(defaultTemplate != null && defaultTemplate.getId().intValue() == printTemplate.getId().intValue()) {
				return Ret.fail("当前是默认模板，无法取消默认");
			}
		}
		
		printTemplate.setUpdatedAt(new Date());
		printTemplate.update();
		
		return Ret.ok();
	}
	
	
	/**
	* 删除
	*/
	public Ret delete(List<Integer> ids) {
		if(ids == null || ids.isEmpty()) {
			return Ret.fail("参数错误");
		}
		for (Integer id : ids) {
			TenantPrintTemplate printTemplate = TenantPrintTemplate.dao.findById(id);
			if(printTemplate == null) {
				continue;
			}
			if(printTemplate.getDefaultFlag()) {
				return Ret.fail("不能删除默认打印模板");
			}
			printTemplate.delete();
		}
		
		return Ret.ok("删除打印模板成功");
	}

}