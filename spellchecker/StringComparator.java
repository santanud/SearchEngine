package cs276.programming.spellchecker;

import java.util.ArrayList;
import java.util.List;

import cs276.programming.spellchecker.Edit.EditType;
import cs276.programming.util.Matrix;

/**
 * Utility class the compare two strings to determine the
 * edit distance between them.
 *
 */
public class StringComparator {
	
	public static final char STR_BEGIN_CHAR = '~'; //This should not be part of the Alphabet.
	
	public StringComparator() {}
	
	public int determineEditDistance(String rawQuery, String correctedQuery) {
		return determineEditDistanceDynamic(rawQuery, correctedQuery);
	}

	public List<Edit> determineEdits(String rawQuery, String correctedQuery) {
		return determineEditsSimpleWalkRecursive(rawQuery, correctedQuery, 0, 0);
	}

	/**
	 * Determines the edit distance between two strings
	 * using the dynamic programming algorithm described
	 * in IIR.
	 * 
	 * @param rawQuery
	 * @param correctedQuery
	 * @return
	 */
	public int determineEditDistanceDynamic(String rawQuery, String correctedQuery) {
		
		int iMax = correctedQuery.length();
		int jMax = rawQuery.length();
		Matrix D = new Matrix(iMax+1, jMax+1);
		D.set(0, 0, 0);
		for(int i = 1; i <= iMax; i++) {
			D.set(i, 0, D.get(i-1, 0) + getCost(new Edit(EditType.DELETE,  i == 1 ? STR_BEGIN_CHAR : correctedQuery.charAt(i-2), correctedQuery.charAt(i-1), i-1)));
		}
		for(int j = 1; j <= jMax; j++) {
			D.set(0, j, D.get(0, j-1) + getCost(new Edit(EditType.INSERT,  j == 1 ? STR_BEGIN_CHAR : rawQuery.charAt(j-2), rawQuery.charAt(j-1), 0)));
		}
		for(int i = 1; i <= iMax; i++) {
			for(int j = 1; j <= jMax; j++) {
				int delValue = D.get(i-1, j) + getCost(new Edit(EditType.DELETE,  i == 1 ? STR_BEGIN_CHAR : correctedQuery.charAt(i-2), correctedQuery.charAt(i-1), i-1));
				int insValue = D.get(i, j-1) + getCost(new Edit(EditType.INSERT,  j == 1 ? STR_BEGIN_CHAR : rawQuery.charAt(j-2), rawQuery.charAt(j-1), i));
				int subValue = D.get(i-1, j-1) + (rawQuery.charAt(j-1) != correctedQuery.charAt(i-1) ? getCost(new Edit(EditType.SUBSTITUTE,  correctedQuery.charAt(i-1), rawQuery.charAt(j-1), i-1)) : 0);
				
				int value = min(delValue, insValue, subValue);
				if(i >= 2 && j >= 2 &&
					rawQuery.charAt(j-1) == correctedQuery.charAt(i-2) &&
					rawQuery.charAt(j-2) == correctedQuery.charAt(i-1)) {
					int transValue = D.get(i-2, j-2) + 
							getCost(new Edit(EditType.TRANSPOSE,  correctedQuery.charAt(i-2), correctedQuery.charAt(i-1), i-2));
					value = min(value, transValue);
				}
				
				D.set(i, j, value);
			}
		}
//		System.err.println(rawQuery + "\t" + correctedQuery);
//		System.err.println(D);
//		System.err.println();
		return D.get(iMax, jMax);
	}
	
	/**
	 * Returns the cost associated with the 
	 * specific edit operation.
	 * 
	 * @param e
	 * @return
	 */
	private int getCost(Edit e) {
		switch(e.getType()) {
		case DELETE:
			return 1;
		case INSERT:
			return 1;
		case SUBSTITUTE:
			return e.getX() == e.getY() ? 0 : 1;
		case TRANSPOSE:
			return e.getX() == e.getY() ? 0 : 1;
		}
		return 1; //should not fall here, but default to 1
	}
	
