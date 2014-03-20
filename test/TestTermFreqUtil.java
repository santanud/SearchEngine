package cs276.programming.test;

import java.io.File;

import cs276.programming.ranking.DocData;
import cs276.programming.ranking.QueryDocData;
import cs276.programming.ranking.DocData.DocFeature;
import cs276.programming.ranking.QueryDocReader;
import cs276.programming.ranking.TermFreqUtil;

/**
 * Class to test the TermFreqUtil functionality.
 */
public class TestTermFreqUtil {

	private final TermFreqUtil tfUtil = new TermFreqUtil();
	
	public void testGetTermCountUrl() {
		
		testGetCountUrl("http://www.stanford.edu/dept/HPS/corn.html", 8);
		testGetCountUrl("https://farmshare.stanford.edu/", 4);
		testGetCountUrl("https://www.stanford.edu/group/farmshare/cgi-bin/wiki/index.php/User_Guide", 13);
		testGetCountUrl("http://news.stanford.edu/news/2012/april/climate-change-corn-041912.html", 12);
		testGetCountUrl("http://www-cs.stanford.edu/computing-guide/file-storage-sharing/afs/accessing-afs-cornmyth", 14);
		testGetCountUrl("http://art.stanford.edu/profile/Wanda+Corn/", 7);
		testGetCountUrl("https://class.stanford.edu/db/Winter2013", 6); //TODO "Winter2013" is considered a single word. Is this right? cs276 is also considered a single word
		testGetCountUrl("http://www.stanford.edu/class/archive/cs/cs143/cs143.1112/materials/lectures/lecture02.pdf", 14);
		testGetCountUrl("https://explorecourses.stanford.edu/CourseSearch/search?view=catalog&filter-coursestatus-Active=on&page=0&q=CS143", 16);
		testGetCountUrl("http://ai.stanford.edu/~ang/", 5);
		testGetCountUrl("http://stanfordwho.stanford.edu/lookup?search=Hesselink,Lambertus", 8);
		testGetCountUrl("http://www.slac.stanford.edu/th/lectures/Munich%202011%20C.pdf", 11);
		testGetCountUrl("http://roboticsclub.stanford.edu/content/%5Btitle-raw%5D-6", 8);
		testGetCountUrl("", 0);
		testGetCountUrl(" ", 0);
		testGetCountUrl("\t \t", 0);
		testGetCountUrl(null, 0);
	}
	private void testGetCountUrl(String url, int expectedCount) {
		DocData dd = new DocData(url);
		int termCount = tfUtil.getTermCount(dd, DocFeature.URL);
		assert termCount == expectedCount : "Got term count of " + termCount + " for Url (" + url + ") instead of " + expectedCount;
	}
	
	public void testGetTermCount() {
		
		testGetTermCount("", 0);
		testGetTermCount(" ", 0);
		testGetTermCount("\t \t", 0);
		testGetTermCount(null, 0);

		testGetTermCount("test", 1);
		testGetTermCount(" test ", 1);
		testGetTermCount("\ttest\t", 1);
		testGetTermCount("this\tis a  \n\ttest\n", 4);

		testGetTermCount("stanford live", 2);
		testGetTermCount("stanford cs 229", 3);
		testGetTermCount("math 108 stanford", 3);
		testGetTermCount("stanford cs stanford cs courses stanford ee stanford ee courses ee364a stanford", 12);
		testGetTermCount("stanford y2e2", 2);
		testGetTermCount("cs276 spring 2010", 3);
		testGetTermCount("math 30", 2);
		
	}
	private void testGetTermCount(String str, int expectedCount) {
		int termCount = tfUtil.getTermCount(str);
		assert termCount == expectedCount : "Got term count of " + termCount + " for String (" + str + ") instead of " + expectedCount;
	}
	
	public void testGetTermFrequency() {
		
		testGetTermFrequency("test", "test", 1);
		testGetTermFrequency(" test ", "test", 1);
		testGetTermFrequency("\ttest\t", "test", 1);
		testGetTermFrequency("this\tis a  \n\ttest\n", "test", 1);

		testGetTermFrequency("stanford live", "stanford", 1);
		testGetTermFrequency("stanford cs 229", "cs", 1);
		testGetTermFrequency("math 108 stanford", "108", 1);
		testGetTermFrequency("stanford cs stanford cs courses stanford ee stanford ee courses ee364a stanford", "cs", 2);
		testGetTermFrequency("stanford cs stanford cs courses stanford ee stanford ee courses ee364a stanford", "ee", 2);
		testGetTermFrequency("stanford cs stanford cs courses stanford ee stanford ee courses ee364a stanford", "stanford", 5);
		testGetTermFrequency("stanford cs stanford cs courses stanford ee stanford ee courses ee364a stanford", "courses", 2);
		testGetTermFrequency("stanford y2e2", "y2e2", 1);
		testGetTermFrequency("cs276 spring 2010", "cs276", 1);
		testGetTermFrequency("math 30", "30", 1);
	}
	private void testGetTermFrequency(String query, String term, int expectedCount) {
		
		int termCount = tfUtil.getTermFrequency(query).get(term); 
		assert  termCount == expectedCount : 
			"Got term count of " + termCount + " for term (" + term + ") in query (" + query + 
			") instead of " + expectedCount;;
	}
	
	public void testWithTrainData() {
		
		for (QueryDocData QDoc : new QueryDocReader(new File("/cs276/PA3/starter/queryDocTrainData"))) {
			if("stanford live".equals(QDoc.getQueryString())) {
				for(DocData doc : QDoc.getDocData()) {
					if("http://live.stanford.edu/aboutus/staff.php".equals(doc.getUrl())) {
						assert tfUtil.getTermCount(doc, DocFeature.ANCHOR) == 0;
						assert tfUtil.getTermCount(doc, DocFeature.BODY) == 264;
						assert tfUtil.getTermCount(doc, DocFeature.HEADER) == 7; //TODO: Is this 6 or 7? stanford live & bing concert hall staff
						assert tfUtil.getTermCount(doc, DocFeature.TITLE) == 6;
						assert tfUtil.getTermCount(doc, DocFeature.URL) == 7;
					}
					
				}
			}
		}

	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		TestTermFreqUtil testFreqUtil = new TestTermFreqUtil();
		testFreqUtil.testGetTermCountUrl();
		testFreqUtil.testGetTermCount();
		testFreqUtil.testGetTermFrequency();
		testFreqUtil.testWithTrainData();
	}

}
