package cs276.programming.index;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Class to hold a dictionary - which
 * maps a String to an id.
 *
 */
public class Dictionary {

	//The last used id
	private int lastID = 0;
	
	//The Map holding the dictionary
	private Map<String, Integer> dict = new HashMap<String, Integer>(1<<10); //Set default capacity to a larger value
	
	/**
	 * Method to generate the next available id.
	 * @return
	 */
	private int getNextID() {
		return ++lastID;
	}
	
	/**
	 * Get an id for the specified key. If the key
	 * does not exist in the dictionary, then it would add 
	 * it to the dictionary and return the assigned id.
	 * 
	 * @param key The key to be added to the dictionary
	 * @return
	 */
	public int getOrAssignID(String key) {
		
		if(dict.containsKey(key)) {
			return dict.get(key);
		} else {
			int value = getNextID();
			dict.put(key, value);
			return value;
		}
	}

	/**
	 * Returns the id associated with the specified 
	 * key. If the key does not exist in the dictionary, 
	 * then it will return -1.
	 * 
	 * @param key The key to be looked up in the dictioanry.
	 * @return
	 */
	public int getID(String key) {
		
		if(dict.containsKey(key)) {
			return dict.get(key);
		} else {
			return -1;
		}
	}

	/**
	 * Save the dictionary to a file.
	 * 
	 * @param outDir The folder where the dictionary is to be saved.
	 * @param outFile The filename for the dictionary.
	 */
	public void saveDict(File outDir, String outFile) {
		try {
			File dictFile = new File(outDir, outFile);
			PrintWriter pw = new PrintWriter(dictFile);
			for(String key : dict.keySet()) {
				pw.println(key + "\t" + dict.get(key));
			}
			pw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns the size of the dictionary.
	 * @return
	 */
	public int size() {
		return dict.size();
	}
	
	/**
	 * Loads a saved dictionary from a file.
	 * 
	 * @param file The location of the dictionary
	 * @return
	 */
	public static Dictionary loadDict(File file) {

		Dictionary dictionary = new Dictionary();
		// populate
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(file));

			String strLine = "";

			while (((strLine = in.readLine()) != null)) {
				String[] fields = strLine.split("\t");
				String text = fields[0];
				int id = Integer.parseInt(fields[1]);
				dictionary.dict.put(text, id);
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
		
		return dictionary;
	}
}
