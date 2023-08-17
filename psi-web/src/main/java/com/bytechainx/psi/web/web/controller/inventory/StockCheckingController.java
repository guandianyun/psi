package com.bytechainx.psi.web.web.controller.inventory;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.bytechainx.psi.common.EnumConstant.AuditStatusEnum;
import com.bytechainx.psi.common.EnumConstant.OrderStatusEnum;
import com.bytechainx.psi.common.EnumConstant.TenantConfigEnum;
import com.bytechainx.psi.common.Permissions;
import com.bytechainx.psi.common.annotation.Permission;
import com.bytechainx.psi.common.dto.ConditionFilter;
import com.bytechainx.psi.common.dto.ConditionFilter.Operator;
import com.bytechainx.psi.common.dto.UserSession;
import com.bytechainx.psi.common.model.InventoryChecking;
import com.bytechainx.psi.common.model.InventoryCheckingGoods;
import com.bytechainx.psi.common.model.TenantAdmin;
import com.bytechainx.psi.common.model.TenantConfig;
import com.bytechainx.psi.common.model.TenantPrintTemplate;
import com.bytechainx.psi.common.service.goods.GoodsInfoService;
import com.bytechainx.psi.common.service.setting.TenantAdminService;
import com.bytechainx.psi.purchase.service.StockCheckingService;
import com.bytechainx.psi.web.epc.TraderEventProducer;
import com.bytechainx.psi.web.epc.event.inventory.StockCheckingEvent;
import com.bytechainx.psi.web.web.controller.base.BaseController;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.core.Path;
import com.jfinal.kit.Kv;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;


/**
* 盘点
*/
@Path("/inventory/stock/checking")
public class StockCheckingController extends BaseController {

	@Inject
	private StockCheckingService checkingService;
	@Inject
	private TenantAdminService adminService;
	@Inject
	private GoodsInfoService goodsInfoService;
	@Inject
	private TraderEventProducer traderEventProducer;

	/**
	* 首页
	*/
	@Permission(Permissions.inventory_stock_checking)
	public void index() {
		
	}

	/**
	* 列表
	*/
	@Permission(Permissions.inventory_stock_checking)
	public void list() {
		int pageNumber = getInt("pageNumber", 1);
		pageSize = getPageSize();
		Integer handlerId = getInt("handler_id");
		Boolean hideDisableFlag = getBoolean("hide_disable_flag"); // 隐藏作废单据
		Boolean hideProfitLossFlag = getBoolean("hide_profit_loss_flag", false); // 隐藏无盈亏单据
		String keyword = get("keyword");
		Kv condKv = Kv.create();
		
		TenantAdmin currentAdmin = getCurrentAdmin();
		UserSession session = getUserSession();
		if(session.hasOper(Permissions.sensitiveData_order_showStockOrder)) { // 有查看他人单据的权限
			if(handlerId != null && handlerId > 0) {
				condKv.set("handler_id", handlerId);
			}
		} else { // 没有查看他人单据权限，只查询自己的
			ConditionFilter filter = new ConditionFilter();
			filter.setOperator(Operator.more);
			filter.setValue(currentAdmin.getId());
			condKv.set("(handler_id = ? or make_man_id = ?)", filter);
		}
		
		condKv.set("order_code,remark", keyword); // 多字段模糊查询
		if(hideDisableFlag) {
			condKv.set("order_status", OrderStatusEnum.normal.getValue());
		} else if(getInt("order_status") != null){
			condKv.set("order_status", getInt("order_status"));
		} else {
			ConditionFilter filter = new ConditionFilter();
			filter.setOperator(Operator.neq);
			filter.setValue(OrderStatusEnum.draft.getValue());
			condKv.set("order_status", filter);
		}
		if(hideProfitLossFlag) {
			ConditionFilter filter = new ConditionFilter();
			filter.setOperator(Operator.neq);
			filter.setValue(0);
			condKv.set("check_number", filter);
		}
		String startTime = get("start_time");
		String endTime = get("end_time");
		
		Page<InventoryChecking> page = checkingService.paginate(startTime, endTime, condKv, pageNumber, pageSize);
		setAttr("page", page);
		setAttr("hideDisableFlag", hideDisableFlag);
		setAttr("hideProfitLossFlag", hideProfitLossFlag);
		setAttrCommon();
	}
	
