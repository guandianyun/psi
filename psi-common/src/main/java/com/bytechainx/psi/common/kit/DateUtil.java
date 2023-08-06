package com.bytechainx.psi.common.kit;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author defier.lai
 * 2010-4-13 下午08:04:21
 * @version 1.0
 */
public class DateUtil {

	private static final Logger log = LoggerFactory.getLogger(DateUtil.class);

	private static final SimpleDateFormat DAY = getFormat("yyyy-MM-dd");
	
	private static final SimpleDateFormat YEAR_MONTH = getFormat("yyyy-MM");
	
	private static final SimpleDateFormat DAY_NUMBER = getFormat("yyyyMMdd");
	
	private static final SimpleDateFormat YEAR_DAY_NUMBER = getFormat("yyMMdd");
	
	private static final SimpleDateFormat DAY_ONLY = getFormat("MM-dd");

	private static final SimpleDateFormat SECOND = getFormat("yyyy-MM-dd HH:mm:ss");
	
	private static final SimpleDateFormat SECOND_NUMBER = getFormat("yyyyMMddHHmmss");

	private static final SimpleDateFormat SECOND_ONLY = getFormat("HH:mm:ss");

	private static final SimpleDateFormat MINUTE = getFormat("yyyy-MM-dd HH:mm");

	private static final SimpleDateFormat MINUTE_ONLY = getFormat("HH:mm");
	
	private static final SimpleDateFormat MONTH_DAY = getFormat("MM月dd日");
	
	private static final SimpleDateFormat MINUTE_ANOTHER = getFormat("yyyyMMdd-HHmm");
	
	private static final int FIRST_DAY = Calendar.SUNDAY;

	public static String getMinuteDbStr(Date date) {
		synchronized (MINUTE_ANOTHER) {
			return getStr(date, MINUTE_ANOTHER);
		}
	}
	
	public static Date getMinuteDbDate(String str) {
		synchronized (MINUTE_ANOTHER) {
			return getDate(str, MINUTE_ANOTHER);
		}
	}
	
	/**
	 * 获取日期属于周几
	 * 星期一：1，星期二：2，星期三：3，星期四：4，星期五：5，星期六：6，星期天：7，如果不限星期
	 * @return
	 */
	public static int getDateWeek(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int week = calendar.get(Calendar.DAY_OF_WEEK);
		if(week == 1) {
			return 7;
		} else {
			return week -1;
		}
	}
	
	public static String getSecondOnlyStr(Date date) {
		synchronized (SECOND_ONLY) {
			return getStr(date, SECOND_ONLY);
		}
	}
	public static String getYearMonthStr(Date date) {
		synchronized (YEAR_MONTH) {
			return getStr(date, YEAR_MONTH);
		}
	}
	public static Date getYearMonthDate(Date date) {
		return getYearMonthDate(getYearMonthStr(date));
	}
	
	public static Date getYearMonthDate(String dateStr) {
		synchronized (YEAR_MONTH) {
			return getDate(dateStr, YEAR_MONTH);
		}
	}
	
	public static String getOnlyDayStr(long date) {
		synchronized (DAY_ONLY) {
			return getStr(new Date(date), DAY_ONLY);
		}
	}
	
	public static String getOnlyDayStr(Date date) {
		synchronized (DAY_ONLY) {
			return getStr(date, DAY_ONLY);
		}
	}
	public static String getSecondDateStr(long date) {
		return getSecondStr(new Date(date));
	}

	public static String getSecondStr(long date) {
		return getSecondOnlyStr(new Date(date));
	}

	public static String getMinuteStr(Date date) {
		synchronized (MINUTE) {
			return getStr(date, MINUTE);
		}
	}

	public static String getMinuteStr(long date) {
		return getMinuteStr(new Date(date));
	}

	public static String getMinuteOnlyStr(Date date) {
		synchronized (MINUTE_ONLY) {
			return getStr(date, MINUTE_ONLY);
		}
	}

	public static String getSecondStr(Date date) {
		synchronized (SECOND) {
			return getStr(date, SECOND);
		}
	}
	
