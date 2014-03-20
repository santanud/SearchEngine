package cs276.programming.ranking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to hold the queryDocTrainData.
 * 
 * File format for queryDocTrainData
 * query
 *   url*
 *     title
 *     header*
 *     body_hits* term poslist
 *     body_length
 *     pagerank
 *     anchor_text?* terms
 *       stanford_anchor_count
 */

public class DocData {
	
	public enum DocFeature {URL, HEADER, BODY, TITLE, ANCHOR};

	private String url;
	private String title;
	private List<String> headers = new ArrayList<String>();
	private int bodyLength;
	private int pageRank;
	private Map<String, Integer> bodyHitMap = new HashMap<String, Integer>(); //map body_hits term to count of pos-id-list 
	private Map<String, Integer> anchorTextCountMap = new HashMap<String, Integer>(); //Map anchor_text term to the stanford_amchor_count
	public double SCORE = 0.0;
	private int NoOfTermsInQuery = 0;
	public double RELEVANCE =0;
	List<String> AnchorText = new ArrayList<String>();
	
	//List<ArrayList<Integer>>  URLHit = new ArrayList<ArrayList<Integer>>();
	//List<ArrayList<Integer>>  TitleHit = new ArrayList<ArrayList<Integer>>();
	//List<ArrayList<Integer>>  HeaderHit = new ArrayList<ArrayList<Integer>>();
	List<ArrayList<Integer>>  bodyHitPos = new ArrayList<ArrayList<Integer>>();
	//List<ArrayList<Integer>>  AnchorHit = new ArrayList<ArrayList<Integer>>();
	
	public List<String> getAnchorText(){
		return AnchorText;
		
	}
	
	public List<ArrayList<Integer>> getbodyHitPos(){
		return bodyHitPos;
	}
	
	public DocData(String url) {
		this.setUrl(url);
	}
	
	void setScore(double score){
		this.SCORE = score;
	}
	
	double getScore(){
		return 	this.SCORE;
	}
	
	
	public String getUrl() {
		return url;
	}

	void setUrl(String url) {
		this.url = url;
	}

	String getTitle() {
		return title;
	}

	void setTitle(String title) {
		this.title = title;
	}

	public int getBodyLength() {
		return bodyLength;
	}

	void setBodyLength(int bodyLength) {
		this.bodyLength = bodyLength;
	}

	public int getPageRank() {
		return pageRank;
	}

	void setPageRank(int pageRank) {
		this.pageRank = pageRank;
	}

	public void addHeader(String header) {
		headers.add(header);
	}
	
	public List<String> getHeaders() {
		return headers;
	}


		
		
	public void addBodyHit(String Line ) {
		
		//	String[] posList = tokens[1].split(" ");
		//	int posCount = posList.length - 1; //since the term is also in the array
		//	docData.addBodyHit(tokens[1]); posList[0], posCount);
		
		String[] posList = Line.split(" ");
		String term = posList[0];
		int posCount = posList.length - 1;
		bodyHitMap.put(term, posCount);
		
		ArrayList<Integer> HitPos =  new ArrayList<Integer>();
		
		for(int i = 1;i < posList.length;i++)
			HitPos.add(Integer.parseInt(posList[i]));
		
		//bodyHit
		bodyHitPos.add(HitPos);
		
		
	}
	
	Map<String, Integer> getBodyHitMap() {
		return bodyHitMap;
	}

	public void addAnchorText(String anchorTerms, int stanfordAnchorCount) {
		
		anchorTextCountMap.put(anchorTerms, stanfordAnchorCount);
		
	 	AnchorText.add(anchorTerms);
	}

	public Map<String, Integer> getAnchorTextCountMap() {
		return anchorTextCountMap;
	}

	public int getRawTFBody(String term) {
		// TODO Auto-generated method stub
		
		int tf = 0;
		if (bodyHitMap.containsKey(term))
			tf = bodyHitMap.get(term);
		
		return tf;
	}

	
}





