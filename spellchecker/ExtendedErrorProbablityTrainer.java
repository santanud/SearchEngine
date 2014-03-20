package cs276.programming.spellchecker;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Based on the model proposed in 
 * Brill, E., Moore, R. C., "An Improved Error Model for Noisy Channel Spelling Correction. 
 * In proceedings of 38th Annual meeting of Association for Computational Linguistics, 2000.
 * 
 * This implementation is incomplete!!!
 */
public class ExtendedErrorProbablityTrainer implements Trainer<ExtendedErrorProbabilityModel>, Serializable {

	private static final long serialVersionUID = -1921927438982070155L;
	private int strSize = 2; //TODO Tune this
	private static StringComparator strComp = new StringComparator();

	@Override
	public ExtendedErrorProbabilityModel train(String edit1sFile) {
		String[] words;
		Map<String, Integer> charCounter = new HashMap<String, Integer>();
		Map<ExtendedEdit, Integer> editCounter = new HashMap<ExtendedEdit, Integer>();

		//to determine the size of the Alphabet - number of unique chars in the data + String-begin-char
		Set<Character> alphabet = new HashSet<Character>();
		alphabet.add(StringComparator.STR_BEGIN_CHAR);

		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(edit1sFile));

			String strLine = "";

			while (((strLine = in.readLine()) != null)) {
				words = strLine.split("\t");
				countChars(charCounter, words[1]);
				if(!words[0].equals(words[1])) {
					List<Edit> edits = strComp.determineEdits(words[0], words[1]);
					if(edits.size() == 1) { //ignore multi-edits
						//keep track of the edits
//						increment(editCounter, edits.get(0)); //TODO
					} else {
						System.err.printf("Found multi-edit pair in edit1s data - %s, %s %s\n", words[0], words[1], edits);
					}
				}
				updateAlphabet(words[0], alphabet);
				updateAlphabet(words[1], alphabet);
			}
			
			System.err.printf("ErrorProbabilityModel: %d unique characters; %d instance-type of Edits; %d count of chars & char-pairs\n", 
					alphabet.size(), editCounter.keySet().size(), charCounter.keySet().size());
			ExtendedErrorProbabilityModel epModel = new ExtendedErrorProbabilityModel(charCounter, editCounter, alphabet.size());
			return epModel;
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				if(in != null) in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void updateAlphabet(String word, Set<Character> alphabet) {
		for(int i = 0; i < word.length(); i++) {
			alphabet.add(word.charAt(i));
		}
	}
	
	private <T> void increment(Map<T, Integer> map, T key) {
		if(map.containsKey(key)) {
			map.put(key, map.get(key)+1);
		} else {
			map.put(key, 1);
		}
	}
	
	private void countChars(Map<String, Integer> charCounter, String str) {
		
		for(int i = -1; i < str.length(); i++) {
			String subStr = "" + (i == -1 ? StringComparator.STR_BEGIN_CHAR : str.charAt(i));
			increment(charCounter, subStr);
			for(int j = 1; j < strSize; j++) {
				if(i+j >= str.length()) {
					break;
				}
				subStr += str.charAt(i + j);
				increment(charCounter, subStr);
			}
		}
	}
}
