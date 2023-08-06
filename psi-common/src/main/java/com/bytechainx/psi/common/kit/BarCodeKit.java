/**
 * 
 */
package com.bytechainx.psi.common.kit;

import java.util.Date;

import org.apache.commons.lang.StringUtils;

import com.bytechainx.psi.common.dto.BarCode;

/**
 * 条码识别，有些是gs1码，要解析出来。
 * @author defier
 *
 */
public class BarCodeKit {

	public static void main(String[] args) {
		String x = "0106971850069070102022111211221115172405142100994";
		BarCode barCode = parseBarCode(x);
		System.out.println(barCode.getCode());
		System.out.println(barCode.getBatchNumber());
		System.out.println(DateUtil.getDayStr(barCode.getExpirationDate()));
		System.out.println(DateUtil.getDayStr(barCode.getProductionDate()));
	}
	
	/**
	 * 是否包含FNC1分隔符
	 * @param c
	 * @return
	 */
	private static boolean isFNC1(char c) {
	    return c == '\u001D';
	}
	
	/**
	 * 是否包含FNC1分隔符
	 * @param c
	 * @return
	 */
	public static boolean containsFNC1(String str) {
	    for (int i = 0; i < str.length(); i++) {
	        if (isFNC1(str.charAt(i))) {
	            return true;
	        }
	    }
	    return false;
	}
	
	public static BarCode parseBarCode(String string) {
		if(StringUtils.isEmpty(string)) {
			return null;
		}
		if(string.length() == 13 || (string.length() == 14 && string.startsWith("0"))) { // 只是一个条码
			BarCode barCode = new BarCode();
			barCode.setCode(string);
			return barCode;
		}
		BarCode barCode = new BarCode();
		String[] stringList = string.split("\u001D"); // FNC1分隔符
		for(String s : stringList) {
			if(s.length() <= 1) {
				continue;
			}
			if(s.startsWith("01")) {
				String code = s.substring(2, 16);
				if(code.startsWith("0")) { // 去掉首位的0
					code = code.substring(1);
					barCode.setCode(code);
				}
				s = s.substring(16); // 剩余字符
			}
			parseCode(s, barCode);
		}
		
		return barCode;
	}
	
	private static void parseCode(String s, BarCode barCode) {
	    if(s.startsWith("11")) { // 生产日期
	        String productionDateStr = s.substring(2, 8);
	        Date productionDate = DateUtil.getDayNumberDate(20+productionDateStr);
	        barCode.setProductionDate(productionDate);
	        if(s.length() > 8) {
	            parseCode(s.substring(8), barCode);
	        }
	    } else if(s.startsWith("17")) { // 失效日期
	        String expirationDateStr = s.substring(2, 8);
	        Date expirationDate = DateUtil.getDayNumberDate(20+expirationDateStr);
	        barCode.setExpirationDate(expirationDate);
	        if(s.length() > 8) {
	            parseCode(s.substring(8), barCode);
	        }
	    } else if(s.startsWith("10")) { // 生产批号
	        String batchNumberStr = s.substring(2); // 生产批号不是固定长度
	        barCode.setBatchNumber(batchNumberStr);
	    }
	}

}
