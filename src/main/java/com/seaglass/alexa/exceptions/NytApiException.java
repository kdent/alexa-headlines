package com.seaglass.alexa.exceptions;

import java.io.IOException;

public class NytApiException extends IOException {

	private static final long serialVersionUID = 1L;

	public NytApiException() {
	}

	public NytApiException(String msg) {
		super(msg);
	}

	public NytApiException(IOException ex) {
		super(ex);
	}

}
