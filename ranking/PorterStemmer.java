package cs276.programming.ranking;

/**
 * Implementation of the 
 * Porter Stemmer Algorithm as described in
 * http://tartarus.org/martin/PorterStemmer/def.txt
 *
 */
public class PorterStemmer {

	
	public String stem(String word) {
		String stemmed = word;
		
		/*
		 * Step 1a
		 * 
		 * SSES -> SS                         caresses  ->  caress
		 * IES  -> I                          ponies    ->  poni
		 *                             ties      ->  ti
		 * SS   -> SS                         caress    ->  caress
		 * S    ->                            cats      ->  cat
		 */
		//Step 1a
		stemmed = replaceEnd(stemmed, "sses", "ss");
		stemmed = replaceEnd(stemmed, "ies", "i");
		
		if(!stemmed.endsWith("ss") && stemmed.endsWith("s")) {
			stemmed = stemmed.substring(0, stemmed.length() - 1);
		}
		
		
		/*
		 * Step 1b
		 *     (m>0) EED -> EE                    feed      ->  feed
		 *    									  agreed    ->  agree
		 *     (*v*) ED  ->                       plastered ->  plaster
		 *     										bled      ->  bled
		 *     (*v*) ING ->                       motoring  ->  motor 
		 *                                        sing      ->  sing
		 */
		
/*		If the second or third of the rules in Step 1b is successful, the following
		is done:

		    AT -> ATE                       conflat(ed)  ->  conflate
		    BL -> BLE                       troubl(ed)   ->  trouble
		    IZ -> IZE                       siz(ed)      ->  size
		    (*d and not (*L or *S or *Z))   //tests for a stem ending with a double consonant other than L, S or Z.
		       -> single letter
		                                    hopp(ing)    ->  hop
		                                    tann(ed)     ->  tan
		                                    fall(ing)    ->  fall
		                                    hiss(ing)    ->  hiss
		                                    fizz(ed)     ->  fizz
		    (m=1 and *o) -> E               fail(ing)    ->  fail  // *o  - the stem ends cvc, where the second c is not W, X or Y (e.g. -WIL, -HOP).
		                                    fil(ing)     ->  file
*/
		stemmed = replaceEnd(stemmed, "eed", "ee", 0);	
		if(stemmed.endsWith("ed")) {
			String temp = replaceEnd(stemmed, "ed", "");
			if(hasVowel(temp)) {
				stemmed = temp;
				stemmed = replaceEnd(stemmed, "at", "ate");
				stemmed = replaceEnd(stemmed, "bl", "ble");
				stemmed = replaceEnd(stemmed, "iz", "ize");
				//(*d and not (*L or *S or *Z)) -> single letter
				if(stemmed.length() > 2 && stemmed.charAt(stemmed.length() - 1) == stemmed.charAt(stemmed.length() - 2)) {
					char dupChar = stemmed.charAt(stemmed.length() - 1);
					if(dupChar != 'l' && dupChar != 's' && dupChar != 'z') {
						stemmed = stemmed.substring(0, stemmed.length()-1);
					}
				}
				//(m=1 and *o) -> E
				// -> m=1 => [c]vc[v] -> so no need to check this as *o => cvc
				if(stemmed.length() > 2 && !isVowel(stemmed, 0) && isVowel(stemmed, 1) && !isVowel(stemmed, 2)) { //cvc
					char c = stemmed.charAt(2);
					if(c != 'w' && c != 'x' && c != 'y') {
						stemmed = stemmed + "e";
					}
				}
			}
		}
		if(stemmed.endsWith("ing")) {
			String temp = replaceEnd(stemmed, "ing", "");
			if(hasVowel(temp)) {
				stemmed = temp;
				stemmed = replaceEnd(stemmed, "at", "ate");
				stemmed = replaceEnd(stemmed, "bl", "ble");
				stemmed = replaceEnd(stemmed, "iz", "ize");
				//(*d and not (*L or *S or *Z)) -> single letter
				if(stemmed.length() > 2 && stemmed.charAt(stemmed.length() - 1) == stemmed.charAt(stemmed.length() - 2)) {
					char dupChar = stemmed.charAt(stemmed.length() - 1);
					if(dupChar != 'l' && dupChar != 's' && dupChar != 'z') {
						stemmed = stemmed.substring(0, stemmed.length()-1);
					}
				}
				//(m=1 and *o) -> E
				// -> m=1 => [c]vc[v] -> so no need to check this as *o => cvc
				if(stemmed.length() > 2 && !isVowel(stemmed, 0) && isVowel(stemmed, 1) && !isVowel(stemmed, 2)) { //cvc
					char c = stemmed.charAt(2);
					if(c != 'w' && c != 'x' && c != 'y') {
						stemmed = stemmed + "e";
					}
				}
			}
		}

		/*
		Step 1c
		
		    (*v*) Y -> I                    happy        ->  happi
		                                    sky          ->  sky
		 */
		stemmed = replaceEndIfVowel(stemmed, "y", "i");
		
		/*
		Step 2
		
		    (m>0) ATIONAL ->  ATE           relational     ->  relate
		    (m>0) TIONAL  ->  TION          conditional    ->  condition
		                                    rational       ->  rational
		    (m>0) ENCI    ->  ENCE          valenci        ->  valence
		    (m>0) ANCI    ->  ANCE          hesitanci      ->  hesitance
		    (m>0) IZER    ->  IZE           digitizer      ->  digitize
		    (m>0) ABLI    ->  ABLE          conformabli    ->  conformable
		    (m>0) ALLI    ->  AL            radicalli      ->  radical
		    (m>0) ENTLI   ->  ENT           differentli    ->  different
		    (m>0) ELI     ->  E             vileli        - >  vile
		    (m>0) OUSLI   ->  OUS           analogousli    ->  analogous
		    (m>0) IZATION ->  IZE           vietnamization ->  vietnamize
		    (m>0) ATION   ->  ATE           predication    ->  predicate
		    (m>0) ATOR    ->  ATE           operator       ->  operate
		    (m>0) ALISM   ->  AL            feudalism      ->  feudal
		    (m>0) IVENESS ->  IVE           decisiveness   ->  decisive
		    (m>0) FULNESS ->  FUL           hopefulness    ->  hopeful
		    (m>0) OUSNESS ->  OUS           callousness    ->  callous
		    (m>0) ALITI   ->  AL            formaliti      ->  formal
		    (m>0) IVITI   ->  IVE           sensitiviti    ->  sensitive
		    (m>0) BILITI  ->  BLE           sensibiliti    ->  sensible
		 */
		stemmed = replaceEnd(stemmed, "ational", "ate", 0);
		stemmed = replaceEnd(stemmed, "tional", "tion", 0);
		stemmed = replaceEnd(stemmed, "enci", "ence", 0);
		stemmed = replaceEnd(stemmed, "anci", "ance", 0);
		stemmed = replaceEnd(stemmed, "izer", "ize", 0);
		stemmed = replaceEnd(stemmed, "abli", "able", 0);
		stemmed = replaceEnd(stemmed, "alli", "al", 0);
		stemmed = replaceEnd(stemmed, "entli", "ent", 0);
		stemmed = replaceEnd(stemmed, "eli", "e", 0);
		stemmed = replaceEnd(stemmed, "ization", "ize", 0);
		stemmed = replaceEnd(stemmed, "ation", "ate", 0);
		stemmed = replaceEnd(stemmed, "ator", "ate", 0);
		stemmed = replaceEnd(stemmed, "alism", "al", 0);
		stemmed = replaceEnd(stemmed, "iveness", "ive", 0);
		stemmed = replaceEnd(stemmed, "fulness", "ful", 0);
		stemmed = replaceEnd(stemmed, "ousness", "ous", 0);
		stemmed = replaceEnd(stemmed, "aliti", "al", 0);
		stemmed = replaceEnd(stemmed, "iviti", "ive", 0);
		stemmed = replaceEnd(stemmed, "biliti", "ble", 0);
		
		/*
		Step 3
		
		    (m>0) ICATE ->  IC              triplicate     ->  triplic
		    (m>0) ATIVE ->                  formative      ->  form
		    (m>0) ALIZE ->  AL              formalize      ->  formal
		    (m>0) ICITI ->  IC              electriciti    ->  electric
		    (m>0) ICAL  ->  IC              electrical     ->  electric
		    (m>0) FUL   ->                  hopeful        ->  hope
		    (m>0) NESS  ->                  goodness       ->  good
		 */
		stemmed = replaceEnd(stemmed, "icate", "ic", 0);
		stemmed = replaceEnd(stemmed, "ative", "", 0);
		stemmed = replaceEnd(stemmed, "alize", "al", 0);
		stemmed = replaceEnd(stemmed, "iciti", "ic", 0);
		stemmed = replaceEnd(stemmed, "ical", "ic", 0);
		stemmed = replaceEnd(stemmed, "ful", "", 0);
		stemmed = replaceEnd(stemmed, "ness", "", 0);
		
		/*
		 Step 4
		    (m>1) AL    ->                  revival        ->  reviv
		    (m>1) ANCE  ->                  allowance      ->  allow
		    (m>1) ENCE  ->                  inference      ->  infer
		    (m>1) ER    ->                  airliner       ->  airlin
		    (m>1) IC    ->                  gyroscopic     ->  gyroscop
		    (m>1) ABLE  ->                  adjustable     ->  adjust
		    (m>1) IBLE  ->                  defensible     ->  defens
		    (m>1) ANT   ->                  irritant       ->  irrit
		    (m>1) EMENT ->                  replacement    ->  replac
		    (m>1) MENT  ->                  adjustment     ->  adjust
		    (m>1) ENT   ->                  dependent      ->  depend
		    (m>1 and (*S or *T)) ION ->     adoption       ->  adopt
		    (m>1) OU    ->                  homologou      ->  homolog
		    (m>1) ISM   ->                  communism      ->  commun
		    (m>1) ATE   ->                  activate       ->  activ
		    (m>1) ITI   ->                  angulariti     ->  angular
		    (m>1) OUS   ->                  homologous     ->  homolog
		    (m>1) IVE   ->                  effective      ->  effect
		    (m>1) IZE   ->                  bowdlerize     ->  bowdler
		 */
		stemmed = replaceEnd(stemmed, "al", "", 1);
		stemmed = replaceEnd(stemmed, "ance", "", 1);
		stemmed = replaceEnd(stemmed, "ence", "", 1);
		stemmed = replaceEnd(stemmed, "er", "", 1);
		stemmed = replaceEnd(stemmed, "ic", "", 1);
		stemmed = replaceEnd(stemmed, "able", "", 1);
		stemmed = replaceEnd(stemmed, "ible", "", 1);
		stemmed = replaceEnd(stemmed, "ant", "", 1);
		stemmed = replaceEnd(stemmed, "ement", "", 1);
		stemmed = replaceEnd(stemmed, "ment", "", 1);
		stemmed = replaceEnd(stemmed, "ent", "", 1);
		//(m>1 and (*S or *T)) ION
		if(stemmed.endsWith("ion")) {
			String stem = stemmed.substring(0, stemmed.length() - 3);
			if(getMeasure(stem) > 1) {
				char lastChar = stem.charAt(stem.length()-1);
				if(lastChar == 's' || lastChar == 't') {
					stemmed = stem;
				}
			}
		}
		stemmed = replaceEnd(stemmed, "ou", "", 1);
		stemmed = replaceEnd(stemmed, "ism", "", 1);
		stemmed = replaceEnd(stemmed, "ate", "", 1);
		stemmed = replaceEnd(stemmed, "iti", "", 1);
		stemmed = replaceEnd(stemmed, "ous", "", 1);
		stemmed = replaceEnd(stemmed, "ive", "", 1);
		stemmed = replaceEnd(stemmed, "ize", "", 1);
		
		/*
		Step 5a
		
		    (m>1) E     ->                  probate        ->  probat
		                                    rate           ->  rate
		    (m=1 and not *o) E ->           cease          ->  ceas
		 */
		stemmed = replaceEnd(stemmed, "e", "", 1);
		//(m=1 and not *o) E -> 
		if(stemmed.length() > 1 && stemmed.charAt(stemmed.length() - 1) == 'e' && 
				getMeasure(stemmed) > 1 && !isVowel(stemmed, 0) && isVowel(stemmed, 1) && !isVowel(stemmed, 2)) { //cvc
			stemmed = stemmed.substring(0, stemmed.length() - 1);
		}

		/*
		Step 5b
		
		    (m > 1 and *d and *L) -> single letter
		                                    controll       ->  control
		                                    roll           ->  roll
		 */
		if(stemmed.length() > 2 && stemmed.charAt(stemmed.length() - 1) == stemmed.charAt(stemmed.length() - 2)) { //*d
			char dupChar = stemmed.charAt(stemmed.length() - 1);
			if(dupChar == 'l' && getMeasure(stemmed) > 1) {
				stemmed = stemmed.substring(0, stemmed.length()-1);
			}
		}
		
		return stemmed;
	}
	private String replaceEndIfVowel(String stemmed, String suffix, String suffix2) {
		String temp = replaceEnd(stemmed, suffix, suffix2);
		if(hasVowel(temp)) {
			stemmed = temp;
		}
		return stemmed;
	}
	private boolean hasVowel(String str) {
		//A \consonant\ in a word is a letter other than A, E, I, O or U, and other
		//than Y preceded by a consonant.
		//So in TOY the consonants are T and Y, and in SYZYGY they are S, Z and G.
		//[b-df-hj-np-tv-xz]|[aeiou]y
		//If a letter is not a consonant it is a \vowel\.
		//[aeiou]|[b-df-hj-np-tv-xz]y
		boolean isVowel = str.contains("a") || str.contains("e") || str.contains("i") || str.contains("o") || str.contains("u");
//		if(!isVowel && str.contains("y")) {
//			//check previous char for all Ys in stem
//			int fromIndex = 0;
//			int pos = -1;
//			while((pos = str.indexOf('y', fromIndex)) != -1) {
//				fromIndex = pos + 1;
//				if(pos == 0) {
//					continue;
//				}
//				char prevChar = str.charAt(pos - 1);
//				if (prevChar != 'a' && prevChar != 'e' && prevChar != 'i' && prevChar != 'o' && prevChar != 'u'); {
//					return true;
//				}
//			}
//		}
		return isVowel;
	}
	private boolean isVowel(String str, int pos) {
		char c = str.charAt(pos);
		if(c == 'a' || c == 'e' || c == 'i' || c == 'o' || c == 'u') {
			return true;
		}
		if(c == 'y' && pos != 0) {
			char prev = str.charAt(pos - 1);
			if(prev == 'a' || prev == 'e' || prev == 'i' || c == 'o' || c == 'u') {
				return true;
			}
		}
		return false;
	}
	private String replaceEnd(String stemmed, String suffix, String suffix2) {

		if(stemmed.endsWith(suffix)) {
			stemmed = stemmed.substring(0, stemmed.length() - suffix.length()) + suffix2;
		}
		return stemmed;
	}
	private String replaceEnd(String stemmed, String suffix, String suffix2, int measure) {

		if(stemmed.endsWith(suffix) && (stemmed.length() - suffix.length()) > measure) {
			String stem = stemmed.substring(0, stemmed.length() - suffix.length()); 
			if(getMeasure(stem) > measure) {
				stemmed = stem + suffix2;
			}
		}
		
		return stemmed;
	}
	
