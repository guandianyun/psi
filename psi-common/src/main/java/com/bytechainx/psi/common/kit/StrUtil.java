/**
 * Copyright (c) Since 2014, Power by Pw186.com
 */
package com.bytechainx.psi.common.kit;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.bytechainx.psi.common.EnumConstant.OrderOddTypeEnum;


/**
 * @author defier
 * 2014年7月29日 下午9:47:39
 * @version 1.0
 */
public class StrUtil {
	
	private static final String[] strArr = {"3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "G", "H", "J", "K", "M", "N", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y"};

	
	public static String generateRandonCode(int imgRandNumber) {
		// 生成随机类
		Random random = new Random();
		String sRand = "";
		for (int i = 0; i < imgRandNumber; i++) {
			String rand = String.valueOf(strArr[random.nextInt(strArr.length)]);
			sRand += rand;
		}
		return sRand;

	}
	
	public static String trimLeftSpt(String path) {
		if (path.startsWith(String.valueOf("/"))) {
			path = path.substring(1);
		}

		return path;
	}

	public static String trimRightSpt(String path) {
		if (path.endsWith(String.valueOf("/"))) {
			path = path.substring(0, path.length() - 1);
		}

		return path;
	}
	/**
	 * 字符串前后追加逗号
	 * @param str
	 * @return
	 */
	public static String beforeAfterAppendComma(String str) {
		if(StringUtils.isEmpty(str)) {
			return "";
		}
		if(!str.startsWith(",")) {
			str = ","+str;
		}
		if(!str.endsWith(",")) {
			str = str + ",";
		}
		return str;
	}
	
	/**
	 * 字符串前后清除逗号
	 * @param str
	 * @return
	 */
	public static String beforeAfterCleanComma(String str) {
		if(StringUtils.isEmpty(str)) {
			return "";
		}
		if(str.startsWith(",")) {
			str = str.substring(1);
		}
		if(str.endsWith(",")) {
			str = str.substring(0, str.length()-1);
		}
		return str;
	}
	
	public static String formatBigDecimal(BigDecimal d, int point) {
		if(d == null) {
			return "0";
		}
		if(d.doubleValue() == 0d) {
			return "0";
		}
		if(point < 0 )
			throw new IllegalArgumentException("小数点位数不合法:" + point);
		String format = "#0";
		if(point > 0)
			format = format + ".";
		for(int i=0; i< point ; i++){
			format = format + "0";
		}
		DecimalFormat df = new DecimalFormat(format); 
		String formatStr = df.format(d);
		String pointEnd = formatStr.substring(formatStr.indexOf("."));
		String formatEnd = format.substring(format.indexOf("."));
		if(formatEnd.equals(pointEnd)) {
			return formatStr.substring(0, formatStr.indexOf("."));
		}
		return formatStr;
	}

	
	/**
	 * 查询符合的手机号码
	 * @param str
	 */
	public static List<String> checkMobilephone(String str) {
		List<String> phoneList = new ArrayList<>();
		Pattern pattern = Pattern.compile("((13[0-9])|(14[5|7])|(15([0-3]|[5-9]))|(18[0,5-9]))\\d{8}");
		Matcher matcher = pattern.matcher(str);
		while (matcher.find()) {
			phoneList.add(matcher.group());
		}
		return phoneList;
	}

	/**
	 * 查询符合的固定电话
	 * @param str
	 * 
	 */
	public static List<String> checkTelephone(String str) {
		List<String> phoneList = new ArrayList<>();
		Pattern pattern = Pattern.compile("(0\\d{2}-\\d{8}(-\\d{1,4})?)|(0\\d{3}-\\d{7,8}(-\\d{1,4})?)");
		Matcher matcher = pattern.matcher(str);
		while (matcher.find()) {
			phoneList.add(matcher.group());
		}
		return phoneList;
	}
	
	/**
	 * 获取手机号和座机号
	 * @param str
	 * @return
	 */
	public static List<String> getPhones(String str) {
		List<String> phoneList = checkTelephone(str);
		phoneList.addAll(checkMobilephone(str));
		return phoneList;
	}
	
	public static String encodeMobile(String mobile) {
		if(StringUtils.isEmpty(mobile)) {
			return "";
		}
		if(StringUtils.trim(mobile).length() < 11) {
			return mobile;
		}
		String start = mobile.substring(0, 3);
		String end = mobile.substring(9, 11);
		
		return start+"******"+end;
	}
	
	public static BigDecimal getOddAmount(BigDecimal amount, Integer orderOddType) {
		BigDecimal oddAmount = BigDecimal.ZERO;
		if (orderOddType == OrderOddTypeEnum.subFen.getValue()) {
			oddAmount = amount.setScale(1, RoundingMode.DOWN);
		} else if (orderOddType == OrderOddTypeEnum.subJiao.getValue()) {
			oddAmount = amount.setScale(0, RoundingMode.DOWN);
		} else if (orderOddType == OrderOddTypeEnum.halfUpJiao.getValue()) {
			oddAmount = amount.setScale(1, RoundingMode.HALF_UP);
		} else if (orderOddType == OrderOddTypeEnum.halfUpYuan.getValue()) {
			oddAmount = amount.setScale(0, RoundingMode.HALF_UP);
		}
		return amount.subtract(oddAmount);
	}
	
}
