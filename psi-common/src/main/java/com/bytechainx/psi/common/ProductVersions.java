/**
 * 
 */
package com.bytechainx.psi.common;

/**
 * 产品版本
 * @author defier
 *
 */
public enum ProductVersions {

	common("通用专业"),
	food("食品酒饮"),
	fashion("服装鞋帽"),
	material("五金建材"),
	electrical("家电数码"),
	book("文体图书"),
	daylife("日化用品"),
	mombaby("母婴用品"),
	automobile("汽修汽配"),
	supermarket("商超便利"),
	medical("医疗器械")
	;

	private String name;

	private ProductVersions(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	public static ProductVersions getEnum(String value) {
		for (ProductVersions c : ProductVersions.values()) {
			if (value.equalsIgnoreCase(c.name())) {
				return c;
			}
		}
		return null;
	}
	
	
}
