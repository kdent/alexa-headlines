package com.seaglass.alexa;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import com.google.gson.stream.JsonReader;


/**
 * Hello world!
 *
 */
public class NewYorkTimesPopularClient
{
    private static String baseURL = "http://api.nytimes.com/svc/mostpopular/v2";
    private String apiKey = null;
    private PopularityType mostType = PopularityType.mostshared;
    private String section = "all-sections";
    private int numDays = 1;

    public NewYorkTimesPopularClient(String apiKey) {
        this.apiKey = apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public void getArticleList() throws IOException {
        if (apiKey == null) {
            throw new RuntimeException("You must set the API key before connecting.");
        }
        URL url = new URL(baseURL + "/" + mostType + "/" + section + "/" +
            numDays + "?api-key=" + URLEncoder.encode(apiKey, "utf-8"));
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        if (conn.getResponseCode() != 200) {
            throw new IOException(conn.getResponseMessage());
        }

        JsonReader jsonReader = new JsonReader(new InputStreamReader(conn.getInputStream()));
        conn.disconnect();
        String jsonContents = new String(buf);
    }

    private enum PopularityType {
        mostemailed, mostshared, mostviewed
    }

    public static void main( String[] args ) throws IOException
    {
        String popularApiKey = null;
        if (args.length < 1) {
            System.err.println("You must specify an API key on the command line.");
            System.exit(1);
        }

        popularApiKey = args[0];
        NewYorkTimesPopularClient client = new NewYorkTimesPopularClient(popularApiKey);
        client.getArticleList();
    }
}
