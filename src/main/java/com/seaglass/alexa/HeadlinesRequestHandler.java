package com.seaglass.alexa;

import java.util.HashSet;
import java.util.Set;

import com.amazon.speech.speechlet.lambda.SpeechletRequestStreamHandler;

public class HeadlinesRequestHandler extends SpeechletRequestStreamHandler {

	private static final Set<String> supportedApplicationIds;

	static {
		supportedApplicationIds = new HashSet<String>();
		supportedApplicationIds.add("amzn1.echo-sdk-ams.app.ebe81acc-b22c-43a1-8f49-4d6677c75f60");
	}


	public HeadlinesRequestHandler() {
		super(new HeadlinesSpeechlet(), supportedApplicationIds);
	}

}
