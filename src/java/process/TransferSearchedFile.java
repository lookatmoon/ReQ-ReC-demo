/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package process;

import fileFetch.RW;
import java.io.*;
import java.util.*;

public class TransferSearchedFile {

    int idIndex = 1;
    HashMap<Long, String> id_to_doc = new HashMap<Long, String>();
    int tweetGetInOuterLoop = 0;
    HashSet<Long> alreadyRetrievedIDs = new HashSet<Long>();

    TransferSearchedFile(PathConfig pathConfig, boolean isFirstSearch) {
	if (isFirstSearch) {
	    copyFile(pathConfig.remoteOriginalFile, pathConfig.originalFile);

	    File trainFile = new File(pathConfig.trainDataFile);
	    if (trainFile.exists()) {
		getLabeledData(pathConfig);
		updateWrongLabelInEva(pathConfig);
		trainFile.delete();
//		getLabeledData(pathConfig.trainDataFile);
//		updateTestFile(pathConfig);
	    }

	    makeDir(pathConfig.backupPath);
	    tweetGetInOuterLoop = copyFile(pathConfig.remoteOriginalFile, pathConfig.completeRetrievedData);
	    
	    File trainPreFile = new File(pathConfig.trainInPreLoop);
	    if (trainPreFile.exists()) {
		trainPreFile.delete();
	    }
	} else {

	    getLabeledData(pathConfig);
	    getCompleteData(pathConfig.completeRetrievedData);
	    updateCompleteData(pathConfig.trainDataFile, pathConfig.completeRetrievedData);
	    updateCompleteData(pathConfig.testDataDir, pathConfig.completeRetrievedData);
	    updateWrongLabelInEva(pathConfig);
	    tweetGetInOuterLoop = appendTrainToOriginal(pathConfig);
	}
    }

    void makeDir(String path) {
	File queryDir = new File(path);
	if (!queryDir.exists()) {
	    queryDir.mkdir();
	}
    }

    void updateCompleteData(String readFile, String writeFile) {
	RW read = new RW(readFile, 'r');
	RW write = new RW(writeFile, 'a');
	BufferedWriter out = write.out;
	try {
	    BufferedReader br = read.br;
	    String strLine;
	    while ((strLine = br.readLine()) != null) {

		String[] elements = strLine.split("\t");
		if (elements.length > 1) {
		    long id = Long.parseLong(elements[idIndex]);
		    if (!alreadyRetrievedIDs.contains(id)) {
			out.write(strLine + "\n");
			alreadyRetrievedIDs.add(id);
		    }
		}
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	}
	read.close();
	write.close();
    }

    void updateWrongLabelInEva(PathConfig pathConfig) {
	String evaBackUpFile = pathConfig.evaluationFile + ".bak";
	copyFile(pathConfig.evaluationFile, evaBackUpFile);
	RW read = new RW(evaBackUpFile, 'r');
	RW write = new RW(pathConfig.evaluationFile, 'w');
	BufferedWriter out = write.out;
	HashMap<Long, String> id_to_strLine = new HashMap<Long, String>(); //to remove duplicates in eva file
	try {
	    BufferedReader br = read.br;
	    String strLine;
	    while ((strLine = br.readLine()) != null) {

		String[] elements = strLine.split("\t");
		if (elements.length > 1) {
		    long id = Long.parseLong(elements[1]);
		    String strInTrain = id_to_doc.get(id);
		    if (strInTrain == null) {
			id_to_strLine.put(id, strLine);
//			out.write(strLine + "\n");
		    } else {
			id_to_strLine.put(id, strInTrain);
//			out.write(strInTrain + "\n");
		    }
		}
	    }
	    
	    
	    for(Map.Entry<Long, String>entry : id_to_strLine.entrySet()){
		out.write(entry.getValue() + "\n");
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	}
	read.close();
	write.close();

    }

    void updateTestFile(PathConfig pathConfig) {
	RW read = new RW(pathConfig.originalFile, 'r');

	String testDataDir = pathConfig.testDataDir;

	RW writeTest = new RW(testDataDir, 'w');
	BufferedWriter outTest = writeTest.out;

	try {
	    BufferedReader br = read.br;
	    String strLine;
	    while ((strLine = br.readLine()) != null) {
		String[] elements = strLine.split("\t");
		if (elements.length > 1) {
		    long id = Long.parseLong(elements[idIndex]);
		    String trainData = id_to_doc.get(id);
		    if (trainData == null) {
			outTest.write(strLine + "\n");
		    }
		}

	    }

	} catch (Exception e) {
	    e.printStackTrace();
	}
	read.close();
	writeTest.close();

    }

    int appendTrainToOriginal(PathConfig pathConfig) {
	RW read = new RW(pathConfig.remoteOriginalFile, 'r');
	RW write = new RW(pathConfig.originalFile, 'w');
	BufferedWriter out = write.out;

	RW writeTest = new RW(pathConfig.testDataDir, 'w');
	BufferedWriter outTest = writeTest.out;

	int tweetGetInOuterLoop = 0;
	try {
	    BufferedReader br = read.br;
	    String strLine;
	    while ((strLine = br.readLine()) != null) {
		String[] elements = strLine.split("\t");
		if (elements.length > 1) {
		    long id = Long.parseLong(elements[idIndex]);
		    String trainData = id_to_doc.get(id);
		    if (!alreadyRetrievedIDs.contains(id)) {
			out.write(strLine + "\n");
			outTest.write(strLine + "\n");

			tweetGetInOuterLoop++;
		    } else if (trainData != null) {
			id_to_doc.remove(id);
			out.write(trainData + "\n");
		    }
		}

	    }

	    //write rest training data to original file
	    for (Map.Entry<Long, String> entry : id_to_doc.entrySet()) {
		out.write(entry.getValue() + "\n");
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	}


	read.close();
	write.close();
	writeTest.close();
	return tweetGetInOuterLoop;
    }

    void getCompleteData(String file) {
	RW read = new RW(file, 'r');
	try {
	    BufferedReader br = read.br;
	    String strLine;
	    while ((strLine = br.readLine()) != null) {

		String[] elements = strLine.split("\t");
		if (elements.length > 1) {
		    long id = Long.parseLong(elements[idIndex]);
		    alreadyRetrievedIDs.add(id);
		}
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	}
	read.close();
    }

    void getLabeledData(PathConfig pathConfig) {
	RW read = new RW(pathConfig.trainDataFile, 'r');
	RW write = new RW(pathConfig.trainInPreLoop, 'w');
	BufferedWriter out = write.out;
	try {
	    BufferedReader br = read.br;
	    String strLine;
	    while ((strLine = br.readLine()) != null) {

		String[] elements = strLine.split("\t");
		if (elements.length > 1) {
		    long id = Long.parseLong(elements[idIndex]);
		    id_to_doc.put(id, strLine);
		    out.write(id+"\n");
		}
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	}
	read.close();
	write.close();
    }

    int copyFile(String readFile, String writeFile) {
	RW read = new RW(readFile, 'r');
	RW write = new RW(writeFile, 'w');
	BufferedWriter out = write.out;
	int tweetGetInOuterLoop = 0;
	try {
	    BufferedReader br = read.br;
	    String strLine;
	    while ((strLine = br.readLine()) != null) {

		out.write(strLine + "\n");
		tweetGetInOuterLoop++;
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	}
	read.close();
	write.close();
	return tweetGetInOuterLoop;
    }
}
