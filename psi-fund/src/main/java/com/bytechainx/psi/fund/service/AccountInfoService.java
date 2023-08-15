package com.bytechainx.psi.fund.service;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.bytechainx.psi.common.EnumConstant.DataStatusEnum;
import com.bytechainx.psi.common.EnumConstant.FundFlowEnum;
import com.bytechainx.psi.common.EnumConstant.RefOrderTypeEnum;
import com.bytechainx.psi.common.kit.PinYinUtil;
import com.bytechainx.psi.common.model.TraderBalanceAccount;
import com.bytechainx.psi.common.model.TraderFundOrder;
import com.bytechainx.psi.common.service.base.CommonService;
import com.jfinal.kit.Kv;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Page;


/**
* 结算帐户
*/
public class AccountInfoService extends CommonService {

	/**
	* 分页列表
	 * @param accountCondKv 
	*/
	public Page<TraderBalanceAccount> paginate(Kv conditionColumns, Integer pageNumber, int pageSize) {
		StringBuffer where = new StringBuffer();
		List<Object> params = new ArrayList<Object>();
		where.append("where 1 = 1 ");
		
		conditionFilter(conditionColumns, where, params);
		
		if(conditionColumns == null || !conditionColumns.containsKey("data_status")) {
			where.append(" and data_status != ?");
			params.add(DataStatusEnum.delete.getValue());
		}

		return TraderBalanceAccount.dao.paginate(pageNumber, pageSize, "select * ", "from trader_balance_account "+where.toString()+" order by id desc", params.toArray());
	}

	/**
	* 新增
	*/
	public Ret create(TraderBalanceAccount balanceAccount) {
		if(balanceAccount == null) {
			return Ret.fail("参数错误");
		}
		if(StringUtils.isEmpty(balanceAccount.getName())) {
			return Ret.fail("结算帐户名称不能为空");
		}
		TraderBalanceAccount _balanceAccount = TraderBalanceAccount.dao.findFirst("select * from trader_balance_account where name = ? and data_status != ? limit 1", balanceAccount.getName(), DataStatusEnum.delete.getValue());
		if(_balanceAccount != null) {
			return Ret.fail("结算帐户名称已存在");
		}
		balanceAccount.setCode(PinYinUtil.getFirstSpell(balanceAccount.getName()));
		balanceAccount.setCreatedAt(new Date());
		balanceAccount.setUpdatedAt(new Date());
		balanceAccount.save();
		
		
		// 要生成余额调整交易流水记录
		createFundOrder(BigDecimal.ZERO, balanceAccount);
		
		return Ret.ok("新增结算账户成功");
	}

	/**
	* 修改
	*/
	public Ret update(TraderBalanceAccount balanceAccount) {
		if(balanceAccount == null || balanceAccount.getId() == null || balanceAccount.getId() <= 0) {
			return Ret.fail("参数错误");
		}
		if(StringUtils.isEmpty(balanceAccount.getName())) {
			return Ret.fail("结算帐户名称不能为空");
		}
		TraderBalanceAccount _balanceAccount = TraderBalanceAccount.dao.findFirst("select * from trader_balance_account where name = ? and data_status != ? limit 1", balanceAccount.getName(), DataStatusEnum.delete.getValue());
		if(_balanceAccount != null && _balanceAccount.getId().intValue() != balanceAccount.getId().intValue()) {
			return Ret.fail("结算帐户名称已存在");
		}
		_balanceAccount = TraderBalanceAccount.dao.findById(balanceAccount.getId());
		if(_balanceAccount == null) {
			return Ret.fail("结算帐户不存在，无法修改");
		}
		BigDecimal oldBalance = _balanceAccount.getBalance();
		
		balanceAccount.setCode(PinYinUtil.getFirstSpell(balanceAccount.getName()));
		balanceAccount.setUpdatedAt(new Date());
		balanceAccount.update();
		
		// 生成余额调整交易流水记录
		createFundOrder(oldBalance, balanceAccount);
		
		return Ret.ok("修改结算账户成功");
	}


	/**
	* 删除
	*/
	public Ret delete(List<Integer> ids) {
		if(ids == null || ids.isEmpty()) {
			return Ret.fail("参数错误");
		}
		for (Integer id : ids) {
			TraderBalanceAccount balanceAccount = TraderBalanceAccount.dao.findById(id);
			if(balanceAccount == null) {
				continue;
			}
			balanceAccount.setDataStatus(DataStatusEnum.delete.getValue());
			balanceAccount.setUpdatedAt(new Date());
			balanceAccount.update();
		}
		
		return Ret.ok("删除结算账户成功");
	}
	
	/**
	* 停用
	*/
	public Ret disable(List<Integer> ids) {
		if(ids == null || ids.isEmpty()) {
			return Ret.fail("参数错误");
		}
		for (Integer id : ids) {
			TraderBalanceAccount balanceAccount = TraderBalanceAccount.dao.findById(id);
			if(balanceAccount == null) {
				continue;
			}
			balanceAccount.setDataStatus(DataStatusEnum.disable.getValue());
			balanceAccount.setUpdatedAt(new Date());
			balanceAccount.update();
		}
		
		return Ret.ok("停用结算账户成功");
	}

	
	/**
	* 启用
	*/
	public Ret enable(List<Integer> ids) {
		if(ids == null || ids.isEmpty()) {
			return Ret.fail("参数错误");
		}
		for (Integer id : ids) {
			TraderBalanceAccount balanceAccount = TraderBalanceAccount.dao.findById(id);
			if(balanceAccount == null) {
				continue;
			}
			balanceAccount.setDataStatus(DataStatusEnum.enable.getValue());
			balanceAccount.setUpdatedAt(new Date());
			balanceAccount.update();
		}
		
		return Ret.ok("启用结算账户成功");
	}

	/**
	 * 余额调整交易流水记录
	 * @param tenantOrgId
	 * @param balanceAccount
	 * @param ref
	 */
	private void createFundOrder(BigDecimal oldBalance, TraderBalanceAccount balanceAccount) {
		if(balanceAccount.getBalance().compareTo(oldBalance) == 0) { // 门店余额发生变更，则生成记录
			return;
		}
		TraderFundOrder fundOrder = new TraderFundOrder();
		fundOrder.setAmount(balanceAccount.getBalance());
		fundOrder.setFundFlow(FundFlowEnum.adjust.getValue());
		fundOrder.setOrderTime(new Date());
		fundOrder.setRefOrderCode("");
		fundOrder.setRefOrderId(balanceAccount.getId());
		fundOrder.setRefOrderType(RefOrderTypeEnum.balance_account.getValue());
		fundOrder.setTraderBalanceAccountId(balanceAccount.getId());
		fundOrder.setCreatedAt(new Date());
		fundOrder.setUpdatedAt(new Date());
		if(oldBalance != null) {
			fundOrder.setRemark("调整前金额:"+oldBalance);
		}
		fundOrder.save();
	}
	
}