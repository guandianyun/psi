/**
 * 
 */
package com.bytechainx.psi.common.kit.excel.interceptor;

/**
 * @author defier
 *
 */
import java.util.List;

import com.jfinal.plugin.activerecord.Model;

public interface PostListProcessor {
	void process(List<Model<?>> list) ;
}
