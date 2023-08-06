package com.bytechainx.psi.common.service.setting;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.bytechainx.psi.common.model.TenantRole;
import com.bytechainx.psi.common.model.TenantRoleOperRef;
import com.bytechainx.psi.common.service.base.CommonService;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;


/**
* 角色管理
*/
public class TenantRoleService extends CommonService {

	/**
	* 分页列表
	*/
	public Page<TenantRole> paginate(Integer pageNumber, int pageSize) {
		StringBuffer where = new StringBuffer();
		List<Object> params = new ArrayList<Object>();
		where.append("where 1 = 1 ");


		return TenantRole.dao.paginate(pageNumber, pageSize, "select * ", "from tenant_role "+where.toString()+" order by id desc", params.toArray());
	}

	/**
	 * 根据角色ID，获取角色所有权限
	 * @return
	 */
	public Set<String> findOperByRoleId(Integer roleId) {
		List<TenantRoleOperRef> operRef = TenantRoleOperRef.dao.find("select * from tenant_role_oper_ref where role_id = ?", roleId);
		Set<String> operCodes = new HashSet<>();
		for (TenantRoleOperRef e : operRef) {
			operCodes.add(e.getOperCode());
		}
		return operCodes;
	}

	/**
	* 新增
	*/
	public Ret create(TenantRole role) {
		if(role == null) {
			return Ret.fail("参数错误");
		}
		if(StringUtils.isEmpty(role.getName())) {
			return Ret.fail("角色名称不能为空");
		}
		TenantRole _role = TenantRole.dao.findFirst("select * from tenant_role where name = ? limit 1", role.getName());
		if(_role != null) {
			return Ret.fail("角色名称已存在");
		}
		role.setCreatedAt(new Date());
		role.setUpdatedAt(new Date());
		role.save();
		
		if(role.getOperCodes() != null) {
			for (String operCode : role.getOperCodes()) {
				TenantRoleOperRef ref = new TenantRoleOperRef();
				ref.setOperCode(operCode);
				ref.setRoleId(role.getId());
				ref.save();
			}
		}
		return Ret.ok("创建角色成功").set("targetId", role.getId());
	}


	/**
	* 修改
	*/
	public Ret update(TenantRole role) {
		if(role == null || role.getId() == null || role.getId() <= 0) {
			return Ret.fail("参数错误");
		}
		if(StringUtils.isEmpty(role.getName())) {
			return Ret.fail("角色名称不能为空");
		}
		TenantRole _role = TenantRole.dao.findFirst("select * from tenant_role where name = ? limit 1", role.getName());
		if(_role != null && _role.getId().intValue() != role.getId().intValue()) {
			return Ret.fail("角色名称已存在");
		}
		_role = TenantRole.dao.findById(role.getId());
		if(_role == null) {
			return Ret.fail("角色不存在，无法修改");
		}
		
		role.setUpdatedAt(new Date());
		role.update();
		
		List<TenantRoleOperRef> oldRoleOperRefList = TenantRoleOperRef.dao.find("select * from tenant_role_oper_ref where role_id = ?", role.getId());
		// 先过滤出已删除的记录
		List<TenantRoleOperRef> deleteRefList = new ArrayList<>();
		for(TenantRoleOperRef oldRef : oldRoleOperRefList) {
			boolean isExist = false;
			if(role.getOperCodes() == null) {
				deleteRefList.add(oldRef);
				continue;
			}
			for (String operCode : role.getOperCodes()) {
				if(oldRef.getOperCode().equalsIgnoreCase(operCode)) {
					isExist = true;
					break;
				}
			}
			if(!isExist) { // 需要删除的记录
				deleteRefList.add(oldRef);
			}
		}
		// 删除没有的权限
		for (TenantRoleOperRef e : deleteRefList) {
			e.delete();
		}
		if(role.getOperCodes() != null) {
			for (String operCode : role.getOperCodes()) {
				TenantRoleOperRef ref = TenantRoleOperRef.dao.findFirst("select * from tenant_role_oper_ref where role_id = ? and oper_code = ?", role.getId(), operCode);
				if(ref != null) {
					continue;
				}
				ref = new TenantRoleOperRef();
				ref.setOperCode(operCode);
				ref.setRoleId(role.getId());
				ref.save();
			}
		}
		
		return Ret.ok("更新角色成功");
	}


	/**
	* 删除
	*/
	public Ret delete(List<Integer> ids) {
		if(ids == null || ids.isEmpty()) {
			return Ret.fail("参数错误");
		}
		StringBuffer resp = new StringBuffer();
		for (Integer id : ids) {
			TenantRole role = TenantRole.dao.findById(id);
			if(role == null) {
				continue;
			}
			int adminCount = Db.queryInt("select count(*) from tenant_admin where role_id = ?", role.getId());
			if(adminCount > 0) {
				resp.append("角色["+role.getName()+"]已使用,不能删除\r\n");
				continue;
			}
			role.delete();
		}
		if(resp.length() > 0) {
			return Ret.fail(resp.toString());
		}
		return Ret.ok("删除角色成功");
	}

}