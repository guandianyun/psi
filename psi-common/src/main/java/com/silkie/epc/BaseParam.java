/**
 * Copyright (c) 2014 by pw186.com.
 * All right reserved.
 */
package com.silkie.epc;

import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * 事件参数，改接口实例将作为参数注入Event中执行
 *
 */
public abstract class BaseParam {
	
	/**
	 * 使用完后的清理方法
	 */
	public void clear() {
		getMap().clear();
	}
	
	//abstract protected BaseParam clone();
	
	/**
	 * 是否有这个name的参数存在
	 * @param name 待检测的name
	 * @return true:存在;false:不存在 
	 */
	public boolean containsName(String name) {
		return getMap().containsKey(name);
	}
	
	public Set<String> nameSet() {
		return getMap().keySet();
	}
	
	public int numOfName() {
		return getMap().size();
	}

	public void delNameAndValue(String name) {
		_delName(name);
	}
	
	public void setValue(String name, Object value) {
		_setValue(name, value);
	}
	@SuppressWarnings("unchecked")
	public List<Object> getListValue(String name) {
		Object object = getObjValue(name);
		if(object != null) {
			return (List<Object>)object;
		}
		return null;
	}
	public Object getObjValue(String name) {
		return _getValue(name);
	}
	public Object getObjValue(String name, Object defaultValue) {
		return _getValue(name, defaultValue); 
	}
	public String getStrValue(String name) {
		return (String)_getValue(name);
	}
	public String getStrValue(String name, String defaultValue) {
		return (String)_getValue(name, defaultValue); 
	}
	public boolean getBoolValue(String name) {
		return (Boolean)_getValue(name);
	}
	public boolean getBoolValue(String name, boolean defaultValue) {
		return (Boolean)_getValue(name, defaultValue); 
	}	
	public byte getByteValue(String name) {
		return (Byte)_getValue(name);
	}
	public byte getByteValue(String name, byte defaultValue) {
		return (Byte)_getValue(name, defaultValue); 
	}		
	public char getCharValue(String name) {
		return (Character)_getValue(name);
	}
	public char getCharValue(String name, char defaultValue) {
		return (Character)_getValue(name, defaultValue); 
	}	
	public short getShortValue(String name) {
		return (Short)_getValue(name);
	}
	public short getShortValue(String name, short defaultValue) {
		return (Short)_getValue(name, defaultValue); 
	}		
	public int getIntValue(String name) {
		return (Integer)_getValue(name);
	}
	public int getIntValue(String name, int defaultValue) {
		return (Integer)_getValue(name, defaultValue); 
	}
	/*public float getFloatValue(String name) {
		return DoubleUtil.double2Float((Double)_getValue(name));
	}
	public float getFloatValue(String name, float defaultValue) {
		return DoubleUtil.double2Float((Double)_getValue(name, DoubleUtil.float2Double(defaultValue))); 
	}*/	
	public double getDoubleValue(String name) {
		return (Double)_getValue(name);
	}
	public double getDoubleValue(String name, double defaultValue) {
		return (Double)_getValue(name, defaultValue); 
	}		
	public long getLongValue(String name) {
		return (Long)_getValue(name);
	}
	public long getLongValue(String name, long defaultValue) {
		return (Long)_getValue(name, defaultValue); 
	}
	@SuppressWarnings("unchecked")
	public List<Byte> getListByte(String name) {
		return (List<Byte>)_getValue(name);
	}
	@SuppressWarnings("unchecked")
	public List<Byte> getListByte(String name, List<Byte> defaultValue) {
		return (List<Byte>)_getValue(name, defaultValue); 
	}	
	@SuppressWarnings("unchecked")
	public List<Boolean> getListBool(String name) {
		return (List<Boolean>)_getValue(name);
	}
	@SuppressWarnings("unchecked")
	public List<Boolean> getListBool(String name, List<Boolean> defaultValue) {
		return (List<Boolean>)_getValue(name, defaultValue); 
	}	
	@SuppressWarnings("unchecked")
	public List<Character> getListChar(String name) {
		return (List<Character>)_getValue(name);
	}
	@SuppressWarnings("unchecked")
	public List<Character> getListChar(String name, List<Character> defaultValue) {
		return (List<Character>)_getValue(name, defaultValue); 
	}	
	@SuppressWarnings("unchecked")
	public List<Short> getListShort(String name) {
		return (List<Short>)_getValue(name);
	}
	@SuppressWarnings("unchecked")
	public List<Short> getListShort(String name, List<Short> defaultValue) {
		return (List<Short>)_getValue(name, defaultValue); 
	}	
	@SuppressWarnings("unchecked")
	public List<Integer> getListInt(String name) {
		return (List<Integer>)_getValue(name);
	}
	@SuppressWarnings("unchecked")
	public List<Integer> getListInt(String name, List<Integer> defaultValue) {
		return (List<Integer>)_getValue(name, defaultValue); 
	}	
	/*@SuppressWarnings("unchecked")
	public List<Float> getListFloat(String name) {
		return dlist2Flist((List<Double>)_getValue(name));
	}
	@SuppressWarnings("unchecked")
	public List<Float> getListFloat(String name, List<Float> defaultValue) {
		return dlist2Flist((List<Double>)_getValue(name, flist2Dlist(defaultValue))); 
	}*/	
	@SuppressWarnings("unchecked")
	public List<Double> getListDouble(String name) {
		return (List<Double>)_getValue(name);
	}
	@SuppressWarnings("unchecked")
	public List<Double> getListDouble(String name, List<Double> defaultValue) {
		return (List<Double>)_getValue(name, defaultValue); 
	}	
	@SuppressWarnings("unchecked")
	public List<String> getListStr(String name) {
		return (List<String>)_getValue(name);
	}
	@SuppressWarnings("unchecked")
	public List<String> getListStr(String name, List<String> defaultValue) {
		return (List<String>)_getValue(name, defaultValue); 
	}		
	public List<?> getListObj(String name) {
		return (List<?>)_getValue(name);
	}
	public List<?> getListObj(String name, List<?> defaultValue) {
		return (List<?>)_getValue(name, defaultValue); 
	}	
	public byte[] getByteArrayValue(String name) {
		return (byte[])_getValue(name);
	}
	public byte[] getByteArrayValue(String name, byte[] defaultValue) {
		return (byte[])_getValue(name, defaultValue);
	}
	
	@Override
	public String toString() {
		return getMap().toString();
	}		
////////////////////////////////////////////////////////////////////////////////	
	protected abstract Map<String, Object> getMap();
	
	private void _delName(String key) {
		getMap().remove(key);
	}
	
	private void _setValue(String key, Object value) {
		getMap().put(key, value);
	}
	
	private Object _getValue(String key) {
		return getMap().get(key);
	}
	
	private Object _getValue(String key, Object defaultValue) {
		return (getMap().containsKey(key))? getMap().get(key) : defaultValue;
	}
	
}
