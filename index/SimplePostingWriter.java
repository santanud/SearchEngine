package cs276.programming.index;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

class SimplePostingWriter implements PostingWriter {

	private PrintWriter pw;
	private boolean isFirstDoc = true;
	private boolean isPostingEnded = true;
	
	public SimplePostingWriter(File file) {
		try {
			pw = new PrintWriter(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void addTermID(int termID) {
		if(!isPostingEnded) {
			pw.println();
		}
		pw.printf("%d\t", termID);
		isFirstDoc = true;
		isPostingEnded = false;
	}

	@Override
	public void addDocID(int docID) {
		if(isFirstDoc) {
			pw.print(docID);
			isFirstDoc = false;
		} else {
			pw.print("," + docID);
		}
	}

	@Override
	public void endPosting() {
		pw.println();
		isPostingEnded = true;
	}

	@Override
	public void close() {
		pw.close();
	}

	@Override
	public void addTermFreq(int termFreq) {
		//do nothing
	}
}