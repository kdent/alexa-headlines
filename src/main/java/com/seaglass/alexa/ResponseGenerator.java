package com.seaglass.alexa;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.SsmlOutputSpeech;

public class ResponseGenerator {

	private static final Logger log = LoggerFactory.getLogger(HeadlinesSpeechlet.class);

	public static SpeechletResponse generate(DialogContext dialogState, String newYorkTimesKey) {
		SpeechletResponse resp = new SpeechletResponse();
    	resp.setShouldEndSession(false);
    	String requestedSection = dialogState.getRequestedSection();
        String responseText = null;
        switch(dialogState.getCurrentState()) {
        case INIT:
        	break;
		case HELP:
			responseText = LanguageGenerator.helpResponse();
			break;
		case IN_LIST:
        	List<String> headlineList = null;
        	try {
        		headlineList = getHeadlines(requestedSection, newYorkTimesKey);
        	} catch (IOException ex) {
    			log.error("Error retrieving article list from the New York Times API", ex);
    			resp.setShouldEndSession(true);
    			responseText = LanguageGenerator.apiError();
    			break;
        	}
        	if (headlineList.size() > 0) {
        		responseText = LanguageGenerator.itemListResponse(dialogState, headlineList);
        	} else {
        		responseText = LanguageGenerator.emptyResponse(dialogState);
        	}
			break;
		case LAUNCH:
			break;
		case UNKNOWN:
			break;
		default:
			break;
        }

    	SsmlOutputSpeech outputSpeech = new SsmlOutputSpeech();
    	outputSpeech.setSsml(responseText);
		resp.setOutputSpeech(outputSpeech);
		return resp;
	}
	private static List<String> getHeadlines(String requestedSection, String newYorkTimesKey) throws IOException {
		if (requestedSection == null || requestedSection.length() < 1) {
			throw new RuntimeException("requestedSection paramater cannot be null or empty");
		}
    	NYTimesTopStoriesClient apiClient = new NYTimesTopStoriesClient(newYorkTimesKey);
    	List<NewYorkTimesArticle> articleList = apiClient.getArticleList(requestedSection);
   		if (articleList == null) {
   			throw new IOException();
   		}
   		log.info("retrieved " + articleList.size() + " article headlines");
		return articleList.stream().map(NewYorkTimesArticle::getTitle).collect(Collectors.toList());
	}

}
