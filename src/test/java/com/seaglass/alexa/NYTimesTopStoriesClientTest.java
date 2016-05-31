package com.seaglass.alexa;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.*;

public class NYTimesTopStoriesClientTest {

	@Test
	public void testJsonParse() throws IOException {
		InputStream jsonInput = getClass().getResourceAsStream("/articleListTest.json");
		NYTimesTopStoriesClient nyt = new NYTimesTopStoriesClient("don't need a real key for this test");
		List<NewYorkTimesArticle> articleList = nyt.parseTopStoriesList(jsonInput);

		assertEquals(26, articleList.size());
		NewYorkTimesArticle firstArticle = articleList.get(0);
		assertEquals("http://www.nytimes.com/2016/06/01/business/media/music-world-bands-together-against-youtube-seeking-change-to-law.html", firstArticle.getUrlString());
		assertEquals("Business Day", firstArticle.getSection());
		assertEquals("Music World Bands Together Against YouTube, Seeking Change to Law", firstArticle.getTitle());

		NewYorkTimesArticle lastArticle = articleList.get(19);
		assertEquals("http://www.nytimes.com/2016/05/29/magazine/bj-novak-thinks-actors-are-bad-at-playing-writers.html", lastArticle.getUrlString());
		assertEquals("Magazine", lastArticle.getSection());
		assertEquals("B.J. Novak Thinks Actors Are Bad at Playing Writers", lastArticle.getTitle());
	}
}
