package cs276.programming.query;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import cs276.programming.util.TermStat;

/**
 * Class to help process the posting dict file.
 *
 */
public class PostingDict {

	private static HashMap<Integer, TermStat> ts;

	public PostingDict(String postingDictFile) {
		ts = new HashMap<Integer, TermStat>();
		BufferedReader in = null;
		String strLine;

		try {
			in = new BufferedReader(new FileReader(new File(postingDictFile)));

			while (((strLine = in.readLine()) != null)) {

				String[] fields = strLine.split("\t");
				TermStat t = TermStat.parseTermStat(strLine);
				ts.put(t.getTermId(), t);
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

	static long getFilePosition(int TermId) {

		TermStat pos = ts.get(TermId);

		if (pos == null) {
			return -1;
		} else {
			return pos.getfilePos();
		}

	}

	public static TermStat getTermStat(int TermId) {

		TermStat pos = ts.get(TermId);
		return pos;

	}

	static int getDocFreq(int TermId) {
		TermStat freq = ts.get(TermId);
		if (freq == null) {
			return 0;
		} else {
			return freq.getFreq();
		}

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String IndexDir = "/Users/sdey/Documents/sdey_personal/apps/workspace/CS276/index";
		String PostingDictFile = IndexDir + "/posting.dict";

		PostingDict pd = new PostingDict(PostingDictFile);

		System.out.println(pd.getFilePosition(200));

	}

}
