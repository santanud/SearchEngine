package cs276.programming.spellchecker;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Class to manage the Channel Model for the spelling
 * corrector. 
 *
 */
public class ChannelModel implements java.io.Serializable {

	private static final long serialVersionUID = 4791587681330971964L;

	public Map<String, Integer> BadToGoodQueryMap;
	public Map<String, Integer> MispelledWords;
	private double LAMBDA = 1;
    private double ALPHA = 1;
    
    /**
     * The probability of a correctly typed word. This parameter is tuned.
     * P(R|Q) |R==Q
     */
    private double EMPIRICAL_QUERY_NO_CHG_PROBABILITY = 0.90;
    
	private ChannelModelType modelType = ChannelModelType.UNIFORM;

	private ErrorProbabilityModel errProbModel;
	private double COST_PER_EDIT = 0.01;

	ChannelModel() {}
	
	ChannelModel(ChannelModelType type) {
		this.modelType = type;
	}

	public void setChannelModelType(ChannelModelType type) {
		this.modelType = type;
	}
	
	/**
	 * Train the Channel Model.
	 * 
	 * @param MisspelledQueriesFile Location of the file containing query pairs that are
	 * one edit distance apart. 
	 */
	public void train(String MisspelledQueriesFile) {
		
		ErrorProbablityTrainer ErrProbEstimate = new ErrorProbablityTrainer();
		errProbModel = ErrProbEstimate.train(MisspelledQueriesFile);
		
		BadToGoodQueryMap = new HashMap<String, Integer>();
		MispelledWords = new HashMap<String, Integer>();

		// populate the hashmap with sample query correction
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(MisspelledQueriesFile));

			String strLine = "";
			ArrayList<String> StrList1;
			ArrayList<String> StrList2;

			while (((strLine = in.readLine()) != null)) {
				String[] Words = strLine.split("\t");
			//	String[] S = FindDifferences(Words[0], Words[1]);
			//	System.out.println(Words[0]+":"+Words[1]);
				
				StrList1 = new ArrayList<String>(); 
				StrList2 = new  ArrayList<String>();
						 
				NGramModel.NormalizeString(StrList1, StrList2, Words[0], Words[1]);
				int i = 0;
				for (String st1 :StrList1) {
					if (!st1.equals(StrList2.get(i))){
						//System.out.println(st1 + "<>" + StrList2.get(i));
						IncrementMap(BadToGoodQueryMap, st1 + "<>" + StrList2.get(i));
						IncrementMap(MispelledWords, st1);
					}
					i++;
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if(in != null) in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	void SetMu(double ChannelLambda){
		LAMBDA = ChannelLambda;
	}
	
	void setCostPerEdit(double cpe) {
		COST_PER_EDIT = cpe;
	}
	
	void setProbabilityRawQueryCorrect(double nchp) {
		EMPIRICAL_QUERY_NO_CHG_PROBABILITY = nchp;
	}

	protected void setUseDefaultNoEditProbability(
			boolean useDefaultNoEditProbability) {
		errProbModel.setUseDefaultNoEditProbability(useDefaultNoEditProbability);
	}
	
	protected void setProbabilityOfCharNoEdits(double pROBABILITY_NO_EDITS) {
		errProbModel.setProbabilityOfCharNoEdits(pROBABILITY_NO_EDITS);
	}

	double getChannelProbability(String RawQuery, String CorrectedQuery) {
		
		switch(modelType) {
		case UNIFORM:
			StringComparator strComp = new StringComparator();
			int editCount = strComp.determineEditDistance(RawQuery, CorrectedQuery);
			//P(R|Q) = P(edit)^Num-Edits * P(no edit)^Num-no-edits
			return RawQuery.equals(CorrectedQuery) ? EMPIRICAL_QUERY_NO_CHG_PROBABILITY :
				Math.pow(COST_PER_EDIT, editCount) * Math.pow( (1-COST_PER_EDIT), (CorrectedQuery.length() - editCount));

//			if (RawQuery.equals(CorrectedQuery)) {
//				return 0.95f;
//			} else {
//				String key = RawQuery + "<>" + CorrectedQuery;
//				int val = 0;
//				int freq = 0;
//				if(BadToGoodQueryMap.containsKey(key)) {
//					val = BadToGoodQueryMap.get(key);
//				}
//				if (MispelledWords.containsKey(RawQuery)){
//					freq = MispelledWords.get(RawQuery);
//				}
//				return (double)(val+1 )/(double)(freq + MispelledWords.size());
//			}
		case EMPIRICAL:
			//determine the probability of edits based on the edit1s data
			//determine the current probability based on the above
			
			return RawQuery.equals(CorrectedQuery) ? EMPIRICAL_QUERY_NO_CHG_PROBABILITY :
				errProbModel.getChannelProbability(RawQuery, CorrectedQuery);
			
		case EXTRA:
			return RawQuery.equals(CorrectedQuery) ? EMPIRICAL_QUERY_NO_CHG_PROBABILITY :
				errProbModel.getChannelProbability(RawQuery, CorrectedQuery) * getWordErrProbability( RawQuery,  CorrectedQuery);
		}
		return 0.0f;
	}
    
    double getWordErrProbability(String RawQuery, String CorrectedQuery) {
        
		if (RawQuery.equals(CorrectedQuery)) {
			return 0.95D;
		} else {
			String key = RawQuery + "<>" + CorrectedQuery;
			int val = 0;
			int freq = 0;
			if (BadToGoodQueryMap.containsKey(key)) {
				val = BadToGoodQueryMap.get(key);
			}
			if (MispelledWords.containsKey(RawQuery)) {
				freq = MispelledWords.get(RawQuery);
			}
			return (double) (val + 1*ALPHA) / (double) (freq + MispelledWords.size()*ALPHA);
		}
	}

	void IncrementMap(Map<String, Integer> MyMap, String Key) {
		int i;
		if (!MyMap.containsKey(Key)) {
			MyMap.put(Key, 1);
		} else {
			i = MyMap.get(Key);
			MyMap.put(Key, i + 1);

		}
	}

	public static void main(String[] args) {

		String MisspelledQueriesFile = args.length > 0 ? args[0] : "/cs276/PA2/data/edit1s.txt";
		ChannelModel M = new ChannelModel(ChannelModelType.UNIFORM);
		M.train(MisspelledQueriesFile);
		
		String RawQuery="to a";
		String CorrectedQuery="to";
		String key = RawQuery + "<>" + CorrectedQuery;
		System.out.println(M.BadToGoodQueryMap.get(key));
	
		System.out.println(M.MispelledWords.get(RawQuery));

		System.out.println(M.getChannelProbability(RawQuery,CorrectedQuery));
		System.out.println(Math.log(M.getChannelProbability(RawQuery,CorrectedQuery)));

	}

}