	/**
	* 草稿单
	*/
	@Permission(Permissions.inventory_stock_checking)
	public void draftIndex() {
		draftList();
	}
	/**
	* 草稿单列表
	*/
	@Permission(Permissions.inventory_stock_checking)
	public void draftList() {
		int pageNumber = getInt("pageNumber", 1);
		pageSize = getPageSize();
		Kv condKv = Kv.create();
		condKv.set("make_man_id", getAdminId()); // 只显示当前用户的草稿单
		condKv.set("order_status", OrderStatusEnum.draft.getValue());
		String startTime = get("start_time");
		String endTime = get("end_time");
		
		Page<InventoryChecking> page = checkingService.paginate(startTime, endTime, condKv, pageNumber, pageSize);
		setAttr("page", page);
	}
	/**
	* 草稿单列表,JSON格式
	*/
	@Permission(Permissions.inventory_stock_checking)
	public void draftListByJson() {
		int pageNumber = getInt("pageNumber", 1);
		pageSize = getPageSize();
		Kv condKv = Kv.create();
		condKv.set("make_man_id", getAdminId()); // 只显示当前用户的草稿单
		condKv.set("order_status", OrderStatusEnum.draft.getValue());
		String startTime = get("start_time");
		String endTime = get("end_time");
		
		Page<InventoryChecking> page = checkingService.paginate(startTime, endTime, condKv, pageNumber, pageSize);
		renderJson(Ret.ok().set("data", page));
	}
	

