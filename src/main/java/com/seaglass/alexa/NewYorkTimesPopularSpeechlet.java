package com.seaglass.alexa;


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
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SsmlOutputSpeech;

public class NewYorkTimesPopularSpeechlet implements Speechlet {

	private static final Logger log = LoggerFactory.getLogger(NewYorkTimesPopularSpeechlet.class); 

	@Override
	public SpeechletResponse onIntent(IntentRequest request, Session session)
			throws SpeechletException {
        log.info("onIntent requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
        SpeechletResponse resp = new SpeechletResponse();
        Intent intent = request.getIntent();

        if (intent == null) {
        	throw new SpeechletException("Received a NULL intent");
        }
        String intentName = intent.getName();
        if (intentName.equals("NYTPopularIntentAllSections")) {
        	// Retrieve the list of most popular articles.
        } else if (intentName.equals("NYTPopularIntentBySection")) {
        	Slot section = intent.getSlot("Section");
        	String sectionName = section.getName();
        	// Retrieve the list of most popular articles in the requested section.
        } else {
        	throw new SpeechletException("Unrecognized intent: " + intentName);
        }

        return resp;
	}

	@Override
	public SpeechletResponse onLaunch(LaunchRequest request, Session session)
			throws SpeechletException {
        log.info("onLaunch requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
        SpeechletResponse resp = new SpeechletResponse();

        String outputText = "Welcome to N.Y.T.'s most popular list. Which section are you interested in?";
        String repromptText = "Please choose a section like: Business <break time=\"0.2s\" />, Science <break time=\"0.2s\"/> or All Sections.";

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
	public void onSessionEnded(SessionEndedRequest request, Session session)
			throws SpeechletException {
        log.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
	}

	@Override
	public void onSessionStarted(SessionStartedRequest request, Session session)
			throws SpeechletException {
        log.info("onSessionStarted requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());		
	}




}
