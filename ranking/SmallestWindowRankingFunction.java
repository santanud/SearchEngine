package cs276.programming.ranking;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cs276.programming.ranking.DocData.DocFeature;
import cs276.programming.util.VectorUtil;

/**
 * Ranking function for the Smallest Window approach. 
 *
 */
public class SmallestWindowRankingFunction implements RankingFunction {

	@Override
	public double getScore(DocData Doc, QueryData query, RankingModel rModel) {
		// TODO Auto-generated method stub

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

		FeatureVariable.put("task3_W_url", FeatureURL);
		FeatureVariable.put("task3_W_title", FeatureTitle);
		FeatureVariable.put("task3_W_body", FeatureBody);
		FeatureVariable.put("task3_W_header", FeatureHeaderL);
		FeatureVariable.put("task3_W_anchor", FeatureAnchor);
	
		try {
		//System.out.println("Score: "+ LinearModel.CalculateScore(FeatureVariable,FeatureWeights));
			int sm = getSmallestWindow(Doc, query);

			
			double B = rModel.getWeight("task3_B");
			double smMultiplier  = 1+(B-1)*Math.exp(-1*sm/B);

			return smMultiplier*LinearModel.CalculateScore(FeatureVariable, FeatureWeights);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		return 0;
		
	}

	public int getSmallestWindow(DocData Doc, QueryData query) {
		// TODO Auto-generated method stub

		ArrayList<String> Qterms = query.GetTerms();
		//System.out.println("uniq q terms "+Qterms);
		
		List<Integer> smList = new ArrayList<Integer>(); 
		
		StringBuilder UniqTerms = new StringBuilder();
		for (String term : Qterms)
			UniqTerms.append(" " + term);

		String newQuery = UniqTerms.toString().trim();
		
		//System.out.println("newQuery "+newQuery);
		
		SmallestWindowUtils SWutil = new SmallestWindowUtils();

		//System.out.println("getUrl "+Doc.getUrl());

		int URLSmallestWindow = SWutil
				.getSmallestWindow(newQuery, Doc.getUrl());
		
		//System.out.println("Doc.getHeaders() "+Doc.getHeaders());

		int HEADERSmallestWindow = Integer.MAX_VALUE;
		for (String header : Doc.getHeaders()) {

			int sm = SWutil.getSmallestWindow(newQuery, header);
			if (sm < HEADERSmallestWindow)
				HEADERSmallestWindow = sm;
		}
		//System.out.println("Doc.getbodyHitPos() "+Doc.getbodyHitPos());

		int BODYSmallestWindow = SWutil.getSmallestWindow(newQuery,Doc.getbodyHitPos());
		

		int TITLESmallestWindow = SWutil.getSmallestWindow(newQuery,
				Doc.getTitle());

		int ANCHORSmallestWindow = Integer.MAX_VALUE;
		//System.out.println("Doc.getAnchorText() "+Doc.getAnchorText());

		for (String anchor : Doc.getAnchorText()) {

			int sm = SWutil.getSmallestWindow(newQuery, anchor);
			if (sm < ANCHORSmallestWindow)
				ANCHORSmallestWindow = sm;
		}
		
		// SWutil.getSmallestWindow(newQuery,Doc.getAnchorTextCountMap());

		smList.add(BODYSmallestWindow);
		smList.add(TITLESmallestWindow);
		smList.add(HEADERSmallestWindow);
		smList.add(URLSmallestWindow);
		smList.add(ANCHORSmallestWindow);

		
		return FindMin(smList);
	}

	private int FindMin(List<Integer> smList) {
		// TODO Auto-generated method stub
		int min = Integer.MAX_VALUE;
		for(int i : smList)
			if(i < min)
				min =i;
		return min;
	}

	public Map<DocFeature, Integer> getSmallestWindows(DocData Doc, QueryData query) {
		
		Map<DocFeature, Integer> map = new EnumMap<DocData.DocFeature, Integer>(DocData.DocFeature.class);

		ArrayList<String> Qterms = query.GetTerms();
		
		StringBuilder UniqTerms = new StringBuilder();
		for (String term : Qterms) {
			UniqTerms.append(" " + term);
		}

		String newQuery = UniqTerms.toString().trim();
		
		SmallestWindowUtils SWutil = new SmallestWindowUtils();

		int URLSmallestWindow = SWutil
				.getSmallestWindow(newQuery, Doc.getUrl().replaceAll("[://?.+_=~&-]", " ").replaceAll("%20", " ").replaceAll("\\s+", " ").toLowerCase());
		
		int HEADERSmallestWindow = Integer.MAX_VALUE;
		for (String header : Doc.getHeaders()) {

			int sm = SWutil.getSmallestWindow(newQuery, header);
			if (sm < HEADERSmallestWindow)
				HEADERSmallestWindow = sm;
		}

		int BODYSmallestWindow = SWutil.getSmallestWindow(newQuery,Doc.getbodyHitPos());
		

		int TITLESmallestWindow = SWutil.getSmallestWindow(newQuery,
				Doc.getTitle());

		int ANCHORSmallestWindow = Integer.MAX_VALUE;

		for (String anchor : Doc.getAnchorText()) {

			int sm = SWutil.getSmallestWindow(newQuery, anchor);
			if (sm < ANCHORSmallestWindow)
				ANCHORSmallestWindow = sm;
		}
		
		map.put(DocFeature.ANCHOR, ANCHORSmallestWindow);
		map.put(DocFeature.BODY, BODYSmallestWindow);
		map.put(DocFeature.HEADER, HEADERSmallestWindow);
		map.put(DocFeature.TITLE, TITLESmallestWindow);
		map.put(DocFeature.URL, URLSmallestWindow);

		
		return map;
	}
}
