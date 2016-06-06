package com.seaglass.alexa;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.SsmlOutputSpeech;
import com.seaglass.alexa.exceptions.NytApiException;

public class ResponseGenerator {
    private static Logger log = LoggerFactory.getLogger(HeadlinesSpeechlet.class);

    public static SpeechletResponse generate(DialogContext dialogContext, String newYorkTimesKey) throws NytApiException {
        SpeechletResponse resp = new SpeechletResponse();
        resp.setShouldEndSession(false);
        String responseText = null;
        switch(dialogContext.getCurrentState()) {
        case INIT:
            break;        // No response from INIT state.
        case HELP:
            responseText = LanguageGenerator.helpResponse();
            break;
        case REQUEST:
            responseText = LanguageGenerator.askSection();
        case IN_LIST:
            List<String> headlineList = null;
            String requestedSection = dialogContext.getRequestedSection();
            log.info("inside generate with IN_LIST");
            try {
                log.info("about to call for headlines");
                headlineList = getHeadlines(requestedSection, newYorkTimesKey);
                log.info("back with " + headlineList.size() + " items");
            } catch (IOException ex) {
                throw new NytApiException(ex);
            }
            if (headlineList.size() > 0) {
                responseText = LanguageGenerator.itemListResponse(dialogContext, headlineList);
            } else {
                responseText = LanguageGenerator.emptyResponse(dialogContext);
            }
            break;
        case LAUNCH:
            break;
        case UNKNOWN:
            responseText = LanguageGenerator.generalError();
            break;
        default:
            responseText = LanguageGenerator.generalError();
            break;
        }

        log.info("response will be " + responseText);
        SsmlOutputSpeech outputSpeech = new SsmlOutputSpeech();
        outputSpeech.setSsml(responseText);
        resp.setOutputSpeech(outputSpeech);
        return resp;
    }
    private static List<String> getHeadlines(String requestedSection, String newYorkTimesKey) throws IOException {
        if (requestedSection == null || requestedSection.length() < 1) {
            throw new RuntimeException("requestedSection paramater cannot be null or empty");
        }
        NYTimesTopStoriesClient apiClient = new NYTimesTopStoriesClient(newYorkTimesKey);
        List<NewYorkTimesArticle> articleList = apiClient.getArticleList(requestedSection);
           if (articleList == null) {
               throw new IOException();
           }
        return articleList.stream().map(NewYorkTimesArticle::getTitle).collect(Collectors.toList());
    }

}
