package cs276.programming.ranking;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.HashMap;
import java.util.zip.DataFormatException;

public class NDCG {

	//private static final HashMap<String, HashMap<String, Double>> String = null;
	/**
	 * @param args
	 */
	public HashMap<String, HashMap<String,Double>> Feedback = new HashMap<String,HashMap<String,Double>>();
	
	public NDCG(String groundTruthFile){
		this.ParseGroundTruth(groundTruthFile);
		//System.out.println(Feedback);
	}
	
	HashMap<String, HashMap<String,Double>> getFeedBackList(){
		return Feedback;
	}

	public double getNDCG(ArrayList<QueryDocData>  DocQueryList) {

		int queryCount = 0;
		double score = 0;
		double curndcg =0;
		for (QueryDocData QDData : DocQueryList) {
			curndcg = getNDCGSore(QDData);
			score += curndcg;
			queryCount++;
	//		if( QDData.getQueryString().equals("library")){
	//			System.out.println("Query : "+QDData.getQueryString()+ " ndcg: "+curndcg);
	//		}
			
		}
	//	System.out.println("Total ndcg : " + score);
	//	System.out.println("Total query count : "+queryCount);
		
		return score / (double) queryCount;
		
	}

	double getNDCGSore(QueryDocData QDData) {

		String Query = QDData.getQueryString();
		int index = 0;
		// ndcgScore += (2**rel - 1)/log(2 + index, 2)
		double ndcgScore = 0.0;
		double rel = 0;
		ArrayList<DocData> docdataList = QDData.getDocData();

		for (DocData dd : docdataList) {
			
			try {
				rel = GetRel(Query, dd.getUrl());
				
			} catch (DataFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
	//		if( Query.equals("library")){
	//			System.out.println("Query : "+Query+ " url : " + dd.getUrl()+" Rel : "+rel);
	//		}
			
		//	System.out.println("Query : "+Query+ " url : " + dd.getUrl()+" Rel : "+rel);
			dd.RELEVANCE = rel;
			ndcgScore += (Math.pow(2, rel) - 1)
					/ (Math.log(2 + index) / Math.log(2));
			index++;
		}
	//	if( Query.equals("library")){
	//		System.out.println("Calculated NDCG : "+ndcgScore);
	//	}

		Collections.sort(docdataList, new Comparator<DocData>() {
			@Override
			public int compare(DocData o1, DocData o2) {
				if (o1.RELEVANCE > o2.RELEVANCE)
					return -1;
				else if (o1.RELEVANCE < o2.RELEVANCE)
					return 1;
				else
					return 0;
			}
		});

		double maxNdcg = 0;
		index=0;
		for (DocData dd : docdataList) {
			
			rel = dd.RELEVANCE;
			maxNdcg += (Math.pow(2, rel) - 1)
					/ (Math.log(2 + index) / Math.log(2));
			index++;
		}
		
	//	if( Query.equals("library")){
	//		System.out.println( "Max NDCG : "+maxNdcg);
	//	}
		
		if (maxNdcg > 0)
			return (ndcgScore / maxNdcg);
		else
			return 1.0;

	}

	public double GetRel(String query, String url) throws DataFormatException {

		// TODO Auto-generated method stub
		//System.out.println("Getting rel for query : "+query+ " url : " + url);
		
		
		if(Feedback.containsKey(query)){
			HashMap<String,Double> urlList = Feedback.get(query);
			if (urlList.containsKey(url)){
				double rel = urlList.get(url);
				if (rel > 0 )
					return rel;
				else
					return 0.0;
			}else{
		//		System.out.println("Getting rel for query : "+query+ " url : " + url);
				throw new DataFormatException("URL is not in rel train"); 
			}
			
		}
		else{
			//	System.out.println("Getting rel for query : "+query+ " url : " + url);
				throw new DataFormatException("Query is not in rel train"); 
			}
	}

	private void ParseGroundTruth(String groundTruthFile) {

		try {

			BufferedReader groundTruthFileReader = new BufferedReader(
					new FileReader(groundTruthFile));
			
			String line = "";
			HashMap<String,Double> CurURLList = null;
			String CurQuery ="";
			String LastQuery ="";
			
			while ((line = groundTruthFileReader.readLine()) != null) {
				//System.out.println("Line : "+line);
				if(line.trim().indexOf("query:") >= 0 ){
					//System.out.println("query line : "+line);
					LastQuery = CurQuery;
					CurQuery = line.trim().substring(line.trim().indexOf("query:")+6).trim();
					//System.out.println("query : "+CurQuery);
					if (CurURLList != null){
						Feedback.put(LastQuery, CurURLList);
					}
					CurURLList= new HashMap<String,Double>();
					
					
				}
				else if(line.trim().indexOf("url:") >= 0){
					
				// System.out.println("index : "+line.trim().indexOf("url:")+5);
					
					//System.out.println("s : "+line.trim().substring(line.trim().indexOf("url:")+5));

					String[] fields = line.trim().substring(line.trim().indexOf("url:")+5).split("\\s+");
					
				//	System.out.println("0  : "+fields[0]+", 1 : "+fields[0]);
					
					String url = fields[0].trim();
					double Rel = Double.parseDouble(fields[1]);
					CurURLList.put(url, Rel);
				}
				else{
					//throw new DataFormatException("Malformed queryDocTrainRel"); 
					System.out.println("Not matched");
				}
				//LastQueryCurQuery
			}
			Feedback.put(CurQuery, CurURLList);

		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
//		catch (DataFormatException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
	//	NDCG eval = new NDCG();
		String cmdArray = "python /cs276/PA3/starter/ndcg.py /cs276/PA3/starter/queryDocTrainRel /cs276/PA3/starter/queryDocTrainRel";
		// String output = eval.RunCommand(cmdArray);
		// System.out.println(output);

	}
}