	public static String getSecondNumber(Date date) {
		synchronized (SECOND_NUMBER) {
			return getStr(date, SECOND_NUMBER);
		}
	}
	
	
	
	public static String getMonthDayStr(Date date) {
		synchronized (MONTH_DAY) {
			return getStr(date, MONTH_DAY);
		}
	}
	
	public static String getYYDayStr(Date date) {
		synchronized (YEAR_DAY_NUMBER) {
			return getStr(date, YEAR_DAY_NUMBER);
		}
	}

	public static String getDayStr(Date date) {
		synchronized (DAY) {
			return getStr(date, DAY);
		}
	}
	
	public static String getDayNumberStr(Date date) {
		synchronized (DAY_NUMBER) {
			return getStr(date, DAY_NUMBER);
		}
	}
	
	public static int getDayNumber(Date date) {
		if(date==null){
			return 0;
		}
		synchronized (DAY_NUMBER) {
			return Integer.valueOf(getStr(date, DAY_NUMBER));
		}
	}
	
	public static int getYYDayNumber(Date date) {
		if(date==null){
			return 0;
		}
		synchronized (YEAR_DAY_NUMBER) {
			return Integer.valueOf(getStr(date, YEAR_DAY_NUMBER));
		}
	}
	
	public static Date getDayDate(Date date) {
		return getDayDate(getDayStr(date));
	}
	
	public static String getDayStr(long date) {
		return getDayStr(new Date(date));
	}

	public static Date getSecondDate(String dateStr) {
		synchronized (SECOND) {
			return getDate(dateStr, SECOND);
		}
	}

	public static Date getDayDate(String dateStr) {
		synchronized (DAY) {
			return getDate(dateStr, DAY);
		}
	}
	
	public static Date getDayNumberDate(String dateStr) {
		synchronized (DAY_NUMBER) {
			return getDate(dateStr, DAY_NUMBER);
		}
	}
	public static Date getMinuteOnlyDate(String dateStr) {
		synchronized (MINUTE_ONLY) {
			return getDate(dateStr, MINUTE_ONLY);
		}
	}
	public static Date getMinuteDate(String dateStr) {
		synchronized (MINUTE) {
			return getDate(dateStr, MINUTE);
		}
	}

	public static Date getMinuteDate(long time) {
		return getMinuteDate(getMinuteStr(time));
	}
	
	public static Date monthsAddOrSub(Date date, int offset) {
		return addOrSub(date, Calendar.MONTH, offset);
	}
	
	public static Date daysAddOrSub(Date date, int offset) {
		return addOrSub(date, Calendar.DAY_OF_MONTH, offset);
	}

	public static Date hoursAddOrSub(Date date, int offset) {
		return addOrSub(date, Calendar.HOUR_OF_DAY, offset);
	}

	public static Date minutesAddOrSub(Date date, int offset) {
		return addOrSub(date, Calendar.MINUTE, offset);
	}

	public static Date secondsAddOrSub(Date date, int offset) {
		return addOrSub(date, Calendar.SECOND, offset);
	}

	private static Date addOrSub(Date date, int type, int offset) {
		if (date == null)
			return null;
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.get(type);
		cal.set(type, cal.get(type) + offset);
		return cal.getTime();
	}

	private static String getStr(Date date, SimpleDateFormat format) {
		if (date == null) {
			return "";
		}
		return format.format(date);
	}

	private static SimpleDateFormat getFormat(String format) {
		return new SimpleDateFormat(format);
	}

	private static Date getDate(String dateStr, SimpleDateFormat format) {
		if ("".equals(dateStr) || dateStr == null)
			return null;
		try {
			return format.parse(dateStr);
		} catch (ParseException e) {
			log.error("format yyyy-MM-dd HH:mm:ss error:", e);
		}
		return null;
	}

