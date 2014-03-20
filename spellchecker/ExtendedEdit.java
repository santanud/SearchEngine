package cs276.programming.spellchecker;

import java.io.Serializable;

/**
 * Class to hold an extended concept of an edit
 * where a string is replaced by another string - instead
 * of a character. For e.g., "ent" is mistyped as
 * "ant" at position 10. 
 *
 */
public class ExtendedEdit implements Serializable {
	
	private static final long serialVersionUID = 2217660167487563744L;
	
	private final String x;
	private final String y;
	
	private final int position;
	
	public ExtendedEdit(String x, String y) {
		this.x = x;
		this.y = y;
		position = -1; //unknown
	}
	
	public ExtendedEdit(String x, String y, int position) {
		this.x = x;
		this.y = y;
		this.position = position;
	}
	
	@Override
	public String toString() {
		return "(" + getX() + ", " + getY() + ")";
	}
	
	@Override
	public boolean equals(Object obj) {
		
		if(this == obj) return true;
		if(!(obj instanceof ExtendedEdit)) return false;
		ExtendedEdit e = (ExtendedEdit) obj;
		
		return e.x == this.x && e.y == this.y;
	}
	
	@Override
	public int hashCode() {
		int hash = 17 + x.hashCode();
		hash = 31 * hash + y.hashCode();
		return hash;
	}

	public String getX() {
		return x;
	}

	public String getY() {
		return y;
	}

	public int getPosition() {
		return position;
	}
}
