package com.seaglass.alexa;

import java.util.List;

public class LanguageGenerator {

	public static String itemListResponse(String listIntro, List<String> itemList) {
		StringBuilder responseText = new StringBuilder(listIntro);

		for (String item : itemList) {
			responseText.append(" " + item + "<break time=\"0.2s\" />");
		}

		return responseText.toString();
	}

}
