package cs276.programming.ranking;

import java.util.ArrayList;

/**
 * Class to hold the query data. 
 *
 */
public class QueryData {

	/**
	 * @param args
	 */
	String query;
	ArrayList<String> terms;

	QueryData(String Q) {
		query = Q;
		terms = new ArrayList<String>();
		
		for (String S : query.split("\\s+")) {
			if(!terms.contains(S))
				terms.add(S);

		}
	}
	
	public ArrayList<String> GetTerms(){
		return terms;
	}
	int getTermCount(){
		return terms.size();
	}

	String getTerm(int position) {

		if (position >= 0 && position < terms.size()) {
			return terms.get(position);
		} else {
			return null;
		}

	}

	public String GetQueryString() {
		return query;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
