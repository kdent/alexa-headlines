package com.seaglass.alexa;

import java.util.List;

public class LanguageGenerator {

	public static String itemListResponse(String listIntro, List<String> itemList) {
		StringBuilder responseText = new StringBuilder("<speak>" + listIntro);

		for (String item : itemList) {
			responseText.append(" " + item + "<break time=\"0.5s\" />");
		}

		responseText.append("</speak>");
		return responseText.toString();
	}

}
