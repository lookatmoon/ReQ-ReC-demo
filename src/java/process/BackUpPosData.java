/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package process;

import fileFetch.RW;
import global.GlobalVar;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;


public class BackUpPosData {

    boolean testing = GlobalVar.local;
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    String dateAsString = simpleDateFormat.format(new Date());

    BackUpPosData(PathConfig pathConfig, String formulatedQuery) {
	String trainDir = pathConfig.backupPath + "train/";
	String testDir = pathConfig.backupPath + "test/";
	String logFile = pathConfig.backupPath + "log.txt";
	
	makeDir(pathConfig.backupPath);
	makeDir(trainDir);
	makeDir(testDir);
	
	recordDir(logFile, formulatedQuery);
	
	copyFile(pathConfig.trainDataFile, trainDir + dateAsString + ".txt");
	copyFile(pathConfig.testDataDir, testDir + dateAsString + ".txt");
	
	delFile(pathConfig.SSLFeatureMatrixFile);
    }
    
    void delFile(String file){
	File delFile = new File(file);
	if(delFile.exists()){
	    delFile.delete();
	}
    }
    
    void recordDir(String writeFile, String formulatedQuery) {
	RW write = new RW(writeFile, 'a');
	write.wLine(dateAsString + "\t" + formulatedQuery);
	write.close();
    }

    void makeDir(String path) {
	File queryDir = new File(path);
	if (!queryDir.exists()) {
	    queryDir.mkdir();
	}
    }

    void copyFile(String readFile, String writeFile) {
	RW read = new RW(readFile, 'r');
	RW write = new RW(writeFile, 'w');
	BufferedWriter out = write.out;
	try {
	    BufferedReader br = read.br;
	    String strLine;
	    while ((strLine = br.readLine()) != null) {

		out.write(strLine + "\n");
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	}
	read.close();
	write.close();
    }
}
