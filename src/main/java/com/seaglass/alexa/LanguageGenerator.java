package com.seaglass.alexa;

import java.util.List;

public class LanguageGenerator {

	private static final String LIST_INTRO = "Here's the list";

	public static String itemListResponse(List<String> itemList, DialogState dialogState) {
		StringBuilder responseText = new StringBuilder();

		if (dialogState.getNextItem() == 0) {
			responseText.append("<speak>" + LIST_INTRO);
		}
		for (String item : itemList) {
			responseText.append(" " + item + "<break time=\"0.5s\" />");
		}

		responseText.append("</speak>");
		return responseText.toString();
	}

}
