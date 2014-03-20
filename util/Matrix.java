package cs276.programming.util;

/**
 * A simple class to hold a matrix of
 * integers.
 */
public class Matrix {
	
	private int entries[][];
	private int xSize = 0;
	private int ySize = 0;
	
	/**
	 * Creates a matrix of the specified size.
	 */
	public Matrix(int xsize, int ysize) {
		entries = new int[xsize][ysize];
		xSize = xsize;
		ySize = ysize;
	}

	/**
	 * Returns the integer value stored
	 * at the specified location.
	 * @return
	 */
	public int get(int i, int j) {
		return entries[i][j];
	}

	/**
	 * Sets the specified integer value
	 * to the specified location in the matrix.
	 */
	public void set(int i, int j, int value) {
		entries[i][j] = value;
	}
	
	@Override
	public String toString() {
		String str = "";
		
		for(int j = ySize - 1; j >= 0; j--) {
			for(int i = 0; i < xSize; i++) {
				str += entries[i][j] + "\t";
			}
			str += "\n";
		}
		return str;
	}
}