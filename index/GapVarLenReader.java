package cs276.programming.index;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import cs276.programming.util.Parameters;
import cs276.programming.util.TermStat;

class GapVarLenReader implements PostingReader {

		private RandomAccessFile raf = null;
		private BufferedReader br = null;

		public GapVarLenReader() {
		}
		
		public GapVarLenReader(File file) {
			try {
				File postingDictFile = new File(file.getAbsoluteFile() + ".pos");
				if(!postingDictFile.exists()) {
					postingDictFile = new File(file.getParentFile(), Parameters.POSTING_DICT_FILENAME);
				}
				br = new BufferedReader(new FileReader(postingDictFile));
				raf = new RandomAccessFile(file, "r");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public TermStat getPostingLineStats(String line) {
			//This cannot be determined!!
			return null;
		}

//		private int varDecode(byte[] ba) {
//			int pos = 0;
//			int value = 0;
//			while(pos < ba.length && ((ba[pos] & (1<<7)) == 0) ) {
//				value = (value <<7) + ba[pos++];
//			}
//			value = (value <<7) + (ba[pos] & ((1 << 7) - 1));
//			return value;
//		}
		
		private int getNextInt() {
			try {
				int value = 0;
				byte nextByte = raf.readByte();
				while( ((nextByte & (1<<7)) == 0) ) {
					value = (value <<7) + nextByte;
					nextByte = raf.readByte();
				}
				value = (value <<7) + (nextByte & ((1 << 7) - 1));
				return value;
			} catch(EOFException e) { 
				//this is ok!
			} catch (IOException e) {
				e.printStackTrace();
			}
			return -1;
		}


		@Override
		public PostingLine getNextPosting() {
			
			try {
				String line = br.readLine();
				if(line != null) {
					TermStat termStat = TermStat.parseTermStat(line);
					seekPosition(termStat);
					
					int termID = getNextInt();
					int[] docIDs = new int[termStat.getFreq()];
					int prevDocID = 0;
					for(int i = 0; i < termStat.getFreq(); i++) {
						docIDs[i] = getNextInt() + prevDocID;
						prevDocID = docIDs[i];
					}
					return new PostingLine(termID, docIDs);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

		private void seekPosition(TermStat termStat) throws IOException {
			raf.seek(termStat.getfilePos());
		}

		@Override
		public void close() {
			
			try {
				if(br != null) br.close();
				if(raf != null) raf.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public ArrayList<Integer> getDocidList(TermStat termStat) {

			ArrayList<Integer> li = new ArrayList<Integer>();
			try {
				seekPosition(termStat);
				int termID = getNextInt();
				int prevDocID = 0;
				for(int i = 0; i < termStat.getFreq(); i++) {
					int currDocID = getNextInt() + prevDocID;
					li.add(currDocID);
					prevDocID = currDocID;
				}
				return li;
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
	}