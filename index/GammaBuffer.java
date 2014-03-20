package cs276.programming.index;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Class to holder a buffer to handle Gamma encoded files.
 *
 */
public class GammaBuffer extends BitVector {
		
		private static final int[] TEST_INTS = new int[]{1,2,3,4,5,6,7,8,9,10,13,24,511,1025};
		private RandomAccessFile filestore = null;
		private boolean readMode = false;
		
		public GammaBuffer() {
		}
		
		public GammaBuffer(RandomAccessFile raf, boolean isReader) {
			filestore = raf;
			readMode = isReader;
			handleUnderflow();
		}

		@Override
		protected void handleOverflow() {
			if(readMode) {
//				init();
				return;
			}
			flush();
		}
		
		@Override
		public void handleUnderflow() {
			if(!readMode) return;
			//read and add bytes
			try {
				byte[] ba = new byte[(63- (readPos -writePos))/8];
				int retVal = filestore.read(ba);
				if(retVal != -1) {
					for(byte b : ba) {
						this.setNextBits(b);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		/**
		 * In the write mode (where a file for storage is present), this method
		 * will flush as many bits as possible into the file. 
		 */
		public void flush() {
			if(readMode) return;
			try {
				while(getSize() >= 8) {
					filestore.writeByte(readByte());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		/**
		 * In the write mode (where a file for storage is present), this method
		 * will flush all the bits to the file. This must be invoked just before
		 * closing the file. 
		 */
		public void flushAll() {
			if(readMode) return;
			try {
				flush();
				if(getSize() > 0) {
					filestore.writeByte((byte)(bitstore>>>(64-8)));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		/**
		 * Helper method to store the length in unary code for gamma encoding.
		 * @param len
		 */
		private void appendGammaLength(int len) {
			for(int i = 0; i < len; i++) {
				setNextBit1();
			}
			setNextBit0();
		}
		
		/**
		 * store the specified integer in gamma encoding.
		 * @param value
		 */
		public void appendGamma(int value) {
			if(value == 0) return;
			
			short bitpos = 4 * 8 - 1;
			short bit = 0;
			while(bitpos >= 0 && bit == 0) { //skip initial zeros
				bit = (short) (value>>>bitpos);
				bit = (short) (bit - ((bit>>>1)<<1));
				bitpos--;
			}
			
			appendGammaLength(bitpos+1);
			
			//set the offset
			while(bitpos >= 0) {
				bit = (short) (value>>>bitpos);
				bit = (short) (bit - ((bit>>>1)<<1));
				if(bit == 0) {
					setNextBit0();
				} else {
					setNextBit1();
				}
				bitpos--; //skip the first '1' bit
			}
		}
		
		public int readNextInt() {
			short len = 0;
			handleUnderflow();
			if((readPos - writePos) < len) { //still has underflow
				return -1;
			}
			
			while(getNextBit() != 0) {
				len++;
			}
			if(len == 0) return 1;
			
			if((readPos - writePos) < len) { //TODO what it len > 64
				handleUnderflow();
			}
			int size = writePos > (63-len) ? (writePos) : (63-len+1);
			int b = (int) (bitstore>>>size) + (1<<len);
			bitstore = (bitstore<<(64-size));
			writePos += 64-size;
			return b;

		}
		
		/**
		 * Using the main method to conduct unit tests on this class.
		 * @param args
		 */
		public static void main(String[] args) {
			//code to test the GammaBuffer
			
			GammaBuffer gb = new GammaBuffer();
			gb.init();
			gb.appendGamma(1891);
			gb.printBits();
			
			gb.init();
//			gb.appendGamma(2);
			gb.set("11100011101010101111101101111011");
			while(true) {
				int i = gb.readNextInt();
				if(i == -1) break;
				System.err.print(i + " ");
			}
			gb.printBits();
			
			gb.init();
			gb.appendGamma(9);
			gb.appendGamma(6);
			gb.appendGamma(3);
			gb.appendGamma(1);
			gb.appendGamma(59);
			gb.appendGamma(7);
			gb.printBits();
			

			gb.init();
			gb.appendGamma(1);
			assert gb.readByte() == (byte) 0;
			
			gb.init();
			gb.appendGamma(2); //100
			assert gb.readByte() == (byte) 4;
			
			gb.init();
			gb.appendGamma(3); //101
			assert gb.readByte() == (byte) 5;
			
			gb.init();
			gb.appendGamma(4); //11000
			assert gb.readByte() == (byte) 24;
			
			gb.init();
			gb.appendGamma(9); //1110,001
			assert gb.readByte() == (byte) 113;
			
			gb.init();
			gb.appendGamma(13); //1110,101
			assert gb.readByte() == (byte) 117;
			
			gb.init();
			gb.appendGamma(24); //11110,1000
			assert gb.readByte() == (byte) 244;
			assert gb.readByte() == (byte) 0;
			
			gb.init();
			gb.appendGamma(511); //11111111 0,11111111
			assert gb.readByte() == (byte) 255;
			assert gb.readByte() == (byte) 127;
			assert gb.readByte() == (byte) 1;
			
			for(int i : TEST_INTS) {
				gb.init();
				gb.appendGamma(i);
				assert gb.readNextInt() == i;
			}
			
			RandomAccessFile raf = null;
			RandomAccessFile raf2 = null;
			try {
				raf = new RandomAccessFile("temp123.temp", "rw");
				GammaBuffer gbWrite = new GammaBuffer(raf, false);
				for(int i : TEST_INTS) {
					gbWrite.appendGamma(i);
				}
				gbWrite.flushAll();
				raf.close();

				raf2 = new RandomAccessFile("temp123.temp", "r");
				GammaBuffer gbRead = new GammaBuffer(raf2, true);
				
				for(int i = 0; i < TEST_INTS.length; i++) {
					int j = gbRead.readNextInt();
					assert j == TEST_INTS[i];
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if(raf != null) raf.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					if(raf2 != null) raf2.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		private void set(String str) {
			init();
			for(int i = 0; i < str.length(); i++) {
				if(str.charAt(i) == '0') {
					setNextBit0();
				} else {
					setNextBit1();
				}
			}
		}
	}