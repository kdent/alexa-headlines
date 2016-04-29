package com.seaglass.alexa;

import java.util.HashSet;
import java.util.Set;

import com.amazon.speech.speechlet.lambda.SpeechletRequestStreamHandler;

public class NewsGuyRequestHandler extends SpeechletRequestStreamHandler {

	private static final Set<String> supportedApplicationIds;

	static {
		supportedApplicationIds = new HashSet<String>();
		supportedApplicationIds.add("amzn1.echo-sdk-ams.app.a3b5f21e-0eca-4174-b9ff-f5e8b4e9c02d");
	}


	public NewsGuyRequestHandler() {
		super(new NewsGuySpeechlet(), supportedApplicationIds);
	}

}
