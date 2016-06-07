package com.seaglass.alexa;

import java.io.IOException;

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
import com.amazon.speech.ui.SsmlOutputSpeech;
import com.seaglass.alexa.DialogManager.State;
import com.seaglass.alexa.exceptions.NytApiException;

/*
 * TODO:
 *     - implement the built-in intents
 *     - include a card in the response (with article summaries)
 *     - cache results from the NYT API (DynamoDB)
 *     - investigate enhancing TTS by analyzing POS tags to give Alexa better guidance, e.g. 
 *       The following words rhyme with said: bed, fed, <w role="ivona:VBD">read</w>
 *
 *       check what happens on a speechlet exception. if it's reasonable do that instead of custom response
 *       see if you can store the DialogContext object and retrieve it
 *       import list of sections to make sure that the section you get is on the list
 *       implement the rest of the built-in intents
 */

public class HeadlinesSpeechlet implements Speechlet {

    private static final Logger log = LoggerFactory.getLogger(HeadlinesSpeechlet.class);
    private static String newYorkTimesKey = null;

    @Override
    public SpeechletResponse onIntent(IntentRequest request, Session session) throws SpeechletException {

        log.info("onIntent requestId=" + request.getRequestId() + ", sessionId=" + session.getSessionId());
        try {
            newYorkTimesKey = KeyReader.getAPIKey();
        } catch (IOException ex) {
            log.error(ex.getMessage());
            SpeechletResponse resp = new SpeechletResponse();
            resp.setShouldEndSession(true);
            SsmlOutputSpeech outputSpeech = new SsmlOutputSpeech();
            outputSpeech.setSsml(LanguageGenerator.apiError());
            resp.setOutputSpeech(outputSpeech);
            return resp;
        }

        Intent intent = request.getIntent();
        DialogContext dialogContext = retrieveDialogContext(session);
        if (dialogContext == null) {
            dialogContext = new DialogContext();
        }
        log.info("retrieved dialog context: " + dialogContext);

        DialogManager.Symbol currentSymbol = null;

        /*
         * If for some reason there's no intent, we've got a fatal problem.
         */
        if (intent == null) {
            throw new SpeechletException("Received a NULL intent");
        }

        /* 
         * Get user's intent and requested section if available.
         */
        String intentName = intent.getName();
        String requestedSection = null;

        if (intentName.equals("StartList")) {
            Slot sectionSlot = intent.getSlot("Section");
            if (sectionSlot != null) {
                requestedSection = sectionSlot.getValue();
                if (requestedSection != null) {
                        requestedSection = requestedSection.toLowerCase();
                        String contextRequestedSection = dialogContext.getRequestedSection();
                        // If someone asks for a new section, reset the list pointer to the top of the list.
                        if (contextRequestedSection != null && (! contextRequestedSection.equals(requestedSection))) {
                            dialogContext.setNextItem(0);
                            dialogContext.setLastStartingItem(0);
                        }
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
        dialogContext.setCurrentState(DialogManager.getNextState(dialogContext.getCurrentState(), currentSymbol, dialogContext));
        log.info("new dialog context: " + dialogContext);

        /*
         * Update the state and get the response to send.
         */
        SpeechletResponse resp = null;
        try {
            resp = ResponseGenerator.generate(dialogContext, newYorkTimesKey);
        } catch (NytApiException ex) {
            throw new SpeechletException(ex);
//            resp = new SpeechletResponse();
//            resp.setShouldEndSession(true);
//            SsmlOutputSpeech outputSpeech = new SsmlOutputSpeech();
//            outputSpeech.setSsml(LanguageGenerator.apiError());
//            resp.setOutputSpeech(outputSpeech);
        }

        log.info("storing dialog context: " + dialogContext);
        storeDialogContext(session, dialogContext);
        return resp;
    }

    @Override
    public SpeechletResponse onLaunch(LaunchRequest request, Session session) throws SpeechletException {
        log.info("onLaunch requestId=" + request.getRequestId() + ", sessionId=" + session.getSessionId());
        SpeechletResponse resp = new SpeechletResponse();

        String outputText = LanguageGenerator.welcomeMessage();
 
        SsmlOutputSpeech outputSpeech = new SsmlOutputSpeech();
        outputSpeech.setSsml(outputText);
 
        resp.setShouldEndSession(false);
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
        try {
            newYorkTimesKey = KeyReader.getAPIKey();
        } catch (IOException ex) {
            log.error(ex.getMessage());
        }
    }

    private DialogContext retrieveDialogContext(Session session) {
        DialogContext dialogState = new DialogContext();
        Integer lastStartingItem = (Integer) session.getAttribute("lastStartingItem");
        if (lastStartingItem == null) {
            dialogState.setLastStartingItem(0);
        } else {
            dialogState.setLastStartingItem(lastStartingItem);            
        }

        Integer nextItem = (Integer) session.getAttribute("nextItem");
        if (nextItem == null) {
            dialogState.setNextItem(0);
        } else {
            dialogState.setNextItem(nextItem);
        }

        Integer listLength = (Integer) session.getAttribute("listLength");
        if (listLength == null) {
            dialogState.setListLength(0);
        } else {
            dialogState.setListLength(listLength);
        }

        String requestedSection = (String) session.getAttribute("requestedSection");
        dialogState.setRequestedSection(requestedSection);

        String currentNode = (String) session.getAttribute("currentState");
        if (currentNode == null) {
            dialogState.setCurrentState(State.INIT);
        } else {
            dialogState.setCurrentState(currentNode);
        }
        return dialogState;
    }

    private void storeDialogContext(Session session, DialogContext dialogState) {
        session.setAttribute("lastStartingItem", dialogState.getLastStartingItem());
        session.setAttribute("nextItem", dialogState.getNextItem());
        session.setAttribute("listLength", dialogState.getListLength());
        session.setAttribute("requestedSection", dialogState.getRequestedSection());
        session.setAttribute("currentState", dialogState.getCurrentState());
    }

}
