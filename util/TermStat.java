package cs276.programming.util;

/**
 * Class to hold an entry from the posting dict
 * for a specific term.
 *
 */
public final class TermStat { //made this class immutable

	//the location on the corpus index where the entry for this term is stored.
	private final long filePos; //Changed this to long, as RandomAccessFile::getFilePointer returns a long.
	
	//the actual bit offset from the above byte boundary location where this data begins.
	private final short bitOffset; //Used only for gamma encoding
	
	//The doc frequency for this term
	private final int Freq;
	
	//An identifier for this term.
	private int TermId;

	public TermStat(int term, int fpos, int frequency) {
		filePos = fpos;
		Freq = frequency;
		TermId = term;
		bitOffset = 0;
	}

	public TermStat(int term, int fpos, short bitOffset, int frequency) {
		filePos = fpos;
		this.bitOffset = bitOffset;
		Freq = frequency;
		TermId = term;
	}

	public long getfilePos() {
		return filePos;
	}

	public int getFreq() {
		return Freq;
	}
	public int getTermId() {
		return TermId;
	}

	public short getBitOffset() {
		return bitOffset;
	}
	
	public static TermStat parseTermStat(String line) {
		String[] fields = line.split("\t");
		if(fields.length == 4) {
			int wordid = Integer.parseInt(fields[0]);
			int filePos = Integer.parseInt(fields[1]);
			short bitOff = Short.parseShort(fields[2]);
			int freq = Integer.parseInt(fields[3]);
			return new TermStat(wordid, filePos, bitOff, freq);
		} else {
			int wordid = Integer.parseInt(fields[0]);
			int filePos = Integer.parseInt(fields[1]);
			int freq = Integer.parseInt(fields[2]);
			return new TermStat(wordid, filePos, freq);
		}
	}
}
