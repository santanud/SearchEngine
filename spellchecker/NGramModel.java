package cs276.programming.spellchecker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import cs276.programming.query.TextProcessor;
import cs276.programming.util.Parameters;

/**
 * Class to manage the NGram model. 
 *
 */
public class NGramModel  implements java.io.Serializable {

	private static final long serialVersionUID = 138153526957621971L;

	private double LAMBDA = 0.2;

	public Map<String, int[]> Unigram;
	public Map<String, Integer> Bigram;
	public int UniGramWordCount = 0;
	public int BiGramWordCount = 0;
//	public HashMap<String,HashSet<String>> EndWithWi;
//	public HashMap<String,HashSet<String>> StartWithWi_1;
	private ChannelModelType modelType = ChannelModelType.UNIFORM;

	
	NGramModel(){
//		EndWithWi = new HashMap<String,HashSet<String>>();
//		StartWithWi_1 = new HashMap<String,HashSet<String>>();
	}
	
	/**
	 * Train the ngram model.
	 */
	void train(String CorpusDir) {
		BufferedReader in = null;
		String strLine = "";
		File CorDir = new File(CorpusDir);
//		public HashMap<String,HashSet<String>> EndWithWi;
//		public HashMap<String,HashSet<String>> StartWithWi_1;
		HashMap<String,HashSet<String>> EndWithWi = new HashMap<String,HashSet<String>>();
		HashMap<String,HashSet<String>> StartWithWi_1 = new HashMap<String,HashSet<String>>();

		Unigram = new HashMap<String, int[]>();
		Bigram = new HashMap<String, Integer>();
		String Wminus1 = "";
		String CurWord = "";
		String BigramWord = "";
		try {

			if (CorDir.isDirectory()) {
				for (File file : CorDir.listFiles()) {

					in = new BufferedReader(new FileReader(file));
					while (((strLine = in.readLine()) != null)) {
                       // System.out.println(strLine+"\n\n\n\n");
						String[] terms = strLine.split("\\s+");
						//for( String s : terms)
							//System.out.println(s);
						//System.out.println(terms[0]);
						Wminus1 = terms[0];
						IncrementUnigram(Unigram, Wminus1);
						UniGramWordCount++;
						//System.out.println(terms.length);

						for (int i = 1; i < terms.length; i++) {

							CurWord = terms[i];
							BigramWord = Wminus1 + "<>" + CurWord;

							IncrementBigram(Bigram, BigramWord);
							BiGramWordCount++;
							PopulateKNS(EndWithWi, StartWithWi_1,  Wminus1,CurWord);

							IncrementUnigram(Unigram, terms[i]);
							Wminus1 = CurWord;
							UniGramWordCount++;
						}
					}
				}
				
				MergeIntoUnigram(StartWithWi_1,EndWithWi);
				
				

				if (Parameters.DEBUG) {
					System.err.println("Language Model training stats");
					System.err.println("Unigram dict size : " + Unigram.size());
					System.err.println("Bigram dict size : " + Bigram.size());
					System.err.println("Unigram Count : " + UniGramWordCount);
					System.err.println("Bigram Count :  " + BiGramWordCount);
					System.err.println("******");
				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if(in != null) in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
	
	void MergeIntoUnigram(HashMap<String,HashSet<String>>StartWithWi_1, HashMap<String,HashSet<String>>EndWithWi){
		
		Map<String, int[]> ugram = this.Unigram;
		
		for(String word : StartWithWi_1.keySet()){
			int[] Uvalue = ugram.get(word);
			Uvalue[1] = StartWithWi_1.get(word).size();
		}
		
		for(String word : EndWithWi.keySet()){
			int[] Uvalue = ugram.get(word);
			Uvalue[2] = EndWithWi.get(word).size();
		}
	}

	void SetLambda(double LMlambda) {
		LAMBDA = LMlambda;
	}
	void SetChannelModel(ChannelModelType type) {
		this.modelType = type;
		
	}

	void IncrementUnigram(Map<String, int[]> MyMap, String Key) {
		
		//System.out.println("Word : "+ Key);
		int Uvalues[] = new int[]{1,0,0};
		if (!MyMap.containsKey(Key)) {
			MyMap.put(Key, Uvalues);
		} else {
			
			Uvalues = MyMap.get(Key);
			Uvalues[0] = Uvalues[0]+1; 
			//MyMap.put(Key, Uvalues);
			//System.out.println("exists : "+Uvalues[0]+":"+Uvalues[1]);
		}
	}
	
	void IncrementBigram(Map<String, Integer> MyMap, String Key) {
		int i = 0;
		if (!MyMap.containsKey(Key)) {
			MyMap.put(Key, 1);
		} else {
			i = MyMap.get(Key);
			MyMap.put(Key, i + 1);
		}
	}

	double getUnigramProbability(String Word) {
		// P_mle(w1) = count(w1) / T
		// System.out.println("Word "+Word );
		return (1.0 * getUnigramFreq(Word)) / UniGramWordCount;
	}

	double getBigramProbability(String PrevWord, String Word) {

		// P_mle(w2|w1) = count(w1,w2) / count(w1)
		String BigramWord = PrevWord + "<>" + Word;
		if (Bigram.containsKey(BigramWord)) {
			return (1.0 * getBigramFreq(BigramWord) / getUnigramFreq(PrevWord));
		} else {
			return 0.0D;

		}

	}

	double getInterpolaProbab(String PrevWord, String Word) {

		return (LAMBDA * getUnigramProbability(Word) + (1 - LAMBDA)
				* getBigramProbability(PrevWord, Word));
	}
	
	void PopulateKNS(HashMap<String,HashSet<String>> EndWithWi , HashMap<String,HashSet<String>> StartWithWi_1, String PrevWord, String Word){
		HashSet<String> Wi_1_Set;
		HashSet<String> Wi_Set;
		
		if(StartWithWi_1.containsKey(PrevWord)){
			Wi_1_Set = StartWithWi_1.get(PrevWord);
			Wi_1_Set.add(Word);
		}
		else{
			Wi_1_Set = new HashSet<String>();
			Wi_1_Set.add(Word);
			StartWithWi_1.put(PrevWord, Wi_1_Set);
		}
		if(EndWithWi.containsKey(Word)){
			Wi_Set = EndWithWi.get(Word);
			Wi_Set.add(PrevWord);
		}
		else{
			Wi_Set = new HashSet<String>();
			Wi_Set.add(PrevWord);
			EndWithWi.put(Word, Wi_Set);
		}
	}
	
	double KneserNeySmoothing(String PrevWord, String Word) {

		//HashMap<String,HashSet<String>> Wi = new HashMap<String,HashSet<String>>();
		//HashSet<String> Wi_1 = new HashSet<String>();
		double KNS_DISCOUNT = 0.75;
		
		String BigramWord =  PrevWord + "<>" + Word;
		
		double DiscoBigramCount = ( getBigramFreq(BigramWord) - KNS_DISCOUNT ) < 0 ? 0 : ( getBigramFreq(BigramWord) - KNS_DISCOUNT );
		
	//	System.out.println("getBigramFreq(BigramWord) - KNS_DISCOUNT ) : "+  (getBigramFreq(BigramWord) - KNS_DISCOUNT ));

	//	System.out.println("DiscoBigramCount : "+ DiscoBigramCount);
		int UgramCountWi_1 = getUnigramFreq(PrevWord) ;
//		System.out.println("UgramCountWi_1 : "+UgramCountWi_1);
		
		int AllBigramCount = Bigram.size();
	//	System.out.println("AllBigramCount : "+AllBigramCount);

		int StartWithWi_1_Count =0;
		int  EndWithWi_Count =0;
		
	//	if (StartWithWi_1.containsKey(PrevWord) && EndWithWi.containsKey(Word) ){
		if (Unigram.get(PrevWord)[1] > 0  && Unigram.get(Word)[2] > 0 ){
			
			//StartWithWi_1_Count = StartWithWi_1.get(PrevWord).size();
			StartWithWi_1_Count = Unigram.get(PrevWord)[1];
			
			//EndWithWi_Count = EndWithWi.get(Word).size();
			EndWithWi_Count = Unigram.get(Word)[2];
		}
	//	System.out.println("StartWithWi_1_Count : "+StartWithWi_1_Count);
	//	System.out.println("EndWithWi_Count : "+EndWithWi_Count);

	//	System.out.println("StartWithWi_1_Count : "+StartWithWi_1_Count);
	//	System.out.println("DiscoBigramCount/UgramCountWi_1 : "+ DiscoBigramCount/UgramCountWi_1);

	//	System.out.println("(KNS_DISCOUNT * StartWithWi_1_Count/ UgramCountWi_1) : "+ (KNS_DISCOUNT * StartWithWi_1_Count/ UgramCountWi_1));
	//	System.out.println("(EndWithWi_Count/AllBigramCount) : "+ (1.0 * EndWithWi_Count/AllBigramCount));


		return DiscoBigramCount/UgramCountWi_1 + (KNS_DISCOUNT * StartWithWi_1_Count/ UgramCountWi_1)* (1.0* EndWithWi_Count/AllBigramCount);
		
	
	}

	double getLMProbability(String sentence) {
		// P_int(w2|w1) = lambda P_mle(w2) + (1 - lambda) P_mle(w2|w1)
		String words[] = sentence.split("\\s+");
		double prob = java.lang.Math.log(getUnigramProbability(words[0]));
		String PrevWord = words[0];
		String CurrWord = words[0];
		for (int i = 1; i < words.length; i++) {

			CurrWord = words[i];
			if (this.modelType == ChannelModelType.EXTRA)
				prob = prob+ java.lang.Math.log(KneserNeySmoothing(PrevWord, CurrWord));
			else
				prob = prob+ + java.lang.Math.log(getInterpolaProbab(PrevWord, CurrWord));
			
			PrevWord = CurrWord;
		}
		return prob;

	}

	public int getUnigramFreq(String word) {
		int val = 0;

		if (Unigram.containsKey(word)) {
			val = Unigram.get(word)[0];
		}
		return val;
	}

	public int getBigramFreq(String word){
		int val = 0;

		if (Bigram.containsKey(word)) {
			val = Bigram.get(word);
		}
		return val;
	}
	
	static void NormalizeString(ArrayList<String> StrList1,
			ArrayList<String> StrList2, String Str1, String Str2) {

		String[] StrSplit1 = Str1.split("\\s+");
		String[] StrSplit2 = Str2.split("\\s+");
		int i1 = 0;
		int i2 = 0;

		StringBuilder TemStr = new StringBuilder();

		if (StrSplit1.length == StrSplit2.length) {

			for (int i = 0; i < StrSplit1.length; i++) {
				StrList1.add(StrSplit1[i]);
				StrList2.add(StrSplit2[i]);
			}

		} else {

			while (i1 < StrSplit1.length && i2 < StrSplit2.length) {
				// System.out.println(StrSplit1[i1]);
				// System.out.println(StrSplit2[i2]);
				if (StrSplit1[i1].equals(StrSplit2[i2])) {
					StrList1.add(StrSplit1[i1]);
					StrList2.add(StrSplit2[i2]);
					i1++;
					i2++;
				} else {
					int m1 = i1;
					int m2 = i2;
					StringBuilder S1 = new StringBuilder();
					StringBuilder S2 = new StringBuilder();
					boolean matched = false;

					while (m2 < StrSplit2.length && matched == false) {
						m1 = i1;
						while (m1 < StrSplit1.length && matched == false) {
							if (StrSplit1[m1].equals(StrSplit2[m2])) {
								matched = true;
								break;
							}
							m1++;
						}
						if (matched == true) {
							break;
						}
						m2++;
					}
//					System.out.println("i1=" + i1);
//					System.out.println("i2=" + i2);
//					System.out.println("m1=" + m1);
//					System.out.println("m2=" + m2);

					if (matched = true) {
						int p1 = i1;
						while (p1 <= m1 - 1) {
							S1.append(" " + StrSplit1[p1]);
							p1++;
						}
						StrList1.add(S1.toString().trim());
						int p2 = i2;
						while (p2 <= m2 - 1) {
							S2.append(" " + StrSplit2[p2]);
							p2++;
						}
						StrList2.add(S2.toString().trim());
						i1 = m1;
						i2 = m2;
					} else {

						int p1 = i1;
						while (p1 <= StrSplit1.length) {
							S1.append(" " + StrSplit1[p1]);
							p1++;
						}
						StrList1.add(S1.toString().trim());
						int p2 = i2;
						while (p2 < StrSplit2.length) {
							S2.append(" " + StrSplit2[p2]);
							p2++;
						}
						StrList2.add(S2.toString().trim());
						i1 = m1;
						i2 = m2;

					}
					// System.out.println("i1=" + i1);System.out.println("i2=" +
					// i2);System.out.println("m1=" +
					// m1);System.out.println("m2=" +
					// m2);
				}
			}
		}
	}
	
	void DisplayDetails(String sentence ){
		
		String words[] = sentence.split("\\s+");
		double prob = java.lang.Math.log(getUnigramProbability(words[0]));
		String PrevWord = words[0];
		String CurrWord = words[0];
		System.err.println("Sentence : "+sentence);
		System.err.println("___unigram term = "+CurrWord+" Unigram count = "+getUnigramFreq(CurrWord)+
				" Unigram prob = "+getUnigramProbability(CurrWord));
		for (int i = 1; i < words.length; i++) {

			CurrWord = words[i];
			String BiGramWord = PrevWord + "<>" + CurrWord;
			System.err.println("___unigram term = "+CurrWord+" Unigram count = "+getUnigramFreq(CurrWord)+
					" Unigram prob = "+getUnigramProbability(CurrWord));
			
			System.err.println("______bigram term = "+BiGramWord+" Bigram count = "+getBigramFreq(BiGramWord)+
					" Bigram prob = "+ getBigramProbability(PrevWord, CurrWord)+" Interpol prob = "+getInterpolaProbab(PrevWord, CurrWord));
			PrevWord = CurrWord;
		}
		System.err.println("Total Prob = "+getLMProbability(sentence));
		
		
		
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		//String CorpusDir = args.length > 0? args[0] : "/cs276/PA2/data/corpus";
		String CorpusDir = args.length > 0? args[0] : "/Users/sdey/Documents/data";
		  Parameters.DEBUG =true;
		  NGramModel ngram = new NGramModel();
		  ngram.train(CorpusDir);
		  //System.out.println(ngram.Unigram);
		  System.out.println(ngram.Bigram);
//		  System.out.println(ngram.EndWithWi);
//		  System.out.println(ngram.StartWithWi_1);
		  
		  for(String s : ngram.Unigram.keySet()){
			  System.out.println(s+":"+ngram.Unigram.get(s)[0]+","+ngram.Unigram.get(s)[1]+","+ngram.Unigram.get(s)[2]);
		  }
		 
		  
		  System.out.println(ngram.KneserNeySmoothing("11", "99"));
		  
		  String sentence = "meadows june 2004 halfway up";
		  
		  //ngram.DisplayDetails(sentence);
		  //System.err.println(ngram.getLMProbability(sentence));
		   
		 /* 
		 * System.out.println(ngram.getUnigramProbability("premios"));
		 * System.out.println(ngram.getUnigramProbability("next"));
		 * System.out.println(ngram.getUnigramProbability("slide"));
		 * 
		 * System.out.println(ngram.getBigramProbability("premios", "next"));
		 * System.out.println(ngram.getBigramProbability("next", "slide"));
		 * 
		 * System.out.println(ngram.getInterpolaProbab("premios", "next"));
		 * System.out.println(ngram.getInterpolaProbab("next", "slide"));
		 * 
		 * System.out.println(ngram.getLMProbability("premios next slide"));
		 */

	//	ArrayList<String> StrList1 = new ArrayList<String>();
	//	ArrayList<String> StrList2 = new ArrayList<String>();
		String strLine = "";

		// File file = new File(
		// "/Users/sdey/Documents/sdey_personal/apps/workspace/PA2/data/edit1s.txt");
		// try {
		// BufferedReader in = new BufferedReader(new FileReader(file));
		// while (((strLine = in.readLine()) != null)) {
		// StrList1 = new ArrayList<String>();
		// StrList2 = new ArrayList<String>();
		//
		// String[] Words = strLine.split("\t");
		// NormalizeString(StrList1, StrList2, Words[0], Words[1]);
		//
		// int i = 0;
		// for (String st1 : StrList1) {
		// if (!st1.equals(StrList2.get(i))) {
		// System.out.println(Words[0]+":"+Words[1]);
		// System.out.println(st1 + "->" + StrList2.get(i));
		// i++;
		// }
		// }
		// }
		// } catch (FileNotFoundException e) { // TODO Auto-generated catch
		// block
		// e.printStackTrace();
		// } catch (IOException e) {
		//
		// e.printStackTrace();
		// }

//		String s1 = "needsi  finding needs filtering invent";
//		String s2 = "needs s finding needs filtering invent";
//		NormalizeString(StrList1, StrList2, s1, s2);
//		for (String st1 : StrList1)
//			System.out.print("|" + st1);
//		System.out.println("\n------");
//		for (String st2 : StrList2)
//			System.out.print("|" + st2);

		System.out.println("\n------");
//
//		int i = 0;
//		for (String st1 : StrList1) {
//			if (!st1.equals(StrList2.get(i))) {
//				// System.out.println(s1+":"+s2);
//				System.out.println(st1 + "->" + StrList2.get(i));
//
//			}
//			i++;
//		}

	}
}
