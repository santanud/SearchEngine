package cs276.programming.spellchecker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import cs276.programming.util.Parameters;

/**
 * Main method to perform the spell-checker
 * operations.
 *
 */
public class SpellCheckerClient {

	private static final String SPELL_CHECKER_MODEL_FILENAME = "SpellChecker.model";
	
	private String corpusDir = "/cs276/PA2/data/corpus";
	private String misspelledQueriesFile = "/cs276/PA2/data/edit1s.txt";
	private String edit1sFile = "";
	private ChannelModelType modelType = ChannelModelType.UNIFORM;
	private boolean isBuildMode = false;
	private boolean isInteractiveMode = false;
	
	/**
	 * Main program to build the spell checked model as well as to 
	 * run the corrector.
	 * 
	 * ./buildmodels.sh [extra] <training corpus dir> <training edit1s file>
	 * ./corrector.sh <uniform | empirical | extra > <queries file>
	 * 
	 * @param args The first argument determines if it is invoked in build
	 * or corrector mode. The remaining arguments are as listed above.
	 */
	public static void main(String[] args) {

		SpellCheckerClient scc = new SpellCheckerClient();
		scc.exec(args);
	}
	
	private void exec(String[] args) {

		parseArgs(args);
		
		if(isBuildMode) {
			//build [extra] <training corpus dir> <training edit1s file>
			SpellCheckerModel SPModel = new SpellCheckerModel(modelType); //TODO parameter only for "Extra"

			System.err.println("Starting training ...");
			SPModel.train(corpusDir, edit1sFile);
			System.err.println("Training Complete");
			String modelFile = (new File(SPELL_CHECKER_MODEL_FILENAME)).getAbsolutePath();
			SpellCheckerModel.SaveToDisk(SPModel, modelFile);
		} else if(isInteractiveMode) {
			runInteractiveMode();
			
		} else { //Run Query Mode
			//correct <uniform | empirical | extra > <queries file>
			SpellCheckerModel SPModel = SpellCheckerModel.ReadFromDisk(SPELL_CHECKER_MODEL_FILENAME);

			//Set Model Type
			SPModel.setChannelModelType(modelType);
			//Set tuned parameters
			switch(modelType) {
			case UNIFORM:
				SPModel.setLangModelLambda(0.8);
				SPModel.setChannelModelMu(0.05);
				SPModel.setCostPerEdit(0.01);
				break;
				
			case EMPIRICAL:
				SPModel.setLangModelLambda(1.6);
				SPModel.setChannelModelMu(0.05);
				
				SPModel.setProbabilityRawQueryCorrect(0.90);
				SPModel.setProbabilityOfCharNoEdits(0.95);
				SPModel.setUseDefaultNoEditProbability(false);
				break;
				
			case EXTRA:
				SPModel.setLangModelLambda(0.1);
				SPModel.setChannelModelMu(0.80);
				
				SPModel.setProbabilityRawQueryCorrect(0.90);
				SPModel.setProbabilityOfCharNoEdits(0.95);
				SPModel.setUseDefaultNoEditProbability(false);
				break;
			}
			String queryLine = null;
			BufferedReader queryBr = null;

			try {
				queryBr = new BufferedReader(new FileReader(misspelledQueriesFile));
				
				while (((queryLine = queryBr.readLine()) != null)) {
					String correctedQuery = SPModel.predict(queryLine.trim());
					System.out.println(correctedQuery);
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if(queryBr != null) queryBr.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
	}

	private void runInteractiveMode() {
		SpellCheckerModel SPModel = new SpellCheckerModel(ChannelModelType.UNIFORM);

		System.err.println("Starting training ...");
		SPModel.train(corpusDir, misspelledQueriesFile);
		System.err.println("Training Complete");

		Parameters.DEBUG = true;

		String RawQuery = "";
		String CorrectedQuery = "";
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		while (true) {
			// System.out.println("Please enter a query ( press enter to exit) : ");
			try {
				RawQuery = br.readLine();
				if (RawQuery == null || RawQuery == "" || RawQuery.toString().length() == 0) {
					// System.out.println("Program ends. Bye !");
					break;
				} else {
					long start_time = System.currentTimeMillis();

					CorrectedQuery = SPModel.predict(RawQuery);
					long end_time = System.currentTimeMillis();
					long difference = end_time - start_time;
					if (Parameters.DEBUG == true) {
						System.out.println("Time taken in milli sec : " + difference);
						System.out.println("Original Query : " + RawQuery);
						System.out.println("Best corrections : " + CorrectedQuery);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void parseArgs(String[] args) {
		
		if(args.length < 3) {
			showUsage();
			System.exit(-1);
		}

		int pos = 0;
		if("build".equalsIgnoreCase(args[pos])) {
			isBuildMode = true;
			pos++;
			if("extra".equalsIgnoreCase(args[pos])) {
				pos++;
				modelType = ChannelModelType.EXTRA;
			}
			corpusDir = args[pos++];
			checkFile(corpusDir, "training corpus dir");
			if(pos >= args.length) {
				System.err.println("Training edit1s file location not specified.\n\n");
				showUsage();
				System.exit(-1);
			}
			edit1sFile = args[pos];
			checkFile(edit1sFile, "training edit1s file");
		} else if("correct".equalsIgnoreCase(args[pos])) {
			pos++;
			modelType = ChannelModelType.valueOf(args[pos++].toUpperCase());
			misspelledQueriesFile = args[pos];
			checkFile(misspelledQueriesFile, "queries file");
			checkFile(SPELL_CHECKER_MODEL_FILENAME, "SpellChecker file");
		} else if("interactive".equalsIgnoreCase(args[pos])) { //Test mode
			pos++;
			modelType = ChannelModelType.valueOf(args[pos++].toUpperCase());
		} else {
			showUsage();
			System.exit(-1);
		}
	}
	
	private void checkFile(String filename, String fileDesc) {
		if(!(new File(filename)).canRead()) {
			System.err.printf("Could not read the %s (\"%s\"). Please check path and try again.\n\n", fileDesc, filename);
			showUsage();
			System.exit(-1);
		}
	}

	private static void showUsage() {
		System.err.println("Invalid command line arguments specified.\n");
		System.err.println("Build mode - ");
//		System.err.println(SpellCheckerClient.class.getCanonicalName() + " build [extra] <training corpus dir> <training edit1s file>");
		System.err.println("buildmodels.sh [extra] <training corpus dir> <training edit1s file>");
		System.err.println("\nRun Query mode - ");
//		System.err.println(SpellCheckerClient.class.getCanonicalName() + " correct <uniform | empirical | extra > <queries file>");
		System.err.println("runcorrector.sh <uniform | empirical | extra > <queries file>");
	}

}
