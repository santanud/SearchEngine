package cs276.programming.ranking;

import java.util.ArrayList;
import java.util.List;

public class SmallestWindowUtils {

	public int getSmallestWindow(String query, String Document) {

		if (Document == null || Document.trim().equals(""))
			return Integer.MAX_VALUE;
		
		List<ArrayList<Integer>> list = GetHistPos(query, Document.replaceAll("[://?.+_=~&-'!()]", " ").replaceAll("%20", " ").replaceAll("%3A", " ").replaceAll("%", " ").replaceAll("\\s+", " ").toLowerCase());
		
		int NoOfTerms = list.size();
		String []  terms = query.split("\\s+");
		
		if (terms.length > NoOfTerms || NoOfTerms == 0)
			return Integer.MAX_VALUE;
		else
			return getSmallestWindow(query,list);
	}

	public int getSmallestWindow(String query, List<ArrayList<Integer>> bodyHit) {
		
		int NoOfTerms = bodyHit.size();
		String []  terms = query.split("\\s+");
		
		if (terms.length > NoOfTerms || NoOfTerms == 0)
			return Integer.MAX_VALUE;
		
		int[] index = new int[NoOfTerms];
		for (int i = 0; i < index.length; i++)
			index[i] = 0;

		int SmallestWindows = Integer.MAX_VALUE;
		int Window = 0;

		int MinIndex = 0;
		while (hasMore(index, bodyHit)) {

			Window = GetSmllestWindow(index, bodyHit);
		//	System.out.println("Window=" + Window);

			if (Window < SmallestWindows) {
				SmallestWindows = Window;
			}

			MinIndex = GetSmallestIndex(index, bodyHit);
//			System.out.println("MinIndex=" + MinIndex);
			index[MinIndex] += 1;

		}
		return SmallestWindows;

	}

	private int GetSmallestIndex(int[] index,
			List<ArrayList<Integer>> bodyHit) {
		int SmallestIndex = 0;
		int SmallestValue = Integer.MAX_VALUE;
		// first get the smallest value
		for (int i = 0; i < index.length; i++)
			if (bodyHit.get(i).get(index[i]) < SmallestValue) {
				SmallestValue = bodyHit.get(i).get(index[i]);
				SmallestIndex = i;
			}

		return SmallestIndex;
	}

	private  int GetSmllestWindow(int[] index,
			List<ArrayList<Integer>> bodyHit) {

		int SmallestValue = Integer.MAX_VALUE;
		int LargestValue = 0;
		// first get the smallest value
		for (int i = 0; i < index.length; i++)
			if (bodyHit.get(i).get(index[i]) < SmallestValue)
				SmallestValue = bodyHit.get(i).get(index[i]);

		// then get largest value

		for (int i = 0; i < index.length; i++)
			if (bodyHit.get(i).get(index[i]) > LargestValue)
				LargestValue = bodyHit.get(i).get(index[i]);

		// return largest - smallest
//		System.out.println("LargestValue=" + LargestValue + " SmallestValue="
	//			+ SmallestValue);
		return (LargestValue - SmallestValue + 1);
	}

	private  boolean hasMore(int[] index, List<ArrayList<Integer>> bodyHit) {
		// Check if any of the list has come to end
		boolean flag = true;
		for (int i = 0; i < index.length; i++) {
//			System.out.println("i=" + i + " bodyHit.get(i).size()="
//					+ bodyHit.get(i).size() + " index i= " + index[i]);

			if (bodyHit.get(i).size() <= index[i]) {
				flag = false;
				break;
			}
		}

		return flag;
	}

	private  List<ArrayList<Integer>> GetHistPos(String query,
			String Document) {

		List<ArrayList<Integer>> ret = new ArrayList<ArrayList<Integer>>();
		
		if (Document == null || Document.equals(""))
			return ret;
		String[] terms = query.split("\\s+");

		String[] dterms;

		ArrayList<Integer> li;
		int pos = 1;
		for (String term : terms) {
			li = new ArrayList<Integer>();
			dterms = Document.split("\\s+");
			pos = 1;
			for (String s : dterms) {
				if (s.equals(term))
					li.add(pos);
				pos++;
			}
			ret.add(li);
		}

		return ret;

	}
	public static void main(String[] args) {

		SmallestWindowUtils SWutil = new SmallestWindowUtils();
		List<ArrayList<Integer>> bodyHit = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> A = new ArrayList<Integer>();
		ArrayList<Integer> B = new ArrayList<Integer>();

		A.add(23);
		A.add(44);
		A.add(92);
		A.add(159);
		A.add(165);

		B.add(97);
		B.add(118);

		bodyHit.add(A);
		bodyHit.add(B);

		int sw = SWutil.getSmallestWindow("query",bodyHit);
		System.out.println("Smallest Window : "+sw);
		
		
		int i = SWutil.getSmallestWindow("a b","s b t y p");
		if (i == Integer.MAX_VALUE)
			System.out.println("NO SM Found : "+ i);
		else
			System.out.println("Smallest Window : "+i);
	}

}