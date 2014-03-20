package cs276.programming.mlr;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class to compare the ranking outputs of task1 & task2 against the rel
 * scores.
 */
public class RankingComparator {

	public HashMap<String, HashMap<String,Double>> qdocRel = new HashMap<String,HashMap<String,Double>>();
	
	public static void main(String[] args) {
		RankingComparator rc = new RankingComparator();
		rc.parseRelFil("/cs276/PA4/release/queryDocTrainRel.dev");
		
		for(String query  : new String[]{ "stanford live", "stanford meal plan" }) {
			System.out.println("\n" + query + " - \n");
			List<Double> relValuesList = rc.printDesiredRanking(query);
			System.out.println("\n");
			rc.printActualRanking("/cs276/PA4/release/rank_1_test.txt", query, relValuesList);
			System.out.println("\n");
			rc.printActualRanking("/cs276/PA4/release/rank_2_test.txt", query, relValuesList);
		}
	}

	private void printActualRanking(String rankingFile, String query, List<Double> relValuesList) {

		BufferedReader relFileReader = null;
		try {

			relFileReader = new BufferedReader(new FileReader(rankingFile));

			String line = "";
			List<String> curURLList = new ArrayList<String>();
			String curQuery = "";
			String lastQuery = "";
			boolean isCurrQuery = false;

			while ((line = relFileReader.readLine()) != null) {
				if (line.trim().indexOf("query:") >= 0) {
					if(isCurrQuery) {
//						print list
						for(String url : curURLList) {
							int group = relValuesList.size() - relValuesList.indexOf(qdocRel.get(query).get(url));
							System.out.printf("%d %s\n", group, url);
						}
						return;
					} else {
						curQuery = line.trim().substring(line.trim().indexOf("query:") + 6).trim();
						if(curQuery.equals(query)) {
							isCurrQuery = true;
						}
					}

				} else if (isCurrQuery && (line.trim().indexOf("url:") >= 0)) {

					String[] fields = line.trim().substring(line.trim().indexOf("url:") + 5).split("\\s+");

					String url = fields[0].trim();
					curURLList.add(url);
				}
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if(relFileReader != null) relFileReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private List<Double> printDesiredRanking(String query) {
		HashMap<String,Double> queryRel = qdocRel.get(query);
		
		Set<Double> relValuesSet = new HashSet<Double>(queryRel.values());
		List<Double> relValuesList = new ArrayList<Double>(relValuesSet);
		Collections.sort(relValuesList);
		for(int i = relValuesList.size() - 1; i >= 0; i--) {
			for(String url : queryRel.keySet()) {
				if(queryRel.get(url).equals(relValuesList.get(i))) {
					System.out.printf("%d %s\t%f\n", (relValuesList.size() - i), url, relValuesList.get(i));
				}
			}
		}
		
		return relValuesList;
	}

	private void parseRelFil(String relFile) {

		BufferedReader relFileReader = null;
		
		try {

			relFileReader = new BufferedReader(new FileReader(relFile));

			String line = "";
			HashMap<String, Double> curURLList = null;
			Set<Double> currRelValue = new HashSet<Double>();
			String curQuery = "";
			String lastQuery = "";

			while ((line = relFileReader.readLine()) != null) {
				// System.out.println("Line : "+line);
				if (line.trim().indexOf("query:") >= 0) {
					// System.out.println("query line : "+line);
					lastQuery = curQuery;
					curQuery = line.trim().substring(line.trim().indexOf("query:") + 6).trim();
					// System.out.println("query : "+CurQuery);
					if (curURLList != null) {
						qdocRel.put(lastQuery, curURLList);
					}
					curURLList = new HashMap<String, Double>();

				} else if (line.trim().indexOf("url:") >= 0) {

					// System.out.println("index : "+line.trim().indexOf("url:")+5);

					// System.out.println("s : "+line.trim().substring(line.trim().indexOf("url:")+5));

					String[] fields = line.trim().substring(line.trim().indexOf("url:") + 5).split("\\s+");

					// System.out.println("0  : "+fields[0]+", 1 : "+fields[0]);

					String url = fields[0].trim();
					double Rel = Double.parseDouble(fields[1]);
					curURLList.put(url, Rel);
					currRelValue.add(Rel);
				} else {
					// throw new
					// DataFormatException("Malformed queryDocTrainRel");
					System.out.println("Not matched");
				}
				// LastQueryCurQuery
			}
			qdocRel.put(curQuery, curURLList);

		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if(relFileReader != null) relFileReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
