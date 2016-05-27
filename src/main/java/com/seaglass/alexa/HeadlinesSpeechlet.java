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
import com.amazon.speech.ui.SsmlOutputSpeech;
import com.seaglass.alexa.DialogManager.Node;

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
	private static int MAX_CONSUMABLE_ITEMS = 3;

	@Override
	public SpeechletResponse onIntent(IntentRequest request, Session session) throws SpeechletException {

		log.info("onIntent requestId=" + request.getRequestId() + ", sessionId=" + session.getSessionId());

        Intent intent = request.getIntent();
        DialogStateObj dialogState = mapSessionToDialogState(session);
        DialogManager.Symbol currentSymbol = null;

        /*
         * If for some reason there's no intent, we've got a fatal problem.
         */
        if (intent == null) {
        	throw new SpeechletException("Received a NULL intent");
        }

        /* 
         * Determine the user's request and map it to an input symbol for the DialogManager.
         */
        String intentName = intent.getName();
        String requestedSection = null;

        if (intentName.equals("StartList")) {
        	Slot sectionSlot = intent.getSlot("Section");
        	if (sectionSlot != null) {
        		requestedSection = sectionSlot.getValue().toLowerCase();
        		if ("all".equals(requestedSection) || "everything".equals(requestedSection)) {
        			requestedSection = null;
        		}
        	}
        	dialogState.setRequestedSection(requestedSection);
        	currentSymbol = DialogManager.Symbol.RequestList;
        } else if (intentName.equals("AMAZON.HelpIntent")) {
        	currentSymbol = DialogManager.Symbol.Help;
        } else {
        	throw new SpeechletException("Unrecognized intent: " + intentName);
        }

        /*
         * Transition to next state based on current state and input symbol.
         */
        dialogState.setCurrentNode(DialogManager.getNextState(dialogState.getCurrentNode(), currentSymbol, dialogState));

        /*
         * Decide what action to take based on the updated state and prepare the response to send.
         */
    	SpeechletResponse resp = new SpeechletResponse();
    	resp.setShouldEndSession(false);
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
    			responseText = LanguageGenerator.apiError();
        	}
        	int nextItem = dialogState.getLastStartingItem() + MAX_CONSUMABLE_ITEMS;
        	headlineList.subList(nextItem, nextItem + MAX_CONSUMABLE_ITEMS);
        	boolean useContinuer = (nextItem > headlineList.size()) ? false : true;
        	boolean useIntro = (nextItem == 0) ? true : false;
        	responseText = LanguageGenerator.itemListResponse(headlineList, requestedSection, useIntro, useContinuer);
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

	private DialogStateObj mapSessionToDialogState(Session session) {
		DialogStateObj dialogState = new DialogStateObj();
		String obj = (String)session.getAttribute("lastStartingItem");
		if (obj == null) {
			dialogState.setLastStartingItem(0);
		} else {
	        dialogState.setLastStartingItem((Integer) Integer.parseInt(obj));			
		}

		obj = (String)session.getAttribute("requestedSection");
        dialogState.setRequestedSection(obj);

        obj = (String) session.getAttribute("currentNode");
        if (obj == null) {
            dialogState.setCurrentNode(Node.INIT);
        } else {
        	dialogState.setCurrentNode(obj);
        }
		return dialogState;
	}

	@Override
	public SpeechletResponse onLaunch(LaunchRequest request, Session session) throws SpeechletException {
        log.info("onLaunch requestId=" + request.getRequestId() + ", sessionId=" + session.getSessionId());
        SpeechletResponse resp = new SpeechletResponse();

        String outputText = LanguageGenerator.welcomeMessage();
 
        PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
        outputSpeech.setText(outputText);
 
        resp.setOutputSpeech(outputSpeech);
 
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
