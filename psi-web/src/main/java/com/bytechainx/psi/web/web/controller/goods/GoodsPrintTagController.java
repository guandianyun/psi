package com.bytechainx.psi.web.web.controller.goods;


import java.util.Arrays;
import java.util.List;

import com.bytechainx.psi.common.Permissions;
import com.bytechainx.psi.common.annotation.Permission;
import com.bytechainx.psi.common.model.GoodsPrintTag;
import com.bytechainx.psi.common.model.TenantPrintTemplate;
import com.bytechainx.psi.common.service.goods.GoodsPrintTagService;
import com.bytechainx.psi.web.web.controller.base.BaseController;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.core.Path;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;


/**
* 打印标签
*/
@Path("/goods/goods/printTag")
public class GoodsPrintTagController extends BaseController {

	@Inject
	private GoodsPrintTagService printTagService;

	/**
	* 首页
	*/
	@Permission(Permissions.goods_goods_printTag)
	public void index() {
		
	}

	/**
	* 列表
	*/
	@Permission(Permissions.goods_goods_printTag)
	public void list() {
		int pageNumber = getInt("pageNumber", 1);
		Page<GoodsPrintTag> page = printTagService.paginate(null, pageNumber, pageSize);
		setAttr("page", page);
	}

	/**
	* 查看
	*/
	@Permission(Permissions.goods_goods_printTag_show)
	public void show() {

		renderJson(Ret.ok());
	}

	/**
	* 新增
	*/
	@Permission(Permissions.goods_goods_printTag_create)
	@Before(Tx.class)
	public void create() {
		GoodsPrintTag goodsPrintTag = getModel(GoodsPrintTag.class, "", true);
		Ret ret = printTagService.create(goodsPrintTag);
		renderJson(ret);
	}


	/**
	* 删除
	*/
	@Permission(Permissions.goods_goods_printTag_delete)
	@Before(Tx.class)
	public void delete() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("ID不能为空"));
			return;
		}
		Ret ret = printTagService.delete(Arrays.asList(id));
		renderJson(ret);
	}

	
	/**
	* 显示打印模板
	*/
	@Permission(Permissions.goods_goods_printTag)
	@Before(Tx.class)
	public void showPrint() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("单据ID不能为空"));
			return;
		}
		GoodsPrintTag printTag = GoodsPrintTag.dao.findById(id);
		if(printTag == null) {
			renderError(404);
			return;
		}
		List<TenantPrintTemplate> templateList = printTag.getPrintTemplateList();
		setAttr("templateList", templateList);
		setAttr("printTag", printTag);
	}
	
	/**
	 * 打印或预览
	 */
	@Permission(Permissions.goods_goods_printTag)
	public void print() {
		Integer id = getInt("id");
		Integer templateId = getInt("template_id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("单据ID不能为空"));
			return;
		}
		GoodsPrintTag printTag = GoodsPrintTag.dao.findById(id);
		if(printTag == null) {
			renderError(404);
			return;
		}
		TenantPrintTemplate printTemplate;
		if(templateId == null || templateId <= 0) {
			printTemplate = printTag.getPrintDefaultTemplate();
		} else {
			printTemplate = TenantPrintTemplate.dao.findById(templateId);
		}
		if(printTemplate == null) {
			renderJson(Ret.fail("打印模板不存在，请先设置打印模板"));
			return;
		}
		Ret ret = Ret.ok();
		ret.set("tpl_content", printTemplate.getContent());
		ret.set("json_data", printTag.getPrintData());
		ret.set("order", printTag);
		
		renderJson(ret);
	}
	
	/**
	 * 更新打印次数
	 */
	@Permission(Permissions.goods_goods_printTag)
	@Before(Tx.class)
	public void updatePrint() {
		renderJson(Ret.ok());
	}


}