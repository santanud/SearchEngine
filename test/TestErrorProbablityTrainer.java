package cs276.programming.test;

import cs276.programming.spellchecker.Edit;
import cs276.programming.spellchecker.ErrorProbabilityModel;
import cs276.programming.spellchecker.ErrorProbablityTrainer;
import cs276.programming.spellchecker.Edit.EditType;

/**
 * Class to unit test the error probability
 * trainer functionality.
 *
 */
public class TestErrorProbablityTrainer {

//	public static final char[] ALPHABET = "abcdefghijklmnopqrstuvwxyz 0123456789".toCharArray();
	public static final char[] ALPHABET = " #$&'+.0123456789_abcdefghijklmnopqrstuvwxyz~".toCharArray();

	public static void main(String[] args) {
		TestErrorProbablityTrainer epe = new TestErrorProbablityTrainer();
		long start = System.nanoTime();
		epe.test();
		long end = System.nanoTime();
		long elapsed = end - start;
		showElapsed(elapsed, "");
	}

	/**
	 * Debug function to display the total time elapsed.
	 * 
	 * @param elapsed Elapsed time in nano seconds.
	 * 
	 * @param prefix A debug prefix to print with the elapsed duration.
	 */
	private static void showElapsed(long elapsed, String prefix) {

		long sec = elapsed/1000000000L;
		long min = sec / 60;
		sec -= min * 60;
		System.err.printf("%s%d min %d sec %d ms\n", prefix, min, sec, (elapsed - sec * 1000000000L)/1000000);
	}
	
	private void test() {
		ErrorProbablityTrainer epe = new ErrorProbablityTrainer();
		ErrorProbabilityModel epModel = epe.train("/cs276/PA2/data/edit1s.txt");
		
		for(EditType type : EditType.values()) {
			System.err.printf("%s probablities\n", type);
			for(char x : ALPHABET) {
				System.err.printf("\t%c", x);
			}
			System.err.println();
			for(char x : ALPHABET) {
				System.err.print(x);
				for(char y : ALPHABET) {
					System.err.printf("\t%.2f", 10000 * epModel.getChannelProbability(new Edit(type, x, y)));
				}
				System.err.println();
			}
			System.err.println();
		}
	}
	
}
