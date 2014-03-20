package cs276.programming.spellchecker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cs276.programming.util.Parameters;

/**
 * Class to tune the various tunable parameters
 * of the spellchecker.
 *
 */
public class TuneParameters {

	//Since there are only 455 queries, we can keep this in memory and save on Disk IO time
	private static List<String> rawQuery = new ArrayList<String>();  
	private static List<String> goldQuery = new ArrayList<String>();
	
	private static String finalSummary = "";
	private static boolean showRunQuerySummary = false;
	private static ChannelModelType modelType;

	public static void main(String[] args) {

		Parameters.DEBUG = false;

		String CorpusDir = "/cs276/PA2/data/corpus";
		String MisspelledQueriesFile = "/cs276/PA2/data/edit1s.txt";
		String QueryFile = "/cs276/PA2/data/queries.txt";
		String GoldCopyFile = "/cs276/PA2/data/gold.txt";
		String mType = "";
		
		if(args.length == 5) {
			mType = args[0];
			CorpusDir = args[1];
			MisspelledQueriesFile = args[2];
			QueryFile = args[3];
			GoldCopyFile = args[4];
		}
		
		if(args.length >= 1) {
			if(args[0].equalsIgnoreCase("empirical")) {
				exec(CorpusDir, MisspelledQueriesFile, QueryFile, GoldCopyFile, ChannelModelType.EMPIRICAL);
			}
			if(args[0].equalsIgnoreCase("uniform")) {
				exec(CorpusDir, MisspelledQueriesFile, QueryFile, GoldCopyFile, ChannelModelType.UNIFORM);
			}
			if(args[0].equalsIgnoreCase("extra")) {
				exec(CorpusDir, MisspelledQueriesFile, QueryFile, GoldCopyFile, ChannelModelType.EXTRA);
			}
		} else {
			exec(CorpusDir, MisspelledQueriesFile, QueryFile, GoldCopyFile, ChannelModelType.EMPIRICAL);
			exec(CorpusDir, MisspelledQueriesFile, QueryFile, GoldCopyFile, ChannelModelType.UNIFORM);
			exec(CorpusDir, MisspelledQueriesFile, QueryFile, GoldCopyFile, ChannelModelType.EXTRA);
		}
	}

	private static void exec(String CorpusDir, String MisspelledQueriesFile, String QueryFilename, String GoldCopyFilename, ChannelModelType modelType) {
		System.err.println("Model Type: " + modelType);
		System.err.println();
		
		SpellCheckerModel SPModel = new SpellCheckerModel();//modelType);
		SPModel.setCostPerEdit(0.10);

		System.err.println("Starting training ...");
		SPModel.train(CorpusDir, MisspelledQueriesFile);
		System.err.println("Training Complete");

		File QueryFile = new File(QueryFilename);
		File GoldCopy = new File(GoldCopyFilename);
		TuneParameters.tune(SPModel, modelType, QueryFile, GoldCopy);
	}

	public static void tune(SpellCheckerModel SPModel, ChannelModelType modelType, File QueryFile, File GoldCopy) {

		loadQueryFile(QueryFile, GoldCopy);
		
		double maxSuccess = 0;//tuneMuLambda(SPModel);
		TuneParameters.modelType = modelType;
		
		if(modelType == ChannelModelType.EMPIRICAL) {
			maxSuccess = tuneMuLambda(SPModel);
			//Tune ErrorProbabilityModel
			tuneEmpiricalModel(SPModel, maxSuccess);
		} else if(modelType == ChannelModelType.UNIFORM) {
			//tune ChannelModel.COST_PER_EDIT
			tuneUniformModel(SPModel, maxSuccess);
		} else if(modelType == ChannelModelType.EXTRA) {
			maxSuccess = tuneMuLambda(SPModel);
			tuneEmpiricalModel(SPModel, maxSuccess);
//			tuneUniformModel(SPModel, maxSuccess);
		}
		
		System.err.println("\n\nFinal Summary - \n" + finalSummary);
		System.err.println();
		showRunQuerySummary = true;
		double d = RunQueryFile(SPModel);
		System.err.println(d);
		showRunQuerySummary = false;
	}

