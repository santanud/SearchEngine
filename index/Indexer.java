package cs276.programming.index;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cs276.programming.query.PostingDict;
import cs276.programming.util.Parameters;
import cs276.programming.util.TermStat;

/**
 * Indexer that processes the input corpus data and
 * generates an index. 
 *
 */
public class Indexer {

	private static final boolean DELETE_TEMP_FILES = true;
	private static final boolean DEBUG = false;
	private static final boolean RUN_TEST_ON_OUTPUT = false;
	
	//Maps a file path to its doc ID.
	private Dictionary docIdDict = new Dictionary();
	
	//Maps a term/word to its term ID.
	private Dictionary wordDict = new Dictionary();
	
	private PostingListManager postingListHelper = PostingListFactory.getSimplePostingListManager();
	
	/**
	 * @param args Command line arguments -
	 * 			input data dir: a string valued argument, giving the location of the input corpus data
	 * 			output index dir: a string valued argument. This is the location of the output directory containing the generated index. 
	 * 								Assumes that the output directory does not exist.
	 *          postings list encoding: the encoding format for the postings list.
	 */
	public static void main(String[] args) {
		/*
		 * 
			for each directory 
			   create a *file* in output for this directory	- block_pl
			   for each file in directory
			      fileCount++
			      add file to doc_id_dict	- doc_id_dict[file_id] = doc_id
			      read tokens in file
			      -> add tokens to word_dict	- word_dict[token] = word_id
			      -> add to term to doc list	- term_doc_list.append( (word_dict[token], doc_id) )
			   end for each file in directory
			
			   sort term doc list 
			
			   write the posting lists to block_pl for this current block (termid to list of docID)
			
			end for each directory 
			
			take the block posting list
			-> merge the posting lists in pairs
			
			====== format of the output
			doc.dict
			Y -> doc name \t doc id
			
			word.dict
			Y -> term \t term id
			
			posting dict
			Y -> term id \t filepos \t doc frequency
			
			corpus index (simple postings list)
			Y -> term id \t doc id,É
			=======

		 */
		
		Indexer indexer = new Indexer();
		indexer.parseArgs(args);
		
		long start = System.nanoTime();
		indexer.generateIndex(args[0], args[1]);
		indexer.cleanup();
		indexer.displayFileCount();
		long end = System.nanoTime();
		long elapsed = end - start;
		showElapsed(elapsed, "");
	}

	private void parseArgs(String[] args) {
		if(args.length < 2) {
			System.err.println("Usage: " + Indexer.class.getCanonicalName() + " <input folder> <output folder>");
			System.exit(1);
		}
		
		if(args.length == 3) { //switch to the appropriate format of the posting list
			int option = 0;
			
			String errMsg = "The third parameter must be numeric - \n\t1. SimplePostingList\n\t2. GapVarLenEncodedPostingList\n\t3. Gamma Encoded Posting List";
			try {
				option = Integer.parseInt(args[2]);
			} catch (NumberFormatException e) {
				e.printStackTrace();
				System.err.println(errMsg);
				System.exit(1);
			}
			
			if(option < 1 || option > 4) {
				System.err.println(errMsg);
				System.exit(1);
			}
			switch(option) {
			case 1:
				postingListHelper = PostingListFactory.getSimplePostingListManager();
				break;
			case 2:
				postingListHelper = PostingListFactory.getGapVarEncodedPostingListManager();
				break;

			case 3:
				postingListHelper = PostingListFactory.getGammaEncodedPostingListManager();
				break;
				
			case 4:
				postingListHelper = PostingListFactory.getTermFreqPostingListManager();
				break;
				
			case 9:
				postingListHelper = PostingListFactory.getGapEncodedPostingListManager();
				break;
			}
			
			if(DEBUG) {
				System.err.println(Indexer.class.getCanonicalName() + " " + args[0] + " " + args[1]);
//				System.err.println("Using - " + postingListHelper.getClass().getCanonicalName());
				System.err.println();
			}
			
		}
	}

	/** 
	 * Display the total number of files processed by the indexer.
	 */
	private void displayFileCount() {
		System.out.println(docIdDict.size());
	}

	/**
	 * Debug function to display the total time elapsed.
	 * 
	 * @param elapsed Elapsed time in nano seconds.
	 * 
	 * @param prefix A debug prefix to print with the elapsed duration.
	 */
	private static void showElapsed(long elapsed, String prefix) {

//		if(DEBUG) {
			long sec = elapsed/1000000000L;
			long min = sec / 60;
			sec -= min * 60;
			System.err.printf("%s%d min %d sec %d ms\n", prefix, min, sec, (elapsed - sec * 1000000000L)/1000000);
//		}
	}

