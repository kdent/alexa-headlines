package com.seaglass.alexa;

import java.util.List;

public class LanguageGenerator {

	private static final String LIST_INTRO = "Here's the list";
	private static final String HELP_MSG = "Here's the help message";

	public static String itemListResponse(List<String> itemList, boolean useIntro) {
		StringBuilder responseText = new StringBuilder();

		if (useIntro) {
			responseText.append("<speak>" + LIST_INTRO);
		}
		for (String item : itemList) {
			responseText.append(" " + item + "<break time=\"0.5s\" />");
		}

		responseText.append("</speak>");
		return responseText.toString();
	}

	public static String helpResponse() {
		return HELP_MSG;
	}

}