	/**
	 * Returns the cost associated with a 
	 * series of edits.
	 * 
	 * @param edits
	 * @return
	 */
	private int getCost(List<Edit> edits) {

		int cost = 0;
		for(Edit e : edits) {
			cost += getCost(e);
		}
		return cost;
	}

	/**
	 * Helper method to return the minimun amongst
	 * a group of integer values.
	 * 
	 * @param values
	 * @return
	 */
	private int min(int... values) {
		int min = Integer.MAX_VALUE;
		for(int i : values) {
			if(i < min) min = i;
		}
		return min;
	}

	private int determineEditDistanceSimpleWalkRecursive(String rawQuery, String correctedQuery) {
		List<Edit> edits = determineEditsSimpleWalkRecursive(rawQuery, correctedQuery, 0, 0);
		return edits.size();
		
	}
	
	private List<Edit> determineEditsSimpleWalkRecursive(String rawQuery, String correctedQuery, final int rawIndex, final int correctedIndex) {
		//Using a simple method to walk (with recursion) through the string to determine the edits
		//recurse through the various edit paths & surface the minimum
		//if only one string has chars remaining
		if(rawIndex < rawQuery.length() && correctedIndex == correctedQuery.length()) {
			List<Edit> edits = new ArrayList<Edit>();
			int localRawIndex = rawIndex;
			//insertion
			while(localRawIndex < rawQuery.length()) {
				char x = localRawIndex > 0 ? rawQuery.charAt(localRawIndex-1) : STR_BEGIN_CHAR;
				edits.add(new Edit(EditType.INSERT, x, rawQuery.charAt(localRawIndex), correctedIndex > 0 ? correctedIndex-1 : 0)); //multiple inserts at the same location in corr string
				localRawIndex++;
			}
			return edits;
		}

		if(rawIndex == rawQuery.length() && correctedIndex < correctedQuery.length()) {
			List<Edit> edits = new ArrayList<Edit>();
			int localCorrectedIndex = correctedIndex;
			//deletion
			while(localCorrectedIndex < correctedQuery.length()) {
				char x = localCorrectedIndex > 0 ? correctedQuery.charAt(localCorrectedIndex-1) : STR_BEGIN_CHAR;
				edits.add(new Edit(EditType.DELETE, x, correctedQuery.charAt(localCorrectedIndex), localCorrectedIndex));
				localCorrectedIndex++;
			}
			return edits;
		}
			
		List<List<Edit>> candidates = new ArrayList<List<Edit>>();

		//skip while same
		if(rawIndex < rawQuery.length() && correctedIndex < correctedQuery.length() &&
				rawQuery.charAt(rawIndex) == correctedQuery.charAt(correctedIndex)) {
			int localRawIndex = rawIndex;
			int localCorrectedIndex = correctedIndex;
			while(localRawIndex < rawQuery.length() && localCorrectedIndex < correctedQuery.length() &&
				rawQuery.charAt(localRawIndex) == correctedQuery.charAt(localCorrectedIndex)) {
				localRawIndex++;
				localCorrectedIndex++;
			}
			List<Edit> edits = new ArrayList<Edit>();
			edits.addAll(determineEditsSimpleWalkRecursive(rawQuery, correctedQuery, localRawIndex, localCorrectedIndex));
			return edits;
		}
		//transposition
		if(rawIndex+1 < rawQuery.length() && correctedIndex+1 < correctedQuery.length() &&
				rawQuery.charAt(rawIndex) == correctedQuery.charAt(correctedIndex+1) && 
				rawQuery.charAt(rawIndex+1) == correctedQuery.charAt(correctedIndex)) {
			
			List<Edit> edits = new ArrayList<Edit>();
			edits.add(new Edit(EditType.TRANSPOSE, rawQuery.charAt(rawIndex+1), rawQuery.charAt(rawIndex), correctedIndex));
			edits.addAll(determineEditsSimpleWalkRecursive(rawQuery, correctedQuery, rawIndex+2, correctedIndex+2));
			candidates.add(edits);
		}
		//insertion
		if(rawIndex+1 < rawQuery.length() && correctedIndex < correctedQuery.length() &&
				(rawQuery.charAt(rawIndex+1) == correctedQuery.charAt(correctedIndex)) ) {
			List<Edit> edits = new ArrayList<Edit>();
			char x = rawIndex > 0 ? rawQuery.charAt(rawIndex-1) : STR_BEGIN_CHAR;
			edits.add(new Edit(EditType.INSERT, x, rawQuery.charAt(rawIndex), correctedIndex));
//			System.err.printf("%s(%d) %s(%d)\n", rawQuery, rawIndex, correctedQuery, correctedIndex);
			edits.addAll(determineEditsSimpleWalkRecursive(rawQuery, correctedQuery, rawIndex+2, correctedIndex+1));
			candidates.add(edits);
		}
		//deletion
		if(rawIndex < rawQuery.length() && correctedIndex+1 < correctedQuery.length() &&
				(rawQuery.charAt(rawIndex) == correctedQuery.charAt(correctedIndex+1)  )) {
			List<Edit> edits = new ArrayList<Edit>();
			char x = correctedIndex > 0 ? correctedQuery.charAt(correctedIndex-1) : STR_BEGIN_CHAR;
			edits.add(new Edit(EditType.DELETE, x, correctedQuery.charAt(correctedIndex), correctedIndex));
//			System.err.printf("%s(%d) %s(%d)\n", rawQuery, rawIndex, correctedQuery, correctedIndex);
			edits.addAll(determineEditsSimpleWalkRecursive(rawQuery, correctedQuery, rawIndex+1, correctedIndex+2));
			candidates.add(edits);
		}
		//substitution
		if(rawIndex+1 < rawQuery.length() && correctedIndex+1 < correctedQuery.length() &&
				 (rawQuery.charAt(rawIndex+1) == correctedQuery.charAt(correctedIndex+1))  ) { //what about adjacent errors?
			List<Edit> edits = new ArrayList<Edit>();
			edits.add(new Edit(EditType.SUBSTITUTE, correctedQuery.charAt(correctedIndex), rawQuery.charAt(rawIndex), correctedIndex));
			edits.addAll(determineEditsSimpleWalkRecursive(rawQuery, correctedQuery, rawIndex+1, correctedIndex+1));
			candidates.add(edits);
		}
		
		//fall through
		if(rawIndex < rawQuery.length() && correctedIndex < correctedQuery.length() && candidates.size() == 0) {
			List<Edit> edits = new ArrayList<Edit>();
			edits.add(new Edit(EditType.SUBSTITUTE, correctedQuery.charAt(correctedIndex), rawQuery.charAt(rawIndex), correctedIndex));
			edits.addAll(determineEditsSimpleWalkRecursive(rawQuery, correctedQuery, rawIndex+1, correctedIndex+1));
			candidates.add(edits);
		}
		
		//find best candidate. Draw arbitrarily for equal cost candidates
		List<Edit> edits = getBestCandidate(candidates);
		return edits;
	}

	/**
	 * Compare the list of edit candidates and select the best
	 * amongst them based on the selected cost model.
	 * @param candidates
	 * @return
	 */
	private List<Edit> getBestCandidate(List<List<Edit>> candidates) {
		int min = Integer.MAX_VALUE;
		List<Edit> edits = new ArrayList<Edit>();
		for(List<Edit> candidate : candidates) {
			int candidateCost = getCost(candidate);
			if(candidateCost < min) {
				min = candidateCost;
				edits = candidate;
			}
		}
		return edits;
	}
	
}
