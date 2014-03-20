package cs276.programming.mlr;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import cs276.programming.ranking.DocData;
import cs276.programming.ranking.QueryData;
import cs276.programming.ranking.QueryDocData;
import cs276.programming.ranking.RankingFunction;
import cs276.programming.ranking.RankingModel;
import cs276.programming.util.Parameters;

public class MLRSVMRegression  {
	
	public double setScoreAll(QueryDocData QueryDoc, RankingModel rModel) {
		// TODO Auto-generated method stub
		String FeatureFile = Parameters.BASE_PATH + "PA4/release/t4_temp_Feature.txt";
		String LabelFile = Parameters.BASE_PATH + "PA4/release/t4_temp_Label.txt";
		BufferedWriter writer;

		try {
				
			File myFile = new File (FeatureFile);
			if (!myFile.exists()){
				myFile.createNewFile();
			}	
			writer = new BufferedWriter(new FileWriter(myFile));

			for (DocData doc : QueryDoc.getDocData()) {
				GenerateFeatures GF = new GenerateFeatures(rModel);
				String FVector = GF.GeneretaFeature(doc, QueryDoc.getQuery());
				writer.write(FVector + "\n");
			}
			writer.close();

			PythonModel rm = new PythonModel();
			boolean x = rm.MakePrediction(FeatureFile, LabelFile, "SVMREGRESSION");
			BufferedReader reader = new BufferedReader(new FileReader(LabelFile));
			
			double score = 0;
			String st = null;
			
			for (DocData doc : QueryDoc.getDocData()) {
				score = 0;
				if (( st = reader.readLine())!=null) {
					score = Double.parseDouble(st);
					doc.SCORE = score;
				}
				else{
					throw new ArithmeticException("Maodel retunred incorrect no of records");
				}
			}

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return 0;
	}

	
	public double getScore(DocData Doc, QueryData query, RankingModel rModel) {
		// TODO Auto-generated method stub
		GenerateFeatures GF = new GenerateFeatures(rModel);
		String FVector = GF.GeneretaFeature(Doc, query);

		String FeatureFile = Parameters.BASE_PATH + "PA4/release/Temp_Feature.txt";
		String LabelFile = Parameters.BASE_PATH + "PA4/release/Temp_Label.txt";

		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter(FeatureFile));
			writer.write(FVector);
			writer.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		PythonModel rm = new PythonModel();

		double score = 0;
		boolean x = rm.MakePrediction(FeatureFile, LabelFile, "LINEAR");

		try {
			BufferedReader reader = new BufferedReader(
					new FileReader(LabelFile));
			String st = reader.readLine();
			if (st != null && st != "") {
				score = Double.parseDouble(st);
			}

			return score;

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// System.out.println(FVector);

		return 0;
	}

	
}
