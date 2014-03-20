package cs276.programming.mlr;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.DataFormatException;

import cs276.programming.ranking.BM25FRankingFunction;
import cs276.programming.ranking.BasicRankingFunction;
import cs276.programming.ranking.BodyPosRakingFunction;
import cs276.programming.ranking.DocData;
import cs276.programming.ranking.DocSmoothingType;
import cs276.programming.ranking.NDCG;
import cs276.programming.ranking.NormalizationType;
import cs276.programming.ranking.QueryData;
import cs276.programming.ranking.QueryDocData;
import cs276.programming.ranking.QueryDocReader;
import cs276.programming.ranking.RankingFunction;
import cs276.programming.ranking.RankingModel;
import cs276.programming.ranking.SmallestWindowRankingFunction;
import cs276.programming.ranking.StemmedCosineRankingFunction;
import cs276.programming.ranking.TermSmoothingType;
import cs276.programming.ranking.Vectorizer;
import cs276.programming.ranking.DocData.DocFeature;
import cs276.programming.util.Parameters;
import cs276.programming.util.VectorUtil;

public class GenerateFeatures {

	// String groundTruthFile = Parameters.BASE_PATH +
	// "PA4/release/queryDocTrainRel.train";
	String IDFTermListFile = Parameters.BASE_PATH + "PA3/starter/AllQueryTerms";
	String CorpusDir = Parameters.BASE_PATH + "PA1/data";
	String DocTrainingDataFile = Parameters.BASE_PATH
			+ "PA4/release/queryDocTrainData.train";
	RankingModel RModel;
	NDCG ndcg;

	GenerateFeatures(RankingModel rModel) {

		this.RModel = rModel;

	}

