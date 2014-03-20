package cs276.programming.mlr;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cs276.programming.ranking.DocData;
import cs276.programming.ranking.QueryData;
import cs276.programming.ranking.QueryDocData;
import cs276.programming.ranking.RankingModel;
import cs276.programming.util.Parameters;
	
public class MLRComprehensive {
			
public void setScoreAll(QueryDocData QueryDoc, RankingModel rModel) {
		
		String FeatureFile = Parameters.BASE_PATH + "PA4/release/t3_temp_Feature.txt";
		String LabelFile = Parameters.BASE_PATH + "PA4/release/t3_temp_Label.txt";
		BufferedWriter writer = null;
		BufferedReader reader = null;
		int DocListSize ;

		try {
				
			File myFile = new File (FeatureFile);
			if (!myFile.exists()){
				myFile.createNewFile();
			}	
			writer = new BufferedWriter(new FileWriter(myFile));
			
			QueryData query = QueryDoc.getQuery();
			ArrayList<DocData> docDataList = QueryDoc.getDocData();
			
			DocListSize =  docDataList.size();
			if (DocListSize == 1){
				docDataList.get(0).SCORE = 1000;
				return;
			}
			
			GenerateFeatures GF = new GenerateFeatures(rModel);
			
			List<String> ListPairs = GenerateFeatures.getPairs(docDataList.size(), false);
			
			int ListSize= ListPairs.size();
			for(String S : ListPairs){
				String[] sp = S.split(",");
				int j = Integer.parseInt(sp[0]);
				int k = Integer.parseInt(sp[1]);
				DocData doc1 = docDataList.get(j);
				DocData doc2 = docDataList.get(k);
			//	System.out.println(doc1.getUrl());
			//	System.out.println(doc2.getUrl());

				String OutPut = GF.getFeatureProducts(doc1, doc2, query);
				OutPut += GF.getAdditionalFeatureProducts(doc1, doc2, query);
			//	System.out.println(OutPut);
				writer.write(OutPut + "\n");
			}
			writer.close();

			PythonModel rm = new PythonModel();
			boolean x = rm.MakePrediction(FeatureFile, LabelFile, "SVMEXTENDED");
			reader = new BufferedReader(new FileReader(LabelFile));
			
			String st = null;
			List<Double> PredClass = new ArrayList<Double>(); 
			while (( st = reader.readLine())!=null) {
				PredClass.add(Double.parseDouble(st));
			}
			if (ListSize != PredClass.size() ){
				throw new ArithmeticException("Maodel retunred incorrect no of records");
			}
			
			List<Integer> Sorted = MLRSVMFunction.SortPairs(ListPairs, PredClass,DocListSize);
			
			double HighestScore = 1000.0;
			
			for (Integer rank :Sorted){
				docDataList.get(rank).SCORE = HighestScore;
				HighestScore -= 10;
			}

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} finally {
			try {
				if(writer != null) writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				if(reader != null) reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public double getScore(DocData Doc, QueryData query, RankingModel rModel) {
	

		return 0;
	}

	
}
