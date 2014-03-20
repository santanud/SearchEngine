package cs276.programming.ranking;

/**
 * The different types of term frequency
 * smoothing as described in Fig 6.15 or IIR. 
 */
public enum TermSmoothingType {
	//tf_t,d
	NATURAL, //aka Raw
	
	//1 + log(tf_t,d)
	LOGARITHM, //aka sublinear scaling
	
	//0.5 + 0.5 * tf_t,d / max_t(tf_t,d)
	AUGMENTED,
	
	//1 if tf_t,d > 0; 0 otherwise
	BOOLEAN,
	
	//(1 + log(tf_t,d)) / (1 + log(ave_tEd(tf_t,d))
	LOGAVERAGE
}