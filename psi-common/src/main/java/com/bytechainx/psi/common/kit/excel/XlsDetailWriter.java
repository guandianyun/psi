/**
 * 
 */
package com.bytechainx.psi.common.kit.excel;

/**
 * 单据详情导出
 * @author defier
 *
 */
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;

import com.bytechainx.psi.common.kit.excel.XlsReadRule.Column;
import com.google.common.base.Preconditions;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

public class XlsDetailWriter {

	private Log LOG = Log.getLog(XlsDetailWriter.class);
    private String[] sheetNames = new String[]{"Sheet"};
    private int cellWidth = 8000;
    private String[][] headers;
    private String[][] headerCols;
    private String[][] listHeaders;
    private String[][] listColumns = new String[][] {};
    private String[][] footers;
    private String[][] footerCols;
    private Map<?, ?>[] data;

    public XlsDetailWriter(Map<?, ?>... data) {
        this.data = data;
    }

    public static XlsDetailWriter data(Map<?, ?>... data) {
        return new XlsDetailWriter(data);
    }

    public boolean writeToFile(String fileName) {
    	OutputStream outputStream = null;
		try {
			File file = new File(fileName);
			if(!file.exists()) {
				file.createNewFile();
			}
			outputStream = new FileOutputStream(file);
			this.write().write(outputStream);
		} catch (IOException e) {
			LOG.error("生成xls文件异常", e);
			throw (new PoiException(e.getLocalizedMessage()));
		} finally {
			if (null != outputStream) {
				try {
					outputStream.flush();
					outputStream.close();
					outputStream = null;
				} catch (IOException e) {
					LOG.error("关闭输出流异常", e);
					throw (new PoiException(e.getLocalizedMessage()));
				}
			}
		}
		return true;
    }

    public Workbook write() {
        Preconditions.checkNotNull(data, "data can not be null");
        Preconditions.checkNotNull(headers, "headers can not be null");
        Preconditions.checkNotNull(headerCols, "columns can not be null");
        Preconditions.checkNotNull(listColumns, "columns can not be null");
        Preconditions.checkArgument(data.length == sheetNames.length && sheetNames.length == headers.length
                && headers.length == headerCols.length, "data,sheetNames,headers and columns'length should be the same." +
                "(data:" + data.length + ",sheetNames:" + sheetNames.length + ",headers:" + headers.length + ",columns:" + headerCols.length + ")");
        Preconditions.checkArgument(cellWidth >= 0, "cellWidth can not be less than 0");
        
        Workbook wb = new HSSFWorkbook();
        int dataLen = data.length;
        for (int i = 0; i < dataLen; i++) {
            //make listHeaders
            Sheet sheet = wb.createSheet(sheetNames[i]);
            Cell cell = null;
            int rowNum = 0;
            Row row = sheet.createRow(rowNum);
            Cell headerCell = row.createCell(5);
    		headerCell.setCellValue("销售出库单");
    		CellStyle cellStyle = wb.createCellStyle();
    		cellStyle.setAlignment(HorizontalAlignment.CENTER); // 居中
    		Font font = wb.createFont();
    		font.setFontName("Microsoft YaHei");
    		font.setFontHeightInPoints((short) 16);//设置字体大小
    		font.setBold(true);
    		cellStyle.setFont(font);//选择需要用到的字体格式
    		headerCell.setCellStyle(cellStyle);
            
    		 rowNum += 1;
    		 
            cellStyle = setCellStyle(sheet);
            int cellIndex = 0;
            for(int j = 0; j < headers[i].length; j++) {
            	if(j % 3 == 0) { // 每行三个
            		row = sheet.createRow(rowNum);
            		cellIndex = 0;
            		rowNum += 1;
            	}
            	if (cellWidth > 0) {
                    sheet.setColumnWidth(j, cellWidth);
                }
                createHeaderCell(wb, i, row, j, cellIndex, headers[i][j]);
                cell = row.createCell(++cellIndex);
                cell.setCellStyle(cellStyle);
    			String column = headerCols[i][j];
    			Object val = data[i].get(column);
                if (null == val) {
                	cell.setCellValue("");
    			} else if (TypeKit.isNumeric(val)) {
    				cell.setCellValue(Double.valueOf(val.toString()));
    			} else if (TypeKit.isBoolean(val)) {
    				cell.setCellValue(Boolean.valueOf(val.toString()));
    			} else {
    				cell.setCellValue(val + "");
    			}
                cellIndex += 2;
            }
            rowNum += 1;
            
            // data list
            row = sheet.createRow(rowNum);
            for (int h = 0; h < listHeaders[i].length; h++) {
                if (cellWidth > 0) {
                    sheet.setColumnWidth(h, cellWidth);
                }
                createListHeaderCell(wb, i, row, h, h);
            }
            rowNum += 1;
            
            //make rows
            cellStyle = setListCellStyle(sheet);
            List<?> dataList = (List<?>) data[i].get("list");
            for (int j = 0, len = dataList.size(); j < len; j++) {
            	row = sheet.createRow(rowNum);
                Object obj = dataList.get(j);
                if (obj == null) {
                    continue;
                }
                if (obj instanceof Map) {
                	processAsMap(sheet, cellStyle, listColumns[i], row, rowNum, obj);
                } else if (obj instanceof Model) {
                	processAsModel(sheet, cellStyle, listColumns[i], row, rowNum, obj);
                } else if (obj instanceof Record) {
                	processAsRecord(sheet, cellStyle, listColumns[i], row, rowNum, obj);
                } else {
                    throw new PoiException("Not support type[" + obj.getClass() + "]");
                }
                rowNum += 1;
            }
            
            
            cellStyle = setCellStyle(sheet);
            cellIndex = 0;
            for(int j = 0; j < footers[i].length; j++) {
            	if(j % 3 == 0) { // 每行三个
            		rowNum += 1;
            		row = sheet.createRow(rowNum);
            		cellIndex = 0;
            	}
            	if (cellWidth > 0) {
                    sheet.setColumnWidth(j, cellWidth);
                }
                createHeaderCell(wb, i, row, j, cellIndex, footers[i][j]);
                cell = row.createCell(++cellIndex);
                cell.setCellStyle(cellStyle);
    			String column = footerCols[i][j];
    			Object val = data[i].get(column);
                if (null == val) {
                	cell.setCellValue("");
    			} else if (TypeKit.isNumeric(val)) {
    				cell.setCellValue(Double.valueOf(val.toString()));
    			} else if (TypeKit.isBoolean(val)) {
    				cell.setCellValue(Boolean.valueOf(val.toString()));
    			} else {
    				cell.setCellValue(val + "");
    			}
                cellIndex += 2;
            }
        }
        return wb;
    }

