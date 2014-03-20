package cs276.programming.ranking;

/**
 * Interface for all the Ranking Functions. 
 *
 */
public interface RankingFunction {
	
	double getScore(DocData Doc, QueryData query, RankingModel rModel);


}
