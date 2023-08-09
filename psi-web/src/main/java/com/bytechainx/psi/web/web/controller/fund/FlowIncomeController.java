package com.bytechainx.psi.web.web.controller.fund;


import com.bytechainx.psi.common.Permissions;
import com.bytechainx.psi.common.EnumConstant.AuditStatusEnum;
import com.bytechainx.psi.common.EnumConstant.FundFlowEnum;
import com.bytechainx.psi.common.EnumConstant.OrderStatusEnum;
import com.bytechainx.psi.common.annotation.Permission;
import com.bytechainx.psi.common.api.TraderCenterApi;
import com.bytechainx.psi.common.dto.ConditionFilter;
import com.bytechainx.psi.common.dto.ConditionFilter.Operator;
import com.bytechainx.psi.common.model.TraderIncomeExpenses;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.core.Path;
import com.jfinal.kit.Kv;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.bytechainx.psi.fund.service.FlowIncomeService;
import com.bytechainx.psi.web.web.controller.base.BaseController;


/**
* 收入单据
*/
@Path("/fund/flow/income")
public class FlowIncomeController extends BaseController {

	@Inject
	private FlowIncomeService incomeService;

	/**
	* 首页
	*/
	@Permission(Permissions.fund_flow_income)
	public void index() {
		
	}

	/**
	* 列表
	*/
	@Permission(Permissions.fund_flow_income)
	public void list() {
		int pageNumber = getInt("pageNumber", 1);
		pageSize = getPageSize();
		Kv condKv = conditions();
		
		String startTime = get("start_time");
		String endTime = get("end_time");
		
		Page<TraderIncomeExpenses> page = incomeService.paginate(startTime, endTime, condKv, pageNumber, pageSize);
		setAttr("page", page);
		setAttr("hideDisableFlag", getBoolean("hide_disable_flag"));
		setAttr("showAuditFlag", getBoolean("show_audit_flag"));
		setAttrCommon();
	}

	/**
	* 草稿单
	*/
	@Permission(Permissions.fund_flow_income)
	public void draftIndex() {
		draftList();
	}
	/**
	* 草稿单列表
	*/
	@Permission(Permissions.fund_flow_income)
	public void draftList() {
		int pageNumber = getInt("pageNumber", 1);
		pageSize = getPageSize();
		Kv condKv = Kv.create();
		condKv.set("make_man_id", getAdminId()); // 只显示当前用户的草稿单
		condKv.set("fund_flow", FundFlowEnum.income.getValue());
		condKv.set("order_status", OrderStatusEnum.draft.getValue());
		String startTime = get("start_time");
		String endTime = get("end_time");
		
		Page<TraderIncomeExpenses> page = incomeService.paginate(startTime, endTime, condKv, pageNumber, pageSize);
		setAttr("page", page);
	}
	/**
	* 草稿单列表,JSON格式
	*/
	@Permission(Permissions.fund_flow_income)
	public void draftListByJson() {
		int pageNumber = getInt("pageNumber", 1);
		pageSize = getPageSize();
		Integer tenantStoreId = getInt("tenant_store_id");
		Kv condKv = Kv.create();
		condKv.set("tenant_store_id", tenantStoreId);
		condKv.set("fund_flow", FundFlowEnum.income.getValue());
		condKv.set("make_man_id", getAdminId()); // 只显示当前用户的草稿单
		condKv.set("order_status", OrderStatusEnum.draft.getValue());
		String startTime = get("start_time");
		String endTime = get("end_time");
		
		Page<TraderIncomeExpenses> page = incomeService.paginate(startTime, endTime, condKv, pageNumber, pageSize);
		renderJson(Ret.ok().set("data", page));
	}
	