	private void createListHeaderCell(Workbook wb, int i, Row row, int j, int cellIndex) {
		Cell headerCell = row.createCell(cellIndex);
		headerCell.setCellValue(listHeaders[i][j]);
		CellStyle cellStyle = wb.createCellStyle();
		cellStyle.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());// 设置背景色
		cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		cellStyle.setAlignment(HorizontalAlignment.CENTER); // 居中
		cellStyle.setBorderBottom(BorderStyle.THIN);
		cellStyle.setBorderLeft(BorderStyle.THIN);
		cellStyle.setBorderRight(BorderStyle.THIN);
		cellStyle.setBorderTop(BorderStyle.THIN);
		Font font = wb.createFont();
		font.setFontName("Microsoft YaHei");
		font.setFontHeightInPoints((short) 10);//设置字体大小
		font.setBold(true);
		cellStyle.setFont(font);//选择需要用到的字体格式
		headerCell.setCellStyle(cellStyle);
	}
	
	private CellStyle setListCellStyle(Sheet sheet) {
		CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
		cellStyle.setAlignment(HorizontalAlignment.CENTER); // 居中
		cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		cellStyle.setBorderBottom(BorderStyle.THIN);
		cellStyle.setBorderLeft(BorderStyle.THIN);
		cellStyle.setBorderRight(BorderStyle.THIN);
		cellStyle.setBorderTop(BorderStyle.THIN);
		Font font = sheet.getWorkbook().createFont();
		font.setFontName("Microsoft YaHei");
		font.setFontHeightInPoints((short) 10);// 设置字体大小
		cellStyle.setFont(font);// 选择需要用到的字体格式
		cellStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("@"));
		return cellStyle;
	}

	/**
	 * @param wb
	 * @param i
	 * @param row
	 * @param j
	 */
	public void createHeaderCell(Workbook wb, int i, Row row, int j, int cellIndex, String name) {
		Cell headerCell = row.createCell(cellIndex);
		headerCell.setCellValue(name+"：");
		CellStyle cellStyle = wb.createCellStyle();
		cellStyle.setAlignment(HorizontalAlignment.CENTER); // 居中
		Font font = wb.createFont();
		font.setFontName("Microsoft YaHei");
		font.setFontHeightInPoints((short) 10);//设置字体大小
		font.setBold(true);
		cellStyle.setFont(font);//选择需要用到的字体格式
		headerCell.setCellStyle(cellStyle);
	}

    @SuppressWarnings("unchecked")
    private int processAsMap(Sheet sheet, CellStyle cellStyle, String[] columns, Row row, int rowNum, Object obj) {
        Cell cell = null;
        Map<String, Object> map = (Map<String, Object>) obj;
        int cellIdx = 0;
        for (int idx = 0; idx < columns.length; idx++) {
			cell = row.createCell(cellIdx++);
			cell.setCellStyle(cellStyle);
			String column = columns[idx];
			Object val = map.get(column);
            if (null == val) {
                cell.setCellValue(" ");
			} else if (TypeKit.isNumeric(val)) {
				cell.setCellValue(Double.valueOf(val.toString()));
			} else if (TypeKit.isBoolean(val)) {
				cell.setCellValue(Boolean.valueOf(val.toString()));
			} else {
				cell.setCellValue(val + "");
			}
        }
        return 1;
    }

	/**
	 * @param sheet
	 * @param cell
	 */
	private CellStyle setCellStyle(Sheet sheet) {
		CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
		cellStyle.setAlignment(HorizontalAlignment.CENTER); // 居中
		cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		Font font = sheet.getWorkbook().createFont();
		font.setFontName("Microsoft YaHei");
		font.setFontHeightInPoints((short) 10);// 设置字体大小
		cellStyle.setFont(font);// 选择需要用到的字体格式
		cellStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("@"));
		return cellStyle;
	}

    private int processAsModel(Sheet sheet, CellStyle cellStyle, String[] columns, Row row, int rowNum, Object obj) {
        Model<?> model = (Model<?>) obj;
        return processAsMap(sheet, cellStyle, columns, row, rowNum, model.toMap());
    }

    private int processAsRecord(Sheet sheet, CellStyle cellStyle, String[] columns, Row row, int rowNum, Object obj) {
        Record record = (Record) obj;
        return processAsMap(sheet, cellStyle, columns, row,rowNum, record.getColumns());
    }

    public XlsDetailWriter sheetName(String sheetName) {
        this.sheetNames = new String[]{sheetName};
        return this;
    }

    public XlsDetailWriter sheetNames(String... sheetName) {
        this.sheetNames = sheetName;
        return this;
    }

    public XlsDetailWriter cellWidth(int cellWidth) {
        this.cellWidth = cellWidth;
        return this;
    }

    public XlsDetailWriter headers(String[]... headers) {
        this.headers = headers;
        return this;
    }

    public XlsDetailWriter headerCols(String... column) {
        this.headerCols = new String[][]{column};
        return this;
    }

    public XlsDetailWriter headerCols(String[]... columns) {
        this.headerCols = columns;
        return this;
    }
    
    public XlsDetailWriter listHeader(String... header) {
        this.listHeaders = new String[][]{header};
        return this;
    }

    public XlsDetailWriter listHeader(String[]... headers) {
        this.listHeaders = headers;
        return this;
    }
    
    public XlsDetailWriter footer(String... footer) {
        this.footers = new String[][]{footer};
        return this;
    }

    public XlsDetailWriter footers(String[]... footers) {
        this.footers = footers;
        return this;
    }

    public XlsDetailWriter footerCols(String... column) {
        this.footerCols = new String[][]{column};
        return this;
    }

    public XlsDetailWriter footerCols(String[]... columns) {
        this.footerCols = columns;
        return this;
    }
    
    public XlsDetailWriter columns(Column... columns) {
    	String[] headers = new String[columns.length];
    	String[] cols = new String[columns.length];	
    	for (int i = 0; i < columns.length; i++) {
			Column col = columns[i];
			headers[i] = col.getHeader();
			cols[i] = col.getAttr();
		}
    	this.headers(headers);
    	this.headerCols(cols);
    	return this;
    }

	public XlsDetailWriter listColumns(String[]... listColumns) {
		 this.listColumns = listColumns;
	     return this;
	}

}