/**
 * 
 */
package com.bytechainx.psi.common.kit.excel;

/**
 * @author defier
 *
 */
import org.apache.poi.ss.formula.functions.T;

import com.jfinal.plugin.activerecord.Model;

/**
 *  单元格值转换器
 */
public interface ColumnConvert {
	T convert(Object val, Model<?> model);
}