	public void genFeature(List<QueryDocData> DocQueryAllList,
			String TraingDataforPython) {

		BufferedWriter writer = null;
		NumberFormat formatter = new DecimalFormat(
				"#0.00######################");

		try {
			writer = new BufferedWriter(new FileWriter(TraingDataforPython));
			int i = 0;
			for (QueryDocData TestQueryDocData : DocQueryAllList) {
				QueryData query = TestQueryDocData.getQuery();
				for (DocData Doc : TestQueryDocData.getDocData()) {
					String OutPut = GeneretaFeature(Doc, query);
					writer.write(OutPut + "\n");
					i++;
				}
			}
			System.out.println("No of Documents =" + i);

		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			try {
				if(writer != null) writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	public void genLabel(List<QueryDocData> DocQueryAllList, String LabelFile,
			String TrainRelFile) {

		this.ndcg = new NDCG(TrainRelFile);

		BufferedWriter writer = null;
		NumberFormat formatter = new DecimalFormat("#0.########");

		try {
			writer = new BufferedWriter(new FileWriter(LabelFile));

			for (QueryDocData TestQueryDocData : DocQueryAllList) {

				QueryData query = TestQueryDocData.getQuery();

				for (DocData Doc : TestQueryDocData.getDocData()) {

					double RelFeedback = ndcg.GetRel(query.GetQueryString(),
							Doc.getUrl());
					// System.out.println(formatter.format(RelFeedback));
					writer.write(formatter.format(RelFeedback) + "\n");

				}
			}
		} catch (DataFormatException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			try {
				if(writer != null) writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	String GeneretaFeature(DocData Doc, QueryData query) {

		// StringBuilder sb = new StringBuilder();
		List<String> termVector = new ArrayList<String>();
		// QueryData query = TestQueryDocData.getQuery();
		List<Double> queryVector = Vectorizer.getQueryVector(
				query.GetQueryString(), termVector,
				TermSmoothingType.LOGARITHM, DocSmoothingType.IDF, this.RModel);

		Map<DocFeature, List<Double>> docVector = Vectorizer.getDocumentVector(
				Doc, termVector, TermSmoothingType.LOGARITHM,
				DocSmoothingType.NONE, NormalizationType.LENGTH, this.RModel);

		double FeatureURL = VectorUtil.dot(queryVector,
				docVector.get(DocFeature.URL));
		double FeatureTitle = VectorUtil.dot(queryVector,
				docVector.get(DocFeature.TITLE));
		double FeatureBody = VectorUtil.dot(queryVector,
				docVector.get(DocFeature.BODY));
		double FeatureHeader = VectorUtil.dot(queryVector,
				docVector.get(DocFeature.HEADER));
		double FeatureAnchor = VectorUtil.dot(queryVector,
				docVector.get(DocFeature.ANCHOR));

		// String OutPut = formatter.format(FeatureURL) + "\t"
		// + formatter.format(FeatureTitle) + "\t" + FeatureBody + "\t"
		// + formatter.format(FeatureHeader) + "\t" +
		// formatter.format(FeatureAnchor) + "\n";
		String OutPut = FeatureURL + "\t" + FeatureTitle + "\t" + FeatureBody
				+ "\t" + FeatureHeader + "\t" + FeatureAnchor;

		return OutPut;
	}

	public static void main(String[] args) {

		String DocTrainingDataFile = "/cs276/PA4/release/queryDocTrainData.train";

		QueryDocReader queryDocTrainDataReader = new QueryDocReader(new File(
				DocTrainingDataFile));
		List<QueryDocData> DocQueryAllList = new ArrayList<QueryDocData>();

		for (QueryDocData TestQueryDocData : queryDocTrainDataReader)
			DocQueryAllList.add(TestQueryDocData);

		String Task1TrainingFile = "/cs276/PA4/release/task1_label.txt";
		// GenerateFeatures GF = new GenerateFeatures();
		// GF.genLabel(DocQueryAllList, Task1TrainingFile);
	}

	public void genSvnFeatureLabel(List<QueryDocData> docQueryAllList,
			String featureFile, String labelFile, String trainRelFile,
			boolean GenLabel, int additionalFeatures) {

		this.ndcg = new NDCG(trainRelFile);

		BufferedWriter writer = null;
		NumberFormat formatter = new DecimalFormat(
				"#0.00######################");

		BufferedWriter labelWriter = null;

		try {
			writer = new BufferedWriter(new FileWriter(featureFile));
			if (GenLabel == true) {
				labelWriter = new BufferedWriter(new FileWriter(labelFile));
			}

			int i = 0;
			for (QueryDocData TestQueryDocData : docQueryAllList) {
				QueryData query = TestQueryDocData.getQuery();
				ArrayList<DocData> docDataList = TestQueryDocData.getDocData();
				List<String> CombList = getPairs(docDataList.size(), true);

				for(String S : CombList){
					//System.out.println("Docs combinnation"+S);
					String[] sp = S.split(",");
					int j = Integer.parseInt(sp[0]);
					int k = Integer.parseInt(sp[1]);

					DocData doc1 = docDataList.get(j);
					double rel1 = ndcg.GetRel(query.GetQueryString(),
							doc1.getUrl());

					DocData doc2 = docDataList.get(k);
					double rel2 = ndcg.GetRel(query.GetQueryString(),
							doc2.getUrl());

					String OutPut = getFeatureProducts(doc1, doc2, query);
					
					if(additionalFeatures == 1) {
						OutPut += getAdditionalFeatureProducts(doc1, doc2, query/*, docQueryAllList*/);
					}
					writer.write(OutPut + "\n");

					if (GenLabel == true) {
						labelWriter.write((rel1 - rel2) >= 0 ? "1\n" : "-1\n"); 
					}
				}

			}
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (DataFormatException e) {
			e.printStackTrace();
		} finally {
			try {
				if (writer != null)
					writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				if (labelWriter != null)
					labelWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
	public String getAdditionalFeatureProducts(DocData doc1, DocData doc2, QueryData query/*, List<QueryDocData> docQueryAllList*/) {
		String out = "";
		
		RankingFunction brf = new BM25FRankingFunction();
//		double bm25fScoreDiff = brf.getScore(doc1, query, RModel) - brf.getScore(doc2, query, RModel);
		double bm25fScore1 = brf.getScore(doc1, query, RModel);
		double bm25fScore2 = brf.getScore(doc2, query, RModel);
		double bm25fScoreDiff = (bm25fScore1 > 0 ? Math.log(bm25fScore1) : 0) - (bm25fScore2 > 0 ? Math.log(bm25fScore2) : 0);

		SmallestWindowRankingFunction swrf = new SmallestWindowRankingFunction();
		double swScoreDiff = swrf.getScore(doc1, query, RModel) - swrf.getScore(doc2, query, RModel);
		
		int pageRankDiff = doc1.getPageRank() - doc2.getPageRank();
		
		int isPdfDoc1 = doc1.getUrl().endsWith(".pdf") ? 1 : 0;
		int isPdfDoc2 = doc2.getUrl().endsWith(".pdf") ? 1 : 0;
		double isPdfDiff = isPdfDoc1 - isPdfDoc2;
		
		out = "\t" + bm25fScoreDiff + "\t" + swScoreDiff + "\t" + pageRankDiff + "\t" + isPdfDiff;
		
		Map<DocFeature, Integer> swmap1 = swrf.getSmallestWindows(doc1, query);
		Map<DocFeature, Integer> swmap2 = swrf.getSmallestWindows(doc2, query);
		
		
		out += "\t" + (swmap1.get(DocFeature.ANCHOR) - swmap2.get(DocFeature.ANCHOR));
		out += "\t" + (swmap1.get(DocFeature.BODY) - swmap2.get(DocFeature.BODY));
		out += "\t" + (swmap1.get(DocFeature.HEADER) - swmap2.get(DocFeature.HEADER));
		out += "\t" + (swmap1.get(DocFeature.TITLE) - swmap2.get(DocFeature.TITLE));
		out += "\t" + (swmap1.get(DocFeature.URL) - swmap2.get(DocFeature.URL));
		out += "\t" + (getTotalAnchorCount(doc1) - getTotalAnchorCount(doc2));
		out += "\t" + (doc1.getUrl().split("/").length - doc2.getUrl().split("/").length);
		BodyPosRakingFunction bprf = new BodyPosRakingFunction();
		out += "\t" + (bprf.getFirstPos(doc1, query) - bprf.getFirstPos(doc2, query));
		
		RankingFunction scrf = new StemmedCosineRankingFunction();
		out += "\t" + (scrf.getScore(doc1, query, RModel) - scrf.getScore(doc2, query, RModel));
		
		RankingFunction basicrf = new BasicRankingFunction();
		out += "\t" + (basicrf.getScore(doc1, query, RModel) - basicrf.getScore(doc2, query, RModel));
		
		int isPptDoc1 = (doc1.getUrl().endsWith(".ppt") || doc1.getUrl().endsWith(".pptx")) ? 1 : 0;
		int isPptDoc2 = (doc2.getUrl().endsWith(".ppt") || doc2.getUrl().endsWith(".pptx")) ? 1 : 0;
		
		out += "\t" + (isPptDoc1 - isPptDoc2);
		
		//URL Length
		out += "\t" + (doc1.getUrl().length() - doc2.getUrl().length());
		
		//URL Depth - from (Nallapati 2004)
		double doc1UrlDepth = (doc1.getUrl().split("/").length - 2) > 0 ? (1.0 / (doc1.getUrl().split("/").length - 2)) : 0;
		double doc2UrlDepth = (doc2.getUrl().split("/").length - 2) > 0 ? (1.0 / (doc2.getUrl().split("/").length - 2)) : 0;
		out += "\t" + (doc1UrlDepth - doc2UrlDepth);
		
		//Link Factor
		out += "\t" + (getLinkFactor(doc1, RModel) - getLinkFactor(doc2, RModel));
		
		//from (Nallapati 2004) - Fig 2
		out += "\t" + (getNallapatiFeature(1, doc1, query, RModel) - getNallapatiFeature(1, doc2, query, RModel));
		out += "\t" + (getNallapatiFeature(2, doc1, query, RModel) - getNallapatiFeature(2, doc2, query, RModel));
//		out += "\t" + (getNallapatiFeature(3, doc1, query, RModel) - getNallapatiFeature(3, doc2, query, RModel));
		out += "\t" + (getNallapatiFeature(5, doc1, query, RModel) - getNallapatiFeature(5, doc2, query, RModel));
		
		//tf.idf
		out += "\t" + (getTfIdf(doc1, query, RModel) - getTfIdf(doc2, query, RModel));
		
//		for(DocFeature feature : swmap1.keySet()) {
			//System.out.println( feature+": Doc 1"+ swmap1.get(feature)+ "Doc 2 :"+swmap2.get(feature));		
//			double B = RModel.getWeight("task3_B");
//			double smMultiplier  = 1+(B-1)*Math.exp(-1*sm/B);
			
//		}
		
		return out;
	}

	private double getNallapatiFeature(int featureId, DocData doc, QueryData query, RankingModel rModel/*, List<QueryDocData> docQueryAllList*/) {
		
		double value = 0;
		ArrayList<String> uniqTerms = query.GetTerms();
//		Map<String, Integer> totalTermCount = getTermCollectionCount(uniqTerms, docQueryAllList);
		for(String term : uniqTerms) {
			double idf = rModel.getDocumentFrequency(term) > 0 ? Math.log( (1.0 * rModel.getNumTrainingDocs()) / rModel.getDocumentFrequency(term) ) : 0;
			double tf = 1.0 * doc.getRawTFBody(term);
			switch(featureId) {
			//Sum log(c(q_i, D))
			case 1:
				value += tf > 0 ? Math.log(tf) : 0;
				break;
			//Sum log(1 + c(q_i, D)/|D|)
			case 2:
				value += tf > 0 ? Math.log(1 + tf/doc.getBodyLength()) : 0;
				break;
			//Sum log(idf(q_i))
			case 3:
				value += idf;
				break;
			//Sum log( |C| / c(q_i, C) )
//			case 4:
//				value += totalTermCount.get(term) > 0 ? Math.log( (1.0*rModel.getNumTrainingDocs()) / (totalTermCount.get(term))) : 0;
//				break;
			//Sum log( 1 + (c(q_i, D) / |D|) * idf(q_i) )
			case 5:
				if (doc.getBodyLength() == 0)
					value += 0;
				else	
					value += Math.log(1.0 + (tf / doc.getBodyLength()) * idf);
				break;
			//Sum log( 1 + (c(q_i, D) / |D|) * (|C| / c(q_i, C)) )
//			case 6:
//				value += Math.log(1.0 + (tf/doc.getBodyLength()) * ( (1.0*rModel.getNumTrainingDocs()) / totalTermCount.get(term)));
//				break;
			}
			
		}
		return value;
	}

	private Map<String, Integer> getTermCollectionCount(ArrayList<String> uniqTerms, List<QueryDocData> docQueryAllList) {
		Map<String, Integer> totalTermCount = new HashMap<String, Integer>();
		for(String term : uniqTerms) {
			totalTermCount.put(term, 0);
		}
		for (QueryDocData QDoc : docQueryAllList) {
			for(DocData doc : QDoc.getDocData()) {
				for(String term : uniqTerms) {
					totalTermCount.put(term, totalTermCount.get(term) + doc.getRawTFBody(term));
				}
			}
		}
		return totalTermCount;
	}

	private double getLinkFactor(DocData doc, RankingModel rModel) {
		//From Discriminative models for information retrieval (Nallapati 2004)
		//Log (1 +  (num-links(D) / Avg Num Links))
		return Math.log(1.0 + ( getTotalAnchorCount(doc) / rModel.getAverageLength(DocFeature.ANCHOR) ) );
	}

	private double getTfIdf(DocData doc, QueryData query, RankingModel rModel) {
		
		double tfidf = 0;
		
		for(String term : query.GetTerms()) {
			double idf = Math.log(1.0 * (1.0 + rModel.getNumTrainingDocs()) / (1.0 + rModel.getDocumentFrequency(term)));
			tfidf += doc.getRawTFBody(term) * idf;
			
		}
		return tfidf;
	}

	public String getFeatureProducts(DocData doc1, DocData doc2,
			QueryData query) {

		List<String> termVector = new ArrayList<String>();
		List<Double> queryVector = Vectorizer.getQueryVector(
				query.GetQueryString(), termVector,
				TermSmoothingType.LOGARITHM, DocSmoothingType.IDF, this.RModel);

		Map<DocFeature, List<Double>> docVector1 = Vectorizer
				.getDocumentVector(doc1, termVector,
						TermSmoothingType.LOGARITHM, DocSmoothingType.NONE,
						NormalizationType.LENGTH, this.RModel);
		Map<DocFeature, List<Double>> docVector2 = Vectorizer
				.getDocumentVector(doc2, termVector,
						TermSmoothingType.LOGARITHM, DocSmoothingType.NONE,
						NormalizationType.LENGTH, this.RModel);

		double FeatureURL =  VectorUtil.dot(queryVector,docVector1.get(DocFeature.URL)) -  VectorUtil.dot(queryVector,docVector2.get(DocFeature.URL));		
		double FeatureTitle =  VectorUtil.dot(queryVector,docVector1.get(DocFeature.TITLE)) -  VectorUtil.dot(queryVector,docVector2.get(DocFeature.TITLE));
		double FeatureBody =  VectorUtil.dot(queryVector,docVector1.get(DocFeature.BODY)) -  VectorUtil.dot(queryVector,docVector2.get(DocFeature.BODY));
		double FeatureHeader =  VectorUtil.dot(queryVector,docVector1.get(DocFeature.HEADER)) -  VectorUtil.dot(queryVector,docVector2.get(DocFeature.HEADER));
		double FeatureAnchor =  VectorUtil.dot(queryVector,docVector1.get(DocFeature.ANCHOR)) -  VectorUtil.dot(queryVector,docVector2.get(DocFeature.ANCHOR));

		
		/*
		double FeatureURL = VectorUtil.dot(
				queryVector,
				VectorUtil.subtract(docVector1.get(DocFeature.URL),
						docVector2.get(DocFeature.URL)));
		double FeatureTitle = VectorUtil.dot(queryVector, VectorUtil.subtract(
				docVector1.get(DocFeature.TITLE),
				docVector2.get(DocFeature.TITLE)));
		double FeatureBody = VectorUtil.dot(
				queryVector,
				VectorUtil.subtract(docVector1.get(DocFeature.BODY),
						docVector2.get(DocFeature.BODY)));
		double FeatureHeader = VectorUtil.dot(queryVector, VectorUtil.subtract(
				docVector1.get(DocFeature.HEADER),
				docVector2.get(DocFeature.HEADER)));
		double FeatureAnchor = VectorUtil.dot(queryVector, VectorUtil.subtract(
				docVector1.get(DocFeature.ANCHOR),
				docVector2.get(DocFeature.ANCHOR)));
		 */
		// String OutPut = formatter.format(FeatureURL) + "\t"
		// + formatter.format(FeatureTitle) + "\t" + FeatureBody + "\t"
		// + formatter.format(FeatureHeader) + "\t" +
		// formatter.format(FeatureAnchor) + "\n";
		String OutPut = FeatureURL + "\t" + FeatureTitle + "\t" + FeatureBody
				+ "\t" + FeatureHeader + "\t" + FeatureAnchor;

		return OutPut;
	}

	public String GenerataComprehensiveFeature(DocData doc, QueryData query) {
		// TODO Auto-generated method stub
		return null;
	}

	public static List<String> getPairs(int size, boolean DoPermute ) {
		// int size = 3;
		List<String> AllPairs = new ArrayList<String>();

		for (int i = 0; i < size; i++) {
			// getPairs(i, size,AllPairs);
			for (int j = 0; j < size; j++) {
				if (j != i) {
					
					String s1 = i + "," + j;
					String s2 = j + "," + i;
					if (i < j &&  DoPermute== false){
						if (!AllPairs.contains(s1))
								AllPairs.add(s1);
					}
					else if(DoPermute== true ){
						if (!AllPairs.contains(s1))
							AllPairs.add(s1);
						if (!AllPairs.contains(s2))
							AllPairs.add(s2);
						}
					}				
				}
			}
		return AllPairs;
	}
	private int getTotalAnchorCount(DocData doc1) {

		Map<String, Integer> anchorTextCountMap = doc1.getAnchorTextCountMap();
		int totalCount = 0;
		for(int i : anchorTextCountMap.values()) {
		totalCount += i;
		}
		return totalCount;
		}

}
