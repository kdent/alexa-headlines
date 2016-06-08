package com.seaglass.alexa;

import java.util.List;

public class LanguageGenerator {

    private static final String HELP_MSG = "Headlines lists the top news stories as provided by the The New York Times. To use Headlines";
    private static final String API_ERROR = "Sorry, I had a problem trying to get the list. Please try again later.";
    private static final String WELCOME_MSG = "With Headlines, you get headlines for the top stories in the news";
    private static final String EXAMPLE_MSG = "For example, you can say, What are the top stories in 'Technology'? Or what are the top stories in 'business'";
    private static final String PROMPT_MSG = "Which section do you want to hear?";
    private static final String GENERAL_ERROR = "Sorry, I'm having a problem and can't help you right now.";
    private static final String UNKNOWN_SECTION_ERROR = "I'm sorry I don't understand the section you asked for.";

    public static String itemListResponse(DialogContext dialogState, List<String> itemList) {
        StringBuilder responseText = new StringBuilder("<speak>");

        if (itemList == null || itemList.size() < 1) {
            responseText.append("no items</speak>");
            return responseText.toString();
        }

        int nextItem = dialogState.getNextItem();
        int lastItem = nextItem + HeadlinesProperties.MAX_CONSUMABLE_ITEMS;
        if (lastItem >= itemList.size()) {
            lastItem = itemList.size();
        }
        List<String> deliveryList = itemList.subList(nextItem, lastItem);

        String sectionName = dialogState.getRequestedSection();
        boolean useContinuer = (lastItem >= (itemList.size() - 1)) ? false : true;
        boolean useIntro = (nextItem == 0) ? true : false;

        if (useIntro) {
            responseText.append("Here's the list of top headlines");
            if (sectionName != null && sectionName.length() > 0) {
                responseText.append(" in " + sectionName);
            }
        }
        responseText.append("<break time=\"0.75s\" />");
        for (String item : deliveryList) {
            responseText.append(" " + item + "<break time=\"0.75s\" />");
        }

        if (useContinuer) {
            responseText.append("<break time=\"1s\"/> " + "Would you like to hear more?");
        }
        responseText.append("</speak>");

        // Update the state.
        dialogState.setLastStartingItem(nextItem);
        dialogState.setNextItem(lastItem);

        return responseText.toString();
    }

    public static String welcomeMessage() {
        return "<speak>" + WELCOME_MSG + "<break time=\"0.5s\"/> " + EXAMPLE_MSG + "<break time=\"0.5s\"/> " + PROMPT_MSG + "</speak>";
    }

    public static String helpResponse() {
        return "<speak>" + HELP_MSG + " " + EXAMPLE_MSG + "<break time=\"0.5s\"/> " + PROMPT_MSG + "</speak>";
    }

    public static String apiError() {
        return "<speak>" + API_ERROR + "</speak>";
    }

    public static String generalError() {
        return "<speak>" + GENERAL_ERROR + "</speak>";
    }

    public static String emptyResponse(DialogContext dialogState) {
        String resp = "Sorry, I didn't find any headlines";
        String requestedSection = dialogState.getRequestedSection();
        if (requestedSection != null && requestedSection.length() > 0) {
            resp = resp + " in the " + requestedSection + " section";
        }
        return resp;
    }

    public static String askSection() {
        return "<speak>" + PROMPT_MSG + "</speak>";
    }

    public static String unknownSectionError() {
        return "<speak>" + UNKNOWN_SECTION_ERROR + " " + PROMPT_MSG + "</speak>";
    }
}
