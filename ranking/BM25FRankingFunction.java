package cs276.programming.ranking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cs276.programming.ranking.DocData.DocFeature;
import cs276.programming.util.VectorUtil;

/**
 * Class implementing the BM25F ranking function.
 */
public class BM25FRankingFunction implements RankingFunction {

	@Override
	public double getScore(DocData Doc, QueryData query, RankingModel rModel) {
		
		HashMap<String, Double> FeatureWeights = rModel.FeatureWeights;
		
		List<String> termVector = new ArrayList<String>();
		List<Double> queryVector = Vectorizer.getQueryVector(query.GetQueryString(), termVector, TermSmoothingType.NATURAL, DocSmoothingType.NONE, rModel);
		//this applies equation (2)
		Map<DocFeature, List<Double>> docVector = Vectorizer.getDocumentVector(Doc, termVector, TermSmoothingType.NATURAL, DocSmoothingType.NONE, NormalizationType.BM25F, rModel);

		// w_d,t = \Sigma_f  W_f . ftf_d,f,t  : equation (3)
		List<Double> w_dt = new ArrayList<Double>();
		for(int i = 0; i < termVector.size(); i++) {
			w_dt.add(0.0);
		}
		for(DocFeature feature : DocFeature.values()) {
			w_dt = VectorUtil.add(w_dt, VectorUtil.scalarMult(docVector.get(feature), FeatureWeights.get("task2_W_" + feature.toString().toLowerCase())));
		}
		
		double score = 0;
		// score = \Sigma_t  { w_d,t / (K_1 + w_d,t) } * idf_t + \lambda * V_j(f)  : equation (4)
		for(int i = 0; i < termVector.size(); i++) {
			double IDF = Vectorizer.getIDF(DocSmoothingType.IDF, termVector.get(i), rModel);
			score += w_dt.get(i)*IDF / (FeatureWeights.get("task2_K1") + w_dt.get(i));
		}
		
		//V_j(f)
		score += FeatureWeights.get("task2_lambda") * V(Doc.getPageRank(), FeatureWeights);
		
		return score;
		// apply(double value, DocSmoothingType dsType, String term, RankingModel rankingModel)
	}

	private double V(int nonTextualFeature, HashMap<String, Double> featureWeights) {
		
		//options -
		//log( \lambda' + f)  //log
		//f / (\lambda' + f)  //Saturation
		//1 / (\lambda' + exp(-f * \lambda"))
		
		
//		return Math.log(featureWeights.get("task2_lambda_prime") * nonTextualFeature);
		return (1.0 * nonTextualFeature) / (featureWeights.get("task2_lambda_prime") + nonTextualFeature);
	}

}
