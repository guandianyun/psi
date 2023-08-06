/**
 * 
 */
package com.bytechainx.psi.common.kit.excel.interceptor;

import java.util.List;

import com.jfinal.plugin.activerecord.Model;

/**
 * @author defier
 *
 */
public interface PreListProcessor {
	void process(List<Model<?>> list);
}
