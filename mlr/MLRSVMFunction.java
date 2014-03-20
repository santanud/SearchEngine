package cs276.programming.mlr;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cs276.programming.ranking.DocData;
import cs276.programming.ranking.QueryData;
import cs276.programming.ranking.QueryDocData;
import cs276.programming.ranking.RankingFunction;
import cs276.programming.ranking.RankingModel;
import cs276.programming.util.Parameters;

public class MLRSVMFunction  {

	
	public void setScoreAll(QueryDocData QueryDoc, RankingModel rModel) {
		
		String FeatureFile = Parameters.BASE_PATH + "PA4/release/t2_temp_Feature.txt";
		String LabelFile = Parameters.BASE_PATH + "PA4/release/t2_temp_Label.txt";
		BufferedWriter writer;
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
			//	System.out.println(OutPut);
				writer.write(OutPut + "\n");
			}
			writer.close();

			PythonModel rm = new PythonModel();
			boolean x = rm.MakePrediction(FeatureFile, LabelFile, "SVM");
			BufferedReader reader = new BufferedReader(new FileReader(LabelFile));
			
			String st = null;
			List<Double> PredClass = new ArrayList<Double>(); 
			while (( st = reader.readLine())!=null) {
				PredClass.add(Double.parseDouble(st));
			}
			if (ListSize != PredClass.size() ){
				throw new ArithmeticException("Maodel retunred incorrect no of records");
			}
			
		//	System.out.println("list pairs"+ListPairs);
			List<Integer> Sorted = SortPairs(ListPairs, PredClass,DocListSize);
		//	System.out.println("sorted"+Sorted);
			double HighestScore = 1000.0;
			
			for (Integer rank :Sorted){
				docDataList.get(rank).SCORE = HighestScore;
				HighestScore -= 10;
			//	System.out.println("val "+(int)rank +" score "+docDataList.get(rank).SCORE);
			}

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}

	static List<Integer> SortPairs(List<String> listPairs,List<Double> predClass, int size) {
		
		int HighestIndex = -1;

		List<Integer> SortedList = new ArrayList<Integer>();
		List<Integer> InnerBag = new ArrayList<Integer>();
		for (int i=0; i < size ;i++){

			InnerBag.add(i);
		}
		while( InnerBag.size() > 0) {
			HighestIndex = InnerBag.get(0);
			int i = HighestIndex;
				for (int j : InnerBag){
					if (i < j)
						//System.out.println(i+","+j);
						if (iGTj(i,j,listPairs,predClass)){
			//				System.out.println(i +">"+j);
							if (iGTj(i,HighestIndex,listPairs,predClass))
								HighestIndex = i;
						}
						else{
			//				System.out.println(i +"<"+j);
							if (iGTj(j,HighestIndex,listPairs,predClass))
								HighestIndex = j;
							}
						}
				
	//		System.out.println("Final HighestIndex:"+HighestIndex);
			SortedList.add(HighestIndex);
			InnerBag.remove((Integer)HighestIndex);
		}
		return SortedList;
	}

	private static boolean iGTj(int i,int j, List<String> listPairs,List<Double> predClass) {
		
		boolean out = false;
		if (j == -1)
			return true;
		if (i < j)
			 out = predClass.get(listPairs.indexOf(i+","+j)) >= 0 ? true : false;
		else if (i > j)
			out = predClass.get(listPairs.indexOf(j+","+i)) >= 0 ? false : true;
	return out;
	
	}

	public double getScore(DocData Doc, QueryData query, RankingModel rModel) {
		// TODO Auto-generated method stub
		return 0;
	}

}
