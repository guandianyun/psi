package com.silkie.util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * 浮动数的精确运算，浮动大小精确比较
 * @author defier.lai
 * 2010-4-13 下午08:05:37
 * @version 1.0
 */
public final class DoubleUtil {
	
	// 默认除法运算精度
	private static final MathContext DEFAULT_MATH_CONTEXT = new MathContext(8, RoundingMode.HALF_UP);

	// 这个类不能实例化
	private DoubleUtil() {
		
	}

	/**
	 * 是否能整除
	 * @param d1
	 * @param d2
	 * @return
	 */
	public static boolean remainIsZero(Double d1, Double d2) {
		String str = "" + remainder(d1, d2);
		String str1 = "[.0]*";
		return str.matches(str1);
	}

	public static BigDecimal remainder(Double d1, Double d2) {
		return new BigDecimal(d1 + "").remainder(new BigDecimal(d2 + ""));
	}
	
	/**
	 * 连加
	 * 
	 * @return v1+v2
	 */
	public static double add(double v1, double... v2s) {
		BigDecimal b1 = new BigDecimal(Double.toString(v1));
		for (double d : v2s) {
			BigDecimal b2 = new BigDecimal(Double.toString(d));
			b1 = b1.add(b2);
		}
		return b1.doubleValue();
	}

	/**
	 * 连减
	 * 
	 * @return 相减差v1-v2
	 */
	public static double subtract(double v1, double... v2s) {
		BigDecimal b1 = new BigDecimal(Double.toString(v1));
		for (double d : v2s) {
			BigDecimal b2 = new BigDecimal(Double.toString(d));
			b1 = b1.subtract(b2);
		}
		return b1.doubleValue();
	}

	/**
	 * 连乘
	 * 
	 * @return v1*v2
	 */
	public static double multiply(double v1, double... v2s) {
		BigDecimal b1 = new BigDecimal(Double.toString(v1));
		for (double d : v2s) {
			BigDecimal b2 = new BigDecimal(Double.toString(d));
			b1 = b1.multiply(b2);
		}
		return b1.doubleValue();
	}
	
	/**
	 * 连除
	 * 
	 * @return v1/v2
	 */
	public static double divide(double v1, double... v2s) {
		return divide(DEFAULT_MATH_CONTEXT, v1, v2s);
	}

	/**
	 * 连除
	 * @param 指定运算精度和位数
	 * 
	 * @return v1/v2
	 */
	public static double divide(MathContext mc, double v1, double... v2s) {
		BigDecimal b1 = new BigDecimal(v1);
		for (double d : v2s) {
			BigDecimal b2 = new BigDecimal(d);
			b1 = b1.divide(b2, mc.getPrecision(), mc.getRoundingMode());
		}
		return b1.doubleValue();
	}
	
