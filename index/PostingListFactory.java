package cs276.programming.index;

import java.io.File;

import cs276.programming.util.Parameters.IndexType;

public class PostingListFactory {

	public static PostingListManager getPLManager(IndexType type) {
		switch(type){
		
		case NO_COMPRESSION:
			return getSimplePostingListManager();
		case VAR_BYTE_GAP_ENCODING:
			return PostingListFactory.getGapVarEncodedPostingListManager();
		case GAMMA_BIT_ENODED:
			return PostingListFactory.getGammaEncodedPostingListManager();
		case TERM_FREQ:
			return PostingListFactory.getTermFreqPostingListManager();
		default:
			System.err.println(type + " : no valid index type");
			System.exit(1);
			return null;
		}
	}
	
	public static PostingListManager getTermFreqPostingListManager() {
		return new PostingListManager() {

			@Override
			public PostingWriter getWriter(File file) {
				return new TfPostingWriter(file);
			}

			@Override
			public PostingReader getReader(File file) {
				return new TfPostingReader(file);
			}

			@Override
			public PostingReader getParser() {
				return new TfPostingReader();
			}

		};

	}

	/**
	 * Returns a Class that process files holding postings list in
	 * a simple <term id>\tdocid[,<doc id>]* format. 
	 *
	 */
	public static PostingListManager getSimplePostingListManager() {
		return new PostingListManager() {

			@Override
			public PostingWriter getWriter(File file) {
				return new SimplePostingWriter(file);
			}

			@Override
			public PostingReader getReader(File file) {
				return new SimplePostingReader(file);
			}

			@Override
			public PostingReader getParser() {
				return new SimplePostingReader();
			}

		};

	}
	
	/**
	 * Returns a Class to process files holding postings list in
	 * a gap variable length format. 
	 *
	 */
	public static PostingListManager getGapVarEncodedPostingListManager() {
		return new PostingListManager() {

			@Override
			public PostingWriter getWriter(File file) {
				return new GapVarLenWriter(file);
			}

			@Override
			public PostingReader getParser() {
				return new GapVarLenReader();
			}

			@Override
			public PostingReader getReader(File file) {
				return new GapVarLenReader(file);
			}

		};
	}
	
	public static PostingListManager getGammaEncodedPostingListManager() {
		return new PostingListManager() {
			
			static final boolean DEBUG = false;
			
			@Override
			public PostingWriter getWriter(File file) {
				return new GammaPLWriter(file);
			}

			@Override
			public PostingReader getParser() {
				return new GammaPLReader();
			}

			@Override
			public PostingReader getReader(File file) {
				return new GammaPLReader(file);
			}

};
	}
	
	public static PostingListManager getGapEncodedPostingListManager() {
		
		return new PostingListManager() {

			@Override
			public PostingWriter getWriter(File file) {
				return new GapEncodedWriter(file);
			}

			@Override
			public PostingReader getParser() {
				return new GapEncodedReader();
			}

			@Override
			public PostingReader getReader(File file) {
				return new GapEncodedReader(file);
			}

		};
	}
}
