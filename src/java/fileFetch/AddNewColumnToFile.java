package fileFetch;

import java.io.*;


public class AddNewColumnToFile {
	
	//input
	String inputFile;
	//output
	String outputFile;
	
	String newColumnVal = "0";
	
	AddNewColumnToFile(String fileName){
		inputFile = "data/" + fileName;
		outputFile = "data/process_" + fileName;
		
		readFile();
	}
	
	void readFile(){
		try {

			FileInputStream fstream = new FileInputStream(inputFile);

			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF8"));
			
			//output
			FileWriter fstreamout = new FileWriter(outputFile);
			BufferedWriter out = new BufferedWriter(fstreamout);
			
			
			String strLine;
			
			while ((strLine = br.readLine()) != null) {

//				System.out.println(strLine);
				String[] elements = strLine.split("\t");
				int len = elements.length;
				if(len > 1){
					String text = elements[len-1];
					System.out.println(text);
					
					
					for(int i=0; i<len-1; i++){
//						out.write(elements[i] + "\t");
					}
//					out.write(newColumnVal + "\t" + text + "\n");
					out.write(elements[1] + "\t" + text + "\n");
				}
			}

			out.close();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	

	
	public static void main(String[] argus) {
		new AddNewColumnToFile("iphone_inuse_allcolumn.txt");
		new AddNewColumnToFile("kobebryant_inuse_allcolumn.txt");
		new AddNewColumnToFile("ladygaga_inuse_allcolumn.txt");
		new AddNewColumnToFile("obama_inuse_allcolumn.txt");
	}

}
