package com.bytechainx.psi.web.web.controller.sale;


import java.util.Arrays;

import com.bytechainx.psi.common.EnumConstant.AuditStatusEnum;
import com.bytechainx.psi.common.EnumConstant.DataStatusEnum;
import com.bytechainx.psi.common.EnumConstant.OrderStatusEnum;
import com.bytechainx.psi.common.Permissions;
import com.bytechainx.psi.common.annotation.Permission;
import com.bytechainx.psi.common.api.TraderCenterApi;
import com.bytechainx.psi.common.dto.ConditionFilter;
import com.bytechainx.psi.common.dto.ConditionFilter.Operator;
import com.bytechainx.psi.common.dto.UserSession;
import com.bytechainx.psi.common.kit.DateUtil;
import com.bytechainx.psi.common.model.CustomerCategory;
import com.bytechainx.psi.common.model.CustomerInfo;
import com.bytechainx.psi.common.model.CustomerPriceLevel;
import com.bytechainx.psi.common.model.SaleOrder;
import com.bytechainx.psi.common.model.TenantAdmin;
import com.bytechainx.psi.sale.service.CustomerCategoryService;
import com.bytechainx.psi.sale.service.CustomerInfoService;
import com.bytechainx.psi.sale.service.CustomerPriceLevelService;
import com.bytechainx.psi.sale.service.SaleOrderService;
import com.bytechainx.psi.web.web.controller.base.BaseController;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.core.Path;
import com.jfinal.kit.Kv;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;


/**
* 客户管理
*/
@Path("/sale/customer/info")
public class CustomerInfoController extends BaseController {

	@Inject
	private CustomerInfoService customerInfoService;
	@Inject
	private CustomerCategoryService customerCategoryService;
	@Inject
	private CustomerPriceLevelService customerPriceLevelService;
	@Inject
	private SaleOrderService saleOrderService;

	/**
	* 首页
	*/
	@Permission(Permissions.sale_customer_info)
	public void index() {
		setAttrCommon();
	}

	/**
	* 列表
	*/
	@Permission(Permissions.sale_customer_info)
	public void list() {
		int pageNumber = getInt("pageNumber", 1);
		Integer priceLevelId = getInt("customer_price_level_id");
		Integer customerCategoryId = getInt("customer_category_id");
		Boolean hideStopFlag = getBoolean("hide_stop_flag"); // 隐藏停用客户
		Boolean hideDebtFlag = getBoolean("hide_debt_flag", false); // 隐藏无欠款客户
		String keyword = get("keyword");
		Kv condKv = Kv.create();
		conditionFilterStore(condKv, Permissions.sale_customer); // 添加门店过滤条件
		condKv.set("customer_price_level_id", priceLevelId);
		condKv.set("customer_category_id", customerCategoryId);
		condKv.set("name,contact,mobile,remark", keyword); // 多字段模糊查询
		
		if(hideStopFlag) {
			condKv.set("data_status", DataStatusEnum.enable.getValue());
		}
		Kv moreCondKv = Kv.create();
		moreCondKv.set("hide_debt_flag", hideDebtFlag);
		
		Page<CustomerInfo> page = customerInfoService.paginate(condKv, moreCondKv, pageNumber, pageSize);
		
		CustomerInfo totalSum = customerInfoService.sumCustomer(condKv, null, null); // 总客户
		CustomerInfo debtSum = customerInfoService.sumCustomer(condKv, true, null); // 欠款客户
		CustomerInfo balanceSum = customerInfoService.sumCustomer(condKv, false, null); // 余款客户
		CustomerInfo monthSum = customerInfoService.sumCustomer(condKv, null, DateUtil.daysAddOrSubNow(-30)); // 30天内未开单
		CustomerInfo halfYearSum = customerInfoService.sumCustomer(condKv, null, DateUtil.daysAddOrSubNow(-180)); // 半年内未开单
		
		setAttr("page", page);
		setAttr("hideStopFlag", hideStopFlag);
		setAttr("hideDebtFlag", hideDebtFlag);
		setAttr("totalSum", totalSum.getLong("count"));
		setAttr("debtSum", debtSum.getLong("count"));
		setAttr("balanceSum", balanceSum.getLong("count"));
		setAttr("monthSum", monthSum.getLong("count"));
		setAttr("halfYearSum", halfYearSum.getLong("count"));
		
	}

