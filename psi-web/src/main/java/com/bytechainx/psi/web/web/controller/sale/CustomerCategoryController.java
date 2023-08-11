package com.bytechainx.psi.web.web.controller.sale;


import java.util.Arrays;

import com.bytechainx.psi.common.Permissions;
import com.bytechainx.psi.common.annotation.Permission;
import com.bytechainx.psi.common.model.CustomerCategory;
import com.bytechainx.psi.sale.service.CustomerCategoryService;
import com.bytechainx.psi.web.web.controller.base.BaseController;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.core.Path;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;


/**
* 客户分类
*/
@Path("/sale/customer/category")
public class CustomerCategoryController extends BaseController {


	@Inject
	private CustomerCategoryService customerCategoryService;

	/**
	* 首页
	*/
	@Permission(Permissions.sale_customer_category)
	public void index() {

	}

	/**
	* 列表
	*/
	@Permission(Permissions.sale_customer_category)
	public void list() {
		int pageNumber = getInt("pageNumber", 1);
		Page<CustomerCategory> page = customerCategoryService.paginate(pageNumber, pageSize);
		setAttr("page", page);
	}
	
	@Permission(Permissions.sale_customer_category)
	public void optionList() {
		Page<CustomerCategory> page = customerCategoryService.paginate(1, maxPageSize);
		setAttr("page", page);
	}

	/**
	* 查看
	*/
	@Permission(Permissions.sale_customer_category_show)
	public void show() {

		renderJson(Ret.ok());
	}


	/**
	* 添加
	*/
	@Permission(Permissions.sale_customer_category_create)
	public void add() {
		setAttr("sourcePage", get("sourcePage"));
	}

	/**
	* 新增
	*/
	@Permission(Permissions.sale_customer_category_create)
	@Before(Tx.class)
	public void create() {
		CustomerCategory category = getModel(CustomerCategory.class, "", true);
		Ret ret = customerCategoryService.create(category);
		renderJson(ret);
	}



	/**
	* 编辑
	*/
	@Permission(Permissions.sale_customer_category_update)
	public void edit() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderError(404);
			return;
		}
		CustomerCategory category = CustomerCategory.dao.findById(id);
		if(category == null) {
			renderError(404);
			return;
		}
		setAttr("customerCategory", category);
	}

	/**
	* 修改
	*/
	@Permission(Permissions.sale_customer_category_update)
	@Before(Tx.class)
	public void update() {
		CustomerCategory category = getModel(CustomerCategory.class, "", true);
		Ret ret = customerCategoryService.update(category);
		renderJson(ret);
	}



	/**
	* 删除
	*/
	@Permission(Permissions.sale_customer_category_delete)
	@Before(Tx.class)
	public void delete() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("ID不能为空"));
			return;
		}
		Ret ret = customerCategoryService.delete(Arrays.asList(id));
		renderJson(ret);
	}


}