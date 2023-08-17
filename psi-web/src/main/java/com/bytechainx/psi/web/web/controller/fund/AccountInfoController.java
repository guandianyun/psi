package com.bytechainx.psi.web.web.controller.fund;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.bytechainx.psi.common.EnumConstant.DataStatusEnum;
import com.bytechainx.psi.common.Permissions;
import com.bytechainx.psi.common.annotation.Permission;
import com.bytechainx.psi.common.model.TraderBalanceAccount;
import com.bytechainx.psi.fund.service.AccountInfoService;
import com.bytechainx.psi.web.epc.TraderEventProducer;
import com.bytechainx.psi.web.epc.event.fund.AccountInfoEvent;
import com.bytechainx.psi.web.web.controller.base.BaseController;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.core.NotAction;
import com.jfinal.core.Path;
import com.jfinal.kit.Kv;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;


/**
* 结算帐户
*/
@Path("/fund/account/info")
public class AccountInfoController extends BaseController {

	@Inject
	private AccountInfoService accountInfoService;
	@Inject
	private TraderEventProducer traderEventProducer;

	/**
	* 首页
	*/
	@Permission(Permissions.fund_account_info)
	public void index() {
		setAttr("hideStopFlag", true);
	}

	/**
	* 列表
	*/
	@Permission(Permissions.fund_account_info)
	public void list() {
		int pageNumber = getInt("pageNumber", 1);
		Boolean hideStopFlag = getBoolean("hide_stop_flag", true); // 隐藏停用结算帐户
		Kv condKv = Kv.create();
		if(hideStopFlag) {
			condKv.set("data_status", DataStatusEnum.enable.getValue());
		}
		Page<TraderBalanceAccount> page = accountInfoService.paginate(condKv, pageNumber, pageSize);
		setAttr("page", page);
	}
	
	@Permission(Permissions.fund_account_info)
	public void optionList() {
		Kv condKv = Kv.create();
		condKv.set("data_status", DataStatusEnum.enable.getValue());
		Page<TraderBalanceAccount> page = accountInfoService.paginate(condKv, 1, maxPageSize);
		setAttr("page", page);
	}

	/**
	* 查看
	*/
	@Permission(Permissions.fund_account_info_show)
	public void show() {

		renderJson(Ret.ok());
	}


	/**
	* 添加
	*/
	@Permission(Permissions.fund_account_info_create)
	public void add() {
		setAttrCommon();
		setAttr("sourcePage", get("sourcePage"));
	}

	/**
	* 新增
	*/
	@Permission(Permissions.fund_account_info_create)
	public void create() {
		TraderBalanceAccount balanceAccount = getModel(TraderBalanceAccount.class, "", true);
		
		Ret ret = traderEventProducer.request(getAdminId(), new AccountInfoEvent("create"), balanceAccount);
		renderJson(ret);
	}

	/**
	* 编辑
	*/
	@Permission(Permissions.fund_account_info_update)
	public void edit() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderError(404);
			return;
		}
		TraderBalanceAccount balanceAccount = TraderBalanceAccount.dao.findById(id);
		if(balanceAccount == null) {
			renderError(404);
			return;
		}
		setAttrCommon();
		setAttr("accountInfo", balanceAccount);
	}

	/**
	* 修改
	*/
	@Permission(Permissions.fund_account_info_update)
	public void update() {
		TraderBalanceAccount balanceAccount = getModel(TraderBalanceAccount.class, "", true);
		
		Ret ret = traderEventProducer.request(getAdminId(), new AccountInfoEvent("update"), balanceAccount);
		renderJson(ret);
	}


	/**
	* 删除
	*/
	@Permission(Permissions.fund_account_info_delete)
	@Before(Tx.class)
	public void delete() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("ID不能为空"));
			return;
		}
		Ret ret = accountInfoService.delete(Arrays.asList(id));
		renderJson(ret);
	}
	
	/**
	* 停用
	*/
	@Permission(Permissions.fund_account_info_disable)
	@Before(Tx.class)
	public void disable() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("ID不能为空"));
			return;
		}
		Ret ret = accountInfoService.disable(Arrays.asList(id));

		renderJson(ret);
	}
	
	/**
	* 启用
	*/
	@Permission(Permissions.fund_account_info_disable)
	@Before(Tx.class)
	public void enable() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("ID不能为空"));
			return;
		}
		Ret ret = accountInfoService.enable(Arrays.asList(id));

		renderJson(ret);
	}
	
	/**
	* 结算帐户
	*/
	@Permission({Permissions.fund_account, Permissions.inventory_purchase, Permissions.sale_sale})
	public void listByJson() {
		Integer storeId = getInt("tenant_store_id"); // 只查询门店关联的结算帐户
		String keyword = get("keyword");
		Boolean payFlag = getBoolean("pay_flag"); // 是否帐户支付，为了区分小余额的使用类型，支付就是 扣款，收入就是存款
		BigDecimal accountBalance = new BigDecimal(get("account_balance", "0"));
		Kv condKv = Kv.create();
		condKv.set("data_status", DataStatusEnum.enable.getValue());
		condKv.set("name,code", keyword);
		Page<TraderBalanceAccount> page = accountInfoService.paginate(condKv, 1, maxPageSize);
		if (storeId == null || storeId == 0) {
			appendAccount(accountBalance, page.getList(), keyword, payFlag);

			renderJson(Ret.ok().set("data", page.getList()));
			return;
		}
		List<TraderBalanceAccount> accountList = new ArrayList<>();
		for (TraderBalanceAccount account : page.getList()) {
			accountList.add(account);
		}
		appendAccount(accountBalance, accountList, keyword, payFlag);
		renderJson(Ret.ok().set("data", accountList));
	}

	/**
	 * @param accountBalance
	 * @param accountList
	 * @param keyword 
	 * @return 
	 */
	private void appendAccount(BigDecimal accountBalance, List<TraderBalanceAccount> accountList, String keyword, Boolean payFlag) {
		if(payFlag == null) {
			return;
		}
		TraderBalanceAccount account = new TraderBalanceAccount();
		if(payFlag) {
			if(accountBalance.compareTo(BigDecimal.ZERO) <= 0) {
				return;
			}
			account.setName("余额扣款(￥" + accountBalance.toPlainString() + ")");
		} else {
			account.setName("存入余额(￥" + accountBalance.toPlainString() + ")");
		}
		account.setId(0);
		if(StringUtils.isNotEmpty(keyword) && !account.getName().contains(keyword)) {
			return;
		}
		if (accountBalance.compareTo(BigDecimal.ZERO) > 0) {
			accountList.add(0, account);
		} else {
			accountList.add(account);
		}
	}
	
	/**
	 * 加载公共数据
	 */
	@NotAction
	private void setAttrCommon() {
		
	}
	
}