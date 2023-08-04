package com.bytechainx.psi.common.model;

import com.bytechainx.psi.common.model.base.BaseCustomerCategory;

/**
 * 客户分类
 */
@SuppressWarnings("serial")
public class CustomerCategory extends BaseCustomerCategory<CustomerCategory> {
	
	public static final CustomerCategory dao = new CustomerCategory().dao();
	
	public CustomerCategory findById(Integer id) {
		return CustomerCategory.dao.findFirst("select * from customer_category where id = ? limit 1", id);
	}
	
}

