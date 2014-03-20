package cs276.programming.index;

/**
 * Interface for writing a postings list.
 */
public interface PostingWriter {

	/**
	 * Add a term id to the postings list.
	 * @param termID
	 */
	void addTermID(int termID);
	
	/**
	 * Add a doc id against the current term id's posting.
	 * @param docID
	 */
	void addDocID(int docID);
	
	/**
	 * Add the term frequency for the current docid+termid pair.
	 * 
	 * @param termFreq
	 */
	void addTermFreq(int termFreq);
	
//	void addPosition(int pos); //placeholder for future functionality :)
	
	/**
	 * Mark the end of the current term id's posting.
	 */
	void endPosting();
	
	/**
	 * Mark the end of the postings list. Close any open resources.
	 */
	void close();
}