	/**
	 * 提供精确的小数位四舍五入处理。
	 * 
	 * @param v
	 *            需要四舍五入的数字
	 * @param scale
	 *            小数点后保留几位
	 * @return 四舍五入后的结果
	 */
	public static double round(double v, int scale) {
		if (scale < 0) {
			throw new IllegalArgumentException(
					"The scale must be a positive integer or zero");
		}
		BigDecimal b = new BigDecimal(Double.toString(v));
		BigDecimal one = new BigDecimal("1");
		return b.divide(one, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
	}
	
	public static double roundFloor(double v) {
		BigDecimal b = new BigDecimal(Double.toString(v));
		BigDecimal one = new BigDecimal("1");
		return b.divide(one, 0, BigDecimal.ROUND_FLOOR).doubleValue();
	}
	
	/**
	 * 获得小数点位数对应的0.1 , 0.01... 等等
	 * 
	 * @return v1*v2
	 */
	public static double getDoubleByPoint(int point) {
		return getDoubleByPoint(1, point);
	}
	
	/**
	 * 获得数字小数点位数对应的
	 * 如：3，传入可返回0.3 , 0.03... 等等
	 * 
	 * @return v1*v2
	 */
	public static double getDoubleByPoint(double value,int point) {
		if(point < 0 ){
			for(int i=1;i<=-point;i++){
				value=multiply(value,10);
			}
		}else{
			for(int i=1;i<=point;i++){
				value=multiply(value,0.1);
			}
		}
		return value;
	}
	
	
	/**
	 * 提供精确的小数位四舍五入处理。
	 * 
	 * @param v
	 *            需要四舍五入的数字
	 * @param scale
	 *            小数点后保留2位
	 * @return 四舍五入后的结果
	 */
	public static double roundCurrency(double v) {
		BigDecimal b = new BigDecimal(Double.toString(v));
		BigDecimal one = new BigDecimal("1");
		return b.divide(one, 2, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	/**
	 * 提供精确的比较方法: ==
	 * 
	 * @param v1
	 * @param v2
	 * @return true表示 v1 == v2
	 */
	public static boolean equals(double v1, double v2) {
		return compareTo(v1, v2) == 0;
	}

	/**
	 * 提供精确的比较方法: !=
	 * 
	 * @param v1
	 * @param v2
	 * @return true表示 v1 != v2
	 */
	public static boolean notEquals(double v1, double v2) {
		return compareTo(v1, v2) != 0;
	}

	/**
	 * 提供精确的比较方法: >
	 * 
	 * @param v1
	 * @param v2
	 * @return true表示 v1 > v2
	 */
	public static boolean greatThan(double v1, double v2) {
		return compareTo(v1, v2) > 0;
	}
	/**
	 * insert是否在start end中间
	 * 
	 * @param start
	 * @param end
	 * @param price
	 * @return
	 */
	public static boolean isBetweenEquals(double start, double end,double price) {
		if(greatEquals(price,start)&&greatEquals(end,price)){
			return true;
		}
		return false;
	}
	/**
	 * 提供精确的比较方法: >=
	 * 
	 * @param v1
	 * @param v2
	 * @return true表示 v1 >= v2
	 */
	public static boolean greatEquals(double v1, double v2) {
		return compareTo(v1, v2) >= 0;
	}

	/**
	 * 提供精确的比较方法: <
	 * 
	 * @param v1
	 * @param v2
	 * @return true表示 v1 < v2
	 */
	public static boolean lessThan(double v1, double v2) {
		return compareTo(v1, v2) < 0;
	}

	/**
	 * 提供精确的比较方法: <=
	 * 
	 * @param v1
	 * @param v2
	 * @return true表示 v1 <= v2
	 */
	public static boolean lessEquals(double v1, double v2) {
		return compareTo(v1, v2) <= 0;
	}

	/**
	 * 提供两个数的比较
	 * 
	 * @param v1
	 * @param v2
	 * @return -1: v1 < v2; 0: v1 == v2; 1: v1 > v2;
	 */
	public static int compareTo(double v1, double v2) {
		return Double.compare(v1, v2);
	}
	
	/**
	 * 将float 转型为 double， JDK 自动转型会导致float不等于double
	 * @param f 待转型的float
	 * @return 相等的 double值
	 */
	public static double float2Double(Float f) {
		return Double.valueOf(f.toString());
	}
	
	/**
	 * 将double 转型为 float，采用强制转型 f_value = (float)d_value
	 * @param d 待转型的double
	 * @return 对应的float值
	 */
	public static float double2Float(Double d) {
		return d.floatValue();
	}
	
	/**
	 * 转换成String XXXX.XX的格式,0的话补0
	 * @param d
	 * @return
	 */
	public static String format(Double d){
		DecimalFormat df = new DecimalFormat("0.00"); 
		return df.format(d);
	}
	
	/**
	 * 转换成String XXXX.XX的格式,0的话补0
	 * @param d
	 * @param point 小数点位数
	 * @return
	 * @throws Exception 小数点位数小于0， IllegalStateException
	 */
	public static String format(Double d, int point) {
		if(point < 0 )
			throw new IllegalArgumentException("小数点位数不合法:" + point);
		String format = "0";
		if(point > 0)
			format = format + ".";
		for(int i=0; i< point ; i++){
			format = format + "0";
		}
		DecimalFormat df = new DecimalFormat(format); 
		return df.format(d);
	}
}