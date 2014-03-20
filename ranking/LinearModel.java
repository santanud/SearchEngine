package cs276.programming.ranking;

import java.util.HashMap;
import java.util.Map;

public class LinearModel {

	/**
	 * @param args
	 * @throws NoSuchFieldException 
	 */
	
	public static double CalculateScore (HashMap<String, Double> Variables , HashMap<String, Double> Weights) 
			throws NoSuchFieldException{
		
		double score = 0;
		double FeatureWeight=0;
		String FeatureName;
		double FeatureValue=0;
		
		for (Map.Entry<String, Double> entry : Variables.entrySet()) {
		     FeatureName = entry.getKey();
		     FeatureValue = entry.getValue();
		    
		    if(!Weights.containsKey(FeatureName))
		    	throw new NoSuchFieldException("The feature has no weight associated with it");
		    else
		    	 FeatureWeight =  Weights.get(FeatureName);
		    
		    score += FeatureValue*FeatureWeight;
		    
		}
		return score;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
