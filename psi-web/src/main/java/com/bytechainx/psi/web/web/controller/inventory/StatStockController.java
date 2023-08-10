package com.bytechainx.psi.web.web.controller.inventory;


import com.bytechainx.psi.web.web.controller.base.BaseController;
import com.jfinal.core.Path;
import com.jfinal.kit.Ret;


/**
* 库存统计
*/
@Path("/inventory/stat/stock")
public class StatStockController extends BaseController {


	/**
	* 首页
	*/
//	@Permission(Permissions.inventory_stat_stock)
	public void index() {

		list();

	}

	/**
	* 列表
	*/
//	@Permission(Permissions.inventory_stat_stock)
	public void list() {


	}

	/**
	* 查看
	*/
//	@Permission(Permissions.inventory_stat_stock_show)
	public void show() {

		renderJson(Ret.ok());
	}
}