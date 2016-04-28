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
 *     - implement by-section intent
 *     - include a card in the response (with links to articles?)
 */

public class NYTPopularSpeechlet implements Speechlet {

	private static final Logger log = Logger.getLogger(NYTPopularSpeechlet.class);
	private static String newYorkTimesKey = null;

	@Override
	public SpeechletResponse onIntent(IntentRequest request, Session session) throws SpeechletException {
        log.info("onIntent requestId=" + request.getRequestId() + ", sessionId=" + session.getSessionId());
        SpeechletResponse resp = new SpeechletResponse();
        Intent intent = request.getIntent();

        if (intent == null) {
        	throw new SpeechletException("Received a NULL intent");
        }
        String intentName = intent.getName();
        if (intentName.equals("NYTPopularIntentAllSections")) {
        	// Retrieve the list of most popular articles.
        	NewYorkTimesPopularClient apiClient = new NewYorkTimesPopularClient(newYorkTimesKey);
        	List<NewYorkTimesArticle> articleList = null;
        	try {
				articleList = apiClient.getArticleList();
			} catch (IOException e) {
				log.error("Error retrieving article list from the New York Times API", e);
				resp = ErrorResponse("Sorry, I had a problem trying to get the list from the New York Times site. Please try again later.");
			}
			List<String> titles = articleList.stream().map(NewYorkTimesArticle::getTitle).collect(Collectors.toList());
			String responseText = LanguageGenerator.itemListResponse("Here is the list of most popular articles:", titles);
			SsmlOutputSpeech outputSpeech = new SsmlOutputSpeech();
			outputSpeech.setSsml(responseText);
			Reprompt reprompt = new Reprompt();
			PlainTextOutputSpeech repromptSpeech = new PlainTextOutputSpeech();
			repromptSpeech.setText("Would you like to hear the list again?");
			resp.setOutputSpeech(outputSpeech);
			resp.setReprompt(reprompt);
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
	public SpeechletResponse onLaunch(LaunchRequest request, Session session) throws SpeechletException {
        log.info("onLaunch requestId=" + request.getRequestId() + ", sessionId=" + session.getSessionId());
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
	public void onSessionEnded(SessionEndedRequest request, Session session) throws SpeechletException {
        log.info("onSessionEnded requestId=" + request.getRequestId() + ", sessionId=" + session.getSessionId());
	}

	@Override
	public void onSessionStarted(SessionStartedRequest request, Session session) throws SpeechletException {
        log.info("onSessionStarted requestId=" + request.getRequestId() + ", sessionId={}" + session.getSessionId());

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
