package cs276.programming.util;

import java.util.ArrayList;
import java.util.List;

public class VectorUtil {

	public static double dot(List<Double> v1, List<Double> v2) {
		if(v1 == null || v2 == null || v1.size() != v2.size()) {
			throw new IllegalArgumentException("Cannot do dot product with different vector sizes.");
		}
		double prd = 0.0;
		for(int i = 0; i < v1.size(); i++) {
			prd += v1.get(i) * v2.get(i);
		}
		return prd;
	}
	
	public static List<Double> scalarMult(List<Double> vect, double scalar) {
		List<Double> product = new ArrayList<Double>();
		for(int i = 0; i < vect.size(); i++) {
			product.add(scalar * vect.get(i));
		}
		return product;
	}
	
	public static List<Double> add(List<Double> v1, List<Double> v2) {
		if(v1 == null || v2 == null || v1.size() != v2.size()) {
			throw new IllegalArgumentException("Cannot do add with different vector sizes.");
		}
		
		List<Double> sum = new ArrayList<Double>();
		for(int i = 0; i < v1.size(); i++) {
			sum.add(v1.get(i) + v2.get(i));
		}
		return sum;
	}
	
	public static List<Double> subtract(List<Double> v1, List<Double> v2) {
		if(v1 == null || v2 == null || v1.size() != v2.size()) {
			throw new IllegalArgumentException("Cannot do add with different vector sizes.");
		}
		
		List<Double> diff = new ArrayList<Double>();
		for(int i = 0; i < v1.size(); i++) {
			diff.add(v1.get(i) - v2.get(i));
		}
		return diff;
	}
}
