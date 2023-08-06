package com.bytechainx.psi.common.service.goods;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.bytechainx.psi.common.EnumConstant.DataStatusEnum;
import com.bytechainx.psi.common.EnumConstant.GoodsAttrTypeEnum;
import com.bytechainx.psi.common.model.GoodsAttribute;
import com.bytechainx.psi.common.service.base.CommonService;
import com.jfinal.kit.Kv;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Page;


/**
* 商品属性
*/
public class GoodsAttributeService extends CommonService {

	/**
	* 分页列表
	 * @param condKv 
	*/
	public Page<GoodsAttribute> paginate(Kv conditionColumns, Integer pageNumber, int pageSize) {
		StringBuffer where = new StringBuffer();
		List<Object> params = new ArrayList<Object>();
		where.append("where 1 = 1 ");
		
		conditionFilter(conditionColumns, where, params);
		
		if(conditionColumns == null || !conditionColumns.containsKey("data_status")) {
			where.append(" and data_status != ?");
			params.add(DataStatusEnum.delete.getValue());
		}
		
		return GoodsAttribute.dao.paginate(pageNumber, pageSize, "select * ", "from goods_attribute "+where.toString()+" order by id desc", params.toArray());
	}

	/**
	* 新增
	*/
	
	public Ret create(GoodsAttribute attribute) {
		if(StringUtils.isEmpty(attribute.getName())) {
			return Ret.fail("属性名称不能为空");
		}
		GoodsAttribute _attribute = GoodsAttribute.dao.findFirst("select * from goods_attribute where name = ? and data_status != ? limit 1", attribute.getName(), DataStatusEnum.delete.getValue());
		if(_attribute != null) {
			return Ret.fail("属性名称已存在");
		}
		if(attribute.getAttrType() == GoodsAttrTypeEnum.select.getValue() && StringUtils.isEmpty(attribute.getAttrValues())) {
			return Ret.fail("属性值不能为空");
		}
		attribute.setCreatedAt(new Date());
		attribute.setUpdatedAt(new Date());
		attribute.save();
		
		return Ret.ok();
	}


	/**
	* 修改
	*/
	
	public Ret update(GoodsAttribute attribute) {
		if(StringUtils.isEmpty(attribute.getName())) {
			return Ret.fail("属性名称不能为空");
		}
		GoodsAttribute _attribute = GoodsAttribute.dao.findFirst("select * from goods_attribute where name = ? and data_status != ? limit 1", attribute.getName(), DataStatusEnum.delete.getValue());
		if(_attribute != null && _attribute.getId().intValue() != attribute.getId().intValue()) {
			return Ret.fail("属性名称已存在");
		}
		if(attribute.getAttrType() == GoodsAttrTypeEnum.select.getValue() && StringUtils.isEmpty(attribute.getAttrValues())) {
			return Ret.fail("属性值不能为空");
		}
		_attribute = GoodsAttribute.dao.findById(attribute.getId());
		if(_attribute == null) {
			return Ret.fail("属性不存在，无法修改");
		}
		
		attribute.setUpdatedAt(new Date());
		attribute.update();
		
		return Ret.ok();
	}


	/**
	* 删除
	*/
	
	public Ret delete(List<Integer> ids) {
		for (Integer id : ids) {
			GoodsAttribute attribute = GoodsAttribute.dao.findById(id);
			if(attribute == null) {
				continue;
			}
			attribute.setDataStatus(DataStatusEnum.delete.getValue());
			attribute.setUpdatedAt(new Date());
			attribute.update();
		}
		return Ret.ok();
	}
	
	/**
	* 停用
	*/
	
	public Ret disable(List<Integer> ids) {
		for (Integer id : ids) {
			GoodsAttribute attribute = GoodsAttribute.dao.findById(id);
			if(attribute == null) {
				continue;
			}
			attribute.setDataStatus(DataStatusEnum.disable.getValue());
			attribute.setUpdatedAt(new Date());
			attribute.update();
		}
		return Ret.ok();
	}

	
	/**
	* 启用
	*/
	
	public Ret enable(List<Integer> ids) {
		for (Integer id : ids) {
			GoodsAttribute attribute = GoodsAttribute.dao.findById(id);
			if(attribute == null) {
				continue;
			}
			attribute.setDataStatus(DataStatusEnum.enable.getValue());
			attribute.setUpdatedAt(new Date());
			attribute.update();
		}
		return Ret.ok();
	}
}