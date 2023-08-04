package com.bytechainx.psi.common.model;

import com.bytechainx.psi.common.model.base.BaseSupplierCategory;

/**
 * 供应商分类
 */
@SuppressWarnings("serial")
public class SupplierCategory extends BaseSupplierCategory<SupplierCategory> {
	
	public static final SupplierCategory dao = new SupplierCategory().dao();
	
	public SupplierCategory findById(Integer id) {
		return SupplierCategory.dao.findFirst("select * from supplier_category where id = ? limit 1", id);
	}
	
	
}

