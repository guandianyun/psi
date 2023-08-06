/**
 * 
 */
package com.bytechainx.psi.common.kit.excel.interceptor;

import com.jfinal.plugin.activerecord.Model;

/**
 *  excel解析后置处理器 ,在整个excel对象保存完毕之后调用
 */
public interface PostXlsProcessor {
	void process(Model<?> m);
}
