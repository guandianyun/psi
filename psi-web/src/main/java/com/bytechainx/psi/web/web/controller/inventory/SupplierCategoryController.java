package com.bytechainx.psi.web.web.controller.inventory;


import java.util.Arrays;

import com.bytechainx.psi.common.Permissions;
import com.bytechainx.psi.common.annotation.Permission;
import com.bytechainx.psi.common.model.SupplierCategory;
import com.bytechainx.psi.purchase.service.SupplierCategoryService;
import com.bytechainx.psi.web.web.controller.base.BaseController;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.core.Path;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;


/**
* 供应商分类
*/
@Path("/inventory/supplier/category")
public class SupplierCategoryController extends BaseController {


	@Inject
	private SupplierCategoryService supplierCategoryService;

	/**
	* 首页
	*/
	@Permission(Permissions.inventory_supplier_category)
	public void index() {

	}

	/**
	* 列表
	*/
	@Permission(Permissions.inventory_supplier_category)
	public void list() {
		int pageNumber = getInt("pageNumber", 1);
		Page<SupplierCategory> page = supplierCategoryService.paginate(pageNumber, pageSize);
		setAttr("page", page);
	}
	
	@Permission(Permissions.inventory_supplier_category)
	public void optionList() {
		Page<SupplierCategory> page = supplierCategoryService.paginate(1, maxPageSize);
		setAttr("page", page);
	}

	/**
	* 查看
	*/
	@Permission(Permissions.inventory_supplier_category_show)
	public void show() {

		renderJson(Ret.ok());
	}


	/**
	* 添加
	*/
	@Permission(Permissions.inventory_supplier_category_create)
	public void add() {
		setAttr("sourcePage", get("sourcePage"));
	}

	/**
	* 新增
	*/
	@Permission(Permissions.inventory_supplier_category_create)
	@Before(Tx.class)
	public void create() {
		SupplierCategory category = getModel(SupplierCategory.class, "", true);
		Ret ret = supplierCategoryService.create(category);
		renderJson(ret);
	}



	/**
	* 编辑
	*/
	@Permission(Permissions.inventory_supplier_category_update)
	public void edit() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderError(404);
			return;
		}
		SupplierCategory category = SupplierCategory.dao.findById(id);
		if(category == null) {
			renderError(404);
			return;
		}
		setAttr("supplierCategory", category);
	}

	/**
	* 修改
	*/
	@Permission(Permissions.inventory_supplier_category_update)
	@Before(Tx.class)
	public void update() {
		SupplierCategory category = getModel(SupplierCategory.class, "", true);
		Ret ret = supplierCategoryService.update(category);
		renderJson(ret);
	}



	/**
	* 删除
	*/
	@Permission(Permissions.inventory_supplier_category_delete)
	@Before(Tx.class)
	public void delete() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("ID不能为空"));
			return;
		}
		Ret ret = supplierCategoryService.delete(Arrays.asList(id));
		renderJson(ret);
	}

}