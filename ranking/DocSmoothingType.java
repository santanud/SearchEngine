package cs276.programming.ranking;

/**
 * The different types of document frequency
 * smoothing as described in Fig 6.15 or IIR. 
 */
public enum DocSmoothingType {
	//1
	NONE,
	
	//log(N/df_t)
	IDF, 
	
	//max{0, log((N-df_t)/df_t)}
	PROBIDF
}