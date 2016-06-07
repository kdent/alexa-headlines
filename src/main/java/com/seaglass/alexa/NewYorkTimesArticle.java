package com.seaglass.alexa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NewYorkTimesArticle {

    private String urlString;
    private String section;
    private String byline;
    private String title;
    private String abstractText;
    private String publishedDateString;
    private static final String[] sectionNames = new String[] {"home", "opinion", "world", "national", "politics", 
        "upshot", "nyregion", "business", "technology", "science", "health", "sports", "arts", "books", "movies", 
        "theater", "sundayreview", "fashion", "tmagazine", "food", "travel", "magazine", "realestate", "automobiles", 
        "obituaries", "insider"};
    private static final Set<String> sectionSet = new HashSet<String>(Arrays.asList(sectionNames));

    public String getUrlString() {
        return urlString;
    }
    public void setUrlString(String urlString) {
        this.urlString = urlString;
    }
    public String getSection() {
        return section;
    }
    public void setSection(String section) {
        this.section = section;
    }
    public String getByline() {
        return byline;
    }
    public void setByline(String byline) {
        this.byline = byline;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getAbstractText() {
        return abstractText;
    }
    public void setAbstractText(String abstractText) {
        this.abstractText = abstractText;
    }
    public String getPublishedDateString() {
        return publishedDateString;
    }
    public void setPublishedDateString(String publishedDateString) {
        this.publishedDateString = publishedDateString;
    }
    public static List<String> getSectionList() {
        return new ArrayList<String>(sectionSet);
    }
    public static boolean isSection(String sectionName) {
        return sectionSet.contains(sectionName);
    }
}