	/**
	* 查看
	*/
	@Permission(Permissions.inventory_stock_checking_show)
	public void show() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderError(404);
			return;
		}
		InventoryChecking stockChecking = InventoryChecking.dao.findById(id);
		if(stockChecking == null) {
			renderError(404);
			return;
		}
		setAttr("stockChecking", stockChecking);
	}


	/**
	* 添加
	*/
	@Permission(Permissions.inventory_stock_checking_create)
	public void add() {
		Kv condKv = Kv.create();
		condKv.set("order_status", OrderStatusEnum.draft.getValue());
		condKv.set("make_man_id", getAdminId());
		Page<InventoryChecking> page = checkingService.paginate(null, null, condKv, 1, 1);
		
		setAttr("draftCount", page.getTotalRow()); // 草稿单数量
		setAttrCommon();
	}

	/**
	* 新增
	*/
	@Permission(Permissions.inventory_stock_checking_create)
	@Before(Tx.class)
	public void create() {
		InventoryChecking stockChecking = getModel(InventoryChecking.class, "", true);
		List<InventoryCheckingGoods> orderGoodList = new ArrayList<>();
		parserParams(stockChecking, orderGoodList);
		
		Ret ret = traderEventProducer.request(getAdminId(), new StockCheckingEvent("create"), stockChecking, orderGoodList);
		
		renderJson(ret);
	}



	/**
	* 编辑
	*/
	@Permission(Permissions.inventory_stock_checking_update)
	public void edit() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderError(404);
			return;
		}
		InventoryChecking stockChecking = InventoryChecking.dao.findById(id);
		if(stockChecking == null) {
			renderError(404);
			return;
		}
		setAttr("stockChecking", stockChecking);
		setAttrCommon();
		
	}
	
	/**
	* 编辑草稿单
	*/
	@Permission(Permissions.inventory_stock_checking_update)
	public void editDraft() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderError(404);
			return;
		}
		InventoryChecking stockChecking = InventoryChecking.dao.findById(id);
		if(stockChecking == null) {
			renderError(404);
			return;
		}
		Kv condKv = Kv.create();
		condKv.set("order_status", OrderStatusEnum.draft.getValue());
		condKv.set("make_man_id", getAdminId());
		Page<InventoryChecking> page = checkingService.paginate(null, null, condKv, 1, 1);
		
		setAttr("draftCount", page.getTotalRow()); // 草稿单数量
		setAttr("stockChecking", stockChecking);
		setAttrCommon();
		
	}
	
	/**
	* 修改
	*/
	@Permission(Permissions.inventory_stock_checking_update)
	public void update() {
		InventoryChecking stockChecking = getModel(InventoryChecking.class, "", true);
		List<InventoryCheckingGoods> orderGoodList = new ArrayList<>();
		
		parserParams(stockChecking, orderGoodList);
		
		Ret ret = traderEventProducer.request(getAdminId(), new StockCheckingEvent("update"), stockChecking, orderGoodList);
		
		renderJson(ret);
	}


	/**
	* 停用
	*/
	@Permission(Permissions.inventory_stock_checking_disable)
	public void disable() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("ID不能为空"));
			return;
		}
		List<Integer> ids = new ArrayList<>();
		ids.add(id);
		
		Ret ret = traderEventProducer.request(getAdminId(), new StockCheckingEvent("disable"), ids);
		
		renderJson(ret);
	}
	
	/**
	* 审核
	*/
	@Permission(Permissions.inventory_stock_checking_audit)
	public void audit() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("ID不能为空"));
			return;
		}
		setAttr("id", id);
	}
	
	/**
	* 审核
	*/
	@Permission(Permissions.inventory_stock_checking_audit)
	public void auditCreate() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("ID不能为空"));
			return;
		}
		List<Integer> ids = new ArrayList<>();
		ids.add(id);
		
		AuditStatusEnum auditStatus = AuditStatusEnum.getEnum(getInt("audit_status"));
		if(auditStatus == null) {
			renderJson(Ret.fail("审核状态不正确"));
			return;
		}
		String auditDesc = get("audit_desc");
		Ret ret = traderEventProducer.request(getAdminId(), new StockCheckingEvent("audit"), ids, auditStatus, auditDesc, getAdminId());
		
		renderJson(ret);
	}
	
	/**
	* 删除
	*/
	@Permission(Permissions.inventory_stock_checking_update)
	@Before(Tx.class)
	public void delete() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("ID不能为空"));
			return;
		}
		Ret ret = checkingService.delete(id);

		renderJson(ret);
	}
	
	private void parserParams(InventoryChecking stockChecking, List<InventoryCheckingGoods> orderGoodList) {
		Integer[] goodsInfoIds = getParaValuesToInt("goods_info_id");
		String[] checkNumbers = getParaValues("check_number");
		String[] currentNumbers = getParaValues("current_number");
		String[] goodsRemarks = getParaValues("goods_remark");
		String[] goodsSpecIds = getParaValues("goods_spec_id");
		Integer[] unitIds = getParaValuesToInt("unit_id");
		String[] goodsBatchs = getParaValues("goods_batch");
		String[] goodsQualitys = getParaValues("goods_quality");
		
		for (int index = 0; index < goodsInfoIds.length; index++) {
			if(goodsInfoIds[index] == null) {
				continue;
			}
			InventoryCheckingGoods goods = new InventoryCheckingGoods();
			if(goodsBatchs != null && goodsBatchs.length > 0) {
				goods.setBatch(goodsBatchs[index]);
			}
			if(goodsQualitys != null && goodsQualitys.length > 0) {
				goods.setQuality(goodsQualitys[index]);
			}
			goods.setCheckNumber(new BigDecimal(checkNumbers[index]));
			goods.setConvertNumber(BigDecimal.ZERO);
			goods.setGoodsInfoId(goodsInfoIds[index]);
			goods.setRemark(goodsRemarks[index]);
			goods.setUnitId(unitIds[index]);
			goods.setCurrentNumber(new BigDecimal(currentNumbers[index]));
			
			goods.setSpec1Id(0);
			goods.setSpecOption1Id(0);
			goods.setSpec2Id(0);
			goods.setSpecOption2Id(0);
			goods.setSpec3Id(0);
			goods.setSpecOption3Id(0);
			
			String specstring = goodsSpecIds[index];// 多个规格使用逗号隔开,格式：规格ID:规格值ID, 如：11:22|33:44
			String[] specList = StringUtils.split(specstring, "|");
			if(specList != null &&  specList.length > 0) {
				String[] spec = StringUtils.split(specList[0], ":"); 
				goods.setSpec1Id(Integer.parseInt((spec[0])));
				goods.setSpecOption1Id(Integer.parseInt((spec[1])));
				
			}
			if(specList != null && specList.length > 1) {
				String[] spec = StringUtils.split(specList[1], ":"); 
				goods.setSpec2Id(Integer.parseInt((spec[0])));
				goods.setSpecOption2Id(Integer.parseInt((spec[1])));
			}
			if(specList != null && specList.length > 2) {
				String[] spec = StringUtils.split(specList[2], ":"); 
				goods.setSpec3Id(Integer.parseInt((spec[0])));
				goods.setSpecOption3Id(Integer.parseInt((spec[1])));
			}
			orderGoodList.add(goods);
		}
		stockChecking.setOrderImg(StringUtils.join(getParaValues("order_imgs"), ","));
		stockChecking.setMakeManId(getAdminId());
		stockChecking.setLastManId(getAdminId());
	}
	
	/**
	 * 公共数据
	 */
	private void setAttrCommon() {
		JSONObject printConfirmConfig = TenantConfig.dao.findJsonByKeyCahce(TenantConfigEnum.create_order_print_confirm);
		setAttr("orderPrintConfig", printConfirmConfig);
		setAttr("orderAuditFlag", InventoryChecking.dao.findAuditConfig()); // 是否审核
		setAttr("orderRows", InventoryChecking.dao.findRowsConfig()); // 默认表格行数，后期要做成配置
	}

	/**
	* 显示打印模板
	*/
	@Permission(Permissions.inventory_stock_checking)
	@Before(Tx.class)
	public void showPrint() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("单据ID不能为空"));
			return;
		}
		InventoryChecking stockChecking = InventoryChecking.dao.findById(id);
		if(stockChecking == null) {
			renderError(404);
			return;
		}
		List<TenantPrintTemplate> templateList = stockChecking.getPrintTemplateList();
		setAttr("templateList", templateList);
		setAttr("stockChecking", stockChecking);
	}
	
	/**
	 * 打印或预览
	 */
	@Permission(Permissions.inventory_stock_checking)
	@Before(Tx.class)
	public void print() {
		Integer id = getInt("id");
		Integer templateId = getInt("template_id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("单据ID不能为空"));
			return;
		}
		InventoryChecking stockChecking = InventoryChecking.dao.findById(id);
		if(stockChecking == null) {
			renderError(404);
			return;
		}
		TenantPrintTemplate printTemplate;
		if(templateId == null || templateId <= 0) {
			printTemplate = stockChecking.getPrintDefaultTemplate();
		} else {
			printTemplate = TenantPrintTemplate.dao.findById(templateId);
		}
		if(printTemplate == null) {
			renderJson(Ret.fail("打印模板不存在，请先设置打印模板"));
			return;
		}
		Ret ret = Ret.ok();
		ret.set("tpl_content", printTemplate.getContent());
		ret.set("json_data", stockChecking.getPrintData());
		ret.set("order", stockChecking);
		
		renderJson(ret);
	}
	
	/**
	 * 更新打印次数
	 */
	@Permission(Permissions.inventory_stock_checking)
	@Before(Tx.class)
	public void updatePrint() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("单据ID不能为空"));
			return;
		}
		InventoryChecking stockChecking = InventoryChecking.dao.findById(id);
		if(stockChecking == null) {
			renderError(404);
			return;
		}
		stockChecking.setPrintCount(stockChecking.getPrintCount()+1);
		stockChecking.setUpdatedAt(new Date());
		stockChecking.update();
		renderJson(Ret.ok());
	}
	
	
}