/**
 * 
 */
package com.bytechainx.psi.common.kit.excel;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.bytechainx.psi.common.CommonConfig;
import com.bytechainx.psi.common.kit.DateUtil;
import com.bytechainx.psi.common.kit.RandomUtil;

/**
 * @author defier
 *
 */
public class XlsKit {

	
	public static String genOrderXls(String sheetNames, String fileName, String[] headers, List<?> data, String[] columns, String[] listColumns) {
		String filePathBase = "/xls/" + DateUtil.getYearMonthStr(new Date()) + "/" + RandomUtil.genRandomNum(8);
		File outDir = new File(CommonConfig.resourceUploadPath + filePathBase);
		if (!outDir.exists()) {
			outDir.mkdirs();
		}
		String filePath =  filePathBase + "/" + fileName + ".xls";
		String absolutePath = CommonConfig.resourceUploadPath + filePath;
		XlsWriter.data(data).sheetNames(sheetNames).headerRow(1).headers(headers).columns(columns).listColumns(listColumns).cellWidth(4000).writeToFile(absolutePath);
		return filePath;
	}
	
	public static String genOrderDetailXls(String sheetNames, String fileName, String[] headers, String[] headerCols, String[] listHeaders, String[] listColumns, String[] footers, String[] footerCols, Map<?,?> data) {
		String filePathBase = "/xls/" + DateUtil.getYearMonthStr(new Date()) + "/" + RandomUtil.genRandomNum(8);
		File outDir = new File(CommonConfig.resourceUploadPath + filePathBase);
		if (!outDir.exists()) {
			outDir.mkdirs();
		}
		String filePath =  filePathBase + "/" + fileName + ".xls";
		String absolutePath = CommonConfig.resourceUploadPath + filePath;
		XlsDetailWriter.data(data).sheetNames(sheetNames).headers(headers).headerCols(headerCols).listHeader(listHeaders).listColumns(listColumns).footers(footers).footerCols(footerCols).cellWidth(4000).writeToFile(absolutePath);
		return filePath;
	}
	
	
}
