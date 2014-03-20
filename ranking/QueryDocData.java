package cs276.programming.ranking;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Class to hold a Query + list of docs data. 
 */
public class QueryDocData {
	
	private QueryData query;
	private ArrayList<DocData> docdata = new ArrayList<DocData>();

	
	public QueryData getQuery() {
		return query;
	}
	public String getQueryString() {
		return query.GetQueryString();
	}
	
	void setQuery(QueryData query) {
		this.query = query;
	}
	void setQueryString(String query) {
		this.query = new QueryData(query);
	}
	
//	Collection<DocData> getDocData() {
//		return docdata;
//	}
	public ArrayList<DocData> getDocData() {
		return docdata;
	}
	
	
	void SortByScore(){
	 Collections.sort(docdata,new Comparator<DocData>( ){

		@Override
		public int compare(DocData o1, DocData o2) {
			if(o1.SCORE > o2.SCORE )
				return -1;
			else  if(o1.SCORE < o2.SCORE )
				return 1;
			else 
				return 0;
		} });	
	}

	public void DisplayResult() {

		System.out.println("query: "+query.GetQueryString());
		for(DocData d: docdata)
			System.out.println("  url: "+d.getUrl());
		
	}

	public void addDocData(DocData docData2) {
		docdata.add(docData2);
	}
}