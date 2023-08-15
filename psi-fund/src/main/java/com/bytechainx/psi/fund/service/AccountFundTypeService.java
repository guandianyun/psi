package com.bytechainx.psi.fund.service;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.bytechainx.psi.common.EnumConstant.DataStatusEnum;
import com.bytechainx.psi.common.model.TraderFundType;
import com.bytechainx.psi.fund.service.base.BaseService;
import com.jfinal.kit.Kv;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Page;


/**
* 收支项目
*/
public class AccountFundTypeService extends BaseService {

	/**
	* 分页列表
	 * @param condKv 
	*/
	public Page<TraderFundType> paginate(Kv conditionColumns, Integer pageNumber, int pageSize) {
		StringBuffer where = new StringBuffer();
		List<Object> params = new ArrayList<Object>();
		where.append("where 1 = 1 ");

		conditionFilter(conditionColumns, where, params);
		
		if(conditionColumns == null || !conditionColumns.containsKey("data_status")) {
			where.append(" and data_status != ?");
			params.add(DataStatusEnum.delete.getValue());
		}
		
		where.append(" and data_status != ?");
		params.add(DataStatusEnum.delete.getValue());
		
		return TraderFundType.dao.paginate(pageNumber, pageSize, "select * ", "from trader_fund_type "+where.toString()+" order by id desc", params.toArray());
	}

	/**
	* 新增
	*/
	public Ret create(TraderFundType fundType) {
		if(fundType == null) {
			return Ret.fail("参数错误");
		}
		if(StringUtils.isEmpty(fundType.getName())) {
			return Ret.fail("收支项目名称不能为空");
		}
		TraderFundType _fundType = TraderFundType.dao.findFirst("select * from trader_fund_type where name = ? and data_status != ? limit 1", fundType.getName(), DataStatusEnum.delete.getValue());
		if(_fundType != null) {
			return Ret.fail("收支项目名称已存在");
		}
		fundType.setCreatedAt(new Date());
		fundType.setUpdatedAt(new Date());
		fundType.save();
		
		return Ret.ok("新增收支项目成功").set("targetId", fundType.getId());
	}


	/**
	* 修改
	*/
	public Ret update(TraderFundType fundType) {
		if(fundType == null || fundType.getId() == null || fundType.getId() <= 0) {
			return Ret.fail("参数错误");
		}
		if(StringUtils.isEmpty(fundType.getName())) {
			return Ret.fail("收支项目名称不能为空");
		}
		TraderFundType _fundType = TraderFundType.dao.findFirst("select * from trader_fund_type where name = ? and data_status != ? limit 1", fundType.getName(), DataStatusEnum.delete.getValue());
		if(_fundType != null && _fundType.getId().intValue() != fundType.getId().intValue()) {
			return Ret.fail("收支项目名称已存在");
		}
		_fundType = TraderFundType.dao.findById(fundType.getId());
		if(_fundType == null) {
			return Ret.fail("收支项目不存在，无法修改");
		}
		
		fundType.setUpdatedAt(new Date());
		fundType.update();
		
		return Ret.ok("修改收支项目成功");
	}


	/**
	* 删除
	*/
	public Ret delete(List<Integer> ids) {
		if(ids == null || ids.isEmpty()) {
			return Ret.fail("参数错误");
		}
		for (Integer id : ids) {
			TraderFundType fundType = TraderFundType.dao.findById(id);
			if(fundType == null) {
				continue;
			}
			fundType.setDataStatus(DataStatusEnum.delete.getValue());
			fundType.setUpdatedAt(new Date());
			fundType.update();
		}
		
		return Ret.ok("删除收支项目成功");
	}
	
	/**
	* 停用
	*/
	public Ret disable(List<Integer> ids) {
		if(ids == null || ids.isEmpty()) {
			return Ret.fail("参数错误");
		}
		for (Integer id : ids) {
			TraderFundType fundType = TraderFundType.dao.findById(id);
			if(fundType == null) {
				continue;
			}
			fundType.setDataStatus(DataStatusEnum.disable.getValue());
			fundType.setUpdatedAt(new Date());
			fundType.update();
		}
		
		return Ret.ok("停用收支项目成功");
	}

	
	/**
	* 启用
	*/
	public Ret enable(List<Integer> ids) {
		if(ids == null || ids.isEmpty()) {
			return Ret.fail("参数错误");
		}
		for (Integer id : ids) {
			TraderFundType fundType = TraderFundType.dao.findById(id);
			if(fundType == null) {
				continue;
			}
			fundType.setDataStatus(DataStatusEnum.enable.getValue());
			fundType.setUpdatedAt(new Date());
			fundType.update();
		}
		
		return Ret.ok("启用收支项目成功");
	}

}