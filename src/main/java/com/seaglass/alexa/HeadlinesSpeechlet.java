package com.seaglass.alexa;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.seaglass.alexa.DialogManager.State;

/*
 * TODO:
 *     - implement the built-in intents
 *     - include a card in the response (with article summaries)
 *     - change to 3 at a time and ask user about continuing or not
 *     - cache results from the NYT API (DynamoDB)
 *     - investigate enhancing TTS by analyzing POS tags to give Alexa better guidance, e.g. 
 *       The following words rhyme with said: bed, fed, <w role="ivona:VBD">read</w>
 *       
 *       
 *       Test NYTimes client with forbidden access
 *       fix listLength not getting set
 *       fix nextItem not being set (or being reset)
 *       import list of sections to make sure that the section you get is on the list
 */

public class HeadlinesSpeechlet implements Speechlet {

	public static int MAX_CONSUMABLE_ITEMS = 3;
	private static final Logger log = LoggerFactory.getLogger(HeadlinesSpeechlet.class);
	private static String newYorkTimesKey = null;

	@Override
	public SpeechletResponse onIntent(IntentRequest request, Session session) throws SpeechletException {

		log.info("onIntent requestId=" + request.getRequestId() + ", sessionId=" + session.getSessionId());
		setAPIKey();

        Intent intent = request.getIntent();
        DialogContext dialogContext = retrieveDialogContext(session);
        if (dialogContext == null) {
        	dialogContext = new DialogContext();
        }
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
        		requestedSection = sectionSlot.getValue();
        		if (requestedSection != null) {
        				requestedSection = requestedSection.toLowerCase();
        		}
        	}
        	dialogContext.setRequestedSection(requestedSection);
        }

        /*
         * Transition to next state based on current state and input symbol.
         */
        currentSymbol = DialogManager.getSymbol(intentName);
        if (currentSymbol == null)
        	throw new SpeechletException("Unrecognized intent: " + intentName);
        dialogContext.setCurrentNode(DialogManager.getNextState(dialogContext.getCurrentState(), currentSymbol, dialogContext));

        /*
         * Update the state and get the response to send.
         */
		storeDialogContext(session, dialogContext);
    	SpeechletResponse resp = ResponseGenerator.generate(dialogContext, newYorkTimesKey);
        return resp;
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
        setAPIKey();
	}

	private DialogContext retrieveDialogContext(Session session) {
		DialogContext dialogState = new DialogContext();
		Integer lastStartingItem = (Integer) session.getAttribute("lastStartingItem");
		if (lastStartingItem == null) {
			dialogState.setLastStartingItem(0);
		} else {
	        dialogState.setLastStartingItem(lastStartingItem);			
		}

		String requestedSection = (String) session.getAttribute("requestedSection");
        dialogState.setRequestedSection(requestedSection);

        String currentNode = (String) session.getAttribute("currentNode");
        if (currentNode == null) {
            dialogState.setCurrentNode(State.INIT);
        } else {
        	dialogState.setCurrentNode(currentNode);
        }
        log.info("current dialog state: " + dialogState);
		return dialogState;
	}

	private void storeDialogContext(Session session, DialogContext dialogState) {
		session.setAttribute("lastStartingItem", dialogState.getLastStartingItem());
		session.setAttribute("requestedSection", dialogState.getRequestedSection());
		session.setAttribute("currentNode", dialogState.getCurrentState());
	}

	private void setAPIKey() throws SpeechletException {
        if (newYorkTimesKey == null) {
        	InputStream nytKeyFile = getClass().getResourceAsStream("/nyt_key");
        	try {
            	if (nytKeyFile == null) {
            		throw new FileNotFoundException("Missing NYT key file. Cannot access headlines API.");
            	}
            	BufferedReader keyReader = new BufferedReader(new InputStreamReader(nytKeyFile));
				newYorkTimesKey = keyReader.readLine();
	        	keyReader.close();
				if (newYorkTimesKey == null || newYorkTimesKey.length() < 1) {
					throw new IOException("NYT API Key is empty or null");
				}
			} catch (IOException e) {
				log.error("Error reading New York Times key file", e);
				throw new SpeechletException(e);
			}
        }
	}

}
