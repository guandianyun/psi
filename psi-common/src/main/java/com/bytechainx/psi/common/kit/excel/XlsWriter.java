/**
 * 
 */
package com.bytechainx.psi.common.kit.excel;

/**
 * @author defier
 *
 */
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
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
import org.apache.poi.ss.util.CellRangeAddress;

import com.bytechainx.psi.common.kit.excel.XlsReadRule.Column;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

public class XlsWriter {

	private Log LOG = Log.getLog(XlsWriter.class);
    private static final int HEADER_ROW = 1;
    private static final int MAX_ROWS = 65535;
    private String[] sheetNames = new String[]{"Sheet"};
    private int cellWidth = 8000;
    private int headerRowCnt;
    private String[][] headers;
    private String[][] columns;
    private String[][] listColumns = new String[][] {};
    private List<?>[] data;

    public XlsWriter(List<?>... data) {
        this.data = data;
    }

    public static XlsWriter data(List<?>... data) {
        return new XlsWriter(data);
    }

    private static List<List<?>> dice(List<?> num, int chunkSize) {
        int size = num.size();
        int chunk_num = size / chunkSize + (size % chunkSize == 0 ? 0 : 1);
        List<List<?>> result = Lists.newArrayList();
        for (int i = 0; i < chunk_num; i++) {
            result.add(Lists.newArrayList(num.subList(i * chunkSize, i == chunk_num - 1 ? size : (i + 1) * chunkSize)));
        }
        return result;
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
        Preconditions.checkNotNull(columns, "columns can not be null");
        Preconditions.checkArgument(data.length == sheetNames.length && sheetNames.length == headers.length
                && headers.length == columns.length, "data,sheetNames,headers and columns'length should be the same." +
                "(data:" + data.length + ",sheetNames:" + sheetNames.length + ",headers:" + headers.length + ",columns:" + columns.length + ")");
        Preconditions.checkArgument(cellWidth >= 0, "cellWidth can not be less than 0");
        
        Workbook wb = new HSSFWorkbook();
        int dataLen = data.length;
        if (dataLen == 0) {
            return wb;
        }
        if (dataLen > 1) {
            for (int i = 0; i < dataLen; i++) {
                List<?> item = data[i];
                Preconditions.checkArgument(item.size() < MAX_ROWS, "Data [" + i + "] is invalid:invalid data size (" + item.size() + ") outside allowable range (0..65535)");
            }
        } else if (dataLen == 1 && data[0].size() > MAX_ROWS) {
            data = dice(data[0], MAX_ROWS).toArray(new List<?>[]{});
            //update data length
            dataLen = data.length;
            String sheetName = sheetNames[0];
            sheetNames = new String[dataLen];
            for (int i = 0; i < dataLen; i++) {
                sheetNames[i] = sheetName + (i == 0 ? "" : (i + 1));
            }
            String[] header = headers[0];
            headers = new String[dataLen][];
            for (int i = 0; i < dataLen; i++) {
                headers[i] = header;
            }
            String[] column = columns[0];
            columns = new String[dataLen][];
            for (int i = 0; i < dataLen; i++) {
                columns[i] = column;
            }
        }
     
        for (int i = 0; i < dataLen; i++) {
            //make headers
            Sheet sheet = wb.createSheet(sheetNames[i]);
            Row row = null;
            Cell headerCell = null;
            int headerLen = headers[i].length;
            if (headerLen > 0) {
            	row = sheet.createRow(0);
                if (headerRowCnt <= 0) {
                    headerRowCnt = HEADER_ROW;
                }
                headerRowCnt = Math.min(headerRowCnt, MAX_ROWS);
                for (int h = 0, lenH = headerLen; h < lenH; h++) {
                    if (cellWidth > 0) {
                        sheet.setColumnWidth(h, cellWidth);
                    }
                    headerCell = row.createCell(h);
                    headerCell.setCellValue(headers[i][h]);
                    
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
            }
            //make rows
            CellStyle cellStyle = setCellStyle(sheet);
            int rowNum = headerRowCnt;;
            for (int j = 0, len = data[i].size(); j < len; j++) {
            	row = sheet.createRow(rowNum);
                Object obj = data[i].get(j);
                if (obj == null) {
                    continue;
                }
                int listSize = 0; // 子列表数量
                if (obj instanceof Map) {
                	listSize = processAsMap(sheet, cellStyle, columns[i], listColumns[i], row, rowNum, obj);
                } else if (obj instanceof Model) {
                	listSize = processAsModel(sheet, cellStyle, columns[i], listColumns[i], row, rowNum, obj);
                } else if (obj instanceof Record) {
                	listSize = processAsRecord(sheet, cellStyle, columns[i], listColumns[i], row, rowNum, obj);
                } else {
                    throw new PoiException("Not support type[" + obj.getClass() + "]");
                }
                rowNum += listSize;
            }
        }
        return wb;
    }

    @SuppressWarnings("unchecked")
    private int processAsMap(Sheet sheet, CellStyle cellStyle, String[] columns, String[] listColumns, Row row, int rowNum, Object obj) {
        Cell cell = null;
        Map<String, Object> map = (Map<String, Object>) obj;
        int cellIdx = 0;
        int startListIndex = 0;
        for (int idx = 0; idx < columns.length; idx++) {
			cell = row.createCell(cellIdx++);
			cell.setCellStyle(cellStyle);
			String column = columns[idx];
			if(column.equals("list")) { // 有子列，合并单元格
				startListIndex = idx; // 列表是从第几列开始的
				List<Model<?>> valList = (List<Model<?>>) map.get(column);
				if(valList == null) {
					valList = new ArrayList<>();
				}
				for (int index = 0; index < valList.size(); index++) {
					Model<?> model = valList.get(index);
					Row _row = row;
					int _cellIndex = cellIdx - 1;
					if(index > 0) {
						_row = sheet.createRow(rowNum + index);
					}
					for (int colidx = 0; colidx < listColumns.length; colidx++) {
						cell = _row.createCell(_cellIndex++);
						cell.setCellStyle(cellStyle);
						Object val = model.get(listColumns[colidx]);
			            if (null == val) {
			                cell.setCellValue("");
						} else if (TypeKit.isNumeric(val)) {
							BigDecimal bdVal = new BigDecimal(val.toString()).setScale(2,BigDecimal.ROUND_HALF_UP);
							cell.setCellValue(bdVal.stripTrailingZeros().toPlainString());
						} else if (TypeKit.isBoolean(val)) {
							cell.setCellValue(Boolean.valueOf(val.toString()));
						} else {
							cell.setCellValue(val + "");
						}
					}
				}
				cellIdx += listColumns.length-1;
				
			} else {
				Object val = map.get(column);
	            if (null == val) {
	                cell.setCellValue("");
				} else if (TypeKit.isNumeric(val)) {
					cell.setCellValue(Double.valueOf(val.toString()));
				} else if (TypeKit.isBoolean(val)) {
					cell.setCellValue(Boolean.valueOf(val.toString()));
				} else {
					cell.setCellValue(val + "");
				}
			}
        }
        List<Model<?>> valList = (List<Model<?>>) map.get("list");
        if(valList != null && !valList.isEmpty() && valList.size() > 1) {
        	for (int index = 0; index < columns.length+listColumns.length; index++) {
        		if(index >= startListIndex && index < startListIndex + listColumns.length) {
        			continue;
        		}
        		CellRangeAddress region = new CellRangeAddress(rowNum, rowNum+ valList.size()-1, (short) index, (short) index); // int firstRow, int lastRow, int firstCol, int lastCol
        		sheet.addMergedRegion(region);
        	}
        }
        return valList == null ? 1 : valList.size();
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

    private int processAsModel(Sheet sheet, CellStyle cellStyle, String[] columns, String[] listColumns, Row row, int rowNum, Object obj) {
        Model<?> model = (Model<?>) obj;
        return processAsMap(sheet, cellStyle, columns, listColumns, row, rowNum, model.toMap());
    }

    private int processAsRecord(Sheet sheet, CellStyle cellStyle, String[] columns, String[] listColumns, Row row, int rowNum, Object obj) {
        Record record = (Record) obj;
        return processAsMap(sheet, cellStyle, columns, listColumns, row,rowNum, record.getColumns());
    }

    public XlsWriter sheetName(String sheetName) {
        this.sheetNames = new String[]{sheetName};
        return this;
    }

    public XlsWriter sheetNames(String... sheetName) {
        this.sheetNames = sheetName;
        return this;
    }

    public XlsWriter cellWidth(int cellWidth) {
        this.cellWidth = cellWidth;
        return this;
    }

    public XlsWriter headerRow(int headerRow) {
        this.headerRowCnt = headerRow;
        return this;
    }

    public XlsWriter header(String... header) {
        this.headers = new String[][]{header};
        return this;
    }

    public XlsWriter headers(String[]... headers) {
        this.headers = headers;
        return this;
    }

    public XlsWriter column(String... column) {
        this.columns = new String[][]{column};
        return this;
    }

    public XlsWriter columns(String[]... columns) {
        this.columns = columns;
        return this;
    }
    
    public XlsWriter columns(Column... columns) {
    	String[] headers = new String[columns.length];
    	String[] cols = new String[columns.length];	
    	for (int i = 0; i < columns.length; i++) {
			Column col = columns[i];
			headers[i] = col.getHeader();
			cols[i] = col.getAttr();
		}
    	this.header(headers);
    	this.column(cols);
    	return this;
    }

	public XlsWriter listColumns(String[]... listColumns) {
		 this.listColumns = listColumns;
	     return this;
	}

}