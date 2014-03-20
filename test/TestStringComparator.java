package cs276.programming.test;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import cs276.programming.spellchecker.Edit;
import cs276.programming.spellchecker.StringComparator;
import cs276.programming.spellchecker.Edit.EditType;

/**
 * Class to unit test the String Comparator functionality.
 *
 */
public class TestStringComparator {

	public static void main(String[] args) {
		TestStringComparator testStrComp = new TestStringComparator();
		testStrComp.test(new StringComparator());
	}

	private void test(StringComparator strComp) {
		strComp.determineEditDistance("haas", "hass");
		testMisc(strComp);
		testAdjacentErrors(strComp);
		
		testStrDel(strComp);
		testStrIns(strComp);
		testStrTrans(strComp);
		
		testStrAcress(strComp);
		
		testStrEqual(strComp);
		testStrBlank(strComp);
		
		//test against the edits1s file
		testEdits1s(strComp);
		
	}

	/**
	 * Test strings that are identical to ensure that
	 * they return an edit distance of 0 (zero).
	 */
	private void testStrEqual(StringComparator strComp) {
		String[][] words ={
				{"to", "to"},
				{"too", "too"},
				{"good", "good"},
				{"aah", "aah"},
				{"", ""},
				};
		int[] editDistances = {0, 0, 0, 0, 0};
		
		testResult(strComp, words, editDistances);
	}
	
	/**
	 * Test to ensure that blank strings and spaces
	 * in strings are handled appropriately.
	 */
	private void testStrBlank(StringComparator strComp) {
		
		String[][] words = {
				{"", "a"},
				{"a", ""},
				{"copyright", "copy right"},
				{"copy right", "copyright"},
				};
		Edit[] expectedEdits = {
				new Edit(EditType.DELETE, StringComparator.STR_BEGIN_CHAR, 'a', 0),
				new Edit(EditType.INSERT, StringComparator.STR_BEGIN_CHAR, 'a', 0),
				new Edit(EditType.DELETE, 'y', ' ', 4),
				new Edit(EditType.INSERT, 'y', ' ', 4),
				};
		testResult(strComp, words, expectedEdits);
	}

	/**
	 * Test to ensure that two consecutive edits (i.e. right
	 * next to each other) are handled.
	 */
	private void testAdjacentErrors(StringComparator strComp) {
		String[][] words ={
				{"cat", "dog"},
				{"alltr", "alter"},
				{"chalk", "chock"},
				};
		int[] editDistances = {3, 2, 2};
		
		testResult(strComp, words, editDistances);
	}

	/**
	 * Miscellaneous edit distance tests.
	 */
	private void testMisc(StringComparator strComp) {
		String[][] words ={
				{"hass", "haas"},
				{"minimun", "minimum"},
				{"al", "pale"},
				{"absorbsion", "absorption"},
				{"charistics", "characteristics"},
				{"eraticly", "erratically"},
				{"Foundland", "Newfoundland"},
				{"sophicated", "sophisticated"},
				{"sucesfuly", "successfully"},
				{"unsucesfuly", "unsuccessfully"},
				{"approproximate", "approximate"},
				{"consequentually", "consequently"},
				{"criticists", "critics"},
				{"deriviated", "derived"},
				{"differentiatiations", "differentiations"},
				{"geometricians", "geometers"},
				{"idaeidae", "idea"},
				{"rememberable", "memorable"},
				{"suburburban", "suburban"},
				{"transcendentational", "transcendental"},
				{"unconfortability", "discomfort"},
				{"ahev", "have"},
				{"amature", "amateur"},
				{"blaim", "blame"},
				{"boaut", "about"},
				{"teh", "the"},
				};
		int[] editDistances = {1, 1, 2, 2, 5, 3, 4, 3, 3, 3, 3, 3, 3, 3, 3, 6, 4, 4, 3, 5, 11, 2, 2, 2, 2, 1};
		
		testResult(strComp, words, editDistances);
	}
	
	private void testResult(StringComparator strComp, String[][] words, int[] editDistances) {
		for(int i = 0; i < words.length; i++) {
			int edist = strComp.determineEditDistance(words[i][0], words[i][1]);
			System.err.printf("%d - %s<>%s\n", edist, words[i][0], words[i][1]);
			assert edist == editDistances[i];
		}
	}

	private void testStrDel(StringComparator strComp) {
		
		String[][] words = {
				{"introst", "interost"},
				{"intrest", "interest"}, //this case can be seen as an deletion or as transposition followed by deletion
				{"al", "ale"}, //deletion at end
				{"al", "pal"}
				};
		Edit[] expectedEdits = {
				new Edit(EditType.DELETE, 't', 'e', 3),
				new Edit(EditType.DELETE, 't', 'e', 3),
				new Edit(EditType.DELETE, 'l', 'e', 2),
				new Edit(EditType.DELETE, StringComparator.STR_BEGIN_CHAR, 'p', 0)
				};
		testResult(strComp, words, expectedEdits);
	}

