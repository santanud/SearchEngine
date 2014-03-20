package cs276.programming.ranking;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Class to estimate the optimal parameters 
 * for the best NDCG value. 
 */
public class ParameterEstimator {

	String IDFTermListFile = "/cs276/PA3/starter/AllQueryTerms";
	String CorpusDir = "/cs276/PA1/data";
	String groundTruthFile = "/cs276/PA3/starter/queryDocTrainRel";
	String DocTrainingDataFile = "/cs276/PA3/starter/queryDocTrainData";
	RankingModel RModel;
	NDCG ndcg;
	ArrayList<QueryDocData> DocQueryAllList;

	public static void main(String[] args) {

		String ModelFileName = "/cs276/PA3/starter/RankingModel";
		ParameterEstimator ParamEst = new ParameterEstimator();

		int HeldOutPercent = -1;
//		int TaskId = args.length > 0 ? Integer.parseInt(args[0]) : 1;
		Map<String, Double> allTaskBestWeights = new HashMap<String, Double>();
		
		for(int TaskId = 1; TaskId < 5; TaskId++) {
			Map<String, Double> BestWeights = new HashMap<String, Double>();
			double[] BestNdcg = ParamEst.RunCrossValidation(HeldOutPercent, TaskId, BestWeights);
			System.err.println("Task : " + TaskId + " Training NDCG : "
					+ BestNdcg[0] + "  Test NDCG : " + BestNdcg[1]);
			allTaskBestWeights.putAll(BestWeights);
		}

		System.err.printf("All Task Weights: %s\n", allTaskBestWeights.toString().replace(", ", "\n"));
		ParamEst.RModel.setWeight(allTaskBestWeights);
		ParamEst.SaveModel(ModelFileName);
		ParamEst.RModel = ParamEst.RModel.ReadFromDisk(ModelFileName);
		System.err.printf("All Task Weights: %s\n", ParamEst.RModel.FeatureWeights.toString().replace(", ", "\n"));
		
	}

	ParameterEstimator() {
		
		ndcg = new NDCG(groundTruthFile);
		RModel = new RankingModel();
		QueryDocReader queryDocTrainDataReader = new QueryDocReader(new File(
				DocTrainingDataFile));

		RModel.train(queryDocTrainDataReader);
		DocQueryAllList = new ArrayList<QueryDocData>();
		queryDocTrainDataReader = new QueryDocReader(new File(
				DocTrainingDataFile));
		for (QueryDocData TestQueryDocData : queryDocTrainDataReader)
			DocQueryAllList.add(TestQueryDocData);
	}

	double[] RunCrossValidation(int HeldOutPercent, int TaskId, Map<String, Double> BestWeights) {

		int counter = 0;
		ArrayList<QueryDocData> DocQueryTrainingSet = new ArrayList<QueryDocData>();
		ArrayList<QueryDocData> DocQueryTestSet = new ArrayList<QueryDocData>();

		double BestNDCG[] = { 0.0, 0.0 };
		Random randomGenerator = new Random();
		int randomInt = 0;

		for (QueryDocData trainData : this.DocQueryAllList) {
			randomInt = randomGenerator.nextInt(100);
			if (randomInt >= HeldOutPercent)
				DocQueryTrainingSet.add(trainData);
			else
				DocQueryTestSet.add(trainData);
		}

		System.err.println("Training Size : " + DocQueryTrainingSet.size());
		System.err.println("Heldout : " + DocQueryTestSet.size());

		BestNDCG[0] = tuneParameters(TaskId, DocQueryTrainingSet, BestWeights);
		BestNDCG[1] = getRankNDCG(TaskId, DocQueryTestSet);

		return BestNDCG;

	}

