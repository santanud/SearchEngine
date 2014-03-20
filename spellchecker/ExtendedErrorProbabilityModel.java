package cs276.programming.spellchecker;

import java.util.Map;

/**
 * Based on the model proposed in 
 * Brill, E., Moore, R. C., "An Improved Error Model for Noisy Channel Spelling Correction. 
 * In proceedings of 38th Annual meeting of Association for Computational Linguistics, 2000.
 *
 * This implementation is incomplete!!!
 * 
 */
public class ExtendedErrorProbabilityModel implements Model {

	private final Map<String, Integer> charCounter;
	private final Map<ExtendedEdit, Integer> editCounter;
//	private final Map<Character, Integer> charEditCount;
	private final int alphabetSize;

	private static StringComparator strComp = new StringComparator();

	public ExtendedErrorProbabilityModel(Map<String, Integer> charCounter2,
			Map<ExtendedEdit, Integer> editCounter2, int size) {

		this.charCounter = charCounter2;
		this.editCounter = editCounter2;
		this.alphabetSize = size;
	}

	@Override
	public double getChannelProbability(String rawQuery, String correctedQuery) {
		return 0;
	}

}
