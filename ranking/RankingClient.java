package cs276.programming.ranking;

import java.io.File;

public class RankingClient {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
	
		//  $ ./rank.sh 0 queryDocTrainData

		File inFile = null;
		int TaskId = 0;
		
		if (args.length <= 1) {
			
			System.err.println("Proper Usage is: java -cp pa3.jar cs276.programming.query.RankingClient <TaskId> <InputFile>");
			System.exit(1);
		} else {
			TaskId = Integer.parseInt(args[0]);
			if (TaskId < 1 || TaskId > 4){
				System.err.println("Proper Usage is: TaskId could be 1, 2,3, or 4");
				System.exit(1);
			}	
			inFile = new File(args[1]);
		}
			
		QueryDocReader queryDocTestData = new QueryDocReader(inFile);
	
		String ModelFileName = "/cs276/PA3/starter/RankingModel";
		RankingModel RModel = RankingModel.ReadFromDisk( ModelFileName);
		
		if(args.length > 0) {
			try {
				TaskId = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		
		for (QueryDocData TestQueryDocData : queryDocTestData) {

			RModel.rank(TestQueryDocData,TaskId);
			TestQueryDocData.DisplayResult();
		}

	}

	private static double getPropValue(String propName, double defaultValue) {
		
		String propValue = System.getProperty(propName);
		if(propValue != null) {
			try {
				double value = Double.parseDouble(propValue);
				return value;
			} catch (NumberFormatException e) {
			}
		}
		return defaultValue;
	}
}