	private static void tuneUniformModel(SpellCheckerModel SPModel, double maxSuccess) {
		double d;
		double bestcpe = 0.01;
//		System.err.printf("COST_PER_EDIT\td\n");
		for(double cpe = 0.01; cpe <= 0.10; cpe += 0.02) {
			SPModel.setCostPerEdit(cpe);
			System.err.printf("COST_PER_EDIT: %f\n", cpe);
			d = tuneMuLambda(SPModel);
//			d = RunQueryFile(SPModel);
			System.err.printf("%f\t%f\n", cpe, d);
			if (d > maxSuccess ) {
				maxSuccess = d;
				bestcpe = cpe;
			}
		}
		SPModel.setCostPerEdit(bestcpe);
		System.err.println("Best Value for ChannelModel.COST_PER_EDIT: " + bestcpe);
		System.err.println("Max rate    : " + maxSuccess);
		
		finalSummary += "\nBest value for the probability of an edit needed for a character (Uniform Model): " + bestcpe + "\n";
	}

	private static void tuneEmpiricalModel(SpellCheckerModel SPModel, double maxSuccess) {
		
		finalSummary += "\nFor the Empirical Model - \n";
		double d;
		System.err.println("Tuning the Empirical Model");
		SPModel.setUseDefaultNoEditProbability(true);
		double bestNep = 0.90;
		double bestNchp = 0.90;

		System.err.println("x-axis no change probability. y-axis no edit probability");
		for(double nchp = 0.90; nchp < 0.97; nchp += 0.01) {
			System.err.printf("\t %.3f", nchp);
		}
		System.err.println();
		
		for(double nep = 0.90; nep < 0.99; nep += 0.01) {
			System.err.printf("%.3f\t", nep);
			for(double nchp = 0.90; nchp < 0.97; nchp += 0.01) {
				SPModel.setProbabilityRawQueryCorrect(nchp);
				SPModel.setProbabilityOfCharNoEdits(nep);
				d = RunQueryFile(SPModel);
				System.err.printf("%.3f\t", d);
				if (d > maxSuccess ) {
					maxSuccess = d;
					bestNep = nep;
					bestNchp = nchp;
				}
			}
			System.err.println();
		}
		SPModel.setProbabilityRawQueryCorrect(bestNchp);
		SPModel.setProbabilityOfCharNoEdits(bestNep);

		System.err.println();
		System.err.println("Best Value for ErrorProbabilityModel.PROBABILITY_NO_EDITS: " + bestNep);
		System.err.println("Best Value for ChannelModel.EMPIRICAL_QUERY_NO_CHG_PROBABILITY: " + bestNchp);
		System.err.println("Max rate    : " + maxSuccess);
		System.err.println();

		SPModel.setUseDefaultNoEditProbability(false);
		d = RunQueryFile(SPModel);
		System.err.printf("useDefaultNoEditProbability: false; d: %f\n", d);
		System.err.println();
		if (d > maxSuccess ) {
			maxSuccess = d;
			System.err.println("ErrorProbabilityModel.useDefaultNoEditProbability should be false.");
			finalSummary += "Use the computed value for the no-edit probability.\n";
			System.err.println("Max rate    : " + maxSuccess);
			SPModel.setUseDefaultNoEditProbability(false);
		} else {
			System.err.println("ErrorProbabilityModel.useDefaultNoEditProbability should be true.");
			finalSummary += "Use a constant value for the no-edit probability - " + bestNep + "\n";
			SPModel.setUseDefaultNoEditProbability(true);
		}
		finalSummary += "Use " + bestNchp + " for the query no change probability.\n"; 
	}