	/**
	 * 查询客户
	 */
	@Permission({Permissions.sale_sale, Permissions.fund_book_receiptOrder})
	public void listByJson() {
		int pageNumber = getInt("pageNumber", 1);
		String keyword = get("keyword");
		Kv condKv = Kv.create();
		TenantAdmin currentAdmin = getCurrentAdmin();
		UserSession session = getUserSession();
		if(!session.hasOper(Permissions.sensitiveData_customer_showSaleman)) { // 没有查看他人的权限
			ConditionFilter filter = new ConditionFilter();
			filter.setOperator(Operator.more);
			filter.setValue(currentAdmin.getId());
			condKv.set("(handler_id = ? or make_man_id = ?)", filter);
		}
		condKv.set("data_status", DataStatusEnum.enable.getValue());
		condKv.set("name,code", keyword); // 多字段模糊查询
		Page<CustomerInfo> page = customerInfoService.paginate(condKv, null, pageNumber, maxPageSize);
		for (CustomerInfo customer : page.getList()) {
			customer.put("debtAmount", customer.getDebtAmount());
			customer.put("availableAmount", customer.getAvailableAmount());
		}
		renderJson(Ret.ok().set("data", page.getList()));
	}

	/**
	* 查看
	*/
	@Permission(Permissions.sale_customer_info_show)
	public void show() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderError(404);
			return;
		}
		CustomerInfo info = CustomerInfo.dao.findById(id);
		if(info == null) {
			renderError(404);
			return;
		}
		Kv saleOrderCondKv = Kv.create();
		saleOrderCondKv.set("customer_info_id", id);
		saleOrderCondKv.set("order_status", OrderStatusEnum.normal.getValue());
		saleOrderCondKv.set("audit_status", AuditStatusEnum.pass.getValue());
		Page<SaleOrder> saleOrderPage = saleOrderService.paginate(null, null, saleOrderCondKv, 1, 10);
		
		setAttr("saleOrderPage", saleOrderPage);
		setAttr("customerInfo", info);
	}


	/**
	* 添加
	*/
	@Permission(Permissions.sale_customer_info_create)
	public void add() {
		setAttrCommon();
	}

	/**
	* 新增
	*/
	@Permission(Permissions.sale_customer_info_create)
	public void create() {
		String responseJson = TraderCenterApi.requestApi("/sale/customer/info/create", getAdminId(), getParaMap());
		renderJson(responseJson);
	}



	/**
	* 编辑
	*/
	@Permission(Permissions.sale_customer_info_update)
	public void edit() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderError(404);
			return;
		}
		CustomerInfo info = CustomerInfo.dao.findById(id);
		if(info == null) {
			renderError(404);
			return;
		}
		setAttr("customerInfo", info);
		setAttrCommon();
		
	}

	/**
	* 修改
	*/
	@Permission(Permissions.sale_customer_info_update)
	public void update() {
		String responseJson = TraderCenterApi.requestApi("/sale/customer/info/update", getAdminId(), getParaMap());
		renderJson(responseJson);
	}



	/**
	* 删除
	*/
	@Permission(Permissions.sale_customer_info_delete)
	@Before(Tx.class)
	public void delete() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("ID不能为空"));
			return;
		}
		Ret ret = customerInfoService.delete(Arrays.asList(id));
		renderJson(ret);
	}



	/**
	* 停用
	*/
	@Permission(Permissions.sale_customer_info_disable)
	@Before(Tx.class)
	public void disable() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("ID不能为空"));
			return;
		}
		Ret ret = customerInfoService.disable(Arrays.asList(id));

		renderJson(ret);
	}
	
	/**
	* 启用
	*/
	@Permission(Permissions.sale_customer_info_disable)
	@Before(Tx.class)
	public void enable() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("ID不能为空"));
			return;
		}
		Ret ret = customerInfoService.enable(Arrays.asList(id));

		renderJson(ret);
	}



	/**
	* 导入
	*/
	@Permission(Permissions.sale_customer_info_createImport)
	public void createImport() {

		renderJson(Ret.ok());
	}
	
	/**
	 * 设置公共数据
	 */
	private void setAttrCommon() {
		Kv priceLevelCondKv = Kv.create();
		priceLevelCondKv.set("data_status", DataStatusEnum.enable.getValue());
		Page<CustomerPriceLevel> priceLevelPage = customerPriceLevelService.paginate(priceLevelCondKv, 1, maxPageSize);
		
		Page<CustomerCategory> customerCategoryPage = customerCategoryService.paginate(1, maxPageSize);
		
		setAttr("priceLevelPage", priceLevelPage);
		setAttr("customerCategoryPage", customerCategoryPage);
	}

}