package process;

import java.io.*;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;


public class DivideFeature {
	
	String inputFeatureFile;
	String outputFeaturePath;
	//divide data into n-fold 
	int n = 5;
	
	Random random = new Random();
	
	FileWriter[] fstreamOut;
	BufferedWriter[] out;

	FileWriter[] fstreamOutID;
	BufferedWriter[] outID;
	
	int maxDataNumPerFile;
	int[] currentDataNum;
	
	DivideFeature(int n, String inputFeatureFile, String outputFeaturePath){
		this.n = n;
		this.inputFeatureFile = inputFeatureFile;
		this.outputFeaturePath = outputFeaturePath;
		
		fstreamOut = new FileWriter[n];
		out = new BufferedWriter[n];

		fstreamOutID = new FileWriter[n];
		outID = new BufferedWriter[n];
		
		currentDataNum = new int[n];
		
		openfile();
		
		int dataNum = countLine(inputFeatureFile);
		maxDataNumPerFile = dataNum/n + 1;
		readFile(inputFeatureFile);
		
		closefile();
	}
	
	int countLine(String filename){
	    InputStream is;
	    try {
	    	is = new BufferedInputStream(new FileInputStream(filename));
	        byte[] c = new byte[1024];
	        int count = 0;
	        int readChars = 0;
	        while ((readChars = is.read(c)) != -1) {
	            for (int i = 0; i < readChars; ++i) {
	                if (c[i] == '\n')
	                    ++count;
	            }
	        }
	        is.close();
	        return count;
	    }catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
	
	
	void readFile(String filePath){
		try {

			FileInputStream fstream = new FileInputStream(filePath);

			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			
			List<Integer> writeFileNumList = new LinkedList<Integer>();
			while ((strLine = br.readLine()) != null) {
				int writeFileNum;
				while(true){
					writeFileNum = random.nextInt(n);
					if(currentDataNum[writeFileNum] < maxDataNumPerFile){
						break;
					}
				}
				String[] elements = strLine.split("\t");
				writeFileNumList.add(writeFileNum);
				out[writeFileNum].write(elements[1] + "\n");
				currentDataNum[writeFileNum] ++;
			}
			br.close();
			in.close();
			fstream.close();
			
			
			fstream = new FileInputStream(filePath);

			in = new DataInputStream(fstream);
			br = new BufferedReader(new InputStreamReader(in));

			Iterator<Integer> it = writeFileNumList.iterator();
			while ((strLine = br.readLine()) != null) {
				String[] elements = strLine.split(" ", 2);
				int writeFileNum = it.next();
				outID[writeFileNum].write(elements[0] + "\n");
			}
			br.close();
			in.close();
			fstream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	void openfile() {
		try {
			for(int i=0; i<n; i++){
				fstreamOut[i] = new FileWriter(outputFeaturePath+i+".txt");
				out[i] = new BufferedWriter(fstreamOut[i]);
				
				fstreamOutID[i] = new FileWriter(outputFeaturePath+i+"text.txt");
				outID[i] = new BufferedWriter(fstreamOutID[i]);
			}

		} catch (Exception e) {// Catch exception if any
			e.printStackTrace();
		}
	}
	
	
	void closefile() {
		// Close the output stream
		try {
			
			for(int i=0; i<n; i++){
				out[i].close();
				fstreamOut[i].close();
				
				outID[i].close();
				fstreamOutID[i].close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args){
		new DivideFeature(5, "", "");
	}
}
