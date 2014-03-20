package cs276.programming.query;

import java.util.ArrayList;
import java.util.Iterator;

import cs276.programming.util.Parameters;
import cs276.programming.util.TermStat;

public class QueryProcessor {

	/**
	 * @param args
	 */
	private RuntimeIndex RT;

	public QueryProcessor(RuntimeIndex rtime) {
		RT = rtime;
	}

	public ArrayList<String> runQuery(String queryString) {
		// TODO
		ArrayList<String> docList = new ArrayList<String>();
		if (queryString == null || queryString.trim() == "") {
			return docList;
		}
		// get the terms one by one and their document counts
		ArrayList<Integer> mergedList = new ArrayList<Integer>();

		String[] terms = TextProcessor.Tokenizer(queryString);

		// Sort the terms by Document Frequency

		if (Parameters.DEBUG) {
			System.err.println("# of terms in " + queryString + " = "
					+ terms.length);
		}

		ArrayList<TermStat> TermS = getSortedTermId(terms);
		if (TermS.size() ==0){
			// one or more of the terms dont exists in dictionary
			// return an empty list
			return docList;
		}
		
		
		
		for (int i = 0; i <= TermS.size() - 1; i++) {

			if (i == 0) {
				// first term
				ArrayList<Integer> postingList = RT.getPostingList(TermS.get(i)
						.getTermId());
				mergedList = postingList;
			} else {

				ArrayList<Integer> postingList = RT.getPostingList(TermS.get(i)
						.getTermId());

				mergedList = Insersect(mergedList, postingList);
			}
		}
		DocDict doc = RT.getDocDict();

		if (Parameters.DEBUG) {
			System.err.println("List of merged documents : ");
		}
		for (Integer i1 : mergedList) {
			docList.add(doc.getDoc(i1));

			if (Parameters.DEBUG) {
				System.err.println("Merged list Doc id : " + i1
						+ " Document name : " + doc.getDoc(i1));
			}

		}
		return docList;
	}

	private ArrayList<TermStat> getSortedTermId(String[] terms) {
		// TODO Auto-generated method stub
		ArrayList<TermStat> sortedTermIds = new ArrayList<TermStat>();
		TermStat t;
		for (int i = 0; i <= terms.length - 1; i++) {

			int termId = RT.getWordDict().getTermId(terms[i]);
			if (termId == -1) {
				// Term is not in doctionary
				// No need to move further
				// return an empty list
				sortedTermIds = new ArrayList<TermStat>();
				return sortedTermIds;
			}
			else {
				TermStat ts = RT.getPostingDict().getTermStat(termId);
				int pointer = 0;
				Iterator It = sortedTermIds.iterator();

				while (It.hasNext()) {
					TermStat s = (TermStat) It.next();
					if (s.getFreq() < ts.getFreq()) {
						break;
					}
					pointer++;
				}
				sortedTermIds.add(pointer, ts);
			}
		}
		return sortedTermIds;
	}

	ArrayList<Integer> Insersect(ArrayList<Integer> list1,
			ArrayList<Integer> list2) {

		
		
		ArrayList<Integer> mergedList = new ArrayList<Integer>();

		if(list1.size() < 1 || list2.size() < 1){
			return mergedList;
		}
		int i1 =0;
		int i2 = 0;
		int val1 =0;
		int val2 =0;
		int counter=0;
		while (i1 < list1.size() && i2 < list2.size()) {
				counter++;
				val1 = list1.get(i1);
				val2 = list2.get(i2);
				
				if (val1  == val2){ 
					mergedList.add(val1);
					i1++;
					i2++;
					
				} else if (val1 > val2) {
					// i2 is smaller, so move further with
					i2++;

				} else {
					// i1 < i2
					i1++;
				}
		}		
		return mergedList;
	}

	public static void main(String[] args) {
		String IndexDir = "/Users/sdey/Documents/sdey_personal/apps/workspace/CS276/tempindex"; // comes
		Parameters.DEBUG = true;
		Parameters.IndexType IType = Parameters.IndexType.NO_COMPRESSION;

		RuntimeIndex Rtime = RuntimeIndex.getInstance(IndexDir,IType);
		QueryProcessor QProcessor = new QueryProcessor(Rtime);

		ArrayList<Integer> list1 = new ArrayList<Integer>();
		ArrayList<Integer> list2 = new ArrayList<Integer>();
		list1.add(1);
		list1.add(3);
		list1.add(5);
		list1.add(8);
		
		list2.add(1);
		list2.add(5);
		list2.add(8);
		
		ArrayList<Integer> li = QProcessor.Insersect(list1, list2);
		
		for(Integer i : li){
			System.out.println(i);
			
		}
		System.out.println("END");

	}

}