	private int getMeasure(String stem) {

		//A \consonant\ in a word is a letter other than A, E, I, O or U, and other
		//than Y preceded by a consonant.
		//So in TOY the consonants are T and Y, and in SYZYGY they are S, Z and G.
		//[b-df-hj-np-tv-xz]|[aeiou]y
		//If a letter is not a consonant it is a \vowel\.
		//[aeiou]|[b-df-hj-np-tv-xz]y
		
		//Using (VC){m} to denote VC repeated m times, a word may again be written as
		//[C](VC){m}[V].
		
		int measure = 0;
		int pos = 0;
		if(!isVowel(stem, pos)) { //skip the initial consonant
			pos++;
		}
		
		int length = stem.length();
		while(pos < length) {
			//read the next vowel
			while(!isVowel(stem, pos)) {
				pos++;
				if(pos >= length) return measure;
			}
			//read the next consonent
			while(isVowel(stem, pos)) {
				pos++;
				if(pos >= length) return measure;
			}
			measure++;
		}
		
		return measure;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		PorterStemmer ps = new PorterStemmer();
		String word;
		String expectedStem;
		
//		System.err.println(ps.hasVowel("tstey"));
//		System.err.println(ps.isVowel("tstwy", 4));
//		System.err.println(ps.stem("failing"));
//		System.err.println(ps.stem("hopping"));
		System.err.println(ps.getMeasure("orrery"));

//		checkStem(ps, "caresses", "caress");
//		checkStem(ps, "ponies", "poni");
//		checkStem(ps, "cement", "cement");
//		checkStem(ps, "caress", "caress");
//		checkStem(ps, "cars", "cars");
//		checkStem(ps, "plastered", "plastered");
//		checkStem(ps, "bled", "bled");
//		checkStem(ps, "cease", "cease");
//		checkStem(ps, "probate", "probate");
//		checkStem(ps, "bled", "bled");
//		checkStem(ps, "rate", "rate");
	}
	private static void checkStem(PorterStemmer ps, String word, String expectedStem) {

		String stemWord = ps.stem(word);
		assert stemWord.equals(expectedStem) : word + " should reduce to " + expectedStem + ", but got stemmed to " + stemWord;
		
	}

}
