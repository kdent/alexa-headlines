package com.seaglass.alexa;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class KeyReader {

    public static String getAPIKey() throws IOException {
        String newYorkTimesKey = null;
        InputStream nytKeyFile = HeadlinesSpeechlet.class.getResourceAsStream("/nyt_key");
        if (nytKeyFile == null) {
            throw new FileNotFoundException("Missing NYT key file. Cannot access headlines API.");
        }
        BufferedReader keyReader = new BufferedReader(new InputStreamReader(nytKeyFile));
        newYorkTimesKey = keyReader.readLine();
        keyReader.close();
        if (newYorkTimesKey == null || newYorkTimesKey.length() < 1) {
            throw new IOException("NYT API Key is empty or null");
        }
        return newYorkTimesKey;
    }

}
