package cs276.programming.spellchecker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import cs276.programming.query.TextProcessor;
import cs276.programming.util.Parameters;

/**
 * Class to manage the model (both the language
 * and channel) for the spelling corrector.  
 *
 */
public class SpellCheckerModel implements java.io.Serializable {

	private static final long serialVersionUID = -2054262881709376427L;
	
	private NGramModel ngram;
	private ChannelModel CModel;
	private double LM_LAMBDA = 0.2;
	private double CH_MU = 1;
	
	//constructor
	SpellCheckerModel(ChannelModelType Ctype) {
//		System.err.println("SpellCheckerModel constructor");
		ngram = new NGramModel();
		ngram.SetLambda(LM_LAMBDA);
		CModel = new ChannelModel(Ctype);
		CModel.SetMu(CH_MU);
	}
	
	SpellCheckerModel() {
//		System.err.println("SpellCheckerModel constructor");
		ngram = new NGramModel();
		ngram.SetLambda(LM_LAMBDA);
		CModel = new ChannelModel();
		CModel.SetMu(CH_MU);
	}
	
	public void setChannelModelType(ChannelModelType type) {
		CModel.setChannelModelType(type);
		ngram.SetChannelModel(type);
	}
	
	void setLangModelLambda(double LMlambda){
	
		LM_LAMBDA=LMlambda;
		ngram.SetLambda(LM_LAMBDA);
	}
	void setChannelModelMu(double CHmu){
		
		CH_MU =CHmu;
		CModel.SetMu(CH_MU);
	}
	
	void setCostPerEdit(double cpe) {
		CModel.setCostPerEdit(cpe);
	}

	void setProbabilityRawQueryCorrect(double nchp) {
		CModel.setProbabilityRawQueryCorrect(nchp);
	}

	protected void setUseDefaultNoEditProbability(
			boolean useDefaultNoEditProbability) {
		CModel.setUseDefaultNoEditProbability(useDefaultNoEditProbability);
	}

	protected void setProbabilityOfCharNoEdits(double pROBABILITY_NO_EDITS) {
		CModel.setProbabilityOfCharNoEdits(pROBABILITY_NO_EDITS);
	}

	public void train(String CorpusDir,String MisspelledQueriesFile) {

		ngram.train(CorpusDir);
		CModel.train(MisspelledQueriesFile);
	}

	NGramModel getNGram(){
		return ngram;
	}
	
	String predict(String query) {

		String output = "";
		String ModifiedQuery = query.replaceAll("\\s+", " ").trim();

		String[] candidates = generateCandidate(ModifiedQuery, 1);

		if (Parameters.DEBUG == true) {

			System.err.println("No of caldidate generated : "
					+ candidates.length);
		}
		if (candidates.length == 0)
			output = ModifiedQuery;
		else if (candidates.length == 1) {
			output = candidates[0];
		} else {
			output = FindArgMax(candidates, ModifiedQuery);
		}
		return output;
	}

	private String FindArgMax(String[] candidates, String Query) {

		double MaxProb = 0;
		double CurProb = 0;
		int index = 0;
		for (int i = 0; i < candidates.length; i++) {
			CurProb = java.lang.Math.exp(FindNoisyChannelProb(candidates[i],
					Query)) * 1000000000;
			if (MaxProb < CurProb) {
				MaxProb = CurProb;
				index = i;
			}
		}
		return candidates[index];
	}

