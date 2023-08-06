package com.bytechainx.psi.common.kit;

import org.apache.commons.lang.StringUtils;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

/** 
 * 使用pinyin4j获取汉字拼音首字母或全拼
 */
public class PinYinUtil {
	
	private static final char[] duoyinzi = new char[]{'柜', '长'};
	private static final String[] duoyinzipinyin = new String[]{"gui", "chang"};
	
	/**  
     * 获取汉字串拼音首字母，英文字符不变  
     * @param chinese 汉字串  
     * @return 汉语拼音首字母  
     */   
    public static String getFirstSpell(String chinese) {   
        StringBuffer pybf = new StringBuffer();   
        char[] arr = chinese.toCharArray();   
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();   
        defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);   
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);   
        for (int i = 0; i < arr.length; i++) {   
            if (arr[i] > 128) {
                try {
                	String pinyin = specialPinyin(arr[i]);
                	if(StringUtils.isNotEmpty(pinyin)){
                		pybf.append(pinyin.charAt(0));
                	}else{
                		String[] temp = PinyinHelper.toHanyuPinyinStringArray(arr[i], defaultFormat);   
                		if (temp != null && temp.length > 0) {   
                			pybf.append(temp[0].charAt(0));   
                		}   
                	}
                } catch (BadHanyuPinyinOutputFormatCombination e) {   
                    e.printStackTrace();   
                }   
            } else {   
                pybf.append(arr[i]);   
            }   
        }   
        return pybf.toString().replaceAll("\\W", "").trim();   
    }   
  
    /**  
     * 获取汉字串拼音，英文字符不变  
     * @param chinese 汉字串  
     * @return 汉语拼音  
     */   
    public static String getFullSpell(String chinese) {   
            StringBuffer pybf = new StringBuffer();   
            char[] arr = chinese.toCharArray();   
            HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();   
            defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);   
            defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);   
            for (int i = 0; i < arr.length; i++) {   
                    if (arr[i] > 128) {   
                            try {   
                                    pybf.append(PinyinHelper.toHanyuPinyinStringArray(arr[i], defaultFormat)[0]);   
                            } catch (BadHanyuPinyinOutputFormatCombination e) {   
                                    e.printStackTrace();   
                            }   
                    } else {   
                            pybf.append(arr[i]);   
                    }   
            }   
            return pybf.toString();   
    }
    
    private static String specialPinyin(char c){
    	String pinyin = "";
    	for(int i = 0;i < duoyinzi.length; i++){
    		char d = duoyinzi[i];
    		if(d == c){
    			pinyin = duoyinzipinyin[i];
    		}
    	}
    	return pinyin;
    }
}
