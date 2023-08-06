/**
 * 
 */
package com.bytechainx.psi.common.kit.excel;

import java.math.BigDecimal;

/**
 * @author defier
 *
 */
public final class TypeKit {

	public static boolean isInteger(Object obj) {
		return TypeKit.isNumeric(obj);
	}
	
	public static boolean isDouble(Object obj) {
		return TypeKit.isNumeric(obj);
	}
	
	public static boolean isFloat(Object obj) {
		return TypeKit.isNumeric(obj);
	}
	
	public static boolean isLong(Object obj) {
		return TypeKit.isNumeric(obj);
	}
	
	public static boolean isNumeric(Object obj) {
		if (null == obj) {
			return false;
		}
		if(obj.toString().startsWith("0")) {
			return false;
		}
        try {
            new BigDecimal(obj.toString());
        } catch (Exception e) {
            return false;
        }
        return true;
//		Pattern pattern = Pattern.compile("-?[0-9]+(\\.[0-9]+)?");
//        Matcher isNum = pattern.matcher(obj.toString());
//        if (!isNum.matches()) {
//            return false;
//        }
//        return true;
	}
	
	public static boolean isString(Object obj) {
		return !TypeKit.isNumeric(obj);
	}
	
	public static boolean isBoolean(Object obj) {
		if (null == obj) {
			return false;
		}
		String val = obj.toString().toUpperCase();
		if (Boolean.TRUE.toString().toUpperCase().equals(val)
				|| Boolean.FALSE.toString().toUpperCase().equals(val)) {
			return true;
		}
		return false;
	}
	
}