	/**
	* 查看
	*/
	@Permission(Permissions.fund_flow_income_show)
	public void show() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderError(404);
			return;
		}
		TraderIncomeExpenses flowIncome = TraderIncomeExpenses.dao.findById(id);
		if(flowIncome == null) {
			renderError(404);
			return;
		}
		setAttrCommon();
		setAttr("flowIncome", flowIncome);
	}
	

	/**
	* 添加
	*/
	@Permission(Permissions.fund_flow_income_create)
	public void add() {
		Kv condKv = Kv.create();
		condKv.set("order_status", OrderStatusEnum.draft.getValue());
		condKv.set("fund_flow", FundFlowEnum.income.getValue());
		condKv.set("make_man_id", getAdminId());
		Page<TraderIncomeExpenses> page = incomeService.paginate(null, null, condKv, 1, 1);
		
		setAttr("draftCount", page.getTotalRow()); // 草稿单数量
		setAttrCommon();
		
	}

	/**
	* 新增
	*/
	@Permission(Permissions.fund_flow_income_create)
	public void create() {
		String responseJson = TraderCenterApi.requestApi("/fund/flow/income/create", getAdminId(), getParaMap());
		renderJson(responseJson);
	}

	/**
	* 编辑
	*/
	@Permission(Permissions.fund_flow_income_update)
	public void edit() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderError(404);
			return;
		}
		TraderIncomeExpenses flowIncome = TraderIncomeExpenses.dao.findById(id);
		if(flowIncome == null) {
			renderError(404);
			return;
		}
		setAttr("flowIncome", flowIncome);
		setAttrCommon();
		
	}
	
	/**
	* 编辑草稿单
	*/
	@Permission(Permissions.fund_flow_income_update)
	public void editDraft() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderError(404);
			return;
		}
		TraderIncomeExpenses flowIncome = TraderIncomeExpenses.dao.findById(id);
		if(flowIncome == null) {
			renderError(404);
			return;
		}
		Kv condKv = Kv.create();
		condKv.set("order_status", OrderStatusEnum.draft.getValue());
		condKv.set("fund_flow", FundFlowEnum.income.getValue());
		condKv.set("make_man_id", getAdminId());
		Page<TraderIncomeExpenses> page = incomeService.paginate(null, null, condKv, 1, 1);
		
		setAttr("draftCount", page.getTotalRow()); // 草稿单数量
		setAttr("flowIncome", flowIncome);
		setAttrCommon();
		
	}
	
	/**
	* 修改
	*/
	@Permission(Permissions.fund_flow_income_update)
	public void update() {
		String responseJson = TraderCenterApi.requestApi("/fund/flow/income/update", getAdminId(), getParaMap());
		renderJson(responseJson);
	}


	/**
	* 停用
	*/
	@Permission(Permissions.fund_flow_income_disable)
	public void disable() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("ID不能为空"));
			return;
		}
		String responseJson = TraderCenterApi.requestApi("/fund/flow/income/disable", getAdminId(), getParaMap());
		renderJson(responseJson);
	}
	
	/**
	* 审核
	*/
	@Permission(Permissions.fund_flow_income_audit)
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
	@Permission(Permissions.fund_flow_income_audit)
	public void auditCreate() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("ID不能为空"));
			return;
		}
		String responseJson = TraderCenterApi.requestApi("/fund/flow/income/audit", getAdminId(), getParaMap());
		renderJson(responseJson);
	}
	
	/**
	* 删除
	*/
	@Permission(Permissions.fund_flow_income_update)
	@Before(Tx.class)
	public void delete() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("ID不能为空"));
			return;
		}
		Ret ret = incomeService.delete(id);

		renderJson(ret);
	}
	
	/**
	 * 公共数据
	 */
	private void setAttrCommon() {
		setAttr("orderAuditFlag", TraderIncomeExpenses.dao.findAuditConfig()); // 是否审核
	}
	
	/**
	 * 导出
	 */
	@Permission(Permissions.fund_flow_income_export)
	public void export() {
		Kv condKv = conditions();
		String startTime = get("start_time");
		String endTime = get("end_time");
		Ret ret = incomeService.export(getAdminId(), startTime, endTime, condKv);
		renderJson(ret);
	}
	
	/**
	 * 查询条件
	 * @return
	 */
	private Kv conditions() {
		Integer handlerId = getInt("handler_id");
		Integer fundTypeId = getInt("fund_type_id");
		Boolean hideDisableFlag = getBoolean("hide_disable_flag"); // 隐藏作废单据
		Boolean showAuditFlag = getBoolean("show_audit_flag"); // 只显示待审核单据
		
		String keyword = get("keyword");
		Kv condKv = Kv.create();
		conditionFilterStore(condKv, Permissions.fund_flow); // 添加门店过滤条件
		condKv.set("handler_id", handlerId);
		condKv.set("fund_flow", FundFlowEnum.income.getValue());
		condKv.set("fund_type_id", fundTypeId);
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
		if(showAuditFlag) {
			condKv.set("audit_status", AuditStatusEnum.waiting.getValue());
		}
		return condKv;
	}
	
}