package cs276.programming.mlr;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cs276.programming.ranking.NDCG;
import cs276.programming.ranking.QueryDocData;
import cs276.programming.ranking.QueryDocReader;
import cs276.programming.ranking.RankingModel;
import cs276.programming.util.Parameters;

public class TrainModels {

	String IDFTermListFile = Parameters.BASE_PATH + "PA3/starter/AllQueryTerms";
	String CorpusDir = Parameters.BASE_PATH + "PA1/data";

	// public String TrainRelFile = Parameters.BASE_PATH +
	// "PA4/release/queryDocTrainRel.train";
	// public String TrainDataFile = Parameters.BASE_PATH +
	// "PA4/release/queryDocTrainData.train";

	public RankingModel RModel;
	NDCG ndcg;

	public static void main(String[] args) {

		
		String TrainRelFile = Parameters.BASE_PATH + "PA4/release/queryDocTrainRel.train";
		String TrainDataFile = Parameters.BASE_PATH + "PA4/release/queryDocTrainData.train";

		//String TrainRelFile = Parameters.BASE_PATH + "PA4/release/train_rel.txt";
		//String TrainDataFile = Parameters.BASE_PATH + "PA4/release/train_data.txt";

		QueryDocReader queryDocTrainDataReader = new QueryDocReader(new File(TrainDataFile));
		List<QueryDocData> DocQueryAllList = new ArrayList<QueryDocData>();
		int i = 0;
		for (QueryDocData TestQueryDocData : queryDocTrainDataReader) {
			DocQueryAllList.add(TestQueryDocData);
			i++;
		}
		System.out.println("i=" + i + " size=" + DocQueryAllList.size()); //Collection of all the Query-Docs in the training data
		
		//Task 1 Training - Pointwise Approach and Linear Regression
//		trainTask1(TrainDataFile, TrainRelFile, DocQueryAllList);
		
		//Task 2 Training - Pairwise Approach and Ranking SVM
		trainTask2(TrainRelFile, TrainDataFile, DocQueryAllList);
//		trainTask3(TrainRelFile, TrainDataFile, DocQueryAllList);
//		trainTask4(TrainRelFile, TrainDataFile, DocQueryAllList);
	}

	
	private static void trainTask2(String TrainRelFile, String TrainDataFile, List<QueryDocData> DocQueryAllList) {
		String FeatureFile = Parameters.BASE_PATH + "PA4/release/task2_train.txt";
		String FeatureLabel = Parameters.BASE_PATH + "PA4/release/task2_label.txt";

		TrainModels TM = new TrainModels(TrainDataFile, TrainRelFile);
		GenerateFeatures GF = new GenerateFeatures(TM.getModel());
		
		boolean GenLabel = true;
		GF.genSvnFeatureLabel(DocQueryAllList, FeatureFile, FeatureLabel, TrainRelFile, GenLabel, 0);

		PythonModel rm2 = new PythonModel();

		boolean x = rm2.RunTraining(FeatureFile, FeatureLabel, "SVM");
	}

	private static void trainTask1(String trainDataFile, String trainRelFile, List<QueryDocData> docQueryAllList) {
		String FeatureFile = Parameters.BASE_PATH + "PA4/release/task1_train.txt";
		String FeatureLabel = Parameters.BASE_PATH + "PA4/release/task1_label.txt";

		TrainModels TM = new TrainModels(trainDataFile, trainRelFile);
		GenerateFeatures GF = new GenerateFeatures(TM.getModel());

		GF.genFeature(docQueryAllList, FeatureFile);
		GF.genLabel(docQueryAllList, FeatureLabel, trainRelFile);

		PythonModel rm = new PythonModel();

		boolean x = rm.RunTraining(FeatureFile, FeatureLabel, "LINEAR");
	}
	
	
	private static void trainTask4(String trainRelFile, String trainDataFile,
			List<QueryDocData> docQueryAllList) {
		String FeatureFile = Parameters.BASE_PATH + "PA4/release/task4_train.txt";
		String FeatureLabel = Parameters.BASE_PATH + "PA4/release/task4_label.txt";

		TrainModels TM = new TrainModels(trainDataFile, trainRelFile);
		GenerateFeatures GF = new GenerateFeatures(TM.getModel());

		GF.genFeature(docQueryAllList, FeatureFile);
		GF.genLabel(docQueryAllList, FeatureLabel, trainRelFile);

		PythonModel rm = new PythonModel();

		boolean x = rm.RunTraining(FeatureFile, FeatureLabel, "SVMREGRESSION");
		
	}

	private static void trainTask3(String trainRelFile, String trainDataFile,
			List<QueryDocData> docQueryAllList) {
		String FeatureFile = Parameters.BASE_PATH + "PA4/release/task3_train.txt";
		String FeatureLabel = Parameters.BASE_PATH + "PA4/release/task3_label.txt";

		TrainModels TM = new TrainModels(trainDataFile, trainRelFile);
		
//		String ModelFileName = Parameters.BASE_PATH + "PA3/starter/RankingModel";
//		RankingModel RModel = RankingModel.ReadFromDisk( ModelFileName);

		GenerateFeatures GF = new GenerateFeatures(TM.getModel());
//		GenerateFeatures GF = new GenerateFeatures(RModel);
		
		boolean GenLabel = true;
		GF.genSvnFeatureLabel(docQueryAllList, FeatureFile, FeatureLabel, trainRelFile, GenLabel, 1);

		PythonModel rm2 = new PythonModel();

		boolean x = rm2.RunTraining(FeatureFile, FeatureLabel, "SVMEXTENDED");
	}


	TrainModels(String TrainDataFile, String TrainRelFile) {
		ndcg = new NDCG(TrainRelFile);
//		RModel = new RankingModel();
		QueryDocReader queryDocTrainDataReader = new QueryDocReader(new File(TrainDataFile));
//		RModel.train(queryDocTrainDataReader);
		String ModelFileName = Parameters.BASE_PATH + "PA3/starter/RankingModel";
		RModel = RankingModel.ReadFromDisk( ModelFileName);

	}

	public RankingModel getModel() {
		return this.RModel;
	}

}
