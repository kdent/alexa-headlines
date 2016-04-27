package com.seaglass.alexa;

public class NewYorkTimesArticle {

	private String urlString;
	private String section;
	private String byline;
	private String title;
	private String abstractText;
	private String publishedDateString;

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
}
