package cs276.programming.index;

/**
 * class to hold a term id - doc id pair.
 * 
 */
public final class TermDoc {
	private final int termID;
	private final int docID;

	public TermDoc(int termID2, int docID2) {
		termID = termID2;
		docID = docID2;
	}

	public int getTermID() {
		return termID;
	}

	public int getDocID() {
		return docID;
	}

	// @Override
	// public String toString() {
	// return "(" + termID + ", " + docID + ")";
	// }
}