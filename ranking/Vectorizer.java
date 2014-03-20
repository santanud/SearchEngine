package cs276.programming.ranking;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

import cs276.programming.ranking.DocData.DocFeature;

/**
 * Utility class to convert a query string or 
 * DocData into a vector. 
 *
 */
public class Vectorizer {
	
	private static final TermFreqUtil tfUtil = new TermFreqUtil();
	
	private static final Vectorizer vectorizer = new Vectorizer();

	private static final PorterStemmer stemmer = new PorterStemmer();
	
	/**
	 * Convert the specified DocData into a Document Vector.
	 */
	public static Map<DocFeature, List<Double>> getRawDocumentVector(DocData doc, List<String> termVector, RankingModel rankingModel) {
		return getDocumentVector(doc, termVector, TermSmoothingType.NATURAL, DocSmoothingType.NONE, NormalizationType.NONE, rankingModel);
	}

	/**
	 * Convert the specified query string into a Query Vector.
	 */
	public static List<Double> getRawQueryVector(String query, List<String> termVector, RankingModel rankingModel) {
		return getQueryVector(query, termVector, TermSmoothingType.NATURAL, DocSmoothingType.NONE, rankingModel);
	}
	
	/**
	 * Convert the specified query string into a query vector and also create the 
	 * corresponding term vector.
	 */
	public static List<Double> getQueryVector(String query, List<String> termVector, TermSmoothingType tstype, DocSmoothingType dsType, RankingModel rankingModel) {
		
		//get unique terms in query
		//get term freq for each term (mostly 1)
		//smooth the term frequency according to the type specified
		//weight above with the specified doc frequency factor
		//No normalization as this will apply to all docs
		
		Map<String, Integer> termFreqMap = tfUtil.getTermFrequency(query);
		termVector.addAll(termFreqMap.keySet()); //assumes that the passed termVector is empty!!
		List<Double> queryVector = new ArrayList<Double>();
		for(int i = 0; i < termVector.size(); i++) {
			String term = termVector.get(i);
			queryVector.add( vectorizer.apply(vectorizer.apply(termFreqMap, tstype, term), dsType, term, rankingModel) );	
		}
		
		return queryVector;
	}
	
	/**
	 * Apply the specified smoothing on the document frequency data.
	 */
	private double apply(double value, DocSmoothingType dsType, String term, RankingModel rankingModel) {
		int docFreq = rankingModel.getDocumentFrequency(term);
		switch(dsType) {
		case NONE: return value;
		case IDF: 
			return value * Math.log(1.0 * (1.0 + rankingModel.getNumTrainingDocs()) / (1.0 + docFreq) ); //IDF + Laplace add-one smoothing
		case PROBIDF: 
			return value * Math.max(0.0, 
					Math.log((rankingModel.getNumTrainingDocs() - docFreq/docFreq)));
		default: throw new UnsupportedOperationException(dsType + " doc smoothing not supported.");
		}
		
	}
	
	/**
	 *  Get IDF value of a  term based on smoothing type.
	 */
	public static double getIDF(DocSmoothingType dsType, String term, RankingModel rankingModel) {
		int docFreq = rankingModel.getDocumentFrequency(term);
		switch(dsType) {
		case NONE: 
					if(docFreq == 0)
						return 0;
					else
						return 	Math.log((double)rankingModel.getNumTrainingDocs()/docFreq);
		case IDF: 
			return  Math.log(1.0 * (1.0 + rankingModel.getNumTrainingDocs()) / (1.0 + docFreq) ); //IDF + Laplace add-one smoothing
		case PROBIDF: 
			return Math.max(0.0, 
					Math.log((rankingModel.getNumTrainingDocs() - docFreq/docFreq)));
		default: throw new UnsupportedOperationException(dsType + " doc smoothing not supported.");
		}
		
	}
	
	
	/**
	 * Apply the specified smoothing on the term frequency data.
	 */
	private double apply(Map<String, Integer> termFreqMap, TermSmoothingType tstype, String term) {
		
		int termFreq = termFreqMap.containsKey(term) ? termFreqMap.get(term) : 0;
		switch(tstype) {
		case NATURAL: return 1.0 * termFreq;
		
		case LOGARITHM: return termFreq > 0 ? 1 + Math.log(termFreq) : 0;
		
		case AUGMENTED: return 0.5 + 0.5 * termFreq / tfUtil.getMaxTermFreq(termFreqMap);
		
		case BOOLEAN: return termFreq > 0 ? 1 : 0;
		
		case LOGAVERAGE: 
			double averageTermFreq = tfUtil.getAverageTermFreq(termFreqMap);
			averageTermFreq = (averageTermFreq == 0) ? 1 : averageTermFreq;
			return termFreq > 0 ? (1 + Math.log(termFreq)) / (1 + Math.log(averageTermFreq)) : 0;
		
		default: throw new UnsupportedOperationException(tstype + " term smoothing not supported.");
		}
	}

