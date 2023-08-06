/**
 * 
 */
package com.bytechainx.psi.common;

/**
 * 系统应用模块
 * @author defier
 *
 */
public enum Modules {

	core("核心模块"),
	;

	private String name;

	private Modules(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	public static Modules getEnum(String value) {
		for (Modules c : Modules.values()) {
			if (value.equalsIgnoreCase(c.name())) {
				return c;
			}
		}
		return null;
	}
	
}

