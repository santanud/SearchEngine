package cs276.programming.index;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;

class GapVarLenWriter implements PostingWriter {

	private RandomAccessFile raf = null;
	private PrintWriter postingPositionFile = null;
	
	private boolean isFirstDoc = true;
	private boolean isPostingEnded = true;
	private int prevDocID = -1;
	
	private int currDocFreq = 0;
	private int currTermId = -1;
	private long currFilePos = -1;
	
	public GapVarLenWriter(File file) {
		try {
			raf = new RandomAccessFile(file, "rw");
			postingPositionFile = new PrintWriter(new File(file.getAbsoluteFile() + ".pos"));
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
			raf.write(varEncode(termID));
//				System.err.println("term id - " + termID);
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
		try {
			currDocFreq++;
			if(isFirstDoc) {
				raf.write(varEncode(docID));
				isFirstDoc = false;
			} else {
				int docIdGap = docID - prevDocID;
				raf.write(varEncode(docIdGap));
//					System.err.println("docid - " + docIdGap);
			}
			prevDocID = docID;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private byte[] varEncode(int docIdGap) {
		
		int i = docIdGap;
		byte[] ba = new byte[4];
		int pos = 0;
		while(i != 0 && i!=-1) {
			ba[pos++] = (byte) (i & ((1 << 7) - 1)); 
			i = i >> 7;
		}
		//flip the continuation bit
		ba[0] = (byte) (ba[0] | (1<<7));
		
		byte[] b2 = new byte[pos];
		int p2 = 0;
		while(pos > 0) {
			b2[p2++] = ba[--pos];
		}
		return b2;
	
	}

	@Override
	public void endPosting() {
		postingPositionFile.println(currTermId + "\t" + currFilePos + "\t" + currDocFreq);
		isPostingEnded = true;
		currDocFreq = 0;
	}

	@Override
	public void close() {
		try {
			if(!isPostingEnded) {
				endPosting();
			}
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