	double tuneParameters(int TaskId,
			ArrayList<QueryDocData> DocQueryTrainingSet, Map<String, Double> BestWeights) {

		Map<String, Double> NewWeights = new HashMap<String, Double>();

		double BestNDCG = 0;
		double ThisNDCG = 0;

		if (TaskId == 1) {

			NewWeights.put("task1_W_title", 10.4);
			NewWeights.put("task1_W_anchor", 6.2);
			NewWeights.put("task1_W_url", 1.5);
			NewWeights.put("task1_W_header", 3.5);
			NewWeights.put("task1_W_body", 1.0);
			System.err.print("Task Id = " + TaskId);
/*
			for (double task1_W_anchor_val = 0.1; task1_W_anchor_val <= 2.0; task1_W_anchor_val += 0.5) {
				// Testing with one set of value for task1_W_title

				NewWeights.put("task1_W_anchor", task1_W_anchor_val);
				System.err
						.println("task1_W_anchor=" + task1_W_anchor_val + " ");

				for (double task1_W_title_val = 1.0; task1_W_title_val < 5.0; task1_W_title_val += 0.5) {

					NewWeights.put("task1_W_title", task1_W_title_val);
					System.err.print("\t task1_W_title=" + task1_W_title_val
							+ " ");

					this.RModel.setWeight(NewWeights);
					ThisNDCG = getRankNDCG(TaskId, DocQueryTrainingSet);
					System.err.print(" NDCG =" + ThisNDCG + " ");

					if (ThisNDCG > BestNDCG) {
						BestNDCG = ThisNDCG;
						BestWeights = new HashMap<String, Double>(NewWeights);

					}
					System.err.print("\n");
				}
			}
			System.err.println(" Final Weights : " + NewWeights);
			// setting the best weights into the model
			this.RModel.setWeight(BestWeights);
*/
			BestNDCG = tuneOneAtATime2(TaskId, DocQueryTrainingSet, NewWeights,
					BestWeights);

		} else if (TaskId == 2) {
			NewWeights.put("task2_W_title", 10.4);
			NewWeights.put("task2_W_anchor", 6.2);
			NewWeights.put("task2_W_url", 1.5);
			NewWeights.put("task2_W_header", 3.5);
			NewWeights.put("task2_W_body", 1.0);
			
			NewWeights.put("task2_B_title", 0.75);
			NewWeights.put("task2_B_anchor", 0.75);
			NewWeights.put("task2_B_url", 0.75);
			NewWeights.put("task2_B_header", 0.75);
			NewWeights.put("task2_B_body", 0.75);
			
			NewWeights.put("task2_K1", 1.5);
			NewWeights.put("task2_lambda", 0.2);
			NewWeights.put("task2_lambda_prime", 0.2);
			
			BestNDCG = tuneOneAtATime2(TaskId, DocQueryTrainingSet, NewWeights,
					BestWeights);

		} else if (TaskId == 3) {
			NewWeights.put("task3_W_title", 10.4);
			NewWeights.put("task3_W_anchor", 6.2);
			NewWeights.put("task3_W_url", 1.5);
			NewWeights.put("task3_W_header", 3.5);
			NewWeights.put("task3_W_body", 1.0);
			NewWeights.put("task3_B", 5.0);
			BestNDCG = tuneOneAtATime2(TaskId, DocQueryTrainingSet, NewWeights,
					BestWeights);
		} else if (TaskId == 4) {
			NewWeights.put("task4_W_title", 10.4);
			NewWeights.put("task4_W_anchor", 6.2);
			NewWeights.put("task4_W_url", 1.5);
			NewWeights.put("task4_W_header", 3.5);
			NewWeights.put("task4_W_body", 1.0);
			NewWeights.put("task4_B", 5.0);
			NewWeights.put("task4_W_first_pos", 5.0);
			BestNDCG = tuneOneAtATime2(TaskId, DocQueryTrainingSet, NewWeights,
					BestWeights);
		} else if (TaskId == 5) {
			NewWeights.put("task5_W_title", 10.4);
			NewWeights.put("task5_W_anchor", 6.2);
			NewWeights.put("task5_W_url", 1.5);
			NewWeights.put("task5_W_header", 3.5);
			NewWeights.put("task5_W_body", 1.0);
			
			BestNDCG = tuneOneAtATime2(TaskId, DocQueryTrainingSet, NewWeights,
					BestWeights);

		}
		return BestNDCG;
	}

	private double tuneOneAtATime(int TaskId,
			ArrayList<QueryDocData> DocQueryTrainingSet,
			Map<String, Double> NewWeights, Map<String, Double> BestWeights) {
		
		double BestNDCG;
		for(String paramName : NewWeights.keySet()) {
			tuneSingleParam(paramName, NewWeights, BestWeights, TaskId, DocQueryTrainingSet);
		}
		
//		System.err.println(" Final Weights : " + BestWeights.toString().replace(", ", "\n"));
		// setting the best weights into the model
		this.RModel.setWeight(BestWeights);
		BestNDCG = getRankNDCG(TaskId, DocQueryTrainingSet);
		return BestNDCG;
	}