	// /////////////////////////////////time
	// tools///////////////////////////////
	/**
	 * 是否为TimeString 样式的String 00:00 - 23:59
	 * 
	 * @param toCheck
	 * @return
	 */
	public static boolean isTimeString(String toCheck) {
		if(!StringUtils.isNotBlank(toCheck)){
			return false;
		}
		if (toCheck.trim().matches("([0-1][0-9]|2[0-3]):[0-5][0-9]|24:00"))
			return true;
		else
			return false;
	}

	/**
	 * 比较两个TimeString 的大小
	 * 
	 * @param ts1
	 * @param ts2
	 * @return
	 */
	public static int compareHHmmInString(String ts1, String ts2) {
		return ts1.compareTo(ts2);
	}

	/**
	 * 检测是否在开始与结束之间，前闭后开区间 -1： start 不小于end 0: 不在start 与end之间 1: 在start与end之间
	 * 
	 * @param ts
	 * @param start
	 * @param end
	 * @return
	 */
	public static int betweenHHmmInString(String ts, String start, String end) {
		if (compareHHmmInString(start, end) >= 0)
			return -1;
		if (compareHHmmInString(ts, start) < 0)
			return 0;
		if (compareHHmmInString(end, ts) <= 0)
			return 0;
		return 1;
	}

