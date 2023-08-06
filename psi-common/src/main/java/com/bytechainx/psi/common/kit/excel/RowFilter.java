package com.bytechainx.psi.common.kit.excel;

import java.util.List;

public interface RowFilter {
	boolean doFilter(int rowNum, List<Object> list);
}
