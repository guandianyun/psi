package com.bytechainx.psi.sale.service;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.bytechainx.psi.common.EnumConstant.DataStatusEnum;
import com.bytechainx.psi.common.EnumConstant.FundFlowEnum;
import com.bytechainx.psi.common.kit.DateUtil;
import com.bytechainx.psi.common.kit.PinYinUtil;
import com.bytechainx.psi.common.model.CustomerInfo;
import com.bytechainx.psi.common.model.TenantAdmin;
import com.bytechainx.psi.common.model.TraderBookAccount;
import com.bytechainx.psi.common.model.TraderBookAccountLogs;
import com.bytechainx.psi.common.service.base.CommonService;
import com.jfinal.kit.Kv;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Page;


/**
* 客户管理
*/
public class CustomerInfoService extends CommonService {

	/**
	* 分页列表
	 * @param moreCondKv 非字段条件
	*/
	public Page<CustomerInfo> paginate(Kv conditionColumns, Kv moreCondKv, Integer pageNumber, int pageSize) {
		StringBuffer where = new StringBuffer();
		List<Object> params = new ArrayList<Object>();
		where.append("where 1 = 1 ");
		
		conditionFilter(conditionColumns, where, params);
		
		if(moreCondKv != null && moreCondKv.getBoolean("hide_debt_flag") != null && moreCondKv.getBoolean("hide_debt_flag")) { // 是否隐藏无欠款客户
			where.append(" and trader_book_account_id in (select id from trader_book_account where (out_amount - in_amount) > 0 )");
		}
		if(conditionColumns == null || !conditionColumns.containsKey("data_status")) {
			where.append(" and data_status != ?");
			params.add(DataStatusEnum.delete.getValue());
		}
		return CustomerInfo.dao.paginate(pageNumber, pageSize, "select * ", "from customer_info "+where.toString()+" order by id desc", params.toArray());
	}
	
	/**
	 * 
	 * @param tenantOrgId
	 * @param conditionColumns
	 * @param debtFlag 是否欠款
	 * @return
	 */
	public CustomerInfo sumCustomer(Kv conditionColumns, Boolean debtFlag, Date lastOrderTime) {
		StringBuffer where = new StringBuffer();
		List<Object> params = new ArrayList<Object>();
		where.append("where 1 = 1 ");
		
		conditionFilter(conditionColumns, where, params);
		
		if(debtFlag != null && debtFlag) { // 有欠款
			where.append(" and trader_book_account_id in (select id from trader_book_account where (out_amount - in_amount) < 0 )");
		} else if(debtFlag != null && !debtFlag) { // 有余款
			where.append(" and trader_book_account_id in (select id from trader_book_account where (out_amount - in_amount) > 0 )");
		}
		if(conditionColumns == null || !conditionColumns.containsKey("data_status")) {
			where.append(" and data_status != ?");
			params.add(DataStatusEnum.delete.getValue());
		}
		if(lastOrderTime != null) {
			where.append(" and last_order_time <= ?");
			params.add(DateUtil.getSecondStr(lastOrderTime));
		}
		return CustomerInfo.dao.findFirst("select count(*) as count from customer_info "+where.toString(), params.toArray());
	}
	

