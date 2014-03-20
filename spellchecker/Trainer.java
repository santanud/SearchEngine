package cs276.programming.spellchecker;

/**
 * 
 * Interface for classes that consume the training data
 * and compute the parameters for the Noisy Channel Model. 
 */
public interface Trainer<M extends Model> {

	public M train(String edit1sFile);
}
