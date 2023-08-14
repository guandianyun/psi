package com.bytechainx.psi.purchase.service;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.bytechainx.psi.common.EnumConstant.DataStatusEnum;
import com.bytechainx.psi.common.EnumConstant.FundFlowEnum;
import com.bytechainx.psi.common.kit.PinYinUtil;
import com.bytechainx.psi.common.model.SupplierInfo;
import com.bytechainx.psi.common.model.TenantAdmin;
import com.bytechainx.psi.common.model.TraderBookAccount;
import com.bytechainx.psi.common.model.TraderBookAccountLogs;
import com.bytechainx.psi.common.service.base.CommonService;
import com.jfinal.kit.Kv;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Page;


/**
* 供应商管理
*/
public class SupplierInfoService extends CommonService {

	/**
	* 分页列表
	 * @param moreCondKv 
	*/
	public Page<SupplierInfo> paginate(Kv conditionColumns, Kv moreCondKv, Integer pageNumber, int pageSize) {
		StringBuffer where = new StringBuffer();
		List<Object> params = new ArrayList<Object>();
		where.append("where 1 = 1 ");

		conditionFilter(conditionColumns, where, params);
		
		if(moreCondKv != null) {
			if(moreCondKv.getBoolean("hide_debt_flag") != null && moreCondKv.getBoolean("hide_debt_flag")) { // 是否隐藏无欠款客户
				where.append(" and trader_book_account_id in (select id from trader_book_account where (in_amount - out_amount) > 0 )");
			}
		}
		if(conditionColumns == null || !conditionColumns.containsKey("data_status")) {
			where.append(" and data_status != ?");
			params.add(DataStatusEnum.delete.getValue());
		}
		
		
		return SupplierInfo.dao.paginate(pageNumber, pageSize, "select * ", "from supplier_info "+where.toString()+" order by id desc", params.toArray());
	}

	/**
	* 新增
	*/
	public Ret create(Integer adminId, SupplierInfo supplierInfo) {
		if(supplierInfo == null) {
			return Ret.fail("参数错误");
		}
		if(StringUtils.isEmpty(supplierInfo.getName())) {
			return Ret.fail("供应商名称不能为空");
		}
		SupplierInfo _supplierInfo = SupplierInfo.dao.findFirst("select * from supplier_info where name = ? and data_status != ? limit 1", supplierInfo.getName(), DataStatusEnum.delete.getValue());
		if(_supplierInfo != null) {
			return Ret.fail("供应商名称已存在");
		}
		// 创建往来帐户
		TraderBookAccount traderBookAccount = new TraderBookAccount();
		traderBookAccount.setCreatedAt(new Date());
		traderBookAccount.setInAmount(BigDecimal.ZERO);
		traderBookAccount.setOpenBalance(BigDecimal.ZERO);
		traderBookAccount.setOutAmount(BigDecimal.ZERO);
		traderBookAccount.setPayAmount(BigDecimal.ZERO);
		traderBookAccount.setUpdatedAt(new Date());
		traderBookAccount.save();
		
		supplierInfo.setTraderBookAccountId(traderBookAccount.getId());
		supplierInfo.setCode(PinYinUtil.getFirstSpell(supplierInfo.getName()));
		supplierInfo.setCreatedAt(new Date());
		supplierInfo.setUpdatedAt(new Date());
		supplierInfo.save();
		
		// 更新往来帐户
		BigDecimal openBalance = supplierInfo.getBigDecimal("open_balance"); // 期初欠款
		updateTraderBookAccount(adminId, traderBookAccount, openBalance);
		
		return Ret.ok("新增供应商成功").set("targetId", supplierInfo.getId());
	}


	/**
	* 修改
	*/
	public Ret update(Integer adminId, SupplierInfo supplierInfo) {
		if(supplierInfo == null || supplierInfo.getId() == null || supplierInfo.getId() <= 0) {
			return Ret.fail("参数错误");
		}
		if(StringUtils.isEmpty(supplierInfo.getName())) {
			return Ret.fail("供应商名称不能为空");
		}
		SupplierInfo _supplierInfo = SupplierInfo.dao.findFirst("select * from supplier_info where name = ? and data_status != ? limit 1", supplierInfo.getName(), DataStatusEnum.delete.getValue());
		if(_supplierInfo != null && _supplierInfo.getId().intValue() != supplierInfo.getId().intValue()) {
			return Ret.fail("供应商名称已存在");
		}
		_supplierInfo = SupplierInfo.dao.findById(supplierInfo.getId());
		if(_supplierInfo == null) {
			return Ret.fail("供应商不存在，无法修改");
		}
		supplierInfo.setCode(PinYinUtil.getFirstSpell(supplierInfo.getName()));
		supplierInfo.setUpdatedAt(new Date());
		supplierInfo.update();
		
		// 更新往来帐户
		BigDecimal openBalance = supplierInfo.getBigDecimal("open_balance"); // 期初欠款
		updateTraderBookAccount(adminId, _supplierInfo.getTraderBookAccount(), openBalance);
		
		return Ret.ok("修改供应商成功");
	}


