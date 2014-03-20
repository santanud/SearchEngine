package cs276.programming.index;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cs276.programming.util.TermStat;

public class TfPostingReader implements PostingReader {

	@Override
	public ArrayList<Integer> getDocidList(TermStat termStat) {
		return getDocidList(termStat.getfilePos());
	}

	private FileInputStream fis = null;
	private Matcher lm;

	private RandomAccessFile CorpusFileReader = null;

	protected static final String TERM_SEPARATOR = "\t";
	protected static final String DOC_ID_SEPARATOR = ",";
	protected static final String DOC_ID_TERM_FREQ_SEPARATOR = ";";

	public TfPostingReader() {
	}
	
	public TfPostingReader(File file) {
		try {
			CorpusFileReader = new RandomAccessFile(file, "r");
			
			//for the parser part
			Charset charset = Charset.forName("ISO-8859-15");
		    CharsetDecoder decoder = charset.newDecoder();
		    Pattern pattern = Pattern.compile("[^\n]*[\n]");
		    
			fis = new FileInputStream(file);
			FileChannel fc = fis.getChannel();
			int sz = (int)fc.size();
			MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, sz);
			CharBuffer cb = decoder.decode(bb);
			
			lm = pattern.matcher(cb);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
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
		int[] termFreqs = new int[docIdStrs.length];
		for(int i = 0; i < docIdStrs.length; i++) {
			String[] docIdtfStrs = docIdStrs[i].split(DOC_ID_TERM_FREQ_SEPARATOR);
			docIDs[i] = Integer.parseInt(docIdtfStrs[0]); //ignoring parse error
			termFreqs[i] = Integer.parseInt(docIdtfStrs[1]); //ignoring parse error
		}
		return new PostingLine(termID, docIDs, termFreqs);
	}

	@Override
	public PostingLine getNextPosting() {

		if (lm.find()) {
			CharSequence cs = lm.group();
			PostingLine pl = parsePostingLine(cs.toString().trim());
			return pl;
		}
//		try {
//			String line = CorpusFileReader.readLine();
//			if(line != null) {
//				PostingLine pl = parsePostingLine(line);
//				return pl;
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		return null;
	}

	@Override
	public void close() {
		
		try {
			if(fis != null) fis.close();
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

					String[] docIdtfStrs = wordList[i].split(DOC_ID_TERM_FREQ_SEPARATOR);
					li.add(Integer.parseInt(docIdtfStrs[0]));
				}
				return li;
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
