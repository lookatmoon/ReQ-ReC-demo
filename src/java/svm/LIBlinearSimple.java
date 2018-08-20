package svm;

import global.GlobalVar;

import java.io.*;

public class LIBlinearSimple {
	//may need to be changed
	String codePath = GlobalVar.codePath;
	String LIBlinearPath = GlobalVar.LIBlinearPath;
	//server
        String featurePath = codePath + "results/svmFeature/";
	String resultPath = codePath + "results/svmResults/";
	
	
	String checkDataPath = LIBlinearPath+"tools/checkdata.py";
	String gridSearchPath = LIBlinearPath+"tools/grid.py";
	String trainPath = LIBlinearPath+"train";
	String predictPath = LIBlinearPath+"predict";
	

	String trainingDataFile = featurePath + "0.txt";
	String testDataFile = featurePath + "1.txt";
	String modelPath = resultPath + "model.txt";
	String resultOutputPath = resultPath + "out.txt";

	
	LIBlinearSimple(){
		
//		checkDataFormat(trainingDataFile);
//		checkDataFormat(testDataFile);
//		double bestC = 0.0192366314585;//default c
		double bestC = 0.101531549545;//default c
		String weight1 = "1";
//		String weight2 = "1";
		String weight2 = "1";
		
//		bestC = gridSearch("-6,3,1");
//		double logC = Math.log(bestC)/Math.log(2);
//		double start = logC-0.5;
//		double end = logC+0.5;
//		bestC = gridSearch(start + "," + end + ",0.1");
		trainModel(bestC, weight1, weight2);
		predictData();

	}
	
	void predictData(){
		String cmd = predictPath +" " + testDataFile +" " + modelPath +" " + resultOutputPath;
		System.out.println(cmd);
		Process p;
		try {
			p = Runtime.getRuntime().exec(cmd);
			InputStream in = p.getInputStream();

			InputStreamReader isr = new InputStreamReader(in);
			BufferedReader br = new BufferedReader(isr);
			String line;

			while ((line = br.readLine()) != null) {
			  System.out.println(line);
			}
			p.waitFor();
			br.close();
			isr.close();
			in.close();
			p.destroy();
		}catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	void trainModel(double bestC, String weight1, String weight2){
//		String[] cmd = {trainPath, "-c", bestC+"", "-w1", weight1, "-w2", weight2, trainingDataFile, modelPath};
		String cmd = trainPath + " -c "+ bestC + " -w1 " + weight1  + " -w-1 " + weight2 +  " " +trainingDataFile + " " + modelPath;
		 System.out.println(cmd);
		Process p;
		try {
			p = Runtime.getRuntime().exec(cmd);
			
			InputStream in = p.getInputStream();
			InputStreamReader isr = new InputStreamReader(in);
			BufferedReader br = new BufferedReader(isr);
//			while (br.readLine() != null);
			String line;

			while ((line = br.readLine()) != null) {
			  System.out.println(line);
			}
			p.waitFor();
			
			br.close();
			isr.close();
			in.close();

			p.destroy();
		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	double gridSearch(String log2CRange){
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
				System.out.println(line);
				lastLine = line;
			}
			bestC = Double.parseDouble(lastLine.split(" ")[0]);
//			System.out.println(bestC);
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
	
	void checkDataFormat(String file){
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
			  System.out.println(line);
			  if(!line.equals("No error.")){
				  System.err.println(line);
				  System.exit(1);
			  }
			}
			br.close();
			isr.close();
			in.close();
			p.destroy();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	

	
	public static void main(String[] args) {
		new LIBlinearSimple();
	}
}
