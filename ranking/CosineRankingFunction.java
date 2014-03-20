package cs276.programming.ranking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cs276.programming.ranking.DocData.DocFeature;
import cs276.programming.util.VectorUtil;

/**
 * Class implementing the cosine
 * ranking function. 
 */
public class CosineRankingFunction implements RankingFunction {

	@Override
	public double getScore(DocData Doc, QueryData query, RankingModel rModel) {
		
		HashMap<String, Double> FeatureWeights = rModel.FeatureWeights;
		
		List<String> termVector = new ArrayList<String>();
		List<Double> queryVector = Vectorizer.getQueryVector(query.GetQueryString(), termVector, TermSmoothingType.LOGARITHM, DocSmoothingType.IDF, rModel);
		Map<DocFeature, List<Double>> docVector = Vectorizer.getDocumentVector(Doc, termVector, TermSmoothingType.LOGARITHM, DocSmoothingType.NONE, NormalizationType.LENGTH, rModel);

		HashMap<String,Double> FeatureVariable = new HashMap<String,Double>();
		
		double FeatureURL     = VectorUtil.dot( queryVector, docVector.get(DocFeature.URL)); 
		double FeatureTitle   = VectorUtil.dot( queryVector, docVector.get(DocFeature.TITLE)); 
		double FeatureBody    = VectorUtil.dot( queryVector, docVector.get(DocFeature.BODY)); 
		double FeatureHeaderL = VectorUtil.dot( queryVector, docVector.get(DocFeature.HEADER)); 
		double FeatureAnchor  = VectorUtil.dot( queryVector, docVector.get(DocFeature.ANCHOR)); 

		FeatureVariable.put("task1_W_url", FeatureURL);
		FeatureVariable.put("task1_W_title", FeatureTitle);
		FeatureVariable.put("task1_W_body", FeatureBody);
		FeatureVariable.put("task1_W_header", FeatureHeaderL);
		FeatureVariable.put("task1_W_anchor", FeatureAnchor);
				
		//ArrayList<Double> Query TF_IDF =  
		
		//System.out.println("query: "+ query.GetQueryString());
		//System.out.println("Doc URL: "+ Doc.getUrl());
		//System.out.println("Variables: "+ FeatureVariable);
		//System.out.println("Weights: "+ FeatureWeights);
		try {
		//System.out.println("Score: "+ LinearModel.CalculateScore(FeatureVariable,FeatureWeights));
		
			return LinearModel.CalculateScore(FeatureVariable, FeatureWeights);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		return 0;
	}
}
