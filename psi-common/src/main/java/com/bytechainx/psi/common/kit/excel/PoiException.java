package com.bytechainx.psi.common.kit.excel;

public class PoiException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public PoiException(String message) {
		super(message);
	}

    public PoiException(Throwable cause) {
        super(cause);
    }
    
}
