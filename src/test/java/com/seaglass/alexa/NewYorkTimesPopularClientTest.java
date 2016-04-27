package com.seaglass.alexa;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.*;

public class NewYorkTimesPopularClientTest {

	@Test
	public void testJsonParse() throws IOException {
		InputStream jsonInput = getClass().getResourceAsStream("/articleListTest.json");
		NewYorkTimesPopularClient nyt = new NewYorkTimesPopularClient("don't need a real key for this test");
		List<NewYorkTimesArticle> articleList = nyt.parsePopularArticleList(jsonInput);

		assertEquals(20, articleList.size());
		NewYorkTimesArticle firstArticle = articleList.get(0);
		assertEquals("http://www.nytimes.com/2016/04/26/world/asia/afghanistan-pakistan-taliban.html", firstArticle.getUrlString());
		assertEquals("World", firstArticle.getSection());
		assertEquals("Afghan President Demands Pakistan Take Military Action Against Taliban", firstArticle.getTitle());

		NewYorkTimesArticle lastArticle = articleList.get(19);
		assertEquals("http://www.nytimes.com/2016/04/25/technology/was-your-dog-walked-your-phone-can-show-you.html", lastArticle.getUrlString());
		assertEquals("Technology", lastArticle.getSection());
		assertEquals("Was Your Dog Walked? Your Phone Can Show You", lastArticle.getTitle());
	}
}
