package cs276.programming.index;

/**
 * A line of posting
 *
 */
public class PostingLine {
	
	protected final int termID;
	protected final int[] docIDs;
	protected final int[] termFreqs;
	
	public PostingLine(int termID, int[] docIDs) {
		this.termID = termID;
		this.docIDs = docIDs;
		termFreqs = null;
	}
	
	public PostingLine(int termID, int[] docIDs, int[] termFreqs) {
		this.termID = termID;
		this.docIDs = docIDs;
		this.termFreqs = termFreqs; //trusting that docIDs.length == termFreqs.length
	}
	
	public int getTermID() {
		return termID;
	}

	public int getDocFreq() {
		return docIDs.length;
	}
	
	//method to walk through the doc id list
	private int currDocIdPos = -1;
	public int getNextDocID() {
		currDocIdPos++;
		return (currDocIdPos < docIDs.length)  ?  docIDs[currDocIdPos] : -1;
	}
	//method to be used in conjunction with getNextDocID
	public int getCorrespondingTermFreq() {
		if(termFreqs != null) {
			return (currDocIdPos < docIDs.length)  ?  termFreqs[currDocIdPos] : -1;
		} else {
			return -1;
		}
	}
	
	@Override
	public String toString() {
		return "(" + termID + ", " + docIDs + ", " + termFreqs + ")";
	}
}