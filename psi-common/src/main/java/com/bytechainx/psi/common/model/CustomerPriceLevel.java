package com.bytechainx.psi.common.model;

import java.util.List;

import com.bytechainx.psi.common.EnumConstant.DataStatusEnum;
import com.bytechainx.psi.common.model.base.BaseCustomerPriceLevel;

/**
 * 客户价格等级
 */
@SuppressWarnings("serial")
public class CustomerPriceLevel extends BaseCustomerPriceLevel<CustomerPriceLevel> {
	
	public static final CustomerPriceLevel dao = new CustomerPriceLevel().dao();
	
	public CustomerPriceLevel findById(Integer id) {
		return CustomerPriceLevel.dao.findFirst("select * from customer_price_level where id = ? limit 1", id);
	}

	public List<CustomerPriceLevel> findAll() {
		return CustomerPriceLevel.dao.find("select * from customer_price_level where data_status = ?", DataStatusEnum.enable.getValue());
	}
	
	
	public CustomerPriceLevel findRetailPriceLevel() {
		return CustomerPriceLevel.dao.findFirst("select * from customer_price_level where name = ? limit 1", "零售价");
	}
}

