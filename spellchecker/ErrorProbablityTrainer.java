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
 * Class to train the Empirical Model.
 *
 */
public class ErrorProbablityTrainer implements Trainer<ErrorProbabilityModel>, Serializable {
	
	private static final long serialVersionUID = 646849500725568648L;

	private static StringComparator strComp = new StringComparator();
	
	/**
	 * Trains the Noisy Channel Model.
	 */
	public ErrorProbabilityModel train(String edit1sFile) {
		String[] words;
		Map<String, Integer> charCounter = new HashMap<String, Integer>();
		Map<Edit, Integer> editCounter = new HashMap<Edit, Integer>();

		//to determine the size of the Alphabet - number of unique chars in the data + String-begin-char
		Set<Character> alphabet = new HashSet<Character>();
		alphabet.add(StringComparator.STR_BEGIN_CHAR);

		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(edit1sFile));

			String strLine = "";

			while (((strLine = in.readLine()) != null)) {
				words = strLine.split("\t");
				//count char-bigrams & chars
				//Should space be considered? What about special chars? Yes
				countChars(charCounter, words[1]); 
				if(!words[0].equals(words[1])) { //to take care of equal strings in the data 
					List<Edit> edits = strComp.determineEdits(words[0], words[1]);
					if(edits.size() == 1) { //ignore multi-edits
						//keep track based on edit type
						increment(editCounter, edits.get(0));
					} else {
						System.err.printf("Found multi-edit pair in edit1s data - %s, %s %s\n", words[0], words[1], edits);
					}
				}
				updateAlphabet(words[0], alphabet);
				updateAlphabet(words[1], alphabet);
			}
			
			System.err.printf("ErrorProbabilityModel: %d unique characters; %d instance-type of Edits; %d count of chars & char-pairs\n", 
					alphabet.size(), editCounter.keySet().size(), charCounter.keySet().size());
			ErrorProbabilityModel epModel = new ErrorProbabilityModel(charCounter, editCounter, alphabet.size());
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

	/**
	 * Keep track of the unique characters in the training data.
	 */
	private void updateAlphabet(String word, Set<Character> alphabet) {
		for(int i = 0; i < word.length(); i++) {
			alphabet.add(word.charAt(i));
		}
	}

	/**
	 * Increment an entry in the counter map.
	 */
	private <T> void increment(Map<T, Integer> map, T key) {
		if(map.containsKey(key)) {
			map.put(key, map.get(key)+1);
		} else {
			map.put(key, 1);
		}
	}

	/**
	 * Count the character unigram & bigrams in the training data.
	 */
	private void countChars(Map<String, Integer> charCounter, String str) {
		
		//assuming that the str is non-empty!
		increment(charCounter, "" + StringComparator.STR_BEGIN_CHAR + str.charAt(0));
		for(int i = 0; i < str.length()-1; i++) {
			increment(charCounter, "" + str.charAt(i));
			increment(charCounter, "" + str.charAt(i) + str.charAt(i+1));
		}
		increment(charCounter, "" + str.charAt(str.length()-1));
	}
}