	/**
	* 新增
	 * @param adminId 
	*/
	public Ret create(Integer adminId, CustomerInfo customerInfo) {
		if(customerInfo == null) {
			return Ret.fail("参数错误");
		}
		if(StringUtils.isEmpty(customerInfo.getName())) {
			return Ret.fail("客户名称不能为空");
		}
		CustomerInfo _customerInfo = CustomerInfo.dao.findFirst("select * from customer_info where name = ? and data_status != ? limit 1", customerInfo.getName(), DataStatusEnum.delete.getValue());
		if(_customerInfo != null) {
			return Ret.fail("客户名称已存在");
		}
		
		// 创建往来帐户,客户欠款=支出-收入，欠供应商款=收入-支出。
		TraderBookAccount traderBookAccount = new TraderBookAccount();
		traderBookAccount.setInAmount(BigDecimal.ZERO);
		traderBookAccount.setOpenBalance(BigDecimal.ZERO);
		traderBookAccount.setOutAmount(BigDecimal.ZERO);
		traderBookAccount.setPayAmount(BigDecimal.ZERO);
		traderBookAccount.setCreatedAt(new Date());
		traderBookAccount.setUpdatedAt(new Date());
		traderBookAccount.save();
		
		customerInfo.setTraderBookAccountId(traderBookAccount.getId());
		customerInfo.setCode(PinYinUtil.getFirstSpell(customerInfo.getName()));
		customerInfo.setCreatedAt(new Date());
		customerInfo.setUpdatedAt(new Date());
		customerInfo.save();
		
		// 更新往来帐户
		BigDecimal openBalance = customerInfo.getBigDecimal("open_balance"); // 期初欠款
		updateTraderBookAccount(adminId, traderBookAccount, openBalance);
		
		return Ret.ok("新增客户成功").set("targetId", customerInfo.getId());
	}


	/**
	* 修改
	*/
	public Ret update(Integer adminId, CustomerInfo customerInfo) {
		if(customerInfo == null || customerInfo.getId() == null || customerInfo.getId() <= 0) {
			return Ret.fail("参数错误");
		}
		if(StringUtils.isEmpty(customerInfo.getName())) {
			return Ret.fail("客户名称不能为空");
		}
		CustomerInfo _customerInfo = CustomerInfo.dao.findFirst("select * from customer_info where name = ? and data_status != ? limit 1", customerInfo.getName(), DataStatusEnum.delete.getValue());
		if(_customerInfo != null && _customerInfo.getId().intValue() != customerInfo.getId().intValue()) {
			return Ret.fail("客户名称已存在");
		}
		_customerInfo = CustomerInfo.dao.findById(customerInfo.getId());
		if(_customerInfo == null) {
			return Ret.fail("客户不存在，无法修改");
		}
		customerInfo.setCode(PinYinUtil.getFirstSpell(customerInfo.getName()));
		customerInfo.setUpdatedAt(new Date());
		customerInfo.update();
		
		// 更新往来帐户
		BigDecimal openBalance = customerInfo.getBigDecimal("open_balance"); // 期初欠款
		updateTraderBookAccount(adminId,  _customerInfo.getTraderBookAccount(), openBalance);
		
		return Ret.ok("修改客户成功");
	}


	/**
	* 删除
	*/
	public Ret delete(List<Integer> ids) {
		if(ids == null || ids.isEmpty()) {
			return Ret.fail("参数错误");
		}
		for (Integer id : ids) {
			CustomerInfo customerInfo = CustomerInfo.dao.findById(id);
			if(customerInfo == null) {
				continue;
			}
			customerInfo.setDataStatus(DataStatusEnum.delete.getValue());
			customerInfo.setUpdatedAt(new Date());
			customerInfo.update();
		}
		
		return Ret.ok("删除客户成功");
	}
	
	/**
	* 停用
	*/
	public Ret disable(List<Integer> ids) {
		if(ids == null || ids.isEmpty()) {
			return Ret.fail("参数错误");
		}
		for (Integer id : ids) {
			CustomerInfo customerInfo = CustomerInfo.dao.findById(id);
			if(customerInfo == null) {
				continue;
			}
			customerInfo.setDataStatus(DataStatusEnum.disable.getValue());
			customerInfo.setUpdatedAt(new Date());
			customerInfo.update();
		}
		return Ret.ok("停用客户成功");
	}

	
	/**
	* 启用
	*/
	public Ret enable(List<Integer> ids) {
		if(ids == null || ids.isEmpty()) {
			return Ret.fail("参数错误");
		}
		for (Integer id : ids) {
			CustomerInfo customerInfo = CustomerInfo.dao.findById(id);
			if(customerInfo == null) {
				continue;
			}
			customerInfo.setDataStatus(DataStatusEnum.enable.getValue());
			customerInfo.setUpdatedAt(new Date());
			customerInfo.update();
		}
		return Ret.ok("启用客户成功");
	}

