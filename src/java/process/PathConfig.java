/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package process;

import global.GlobalVar;

public class PathConfig {

    boolean isLocal = GlobalVar.local;
    String remoteOriginalFile;
    String originalFile;
    String trainDataFile;
    String trainInPreLoop;
    String trainFeatureFile;
    String trainFeatureTokenFile;
    String testDataDir;
    String testFeatureFile;
    String testFeatureTokenFile;
    String featureToIndexDir;
    
    //path for svm
    public String modelFile;
    public String trainingDataFile;
    public String testDataFile;
    public String resultOutputPath;
    public String resultFile;
    public String svmWeightFile;
    
    //backup
    String backupPath;
    String parameterPath;
    String completeRetrievedData;
    
    //semi-supervised learning
    String SSLPath;
    String SSLFeatureMatrixFile;
    String SSLFeatureMatrixOutPutFile;
    String SSLDataAllFile;
    String SSLLabeledFile;
    String SSLOutputPredictionFile;
    
    String evaluationFile;
    
    String timeFile;

    PathConfig(String queryName) {
	if(isLocal){
	    //remoteOriginalFile = GlobalVar.codePath + "data/rawdata/" + queryName + "_remote.txt";
	    remoteOriginalFile = GlobalVar.search_result_dir + queryName + "/annotate.txt";

            evaluationFile = GlobalVar.codePath + "data/rawdata/eva/" + queryName + ".txt";
	    backupPath = GlobalVar.codePath + "data/backup/" + queryName + "/";
	    originalFile = GlobalVar.codePath + "data/rawdata/" + queryName + ".txt";
	    trainDataFile = GlobalVar.codePath + "data/train/" + queryName + "_train.txt";
	    trainInPreLoop = GlobalVar.codePath + "data/train/" + queryName + "_train_pre.txt";
	    testDataDir = GlobalVar.codePath + "data/test/" + queryName + "_test.txt";
	    parameterPath = GlobalVar.codePath + "data/parameters/" + queryName + ".txt";
	}else{
            
            //remoteOriginalFile = GlobalVar.codePath + "data/rawdata/" + queryName + "_remote.txt";
	    remoteOriginalFile = GlobalVar.search_result_dir + queryName + "/annotate.txt";

            evaluationFile = GlobalVar.codePath + "data/rawdata/eva/" + queryName + ".txt";
	    backupPath = GlobalVar.codePath + "data/backup/" + queryName + "/";
	    originalFile = GlobalVar.codePath + "data/rawdata/" + queryName + ".txt";
	    trainDataFile = GlobalVar.codePath + "data/train/" + queryName + "_train.txt";
	    trainInPreLoop = GlobalVar.codePath + "data/train/" + queryName + "_train_pre.txt";
	    testDataDir = GlobalVar.codePath + "data/test/" + queryName + "_test.txt";
	    parameterPath = GlobalVar.codePath + "data/parameters/" + queryName + ".txt";

	}
	
	completeRetrievedData = backupPath + "completeRetrievedData.txt";
	
	timeFile = GlobalVar.codePath + "data/query/timestamp.tsv";
	
	
	trainFeatureFile = GlobalVar.codePath + "results/pipeline/" + queryName + "_trainFeature.txt";
	trainFeatureTokenFile = GlobalVar.codePath + "results/pipeline/" + queryName + "_trainFeatureTokens.txt";
	
	testFeatureFile = GlobalVar.codePath + "results/svmFeature/" + queryName + "_1.txt";
	testFeatureTokenFile = GlobalVar.codePath + "results/pipeline/" + queryName + "_testFeatureTokens.txt";
	featureToIndexDir = GlobalVar.codePath + "data/featureToIndex/" + queryName + "_";
	
		
	//path for svm
	modelFile = GlobalVar.codePath + "results/svmResults/" + queryName + "_model.txt";
	trainingDataFile = GlobalVar.codePath + "results/svmFeature/" + queryName + "_0.txt";
	testDataFile = GlobalVar.codePath + "results/svmFeature/" + queryName + "_1.txt";
	resultOutputPath = GlobalVar.codePath + "results/svmResults/" + queryName + "_out.txt";
	resultFile = GlobalVar.codePath + "results/svmResults/" + queryName + "_accumulatedResult.txt";
	svmWeightFile = GlobalVar.codePath + "data/train/" + queryName + "_weight.txt";
	
	SSLPath = GlobalVar.codePath + "data/SSL/" + queryName + "/";
	SSLFeatureMatrixFile = SSLPath + "featureMatrix.txt";
	SSLFeatureMatrixOutPutFile = SSLPath + "featureMatrixOut.txt";
	SSLDataAllFile = SSLPath + "dataAll.txt";
	SSLLabeledFile = SSLPath + "dataLabeled.txt";
	SSLOutputPredictionFile = SSLPath + "outputPrediction.txt";
    }
}
