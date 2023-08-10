package com.bytechainx.psi.web.web.controller.goods;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.bytechainx.psi.common.EnumConstant.DataStatusEnum;
import com.bytechainx.psi.common.Permissions;
import com.bytechainx.psi.common.annotation.Permission;
import com.bytechainx.psi.common.model.GoodsInfo;
import com.bytechainx.psi.common.model.GoodsSpec;
import com.bytechainx.psi.common.model.GoodsSpecOptions;
import com.bytechainx.psi.common.model.GoodsSpecRef;
import com.bytechainx.psi.common.service.goods.GoodsSpecService;
import com.bytechainx.psi.web.web.controller.base.BaseController;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.core.Path;
import com.jfinal.kit.Kv;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;


/**
* 规格管理
*/
@Path("/goods/goods/spec")
public class GoodsSpecController extends BaseController {

	@Inject
	private GoodsSpecService goodsSpecService;

	/**
	* 首页
	*/
	@Permission(Permissions.goods_goods_spec)
	public void index() {
		setAttr("hideStopFlag", true);
	}

	/**
	* 列表
	*/
	@Permission(Permissions.goods_goods_spec)
	public void list() {
		int pageNumber = getInt("pageNumber", 1);
		Boolean hideStopFlag = getBoolean("hide_stop_flag", true); // 隐藏停用客户
		Kv condKv = Kv.create();
		if(hideStopFlag) {
			condKv.set("data_status", DataStatusEnum.enable.getValue());
		}
		Page<GoodsSpec> page = goodsSpecService.paginate(condKv, pageNumber, pageSize);
		setAttr("page", page);
	}
	
	@Permission(Permissions.goods_goods)
	public void listByJson() {
		Integer id = getInt("id");
		GoodsInfo goodsInfo = GoodsInfo.dao.findById(id);
		if(goodsInfo == null) {
			renderJson(Ret.fail());
			return;
		}
		List<GoodsSpecRef> specRef = goodsInfo.getGoodsSpecRefList();
		for (GoodsSpecRef ref : specRef) {
			ref.put("name", ref.getGoodsSpec().getName());
		}
		renderJson(Ret.ok().set("data", specRef));
	}

	/**
	* 查看
	*/
	@Permission(Permissions.goods_goods_spec_show)
	public void show() {

		renderJson(Ret.ok());
	}


	/**
	* 添加
	*/
	@Permission(Permissions.goods_goods_spec_create)
	public void add() {

	}

	/**
	* 新增
	*/
	@Permission(Permissions.goods_goods_spec_create)
	@Before(Tx.class)
	public void create() {
		GoodsSpec goodsSpec = getModel(GoodsSpec.class, "", true);
		String[] specOptionValues = getParaValues("goods_spec_options_option_value");
		List<GoodsSpecOptions> optionList = new ArrayList<>();
		for (String v : specOptionValues) {
			GoodsSpecOptions option = new GoodsSpecOptions();
			option.setOptionValue(v);
			option.setDataStatus(DataStatusEnum.enable.getValue());
			optionList.add(option);
		}
		Ret ret = goodsSpecService.create(goodsSpec, optionList);
		renderJson(ret);
	}



	/**
	* 编辑
	*/
	@Permission(Permissions.goods_goods_spec_update)
	public void edit() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderError(404);
			return;
		}
		GoodsSpec goodsSpec = GoodsSpec.dao.findById(id);
		if(goodsSpec == null) {
			renderError(404);
			return;
		}
		setAttr("goodsSpec", goodsSpec);
	}

	/**
	* 修改
	*/
	@Permission(Permissions.goods_goods_spec_update)
	@Before(Tx.class)
	public void update() {
		GoodsSpec goodsSpec = getModel(GoodsSpec.class, "", true);
		Integer[] specOptionIds = getParaValuesToInt("goods_spec_options_id");
		String[] specOptionValues = getParaValues("goods_spec_options_option_value");
		List<GoodsSpecOptions> optionList = new ArrayList<>();
		for (int index = 0; index < specOptionIds.length; index++) {
			GoodsSpecOptions option = new GoodsSpecOptions();
			option.setId(specOptionIds[index]);
			option.setOptionValue(specOptionValues[index]);
			option.setDataStatus(DataStatusEnum.enable.getValue());
			optionList.add(option);
		}
		Ret ret = goodsSpecService.update(goodsSpec, optionList);
		renderJson(ret);
	}



	/**
	* 删除
	*/
	@Permission(Permissions.goods_goods_spec_delete)
	@Before(Tx.class)
	public void delete() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("ID不能为空"));
			return;
		}
		Ret ret = goodsSpecService.delete(Arrays.asList(id));
		renderJson(ret);
	}



	/**
	* 停用
	*/
	@Permission(Permissions.goods_goods_spec_disable)
	@Before(Tx.class)
	public void disable() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("ID不能为空"));
			return;
		}
		Ret ret = goodsSpecService.disable(Arrays.asList(id));

		renderJson(ret);
	}
	
	/**
	* 启用
	*/
	@Permission(Permissions.goods_goods_spec_disable)
	@Before(Tx.class)
	public void enable() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("ID不能为空"));
			return;
		}
		Ret ret = goodsSpecService.enable(Arrays.asList(id));

		renderJson(ret);
	}
	
	/**
	 * 获取规格对应的选项
	 */
	public void specOptionJson() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("ID不能为空"));
			return;
		}
		GoodsSpec goodsSpec = GoodsSpec.dao.findFirst("select * from goods_spec where id = ?", id);
		if(goodsSpec == null) {
			renderJson(Ret.fail("规格不存在"));
			return;
		}
		List<GoodsSpecOptions> list = null;
		Integer goodsInfoId = getInt("goods_info_id");
		if(goodsInfoId != null && goodsInfoId > 0) {
			GoodsSpecRef goodsSpecRef = GoodsSpecRef.dao.findFirst("select * from goods_spec_ref where goods_info_id = ? and goods_spec_id = ?", goodsInfoId, id);
			list = goodsSpecRef.getSpecValueList();
		} else { 
			list = goodsSpec.getOptions();
		}
		for (GoodsSpecOptions option : list) {
			option.put("name", option.getOptionValue());
		}
		renderJson(Ret.ok().set("data", list));
	}

}