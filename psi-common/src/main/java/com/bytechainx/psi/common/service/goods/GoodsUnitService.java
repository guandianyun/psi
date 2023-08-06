package com.bytechainx.psi.common.service.goods;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.bytechainx.psi.common.EnumConstant.DataStatusEnum;
import com.bytechainx.psi.common.model.GoodsUnit;
import com.bytechainx.psi.common.service.base.CommonService;
import com.jfinal.kit.Kv;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Page;


/**
* 单位管理
*/
public class GoodsUnitService extends CommonService {


	/**
	* 分页列表
	 * @param condKv 
	*/
	public Page<GoodsUnit> paginate(Kv conditionColumns, Integer pageNumber, int pageSize) {
		StringBuffer where = new StringBuffer();
		List<Object> params = new ArrayList<Object>();
		where.append("where 1 = 1 ");

		conditionFilter(conditionColumns, where, params);
		
		if(conditionColumns == null || !conditionColumns.containsKey("data_status")) {
			where.append(" and data_status != ?");
			params.add(DataStatusEnum.delete.getValue());
		}
		
		return GoodsUnit.dao.paginate(pageNumber, pageSize, "select * ", "from goods_unit "+where.toString()+" order by id desc", params.toArray());
	}

	/**
	* 新增
	*/
	public Ret create(GoodsUnit unit) {
		if(unit == null) {
			return Ret.fail("参数错误");
		}
		if(StringUtils.isEmpty(unit.getName())) {
			return Ret.fail("单位名称不能为空");
		}
		GoodsUnit _unit = GoodsUnit.dao.findFirst("select * from goods_unit where name = ? and data_status != ? limit 1", unit.getName(), DataStatusEnum.delete.getValue());
		if(_unit != null) {
			return Ret.fail("单位名称已存在");
		}
		unit.setCreatedAt(new Date());
		unit.setUpdatedAt(new Date());
		unit.save();
		
		return Ret.ok();
	}


	/**
	* 修改
	*/
	public Ret update(GoodsUnit unit) {
		if(unit == null || unit.getId() == null || unit.getId() <= 0) {
			return Ret.fail("参数错误");
		}
		if(StringUtils.isEmpty(unit.getName())) {
			return Ret.fail("单位名称不能为空");
		}
		GoodsUnit _unit = GoodsUnit.dao.findFirst("select * from goods_unit where name = ? and data_status != ? limit 1", unit.getName(), DataStatusEnum.delete.getValue());
		if(_unit != null && _unit.getId().intValue() != unit.getId().intValue()) {
			return Ret.fail("单位名称已存在");
		}
		_unit = GoodsUnit.dao.findById(unit.getId());
		if(_unit == null) {
			return Ret.fail("商品单位不存在，无法修改");
		}
		
		unit.setUpdatedAt(new Date());
		unit.update();
		
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
			GoodsUnit unit = GoodsUnit.dao.findById(id);
			if(unit == null) {
				continue;
			}
			unit.setDataStatus(DataStatusEnum.delete.getValue());
			unit.setUpdatedAt(new Date());
			unit.update();
		}
		return Ret.ok();
	}
	
	/**
	* 停用
	*/
	public Ret disable(List<Integer> ids) {
		if(ids == null || ids.isEmpty()) {
			return Ret.fail("参数错误");
		}
		for (Integer id : ids) {
			GoodsUnit unit = GoodsUnit.dao.findById(id);
			if(unit == null) {
				continue;
			}
			unit.setDataStatus(DataStatusEnum.disable.getValue());
			unit.setUpdatedAt(new Date());
			unit.update();
		}
		return Ret.ok();
	}

	
	/**
	* 启用
	*/
	public Ret enable(List<Integer> ids) {
		if(ids == null || ids.isEmpty()) {
			return Ret.fail("参数错误");
		}
		for (Integer id : ids) {
			GoodsUnit unit = GoodsUnit.dao.findById(id);
			if(unit == null) {
				continue;
			}
			unit.setDataStatus(DataStatusEnum.enable.getValue());
			unit.setUpdatedAt(new Date());
			unit.update();
		}
		return Ret.ok();
	}

}