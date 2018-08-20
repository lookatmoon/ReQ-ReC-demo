package fileFetch;

import java.io.*;
import java.util.*;

public class Merge2FilesByID {
	
	String fileMore = "data/rawdata/label.txt";
	String fileLess = "data/rawdata/text.txt";
	
	String outputFile = "data/rawdata/train.txt";
	
	Merge2FilesByID() throws IOException{
		FileWriter fstreamOut = new FileWriter(outputFile);
		BufferedWriter out = new BufferedWriter(fstreamOut);
		HashMap<Long, String> id_to_data = readFileLess(fileLess);
		
		readFileMore(fileMore, id_to_data, out);
		
		out.close();
		fstreamOut.close();
	}
	

	
	HashMap<Long, String> readFileLess(String filePath){
		HashMap<Long, String> id_to_data = new HashMap<Long, String>();
		try {

			FileInputStream fstream = new FileInputStream(filePath);

			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF8"));

			String strLine;

			while ((strLine = br.readLine()) != null) {

//				System.out.println(strLine);
				String[] elements = strLine.split("\t");
				if(elements.length > 1){
					long id = Long.parseLong(elements[0]);
					id_to_data.put(id, elements[1]);
				}
			}
			br.close();
			in.close();
			fstream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return id_to_data;
	}
	
	void readFileMore(String filePath, HashMap<Long, String> id_to_data, BufferedWriter out){
		try {

			FileInputStream fstream = new FileInputStream(filePath);

			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF8"));

			String strLine;

			while ((strLine = br.readLine()) != null) {

				String[] elements = strLine.split("\t");
				if(elements.length > 1){
					long id = Long.parseLong(elements[1]);
					String data = id_to_data.get(id);
					if(data != null){
						out.write(strLine + "\t" + data + "\n");
					}else{
						RW.pLine("No data");
					}
				}
			}
			br.close();
			in.close();
			fstream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws IOException {
		new Merge2FilesByID();
	}
}
