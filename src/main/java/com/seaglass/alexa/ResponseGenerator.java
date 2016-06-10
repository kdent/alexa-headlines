package com.seaglass.alexa;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.Card;
import com.amazon.speech.ui.SimpleCard;
import com.amazon.speech.ui.SsmlOutputSpeech;
import com.seaglass.alexa.exceptions.NytApiException;

public class ResponseGenerator {

    public static SpeechletResponse generate(DialogContext dialogContext, String newYorkTimesKey) throws NytApiException {
        SpeechletResponse resp = new SpeechletResponse();
        String responseText = null;

        switch(dialogContext.getCurrentState()) {
        case INIT:
            resp.setShouldEndSession(true);
            break;        // There is no response from the INIT state.
        case HELP:
            responseText = LanguageGenerator.helpResponse();
            resp.setShouldEndSession(true);
            break;
        case REQUEST:
            responseText = LanguageGenerator.askSection();
            resp.setShouldEndSession(false);
            break;
        case DELIVER_LIST:
            List<NewYorkTimesArticle> articleList = null;
            List<String> headlineList = null;
            String requestedSection = dialogContext.getRequestedSection();

            if (requestedSection == null) {
                responseText = LanguageGenerator.askSection();
                resp.setShouldEndSession(false);
            } else {
                try {
                    articleList = getArticleList(requestedSection, newYorkTimesKey);
                    headlineList = articleList.stream().map(NewYorkTimesArticle::getTitle).collect(Collectors.toList());
                    dialogContext.setListLength(headlineList.size());
                } catch (IOException ex) {
                    throw new NytApiException(ex);
                }
                // At the beginning of a list generate a Card to include in the response.
                if (dialogContext.getNextItem() == 0) {
                    Card responseCard = makeResponseCard(articleList, requestedSection);
                    resp.setCard(responseCard);
                }
                if (headlineList.size() > 0) {
                    responseText = LanguageGenerator.itemListResponse(dialogContext, headlineList);
                } else {
                    responseText = LanguageGenerator.emptyResponse(dialogContext);
                }
                // Check whether the list has completed or not.
                if (dialogContext.getNextItem() != 0 && (dialogContext.getNextItem() >= dialogContext.getListLength())) {
                    resp.setShouldEndSession(true);
                } else {
                    resp.setShouldEndSession(false);
                }
            }
            break;
        case LAUNCH:
            responseText = LanguageGenerator.welcomeMessage();
            resp.setShouldEndSession(false);
            break;
        case END:
            resp.setShouldEndSession(true);
            break;
        default:
            responseText = LanguageGenerator.generalError();
            break;
        }

        SsmlOutputSpeech outputSpeech = new SsmlOutputSpeech();
        outputSpeech.setSsml(responseText);
        resp.setOutputSpeech(outputSpeech);
        return resp;
    }

    public static SpeechletResponse errorResponse(String errorMsg) {
        return errorResponse(errorMsg, true);
    }

    public static SpeechletResponse errorResponse(String errorMsg, boolean shouldEndSession) {
        SpeechletResponse resp = new SpeechletResponse();
        resp.setShouldEndSession(true);
        SsmlOutputSpeech outputSpeech = new SsmlOutputSpeech();
        outputSpeech.setSsml(errorMsg);
        resp.setOutputSpeech(outputSpeech);
        resp.setShouldEndSession(shouldEndSession);
        return resp;
    }

    private static List<NewYorkTimesArticle> getArticleList(String requestedSection, String newYorkTimesKey) throws IOException {
        if (requestedSection == null || requestedSection.length() < 1) {
            throw new RuntimeException("requestedSection paramater cannot be null or empty");
        }
        NYTimesTopStoriesClient apiClient = new NYTimesTopStoriesClient(newYorkTimesKey);
        List<NewYorkTimesArticle> articleList = apiClient.getArticleList(requestedSection);
           if (articleList == null) {
               throw new IOException();
           }
        return articleList;
    }

    private static Card makeResponseCard(List<NewYorkTimesArticle> articleList, String sectionName) {
        SimpleCard card = new SimpleCard();
        card.setTitle("Top Stories in " + sectionName);
        StringBuilder buf = new StringBuilder();
        for (NewYorkTimesArticle article : articleList) {
            buf.append(article.getTitle() + "\n" + article.getAbstractText() + "\n\n");
        }
        card.setContent(buf.toString());
        return card;
    }

}
