package cs276.programming.util;

/**
 * Parameters shared across the code base. 
 */
public class Parameters {

	public static boolean DEBUG = false;
	public static final String POSTING_DICT_FILENAME = "posting.dict";
	public static final String CORPUS_INDEX_FILENAME = "corpus.index";
	public static final String DOC_DICT_FILENAME = "doc.dict";
	public static final String WORD_DICT_FILENAME = "word.dict";
	
	public static final boolean DELETE_TEMP_FILES = true;
	
	public static String BASE_PATH = "/cs276/";
//	public static String BASE_PATH = "/afs/.ir.stanford.edu/users/c/o/conradr/";

	public static enum IndexType {
		 NO_COMPRESSION, VAR_BYTE_GAP_ENCODING, GAMMA_BIT_ENODED, TERM_FREQ;  //; is optional
	}
}
