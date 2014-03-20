package cs276.programming.query;

import java.io.File;
import java.util.ArrayList;

import cs276.programming.index.PostingListFactory;
import cs276.programming.index.PostingReader;
import cs276.programming.util.Parameters;

public class RuntimeIndex {

	/**
	 * @param args
	 */
	private static RuntimeIndex instance = null;
	private WordDict word;
	private PostingDict posting;
	private DocDict doc;
	private PostingReader postingReader;

	public static RuntimeIndex getInstance(String IndexDir, Parameters.IndexType iType) {
		if (instance == null) {
			instance = new RuntimeIndex(IndexDir,iType);
		}
		return instance;
	}

	protected RuntimeIndex(String IndexDir,Parameters.IndexType IType) {
		if (Parameters.DEBUG) {
			System.err.println("Indexing starts ... ");
		}
		String WordDictFile = IndexDir + "/"+Parameters.WORD_DICT_FILENAME;//word.dict
		String DocDictFile = IndexDir + "/"+ Parameters.DOC_DICT_FILENAME ; //doc.dict
		String PostingDictFile = IndexDir + "/"+ Parameters.POSTING_DICT_FILENAME; //posting.dict
		String CorpusIndexFile = IndexDir + "/"+ Parameters.CORPUS_INDEX_FILENAME;//corpus.index

		word = new WordDict(WordDictFile);
		posting = new PostingDict(PostingDictFile);
		doc = new DocDict(DocDictFile);
		File posingListFile = new File(CorpusIndexFile);
		
		postingReader = PostingListFactory.getPLManager(IType).getReader(posingListFile);
		if (Parameters.DEBUG) {
			System.err.println("Indexing ends ... ");
		}

	}

	public DocDict getDocDict() {
		return doc;
	}
	public PostingDict getPostingDict() {
		return posting;
	}
	public WordDict getWordDict() {
		return word;
	}
	ArrayList<Integer> getPostingList(int TermId) {

		// ArrayList<Integer> li = new ArrayList<Integer>();
		// get Termid WordDicts
		
		if (Parameters.DEBUG) {
			System.err.println(" ************************");
			System.err.println(" termid :" + TermId);
		}
		// get file position from PostingDict
		long FilePosition = PostingDict.getFilePosition(TermId);
		if (Parameters.DEBUG) {
			//System.err.println(this.getClass().getName());
			System.err.println(" file position :" + FilePosition);
		}
		// get the docid list from CorpusIndex
		
		ArrayList<Integer> li = postingReader.getDocidList(PostingDict.getTermStat(TermId));
		
		
		if (Parameters.DEBUG) {
			System.err.println("Posting list for  :" + TermId);
			for(Integer x : li){
				System.err.println("docid : " + x);
			}
		}
		return li;

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
