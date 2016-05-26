package com.seaglass.alexa;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

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
	private static int MAX_ITEMS = 3;

	@Override
	public SpeechletResponse onIntent(IntentRequest request, Session session) throws SpeechletException {
        log.info("onIntent requestId=" + request.getRequestId() + ", sessionId=" + session.getSessionId());
        Intent intent = request.getIntent();
        String requestedSection = null;
        DialogState dialogState = new DialogState();
        dialogState.setNextItem((int) session.getAttribute("nextItem"));
        dialogState.setRequestedSection((String) session.getAttribute("requestedSection"));
        dialogState.setCurrentNode((String) session.getAttribute("currentNode"));
        DialogManager.Symbol currentSymbol = null;

        /*
         * If for some reason there's no intent, we've got a fatal problem.
         */
        if (intent == null) {
        	throw new SpeechletException("Received a NULL intent");
        }

        /* 
         * Determine the user's request.
         */
        String intentName = intent.getName();

        if (intentName.equals("StartList")) {
        	Slot section = intent.getSlot("Section");
        	if (section != null) {
        		requestedSection = section.getValue().toLowerCase();
        		if ("all".equals(requestedSection) || "everything".equals(requestedSection)) {
        			requestedSection = null;
        		}
        	}
        	dialogState.setRequestedSection(requestedSection);
        	currentSymbol = DialogManager.Symbol.RequestList;
        } else if (intentName.equals("AMAZON.HelpIntent")) {
        	// TODO: to be implemented
        	currentSymbol = DialogManager.Symbol.Help;
        } else {
        	throw new SpeechletException("Unrecognized intent: " + intentName);
        }

        /*
         * Decide what action to take based on the current state.
         */
        dialogState.setCurrentNode(DialogManager.getNextState(dialogState.getCurrentNode(), currentSymbol, dialogState));
    	SpeechletResponse resp = new SpeechletResponse();
        String responseText = null;
        switch(dialogState.getCurrentNode()) {
        case INIT:
        	break;
		case HELP:
			responseText = LanguageGenerator.helpResponse();
			break;
		case IN_LIST:
        	List<String> headlineList = null;
        	try {
        		headlineList = getHeadlines(requestedSection);
        	} catch (IOException ex) {
    			log.error("Error retrieving article list from the New York Times API", ex);
    			resp.setShouldEndSession(true);
    			responseText = "Sorry, I had a problem trying to get the list from the New York Times site. Please try again later.";
        	}
        	int nextItem = dialogState.getNextItem();
        	boolean useIntro = (nextItem == 0) ? true : false;
        	headlineList.subList(nextItem, nextItem + MAX_ITEMS);
        	responseText = LanguageGenerator.itemListResponse(headlineList, useIntro);
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

	private List<String> getHeadlines(String requestedSection) throws IOException {
		List<String> headlineList = new ArrayList<String>();
    	NewYorkTimesPopularClient apiClient = new NewYorkTimesPopularClient(newYorkTimesKey);
    	List<NewYorkTimesArticle> articleList = null;
   		articleList = (requestedSection == null) ? apiClient.getArticleList() : apiClient.getArticleList(requestedSection);
   		if (articleList == null) {
   			throw new IOException();
   		}
		return headlineList;
	}

}
