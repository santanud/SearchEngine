package cs276.programming.index;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import cs276.programming.util.TermStat;

class SimplePostingReader implements PostingReader {

	private BufferedReader br = null;
	private RandomAccessFile CorpusFileReader = null;

	private static final String TERM_SEPARATOR = "\t";
	private static final String DOC_ID_SEPARATOR = ",";

	public SimplePostingReader() {
	}
	
	public SimplePostingReader(File file) {
		try {
			br = new BufferedReader(new FileReader(file));
			CorpusFileReader = new RandomAccessFile(file, "r");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public TermStat getPostingLineStats(String line) {
		String[] tokens = line.split(TERM_SEPARATOR); //assuming 2 tokens
		int termID = Integer.parseInt(tokens[0]);
		String[] docIdStrs = tokens[1].split(DOC_ID_SEPARATOR);
		return new TermStat(termID, 0, docIdStrs.length);
	}

	public PostingLine parsePostingLine(String line) {
		String[] tokens = line.split(TERM_SEPARATOR); //assuming 2 tokens
		int termID = Integer.parseInt(tokens[0]);
		String[] docIdStrs = tokens[1].split(DOC_ID_SEPARATOR);
		int[] docIDs = new int[docIdStrs.length];
		for(int i = 0; i < docIdStrs.length; i++) {
			docIDs[i] = Integer.parseInt(docIdStrs[i]); //ignoring parse error
		}
		return new PostingLine(termID, docIDs);
	}

	@Override
	public PostingLine getNextPosting() {
		
		try {
			String line = CorpusFileReader.readLine();
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
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
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
				String[] fields = posting.split(TERM_SEPARATOR);
				String wlist = fields[1];
				String[] wordList = wlist.split(DOC_ID_SEPARATOR);
				for (int i = 0; i <= wordList.length - 1; i++) {

					li.add(Integer.parseInt(wordList[i]));
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