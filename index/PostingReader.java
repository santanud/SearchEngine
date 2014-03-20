package cs276.programming.index;

import java.util.ArrayList;

import cs276.programming.util.TermStat;

/**
 * Interface for reading a postings list.
 *
 */
public interface PostingReader {

	TermStat getPostingLineStats(String line); //Required?
	
	/**
	 * Read the next posting line at the current location.
	 * @return
	 */
	PostingLine getNextPosting();

	/**
	 * Retreive the list of doc ids associated with
	 * the term id at the location specified by the
	 * passed termstat.
	 * @param termStat
	 * @return
	 */
	ArrayList<Integer> getDocidList(TermStat termStat);
	
	/**
	 * Close and clean up any resources being used for reading
	 * the postings list.
	 */
	void close();
}
