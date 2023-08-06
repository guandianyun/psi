/**
 * 
 */
package com.bytechainx.psi.common.kit.excel;

import java.util.HashMap;
import java.util.Map;

import com.bytechainx.psi.common.kit.excel.interceptor.PostListProcessor;
import com.bytechainx.psi.common.kit.excel.interceptor.PostXlsProcessor;
import com.bytechainx.psi.common.kit.excel.interceptor.PreListProcessor;
import com.bytechainx.psi.common.kit.excel.interceptor.PreXlsProcessor;
import com.jfinal.plugin.activerecord.Model;


public class XlsReadRule {
	
	/**
	 * Data From
	 */
	private int start;

	/**
	 * Data End
	 */
	private int end;
	
	/**
	 * Date Column's Format
	 */
	private String dateFormat = "yyyy-MM-dd";

	private PreXlsProcessor preXlsProcessor;

	private PostXlsProcessor postXlsProcessor;

	private PreListProcessor preListProcessor;

	private PostListProcessor postListProcessor;
    
	/**
	 * Data Convert Model's Class
	 */
    private Class<? extends Model<?>> clazz;

    private Map<Integer, Column> columns = new HashMap<Integer, Column>();

    public int getStart() {
        return start;
    }

    public void setStart(int value) {
        this.start = value;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int value) {
        this.end = value;
    }
    
    public String getDateFormat() {
		return dateFormat;
	}

	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	public PreXlsProcessor getPreExcelProcessor() {
        return preXlsProcessor;
    }

    public void setPreExcelProcessor(PreXlsProcessor value) {
        this.preXlsProcessor = value;
    }

    public PostXlsProcessor getPostExcelProcessor() {
        return postXlsProcessor;
    }

    public void setPostExcelProcessor(PostXlsProcessor value) {
        this.postXlsProcessor = value;
    }

    public PreListProcessor getPreListProcessor() {
        return preListProcessor;
    }

    public void setPreListProcessor(PreListProcessor value) {
        this.preListProcessor = value;
    }

    public PostListProcessor getPostListProcessor() {
        return postListProcessor;
    }

    public void setPostListProcessor(PostListProcessor value) {
        this.postListProcessor = value;
    }
    
    public Class<? extends Model<?>> getClazz() {
		return clazz;
	}

	public void setClazz(Class<? extends Model<?>> clazz) {
		this.clazz = clazz;
	}

    public Map<Integer, Column> getColumns() {
        return columns;
    }

    public void setColumns(Map<Integer, Column> columns) {
        this.columns = columns;
    }
    
    public void alignColumn(String... columns) {
    	if (null == columns) {
			return;
		}
    	
    	for (int idx = 0; idx < columns.length; idx++) {
			Column col = Column.create(columns[idx]);
			col.setIndex(idx);
	    	this.columns.put(idx, col);
		}
    }

    public void alignColumn(Column... columns) {
    	if (null == columns) {
			return;
		}
    	for (int idx = 0; idx < columns.length; idx++) {
			Column col = columns[idx];
			col.index = idx;
	    	this.columns.put(idx, col);
		}
    }
    
    public static class Column {

    	private int index;

    	private String attr;
    	
    	private String header;
    	
    	//TODO
    	private String format;

    	private ColumnConvert convert;

    	private ColumnValidate validate;
    	
    	private Column() {
    		
    	}
    	
    	public static Column header(String header, String attr) {
            Column column = new Column();
            column.setAttr(attr);
            column.setHeader(header);
            return column;
    	}

    	public static Column create(String attr) {
            Column column = new Column();
            column.setAttr(attr);
            return column;
        }
    	
    	public static Column create(String attr, String format) {
    		Column column = Column.create(attr);
    		column.setFormat(format);
    		return column;
    	}

        public static Column create(String attr, ColumnConvert convert, ColumnValidate validate) {
            Column column = create(attr);
            column.setConvert(convert);
            column.setValidate(validate);
            return column;
        }
        
        public static Column create(String attr, String format, ColumnConvert convert, ColumnValidate validate) {
            Column column = create(attr, format);
            column.setConvert(convert);
            column.setValidate(validate);
            return column;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int value) {
            this.index = value;
        }

        public String getAttr() {
            return attr;
        }

        public void setAttr(String attr) {
            this.attr = attr;
        }

        public String getHeader() {
			return header;
		}

		public void setHeader(String header) {
			this.header = header;
		}

		public String getFormat() {
			return format;
		}

		public void setFormat(String format) {
			this.format = format;
		}

		public ColumnConvert getConvert() {
            return convert;
        }

        public void setConvert(ColumnConvert value) {
            this.convert = value;
        }

        public ColumnValidate getValidate() {
            return validate;
        }

        public void setValidate(ColumnValidate value) {
            this.validate = value;
        }

        @Override
		public String toString() {
			return "Column [index=" + index + ", attr=" + attr + ", convert=" + convert + ", validate="
					+ validate + "]";
		}
    }

	@Override
	public String toString() {
		return "XlsReadRule [start=" + start + ", end=" + end + ", dateFormat=" + dateFormat + ", preXlsProcessor="
				+ preXlsProcessor + ", postXlsProcessor=" + postXlsProcessor + ", preListProcessor="
				+ preListProcessor + ", postListProcessor=" + postListProcessor + ", clazz=" + clazz + ", columns="
				+ columns + "]";
	}

}