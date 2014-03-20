package cs276.programming.index;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import cs276.programming.util.TermStat;

class GapEncodedReader implements PostingReader {

	private BufferedReader br = null;
	private RandomAccessFile CorpusFileReader = null;

	public GapEncodedReader() {
	}
	
	public GapEncodedReader(File file) {
		try {
			br = new BufferedReader(new FileReader(file));
			CorpusFileReader = new RandomAccessFile(file, "r");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public TermStat getPostingLineStats(String line) {
		String[] tokens = line.split("\t"); //assuming 2 tokens
		int termID = Integer.parseInt(tokens[0]);
		String[] docIdStrs = tokens[1].split(",");
		return new TermStat(termID, 0, docIdStrs.length);
	}

	public PostingLine parsePostingLine(String line) {
		String[] tokens = line.split("\t"); //assuming 2 tokens
		int termID = Integer.parseInt(tokens[0]);
		String[] docIdStrs = tokens[1].split(",");
		int[] docIDs = new int[docIdStrs.length];
		int prevDocID = 0;
		for(int i = 0; i < docIdStrs.length; i++) {
			docIDs[i] = Integer.parseInt(docIdStrs[i]) + prevDocID; //ignoring parse error
			prevDocID = docIDs[i]; 
		}
		return new PostingLine(termID, docIDs);
	}

	@Override
	public PostingLine getNextPosting() {
		
		try {
			String line = br.readLine();
			if(line != null) {
				PostingLine pl = parsePostingLine(line);
				return pl;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void close() {
		
		try {
			if(br != null) br.close();
			if(CorpusFileReader != null) CorpusFileReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public ArrayList<Integer> getDocidList(long filePosition) {

		ArrayList<Integer> li = new ArrayList<Integer>();
		try {
			CorpusFileReader.seek(filePosition);
			String posting = CorpusFileReader.readLine();
			if (posting != "" && posting != null) {
				String[] fields = posting.split("\t");
				String wlist = fields[1];
				String[] wordList = wlist.split(",");
				int prevDocID = 0;
				for (int i = 0; i <= wordList.length - 1; i++) {

					int currDocID = Integer.parseInt(wordList[i]) + prevDocID;
					li.add(currDocID);
					prevDocID = currDocID; 
				}
				return li;
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public ArrayList<Integer> getDocidList(TermStat termStat) {
		return getDocidList(termStat.getfilePos());
	}
}