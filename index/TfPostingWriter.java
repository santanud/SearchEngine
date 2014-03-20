package cs276.programming.index;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class TfPostingWriter implements PostingWriter {

	private PrintWriter pw;
	private boolean isFirstDoc = true;
	private boolean isPostingEnded = true;
	
	//temp store for doc id while awaiting the term freq. If termfreq is not added, this doc id will be skipped!! (that is intentional)
	private int currDocID = -1;
	
	public TfPostingWriter(File file) {
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
		currDocID = docID;
	}

	@Override
	public void addTermFreq(int termFreq) {
		addDocID(currDocID, termFreq);
	}

	private void addDocID(int docID, int termFreq) {
		if(isFirstDoc) {
			pw.print(docID + TfPostingReader.DOC_ID_TERM_FREQ_SEPARATOR + termFreq);
			isFirstDoc = false;
		} else {
			pw.print(TfPostingReader.DOC_ID_SEPARATOR + docID + TfPostingReader.DOC_ID_TERM_FREQ_SEPARATOR + termFreq);
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
}
