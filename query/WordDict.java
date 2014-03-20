package cs276.programming.query;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class WordDict {

	/**
	 * @param args
	 */
	private static HashMap<String, Integer> wordDictMap;

	WordDict(String wordDictFile) {
		// populate
		wordDictMap = new HashMap<String, Integer>();
		// Populate the WordDictionary Here
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(new File(wordDictFile)));

			String strLine = "";

			while (((strLine = in.readLine()) != null)) {
				String[] fields = strLine.split("\t");
				String text = fields[0];
				int id = Integer.parseInt(fields[1]);
				wordDictMap.put(text, id);
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

	public static int getTermId(String word) {

		// return the termid for the word
		if (wordDictMap.containsKey(word)){
			return wordDictMap.get(word);	
		}
		else{
			return -1;
		}
			
		

	}

	void printList() {
		for (String key : wordDictMap.keySet()) {
			System.out.println("key:" + key + ",value:" + wordDictMap.get(key));
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String IndexDir = "/Users/sdey/Documents/sdey_personal/apps/workspace/CS276/index";
		String WordDictFile = IndexDir + "/word.dict";

		WordDict word = new WordDict(WordDictFile);
		word.printList();
		String w = "ebay";
		System.out.println("key:" + word.getTermId(w));

	}

}
