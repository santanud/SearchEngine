package cs276.programming.query;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

public class TextProcessor {

	/**
	 * @param args
	 */
	public static String[] Tokenizer(String str ){
		String[] rawTokens = str.split(" ");
		
		Set<String> set = new TreeSet();
		for (int i = 0; i <= rawTokens.length - 1; i++) {
			//System.out.println("rawtokens:\""+i+";"+rawTokens[i]+"\"");
			if (rawTokens[i].trim() != "" && !rawTokens[i].isEmpty() ){
				set.add(rawTokens[i]);
				//System.out.println("Added"+rawTokens[i].charAt(0));
			}
		}
		
		Iterator it = set.iterator();
		
		String[] tokens = new String[set.size()];
		
		int j=0;
        while (it.hasNext()) {
        	tokens[j]=(String) it.next();
        	j++;
        } 
        
        
		return tokens;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TextProcessor T = new TextProcessor();
		String[] Tokens = T.Tokenizer("   ");
		for (int i = 0; i <= Tokens.length - 1; i++) {
			System.out.println("\""+Tokens[i]+"\"");
		}
		

	}

}
