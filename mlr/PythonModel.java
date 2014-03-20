package cs276.programming.mlr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import cs276.programming.util.Parameters;

public class PythonModel {

	/**
	 * @param args
	 */

	boolean MakePrediction(String FeatureFile,String OutputFile , String mType){
		
		String PythonCommand = "python " + Parameters.BASE_PATH + "PA4/release/PredictModel.py";
		  try
	        {
	            Runtime rt2 = Runtime.getRuntime();
	            Process pr2 = rt2.exec(PythonCommand+" "+FeatureFile+" "+OutputFile+" "+mType);
	            pr2.waitFor();
	            BufferedReader br = new BufferedReader(new InputStreamReader(pr2.getInputStream()));
	            dumpStream(pr2.getErrorStream());
	            dumpStream(pr2.getInputStream());
//	            System.err.printf("exit value %d for '%s'\n", pr2.exitValue(), PythonCommand);
	        }
	        catch(Exception e)
	        {
	            System.out.println(e.toString());
	            e.printStackTrace();
	        }
		return true;
	}
	
	boolean RunTraining(String FeatureFile,String LabelFile , String mType){
		
		String PythonCommand = "python " + Parameters.BASE_PATH + "PA4/release/TrainModel.py";
		  try
	        {
	            Runtime rt2 = Runtime.getRuntime();
	            String cmd = PythonCommand + " " + FeatureFile + " " + LabelFile + " " + mType;
	            System.err.printf("invoking '%s'\n", cmd);
				Process pr2 = rt2.exec(cmd);
	            pr2.waitFor();
	            dumpStream(pr2.getErrorStream());
	            dumpStream(pr2.getInputStream());
	            System.err.printf("exit value %d for '%s'\n", pr2.exitValue(), cmd);
	        }
	        catch(Exception e)
	        {
	            System.out.println(e.toString());
	            e.printStackTrace();
	        }
		return true;

	}

	private void dumpStream(InputStream stream) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(stream));
		String line = "";
		while ((line = br.readLine()) != null)
		{
		    System.out.println(line);
		}
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		PythonModel rm = new PythonModel();
		String FeatureFile = Parameters.BASE_PATH + "PA4/release/tem_feature.txt";
		String FeatureLabel = Parameters.BASE_PATH + "PA4/release/outut.txt";
		
		//boolean x = rm.RunTraining(FeatureFile,FeatureLabel,"LINEAR");
		
		boolean y = rm.MakePrediction(FeatureFile,FeatureLabel,"LINEAR");
	}
	

}
