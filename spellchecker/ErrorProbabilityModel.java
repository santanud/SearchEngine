package cs276.programming.spellchecker;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cs276.programming.spellchecker.Edit.EditType;

/**
 * Class to determine the probability based on the Empirical
 * Cost model. 
 *
 */
public class ErrorProbabilityModel implements Model, Serializable {

	private static final long serialVersionUID = 671507852313186019L;

	private final Map<String, Integer> charCounter;
	private final Map<Edit, Integer> editCounter;
	private final Map<Character, Integer> charEditCount;
	private final int alphabetSize;
	
	private static final StringComparator strComp = new StringComparator();

	private double PROBABILITY_NO_EDITS = 0.95;
	/**
	 * Flag two toggle between two behaviors - if
	 * true - a fixed value (PROBABILITY_NO_EDITS) will be used for the probability that a specific character is not mistyped/entered.
	 * false - then the probability that the character in the input is not mistyped/entered is computed based on input data.
	 */
	private boolean useDefaultNoEditProbability = true;
	
	/**
	 * A toggle, used only for debug purposes - if
	 * true - the error probability is smoothed, due to data sparsity, using the Laplace add-one smoothing.
	 * false - (only used during debug) the raw error probability is used.
	 */
//	private static final boolean doLaplaceAddOneSmoothing = true;

	public ErrorProbabilityModel(Map<String, Integer> charCounter,
			Map<Edit, Integer> editCounter, int alphabetSize) {

		this.charCounter = Collections.unmodifiableMap(charCounter);
		this.editCounter = Collections.unmodifiableMap(editCounter);
		this.alphabetSize = alphabetSize;
		
		charEditCount = new HashMap<Character, Integer>();
		for(Edit e : editCounter.keySet()) {
			//count(Edit, insert ?char; del ?char; sub char?; trans char? & ?char)
			switch(e.getType()) {
			case INSERT:
				increment(charEditCount, e.getY());
				break;
			case DELETE:
				increment(charEditCount, e.getY());
				break;
			case SUBSTITUTE:
				increment(charEditCount, e.getX());
				break;
			case TRANSPOSE:
				increment(charEditCount, e.getX());
				increment(charEditCount, e.getY());
				break;
			}
		}
	}
	
	private void increment(Map<Character, Integer> map, Character x) {
		if(!map.containsKey(x)) {
			map.put(x, 1);
		} else {
			map.put(x, map.get(x) + 1);
		}
	}

	/**
	 * Determines the noisy channel probability.
	 */
	public double getChannelProbability(String rawQuery, String correctedQuery) {
		
		if(rawQuery.equals(correctedQuery)) {
			return 0.90; //ChannelModel.EMPIRICAL_QUERY_NO_CHG_PROBABILITY;
			//This is just a safety net. This method is not invoked if the two strings are identical.
		}
		
		List<Edit> edits = strComp.determineEdits(rawQuery, correctedQuery);
		Collections.sort(edits, new Comparator<Edit>() {
			@Override
			public int compare(Edit arg0, Edit arg1) {
				return arg0.getPosition() > arg1.getPosition() ? 1 : (arg0.getPosition() == arg1.getPosition() ? 0 : -1);
			}
		});
		
		double prob = 1;

		if(useDefaultNoEditProbability) {
			for(Edit e : edits) {
				prob *= getChannelProbability(e);
			}
			
			prob *= Math.pow(PROBABILITY_NO_EDITS, correctedQuery.length() - edits.size());
		} else {
			int editPos = 0;
			for(int i = 0; i < correctedQuery.length(); i++) {
				if(edits.size() > editPos && i == edits.get(editPos).getPosition()) {
					//there is an edit at current position
					prob *= getChannelProbability(edits.get(editPos));
					if(edits.get(editPos).getType() == EditType.TRANSPOSE) {
						//two positions involved, so move ahead two steps
						i++;
					}
					editPos++;
				} else {
					char chara = correctedQuery.charAt(i);
					prob *= smoothIt(getCharCount("" + chara), getCharCount("" + chara) + (charEditCount.containsKey(chara) ? charEditCount.get(chara) : 0));
//					PROBABILITY_NO_EDITS = count(char) in corrected / count(Edit, insert ?char; del ?char; sub char?; trans char?/?char)
				}
			}
		}
		
		return prob;
	}

	/**
	 * Determines the conditional probability of the specified
	 * edit based on the four confusion matrices.
	 */
	public double getChannelProbability(Edit e) {
		
		switch(e.getType()) {
		case DELETE:
			//P(x|w) = del[w_i-1, w_i] / count[w_i-1 w_i]
			return smoothIt(getEditCount(e), getCharCount("" + e.getX() + e.getY()));
			
		case TRANSPOSE:
			//P(x|w) = trans[w_i, w_i+1] / count[w_i w_i+1]
			return smoothIt(getEditCount(e), getCharCount("" + e.getX() + e.getY()));
			
		case INSERT:
			//P(x|w) = ins[w_i-1, x_i] / count[w_i-1]
			return smoothIt(getEditCount(e), getCharCount("" + e.getX()));
			
		case SUBSTITUTE:
			//P(x|w) = sub[x_i, w_i] / count[w_i]
			return smoothIt(getEditCount(e), getCharCount("" + e.getY()));
		}
		
		System.err.printf("Fallthrough in getChannelProbability %s\n", e);
		return 0; //should never happen
	}

	/**
	 * Smoothes the probability using the Laplace
	 * add one smoothing.
	 */
	protected double smoothIt(int numerator, int denominator) {
//		if(doLaplaceAddOneSmoothing) {
		return (1.0 * numerator + 1) / (denominator + alphabetSize);
//		} else {
//			return (1.0 * numerator) / (denominator); //No Smoothing
//		}
	}

	private int getEditCount(Edit e) {
		return editCounter.containsKey(e) ? editCounter.get(e) : 0;
	}
	
	private int getCharCount(String str) {
		return charCounter.containsKey(str) ? charCounter.get(str) : 0;
	}

	protected void setUseDefaultNoEditProbability(boolean useDefaultNoEditProbability) {
		this.useDefaultNoEditProbability = useDefaultNoEditProbability;
	}

	protected void setProbabilityOfCharNoEdits(double pROBABILITY_NO_EDITS) {
		PROBABILITY_NO_EDITS = pROBABILITY_NO_EDITS;
	}
}