	/**
	 * 检测两个时间是否相等, 00:00 == 24:00
	 * 
	 * @param ts1
	 * @param ts2
	 * @return
	 */
	public static boolean equalsInTimeString(String ts1, String ts2) {
		if (ts1.equals(ts2))
			return true;
		if (ts1.equals("00:00") || ts1.equals("24:00")) {
			if (ts2.equals("00:00") || ts2.equals("24:00")) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 检测是否为同一个小时和分钟. 1: cal>c; 0:cal=c; -1:cal<c
	 */
	public static int compareHHmm(Calendar cal, Calendar c) {
		if (cal.get(Calendar.HOUR_OF_DAY) > c.get(Calendar.HOUR_OF_DAY)) {
			return 1;
		} else if (cal.get(Calendar.HOUR_OF_DAY) == c.get(Calendar.HOUR_OF_DAY)) {
			if (cal.get(Calendar.MINUTE) > c.get(Calendar.MINUTE))
				return 1;
			else if (cal.get(Calendar.MINUTE) == c.get(Calendar.MINUTE))
				return 0;
			else
				return -1;
		} else
			return -1;
	}

	/**
	 * 检测是否在开始与结束之间，闭区间 -1： start 不小于end 0: 不在start 与end之间 1: 在start与end之间
	 * 
	 * @param cal
	 * @param start
	 * @param end
	 * @return
	 */
	public static int betweenHHmm(Calendar cal, Calendar start, Calendar end) {
		if (compareHHmm(start, end) != -1)
			return -1;
		if (compareHHmm(cal, start) == -1)
			return 0;
		if (compareHHmm(end, cal) == -1)
			return 0;
		return 1;
	}

	/**
	 * 检测是否为同在一天. 1: cal>c; 0:cal=c; -1:cal<c
	 */
	public static boolean compareDay(Calendar cal, Calendar c) {
		if (cal.get(Calendar.MONTH) == c.get(Calendar.MONTH)
				&& cal.get(Calendar.DAY_OF_MONTH) == c
						.get(Calendar.DAY_OF_MONTH))
			return true;
		else
			return false;
	}

	/**
	 * 将00:00格式的字符串转为Calender
	 * 
	 * @param timeString
	 * @return Calender
	 * @throws Exception
	 */
	public static Calendar string2calendar(String timeString) throws Exception {
		if (!DateUtil.isTimeString(timeString))
			throw new Exception("Wrong argument : timeString format error "
					+ timeString);
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, getHour(timeString));
		cal.set(Calendar.MINUTE, getMinute(timeString));
		return cal;
	}

	/**
	 * 将calendar转为HH:MM格式的字符串
	 * 
	 * @param cal
	 * @return
	 */
	public static String calendar2TimeString(Calendar cal) {
		return (cal.get(Calendar.HOUR_OF_DAY) > 9 ? cal
				.get(Calendar.HOUR_OF_DAY) : "0"
				+ cal.get(Calendar.HOUR_OF_DAY))
				+ ":"
				+ (cal.get(Calendar.MINUTE) > 9 ? cal.get(Calendar.MINUTE)
						: "0" + cal.get(Calendar.MINUTE));
	}

	/**
	 * TimeString 中得到小时信息
	 * 
	 * @param timeString
	 * @return
	 */
	public static int getHour(String timeString) {
		return Integer.parseInt(timeString.substring(0, 2));
	}

	/**
	 * TimeString 中得到分钟信息
	 * 
	 * @param timeString
	 * @return
	 */
	public static int getMinute(String timeString) {
		return Integer.parseInt(timeString.substring(3, 5));
	}

	public static String getDateStringFromMinute(String minute) {
		return minute.substring(5, 10);
	}

	public static String getMiniteOnlyFromMinute(String minute) {
		return minute.substring(11, 16);
	}

	public static boolean isMiniteDate(String str) {
		if (str == null) {
			return false;
		}
		try {
			MINUTE_ONLY.parse(str);
			return true;
		} catch (Exception e) {
		}
		return false;
	}
	public static long getMiniteDate(Date date,String str) {
		if(str==null)
			return 0;
		return getMinuteDate(getDayStr(date)+" "+str).getTime();
	}
	/**
	 * 从Date的字符串中只得到月日信息
	 * 
	 * @param dateString
	 * @return
	 */
	public static String getDateOnlyFromDate(String dateString) {
		return dateString.substring(5, 10);
	}

	// /////////////////////////////date tools////////////////////////////
	/**
	 * 将calendar转为MM:DD格式的字符串
	 * 
	 * @param cal
	 * @return
	 */
	public static String calendar2DateString(Calendar cal) {
		return (cal.get(Calendar.MONTH) + 1 > 9 ? cal.get(Calendar.MONTH) + 1
				: "0" + (cal.get(Calendar.MONTH) + 1))
				+ ":"
				+ (cal.get(Calendar.DAY_OF_MONTH) > 9 ? cal
						.get(Calendar.DAY_OF_MONTH) : "0"
						+ cal.get(Calendar.DAY_OF_MONTH));
	}

	public static boolean isDateString(String toCheck) {
		if (toCheck == null)
			return false;
		if (toCheck.trim().matches("(0[0-9]|1[0-2]):([0-2][0-9]|3[0-1])"))
			return true;
		else
			return false;
	}
	public static Date[] getIntervalDate(Date start, Date end) {  
		  List<Date> ret = new ArrayList<Date>();  
		  Calendar calendar = Calendar.getInstance();  
		  calendar.setTime(getDayDate(start));  
		  Date tmpDate = calendar.getTime();  
		  long endTime = getDayDate(end).getTime();  
		  while (tmpDate.before(end) || tmpDate.getTime() == endTime) {  
		    ret.add(calendar.getTime());  
		    calendar.add(GregorianCalendar.DATE, 1);  
		    tmpDate = calendar.getTime();  
		  }  
		  return (Date[]) ret.toArray(new Date[ret.size()]);  
	}  
	
	
	/**
	 * 两个时间相差天数
	 * @param startDate
	 * @param endDate
	 * @return
	 * @throws ParseException
	 */
	public static int getDaySpace(Date startDate, Date endDate) {
       return Math.abs(getDaySpaceBetween(startDate, endDate));  
    }
	
	public static int getDaySpaceBetween(Date startDate, Date endDate) {
		java.util.Calendar calst = java.util.Calendar.getInstance();   
        java.util.Calendar caled = java.util.Calendar.getInstance();   
        calst.setTime(startDate);   
        caled.setTime(endDate);   
        //设置时间为0时   
        calst.set(java.util.Calendar.HOUR_OF_DAY, 0);   
        calst.set(java.util.Calendar.MINUTE, 0);   
        calst.set(java.util.Calendar.SECOND, 0);   
        caled.set(java.util.Calendar.HOUR_OF_DAY, 0);   
        caled.set(java.util.Calendar.MINUTE, 0);   
        caled.set(java.util.Calendar.SECOND, 0);   
       //得到两个日期相差的天数   
        int days = ((int) (caled.getTime().getTime() / 1000) - (int) (calst.getTime().getTime() / 1000)) / 3600 / 24;   
        
       return days;  
    }
	
	/**
	 * 得到本周日期
	 * @return
	 */
	public static List<String> getWeekDate() {
		List<String> list = new ArrayList<String>();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        while (calendar.get(Calendar.DAY_OF_WEEK) != FIRST_DAY) {
            calendar.add(Calendar.DATE, -1);
        }
        for (int i = 0; i < 7; i++) {
        	String date = dateFormat.format(calendar.getTime());
        	list.add(date);
        	calendar.add(Calendar.DATE, 1);
        }
        return list;
    }
	
	/**
	 * 上一周数据
	 */
	public static List<String> getLastWeekDate() {
		List<String> list = new ArrayList<String>();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        for (int i = 0; i < 7; i++) {
        	String date = dateFormat.format(calendar.getTime());
        	list.add(date);
        	calendar.add(Calendar.DATE, -1);
        }
        return list;
    }
	
	/**
	 * 是否在同一个月
	 * @param date1
	 * @param date2
	 * @return
	 */
	public static boolean isSameMonth(Date date1, Date date2) {
		Calendar calendar1 = Calendar.getInstance();
        calendar1.setTime(date1);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(date2);
        return calendar1.get(Calendar.ERA) == calendar2.get(Calendar.ERA) 
        		&& calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) 
        		&& calendar1.get(Calendar.MONTH) == calendar2.get(Calendar.MONTH);
         
	}
	
