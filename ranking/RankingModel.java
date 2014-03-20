package cs276.programming.ranking;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import cs276.programming.mlr.MLRComprehensive;
import cs276.programming.mlr.MLRLinearFunction;
import cs276.programming.mlr.MLRSVMFunction;
import cs276.programming.mlr.MLRSVMRegression;
import cs276.programming.ranking.DocData.DocFeature;
import cs276.programming.util.Parameters;

public class RankingModel implements java.io.Serializable {

	private static final long serialVersionUID = 6804596085499881998L;

	HashMap<String, Double> FeatureWeights;
	HashMap<String, Double> IDFValues;
	NDCG ndcg;
	
	/**
	 * Maps a term to its document frequency (df) in the training data set.
	 * The IDF can be computed (for various types) based off this value.
	 */
	private Map<String, Integer> dfMap;
	
	private Map<DocFeature, Double> averageLength;
	
	/**
	 * Total number of training documents. Used to compute IDF.
	 */
	private int numTrainingDocs;

	public RankingModel() {

		FeatureWeights = new HashMap<String, Double>();

		//for Cosine Ranking Function
		FeatureWeights.put("task1_W_url", 1.0);
		FeatureWeights.put("task1_W_title", 1.0);
		FeatureWeights.put("task1_W_body", 1.0);
		FeatureWeights.put("task1_W_header", 1.0);
		FeatureWeights.put("task1_W_anchor", 1.0);

		//B25F Ranking function
		FeatureWeights.put("task2_W_url", 124.0);
		FeatureWeights.put("task2_W_title", 0.79);
		FeatureWeights.put("task2_W_body", 0.09);
		FeatureWeights.put("task2_W_header", 2.0);
		FeatureWeights.put("task2_W_anchor", 0.5);
		FeatureWeights.put("task2_K1", 1.09);
		FeatureWeights.put("task2_lambda", 30.0);
		FeatureWeights.put("task2_lambda_prime", 117.0);

		FeatureWeights.put("task2_B_url", 1.09);
		FeatureWeights.put("task2_B_title", 1.0);
		FeatureWeights.put("task2_B_header", 1.0);
		FeatureWeights.put("task2_B_body", 0.77);
		FeatureWeights.put("task2_B_anchor", 0.21);

		// task3_
		FeatureWeights.put("task3_W_url", 1.0);
		FeatureWeights.put("task3_W_title", 1.0);
		FeatureWeights.put("task3_W_body", 1.0);
		FeatureWeights.put("task3_W_header", 1.0);
		FeatureWeights.put("task3_W_anchor", 1.0);
		FeatureWeights.put("task3_B", 0.7);
			
		// extra_
		FeatureWeights.put("task4_W_url", 1.0);
		FeatureWeights.put("task4_W_title", 1.0);
		FeatureWeights.put("task4_W_body", 1.0);
		FeatureWeights.put("task4_W_header", 1.0);
		FeatureWeights.put("task4_W_anchor", 1.0);
		FeatureWeights.put("task4_B", 5.0);
		FeatureWeights.put("task4_W_first_pos", 10.5);
		
		//for Stemmed Cosine Ranking Function
		FeatureWeights.put("task5_W_url", 1.0);
		FeatureWeights.put("task5_W_title", 1.0);
		FeatureWeights.put("task5_W_body", 1.0);
		FeatureWeights.put("task5_W_header", 1.0);
		FeatureWeights.put("task5_W_anchor", 1.0);
		
		
	}
	
	double getWeight(String FeatureName ){
		if (FeatureWeights.containsKey(FeatureName)) {
			return FeatureWeights.get(FeatureName);
		} else {
			throw new UnsupportedOperationException();
		}
	}

	void setWeight(String FeatureName, double Weight) {

		if (FeatureWeights.containsKey(FeatureName)) {
			FeatureWeights.put(FeatureName, Weight);
		} else {
			throw new UnsupportedOperationException();
		}
	}

