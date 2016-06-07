package com.seaglass.alexa;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.SsmlOutputSpeech;
import com.seaglass.alexa.exceptions.NytApiException;

public class ResponseGeneratorTest {

	@Test
	public void testEmptyContext() throws NytApiException, IOException {

		SpeechletResponse resp = ResponseGenerator.generate(new DialogContext(), KeyReader.getAPIKey());

		SsmlOutputSpeech speech = (SsmlOutputSpeech) resp.getOutputSpeech();
		assertNull(speech.getSsml());
	}

	@Test
	public void testHelpRequest() throws NytApiException, IOException {

		DialogContext dialogContext = new DialogContext();
		dialogContext.setCurrentState("HELP");
		SpeechletResponse resp = ResponseGenerator.generate(dialogContext, KeyReader.getAPIKey());

		SsmlOutputSpeech speech = (SsmlOutputSpeech) resp.getOutputSpeech();
		assertEquals(LanguageGenerator.helpResponse(), speech.getSsml());
	}

	@Test
	public void testRequestList() throws NytApiException, IOException {
	    DialogContext dialogContext = new DialogContext();
	    dialogContext.setCurrentState("REQUEST");
	    SpeechletResponse resp = ResponseGenerator.generate(dialogContext, KeyReader.getAPIKey());
	    SsmlOutputSpeech speech = (SsmlOutputSpeech) resp.getOutputSpeech();
	    assertEquals(LanguageGenerator.askSection(), speech.getSsml());
	}

	@Test
	public void testInList() throws NytApiException, IOException {

		DialogContext dialogContext = new DialogContext();
		dialogContext.setCurrentState("IN_LIST");
		dialogContext.setRequestedSection("technology");
		SpeechletResponse resp = ResponseGenerator.generate(dialogContext, KeyReader.getAPIKey());

		SsmlOutputSpeech speech = (SsmlOutputSpeech) resp.getOutputSpeech();
		assertTrue(speech.getSsml().startsWith("<speak>"));
	}

	@Test
	public void testNullSection() {

		DialogContext dialogContext = new DialogContext();
		dialogContext.setCurrentState("IN_LIST");
		try {
			ResponseGenerator.generate(dialogContext, KeyReader.getAPIKey());
		} catch (RuntimeException ex) {
			return;
		} catch (IOException ex) {
			;
		}

		fail("There should have been a NytApiException exception");

	}

	@Test
	public void testInvalidSection() throws NytApiException, IOException {

		DialogContext dialogContext = new DialogContext();
		dialogContext.setCurrentState("IN_LIST");
		dialogContext.setRequestedSection("bogus_section");
		try {
			ResponseGenerator.generate(dialogContext, KeyReader.getAPIKey());
		} catch (NytApiException ex) {
			assertEquals("java.io.IOException: Forbidden", ex.getMessage());
			return;
		}

		fail("Expecting forbidden access");
	}

}
