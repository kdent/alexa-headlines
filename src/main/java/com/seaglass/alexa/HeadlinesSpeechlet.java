package com.seaglass.alexa;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

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

	public static int MAX_CONSUMABLE_ITEMS = 3;
	private static final Logger log = LoggerFactory.getLogger(HeadlinesSpeechlet.class);
	private static String newYorkTimesKey = null;

	@Override
	public SpeechletResponse onIntent(IntentRequest request, Session session) throws SpeechletException {

		log.info("onIntent requestId=" + request.getRequestId() + ", sessionId=" + session.getSessionId());
		setAPIKey();

        Intent intent = request.getIntent();
        DialogStateObj dialogState = updateDialogState(session);
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

	private DialogStateObj updateDialogState(Session session) {
		DialogStateObj dialogState = new DialogStateObj();
		String obj = (String)session.getAttribute("lastStartingItem");
		if (obj == null) {
			dialogState.setLastStartingItem(0);
		} else {
	        dialogState.setLastStartingItem((Integer) Integer.parseInt(obj));			
		}
		dialogState.setNextItem(dialogState.getLastStartingItem() + MAX_CONSUMABLE_ITEMS);

		obj = (String)session.getAttribute("requestedSection");
        dialogState.setRequestedSection(obj);

        obj = (String) session.getAttribute("currentNode");
        if (obj == null) {
            dialogState.setCurrentNode(Node.INIT);
        } else {
        	dialogState.setCurrentNode(obj);
        }
        log.info("current dialog state: " + dialogState);
		return dialogState;
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

	private List<String> getHeadlines(String requestedSection) throws IOException {
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