	private double FindNoisyChannelProb(String candidate, String Query) {

		double prob = Double.MIN_VALUE;
		String[] candiList = candidate.split("//s+");
		String[] QueryList = Query.split("//s+");
		ArrayList<String> CandidateStr = new ArrayList<String>();
		ArrayList<String> QuertStr = new ArrayList<String>();

		// String s1 = "Ra m is boy q";
		ngram.NormalizeString(CandidateStr, QuertStr, candidate, Query);

		/*
		 * for (String st1 : CandidateStr) System.out.print("|" + st1);
		 * System.out.println("\n-"); for (String st2 : QuertStr)
		 * System.out.print("|" + st2);
		 * System.out.println("\n****************");
		 */
		int i = 0;
		double ChannleProb = 0;
		for (String updatedWord : CandidateStr) {
			String raw = QuertStr.get(i);
			ChannleProb = ChannleProb
					+ java.lang.Math.log(CModel.getChannelProbability(raw,
							updatedWord));
			i++;

		}

		double LMProb = ngram.getLMProbability(candidate);
		// prob = CModel.getChannelProbability(candidate) *

		double totalLogProb = ChannleProb + CH_MU*LMProb;
		/*
		 * System.err.println("Candiate : " +candidate);
		 * System.err.print("Channel Prob : " +ChannleProb);
		 * System.err.println("LM : " +LMProb); System.err.println("Total : "
		 * +totalLogProb);
		 */
		if (Parameters.DEBUG == true) {

			System.err.print(" Candidate : " + candidate);
			System.err.print(" # Channel Log Prob : " + ChannleProb);
			System.err.print(" LM Log Prob : " + LMProb);
			System.err.print(" Total Log Prob : " + totalLogProb + "\n");
		}
		return totalLogProb;
	}

