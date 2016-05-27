package com.seaglass.alexa;

import java.util.List;

public class LanguageGenerator {

	private static final String HELP_MSG = "Headlines lists the top news stories.";
	private static final String API_ERROR = "Sorry, I had a problem trying to get the list from the New York Times site. Please try again later.";
	private static final String WELCOME_MSG = "With Headlines, you can get the top stories in the news by section or for all sections";
	private static final String EXAMPLE_MSG = "For example, you can say, What are the top stories in all sections? or What are the top stories in Technology?";
	private static final String PROMPT_MSG = "Which section do you want to hear?";

	public static String itemListResponse(List<String> itemList, String sectionName, boolean useIntro, boolean useContinuer) {
		StringBuilder responseText = new StringBuilder("<speak>");

		if (useIntro) {
			responseText.append("Here's the list of top headlines");
			if (sectionName != null && sectionName.length() > 0) {
				responseText.append(" in " + sectionName);
			}
		}
		for (String item : itemList) {
			responseText.append(" " + item + "<break time=\"0.5s\" />");
		}

		if (useContinuer) {
			responseText.append(" " + "Would you like to hear more?");
		}
		responseText.append("</speak>");
		return responseText.toString();
	}

	public static String welcomeMessage() {
		return WELCOME_MSG + " " + EXAMPLE_MSG + " " + PROMPT_MSG;
	}

	public static String helpResponse() {
		return HELP_MSG + " " + EXAMPLE_MSG;
	}

	public static String apiError() {
		return API_ERROR;
	}
}
