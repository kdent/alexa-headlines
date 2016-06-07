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
import com.seaglass.alexa.DialogManager.State;
import com.seaglass.alexa.exceptions.NytApiException;

/*
 * TODO:
 *     - cache results from the NYT API (DynamoDB)
 *     - investigate enhancing TTS by analyzing POS tags to give Alexa better guidance, e.g. 
 *       The following words rhyme with said: bed, fed, <w role="ivona:VBD">read</w>
 *
 *       implement the rest of the built-in intents
 *     - include a card in the response (with article summaries)
 */

public class HeadlinesSpeechlet implements Speechlet {

    private static final Logger log = LoggerFactory.getLogger(HeadlinesSpeechlet.class);

    @Override
    public SpeechletResponse onIntent(IntentRequest request, Session session) throws SpeechletException {

        log.info("onIntent requestId=" + request.getRequestId() + ", sessionId=" + session.getSessionId());

        Intent intent = request.getIntent();
        DialogContext dialogContext = retrieveDialogContext(session);
        if (dialogContext == null) {
            dialogContext = new DialogContext();
        }
        log.info("retrieved dialog context: " + dialogContext);

        DialogManager.Symbol currentSymbol = null;

        /*
         * If for some reason there's no intent, there is a fatal problem.
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
                    if (! NewYorkTimesArticle.isSection(requestedSection)) {
                        log.error("requestedSection: " + requestedSection + " is unknown");
                        return ResponseGenerator.errorResponse(LanguageGenerator.unknownSectionError());
                    }

                    // If someone asks for a new section, reset the list pointer to the top of the list.
                    String contextRequestedSection = dialogContext.getRequestedSection();
                    if (contextRequestedSection != null && (! contextRequestedSection.equals(requestedSection))) {
                        dialogContext.setNextItem(0);
                        dialogContext.setLastStartingItem(0);
                    }
                }
            }
            dialogContext.setRequestedSection(requestedSection);
        } else if (intentName.equals("AMAZON.RepeatIntent")) {
            dialogContext.setNextItem(dialogContext.getLastStartingItem());
        } else if (intentName.equals("AMAZON.StartOverIntent")) {
            dialogContext.setNextItem(0);
            dialogContext.setLastStartingItem(0);
        }

        /*
         * Transition to next state based on current state and input symbol.
         */
        currentSymbol = DialogManager.getSymbol(intentName);
        if (currentSymbol == null)
            throw new SpeechletException("Unrecognized intent: " + intentName);
        dialogContext.setCurrentState(DialogManager.getNextState(dialogContext, currentSymbol));
        log.info("new dialog context: " + dialogContext);

        /*
         * Update the state and get the response to send.
         */
        SpeechletResponse resp = null;
        try {
            resp = ResponseGenerator.generate(dialogContext, KeyReader.getAPIKey());
        } catch (NytApiException ex) {
            log.error(ex.getMessage());
            resp = ResponseGenerator.errorResponse(LanguageGenerator.apiError());
        } catch (IOException ex) {
            log.error(ex.getMessage());
            resp = ResponseGenerator.errorResponse(LanguageGenerator.apiError());
        }

        log.info("storing dialog context: " + dialogContext);
        storeDialogContext(session, dialogContext);
        return resp;
    }

    @Override
    public SpeechletResponse onLaunch(LaunchRequest request, Session session) throws SpeechletException {
        log.info("onLaunch requestId=" + request.getRequestId() + ", sessionId=" + session.getSessionId());

        DialogContext dialogContext = new DialogContext();
        dialogContext.setCurrentState(DialogManager.getNextState(dialogContext, DialogManager.getSymbol("Launch")));
        SpeechletResponse resp = null;
        try {
            resp = ResponseGenerator.generate(dialogContext, KeyReader.getAPIKey());
        } catch (NytApiException ex) {
            log.error(ex.getMessage());
            resp = ResponseGenerator.errorResponse(LanguageGenerator.apiError());
        } catch (IOException ex) {
            log.error(ex.getMessage());
            resp = ResponseGenerator.errorResponse(LanguageGenerator.apiError());
        }

        return resp;
    }

    @Override
    public void onSessionEnded(SessionEndedRequest request, Session session) throws SpeechletException {
        log.info("onSessionEnded requestId=" + request.getRequestId() + ", sessionId=" + session.getSessionId());
    }

    @Override
    public void onSessionStarted(SessionStartedRequest request, Session session) throws SpeechletException {
        log.info("onSessionStarted requestId=" + request.getRequestId() + ", sessionId=" + session.getSessionId());
    }

    private DialogContext retrieveDialogContext(Session session) {
        DialogContext dialogContext = new DialogContext();
        Integer lastStartingItem = (Integer) session.getAttribute(DialogContext.LAST_STARTING_ITEM);
        if (lastStartingItem == null) {
            dialogContext.setLastStartingItem(0);
        } else {
            dialogContext.setLastStartingItem(lastStartingItem);            
        }

        Integer nextItem = (Integer) session.getAttribute(DialogContext.NEXT_ITEM);
        if (nextItem == null) {
            dialogContext.setNextItem(0);
        } else {
            dialogContext.setNextItem(nextItem);
        }

        Integer listLength = (Integer) session.getAttribute(DialogContext.LIST_LENGTH);
        if (listLength == null) {
            dialogContext.setListLength(0);
        } else {
            dialogContext.setListLength(listLength);
        }

        String requestedSection = (String) session.getAttribute(DialogContext.REQUESTED_SECTION);
        dialogContext.setRequestedSection(requestedSection);

        String currentState = (String) session.getAttribute(DialogContext.CURRENT_STATE);
        if (currentState == null) {
            dialogContext.setCurrentState(State.INIT);
        } else {
            dialogContext.setCurrentState(currentState);
        }
        return dialogContext;
    }

    private void storeDialogContext(Session session, DialogContext dialogContext) {
        session.setAttribute(DialogContext.LAST_STARTING_ITEM, dialogContext.getLastStartingItem());
        session.setAttribute(DialogContext.NEXT_ITEM, dialogContext.getNextItem());
        session.setAttribute(DialogContext.LIST_LENGTH, dialogContext.getListLength());
        session.setAttribute(DialogContext.REQUESTED_SECTION, dialogContext.getRequestedSection());
        session.setAttribute(DialogContext.CURRENT_STATE, dialogContext.getCurrentState());
    }

}
