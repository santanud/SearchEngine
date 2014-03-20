package cs276.programming.util;

import java.util.ArrayList;

public class TFIDFUtil {

	public static ArrayList<Double> getSublinearScaleTF(ArrayList<Double> rawTF) {
		// tfi = 1 + log(rsi) if rsi > 0
		ArrayList<Double> scaleTF = new ArrayList<Double>();
		double tf;
		for (Double d : rawTF) {
			if (IsZero(d)) {
				tf = 0;
			} else {
				tf = 1 + Math.log(d);
			}
			scaleTF.add(tf);
		}
		return scaleTF;
	}

	public static boolean IsZero(double d) {

		double EPSILON = 1E-5;

		if (d < EPSILON && d > (-1) * EPSILON)
			return true;
		else
			return false;

	}

}
