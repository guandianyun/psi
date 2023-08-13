package com.bytechainx.psi.sale.service;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.bytechainx.psi.common.model.CustomerCategory;
import com.bytechainx.psi.common.service.base.CommonService;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;


/**
* 客户分类
*/
public class CustomerCategoryService extends CommonService {

	/**
	* 分页列表
	*/
	public Page<CustomerCategory> paginate(Integer pageNumber, int pageSize) {
		StringBuffer where = new StringBuffer();
		List<Object> params = new ArrayList<Object>();
		where.append("where 1 = 1 ");


		return CustomerCategory.dao.paginate(pageNumber, pageSize, "select * ", "from customer_category "+where.toString()+" order by id desc", params.toArray());
	}

	/**
	* 新增
	*/
	public Ret create(CustomerCategory category) {
		if(category == null) {
			return Ret.fail("参数错误");
		}
		if(StringUtils.isEmpty(category.getName())) {
			return Ret.fail("分类名称不能为空");
		}
		CustomerCategory _category = CustomerCategory.dao.findFirst("select * from customer_category where name = ? limit 1", category.getName());
		if(_category != null) {
			return Ret.fail("分类名称已存在");
		}
		category.setCreatedAt(new Date());
		category.save();
		
		return Ret.ok("新增分类成功").set("targetId", category.getId());
	}


	/**
	* 修改
	*/
	public Ret update(CustomerCategory category) {
		if(category == null || category.getId() == null || category.getId() <= 0) {
			return Ret.fail("参数错误");
		}
		if(StringUtils.isEmpty(category.getName())) {
			return Ret.fail("分类名称不能为空");
		}
		CustomerCategory _category = CustomerCategory.dao.findFirst("select * from customer_category where name = ? limit 1", category.getName());
		if(_category != null && _category.getId().intValue() != category.getId().intValue()) {
			return Ret.fail("分类名称已存在");
		}
		_category = CustomerCategory.dao.findById(category.getId());
		if(_category == null) {
			return Ret.fail("客户分类不存在，无法修改");
		}
		
		category.update();
		
		return Ret.ok("修改分类成功");
	}


	/**
	* 删除
	*/
	public Ret delete(List<Integer> ids) {
		if(ids == null || ids.isEmpty()) {
			return Ret.fail("参数错误");
		}
		for (Integer id : ids) {
			CustomerCategory.dao.deleteById(id);
			Db.update("update customer_info set customer_category_id = ? where customer_category_id = ?", 0, id);
		}
		
		return Ret.ok("删除分类成功");
	}

}