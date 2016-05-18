package com.seaglass.alexa;

public class DialogModel {

	private String requestedSection;



	public enum State {
		INIT,
		LAUNCH,
		START_LIST,
		IN_LIST,
		HELP
	}

	public enum Symbol {
		Launch,
		List,
		Yes,
		No,
		Help
	}


}
