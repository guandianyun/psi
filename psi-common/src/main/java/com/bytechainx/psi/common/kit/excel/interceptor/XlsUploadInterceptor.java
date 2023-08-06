/**
 * 
 */
package com.bytechainx.psi.common.kit.excel.interceptor;

import java.util.List;

import com.bytechainx.psi.common.kit.excel.RowFilter;
import com.bytechainx.psi.common.kit.excel.XlsReadRule;
import com.bytechainx.psi.common.kit.excel.XlsReader;
import com.bytechainx.psi.common.plugin.Reflect;
import com.google.common.collect.Lists;
import com.jfinal.aop.Invocation;
import com.jfinal.aop.PrototypeInterceptor;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Model;

public abstract class XlsUploadInterceptor extends PrototypeInterceptor {

    private XlsReadRule xlsReadRule;

    public abstract XlsReadRule configRule();

    public abstract void callback(Model<?> model);

    public void doIntercept(Invocation ai) {
        xlsReadRule = configRule();
        Controller controller = ai.getController();
        List<Model<?>> list = XlsReader.readToModel(controller.getFile().getFile(), xlsReadRule);
        execPreListProcessor(list);
        for (Model<?> model : list) {
            execPreExcelProcessor(model);
            callback(model);
            execPostExcelProcessor(model);
        }
        execPostListProcessor(list);
        ai.invoke();
    }

    private void execPreListProcessor(List<Model<?>> list) {
        PreListProcessor preListProcessor = xlsReadRule.getPreListProcessor();
        if (null != preListProcessor) {
        	preListProcessor.process(list);
        }
    }

    private void execPostListProcessor(List<Model<?>> list) {
    	PostListProcessor postListProcessor = xlsReadRule.getPostListProcessor();
        if (null != postListProcessor) {
        	postListProcessor.process(list);
        }
    }

    private void execPreExcelProcessor(Model<?> model) {
    	PreXlsProcessor preXlsProcessor = xlsReadRule.getPreExcelProcessor();
        if (null != preXlsProcessor) {
            preXlsProcessor.process(model);
        }
    }

    private void execPostExcelProcessor(Model<?> model) {
    	PostXlsProcessor postXlsProcessor = xlsReadRule.getPostExcelProcessor();
        if (null != postXlsProcessor) {
            postXlsProcessor.process(model);
        }
    }
    
    @SuppressWarnings("unused")
	private List<RowFilter> getRowFilterList(String rowFilter) {
        List<RowFilter> rowFilterList = Lists.newArrayList();
        String[] rowFilters = rowFilter.split(",");
        if (rowFilters == null)
            return rowFilterList;
        for (String filter : rowFilters) {
            rowFilterList.add((RowFilter) Reflect.on(filter).create().get());
        }
        return rowFilterList;
    }
}
