/**
 * 
 */
package com.bytechainx.psi.common.dto;

import java.util.Date;

import com.bytechainx.psi.common.kit.DateUtil;

/**
 * 商品条码，GS1码
 * @author defier
 *
 */
public class BarCode {

	private String code; // 条码
	private Date productionDate; // 生产日期（11）
	private Date expirationDate; // 失效日期（17）
	private String batchNumber; // 生产批号（10）
	
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public Date getProductionDate() {
		return productionDate;
	}
	public void setProductionDate(Date productionDate) {
		this.productionDate = productionDate;
	}
	public Date getExpirationDate() {
		return expirationDate;
	}
	public void setExpirationDate(Date expirationDate) {
		this.expirationDate = expirationDate;
	}
	public String getBatchNumber() {
		return batchNumber;
	}
	public void setBatchNumber(String batchNumber) {
		this.batchNumber = batchNumber;
	}
	
	public String toString() {
		return getCode()+"."+getBatchNumber()+"."+DateUtil.getDayStr(getExpirationDate())+"."+DateUtil.getDayStr(getProductionDate());
	}
	
}
