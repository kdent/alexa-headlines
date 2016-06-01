package com.seaglass.alexa;

import java.util.List;

public class LanguageGenerator {

	private static final String HELP_MSG = "Headlines lists the top news stories.";
	private static final String API_ERROR = "Sorry, I had a problem trying to get the list from the New York Times site. Please try again later.";
	private static final String WELCOME_MSG = "With Headlines, you can get the top stories in the news by section or for all sections";
	private static final String EXAMPLE_MSG = "For example, you can say, What are the top stories in all sections? or What are the top stories in Technology?";
	private static final String PROMPT_MSG = "Which section do you want to hear?";

	public static String itemListResponse(DialogStateObj dialogState, List<String> itemList) {
		StringBuilder responseText = new StringBuilder("<speak>");

		if (itemList == null || itemList.size() < 1) {
			responseText.append("no items</speak>");
			return responseText.toString();
		}

		int nextItem = dialogState.getNextItem();
    	int lastItem = nextItem + HeadlinesSpeechlet.MAX_CONSUMABLE_ITEMS;
    	if (lastItem > itemList.size()) {
    		lastItem = itemList.size();
    	}
    	List<String> deliveryList = itemList.subList(nextItem, lastItem);

    	String sectionName = dialogState.getRequestedSection();
    	boolean useContinuer = (lastItem > itemList.size()) ? false : true;
    	boolean useIntro = (nextItem == 0) ? true : false;

    	if (useIntro) {
			responseText.append("Here's the list of top headlines<break time=\"0.75\" />");
			if (sectionName != null && sectionName.length() > 0) {
				responseText.append(" in " + sectionName);
			}
		}
		for (String item : deliveryList) {
			responseText.append(" " + item + "<break time=\"0.5s\" />");
		}

		if (useContinuer) {
			responseText.append(" " + "Would you like to hear more?");
		}
		responseText.append("</speak>");

		// Update the state.
		dialogState.setLastStartingItem(nextItem);
		dialogState.setNextItem(lastItem + 1);

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

	public static String emptyResponse(DialogStateObj dialogState) {
		String resp = "Sorry, I didn't find any headlines";
		String requestedSection = dialogState.getRequestedSection();
		if (requestedSection != null && requestedSection.length() > 0) {
			resp = resp + " in the " + requestedSection + " section";
		}
		return resp;
	}
}
