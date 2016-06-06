package com.seaglass.alexa;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.stream.JsonReader;


/**
 * Hello world!
 *
 */
public class NYTimesTopStoriesClient
{
    private static String baseURL = "http://api.nytimes.com/svc/topstories/v2";
    private String apiKey = null;

    public NYTimesTopStoriesClient(String apiKey) {
        this.apiKey = apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public List<NewYorkTimesArticle> getArticleList(String section) throws IOException {
        if (apiKey == null) {
            throw new RuntimeException("You must set the API key before connecting.");
        }
        List<NewYorkTimesArticle> articleList = null;
        URL url = new URL(baseURL + "/" + section + ".json?api-key=" + URLEncoder.encode(apiKey, "utf-8"));
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        if (conn.getResponseCode() != 200) {
            throw new IOException(conn.getResponseMessage());
        }

        InputStream input = conn.getInputStream();
        articleList = parseTopStoriesList(input);
        input.close();
        conn.disconnect();
        return articleList;
    }

    public List<NewYorkTimesArticle> parseTopStoriesList(InputStream inputStream) throws IOException {
        List<NewYorkTimesArticle> articleList = new ArrayList<NewYorkTimesArticle>();

        JsonReader jsonReader = new JsonReader(new InputStreamReader(inputStream));
        jsonReader.beginObject();

        // Find the results array.
        while (jsonReader.hasNext()) {
            String name = jsonReader.nextName();
            if (name.equals("results")) {
                jsonReader.beginArray();
                // For each item in the results array.
                while (jsonReader.hasNext()) {
                    NewYorkTimesArticle article = new NewYorkTimesArticle();
                    jsonReader.beginObject();
                    while (jsonReader.hasNext()) {
                        String attribute = jsonReader.nextName();
                        if (attribute.equals("url")) {
                            article.setUrlString(jsonReader.nextString());
                        } else if (attribute.equals("section")) {
                            article.setSection(jsonReader.nextString());
                        } else if (attribute.equals("byline")) {
                            article.setByline(jsonReader.nextString());
                        } else if (attribute.equals("title")) {
                            article.setTitle(jsonReader.nextString());
                        } else if (attribute.equals("abstract")) {
                            article.setAbstractText(jsonReader.nextString());
                        } else if (attribute.equals("published_date")) {
                            article.setPublishedDateString(jsonReader.nextString());
                        } else {
                            jsonReader.skipValue();
                        }
                    }
                    jsonReader.endObject();
                    articleList.add(article);
                }
                jsonReader.endArray();
            } else {
                jsonReader.skipValue();
            }
        }
        jsonReader.close();
        return articleList;
    }

}