	private void testStrIns(StringComparator strComp) {
		
		String[][] words = {
				{"ale", "al"},
				{"pal", "al"},
				{"axl", "al"}
				};
		Edit[] expectedEdits = {
				new Edit(EditType.INSERT, 'l', 'e', 1),
				new Edit(EditType.INSERT, StringComparator.STR_BEGIN_CHAR, 'p', 0),
				new Edit(EditType.INSERT, 'a', 'x', 1)
				};
		
		testResult(strComp, words, expectedEdits);
	}

	private void testResult(StringComparator strComp, String[][] words,
			Edit[] expectedEdits) {
		for(int i = 0; i < words.length; i++) {
			List<Edit> edits = strComp.determineEdits(words[i][0], words[i][1]);
			assert edits.size() == 1;
			Edit e = edits.get(0);
			System.err.printf("%d - %s<>%s - %d\n", edits.size(), words[i][0], words[i][1], e.getPosition());
			assert e.getType() == expectedEdits[i].getType();
			assert e.getX() == expectedEdits[i].getX();
			assert e.getY() == expectedEdits[i].getY();
			assert e.getPosition() == expectedEdits[i].getPosition();
			
			int edist = strComp.determineEditDistance(words[i][0], words[i][1]);
			assert edist == 1;
			
		}
	}

	private void testStrTrans(StringComparator strComp) {
		
		String[][] words = {
				{"la", "al"},
				{"apl", "pal"},
				{"pla", "pal"},
				{"plas", "pals"}
				};
		Edit[] expectedEdits = {
				new Edit(EditType.TRANSPOSE, 'a', 'l', 0),
				new Edit(EditType.TRANSPOSE, 'p', 'a', 0),
				new Edit(EditType.TRANSPOSE, 'a', 'l', 1),
				new Edit(EditType.TRANSPOSE, 'a', 'l', 1)
				};
		testResult(strComp, words, expectedEdits);
	}

	private void testStrAcress(StringComparator strComp) {
		String[][] words = {
				{"acress", "actress"},
				{"acress", "cress"},
				{"acress", "caress"},
				{"acress", "access"},
				{"acress", "across"}
				};
		Edit[] expectedEdits = {
				new Edit(EditType.DELETE, 'c', 't', 2),
				new Edit(EditType.INSERT, StringComparator.STR_BEGIN_CHAR, 'a', 0),
				new Edit(EditType.TRANSPOSE, 'c', 'a', 0),
				new Edit(EditType.SUBSTITUTE, 'c', 'r', 2),
				new Edit(EditType.SUBSTITUTE, 'o', 'e', 3)
				};
		testResult(strComp, words, expectedEdits);

		String[] words2 = new String[]{"acress", "acres"};
		List<Edit> edits = strComp.determineEdits(words2[0], words2[1]);
		//acres -> ins ss or es
		assert edits.size() == 1;
		Edit e = edits.get(0);
		System.err.printf("%d - %s<>%s - %d\n", edits.size(), words2[0], words2[1], e.getPosition());
		assert e.getType() == EditType.INSERT;
		assert (e.getX() == 's' && e.getPosition() == 4) || (e.getPosition() == 3 && e.getX() == 'e');
		assert e.getY() == 's';
	}

	private void testEdits1s(StringComparator strComp) {
		String[] words;
		String MisspelledQueriesFile = "/cs276/PA2/data/edit1s.txt";
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(MisspelledQueriesFile));

			String strLine = "";
			int correctCount = 0;
			int incorrectCount = 0;

			while (((strLine = in.readLine()) != null)) {
				words = strLine.split("\t");
				int cost = strComp.determineEditDistance(words[0], words[1]);
				if(words[0].equals(words[1])) { //to take care of bad data
					if(cost == 0) {
						correctCount++;
					} else {
						System.err.printf("%d - %s<>%s\n", cost, words[0], words[1]);
						incorrectCount++;
					}
				} else {
					if(cost != 1) {
						System.err.printf("%d - %s<>%s\n", cost, words[0], words[1]);
						incorrectCount++;
					} else {
	//					System.err.printf("%d - %s<>%s\n", cost, words[0], words[1]);
						correctCount++;
					}
				}
			}
			
			System.err.printf("Got %d correct and %d incorrect\n", correctCount, incorrectCount);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if(in != null) in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
