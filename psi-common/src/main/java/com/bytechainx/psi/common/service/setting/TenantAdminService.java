package com.bytechainx.psi.common.service.setting;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.bytechainx.psi.common.EnumConstant.UserActiveStatusEnum;
import com.bytechainx.psi.common.kit.CipherkeyUtil;
import com.bytechainx.psi.common.kit.PinYinUtil;
import com.bytechainx.psi.common.kit.RandomUtil;
import com.bytechainx.psi.common.kit.SmsKit;
import com.bytechainx.psi.common.model.TenantAdmin;
import com.bytechainx.psi.common.service.base.CommonService;
import com.jfinal.kit.Kv;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Page;


/**
* 员工管理
*/
public class TenantAdminService extends CommonService {

	/**
	* 分页列表
	*/
	public Page<TenantAdmin> paginate(Kv conditionColumns, Integer pageNumber, int pageSize) {
		StringBuffer where = new StringBuffer();
		List<Object> params = new ArrayList<Object>();
		where.append("where 1 = 1 ");

		conditionFilter(conditionColumns, where, params);
		
		return TenantAdmin.dao.paginate(pageNumber, pageSize, "select * ", "from tenant_admin "+where.toString()+" order by id desc", params.toArray());
	}

	/**
	* 新增
	*/
	public Ret create(TenantAdmin admin) {
		if(admin == null) {
			return Ret.fail("参数错误");
		}
		if(StringUtils.isEmpty(admin.getRealName())) {
			return Ret.fail("员工名称不能为空");
		}
		if(StringUtils.isEmpty(admin.getMobile())) {
			return Ret.fail("手机号不能为空");
		}
		if(admin.getRoleId() == null || admin.getRoleId() <= 0) {
			return Ret.fail("角色不能为空");
		}
		TenantAdmin _admin = TenantAdmin.dao.findBy(admin.getMobile());
		if(_admin != null) {
			return Ret.fail("手机号已存在");
		}
		admin.setEncrypt(CipherkeyUtil.encodeSalt(RandomUtil.genRandomNum(10)));
		String randomPwd = RandomUtil.getRandomNum(4);
		String encodePassword = CipherkeyUtil.encodePassword(randomPwd, admin.getEncrypt());
		admin.setPassword(encodePassword);
		admin.setActiveStatus(UserActiveStatusEnum.waiting.getValue());
		admin.setCode(PinYinUtil.getFirstSpell(admin.getRealName()));
		admin.setCreatedAt(new Date());
		admin.setUpdatedAt(new Date());
		admin.save();
		
		if(admin.getLoginFlag()) { // 可登录帐户
			// 发送激活短信
			SmsKit.sendAdminActive(admin, randomPwd);
		}
		
		return Ret.ok("新增员工成功").set("targetId", admin.getId());
	}


	/**
	* 修改
	*/
	public Ret update(TenantAdmin admin) {
		if(admin == null || admin.getId() == null || admin.getId() <= 0) {
			return Ret.fail("参数错误");
		}
		if(StringUtils.isEmpty(admin.getRealName())) {
			return Ret.fail("员工名称不能为空");
		}
		if(StringUtils.isEmpty(admin.getMobile())) {
			return Ret.fail("手机号不能为空");
		}
		TenantAdmin _admin = TenantAdmin.dao.findBy(admin.getMobile());
		if(_admin != null && _admin.getId().intValue() != admin.getId().intValue()) {
			return Ret.fail("手机号已存在");
		}
		TenantAdmin tenantAdmin = TenantAdmin.dao.findById(admin.getId());
		if(tenantAdmin == null) {
			return Ret.fail("帐户不存在，无法修改");
		}
		
		if(StringUtils.isNotEmpty(admin.getPassword())) {
			String encodePassword = CipherkeyUtil.encodePassword(admin.getPassword(), tenantAdmin.getEncrypt());
			admin.setPassword(encodePassword);
		} else {
			admin.remove("password"); // 不修改密码
		}
		admin.setUpdatedAt(new Date());
		admin.update();
		
		if(tenantAdmin.getActiveStatus() == UserActiveStatusEnum.waiting.getValue() && admin.getLoginFlag()) { // 如果是待激活用户和可登录帐户，则发送激活短信
			String randomPwd = RandomUtil.getRandomNum(4);
			String encodePassword = CipherkeyUtil.encodePassword(randomPwd, tenantAdmin.getEncrypt());
			admin.setPassword(encodePassword);
			admin.update();
			
			SmsKit.sendAdminActive(admin, randomPwd);
		}
		
		return Ret.ok("员工修改成功");
	}

	/**
	 * 修改密码
	 * @param tenantOrgId
	 * @param adminId
	 * @param oldPassword
	 * @param password
	 * @return
	 */
	public Ret updatePwd(Integer adminId, String oldPassword, String password) {
		if(adminId == null) {
			return Ret.fail("参数错误");
		}
		if(StringUtils.isEmpty(oldPassword)) {
			return Ret.fail("原密码不能为空");
		}
		if(StringUtils.isEmpty(password)) {
			return Ret.fail("新密码不能为空");
		}
		TenantAdmin admin = TenantAdmin.dao.findById(adminId);
		if(admin == null) {
			return Ret.fail("用户不存在");
		}
		String encodeOldPassword = CipherkeyUtil.encodePassword(oldPassword, admin.getEncrypt());
		if(!StringUtils.equals(admin.getPassword(), encodeOldPassword)) {
			return Ret.fail("原密码不正确");
		}
		
		String encodePassword = CipherkeyUtil.encodePassword(password, admin.getEncrypt());
		admin.setPassword(encodePassword);
		admin.setUpdatedAt(new Date());
		admin.update();
		
		return Ret.ok("修改密码成功");
	}

	/**
	* 停用
	*/
	public Ret disable(List<Integer> ids) {
		if(ids == null || ids.isEmpty()) {
			return Ret.fail("参数错误");
		}
		for (Integer id : ids) {
			TenantAdmin admin = TenantAdmin.dao.findById(id);
			if(admin == null) {
				continue;
			}
			admin.setActiveStatus(UserActiveStatusEnum.disable.getValue());
			admin.setUpdatedAt(new Date());
			admin.update();
		}
		return Ret.ok("禁用成功");
	}

	
	/**
	* 启用
	*/
	public Ret enable(List<Integer> ids) {
		if(ids == null || ids.isEmpty()) {
			return Ret.fail("参数错误");
		}
		for (Integer id : ids) {
			TenantAdmin admin = TenantAdmin.dao.findById(id);
			if(admin == null) {
				continue;
			}
			admin.setActiveStatus(UserActiveStatusEnum.enable.getValue());
			admin.setUpdatedAt(new Date());
			admin.update();
		}
		return Ret.ok("启用成功");
	}

}