	/**
	 * Compute the parameters required for normalization.
	 * This is used by is used by Vectorizer::getDocumentVector. 
	 * The ParamMap values are applied in NormalizationType.BM25F::normalize
	 */
	public Map<DocFeature, Map<String, Double>> getParamMap(DocData doc, RankingModel rankingModel) {
		
		Map<DocFeature, Map<String, Double>> paramMap = new HashMap<DocData.DocFeature, Map<String,Double>>();
		
		for(DocFeature feature : DocFeature.values()) {
			
			Map<String, Double> params = new HashMap<String, Double>();
			params.put("bodyLenSmoothingFactor", 500.0);
			params.put("body_length", 1.0 * doc.getBodyLength());
			params.put("B_f", rankingModel.FeatureWeights.get("task2_B_" + feature.toString().toLowerCase()));
			params.put("len_d,f", 1.0 * tfUtil.getTermCount(doc, feature));
			params.put("avlen_f", rankingModel.getAverageLength(feature));
			
			paramMap.put(feature, params);
		}
		return paramMap;
	}
	
	public static Map<DocFeature, List<Double>> getDocumentVector(DocData doc, List<String> termVector, TermSmoothingType tstype, DocSmoothingType dsType, NormalizationType ntype, RankingModel rankingModel) {
		
		//do below for each feature in Document:
		//get term frequency for each unique term in the query
		//apply selected scaling (e.g. sub-linear)
		//normalize the data (e.g. length normalization)
		
		Map<DocFeature, List<Double>> documentVector = new EnumMap<DocData.DocFeature, List<Double>>(DocFeature.class);

		Map<DocFeature, Map<String, Double>> paramMap = vectorizer.getParamMap(doc, rankingModel);

		//For the term score vector use TermSmoothingType.NATURAL, DocSmoothingType.NONE

		//handle title, url, header, body_hits, anchor_text
		String docStr = doc.getTitle() != null ? doc.getTitle().toLowerCase() : "";
		List<Double> featureVector = vectorizer.getFeatureVector(docStr, termVector, tstype, dsType, ntype, doc, rankingModel, paramMap.get(DocFeature.TITLE));
		documentVector.put(DocFeature.TITLE, featureVector);
		
//		docStr = doc.getUrl().toLowerCase().replace('/', ' ').replace('.', ' ').replace(':', ' '); 
//		Map<String, Integer> urlTermFrequency = vectorizer.getTermFrequency(docStr, termVector);
		Scanner sc = new Scanner(doc.getUrl() == null ? "" : doc.getUrl().toLowerCase());
		Map<String, Integer> urlTermFrequency = tfUtil.getTermFrequency(termVector, Pattern.compile("[a-z0-9]+"), sc);
		featureVector = vectorizer.getFeatureVector(urlTermFrequency, termVector, tstype, dsType, ntype, doc, rankingModel, paramMap.get(DocFeature.URL));
		documentVector.put(DocFeature.URL, featureVector);
		
		docStr = "";
		for(String header : doc.getHeaders()) {
			docStr += header + " ";
		}
		featureVector = vectorizer.getFeatureVector(docStr, termVector, tstype, dsType, ntype, doc, rankingModel, paramMap.get(DocFeature.HEADER));
		documentVector.put(DocFeature.HEADER, featureVector);

		//"For the anchor field, we assume that there is one big document that contains all of 
		//the anchors with the anchor text multiplied by the anchor count."
		Map<String, Integer> anchorTextCountMap = doc.getAnchorTextCountMap();
		Map<String, Integer> anchorTermFrequency = new HashMap<String, Integer>();
		for(String term : termVector) {
			anchorTermFrequency.put(TermFreqUtil.enableStemming ? stemmer.stem(term) : term, 0);
		}
		for(String anchorText : anchorTextCountMap.keySet()) {
			String[] tokens = anchorText.toLowerCase().trim().split(" ");
			int count = anchorTextCountMap.get(anchorText);
			for(String token : tokens) {
				token = TermFreqUtil.enableStemming ? stemmer.stem(token) : token;
				if(anchorTermFrequency.containsKey(token)) {
					anchorTermFrequency.put(token, anchorTermFrequency.get(token) + count);
				} //else implies token is not a query term
			}
		}
		featureVector = vectorizer.getFeatureVector(anchorTermFrequency, termVector, tstype, dsType, ntype, doc, rankingModel, paramMap.get(DocFeature.ANCHOR));
		documentVector.put(DocFeature.ANCHOR, featureVector);
		
		Map<String, Integer> bodyHitMap = doc.getBodyHitMap(); //Maps query term to frequency!
		featureVector = vectorizer.getFeatureVector(bodyHitMap, termVector, tstype, dsType, ntype, doc, rankingModel, paramMap.get(DocFeature.BODY));
		documentVector.put(DocFeature.BODY, featureVector);
		
		return documentVector;
	}

