package cs276.programming.ranking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cs276.programming.ranking.DocData.DocFeature;
import cs276.programming.util.VectorUtil;

/**
 * Class implementing the body positon ranking function.
 */
public class BodyPosRakingFunction implements RankingFunction {

	@Override
	public double getScore(DocData Doc, QueryData query, RankingModel rModel) {

		HashMap<String, Double> FeatureWeights = rModel.FeatureWeights;

		TermFreqUtil.enableStemming = true;

		List<String> termVector = new ArrayList<String>();
		
		List<Double> queryVector = Vectorizer.getQueryVector(query.GetQueryString(), termVector,
				TermSmoothingType.LOGARITHM, DocSmoothingType.IDF, rModel);
		
		Map<DocFeature, List<Double>> docVector = Vectorizer.getDocumentVector( Doc, termVector, TermSmoothingType.LOGARITHM,
				DocSmoothingType.NONE, NormalizationType.LENGTH, rModel);

		HashMap<String, Double> FeatureVariable = new HashMap<String, Double>();

		double FeatureURL = VectorUtil.dot(queryVector,
				docVector.get(DocFeature.URL));
		double FeatureTitle = VectorUtil.dot(queryVector,
				docVector.get(DocFeature.TITLE));
		double FeatureBody = VectorUtil.dot(queryVector,
				docVector.get(DocFeature.BODY));
		double FeatureHeaderL = VectorUtil.dot(queryVector,
				docVector.get(DocFeature.HEADER));
		double FeatureAnchor = VectorUtil.dot(queryVector,
				docVector.get(DocFeature.ANCHOR));

		double FirstPos = getFirstPos(Doc, query);

		FeatureVariable.put("task4_W_url", FeatureURL);
		FeatureVariable.put("task4_W_title", FeatureTitle);
		FeatureVariable.put("task4_W_body", FeatureBody);
		FeatureVariable.put("task4_W_header", FeatureHeaderL);
		FeatureVariable.put("task4_W_anchor", FeatureAnchor);
		FeatureVariable.put("task4_W_first_pos", FirstPos);

		try {

			return LinearModel.CalculateScore(FeatureVariable, FeatureWeights);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		return 0;

	}

	public double getFirstPos(DocData Doc, QueryData query) {
		// TODO Auto-generated method stub

		// int length = Doc.getBodyLength();

		double sum = 0;
		int termCount = 0;

		for (ArrayList<Integer> li : Doc.getbodyHitPos()) {
			for (int i : li) {
				if (i == 1)
					sum += 1 / Math.log(1.2);
				else
					sum += 1 / Math.log(i);
				termCount++;
			}
		}

		if (sum == 0)
			return 0;
		else
			return sum / termCount;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
