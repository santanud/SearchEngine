package cs276.programming.ranking;

import java.util.Map;

/**
 * The different types of normalization
 * as described in Fig 6.15 or IIR. 
 */
public enum NormalizationType {
	//1
	NONE {
		@Override
		public double normalize(double value, Map<String, Double> params) {
			return value;
		}
	},
	
	//1/sqrt(sumsq(w_i))
	COSINE {
		//We cannot use cosine normalization as we do not have access to the contents of the document
		@Override
		public double normalize(double value, Map<String, Double> params) {
			throw new UnsupportedOperationException("Cosine normalization not supported.");
		}
	}, 
	
	//1/u
	PIVOTEDUNIQUE {
		@Override
		public double normalize(double value, Map<String, Double> params) {
			throw new UnsupportedOperationException("Pivoted-Unique normalization not supported.");
		}
	},
	
	//1/CharLength^a a < 1
	BYTESIZE {
		@Override
		public double normalize(double value, Map<String, Double> params) {
			throw new UnsupportedOperationException("Bytesize normalization not supported.");
		}
	},
	
	//This one is from PA3
	
	//1/(body_length + k) k is a constant (e.g. 500)
	LENGTH {
		@Override
		public double normalize(double value, Map<String, Double> params) {
			return value / ( params.get("body_length") + params.get("bodyLenSmoothingFactor") );
		}
	},
	
	//1 / (1 + B_f ( (len_d,f / avlen_f) - 1)
	BM25F {
		@Override
		public double normalize(double value, Map<String, Double> params) {
			return params.get("len_d,f") == 0 ? 0 : value / ( 1 + params.get("B_f") * ((params.get("len_d,f")/params.get("avlen_f")) - 1) );
		}
	};
	
	/**
	 * Normalize the value according to the specified type.
	 */
	public double normalize(double value, Map<String, Double> params) {
		throw new UnsupportedOperationException("Default normalization not supported.");
	}
}