package cs276.programming.query;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;

import cs276.programming.util.Parameters;

public class Query {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Parameters.DEBUG = false;
		Parameters.IndexType IType = Parameters.IndexType.NO_COMPRESSION;

		// String IndexDir
		// ="/Users/sdey/Documents/sdey_personal/apps/workspace/CS276/tempindex";
		String IndexDir = "";
		if (args.length <= 0) {
			// for (int i=0;i< args.length;i++){
			// System.err.println(i+":"+args[i]);
			// }
			System.err
					.println("Proper Usage is: java -cp pa1.jar cs276.programming.query.Query <index_dir>");
			System.exit(1);
		} else {

			IndexDir = args[0];
			File getDir = new File(IndexDir);
			if (!getDir.exists()) {
				System.err.println(IndexDir
						+ " does not exist. Please enter the right dir.");
				System.exit(1);
			}
			int IxType = Integer.parseInt(args[1]);
			if(IxType < 1 || IxType > 3) {
				System.err.println("Index type can be 1 or 2 or 3.");
				System.exit(1);
			}
			

			if (IxType == 1) {
				IType = Parameters.IndexType.NO_COMPRESSION;
			} else if (IxType == 2) {
				IType = Parameters.IndexType.VAR_BYTE_GAP_ENCODING;
			} else if (IxType == 3) {
				IType = Parameters.IndexType.GAMMA_BIT_ENODED;
			}

		}

		// Initialize the runtime there will be only instance with all the data

		RuntimeIndex Rtime = RuntimeIndex.getInstance(IndexDir, IType);

		QueryProcessor QProcessor = new QueryProcessor(Rtime);
		ArrayList<String> QResults = new ArrayList<String>();

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String QueryString;

		while (true) {
			// System.out.println("Please enter a query ( press enter to exit) : ");
			try {
				QueryString = br.readLine();
				if (QueryString == null || QueryString == ""
						|| QueryString.toString().length() == 0) {
					// System.out.println("Program ends. Bye !");
					break;
				} else {
					long start_time = System.currentTimeMillis();

					QResults = QProcessor.runQuery(QueryString);
					Collections.sort(QResults);
					int docCount = QResults.size();

					if (docCount == 0) {
						System.out.println("no results found");
					} else {
						for (String s : QResults)
							System.out.println(s);
					}
					long end_time = System.currentTimeMillis();
					long difference = end_time - start_time;
					if (Parameters.DEBUG) {
						System.out.println("Time taken in milli sec : "
								+ difference);
						System.out.println("No of documents : " + docCount);
					}
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// System.out.println("End of the query process");

	}
}
