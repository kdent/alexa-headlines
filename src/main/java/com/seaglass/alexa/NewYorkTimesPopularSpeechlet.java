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
	public SpeechletResponse onIntent(IntentRequest req, Session session)
			throws SpeechletException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SpeechletResponse onLaunch(LaunchRequest req, Session session)
			throws SpeechletException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onSessionEnded(SessionEndedRequest req, Session session)
			throws SpeechletException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSessionStarted(SessionStartedRequest req, Session session)
			throws SpeechletException {
		// TODO Auto-generated method stub
		
	}




}
