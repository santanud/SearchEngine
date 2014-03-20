package cs276.programming.spellchecker;

/**
 * Interface representing a Noisy Channel Model. 
 *
 */
public interface Model {

	double getChannelProbability(String rawQuery, String correctedQuery);
}
