package cs276.programming.ranking;

/**
 * Class implementing a basic ranking function.
 */
public class BasicRankingFunction implements RankingFunction {

	@Override
	public double getScore(DocData Doc, QueryData query, RankingModel rModel) {
		
		//decreasing order of number of body_hits across all query terms
		double S =0;
		for(String term :query.GetTerms()){
			
			S+= (double) Doc.getRawTFBody(term);
		}
		
		return S;
	}

}
