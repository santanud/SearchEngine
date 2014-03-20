package cs276.programming.index;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;

class GammaPLWriter implements PostingWriter {

	private static final boolean DEBUG = false;
	
	private RandomAccessFile raf = null;
	private PrintWriter postingPositionFile = null;
	private GammaBuffer gammaBuffer = null;
	
	private boolean isFirstDoc = true;
	private boolean isPostingEnded = true;
	private int prevDocID = -1;
	
	private int currDocFreq = 0;
	private int currTermId = -1;
	private long currFilePos = -1;
	private long currBitOffset = -1;
	
	public GammaPLWriter(File file) {
		try {
			raf = new RandomAccessFile(file, "rw");
			postingPositionFile = new PrintWriter(new File(file.getAbsoluteFile() + ".pos"));
			gammaBuffer = new GammaBuffer(raf, false);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void addTermID(int termID) {
		try {
			if(!isPostingEnded) {
				endPosting();
			}
			currFilePos = raf.getFilePointer(); 
			//handle the bit offset from the last byte!!
			currBitOffset = gammaBuffer.getSize();
			gammaBuffer.appendGamma(termID);
			currTermId = termID;
			currDocFreq = 0;
		} catch (IOException e) {
			e.printStackTrace();
		}
		isFirstDoc = true;
		isPostingEnded = false;
		prevDocID = -1;
		currDocFreq = 0;
	}

	@Override
	public void addDocID(int docID) {
		currDocFreq++;
		if(isFirstDoc) {
			gammaBuffer.appendGamma(docID);
			isFirstDoc = false;
		} else {
			int docIdGap = docID - prevDocID;
			gammaBuffer.appendGamma(docIdGap);
		}
		prevDocID = docID;
		if(DEBUG) {
			System.err.printf("%d, ", docID);
		}
	}

	@Override
	public void endPosting() {
		postingPositionFile.println(currTermId + "\t" + currFilePos + "\t" + currBitOffset + "\t" + currDocFreq);
		isPostingEnded = true;
		currDocFreq = 0;
		gammaBuffer.flush();
	}

	@Override
	public void close() {
		try {
			if(!isPostingEnded) {
				endPosting();
			}
			gammaBuffer.flushAll();
			raf.close();
			postingPositionFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void addTermFreq(int termFreq) {
		//do nothing
	}
}