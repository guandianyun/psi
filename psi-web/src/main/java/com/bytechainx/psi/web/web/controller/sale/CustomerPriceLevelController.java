package com.bytechainx.psi.web.web.controller.sale;


import java.util.Arrays;

import com.bytechainx.psi.common.Permissions;
import com.bytechainx.psi.common.annotation.Permission;
import com.bytechainx.psi.common.model.CustomerPriceLevel;
import com.bytechainx.psi.sale.service.CustomerPriceLevelService;
import com.bytechainx.psi.web.web.controller.base.BaseController;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.core.Path;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;


/**
* 价格等级
*/
@Path("/sale/customer/priceLevel")
public class CustomerPriceLevelController extends BaseController {

	@Inject
	private CustomerPriceLevelService customerPriceLevelService;

	/**
	* 首页
	*/
	@Permission(Permissions.sale_customer_priceLevel)
	public void index() {

	}

	/**
	* 列表
	*/
	@Permission(Permissions.sale_customer_priceLevel)
	public void list() {
		int pageNumber = getInt("pageNumber", 1);
		Page<CustomerPriceLevel> page = customerPriceLevelService.paginate(null, pageNumber, pageSize);
		setAttr("page", page);
	}

	/**
	* 查看
	*/
	@Permission(Permissions.sale_customer_priceLevel_show)
	public void show() {

		renderJson(Ret.ok());
	}


	/**
	* 添加
	*/
	@Permission(Permissions.sale_customer_priceLevel_create)
	public void add() {

	}

	/**
	* 新增
	*/
	@Permission(Permissions.sale_customer_priceLevel_create)
	@Before(Tx.class)
	public void create() {
		CustomerPriceLevel priceLevel = getModel(CustomerPriceLevel.class, "", true);
		Ret ret = customerPriceLevelService.create(priceLevel);
		renderJson(ret);
	}



	/**
	* 编辑
	*/
	@Permission(Permissions.sale_customer_priceLevel_update)
	public void edit() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderError(404);
			return;
		}
		CustomerPriceLevel priceLevel = CustomerPriceLevel.dao.findById(id);
		if(priceLevel == null) {
			renderError(404);
			return;
		}
		setAttr("customerPriceLevel", priceLevel);
	}

	/**
	* 修改
	*/
	@Permission(Permissions.sale_customer_priceLevel_update)
	@Before(Tx.class)
	public void update() {
		CustomerPriceLevel priceLevel = getModel(CustomerPriceLevel.class, "", true);
		Ret ret = customerPriceLevelService.update(priceLevel);
		renderJson(ret);
	}



	/**
	* 删除
	*/
	@Permission(Permissions.sale_customer_priceLevel_delete)
	@Before(Tx.class)
	public void delete() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("ID不能为空"));
			return;
		}
		Ret ret = customerPriceLevelService.delete(Arrays.asList(id));
		renderJson(ret);
	}



	/**
	* 停用
	*/
	@Permission(Permissions.sale_customer_priceLevel_disable)
	@Before(Tx.class)
	public void disable() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("ID不能为空"));
			return;
		}
		Ret ret = customerPriceLevelService.disable(Arrays.asList(id));

		renderJson(ret);
	}
	
	/**
	* 启用
	*/
	@Permission(Permissions.sale_customer_priceLevel_disable)
	@Before(Tx.class)
	public void enable() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("ID不能为空"));
			return;
		}
		Ret ret = customerPriceLevelService.enable(Arrays.asList(id));

		renderJson(ret);
	}

}