	/**
	 * 更新往来帐户资金
	 * @param tenantOrgId
	 * @param adminId 
	 * @param order
	 * @param changeAmount
	 * @return 
	 */
	private Ret updateTraderBookAccount(Integer adminId, TraderBookAccount traderBookAccount , BigDecimal openBalance) {
		if(openBalance == null) {
			return Ret.fail("期初金额不能为空");
		}
		BigDecimal oldOpenBalance = traderBookAccount.getOpenBalance();
		if(oldOpenBalance.compareTo(openBalance) == 0) {
			return Ret.fail("期初金额未发生变化");
		}
		// 修改往来帐户,客户欠款=支出-收入，欠供应商款=收入-支出。
		// 先回退老的期初欠款
		if (oldOpenBalance.compareTo(BigDecimal.ZERO) > 0) { // 客户有欠款
			BigDecimal outAmount = traderBookAccount.getOutAmount().subtract(oldOpenBalance);
			traderBookAccount.setOutAmount(outAmount);
			
		} else if (oldOpenBalance.compareTo(BigDecimal.ZERO) < 0) { // 负数，客户有预存款
			BigDecimal inAmount = traderBookAccount.getInAmount().subtract(BigDecimal.ZERO.subtract(oldOpenBalance));
			traderBookAccount.setInAmount(inAmount);
		}
		// 再处理新的期初欠款
		if (openBalance.compareTo(BigDecimal.ZERO) > 0) { // 客户有欠款
			BigDecimal outAmount = traderBookAccount.getOutAmount().add(openBalance);
			traderBookAccount.setOutAmount(outAmount);
			
		} else if (openBalance.compareTo(BigDecimal.ZERO) < 0) { // 负数，客户有预存款
			BigDecimal inAmount = traderBookAccount.getInAmount().add(BigDecimal.ZERO.subtract(openBalance));
			traderBookAccount.setInAmount(inAmount);
		}
		
		traderBookAccount.setOpenBalance(openBalance);
		traderBookAccount.setUpdatedAt(new Date());
		traderBookAccount.update();
		
		BigDecimal balance = traderBookAccount.getCustomerDebtAmount(); // 客户欠款=支出-收入
		
		// 记录往来帐户出入金日志表
		TraderBookAccountLogs bookAccountLogs = new TraderBookAccountLogs();
		bookAccountLogs.setAmount(openBalance);
		bookAccountLogs.setBalance(balance);
		bookAccountLogs.setCreatedAt(new Date());
		bookAccountLogs.setFundFlow(FundFlowEnum.adjust.getValue());
		bookAccountLogs.setTraderBookAccountId(traderBookAccount.getId());
		TenantAdmin admin = TenantAdmin.dao.findById(adminId);
		bookAccountLogs.setRemark(admin.getRealName()+" 调整期初");
		bookAccountLogs.save();
		
		return Ret.ok("调整期初金额成功");
	}


	/**
	* 导入
	*/
	public Ret createImport() {

		return Ret.ok();
	}

	/**
	 * 期初调整
	 * @param tenantOrgId
	 * @param customerInfoId 
	 * @param customerInfoIdObject
	 * @param openBalanceObject
	 * @return
	 */
	public Ret updateOpenBalance(Integer adminId, Integer customerInfoId, BigDecimal openBalance) {
		if(customerInfoId == null || customerInfoId <= 0) {
			return Ret.fail("客户不能为空");
		}
		if(openBalance == null) {
			return Ret.fail("期初金额不能为空");
		}
		CustomerInfo customerInfo = CustomerInfo.dao.findById(customerInfoId);
		Ret ret = updateTraderBookAccount(adminId, customerInfo.getTraderBookAccount(), openBalance);
		return ret;
	}

}