	private List<Double> getFeatureVector(String docStr, final List<String> termVector,
			TermSmoothingType tstype, DocSmoothingType dsType, NormalizationType ntype, DocData doc, RankingModel rankingModel, Map<String, Double> params) {
		
		Map<String, Integer> termFrequency = tfUtil.getTermFrequency(docStr, termVector);
		return getFeatureVector(termFrequency, termVector, tstype, dsType, ntype, doc, rankingModel, params);
	}

	private List<Double> getFeatureVector(Map<String, Integer> termFrequency, final List<String> termVector,
			TermSmoothingType tstype, DocSmoothingType dsType,
			NormalizationType ntype, DocData doc, RankingModel rankingModel, Map<String, Double> params) {
		List<Double> featureVector = new ArrayList<Double>();
		for(int i = 0; i < termVector.size(); i++) {
			String term = termVector.get(i);
			featureVector.add( ntype.normalize( apply(apply(termFrequency, tstype, term), dsType, term, rankingModel), params ));	
		}
		return featureVector;
	}
	
	public static void main(String[] args) {
		
		TermFreqUtil.enableStemming = true;
		String ModelFileName = "RankingModel";
		RankingModel RModel = RankingModel.ReadFromDisk( ModelFileName);
		
		//Note: parameters to getXYZVector set as needed for cosine ranking function
		String query = "stanford knuth 2012 stanford classes";
		List<String> termVector = new ArrayList<String>();
		List<Double> queryVector = Vectorizer.getQueryVector(query, termVector, TermSmoothingType.NATURAL, DocSmoothingType.IDF, RModel);
		System.err.println("Term Vector - ");
		System.err.println(termVector);
		System.err.println("\nQuery Vector - ");
		System.err.println(queryVector);
		System.err.println();
		
		boolean testAllDocs = false;
		for(QueryDocData qdoc : new QueryDocReader( new File("/cs276/PA3/starter/queryDocTrainData"))) {
			if(testAllDocs) {
				for(DocData doc : qdoc.getDocData()) {
					Map<DocFeature, List<Double>> docVector = Vectorizer.getDocumentVector(doc, termVector, TermSmoothingType.NATURAL, DocSmoothingType.IDF, NormalizationType.BM25F, RModel);
					System.err.println(docVector);
				}
			} else {
				DocData doc = qdoc.getDocData().get(0); //just checking for the 1st doc for now
				Map<DocFeature, List<Double>> docVector = Vectorizer.getDocumentVector(doc, termVector, TermSmoothingType.NATURAL, DocSmoothingType.IDF, NormalizationType.BM25F, RModel);
				System.err.println(docVector);
			}
		}
		
		//To get the raw term frequencies, use
//		List<Double> queryVector = Vectorizer.getRawQueryVector(query, termVector, RModel);
//		Map<DocFeature, List<Double>> docVector = Vectorizer.getRawDocumentVector(doc, termVector, RModel);
		
		//for BM25F, we'll set
//		List<Double> queryVector = Vectorizer.getQueryVector(query, termVector, TermSmoothingType.NATURAL, DocSmoothingType.NONE, RModel);
//		Map<DocFeature, List<Double>> docVector = Vectorizer.getDocumentVector(doc, termVector, TermSmoothingType.NATURAL, DocSmoothingType.NONE, NormalizationType.BM25F, RModel);
		
		
	}
}
