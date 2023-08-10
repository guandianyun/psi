package com.bytechainx.psi.web.web.controller.goods;


import java.util.Arrays;

import com.bytechainx.psi.common.Permissions;
import com.bytechainx.psi.common.EnumConstant.DataStatusEnum;
import com.bytechainx.psi.common.annotation.Permission;
import com.bytechainx.psi.common.model.GoodsUnit;
import com.bytechainx.psi.common.service.goods.GoodsUnitService;
import com.bytechainx.psi.web.web.controller.base.BaseController;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.core.Path;
import com.jfinal.kit.Kv;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;


/**
* 单位管理
*/
@Path("/goods/goods/unit")
public class GoodsUnitController extends BaseController {

	@Inject
	private GoodsUnitService goodsUnitService;

	/**
	* 首页
	*/
	@Permission(Permissions.goods_goods_unit)
	public void index() {
		setAttr("hideStopFlag", true);
	}

	/**
	* 列表
	*/
	@Permission(Permissions.goods_goods_unit)
	public void list() {
		int pageNumber = getInt("pageNumber", 1);
		Boolean hideStopFlag = getBoolean("hide_stop_flag", true); // 隐藏停用客户
		Kv condKv = Kv.create();
		if(hideStopFlag) {
			condKv.set("data_status", DataStatusEnum.enable.getValue());
		}
		Page<GoodsUnit> page = goodsUnitService.paginate(condKv, pageNumber, pageSize);
		setAttr("page", page);
	}

	/**
	* 查看
	*/
	@Permission(Permissions.goods_goods_unit_show)
	public void show() {

		renderJson(Ret.ok());
	}


	/**
	* 添加
	*/
	@Permission(Permissions.goods_goods_unit_create)
	public void add() {

	}

	/**
	* 新增
	*/
	@Permission(Permissions.goods_goods_unit_create)
	@Before(Tx.class)
	public void create() {
		GoodsUnit goodsUnit = getModel(GoodsUnit.class, "", true);
		Ret ret = goodsUnitService.create(goodsUnit);
		renderJson(ret);
	}



	/**
	* 编辑
	*/
	@Permission(Permissions.goods_goods_unit_update)
	public void edit() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderError(404);
			return;
		}
		GoodsUnit goodsUnit = GoodsUnit.dao.findById(id);
		if(goodsUnit == null) {
			renderError(404);
			return;
		}
		setAttr("goodsUnit", goodsUnit);
	}

	/**
	* 修改
	*/
	@Permission(Permissions.goods_goods_unit_update)
	@Before(Tx.class)
	public void update() {
		GoodsUnit goodsUnit = getModel(GoodsUnit.class, "", true);
		Ret ret = goodsUnitService.update(goodsUnit);
		renderJson(ret);
	}



	/**
	* 删除
	*/
	@Permission(Permissions.goods_goods_unit_delete)
	@Before(Tx.class)
	public void delete() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("ID不能为空"));
			return;
		}
		Ret ret = goodsUnitService.delete(Arrays.asList(id));
		renderJson(ret);
	}



	/**
	* 停用
	*/
	@Permission(Permissions.goods_goods_unit_disable)
	@Before(Tx.class)
	public void disable() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("ID不能为空"));
			return;
		}
		Ret ret = goodsUnitService.disable(Arrays.asList(id));

		renderJson(ret);
	}
	
	/**
	* 启用
	*/
	@Permission(Permissions.goods_goods_unit_disable)
	@Before(Tx.class)
	public void enable() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("ID不能为空"));
			return;
		}
		Ret ret = goodsUnitService.enable(Arrays.asList(id));

		renderJson(ret);
	}

}