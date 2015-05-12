
package com.seal.fetch;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.seal.util.Helper;

public class BaiduWebSearcher extends WebSearcher {

	/********************** Baidu Web Parameters **************************/
	public static final String BASE_URL = "https://www.baidu.com/s?rn=100&";
	public static final String RESULTS_KEY = "num";
	public static final String START_KEY = "start";
	public static final String LANG_KEY = "lr";
	public static final String QUERY_KEY = "wd";
	public static final String Baidu_LANG_PREFIX = "lang_";
	public static final String PAGE_FORBIDDEN_ERROR = "<title>403 ";
	/******************************************************************/

	public static final String HOST = Helper.toURL(BASE_URL).getHost();
	public static Logger log = Logger.getLogger(BaiduWebSearcher.class);

	public static boolean containsBaiduURL(List<URL> urls) {
		if (urls == null) return false;
		for (URL url : urls)
			if (isBaiduURL(url))
				return true;
				return false;
	}

	public static boolean isBlockedByBaidu(String doc, URL url) {
		if (doc == null || url == null) return false;
		if (isBaiduURL(url) && doc.contains(PAGE_FORBIDDEN_ERROR)) {
			log.fatal("Congratulations! Baidu has blocked you on: " + new Date());
			return true;
		}
		return false;
	}

	public static boolean isBaiduURL(URL url) {
		if (url == null) return false;
		return url.getHost().equals(HOST);
	}

	public static void main(String args[]) {
		
		int numResults = 100;
		BaiduWebSearcher gs = new BaiduWebSearcher();
		gs.setCacheDir(new File("/www.cache/"));
		gs.setNumResults(numResults);
		gs.setTimeOutInMS(10*1000);
		gs.setMaxDocSizeInKB(512);
		gs.addQuery("黑龙江", true);
		gs.run();
		Set<Snippet> snippets = gs.getSnippets();
		for (Snippet snippet : snippets)
			log.info(snippet.getPageURL());
				if (numResults == snippets.size())
					log.info("Test succeeded!");
				else log.error("Test failed! Expecting: " + numResults + " Actual: " + snippets.size());
	}

	public BaiduWebSearcher() {
		super();
	}

	protected void buildSnippets(String resultPage) {
		Pattern snippetPat = Pattern.compile("(?s)<div class=\"result c-container.+?</div>"); 
		Pattern[] patterns = new Pattern[] {
				
				Pattern.compile("href.*?=.*?\"(.*?)\""),  // 0: page URL  -
				Pattern.compile(">(.+?)</a>"),  // 1: title
				Pattern.compile("<a href=\"([^\"]+)\"[^<>]*>View as HTML"), // 2: cached URL #1
				Pattern.compile("(?s)(?:<div class=std>|</a><br>)(.+?)<br>"), // 3: summary
				Pattern.compile("<a class=fl href=\"([^\"]+)\"[^<>]*>Cached</a>"),  // 4: cached URL #2
		};

		List<String> extractions = new ArrayList<String>();
		Matcher matcher = snippetPat.matcher(resultPage);
		while (matcher.find()) {
//			System.out.println("--------------------------------------------------------------");
//			System.out.println(matcher.group(0));
			buildSingleSnippet(matcher.group(0), extractions, patterns);
		}
	}

	protected void buildSingleSnippet(String rawSnippet, List<String> extractions, Pattern[] patterns) {
		extractions.clear();
		for (Pattern p : patterns) {
			Matcher m = p.matcher(rawSnippet);
			if (m.find()) {
				extractions.add(m.group(1));
				rawSnippet = rawSnippet.substring(m.end(1));
			} else extractions.add(null);
		}

		String pageURL = extractions.get(0);
		String title = extractions.get(1);
		String cacheURL1 = extractions.get(2);
		String summary = extractions.get(3);
		String cacheURL2 = extractions.get(4);
//		System.out.println("----------------------------------");
//		System.out.println("pageURL:"+pageURL);
//		System.out.println("Title:"+title);

		// a valid snippet must contain page URL and title
		if (pageURL == null || title == null) return;

		Snippet snippet = new Snippet();
		snippet.setPageURL(pageURL);
		snippet.setTitle(title);
		snippet.setCacheURL(cacheURL1 == null ? cacheURL2 : cacheURL1);
		snippet.setSummary(summary);
		snippet.setRank(snippets.size()+1);
		snippets.add(snippet);
	}

	/**
	 * Returns a URL for Baidu search page
	 * @param numResultsForThisPage number of results per page (between 1 and 100 inclusively)
	 * @param pageNum page number (greater than 0)
	 * @param query query terms
	 * @return Baidu search page URL
	 */
	protected String getSearchURL(int numResultsForThisPage, int pageNum, String query) {
		if (numResultsForThisPage < 1 || numResultsForThisPage > maxResultsPerPage)
			throw new IllegalArgumentException("Number of results for this page must be between 1 and " + maxResultsPerPage + " inclusively.");
		if (pageNum < 1)
			throw new IllegalArgumentException("Page number must be at least 1.");

		int startIndex = (pageNum-1) * maxResultsPerPage;
		return getURL(numResultsForThisPage, startIndex, langCode, query);
	}

	public static String getURL(int numResults, int start, String langID, String query) {
		StringBuffer url = new StringBuffer(BASE_URL);
		//url.append(RESULTS_KEY).append("=").append(numResults).append("&");
		//url.append(START_KEY).append("=").append(start).append("&");
		//url.append(LANG_KEY).append("=").append("lang_zh-CN").append("&");
		url.append(QUERY_KEY).append("=").append(query);
		log.info(url.toString());
		return url.toString();
	}
}
