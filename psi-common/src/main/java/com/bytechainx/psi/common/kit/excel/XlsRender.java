/**
 * 
 */
package com.bytechainx.psi.common.kit.excel;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import com.jfinal.log.Log;
import com.jfinal.render.Render;
import com.jfinal.render.RenderException;

public class XlsRender extends Render {

    private final Log LOG = Log.getLog(getClass());
    private final static String CONTENT_TYPE = "application/msexcel;charset=" + getEncoding();
    private List<?>[] data;
    private String[][] headers;
    private String[] sheetNames = new String[]{"Sheet"};
    private int cellWidth;
    private String[] columns = new String[]{};
    private String fileName = "file1.xls";
    private int headerRow;

    public XlsRender(List<?>[] data) {
        this.data = data;
    }

    public static XlsRender me(List<?>... data) {
        return new XlsRender(data);
    }

    @Override
    public void render() {
        response.reset();
        response.setHeader("Content-Disposition", "attachment;Filename=" + this.fileName);
        response.setContentType(CONTENT_TYPE);
        OutputStream os = null;
        try {
            os = response.getOutputStream();
            XlsWriter.data(data).sheetNames(sheetNames).headerRow(headerRow).headers(headers).columns(columns)
                    .cellWidth(cellWidth).write().write(os);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new RenderException(e);
        } finally {
            try {
                if (os != null) {
                    os.flush();
                    os.close();
                }
            } catch (IOException e) {
            	e.printStackTrace();
                LOG.error(e.getMessage(), e);
                throw new RenderException(e);
            }
        }
    }

    public XlsRender headers(String[]... headers) {
        this.headers = headers;
        return this;
    }

    public XlsRender headerRow(int headerRow) {
        this.headerRow = headerRow;
        return this;
    }

    public XlsRender columns(String... columns) {
        this.columns = columns;
        return this;
    }

    public XlsRender sheetName(String... sheetName) {
        this.sheetNames = sheetName;
        return this;
    }

    public XlsRender cellWidth(int cellWidth) {
        this.cellWidth = cellWidth;
        return this;
    }

    public XlsRender fileName(String fileName) {
        this.fileName = fileName;
        return this;
    }
}