	/**
	 * Placeholder method to do any resource cleanups.
	 */
	private void cleanup() {
		//placeholder for any resource cleanups required.
	}

	/**
	 * Parse the files in the input blocks and generate the reverse index.
	 * 
	 * @param inputDir The folder location where all the input blocks are present.
	 * @param outputDir The folder location where the index needs to be created.
	 */
	private void generateIndex(String inputDir, String outputDir) {
		
		File rootFolder = new File(inputDir);
		File outDir = new File(outputDir);
		outDir.mkdirs();
		Queue<File> bplQueue = new LinkedList<File>();
		
		if(rootFolder.isDirectory()) { //just to be sure
			for(File fileBlock : rootFolder.listFiles()) {
				if(fileBlock.isDirectory()) { //to ignore system & hidden files
					processBlock(fileBlock, outDir, bplQueue);
				}
			}
			
			saveDocDict(outDir); //store the document dictionary
			saveTermDict(outDir); //store the word dictionary
			/*File corpusIndex =*/ mergePostingLists(bplQueue);
			testOutput(rootFolder, outDir);
		} else {
			System.err.printf("Invalid input provided. '%s' is not a directory.", inputDir);
		}

	}

	/**
	 * Simple sanity test to ensure that the generated index has the right data.
	 *  
	 * @param rootFolder the input folder.
	 * @param outDir the output folder.
	 */
	private void testOutput(File rootFolder, File outDir) {

		if(!RUN_TEST_ON_OUTPUT) return;
		
		int failCount = 0;
		
		PostingDict postingDict = new PostingDict((new File(outDir, Parameters.POSTING_DICT_FILENAME)).getAbsolutePath());
		PostingReader reader = postingListHelper.getReader(new File(outDir, Parameters.CORPUS_INDEX_FILENAME));
		
		for(File fileBlock : rootFolder.listFiles()) {
			if(fileBlock.isDirectory()) { //to ignore system & hidden files
				for(File file : fileBlock.listFiles()) {
					if(file.isHidden()) continue;
					int docID = docIdDict.getID(getBlockFilename(file));
					BufferedReader br = null;
					try {
						br = new BufferedReader(new FileReader(file));
						String line;
						int lineCount = 0;
						while ((line = br.readLine()) != null && lineCount < 10) {
							lineCount++; //limit only to first 10 lines, so that it can be run on big corpus too, if needed.
						   String[] tokens = line.trim().split(" +");
						   for(String term : tokens) {
							   int termID = getTermID(term);
							   TermStat termStat = postingDict.getTermStat(termID);
							   ArrayList<Integer> docidList = reader.getDocidList(termStat);
							if(!docidList.contains(docID)) {
								   failCount++;
								   System.err.printf("Condition failed for \"%s\" (term id: %d) in file %s (doc id: %d). %s\n",
										   term, termID, file.getAbsoluteFile(), docID, docidList);
							   }
						   }
						}
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						try {
							if(br != null) br.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}

				}
			}
		}
		
		if(failCount == 0) {
			System.err.println("Passed all test conditions.");
		} else {
			System.err.printf("Failed %d test Conditions.\n", failCount);
		}
	}

	/** 
	 * Generate the posting dict.
	 * 
	 * @param corpusIndex The location of the corpus index.
	 * @param outDir The folder where the posting dict is to be created.
	 */
	private void generatePostingDict(File corpusIndex, File outDir) {
		PostingReader pr = postingListHelper.getParser();
		RandomAccessFile raf = null;
		PrintWriter pw = null;
		try {
			raf = new RandomAccessFile(corpusIndex, "r");
			pw = new PrintWriter(new File(outDir, Parameters.POSTING_DICT_FILENAME));
			String line = null;
			while(true) {
				long pos = raf.getFilePointer();
				line = raf.readLine();
				if(line == null) break;
				TermStat pl = pr.getPostingLineStats(line);
				pw.println(pl.getTermId() + "\t" + pos + "\t" + pl.getFreq());

			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if(raf != null) raf.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if(pw != null) pw.close();
		}
	}

	/**
	 * Merge all the block-level postings lists into a single
	 * file - and in the process create the corpus index.
	 * 
	 * @param bplQueue A queue containing all the generated block-level postings lists.
	 * @return
	 */
	private File mergePostingLists(final Queue<File> bplQueue) {
/*
	//Put this in a separate class in a producer-consumer model.
		BlockingQueue<File> bpQ = new ArrayBlockingQueue<File>(bplQueue.size());
		for(File f : bplQueue) {
			bpQ.add(f);
		}
		ExecutorService execSvc = Executors.newFixedThreadPool(3);
		CompletionService<File> compSvc = new ExecutorCompletionService<File>(execSvc);
		*/
		do {
		ExecutorService execSvc = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		while(bplQueue.size() > 1) {
			final File file1 = bplQueue.remove();
			final File file2 = bplQueue.remove();
			if(DEBUG) System.err.printf("Executer merging %s & %s\n", file1.getName(), file2.getName());
			execSvc.execute(new Runnable() {
	
				@Override
				public void run() {
					if(DEBUG) System.err.printf("++Executer merging %s & %s\n", file1.getName(), file2.getName());
					File merged = mergePostingList(file1, file2);
					if(DEBUG) System.err.printf("--Executer merging %s & %s\n", file1.getName(), file2.getName());
					bplQueue.add(merged);
				}
				
			});
		}
		if(DEBUG) System.err.println("=== Awaiting shutdown\n");
		shutdownAndAwaitCompletion(execSvc);
		} while(bplQueue.size() > 1); //a quick hack, since this is both a producer and a consumer. Look into Future.
		

		while(bplQueue.size() > 1) { //it should not reach here
			File file1 = bplQueue.remove();
			File file2 = bplQueue.remove();
			File merged = mergePostingList(file1, file2);
			if(DEBUG) System.err.printf("Regular loop merging %s & %s\n", file1.getName(), file2.getName());
			bplQueue.add(merged);
		}
		File merged = bplQueue.remove();
		File corpusIndex = new File(merged.getParentFile(), Parameters.CORPUS_INDEX_FILENAME);
		merged.renameTo(corpusIndex);
		File postdict = new File(merged.getParentFile(), merged.getName() + ".pos");
		if(postdict.exists()) {
			try {
				postdict.renameTo(new File(merged.getParentFile(), Parameters.POSTING_DICT_FILENAME));
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			generatePostingDict(corpusIndex, merged.getParentFile());
		}
		return corpusIndex;
	}

	/**
	 * Based on shutdownAndAwaitTermination described in the ExecutorService Java documentation.
	 * 
	 * http://docs.oracle.com/javase/6/docs/api/index.html?java/util/concurrent/ExecutorService.html
	 * @param pool
	 */
	static void shutdownAndAwaitCompletion(ExecutorService pool) {
		pool.shutdown(); // Disable new tasks from being submitted
		try {
			while(!pool.awaitTermination(30, TimeUnit.SECONDS));
		} catch (InterruptedException ie) {
			// Cancel if current thread is interrupted
			pool.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * Merge two files containing block-level postings lists.
	 * 
	 * @param file1 The first postings list file.
	 * @param file2 The second  postings list file.
	 * 
	 * @return Returns the location of the merged postings list file.
	 */
	private File mergePostingList(File file1, File file2) {
		File merged = new File(file1.getParent(), file1.getName() + '+' + file2.getName());
		
		PostingReader pr1 = null;
		PostingReader pr2 = null;
		PostingWriter pw = null;
		try {
			pr1 = postingListHelper.getReader(file1);
			pr2 = postingListHelper.getReader(file2);

			pw = postingListHelper.getWriter(merged);

			PostingLine pl1 = pr1.getNextPosting();
			PostingLine pl2 = pr2.getNextPosting();
			
			while (pl1 != null && pl2 != null) {
				if(pl1.getTermID() < pl2.getTermID()) {
					//write posting 1 & get next
					writePosting(pw, pl1);
					pl1 = pr1.getNextPosting();
				} else if(pl1.getTermID() > pl2.getTermID()) {
					//write posting 2 & get next
					writePosting(pw, pl2);
					pl2 = pr2.getNextPosting();
				} else { //== 
					//merge & write 
					pw.addTermID(pl1.getTermID());

					int currPl1DocId = pl1.getNextDocID();
					int currPl2DocId = pl2.getNextDocID();
					while(currPl1DocId != -1 && currPl2DocId != -1) { 
						if(currPl1DocId < currPl2DocId) {
							//write pl1Position & move ahead
							pw.addDocID(currPl1DocId);
							pw.addTermFreq(pl1.getCorrespondingTermFreq());
							currPl1DocId = pl1.getNextDocID();
						} else if(currPl1DocId > currPl2DocId) {
							//write pl2Position & move ahead
							pw.addDocID(currPl2DocId);
							pw.addTermFreq(pl2.getCorrespondingTermFreq());
							currPl2DocId = pl2.getNextDocID();
						} else {
							//write once & move both
							pw.addDocID(currPl1DocId);
							pw.addTermFreq(pl1.getCorrespondingTermFreq() + pl2.getCorrespondingTermFreq());
							currPl1DocId = pl1.getNextDocID();
							currPl2DocId = pl2.getNextDocID();
						}
					}

					//write the remaining. Note only one of the loops will be executed.
					while(currPl1DocId != -1) {
						pw.addDocID(currPl1DocId);
						pw.addTermFreq(pl1.getCorrespondingTermFreq());
						currPl1DocId = pl1.getNextDocID();
					}
					while(currPl2DocId != -1) {
						pw.addDocID(currPl2DocId);
						pw.addTermFreq(pl2.getCorrespondingTermFreq());
						currPl2DocId = pl2.getNextDocID();
					}
					pw.endPosting();
					pl1 = pr1.getNextPosting();
					pl2 = pr2.getNextPosting();
				}
			}
			//write the remaining
			while(pl1 != null) {
				writePosting(pw, pl1);
				pl1 = pr1.getNextPosting();
			}
			while(pl2 != null) {
				writePosting(pw, pl2);
				pl2 = pr2.getNextPosting();
			}
			
			return merged;
			
		} finally {
			if(pw != null) pw.close();
			if(pr1 != null) pr1.close();
			if(pr2 != null) pr2.close();
			
			if(DELETE_TEMP_FILES) {
				File pos1file = new File(file1.getParentFile(), file1.getName() + ".pos");
				File pos2file = new File(file2.getParentFile(), file2.getName() + ".pos");
				file1.delete();
				file2.delete();
				pos1file.delete();
				pos2file.delete();
			}
		}
	}

	/**
	 * Write a postings line to the output.
	 * 
	 * @param pw
	 * @param pl1
	 */
	private void writePosting(PostingWriter pw, PostingLine pl1) {
		pw.addTermID(pl1.getTermID());

		int docId = pl1.getNextDocID();
		while(docId != -1) {
			pw.addDocID(docId);
			int termfreq = pl1.getCorrespondingTermFreq();
			pw.addTermFreq(termfreq);
			docId = pl1.getNextDocID();
		}

		pw.endPosting();
	}

	/**
	 * Process a block.
	 * 
	 * @param fileBlock The folder location of this block.
	 * @param outDir The location where the output is to be stored.
	 * @param bplQueue
	 */
	private void processBlock(File fileBlock, File outDir, Queue<File> bplQueue) {
		final List<TermDoc> termDocList = Collections.synchronizedList(new ArrayList<TermDoc>()); //term-doc list for file content within this block
		ExecutorService execSvc = Executors.newFixedThreadPool(20);
		for(File file : fileBlock.listFiles()) {
			if(file.isHidden()) continue;
			final File currFile = file;
			final int docID = assignDocID(file);
//			System.err.printf("%d - %s\n", docID, file.getAbsolutePath());
			execSvc.execute(new Runnable() {

				@Override
				public void run() {
					parseInputFile(currFile, docID, termDocList);
				}
				
			});
		}
		shutdownAndAwaitCompletion(execSvc);
		//sort term-doc list - by termID followed by docID
		sortTermDocs(termDocList);
		File bplFile = new File(outDir, fileBlock.getName());
		bplQueue.add(bplFile);
		generateBlockPostingList(bplFile, termDocList);
		if(DEBUG) {
			System.err.printf("Block Stats: %s\t%d\t%d\n", fileBlock.getName(), docIdDict.size(), wordDict.size());
		}
	}

	private void sortTermDocs(List<TermDoc> termDocList) {
		Collections.sort(termDocList, new Comparator<TermDoc>() {

			@Override
			public int compare(TermDoc arg0, TermDoc arg1) {
				//term1 < term2 => <
				//term1 > term2 => >
				//term1 == term2 && doc1 < doc2 => <
				//term1 == term2 && doc1 > doc2 => >
				//term1 == tmer2 && doc1 == doc2 => =
				int termIdDiff = arg0.getTermID() - arg1.getTermID();
				if(termIdDiff == 0) {
					int docIdDiff = arg0.getDocID() - arg1.getDocID();
					return docIdDiff < 0 ? -1 : (docIdDiff > 0 ? 1 : 0);
				} else {
					return termIdDiff < 0 ? -1 : 1;
				}
			}});
	}
	
	/**
	 * Invert the list of term-docs to generate the postings list
	 * for the current block.
	 * 
	 * @param bplFile The location where the block postings list is to be stored.
	 * @param termDocList The sorted list of term-docs for the current block.
	 */
	private void generateBlockPostingList(File bplFile, List<TermDoc> termDocList) {
		//The input is a sorted term-doc list
		
		int prevTermID = -1;
		int prevDocID = -1;
		int currTermFreq = 0;
		
		PostingWriter pw = postingListHelper.getWriter(bplFile);
		
		for(TermDoc td : termDocList) {
			if(td.getTermID() != prevTermID) {
				prevTermID = td.getTermID();
				prevDocID = td.getDocID();
				pw.addTermID(prevTermID);
				pw.addDocID(td.getDocID());
				pw.addTermFreq(currTermFreq + 1); //+1 for curr entry
				currTermFreq = 0;
			} else {
				if(prevDocID != td.getDocID()) {
					prevDocID = td.getDocID();
					pw.addDocID(td.getDocID());
					pw.addTermFreq(currTermFreq + 1); //+1 for curr entry
				} else { //doc id & term ids are the same
					currTermFreq++;
				}
			}
		}
		
		pw.close();

	}

	/**
	 * Parse the input file to read all the tokens in it and add
	 * them to the current block's term-doc list.
	 * 
	 * @param file The file to be parsed.
	 * @param docID The docid for the current file being parsed.
	 * @param termDocList The term-doc list for the current block.
	 */
	private void parseInputFile(File file, int docID, List<TermDoc> termDocList) {

//		try {
//			BufferedReader br = new BufferedReader(new FileReader(file));
//			String line;
//			while ((line = br.readLine()) != null) {
//			   String[] tokens = line.trim().split(" +");
//			   for(String term : tokens) {
//				   int termID = getTermID(new String(term));
//				   termDocList.add(new TermDoc(termID, docID));
//			   }
//			}
//			br.close();
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		
//		Scanner s = null;
//
//        try {
//            s = new Scanner(new BufferedReader(new FileReader(file)));
//            s.useDelimiter(" +");
//
//            while (s.hasNext()) {
//				   int termID = getTermID(s.next());
//				   termDocList.add(new TermDoc(termID, docID));
//            }
//        } catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} finally {
//            if (s != null) {
//                s.close();
//            }
//        }
		
//		try {
//			FileChannel channel = new FileInputStream(file).getChannel();
//			   ByteBuffer buffer = ByteBuffer.allocate((int) channel.size());
//			   channel.read(buffer);
//			   buffer.asCharBuffer().
//			   channel.close();
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}

		FileInputStream fis = null;
		try {
			Charset charset = Charset.forName("ISO-8859-15");
		    CharsetDecoder decoder = charset.newDecoder();
		    Pattern pattern = Pattern.compile("[^ \n]*[ \n]+");
		    
			fis = new FileInputStream(file);
			FileChannel fc = fis.getChannel();
			int sz = (int)fc.size();
			MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, sz);
			CharBuffer cb = decoder.decode(bb);
			
			Matcher lm = pattern.matcher(cb);
			while (lm.find()) {
				CharSequence cs = lm.group();
				int termID = getTermID(cs.toString());
				termDocList.add(new TermDoc(termID, docID));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if(fis != null) fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Save the word dict to file.
	 * 
	 * @param outDir The output folder.
	 */
	private void saveTermDict(File outDir) {
		wordDict.saveDict(outDir, Parameters.WORD_DICT_FILENAME);
	}

	/**
	 * Save the doc dict to file.
	 * 
	 * @param outDir The output folder.
	 */
	private void saveDocDict(File outDir) {
		docIdDict.saveDict(outDir, Parameters.DOC_DICT_FILENAME);
	}

	/**
	 * Get the term id for the specified term. Assign a new
	 * term id if this term is not present in the dictionary.
	 * 
	 * @param term The term to be looked up.
	 * 
	 * @return The term id for the specified term.
	 */
	private synchronized int getTermID(String term) {
		return wordDict.getOrAssignID(term.trim());
	}

	/**
	 * Get the doc id for the specified document. Assign a new
	 * term id if this document is not present in the dictionary.
	 * 
	 * @param file The document to be looked up.
	 * 
	 * @return The doc id for the specified document.
	 */
	private synchronized int assignDocID(File file) {
		//note: we can either keep the doc id dict in memory & save later; or save it right away.
		return docIdDict.getOrAssignID(getBlockFilename(file));
	}

	/**
	 * Get a formatted name for the specified file.
	 * 
	 * @param file
	 * @return
	 */
	private String getBlockFilename(File file) {
		return file.getParentFile().getName() + File.separator + file.getName();
	}
}
