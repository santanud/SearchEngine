package cs276.programming.mlr;

import java.io.File;

import cs276.programming.ranking.QueryDocData;
import cs276.programming.ranking.QueryDocReader;
import cs276.programming.ranking.RankingModel;
import cs276.programming.util.Parameters;

public class MLRClient {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		//python l2r.py queryDocTrainData.train queryDocTrainRel.train <test_data_file> <task>

		File inFile = null;
		int TaskId = 0;
	
		if (args.length <= 1) {
			
			System.err.println("Proper Usage is: java -cp pa4.jar cs276.programming.mlr.MLRClient <TaskId> <InputFile>");
			System.exit(1);
		} else {
			TaskId = Integer.parseInt(args[0]);
			if (TaskId < 1 || TaskId > 4){
				System.err.println("Proper Usage is: TaskId could be 1, 2,3, or 4");
				System.exit(1);
			}	
			inFile = new File(args[1]);
		}

		//File inFile = new File(Parameters.BASE_PATH + "PA4/release/queryDocTrainData.dev");
		//int TaskId = 1;
		
		QueryDocReader queryDocTestData = new QueryDocReader(inFile);
		
		String ModelFileName = Parameters.BASE_PATH + "PA3/starter/RankingModel";
		RankingModel RModel = RankingModel.ReadFromDisk( ModelFileName);

		for (QueryDocData TestQueryDocData : queryDocTestData) {

			RModel.rankMLR(TestQueryDocData,TaskId);
			TestQueryDocData.DisplayResult();
		}
		
		queryDocTestData.close();
	}

}
