package cs276.programming.ranking;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class to parse through the corpus to determine the
 * document frequency (df) for each term in the AllQueries list
 * within the corpus files.
 */
public class DocFreqCalculator {

	private static final PorterStemmer stemmer = new PorterStemmer();
	
	/**
	 * Compute the document frequency by parsing the corpus files.
	 */
	public int calculateDf(File allQueriesFile, File rootFolder, Map<String, Integer> dfMap) {
		
		int numFiles = 0;
		readAllQueries(allQueriesFile, dfMap);

		if(rootFolder.isDirectory()) { //just to be sure
			for(File fileBlock : rootFolder.listFiles()) {
				if(fileBlock.isDirectory()) { //to ignore system & hidden files
					int blockFileCnt = processBlock(fileBlock, dfMap);
					numFiles += blockFileCnt;
				}
			}
		} else {
			System.err.printf("Invalid input provided. '%s' is not a directory.", rootFolder);
		}

		return numFiles;
	}

	/**
	 * Retrieve the list of all the query terms from the AllQueries file.
	 */
	private Map<String, Integer> readAllQueries(File allQueriesFile, Map<String, Integer> dfMap) {

		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(allQueriesFile));
			String line = null;
			while ((line = br.readLine()) != null) {
				String token = line.trim();
				token = TermFreqUtil.enableStemming ? stemmer.stem(token) : token;
				dfMap.put(token, 0);
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
		return dfMap;
	}
	
	/**
	 * Process a block.
	 * 
	 * @param fileBlock The folder location of this block.
	 * @param dfMap the document frequency map to be updated. 
	 */
	private int processBlock(File fileBlock, final Map<String, Integer> dfMap) {

		int fileCount = 0;
		ExecutorService execSvc = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		for(File file : fileBlock.listFiles()) {
			if(file.isHidden()) continue;
			fileCount++;
			final File currFile = file;
			execSvc.execute(new Runnable() {

				@Override
				public void run() {
					Set<String> foundTerms = parseInputFile(currFile, dfMap.keySet());
					for(String term : foundTerms) {
						term = TermFreqUtil.enableStemming ? stemmer.stem(term) : term;
						dfMap.put(term, dfMap.get(term) + 1);
					}
				}
			});
		}
		shutdownAndAwaitCompletion(execSvc);
		return fileCount;
	}

	/**
	 * Parse the input file to read all the tokens in it and add
	 * them to the current block's term-doc list.
	 * 
	 * @param file The file to be parsed.
	 * @param docID The docid for the current file being parsed.
	 * @param dfMap The term-doc list for the current block.
	 * @return 
	 */
	private Set<String> parseInputFile(File file, Set<String> allQueries) {
		
		Set<String> foundTerms = new HashSet<String>();

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
				String term = cs.toString().toLowerCase().trim();
				term = TermFreqUtil.enableStemming ? stemmer.stem(term) : term;
				if(allQueries.contains(term)) {
					foundTerms.add(term);
				}
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
		
		return foundTerms;
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
	
	public static void main(String[] args) {
		
		TermFreqUtil.enableStemming = false;
		
		Map<String, Integer> dfMap = Collections.synchronizedMap(new HashMap<String, Integer>());

		DocFreqCalculator dfc = new DocFreqCalculator();
		int numDocs = dfc.calculateDf(new File("/cs276/PA3/starter/AllQueryTerms"), new File("/cs276/PA1/data"), dfMap);
		System.err.println(dfMap);
		String maxdfTerm = null;
		int maxdf = -1;
		for(String term : dfMap.keySet()) {
			int termdf = dfMap.get(term);
			if(termdf > maxdf) {
				maxdf = termdf;
				maxdfTerm = term;
			}
		}
		System.err.printf("Max df is %d for term '%s'.Total files read = %d\n\n", maxdf, maxdfTerm, numDocs);
	}
}