	void setWeight(Map<String, Double> newWeights){
		
		for(String Feature : newWeights.keySet()){
			setWeight( Feature,newWeights.get(Feature));
		}
		
	}
	
	
	public void train(QueryDocReader queryDocTrainData) {
		
		DocFreqCalculator dfc = new DocFreqCalculator();
		TermFreqUtil tfUtil = new TermFreqUtil();
		
		dfMap = Collections.synchronizedMap(new HashMap<String, Integer>());
		numTrainingDocs = dfc.calculateDf(new File(Parameters.BASE_PATH + "PA3/starter/AllQueryTerms"), new File(Parameters.BASE_PATH + "PA1/data"), dfMap); //TODO: parameterize location of file

		int docCount = 0;
		//total length (number of terms) for each feature in the doc.
		Map<DocFeature, Integer> totlenMap = new HashMap<DocData.DocFeature, Integer>();
		for(DocFeature feature : DocFeature.values()) {
			totlenMap.put(feature, 0);
		}
		//populated with terms from AllQueryTerms

		averageLength = new EnumMap<DocData.DocFeature, Double>(DocFeature.class);
		for (QueryDocData QDoc : queryDocTrainData) {
			for(DocData doc : QDoc.getDocData()) { //TODO: there are duplicate docs across queries. What to do about that?
				docCount++;
				for(DocFeature feature : DocFeature.values()) {
					totlenMap.put(feature, totlenMap.get(feature) + tfUtil.getTermCount(doc, feature));
				}
			}
		}
		for(DocFeature feature : DocFeature.values()) {
			averageLength.put(feature, (1.0 * totlenMap.get(feature)) / docCount);
			System.err.printf("Feature: %s, Average Length: %f\n", feature, averageLength.get(feature));
		}
	}

	public double getAverageLength(DocFeature feature) {
		return averageLength.get(feature);
	}
	
	/**
	 * Returns the document frequency for the specified
	 * term in the training corpus.
	 */
	public int getDocumentFrequency(String term) {
		return dfMap.containsKey(term) ? dfMap.get(term) : 0;
	}

	/**
	 * Returns the total number of documents in the 
	 * training corpus. 
	 */
	public int getNumTrainingDocs() {
		return numTrainingDocs;
	}

	public void rank(QueryDocData testDoc, int TaskId) {

		double score = 0;
		QueryData query = testDoc.getQuery();
		for (DocData doc : testDoc.getDocData()) {

			switch (TaskId) {
			case 0:
				RankingFunction rf = new BasicRankingFunction();
				score = rf.getScore(doc, query, this);
				break;
			case 1:
				RankingFunction crf = new CosineRankingFunction();
				score = crf.getScore(doc, query, this);
				break;
			case 2:
				RankingFunction brf = new BM25FRankingFunction();
				score = brf.getScore(doc, query, this);
				break;
			case 3:
				RankingFunction swrf = new SmallestWindowRankingFunction();
				score = swrf.getScore(doc, query, this);
				break;
			case 4:
				RankingFunction erf = new BodyPosRakingFunction();
				score = erf.getScore(doc, query, this);
				break;
			case 5:
				RankingFunction scrf = new StemmedCosineRankingFunction();
				score = scrf.getScore(doc, query, this);
				break;
					
			default:
				throw new UnsupportedOperationException();
			}
			doc.SCORE = score;
		}
		testDoc.SortByScore();
	}

	public static void SaveToDisk(RankingModel sp, String ModelFileName) {

		FileOutputStream fileOut = null;
		ObjectOutputStream out = null;
		try {
			fileOut = new FileOutputStream(ModelFileName);

			out = new ObjectOutputStream(fileOut);
			out.writeObject(sp);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if(out != null) out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				if(fileOut != null) fileOut.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	public static RankingModel ReadFromDisk(String ModelFileName) {

		FileInputStream fileIn = null;
		ObjectInputStream in = null;
		try {
			fileIn = new FileInputStream(ModelFileName);

			in = new ObjectInputStream(fileIn);
			RankingModel sp = (RankingModel) in.readObject();
			return (sp);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if(in != null) in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				if(fileIn != null) fileIn.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public double getNDCG(QueryDocReader queryDocTrainDataReader) {
		// TODO Auto-generated method stub
		return 0;
	}

	public void rankMLR(QueryDocData testDoc, int TaskId) {
		double score = 0;
		QueryData query = testDoc.getQuery();
		//for (DocData doc : testDoc.getDocData()) {

			switch (TaskId) {
			case 1:
				MLRLinearFunction Linear = new MLRLinearFunction();
				Linear.setScoreAll(testDoc, this);
				break;
			case 2:
				MLRSVMFunction svm = new MLRSVMFunction();
				svm.setScoreAll(testDoc, this);
				break;
			case 3:
				MLRComprehensive svmEx = new MLRComprehensive();
				svmEx.setScoreAll(testDoc, this);
				break;
			case 4:
				MLRSVMRegression svr = new MLRSVMRegression();
				svr.setScoreAll(testDoc, this);
				break;
			default:
				throw new UnsupportedOperationException();
			}
			//doc.SCORE = score;
	//	}
		testDoc.SortByScore();
		
	}
}
