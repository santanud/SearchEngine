package cs276.programming.index;

/**
 * Class to hold a sequence of bits representing encoded
 * numbers.
 *
 */
public class BitVector {
	
	//storage for the sequence of bits
	protected long bitstore = 0;
	
	//the position (from left-hand side) until which bits have been written-to in the bit store.
	protected short writePos = 64-1;
	
	//the position until which the bits in the store have been read.
	protected short readPos = 64-1;
	
	/**
	 * initialize / reset the BitVector.
	 */
	public final void init() {
		bitstore = 0;
		writePos = 64-1;
		readPos = 64-1;
	}
	
	/**
	 * Print the bits stored in the BigVector for debugging purposes.
	 */
	public void printBits() {
		long b = bitstore;
		
		short bitpos = 8 * 8 - 1;
		while(bitpos >= 0) {
			short bit = (short) (b>>>bitpos);
			bit = (short) (bit - ((bit>>>1)<<1));
			System.err.print(bit);
			bitpos--;
		}
		System.err.println();
		if(writePos != 63) {
			for(int i = 0; i < 63-writePos; i++) {
				System.err.print(" ");
			}
			System.err.println("^");
		}

	}
	
	/**
	 * Set the next bit to 0.
	 */
	protected final void setNextBit0() {
		moveWritePosition();
	}

	/**
	 * Set the next bit to 1.
	 */
	protected final void setNextBit1() {
		bitstore += (long) (1L << writePos);
		moveWritePosition();
	}
	
	/**
	 * Move the current write position by 1.
	 */
	private void moveWritePosition() {
		writePos--;
		if(writePos == 0) {
			handleOverflow();
		}
	}

	/**
	 * Placeholder method handle buffer overflow. This will be
	 * implemented in the child classes. 
	 */
	protected void handleOverflow() {
		//process overflow as necessary
		//reset bitstore
		init(); //this is just a placeholder implementation. Subclasses must implement.
		
		//future - this method could even push the long to a queue
	}

	/**
	 * Set the next 8 bits in the vector according to
	 * the byte provided.
	 * @param b
	 */
	protected void setNextBits(byte b) {
		
		short bitpos = 8 * 1 - 1;
		while(bitpos >= 0) {
			short bit = (short) (b>>>bitpos);
			bit = (short) (bit - ((bit>>>1)<<1));
			if(bit == 0) {
				setNextBit0();
			} else {
				setNextBit1();
			}
			bitpos--;
		}
	}
	
	/**
	 * Read the next 8 bits from the BitVector and
	 * return that as a byte.
	 * 
	 * @return
	 */
	protected byte readByte() {
//		if(writePos > 64-8) {
		if((readPos - writePos) < 8) {
			handleUnderflow();
		}
		short size = writePos > (63-8) ? (short)(writePos+1) : (63-8+1);
		byte b = (byte)(bitstore>>>size);
		bitstore = (bitstore<<(64-size));
		writePos += 64-size;
		return b;
	}

	/**
	 * Placeholder method handle buffer undreflow. This will be
	 * implemented in the child classes. 
	 */
	protected void handleUnderflow() {
	}
	
	/**
	 * Return the number of bits in the currently stored
	 * BitVector.
	 * @return
	 */
	public int getSize() {
//		return 63-writePos;
		return readPos - writePos;
	}

	/**
	 * Read and return the next bit from the bit store.
	 * @return
	 */
	public short getNextBit() {
		short bit = (short) (bitstore>>>63);
		bitstore = ((bitstore-(bit>>63))<<(1));
		writePos++;
		return bit;
	}
	
	public static void main(String[] args) {
		//code to test the BitVector
		
		BitVector bv = new BitVector();
		
		assert bv.getSize() == 0;
		
		bv.init();
		bv.setNextBit0();
		assert bv.getNextBit() == 0;
		
		bv.init();
		bv.setNextBit1();
		assert bv.getNextBit() == 1;
		
		bv.init();
		bv.setNextBit0();
		bv.setNextBit0();
		bv.setNextBit1();
		assert bv.getNextBit() == 0;
		assert bv.getNextBit() == 0;
		assert bv.getNextBit() == 1;

		bv.init();
		bv.setNextBits((byte) 120); //0111 1000
		assert bv.getNextBit() == 0;
		assert bv.getNextBit() == 1;
		assert bv.getNextBit() == 1;
		assert bv.getNextBit() == 1;
		assert bv.getNextBit() == 1;
		assert bv.getNextBit() == 0;
		assert bv.getNextBit() == 0;
		assert bv.getNextBit() == 0;

		bv.init();
		bv.setNextBits((byte) 35); //0010 0011
		bv.setNextBits((byte) 72); //0100 1000
		assert bv.readByte() == (byte) 35;
		assert bv.readByte() == (byte) 72;
		
		bv.init();
		bv.setNextBit1();
		bv.setNextBit0();
		bv.setNextBit0();
		bv.setNextBit1();
		bv.setNextBit1();
		//10011
		assert bv.readByte() == (byte) 19;
	}
}
