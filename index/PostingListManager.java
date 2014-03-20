package cs276.programming.index;

import java.io.File;

/**
 * A facade that returns instance of the postings list
 * reader and writer for a specific postings list format. 
 *
 */
public interface PostingListManager {

	PostingWriter getWriter(File file);
	PostingReader getParser();
	PostingReader getReader(File file);
}
