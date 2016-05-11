package com.seaglass.alexa;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.Speechlet;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SsmlOutputSpeech;

/*
 * TODO:
 *     - implement the built-in intents
 *     - include a card in the response (with article summaries)
 *     - change to 3 at a time and ask user about continuing or not
 *     - cache results from the NYT API (DynamoDB)
 *     - investigate enhancing TTS by analyzing POS tags to give Alexa better guidance, e.g. 
 *       The following words rhyme with said: bed, fed, <w role="ivona:VBD">read</w>
 */

public class HeadlinesSpeechlet implements Speechlet {

	private static final Logger log = Logger.getLogger(HeadlinesSpeechlet.class);
	private static String newYorkTimesKey = null;

	@Override
	public SpeechletResponse onIntent(IntentRequest request, Session session) throws SpeechletException {
        log.info("onIntent requestId=" + request.getRequestId() + ", sessionId=" + session.getSessionId());
        SpeechletResponse resp = new SpeechletResponse();
        Intent intent = request.getIntent();
        String listIntroText = null;
        String requestedSection = null;

        if (intent == null) {
        	throw new SpeechletException("Received a NULL intent");
        }

        /* 
         * Determine user's request intent.
         * TODO: add built-in intents to the schema and re-upload.
         */
        String intentName = intent.getName();
        if (intentName.equals("MostPopularIntentAllSections")) {
        	listIntroText = "Here is the list of most popular articles:";
        } else if (intentName.equals("MostPopularIntentBySection")) {
        	Slot section = intent.getSlot("Section");
        	if (section != null) {
        		requestedSection = section.getValue().toLowerCase();
        		if ("all".equals(requestedSection) || "everything".equals(requestedSection)) {
        			requestedSection = null;
        		}
        	}
        	listIntroText = "Here is the list ot most popular articles in the " + requestedSection + "section";
        } else if (intentName.equals("AMAZON.HelpIntent")) {
        	// TODO: to be implemented
        } else {
        	throw new SpeechletException("Unrecognized intent: " + intentName);
        }

        /*
         * Retrieve requested information from the New York Times API and formulate
         * the response output.
         */
    	NewYorkTimesPopularClient apiClient = new NewYorkTimesPopularClient(newYorkTimesKey);
    	List<NewYorkTimesArticle> articleList = null;
    	try {
    		articleList = (requestedSection == null) ? apiClient.getArticleList() : apiClient.getArticleList(requestedSection);
    		if (articleList == null) {
    			throw new IOException();
    		}
		} catch (IOException e) {
			log.error("Error retrieving article list from the New York Times API", e);
			resp = ErrorResponse("Sorry, I had a problem trying to get the list from the New York Times site. Please try again later.");
		}
    	String responseText = null;
    	if (articleList.size() == 0) {
    		responseText = "<speak>No articles were found.</speak>";
    	} else {
        	List<String> titles = articleList.subList(0, 4).stream().map(NewYorkTimesArticle::getTitle).collect(Collectors.toList());
    		responseText = LanguageGenerator.itemListResponse(listIntroText, titles);
    	}
		SsmlOutputSpeech outputSpeech = new SsmlOutputSpeech();
		outputSpeech.setSsml(responseText);
		resp.setOutputSpeech(outputSpeech);

        return resp;
	}

	@Override
	public SpeechletResponse onLaunch(LaunchRequest request, Session session) throws SpeechletException {
        log.info("onLaunch requestId=" + request.getRequestId() + ", sessionId=" + session.getSessionId());
        SpeechletResponse resp = new SpeechletResponse();

        String outputText = "Welcome to News Guy's most popular stories list. You may ask for headlines from all the sections or name a particular one.";
        String repromptText = "<speak>Please choose a section like: Business <break time=\"0.2s\" />, Science <break time=\"0.2s\"/> or All Sections.</speak>";

        PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
        SsmlOutputSpeech repromptSpeech = new SsmlOutputSpeech();
        outputSpeech.setText(outputText);
        repromptSpeech.setSsml(repromptText);
        Reprompt repromptObj = new Reprompt();
        repromptObj.setOutputSpeech(repromptSpeech);

        resp.setOutputSpeech(outputSpeech);
        resp.setReprompt(repromptObj);

        return resp;
	}

	@Override
	public void onSessionEnded(SessionEndedRequest request, Session session) throws SpeechletException {
        log.info("onSessionEnded requestId=" + request.getRequestId() + ", sessionId=" + session.getSessionId());
	}

	@Override
	public void onSessionStarted(SessionStartedRequest request, Session session) throws SpeechletException {
        log.info("onSessionStarted requestId=" + request.getRequestId() + ", sessionId=" + session.getSessionId());

        if (newYorkTimesKey == null) {
        	BufferedReader keyReader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/nyt_key")));
        	try {
				newYorkTimesKey = keyReader.readLine();
	        	keyReader.close();
			} catch (IOException e) {
				log.error("Error reading New York Times key file", e);
			}
        }
	}

	private SpeechletResponse ErrorResponse(String errorText) {
		SpeechletResponse resp = new SpeechletResponse();
		PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
		outputSpeech.setText(errorText);
		resp.setOutputSpeech(outputSpeech);
		resp.setShouldEndSession(true);
		return resp;
	}



}
