package cs276.programming.query;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class DocDict {

	private static HashMap<Integer, String> docDictMap;

	DocDict(String docDictFile) {
		docDictMap = new HashMap<Integer, String>();

		BufferedReader in = null;
		String strLine;

		try {
			in = new BufferedReader(new FileReader(new File(docDictFile)));

			while (((strLine = in.readLine()) != null)) {
				String[] fields = strLine.split("\t");
				String doc = fields[0];
				int id = Integer.parseInt(fields[1]);
				docDictMap.put(id, doc);

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

	public static String getDoc(int wordid) {

		// return the termid for the word
		return docDictMap.get(wordid);

	}

	void printList() {
		for (int key : docDictMap.keySet()) {
			System.out.println("key:" + key + ",value:" + docDictMap.get(key));
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String IndexDir = "/Users/sdey/Documents/sdey_personal/apps/workspace/CS276/index";
		String WordDictFile = IndexDir + "/doc.dict";

		DocDict word = new DocDict(WordDictFile);
		word.printList();
		// word.printList();
		int id = 20;
		System.out.println("key:" + word.getDoc(id));

	}

}
