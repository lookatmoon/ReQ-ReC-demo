package fileFetch;

import java.io.*;

//read or write to file/console
public class RW {
	char openType; //r: read; w: write; c: count lines; p: print to console; a: append contents to file
	
	//reading file resources
	FileInputStream fstream;
	DataInputStream in;
	public BufferedReader br;
	
	//writing file resources
	FileOutputStream fstreamOut;
	OutputStreamWriter outWriter;
	public BufferedWriter out;
	
	public int lineCnt = 0;
	
	public RW(String filePath, char openType){
		this.openType = openType;
		if(openType == 'r'){
			readFileInit(filePath, null);
		}else if(openType == 'w'){
			writeFileInit(filePath, null, false);
		}else if(openType == 'a'){
			writeFileInit(filePath, null, true);
		}else if(openType == 'c'){
			lineCnt = countLine(filePath);
		}
	}
	
	public RW(String filePath, char openType, String encoding){
		this.openType = openType;
		if(openType == 'r'){
			readFileInit(filePath, encoding);
		}else if(openType == 'w'){
			writeFileInit(filePath, encoding, false);
		}else if(openType == 'a'){
			writeFileInit(filePath, encoding, true);
		}
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
			e.printStackTrace();
		}
		return 0;
	}
	
	public static void print(String line){
		System.out.print(line);
	}
	public static void print(int num){
		System.out.print(num);
	}
	public static void print(long num){
		System.out.print(num);
	}
	public static void print(double num){
		System.out.print(num);
	}
	
	public static void pLine(String line){
		System.out.println(line);
	}
	public static void pLine(int num){
		System.out.println(num);
	}
	public static void pLine(long num){
		System.out.println(num);
	}
	public static void pLine(double num){
		System.out.println(num);
	}
	
	void writeFileInit(String filePath, String encoding, boolean append){
		try {
			// Create file
			fstreamOut = new FileOutputStream(filePath, append);
			if(encoding == null){
				outWriter = new OutputStreamWriter(fstreamOut);
			}else{
//				out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileDir), "UTF8"));
				outWriter = new OutputStreamWriter(fstreamOut, encoding);
			}
			
			out = new BufferedWriter(outWriter);

		} catch (Exception e) {// Catch exception if any
			e.printStackTrace();
		}
	}

	void readFileInit(String filePath, String encoding){
		try {

			fstream = new FileInputStream(filePath);
			in = new DataInputStream(fstream);
			if(encoding == null){
				br = new BufferedReader(new InputStreamReader(in));
			}else{
				br = new BufferedReader(new InputStreamReader(in, encoding));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//write line
	public void wLine(String line){
		try {

			out.write(line + "\n");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void write(String line){
		try {

			out.write(line);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
//	String read(){
//		try {
//
//			return br.readLine();
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return null;
//	}
	
	public void close(){
		try {

			if(openType == 'r'){
				br.close();
				in.close();
				fstream.close();
			}else if(openType == 'w' || openType == 'a'){
				out.close();
				outWriter.close();
				fstreamOut.close();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public static void main(String[] args){
		
	}
}
