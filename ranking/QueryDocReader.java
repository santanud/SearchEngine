package cs276.programming.ranking;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

/**
 * Utility class to read the Query Doc data. 
 * This consolidates all the file parsing operations allowing us
 * to isolate any format related changes or file read related optimizations.
 */
public class QueryDocReader implements Iterable<QueryDocData> {
	
	private static final String QUERY = "query";
	BufferedReader queryDocTrainDataReader = null;
	QueryDocData nextQdocData;
	
	public QueryDocReader(File queryDocTrainData) {
		init(queryDocTrainData);
	}
	
	private void init(File queryDocTrainData) {
		//assumes that the queries in both the files are in the same order
		try {
			String line = null;
			nextQdocData = new QueryDocData();
			queryDocTrainDataReader = new BufferedReader(new FileReader(queryDocTrainData));
			while ((line = queryDocTrainDataReader.readLine()) != null) {
				String[] tokens = line.split(": ", 2);
				if(tokens[0].equals(QUERY)) {
					nextQdocData.setQueryString(tokens[1]);
					break;
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			nextQdocData = null;
		} catch (IOException e) {
			e.printStackTrace();
			nextQdocData = null;
		}
	}
	
	private String getNextDataLine() throws IOException {
		if(queryDocTrainDataReader != null) {
			String line = queryDocTrainDataReader.readLine();
			return line != null ? line.trim() : null;
		}
		return null;
	}
	
	public void close() {
		try {
			queryDocTrainDataReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Iterator<QueryDocData> iterator() {
		
		return new Iterator<QueryDocData>() {

			@Override
			public boolean hasNext() {
				return nextQdocData != null && nextQdocData.getQuery() != null;
			}

			@Override
			public QueryDocData next() {
				try {
					QueryDocData currQdocData = nextQdocData; 
					nextQdocData = new QueryDocData();
					readDataFile(currQdocData, nextQdocData);
					return currQdocData;
				} catch (IOException e) {
					e.printStackTrace();
					nextQdocData = null;
					return null;
				}
			}

			private void readDataFile(QueryDocData currQdocData, QueryDocData nextQdocData) throws IOException {
				/**
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

				String line = null;
				String currUrl = null;
				DocData docData = null;
				while((line = getNextDataLine()) != null) {
					
					String[] tokens = line.split(": ", 2);
					if(tokens[0].equals(QUERY)) {
						if(docData != null) {
							currQdocData.addDocData(docData);
						}
						nextQdocData.setQueryString(tokens[1]);
						return;
					}
					if(tokens[0].equals("url")) {
						currUrl = tokens[1];
						if(docData != null) {
							currQdocData.addDocData(docData);
						}
						docData = new DocData(currUrl);
					} else if(tokens[0].equals("title")) {
						docData.setTitle(tokens[1]);
					} else if(tokens[0].equals("header")) {
						docData.addHeader(tokens[1]);
					} else if(tokens[0].equals("body_hits")) {
					//	String[] posList = tokens[1].split(" ");
					//	int posCount = posList.length - 1; //since the term is also in the array
						docData.addBodyHit(tokens[1]); //posList[0], posCount);
					} else if(tokens[0].equals("body_length")) {
						docData.setBodyLength(Integer.parseInt(tokens[1]));
					} else if(tokens[0].equals("pagerank")) {
						docData.setPageRank(Integer.parseInt(tokens[1]));
					} else if(tokens[0].equals("anchor_text")) {
						String anchorTerms = tokens[1];
						String nextLine = getNextDataLine(); //anchor_text always has a follow up line for the count
						String[] t2 = nextLine.split(": ", 2);
						docData.addAnchorText(anchorTerms, Integer.parseInt(t2[1]));
					}
				}
				if(docData != null) {
					currQdocData.addDocData(docData);
				}
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
	
	
	public static void main(String[] args) {
		
		QueryDocReader reader = new QueryDocReader(new File("/cs276/PA3/starter/queryDocTrainData"));
		
		int queryCount = 0;
		for(QueryDocData qurl : reader) {
			queryCount++;
			System.out.println("query: "+qurl.getQueryString());
			for(DocData dd : qurl.getDocData()) {
				System.out.printf("  url: %s, anchorCnt: %d\n", dd.getUrl(), dd.getAnchorTextCountMap().keySet().size());
			}
		}
		
		System.out.printf("\nQuery Count: %d\n", queryCount);
		
		reader.close();
	}
	
}
