package cs276.programming.ranking;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

import cs276.programming.ranking.DocData.DocFeature;

/**
 * Utility class to compute the term frequencies.
 */
public class TermFreqUtil {
	
	//TODO: Use the first pattern is we want to separate cs276 as two separate terms
	//Use the second one if we want it as a single term
	//TODO: Should we use a combination of the two? cs276 -> cs, 276, cs276
//	private static final Pattern alphaNumericPattern = Pattern.compile("([a-z]+|[0-9]+)");
	private static final Pattern alphaNumericPattern = Pattern.compile("([a-z0-9]+)");
	
	private static final Pattern spaceDelimitedPattern = Pattern.compile("[^\\p{Space}]+");
	
	public static boolean enableStemming = false;

	public int getTermCount(DocData doc, DocFeature feature) {
	
		//len_d,f
		switch(feature) {
		case ANCHOR:
			int anchorLen = 0;
			for(String anchor : doc.getAnchorTextCountMap().keySet()) {
				anchorLen += getTermCount(anchor);
			}
			return anchorLen;

		case BODY:
			return doc.getBodyLength();
		case HEADER:
			int headerLen = 0;
			for(String header : doc.getHeaders()) {
				headerLen += getTermCount(header);
			}
			return headerLen;

		case TITLE:  return getTermCount(doc.getTitle());
		case URL:
			int termCount = 0;
			Scanner sc;
			try {
				String url = doc.getUrl();
				sc = new Scanner(url == null ? "" : URLDecoder.decode(url.toLowerCase(), "UTF-8"));
				String token;
				while((token = sc.findWithinHorizon(alphaNumericPattern, 0)) != null) {
					termCount++;
//					System.err.println(token);
				}
				return termCount;
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace(); //shouldn't happen
			}
		}
		return 0;
	}

	public double getAverageTermFreq(Map<String, Integer> termFreqMap) {
		int total = 0;
		int count = 0;
		for(int value : termFreqMap.values()) {
			total += value;
			count++;
		}
		return (1.0 * total) / count;
	}

	public int getMaxTermFreq(Map<String, Integer> termFreqMap) {
		int max = -1;
		for(int value : termFreqMap.values()) {
			max = (value > max ? value : max);
		}
		return max;
	}

	public int getTermCount(String str) {

		Scanner sc = new Scanner(str == null ? "" : str.toLowerCase());
		int count = 0;
		while(sc.findWithinHorizon(spaceDelimitedPattern, 0) != null) {
			count++;
		}
		return count;
	}

	public Map<String, Integer> getTermFrequency(List<String> termVector, Pattern pattern, Scanner sc) {
		Map<String, Integer> termFrequency = new HashMap<String, Integer>();
		//add all the terms in termFrequency to merge the checks -> is term counted && is term in termVector
		for(String term : termVector) {
			termFrequency.put(term, 0);
		}
		String token;
		while((token = sc.findWithinHorizon(pattern, 0)) != null) {
			token = enableStemming ? stem(token) : token;
			if(termFrequency.containsKey(token)) {
				termFrequency.put(token, termFrequency.get(token)+1);
			} //else implies token is not a query term
		}
		return termFrequency;
	}

	public Map<String, Integer> getTermFrequency(String query) {

		Map<String, Integer> termFrequency = new HashMap<String, Integer>(); 
		Scanner sc = new Scanner(query == null ? "" : query.toLowerCase());
		String token;
		while((token = sc.findWithinHorizon(spaceDelimitedPattern, 0)) != null) {
			token = enableStemming ? stem(token) : token;
			if(termFrequency.containsKey(token)) {
				termFrequency.put(token, termFrequency.get(token)+1);
			} else {
				termFrequency.put(token, 1);
			}
		}
		return termFrequency;
	}

	private String stem(String token) {
		PorterStemmer stemmer = new PorterStemmer();
		return stemmer.stem(token);
	}

	public Map<String, Integer> getTermFrequency(String docStr, List<String> termVector) {
			
		Map<String, Integer> termFrequency = new HashMap<String, Integer>();
		//add all the terms in termFrequency to merge the checks -> is term counted && is term in termVector
		for(String term : termVector) {
			termFrequency.put(term, 0);
		}
		
		Scanner sc = new Scanner(docStr == null ? "" : docStr.toLowerCase());
		String token;
		while((token = sc.findWithinHorizon(spaceDelimitedPattern, 0)) != null) {
			token = enableStemming ? stem(token) : token;
			if(termFrequency.containsKey(token)) {
				termFrequency.put(token, termFrequency.get(token)+1);
			} //else implies token is not a query term
		}
		return termFrequency;
	}

}
