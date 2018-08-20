package svm;

import global.GlobalVar;

import java.io.*;

public class LIBlinearTestLabelKnown {
    //may need to be changed

    String codePath = GlobalVar.codePath;
    boolean testing = GlobalVar.testing;
    String LIBlinearPath = GlobalVar.LIBlinearPath;
    //server

    String featurePath = codePath + "results/svmFeature/";
    String resultPath = codePath + "results/svmResults/";
    String checkDataPath = LIBlinearPath + "tools/checkdata.py";
    String gridSearchPath = LIBlinearPath + "tools/grid.py";
    String trainPath = LIBlinearPath + "train";
    String predictPath = LIBlinearPath + "predict";
    //n-fold validation
    int n = 5;
    String trainingDataFile = featurePath + "training.txt";
    String testDataFile;
    String modelPath = resultPath + "model.txt";
    String resultOutputPath = resultPath + "out.txt";
    public double avgAccuracy = 0;
    public String wrongExamples = "";

    public LIBlinearTestLabelKnown(int n) {
	this.n = n;
//		for(int testData=0; testData<n; testData++){
//			testDataFile = featurePath + testData + ".txt";
//			checkDataFormat(testDataFile);
//		}

	double accuracySum = 0;
	double recordNumSum = 0;
	//do n-fold cross-validation
	for (int testData = 0; testData < n; testData++) {
	    testDataFile = featurePath + testData + ".txt";
	    resultOutputPath = resultPath + testData + "out.txt";

	    generateTrainingData(testData);
	    double bestC = gridSearch("-6,3,1");
	    double logC = Math.log(bestC) / Math.log(2);
	    double start = logC - 0.5;
	    double end = logC + 0.5;
	    bestC = gridSearch(start + "," + end + ",0.1");

	    String weight1 = "1";
//			String weight2 = "1";
	    String weight2 = "1";
//			System.out.println(bestC);
	    trainModel(bestC, weight1, weight2);
	    double[] results = predictData();
	    accuracySum += results[0] * results[1];
	    recordNumSum += results[1];

	}

	avgAccuracy = accuracySum / recordNumSum;
	System.out.println("Avg accuracy: " + avgAccuracy);
    }

    double[] predictData() {
	double[] results = new double[2];
	Runtime rt = Runtime.getRuntime();
	String[] cmd = {predictPath, testDataFile, modelPath, resultOutputPath};
	Process p;
	try {
	    p = rt.exec(cmd);
	    InputStream in = p.getInputStream();

	    InputStreamReader isr = new InputStreamReader(in);
	    BufferedReader br = new BufferedReader(isr);
	    String line;

	    while ((line = br.readLine()) != null) {
		if(testing){
		    System.out.println(line);
		}

		String[] resultsStr = line.split(" ");
		String accuracyStr = resultsStr[2];
		//accuracy
		results[0] = Double.parseDouble(accuracyStr.substring(0, accuracyStr.length() - 1));
		//number of records
		String casesStr = resultsStr[3].split("/")[1];
		results[1] = Double.parseDouble(casesStr.substring(0, casesStr.length() - 1));
//			  System.out.println(results[0] + "\t" + results[1]);
	    }
	    p.waitFor();
	    br.close();
	    isr.close();
	    in.close();
	    p.destroy();
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	return results;
    }

    void trainModel(double bestC, String weight1, String weight2) {
	Runtime rt = Runtime.getRuntime();
	String cmd = trainPath + " -c " + bestC + " -w1 " + weight1 + " -w-1 " + weight2 + " " + trainingDataFile + " " + modelPath;

	Process p;
	try {
	    p = rt.exec(cmd);

	    InputStream in = p.getInputStream();
	    InputStreamReader isr = new InputStreamReader(in);
	    BufferedReader br = new BufferedReader(isr);
	    String line;
	    while ((line = br.readLine()) != null) {
//				System.out.println(line);
	    }
	    p.waitFor();
	    br.close();
	    isr.close();
	    in.close();

	    p.destroy();
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    double gridSearch(String log2CRange) {
	double bestC = 0;
	Runtime rt = Runtime.getRuntime();
	String[] cmd = {gridSearchPath, "-log2c", log2CRange, "-log2g", "null", "-svmtrain", trainPath, trainingDataFile};
	Process p;
	try {
	    p = rt.exec(cmd);
	    InputStream in = p.getInputStream();

	    InputStreamReader isr = new InputStreamReader(in);
	    BufferedReader br = new BufferedReader(isr);
	    String line;
	    String lastLine = null;
	    while ((line = br.readLine()) != null) {
//				System.out.println(line);
		lastLine = line;
	    }
	    bestC = Double.parseDouble(lastLine.split(" ")[0]);
//			System.out.println(bestC);
	    p.waitFor();
	    br.close();
	    isr.close();
	    in.close();
	    p.destroy();
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

	return bestC;
    }

    void checkDataFormat(String file) {
	Runtime rt = Runtime.getRuntime();
	String[] cmd = {checkDataPath, file};
	Process p;
	try {
	    p = rt.exec(cmd);
	    InputStream in = p.getInputStream();

	    InputStreamReader isr = new InputStreamReader(in);
	    BufferedReader br = new BufferedReader(isr);
	    String line;

	    while ((line = br.readLine()) != null) {
//			  System.out.println(line);
		if (!line.equals("No error.")) {
		    System.err.println(line);
		    System.exit(1);
		}
	    }
	    p.waitFor();
	    br.close();
	    isr.close();
	    in.close();
	    p.destroy();
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

    }

    void generateTrainingData(int testData) {

	try {
	    // Create file
	    FileWriter fstreamOut = new FileWriter(trainingDataFile);
	    BufferedWriter out = new BufferedWriter(fstreamOut);

	    for (int i = 0; i < n; i++) {
		if (i == testData) {
		    continue;
		}

		String filePath = featurePath + i + ".txt";
		FileInputStream fstream = new FileInputStream(filePath);

		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine;

		while ((strLine = br.readLine()) != null) {

		    out.write(strLine + "\n");
		}
		br.close();
		in.close();
		fstream.close();
	    }

	    // Close the output stream
	    out.close();
	    fstreamOut.close();
	} catch (Exception e) {// Catch exception if any
	    System.err.println("Error: " + e.getMessage());
	}


    }

    public static void main(String[] args) {
	new LIBlinearTestLabelKnown(10);
    }
}
