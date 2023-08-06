/**
 * 
 */
package com.bytechainx.psi.common.kit.excel.interceptor;

import com.jfinal.plugin.activerecord.Model;

/**
 *  excel解析前置处理器，在每一个元素 保存之前调用
 */
public interface PreXlsProcessor {
	void process(Model<?> m);
}