	/**
	* 删除
	*/
	public Ret delete(List<Integer> ids) {
		if(ids == null || ids.isEmpty()) {
			return Ret.fail("参数错误");
		}
		for (Integer id : ids) {
			SupplierInfo supplierInfo = SupplierInfo.dao.findById(id);
			if(supplierInfo == null) {
				continue;
			}
			supplierInfo.setDataStatus(DataStatusEnum.delete.getValue());
			supplierInfo.setUpdatedAt(new Date());
			supplierInfo.update();
		}
		
		return Ret.ok("删除供应商成功");
	}
	
	/**
	* 停用
	*/
	public Ret disable(List<Integer> ids) {
		if(ids == null || ids.isEmpty()) {
			return Ret.fail("参数错误");
		}
		for (Integer id : ids) {
			SupplierInfo supplierInfo = SupplierInfo.dao.findById(id);
			if(supplierInfo == null) {
				continue;
			}
			supplierInfo.setDataStatus(DataStatusEnum.disable.getValue());
			supplierInfo.setUpdatedAt(new Date());
			supplierInfo.update();
		}
		
		return Ret.ok("停用供应商成功");
	}

	
	/**
	* 启用
	*/
	public Ret enable(List<Integer> ids) {
		if(ids == null || ids.isEmpty()) {
			return Ret.fail("参数错误");
		}
		for (Integer id : ids) {
			SupplierInfo supplierInfo = SupplierInfo.dao.findById(id);
			if(supplierInfo == null) {
				continue;
			}
			supplierInfo.setDataStatus(DataStatusEnum.enable.getValue());
			supplierInfo.setUpdatedAt(new Date());
			supplierInfo.update();
		}
		
		return Ret.ok("启用供应商成功");
	}

	/**
	* 导入
	*/
	public Ret createImport() {

		return Ret.ok("导入供应商成功");
	}

	/**
	 * 更新往来帐户资金
	 * @param tenantOrgId
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
			BigDecimal inAmount = traderBookAccount.getInAmount().subtract(oldOpenBalance);
			traderBookAccount.setInAmount(inAmount);
			
		} else if (oldOpenBalance.compareTo(BigDecimal.ZERO) < 0) { // 负数，客户有预存款
			BigDecimal outAmount = traderBookAccount.getOutAmount().subtract(BigDecimal.ZERO.subtract(oldOpenBalance));
			traderBookAccount.setOutAmount(outAmount);
		}
		// 再处理新的期初欠款
		if (openBalance.compareTo(BigDecimal.ZERO) > 0) { // 客户有欠款
			BigDecimal inAmount = traderBookAccount.getInAmount().add(openBalance);
			traderBookAccount.setInAmount(inAmount);
			
		} else if (openBalance.compareTo(BigDecimal.ZERO) < 0) { // 负数，客户有预存款
			BigDecimal outAmount = traderBookAccount.getOutAmount().add(BigDecimal.ZERO.subtract(openBalance));
			traderBookAccount.setOutAmount(outAmount);
		}
		
		traderBookAccount.setOpenBalance(openBalance);
		traderBookAccount.setUpdatedAt(new Date());
		traderBookAccount.update();
		
		BigDecimal balance = traderBookAccount.getSupplierDebtAmount(); // 欠供应商款=收入-支出
		
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
	 * 期初调整
	 * @param tenantOrgId
	 * @param customerInfoId 
	 * @param customerInfoIdObject
	 * @param openBalanceObject
	 * @return
	 */
	public Ret updateOpenBalance(Integer adminId, Integer supplierInfoId, BigDecimal openBalance) {
		if(supplierInfoId == null || supplierInfoId <= 0) {
			return Ret.fail("客户不能为空");
		}
		if(openBalance == null) {
			return Ret.fail("期初金额不能为空");
		}
		SupplierInfo supplierInfo = SupplierInfo.dao.findById(supplierInfoId);
		Ret ret = updateTraderBookAccount(adminId, supplierInfo.getTraderBookAccount(), openBalance);
		return ret;
	}
}