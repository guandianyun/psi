package com.bytechainx.psi.web.web.controller.goods;


import java.util.Arrays;

import com.bytechainx.psi.common.Permissions;
import com.bytechainx.psi.common.EnumConstant.DataStatusEnum;
import com.bytechainx.psi.common.annotation.Permission;
import com.bytechainx.psi.common.model.GoodsAttribute;
import com.bytechainx.psi.common.service.goods.GoodsAttributeService;
import com.bytechainx.psi.web.web.controller.base.BaseController;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.core.Path;
import com.jfinal.kit.Kv;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;


/**
* 商品属性
*/
@Path("/goods/goods/attribute")
public class GoodsAttributeController extends BaseController {

	@Inject
	private GoodsAttributeService attributeService;
	/**
	* 首页
	*/
	@Permission(Permissions.goods_goods_attribute)
	public void index() {
		setAttr("hideStopFlag", true);
	}

	/**
	* 列表
	*/
	@Permission(Permissions.goods_goods_attribute)
	public void list() {
		int pageNumber = getInt("pageNumber", 1);
		Boolean hideStopFlag = getBoolean("hide_stop_flag", true); // 隐藏停用
		Kv condKv = Kv.create();
		if(hideStopFlag) {
			condKv.set("data_status", DataStatusEnum.enable.getValue());
		}
		Page<GoodsAttribute> page = attributeService.paginate(condKv, pageNumber, pageSize);
		setAttr("page", page);
	}

	/**
	* 查看
	*/
	@Permission(Permissions.goods_goods_attribute_show)
	public void show() {

		renderJson(Ret.ok());
	}


	/**
	* 添加
	*/
	@Permission(Permissions.goods_goods_attribute_create)
	public void add() {

	}

	/**
	* 新增
	*/
	@Permission(Permissions.goods_goods_attribute_create)
	@Before(Tx.class)
	public void create() {
		GoodsAttribute goodsAttribute = getModel(GoodsAttribute.class, "", true);
		Ret ret = attributeService.create(goodsAttribute);
		renderJson(ret);
	}



	/**
	* 编辑
	*/
	@Permission(Permissions.goods_goods_attribute_update)
	public void edit() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderError(404);
			return;
		}
		GoodsAttribute goodsAttribute = GoodsAttribute.dao.findById(id);
		if(goodsAttribute == null) {
			renderError(404);
			return;
		}
		setAttr("goodsAttribute", goodsAttribute);
	}

	/**
	* 修改
	*/
	@Permission(Permissions.goods_goods_attribute_update)
	@Before(Tx.class)
	public void update() {
		GoodsAttribute goodsAttribute = getModel(GoodsAttribute.class, "", true);
		Ret ret = attributeService.update(goodsAttribute);
		renderJson(ret);
	}



	/**
	* 删除
	*/
	@Permission(Permissions.goods_goods_attribute_delete)
	@Before(Tx.class)
	public void delete() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("ID不能为空"));
			return;
		}
		Ret ret = attributeService.delete(Arrays.asList(id));
		renderJson(ret);
	}



	/**
	* 停用
	*/
	@Permission(Permissions.goods_goods_attribute_disable)
	@Before(Tx.class)
	public void disable() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("ID不能为空"));
			return;
		}
		Ret ret = attributeService.disable(Arrays.asList(id));

		renderJson(ret);
	}
	
	/**
	* 启用
	*/
	@Permission(Permissions.goods_goods_attribute_disable)
	@Before(Tx.class)
	public void enable() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("ID不能为空"));
			return;
		}
		Ret ret = attributeService.enable(Arrays.asList(id));

		renderJson(ret);
	}


}