package fileFetch;

import java.io.*;

public class RWfileTest {

    RWfileTest() {
	String file = "data/dataAll.txt";
	String outFile = "data/test.txt";
	String file1 = "data/rawdata/test1.txt";
	String file2 = "data/rawdata/test2.txt";

	readFile(file);
	readTwoFiles(file1, file2);
	readFileEncoding(file);

	writeFile(outFile);
	writeFileEncoding(outFile);
	appendFile(outFile);
	writeLargeFile(outFile);
	writeArray();

	readWriteFile(file, outFile);

	RW.print("test\t");
	RW.pLine("test");

	int lineCnt = new RW(file, 'c').lineCnt;
	RW.pLine(lineCnt);
    }

    void writeArray() {
	int arrayLen = 2;
	RW[] writeArray = new RW[arrayLen];

	initWriteArray(arrayLen, writeArray);
	writeArray[0].wLine("test");
	writeArray[1].wLine("test");

	closeWriteArray(arrayLen, writeArray);
    }

    void closeWriteArray(int arrayLen, RW[] writeArray) {
	for (int i = 0; i < arrayLen; i++) {
	    writeArray[i].close();
	}
    }

    void initWriteArray(int arrayLen, RW[] writeArray) {
	for (int i = 0; i < arrayLen; i++) {
	    writeArray[i] = new RW("data/test" + i + ".txt", 'w');
	}
    }

    void readWriteFile(String readFile, String writeFile) {
	RW read = new RW(readFile, 'r');
	RW write = new RW(writeFile, 'w');
	BufferedWriter out = write.out;
	try {
	    BufferedReader br = read.br;
	    String strLine;
	    while ((strLine = br.readLine()) != null) {


		String[] elements = strLine.split("\t");
		if (elements.length > 1) {
		    long id = Long.parseLong(elements[1]);
		    double posProb = Double.parseDouble(elements[2]);
		    int index = Integer.parseInt(elements[0]);
		    String text = elements[3];
		    out.write(id + "\t" + index + "\t" + posProb + "\t" + text + "\n");
		}
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	}
	read.close();
	write.close();
    }

    void writeLargeFile(String writeFile) {
	RW write = new RW(writeFile, 'w');
	BufferedWriter out = write.out;
	try {
	    out.write("\n");
	} catch (Exception e) {
	    e.printStackTrace();
	}
	write.close();
    }

    void writeFile(String writeFile) {
	RW write = new RW(writeFile, 'w');
	write.write("test\t");
	write.wLine("test1");
	write.close();
    }

    void writeFileEncoding(String writeFile) {
	RW write = new RW(writeFile, 'w', "UTF8");
	write.write("test\t");
	write.wLine("test1");
	write.close();
    }

    void appendFile(String writeFile) {
	RW write = new RW(writeFile, 'a');
	write.write("append\t");
	write.wLine("append text");
	write.close();
    }

    void readTwoFiles(String file1, String file2) {
	RW read1 = new RW(file1, 'r');
	RW read2 = new RW(file2, 'r');

	try {
	    BufferedReader br1 = read1.br;
	    BufferedReader br2 = read2.br;
	    String strLine1 = null;
	    String strLine2 = null;

	    while ((strLine1 = br1.readLine()) != null && (strLine2 = br2.readLine()) != null) {

		String[] elements1 = strLine1.split("\t");
		String[] elements2 = strLine2.split("\t");
		if (elements1.length > 1 && elements2.length > 1) {
		    long id1 = Long.parseLong(elements1[2]);
		    double posProb1 = Double.parseDouble(elements1[1]);
		    int index1 = Integer.parseInt(elements1[0]);
		    String text1 = elements1[3];
		    System.out.println(id1 + "\t" + index1 + "\t" + posProb1 + "\t" + text1);

		    long id2 = Long.parseLong(elements2[2]);
		    double posProb2 = Double.parseDouble(elements2[1]);
		    int index2 = Integer.parseInt(elements2[0]);
		    String text2 = elements2[3];
		    System.out.println(id2 + "\t" + index2 + "\t" + posProb2 + "\t" + text2);
		} else if (elements1.length <= 1) {
		    System.err.println(file1 + " format is incorrect: " + strLine1);
		} else {
		    System.err.println(file2 + " format is incorrect: " + strLine2);
		}
	    }

	    if (strLine1 != null) {
		System.err.println(file1 + " is longer than " + file2);
	    } else if (br2.readLine() != null) {
		System.err.println(file2 + " is longer than " + file1);
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	}
	read1.close();
	read2.close();
    }

    void readFile(String file) {
	RW read = new RW(file, 'r');
	try {
	    BufferedReader br = read.br;
	    String strLine;
	    while ((strLine = br.readLine()) != null) {


		String[] elements = strLine.split("\t");
		if (elements.length > 1) {
		    long id = Long.parseLong(elements[1]);
		    double posProb = Double.parseDouble(elements[2]);
		    int index = Integer.parseInt(elements[0]);
		    String text = elements[3];
		    System.out.println(id + "\t" + index + "\t" + posProb + "\t" + text);
		}
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	}
	read.close();
    }

    void readFileEncoding(String file) {
	RW read = new RW(file, 'r', "UTF8");
	try {
	    BufferedReader br = read.br;
	    String strLine;
	    while ((strLine = br.readLine()) != null) {


		String[] elements = strLine.split("\t");
		if (elements.length > 1) {
		    long id = Long.parseLong(elements[1]);
		    double posProb = Double.parseDouble(elements[1]);
		    int index = Integer.parseInt(elements[0]);
		    String text = elements[3];
		    System.out.println(id + "\t" + index + "\t" + posProb + "\t" + text);
		}
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	}
	read.close();
    }

    public static void main(String[] args) {
	new RWfileTest();
    }
}
