package com.seaglass.alexa;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.Speechlet;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;

public class NewYorkTimesPopularSpeechlet implements Speechlet {

	private static final Logger log = LoggerFactory.getLogger(NewYorkTimesPopularSpeechlet.class); 

	@Override
	public SpeechletResponse onIntent(IntentRequest request, Session session)
			throws SpeechletException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SpeechletResponse onLaunch(LaunchRequest request, Session session)
			throws SpeechletException {
        log.info("onLaunch requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
		return null;
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