	private double tuneOneAtATime2(int TaskId,
			ArrayList<QueryDocData> DocQueryTrainingSet,
			Map<String, Double> NewWeights, Map<String, Double> BestWeights) {
		
		double BestNDCG;
		for(String paramName : NewWeights.keySet()) {
			tuneSingleParam(paramName, NewWeights, BestWeights, TaskId, DocQueryTrainingSet);
		}
		
		// setting the best weights into the model
		this.RModel.setWeight(BestWeights);
		BestNDCG = getRankNDCG(TaskId, DocQueryTrainingSet);
//		System.err.printf(" Final Weights (NDCG = %.5f) : %s\n", BestNDCG, BestWeights.toString().replace(", ", "\n"));
		
		double overallBestNdcg = BestNDCG;
		Map<String, Double> overallBestWeights = BestWeights;
//		boolean within10 = false;
		int count = -1;
		do {
			count++;
			if(overallBestNdcg < BestNDCG) {
				overallBestNdcg = BestNDCG;
				overallBestWeights = BestWeights;
			}
			for(String paramName : NewWeights.keySet()) {
				tuneSingleParam(paramName, NewWeights, BestWeights, TaskId, DocQueryTrainingSet);
			}
			
			// setting the best weights into the model
			this.RModel.setWeight(BestWeights);
			BestNDCG = getRankNDCG(TaskId, DocQueryTrainingSet);
//			System.err.printf(" Final Weights (%.5f) : %s\n", BestNDCG, BestWeights.toString().replace(", ", "\n"));
//			System.err.printf("\n***Previous Best NDCG: %.6f Best value from current run:%.4f\n", overallBestNdcg, BestNDCG);
//			within10 = (Math.abs((overallBestNdcg - BestNDCG) / BestNDCG) < 0.1);
		} while(count < 3);
		
		System.err.printf("Overall  Final Weights (%.5f) : %s\n", BestNDCG, overallBestWeights.toString().replace(", ", "\n"));
		this.RModel.setWeight(overallBestWeights);
		return overallBestNdcg;
	}

	void tuneSingleParam(String paramName, Map<String, Double> newWeights, Map<String, Double> bestWeights, int taskId, ArrayList<QueryDocData> docQueryTrainingSet) {
		
		double bestNdcg = 0;
		double currNdcg = 0;

//		System.err.println(paramName);
		for (double param_val = 0.1; param_val < 1.0; param_val += 0.1) {

			newWeights.put(paramName, param_val);

			this.RModel.setWeight(newWeights);
			currNdcg = getRankNDCG(taskId, docQueryTrainingSet);
//			System.err.printf("%.1f\t%.4f\n", param_val, currNdcg);

			if (currNdcg > bestNdcg) {
				bestNdcg = currNdcg;
				bestWeights.put(paramName, newWeights.get(paramName));
			}
		}

		for (double param_val = 10; param_val < 150; param_val += 10) {

			newWeights.put(paramName, param_val);

			this.RModel.setWeight(newWeights);
			currNdcg = getRankNDCG(taskId, docQueryTrainingSet);
//			System.err.printf("%.1f\t%.4f\n", param_val, currNdcg);

			if (currNdcg > bestNdcg) {
				bestNdcg = currNdcg;
				bestWeights.put(paramName, newWeights.get(paramName));
			}
		}
		//zoom in further
		double wt = bestWeights.get(paramName);
//		System.err.println("\t" + wt);
		double lowValue, highValue, increment;
		
		if(wt > 1) {
			lowValue = wt - 10;
			highValue = wt + 10;
			increment = 1;
		} else {
			lowValue = wt - 0.1;
			highValue = wt + 0.1;
			increment = .01;
		}
		for (double param_val = lowValue; param_val < highValue; param_val += increment) {

			newWeights.put(paramName, param_val);

			this.RModel.setWeight(newWeights);
			currNdcg = getRankNDCG(taskId, docQueryTrainingSet);
//			System.err.printf("%.2f\t%.4f\n", param_val, currNdcg);

			if (currNdcg > bestNdcg) {
				bestNdcg = currNdcg;
				bestWeights.put(paramName, newWeights.get(paramName));
			}
		}
		
		
//		System.err.println(bestWeights);

	}

	double getRankNDCG(int TaskId, ArrayList<QueryDocData> DocQueryTestSet) {

		for (QueryDocData TestQueryDocData : DocQueryTestSet) {
			this.RModel.rank(TestQueryDocData, TaskId);
		}

		double tempNdcg = this.ndcg.getNDCG(DocQueryTestSet);
		return tempNdcg;

	}

	private void SaveModel(String modelFileName) {
		// TODO Auto-generated method stub
		RankingModel.SaveToDisk(this.RModel, modelFileName);

	}

}