	/**
	 * 是否在同一个天
	 * @param date1
	 * @param date2
	 * @return
	 */
	public static boolean isSameDay(Date date1, Date date2) {
		Calendar calendar1 = Calendar.getInstance();
        calendar1.setTime(date1);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(date2);
        return calendar1.get(Calendar.ERA) == calendar2.get(Calendar.ERA) 
        		&& calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) 
        		&& calendar1.get(Calendar.DAY_OF_YEAR) == calendar2.get(Calendar.DAY_OF_YEAR);
         
	}
	
	public static Date getCurrentTime() {
		return new Date();
	}
	
	public static Date daysAddOrSubNow(Integer offset) {
		return daysAddOrSub(new Date(), offset);
	}
	
	/**
	 * 获取当月第一天
	 * @return
	 */
	public static String getMonthFirstDay() {
		String startDay = DAY.format(new Date());
		Calendar calendar = Calendar.getInstance();
		calendar.set(Integer.parseInt(startDay.substring(0,4)), Integer.parseInt(startDay.substring(5,7)) - 1, 1);
		return DAY.format(calendar.getTime());
	}
	
	/**
	 * 获取当月最后一天
	 * @return
	 */
	public static String getMonthLastDay() {
		String startDay = DAY.format(new Date());
		Calendar calendar = Calendar.getInstance();
		calendar.set(Integer.parseInt(startDay.substring(0,4)), Integer.parseInt(startDay.substring(5,7)), 1);
		calendar.add(Calendar.DATE, -1);
		return DAY.format(calendar.getTime());
	}
	
	/**
	 * 获取上月第一天
	 * @return
	 */
	public static String getPreMonthFirstDay() {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MONTH, -1);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		return DAY.format(calendar.getTime());
	}
	/**
	 * 获取上月最后一天
	 * @return
	 */
	public static String getPreMonthLastDay() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_MONTH, 0);
		return DAY.format(calendar.getTime());
	}
	
	/**
	 * 获取月份第一天
	 * @return
	 */
	public static String getMonthFirstDay(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.MONTH, -1);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		return DAY.format(calendar.getTime());
	}
	/**
	 * 获取月份最后一天
	 * @return
	 */
	public static String getMonthLastDay(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
		return DAY.format(calendar.getTime());
	}
	
   
}

