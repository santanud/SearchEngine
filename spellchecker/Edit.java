package cs276.programming.spellchecker;

import java.io.Serializable;

/**
 * An immutable class that identifies
 * the edit performed on a string.
 *
 */
public class Edit implements Serializable {

	private static final long serialVersionUID = 5476172302633383307L;

	/**
	 * An enumeration of the various kinds of edits.
	 * 
	 * Insert xy     => x  typed as xy
	 * Delete xy     => xy typed as x
	 * Substitute xy => y  typed as x
	 * Transpose xy  => xy typed as yx
	 */
	public enum EditType {
		INSERT, DELETE, SUBSTITUTE, TRANSPOSE;
	}
	
	private final EditType type;
	private final char x;
	private final char y;
	
	private final int position;
	
	public Edit(EditType type, char x, char y) {
		this.type = type;
		this.x = x;
		this.y = y;
		position = -1; //unknown
	}
	
	public Edit(EditType type, char x, char y, int position) {
		this.type = type;
		this.x = x;
		this.y = y;
		this.position = position;
	}
	
	@Override
	public String toString() {
		return "(" + getType() + ", " + getX() + ", " + getY() + ")";
	}
	
	@Override
	public boolean equals(Object obj) {
		
		if(this == obj) return true;
		if(!(obj instanceof Edit)) return false;
		Edit e = (Edit) obj;
		
		return e.type == this.type && e.x == this.x && e.y == this.y;
	}
	
	@Override
	public int hashCode() {
		int hash = 17 + getType().ordinal();
		hash = 31 * hash + (int) getX();
		hash = 31 * hash + (int) getY();
		return hash;
	}

	public EditType getType() {
		return type;
	}

	public char getX() {
		return x;
	}

	public char getY() {
		return y;
	}

	public int getPosition() {
		return position;
	}
}