package com.bytechainx.psi.sale.service;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.bytechainx.psi.common.EnumConstant.DataStatusEnum;
import com.bytechainx.psi.common.model.CustomerPriceLevel;
import com.bytechainx.psi.common.service.base.CommonService;
import com.jfinal.kit.Kv;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Page;


/**
* 价格等级
*/
public class CustomerPriceLevelService extends CommonService {

	/**
	* 分页列表
	*/
	public Page<CustomerPriceLevel> paginate(Kv conditionColumns, Integer pageNumber, int pageSize) {
		StringBuffer where = new StringBuffer();
		List<Object> params = new ArrayList<Object>();
		where.append("where 1 = 1 ");

		conditionFilter(conditionColumns, where, params);
		
		where.append(" and data_status != ?");
		params.add(DataStatusEnum.delete.getValue());
		
		return CustomerPriceLevel.dao.paginate(pageNumber, pageSize, "select * ", "from customer_price_level "+where.toString()+" order by id desc", params.toArray());
	}

	/**
	* 新增
	*/
	public Ret create(CustomerPriceLevel priceLevel) {
		if(priceLevel == null) {
			return Ret.fail("参数错误");
		}
		if(StringUtils.isEmpty(priceLevel.getName())) {
			return Ret.fail("价格等级名称不能为空");
		}
		CustomerPriceLevel _priceLevel = CustomerPriceLevel.dao.findFirst("select * from customer_price_level where name = ? and data_status != ? limit 1", priceLevel.getName(), DataStatusEnum.delete.getValue());
		if(_priceLevel != null) {
			return Ret.fail("价格等级名称已存在");
		}
		priceLevel.setCreatedAt(new Date());
		priceLevel.setUpdatedAt(new Date());
		priceLevel.save();
		
		return Ret.ok("新增价格等级成功").set("targetId", priceLevel.getId());
	}


	/**
	* 修改
	*/
	public Ret update(CustomerPriceLevel priceLevel) {
		if(priceLevel == null || priceLevel.getId() == null || priceLevel.getId() <= 0) {
			return Ret.fail("参数错误");
		}
		if(StringUtils.isEmpty(priceLevel.getName())) {
			return Ret.fail("价格等级名称不能为空");
		}
		CustomerPriceLevel _priceLevel = CustomerPriceLevel.dao.findFirst("select * from customer_price_level where name = ? and data_status != ? limit 1", priceLevel.getName(), DataStatusEnum.delete.getValue());
		if(_priceLevel != null && _priceLevel.getId().intValue() != priceLevel.getId().intValue()) {
			return Ret.fail("价格等级名称已存在");
		}
		_priceLevel = CustomerPriceLevel.dao.findById(priceLevel.getId());
		if(_priceLevel == null) {
			return Ret.fail("价格等级不存在，无法修改");
		}
		
		priceLevel.setUpdatedAt(new Date());
		priceLevel.update();
		
		return Ret.ok("修改价格等级成功");
	}


	/**
	* 删除
	*/
	public Ret delete(List<Integer> ids) {
		if(ids == null || ids.isEmpty()) {
			return Ret.fail("参数错误");
		}
		for (Integer id : ids) {
			CustomerPriceLevel priceLevel = CustomerPriceLevel.dao.findById(id);
			if(priceLevel == null) {
				continue;
			}
			priceLevel.setDataStatus(DataStatusEnum.delete.getValue());
			priceLevel.setUpdatedAt(new Date());
			priceLevel.update();
		}
		return Ret.ok("删除价格等级成功");
	}
	
	/**
	* 停用
	*/
	public Ret disable(List<Integer> ids) {
		if(ids == null || ids.isEmpty()) {
			return Ret.fail("参数错误");
		}
		for (Integer id : ids) {
			CustomerPriceLevel priceLevel = CustomerPriceLevel.dao.findById(id);
			if(priceLevel == null) {
				continue;
			}
			priceLevel.setDataStatus(DataStatusEnum.disable.getValue());
			priceLevel.setUpdatedAt(new Date());
			priceLevel.update();
		}
		return Ret.ok("停用价格等级成功");
	}

	
	/**
	* 启用
	*/
	public Ret enable(List<Integer> ids) {
		if(ids == null || ids.isEmpty()) {
			return Ret.fail("参数错误");
		}
		for (Integer id : ids) {
			CustomerPriceLevel priceLevel = CustomerPriceLevel.dao.findById(id);
			if(priceLevel == null) {
				continue;
			}
			priceLevel.setDataStatus(DataStatusEnum.enable.getValue());
			priceLevel.setUpdatedAt(new Date());
			priceLevel.update();
		}
		return Ret.ok("启用价格等级成功");
	}

}