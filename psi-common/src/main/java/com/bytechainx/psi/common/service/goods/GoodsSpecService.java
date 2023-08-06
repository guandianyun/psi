package com.bytechainx.psi.common.service.goods;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.bytechainx.psi.common.EnumConstant.DataStatusEnum;
import com.bytechainx.psi.common.model.GoodsSpec;
import com.bytechainx.psi.common.model.GoodsSpecOptions;
import com.bytechainx.psi.common.service.base.CommonService;
import com.jfinal.kit.Kv;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Page;


/**
* 规格管理
*/
public class GoodsSpecService extends CommonService {

	/**
	* 分页列表
	 * @param condKv 
	*/
	public Page<GoodsSpec> paginate(Kv conditionColumns, Integer pageNumber, int pageSize) {
		StringBuffer where = new StringBuffer();
		List<Object> params = new ArrayList<Object>();
		where.append("where 1 = 1 ");

		conditionFilter(conditionColumns, where, params);
		
		if(conditionColumns == null || !conditionColumns.containsKey("data_status")) {
			where.append(" and data_status != ?");
			params.add(DataStatusEnum.delete.getValue());
		}

		return GoodsSpec.dao.paginate(pageNumber, pageSize, "select * ", "from goods_spec "+where.toString()+" order by id desc", params.toArray());
	}

	/**
	* 新增
	*/
	
	public Ret create(GoodsSpec spec, List<GoodsSpecOptions> options) {
		if(spec == null || options.isEmpty()) {
			return Ret.fail("参数错误");
		}
		
		if(StringUtils.isEmpty(spec.getName())) {
			return Ret.fail("规格名称不能为空");
		}
		GoodsSpec _spec = GoodsSpec.dao.findFirst("select * from goods_spec where name = ? and data_status != ? limit 1", spec.getName(), DataStatusEnum.delete.getValue());
		if(_spec != null) {
			return Ret.fail("规格名称已存在");
		}
		spec.setCreatedAt(new Date());
		spec.setUpdatedAt(new Date());
		spec.save();
		
		for (GoodsSpecOptions option : options) {
			if(StringUtils.isEmpty(option.getOptionValue())) {
				continue;
			}
			option.setGoodsSpecId(spec.getId());
			option.save();
		}
		return Ret.ok();
	}


	/**
	* 修改
	*/
	
	public Ret update(GoodsSpec spec, List<GoodsSpecOptions> options) {
		if(spec == null || spec.getId() == null || spec.getId() <= 0 || options.isEmpty()) {
			return Ret.fail("参数错误");
		}
		if(StringUtils.isEmpty(spec.getName())) {
			return Ret.fail("规格名称不能为空");
		}
		if(options == null || options.isEmpty()) {
			return Ret.fail("规格选项不能为空");
		}
		GoodsSpec _spec = GoodsSpec.dao.findFirst("select * from goods_spec where name = ? and data_status != ? limit 1", spec.getName(), DataStatusEnum.delete.getValue());
		if(_spec != null && _spec.getId().intValue() != spec.getId().intValue()) {
			return Ret.fail("规格名称已存在");
		}
		_spec = GoodsSpec.dao.findById(spec.getId());
		if(_spec == null) {
			return Ret.fail("规格不存在，无法修改");
		}
		
		spec.setUpdatedAt(new Date());
		spec.update();
		
		List<GoodsSpecOptions> deleteOptions = new ArrayList<>();
		for(GoodsSpecOptions oldOption : _spec.getOptions()) {
			boolean isExist = false;
			for (GoodsSpecOptions newOption : options) {
				// 选项为空也要删除
				if(StringUtils.equalsIgnoreCase(oldOption.getOptionValue(), newOption.getOptionValue())
						|| (newOption.getId() != null && newOption.getId().intValue() == oldOption.getId().intValue() && StringUtils.isNotEmpty(newOption.getOptionValue()))) {
					isExist = true;
					break;
				}
			}
			if(!isExist) { // 需要删除的选项
				deleteOptions.add(oldOption);
			}
		}
		
		for (GoodsSpecOptions newOption : options) {
			if(StringUtils.isEmpty(newOption.getOptionValue())) {
				continue;
			}
			GoodsSpecOptions _option = GoodsSpecOptions.dao.findFirst("select * from goods_spec_options where goods_spec_id = ? and option_value = ? limit 1", spec.getId(), newOption.getOptionValue());
			if(_option != null) {
				if(newOption.getId() != null && newOption.getId().intValue() > 0 && _option.getId().intValue() != newOption.getId().intValue()) {
					continue;
				} else if(newOption.getId() == null || newOption.getId().intValue() <= 0) {
					_option.setDataStatus(DataStatusEnum.enable.getValue()); // 之前状态可能是停用或者删除，重新启用。
					_option.update();
					continue;
				}
			} 
			newOption.setGoodsSpecId(spec.getId());
			
			if(newOption.getId() != null && newOption.getId() > 0) {
				newOption.update();
			} else {
				newOption.save();
			}
		}
		for (GoodsSpecOptions option : deleteOptions) {
			option.setDataStatus(DataStatusEnum.delete.getValue());
			option.update();
		}
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
			GoodsSpec spec = GoodsSpec.dao.findById(id);
			if(spec == null) {
				continue;
			}
			for (GoodsSpecOptions option : spec.getOptions()) {
				option.setDataStatus(DataStatusEnum.delete.getValue());
				option.update();
			}
			spec.setDataStatus(DataStatusEnum.delete.getValue());
			spec.setUpdatedAt(new Date());
			spec.update();
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
			GoodsSpec spec = GoodsSpec.dao.findById(id);
			if(spec == null) {
				continue;
			}
			for (GoodsSpecOptions option : spec.getOptions()) {
				option.setDataStatus(DataStatusEnum.disable.getValue());
				option.update();
			}
			spec.setDataStatus(DataStatusEnum.disable.getValue());
			spec.setUpdatedAt(new Date());
			spec.update();
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
			GoodsSpec spec = GoodsSpec.dao.findById(id);
			if(spec == null) {
				continue;
			}
			for (GoodsSpecOptions option : spec.getOptions()) {
				option.setDataStatus(DataStatusEnum.enable.getValue());
				option.update();
			}
			spec.setDataStatus(DataStatusEnum.enable.getValue());
			spec.setUpdatedAt(new Date());
			spec.update();
		}
		return Ret.ok();
	}

}