	public static void SaveToDisk(SpellCheckerModel sp, String ModelFileName) {

		FileOutputStream fileOut;
		try {
			fileOut = new FileOutputStream(ModelFileName);

			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(sp);
			out.close();
			fileOut.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static SpellCheckerModel ReadFromDisk(String ModelFileName) {

		FileInputStream fileIn;
		try {
			fileIn = new FileInputStream(ModelFileName);

			ObjectInputStream in = new ObjectInputStream(fileIn);
			SpellCheckerModel sp = (SpellCheckerModel) in.readObject();
			in.close();
			fileIn.close();
			return (sp);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;

	}

	String[] generateCandidate(String NoisyWord, int EditDist) {

		if (EditDist < 1) {
			return null;
		}
		int i = 1;
		ArrayList<String> FinalList = new ArrayList<String>();
		AddToCondidates(FinalList, NoisyWord, true); // true = check dictionary
		ArrayList<String> LastList = new ArrayList<String>();
		LastList.add(NoisyWord);
		ArrayList<String> CurrListWithDict;
		ArrayList<String> CurrListNoDict;
		
		while (i <= EditDist) {
			// Get current list
//			System.out.println("Noisey Word : "+ NoisyWord);
//			System.out.println("LastList size  "+ LastList.size());

			
			CurrListWithDict = new ArrayList<String>();
			CurrListNoDict = new ArrayList<String>();

			for (String candi : LastList) {

				ArrayList<String> tempWithDictCheck = generateCandidate(candi,
						true); // true = check dictionary
//				for (String S : tempWithDictCheck) {
//					 System.out.println(" tempWithDictCheck: " + S);
//				 }
				
				ArrayList<String> tempNoDictCheck = generateCandidate(candi,
						false);// false = dont check dictionary
				
//				for (String S : tempNoDictCheck) {
//					System.out.println(" tempNoDictCheck: " + S);
//				}
				
				AddWithoutDuplicate(CurrListWithDict, tempWithDictCheck);
				AddWithoutDuplicate(CurrListNoDict, tempNoDictCheck);

			}
			// appedn to master list
			
//			for (String S : CurrListWithDict) {
//				 System.out.println(" CurrListWithDict: " + S);
//			 }
//			for (String S : CurrListNoDict) {
//				 System.out.println(" CurrListNoDict: " + S);
//			 }
	
			
			AddWithoutDuplicate(FinalList, CurrListWithDict);

			// last list = current list
			LastList = CurrListNoDict;

			i++;
		}
		String words[] = FinalList.toArray(new String[FinalList.size()]);
		return words;
		// return FinalList;

	}

	void AddWithoutDuplicate(ArrayList<String> target, ArrayList<String> source) {
		for (String S : source) {
			if (!target.contains(S)) {
				target.add(S);
			}
		}
	}

	ArrayList<String> generateCandidate(String NoisyWord, boolean CheckDict) {
		StringBuilder NWord = new StringBuilder(NoisyWord);
		ArrayList<String> ExpandedList = new ArrayList<String>();
		StringBuilder TempDel;
		StringBuilder TempSubs;
		StringBuilder TempIns;

		String Alphabet = " abcdefghijklmnopqrstuvwxyz'";

		AddToCondidates(ExpandedList, NoisyWord, CheckDict);
		// Deletion
		
		StringBuilder SB;
		for (int i = 0; i < NWord.length(); i++) {

			TempDel = new StringBuilder(NWord);

			TempDel.deleteCharAt(i);
			// System.out.println(TempDel);
			AddToCondidates(ExpandedList, TempDel.toString(), CheckDict);
			for (int j = 0; j <= Alphabet.length() - 1; j++) {
				TempSubs = new StringBuilder(TempDel);

				String subs = TempSubs.insert(i, Alphabet.charAt(j)).toString();
				// System.out.println(ins);

				AddToCondidates(ExpandedList, subs, CheckDict);
			}
			for (int j = 0; j <= Alphabet.length() - 1; j++) {
				TempIns = new StringBuilder(NWord);
				String ins = TempIns.insert(i, Alphabet.charAt(j)).toString();
				AddToCondidates(ExpandedList, ins,CheckDict);
			}
			
			// Substitution
		
			if (i-1 >= 0 ){
				SB = new StringBuilder(NoisyWord);
				//mChar = NoisyWord.charAt(m);
				//nChar = NoisyWord.charAt(n);
				SB.setCharAt(i, NoisyWord.charAt(i-1));
				SB.setCharAt(i-1, NoisyWord.charAt(i));
				
				//System.out.println(SB.toString());
				AddToCondidates(ExpandedList, SB.toString(), CheckDict);
			}

		}
		for (int j = 0; j <= Alphabet.length() - 1; j++) {
			TempIns = new StringBuilder(NWord);
			String ins = TempIns.append(Alphabet.charAt(j)).toString();
			AddToCondidates(ExpandedList, ins, CheckDict);
		}

		return ExpandedList;

	}

	void AddToCondidates(ArrayList<String> ExpandedList, String newCandidate,
			boolean CheckDict) {

		String S = newCandidate.replaceAll("\\s+", " ").trim();
		boolean ExistInDict = true;
		//System.out.println("*****************"+newCandidate+":"+CheckDict+"*************");
		if (!ExpandedList.contains(S) && !S.equals("")) {

			if (CheckDict == false) // false = dont check dictionary
			{
				ExpandedList.add(S);
				//System.out.println("Not checkign for dict ");
			} else {
				String[] strWords = S.split("\\s+");
				for (int i = 0; i < strWords.length; i++) {

					
						// System.out.println("In Dict : "+ngram.getFrequency(strWords[i])+" : "+strWords[i]);
						if (ExistInDict == true && ngram.getUnigramFreq(strWords[i]) == 0) {
							//System.out.println("Does not exists : "+strWords[i]);
							ExistInDict = false;
						}
				}
				if (ExistInDict == true) {
					  // System.out.println("Candidate added "+ S);
						ExpandedList.add(S);
				}

				
			}

		}

	}
	
	
	public static void main(String[] args) {

		SpellCheckerModel SPModel = new SpellCheckerModel(ChannelModelType.UNIFORM);
		String MisspelledQueriesFile = "/cs276/PA2/data/edit1s.txt";
		String CorpusDir = "/cs276/PA2/data/corpus";
		System.out.println("Starting training ...");

		SPModel.train(CorpusDir,MisspelledQueriesFile);
		System.out.println("Training Complete");

		String word = "oppertunity";
		
		//String [] SP = SPModel.generateSubs(word);
	//	System.out.println(SPModel.getNGram().getFrequency("ghalnd"));
	//	System.out.println(SPModel.getNGram().getUnigramProbability("ghalnd"));
		
//		 ArrayList<String> sarr = SPModel.generateCandidate(word,true);
//		 for (String S : sarr) {
//			 System.out.println(S+":"+SPModel.getNGram().getFrequency(S));
//		 }

//		String[] candidates = SPModel.generateCandidate(word,1);
//
//		for (int i = 0; i < candidates.length; i++) {
//			//System.out.println(" Generated words :" + );
//			System.out.println(candidates[i]+":"+SPModel.getNGram().getFrequency(candidates[i]));
//		}

		System.out.println("End");

	}

	public void RunQueryFile(SpellCheckerModel sPModel, File queryFile,
			File goldCopy) {
		// TODO Auto-generated method stub
		
	}
}