	private static double tuneMuLambda(SpellCheckerModel SPModel) {

		double d;
		double maxSuccess = 0;
		double Best_CHmu = 0;
		double Best_LMlambda = 0;
		
		double[] CHmu = {0.6, 0.7, 0.8, 0.9, 1.0, 1.2, 1.4, 1.6};//{ 0.8 , 1.0 , 1.6 };
		double[] LMlambda = { 0.05, 0.1, 0.2, 0.3 };
		
		if(TuneParameters.modelType == ChannelModelType.UNIFORM) {
			CHmu = new double[]{0.4, 0.5, 0.6, 0.8, 1.0, 1.2};//{ 0.8 , 1.0 , 1.6 };
			LMlambda = new double[]{ 0.05, 0.1, 0.2, 0.3 };
		} else if(TuneParameters.modelType == ChannelModelType.EXTRA) {
			CHmu = new double[]{0.4, 0.5, 0.6, 0.8, 1.0, 1.2};
			LMlambda = new double[]{ 0.1};
		}

		System.err.println("\nTuning for mu and lambda.");
		
		System.err.println("x-axis ch mu. y-axis lm lambda");
		for (int j = 0; j < CHmu.length; j++) {
			System.err.printf("\t %.3f", CHmu[j]);
		}
		System.err.println();
		
		for (int i = 0; i < LMlambda.length; i++) {
			System.err.printf("%.3f\t", LMlambda[i]);
			SPModel.setLangModelLambda(LMlambda[i]);
			for (int j = 0; j < CHmu.length; j++) {
				SPModel.setChannelModelMu(CHmu[j]);
				d = RunQueryFile(SPModel);
				System.err.printf("%.3f\t", d);

				if (d > maxSuccess ) {
					maxSuccess = d;
					Best_CHmu = CHmu[j];
					Best_LMlambda = LMlambda[i];
				}
			}
			System.err.println();
		}
		
//		Best_CHmu = 1.6;
//		Best_LMlambda = 0.05;
		SPModel.setLangModelLambda(Best_LMlambda);
		SPModel.setChannelModelMu(Best_CHmu);
		
		System.err.println();
		System.err.println("CH Mu   : " + Best_CHmu);
		System.err.println("LM Lambda   : " + Best_LMlambda);
		System.err.println("Max rate    : " + maxSuccess);
		System.err.println();
		
		finalSummary += "\nUse " + Best_CHmu + " for mu and " + Best_LMlambda + " for lambda.\n";
		return maxSuccess;
	}

	private static double RunQueryFile(SpellCheckerModel SPModel) {

		String strLineQ = "";
		String strLineGold = "";
		String CorrectedQuery = "";

		int QueryCount = 0;
		int ErrorCount = 0;
		int NoChangeCount = 0;
		int CandGeneratedCount = 0;
		long start_time = System.currentTimeMillis();
		int unMatchedSample = 0;

		for(int j = 0; j < rawQuery.size(); j++) {
			strLineQ = rawQuery.get(j);
			strLineGold = goldQuery.get(j);

			QueryCount++;
			CorrectedQuery = SPModel.predict(strLineQ);

			if (strLineGold.equals(strLineQ)) {
				NoChangeCount++;
			}

			if (!CorrectedQuery.equals(strLineGold)) {
				ErrorCount++;
				if (showRunQuerySummary) {
//					System.err.println("query     : " + strLineQ);
//					System.err.println("correct   : " + strLineGold);
//					System.err.println("suggested : " + CorrectedQuery);
				}
				unMatchedSample++;

				if (unMatchedSample <= 10 && Parameters.DEBUG) {
					System.err.println("query   : " + strLineQ);
					System.err.println("correct : " + strLineGold);
				}

				String[] candidates = SPModel.generateCandidate(strLineQ, 1);
				boolean hasMatched = false;
				for (int i = 0; i < candidates.length; i++) {
					if (CorrectedQuery.equals(candidates[i])) {
						hasMatched = true;
					}
				}
				if (!hasMatched && Parameters.DEBUG) {
					System.err.println("  query : "+strLineQ);
					System.err.println("correct : "+strLineGold);
					CandGeneratedCount++;
				}
				// System.err.println("query : "+strLineQ);
			}
		}
		long end_time = System.currentTimeMillis();
		if (showRunQuerySummary) {

			System.err.println("Total Query : " + QueryCount);
			System.err.println("Correct : " + (QueryCount - ErrorCount));
			System.err.println("Error : " + ErrorCount);
			System.err.println("Correct % : "
					+ (double) (QueryCount - ErrorCount) * 100
					/ (double) QueryCount);
			System.err.println("Same query : " + NoChangeCount);
			System.err.println("Queries with no candidate generated : " + CandGeneratedCount);

			long difference = end_time - start_time;
			System.err.println("Time taken in milli sec : " + difference);
		}
		return ((double) (QueryCount - ErrorCount) * 100.0/ ((double) QueryCount));
	}

	private static void loadQueryFile(File queryFile, File goldCopy) {

		String queryLine = null;
		String goldLine  = null;
		BufferedReader queryBr = null;
		BufferedReader goldBr = null;
		rawQuery = new ArrayList<String>();  
		goldQuery = new ArrayList<String>();

		try {
			queryBr = new BufferedReader(new FileReader(queryFile));
			goldBr = new BufferedReader(new FileReader(goldCopy));
			
			while (((queryLine = queryBr.readLine()) != null)
					&& ((goldLine = goldBr.readLine()) != null)) {
				rawQuery.add(queryLine.trim());
				goldQuery.add(goldLine.trim());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if(queryBr != null) queryBr.close();
				if(goldBr != null) goldBr.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
