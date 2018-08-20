package process;

import feature.FeatureConfig;
import feature.Tokenize;
import fileFetch.RW;
import global.GlobalVar;

import java.io.*;
import java.util.*;
import java.util.regex.*;
import svm.LIBlinearTestLabelUnknown;
import svm.LIBlinearWeighted;

public class PredictCompleteData {
    
    double sumAccuracy = 0;
    HashSet<String> trainIDs = new HashSet<String>();
    HashSet<String> allunlabeledIDs = new HashSet<String>();
    HashMap<Double, String> confidence_to_testData = new HashMap<Double, String>();
    PriorityQueue<TweetMeta> allTweetsQueue;
    String allTweets;
    String queryTweets;
    String predictStats = "";
    HashSet<String> topRetriTweetIDs = new HashSet<String>();
    String confidentTweets;
    int posNum = 0;
    int negNum = 0;
    int unseenNum = 0;
    boolean local = GlobalVar.local;
    HashMap<String, HashSet<String>> token_to_wordSet;
    HashMap<Long, Integer> confidentAndManualTweetID_to_label = new HashMap<Long, Integer>();
    double throwAwayDataConfidence = 0;
    int labeldConfidence = 10;
    double confidentTheta = 0.9;
    double pickAsConfidentTopPct = 0.4; //smaller would be more confident
    int manualTweetWeight = 3;
    int SSLTweetWeight = 1;
    HashMap<String, URLMeta> url_to_URLMeta = new HashMap<String, URLMeta>();
    Pattern urlPattern = Pattern.compile(GlobalVar.urlPattern);
    List<Long> timeStampList = new ArrayList<Long>();
    double queryTimeDbl;
    String queryTimeStr;
    
    PredictCompleteData(PathConfig pathConfig, String methodName, int queryNumReturn, int confidentSampleNumReturn, FeatureConfig config, int posHalfWindowSize, int ngramPOS,
	    int ngramDependency, boolean needAllTweets, HashMap<Long, Integer> idInEvaFile_to_trueLabel, 
	    String tweetQueryTime, boolean needShowTopRetriTweets) throws IOException {
	
	int idIndex = 1;
	int textIndex = 2;
	int targetIndex = 0;
	int labelIndex = 0;
	int nGram = 3;//n-gram

	String trainDataFile = pathConfig.trainDataFile;
	String trainFeatureFile = pathConfig.trainFeatureFile;
	String trainFeatureTokenFile = pathConfig.trainFeatureTokenFile;
	String testDataDir = pathConfig.completeRetrievedData + ".noTrain";
	String testFeatureFile = pathConfig.testFeatureFile + ".com";
	String testFeatureTokenFile = pathConfig.testFeatureTokenFile + ".com";
	String featureToIndexDir = pathConfig.featureToIndexDir;
	//path for svm
	String modelFile = pathConfig.modelFile + ".com";
	String trainingDataFile = pathConfig.trainingDataFile + ".com";
	String testDataFile = pathConfig.testDataFile + ".com";
	String resultOutputPath = pathConfig.resultOutputPath + ".com";
	
	getLabeledDataIDs(trainDataFile);
	int testDataNum = generateTestData(pathConfig.completeRetrievedData, testDataDir);
	testDataNum += appendTestData(pathConfig.testDataDir, testDataDir, queryNumReturn, needShowTopRetriTweets);
	
	Tokenize tokenize = new Tokenize(config);

//	if (!local) {
//	    File dir = new File(pathConfig.SSLPath);
//	    if (!dir.exists()) {
//		dir.mkdir();
//	    }
//	    File featureMatrixFile = new File(pathConfig.SSLFeatureMatrixFile);
//	    if (!featureMatrixFile.exists()) {
//		generateGraph(pathConfig);
//
//		TrainingDataToFeature trainingDataToFeature = new TrainingDataToFeature(trainDataFile,
//			trainFeatureFile, trainFeatureTokenFile, featureToIndexDir, tokenize,
//			nGram, config, labelIndex, textIndex, idIndex, targetIndex, posHalfWindowSize, ngramPOS, ngramDependency);
//		int featureNum = trainingDataToFeature.vocSize + trainingDataToFeature.nonWordFeature_to_index.size();
//		int dataNum = trainingDataToFeature.trainDataNum;
//		callGraphGeneration(pathConfig.trainFeatureFile, dataNum, featureNum, pathConfig.SSLFeatureMatrixOutPutFile);
//
//	    }
//
//	    generateLabeledFile(pathConfig.trainDataFile, pathConfig.SSLLabeledFile);
//	    callLabelpropagate(pathConfig);
//
//	    readPredictFile(pathConfig);
//	    writeConfidentAndManualTweetsToTrain(pathConfig);
//	}

//	getWeightsOfTrainExamples(pathConfig);

	getUrl_to_URLMeta(pathConfig);
	readTimeFile(pathConfig.timeFile);
	queryTimeDbl = computeTime(Long.parseLong(tweetQueryTime), 0, timeStampList.size() - 1);
	queryTimeStr = String.format("%.2f", queryTimeDbl);
	
	TrainingDataToFeature trainingDataToFeature = new TrainingDataToFeature(trainDataFile,
		trainFeatureFile, trainFeatureTokenFile, featureToIndexDir, tokenize,
		nGram, config, labelIndex, textIndex, idIndex, targetIndex, posHalfWindowSize, ngramPOS, ngramDependency);
	new TestDataToFeature(testDataDir, testFeatureFile, testFeatureTokenFile,
		featureToIndexDir, tokenize, nGram, config, labelIndex, textIndex,
		idIndex, targetIndex, posHalfWindowSize, ngramPOS, ngramDependency);
	
	token_to_wordSet = tokenize.token_to_wordSet;
	
	writeTrainFeature(trainFeatureFile, trainingDataFile);
	
	new LIBlinearTestLabelUnknown(trainingDataFile, testDataFile, modelFile, resultOutputPath);
//	new LIBlinearWeighted(pathConfig);

	int featureNum = trainingDataToFeature.vocSize + trainingDataToFeature.nonWordFeature_to_index.size();
	double[] featureIndex_to_weight = getPredictedFeatureWeights(featureNum, modelFile);
	
	initQueue(testDataNum);
	getConfidenceOfTest(featureIndex_to_weight, testFeatureTokenFile);
	
	outputAllTweets(queryNumReturn, confidentSampleNumReturn, needAllTweets, idInEvaFile_to_trueLabel, needShowTopRetriTweets);
    }
    
    int appendTestData(String readFile, String writeFile, int queryNumReturn, boolean needShowTopRetriTweets) {
	int testDataNum = 0;
	RW read = new RW(readFile, 'r');
	RW write = new RW(writeFile, 'a');
	BufferedWriter out = write.out;
	try {
	    BufferedReader br = read.br;
	    String strLine;
	    while ((strLine = br.readLine()) != null) {
		
		String[] elements = strLine.split("\t");
		if (elements.length > 1) {
		    if (!allunlabeledIDs.contains(elements[1])) {
			out.write(strLine + "\n");
			
			if(needShowTopRetriTweets && testDataNum < queryNumReturn){
			    topRetriTweetIDs.add(elements[1]);
			}
			
			testDataNum++;
		    }
		}
		
	    }
	    
	} catch (Exception e) {
	    e.printStackTrace();
	}
	read.close();
	write.close();
	return testDataNum;
    }
    
    double computeTime(long id, int startIndex, int endIndex) {
	long startID = timeStampList.get(startIndex);
	long endID = timeStampList.get(endIndex);
	long firstID = timeStampList.get(0);
	long lastID = timeStampList.get(timeStampList.size() - 1);
	
	if (id < firstID) {
	    return -1;
	} else if (id > lastID) {
	    return timeStampList.size();
	}
	
	if (id == startID) {
	    return startIndex;
	}
	
	if (id == endID) {
	    return endIndex;
	}
	
	if (endIndex - 1 == startIndex) {
	    double time = startIndex + (double) (id - startID) / (endID - startID);
//	    int timeInt = (int) (time * 100);
//	    time = (double) timeInt / 100;
	    return time;
	}
	
	int midIndex = (startIndex + endIndex) / 2;
	long midID = timeStampList.get(midIndex);
	if (id == midID) {
	    return midIndex;
	} else if (id < midID) {
	    return computeTime(id, startIndex, midIndex);
	} else { //id>midID
	    return computeTime(id, midIndex, endIndex);
	}
    }
    
    void readTimeFile(String file) {
	RW read = new RW(file, 'r');
	try {
	    BufferedReader br = read.br;
	    String strLine;
	    while ((strLine = br.readLine()) != null) {
		
		String[] elements = strLine.split("\t");
		if (elements.length > 1) {
		    long id = Long.parseLong(elements[0]);
		    timeStampList.add(id);
		}
	    }
	    
	} catch (Exception e) {
	    e.printStackTrace();
	}
	read.close();
    }
    
    int generateTestData(String readFile, String writeFile) {
	int testDataNum = 0;
	RW read = new RW(readFile, 'r');
	RW write = new RW(writeFile, 'w');
	BufferedWriter out = write.out;
	try {
	    BufferedReader br = read.br;
	    String strLine;
	    while ((strLine = br.readLine()) != null) {
		
		String[] elements = strLine.split("\t");
		if (elements.length > 1) {
		    if (!trainIDs.contains(elements[1])) {
			allunlabeledIDs.add(elements[1]);
			out.write(strLine + "\n");
			testDataNum++;
		    }
		}
	    }
	    
	} catch (Exception e) {
	    e.printStackTrace();
	}
	read.close();
	write.close();
	return testDataNum;
    }
    
    void getLabeledDataIDs(String file) {
	RW read = new RW(file, 'r');
	try {
	    BufferedReader br = read.br;
	    String strLine;
	    while ((strLine = br.readLine()) != null) {
		
		String[] elements = strLine.split("\t");
		if (elements.length > 1) {
		    trainIDs.add(elements[1]);
		}
	    }
	    
	} catch (Exception e) {
	    e.printStackTrace();
	}
	read.close();
    }
    
    String addURLInfo(String text) {
	String url = extractURL(text);
	if (url != null) {
	    URLMeta uRLMeta = url_to_URLMeta.get(url);
	    if (uRLMeta != null) {
		text += " (URL: " + uRLMeta.inPosDataNum + " in pos labeled, " + uRLMeta.inNegDataNum + " in neg labeled)";
	    } else {
		text += " (URL: 0 in pos labeled, 0 in neg labeled)";
	    }
	}
	return text;
    }
    
    class URLMeta {
	
	int inPosDataNum = 0; //number of times appear in manually labeled positive data
	int inNegDataNum = 0;
    }
    
    String extractURL(String text) {
	Matcher m = urlPattern.matcher(text);
	if (m.find()) {
	    return m.group();
	}
	
	return null;
    }
    
    void getUrl_to_URLMeta(PathConfig pathConfig) {
	RW read = new RW(pathConfig.evaluationFile, 'r');
	try {
	    BufferedReader br = read.br;
	    String strLine;
	    while ((strLine = br.readLine()) != null) {
		String[] elements = strLine.split("\t");
		if (elements.length > 2) {
		    String text = elements[2];
		    String label = elements[0];
		    String url = extractURL(text);
		    if (url != null) {
			
			if (local) {
//			    RW.pLine(url);
			}
			
			URLMeta uRLMeta = url_to_URLMeta.get(url);
			if (uRLMeta == null) {
			    uRLMeta = new URLMeta();
			    url_to_URLMeta.put(url, uRLMeta);
			}
			
			if ("1".equals(label)) {
			    uRLMeta.inPosDataNum++;
			} else {
			    uRLMeta.inNegDataNum++;
			}
		    }
		}
	    }
	    
	} catch (Exception e) {
	    e.printStackTrace();
	}
	read.close();
    }
    
    double[] getPredictedFeatureWeights(int featureNum, String modelFile) {
	//index start from 1
	double[] featureIndex_to_weight = new double[featureNum + 1];
	
	try {
	    
	    FileInputStream fstream = new FileInputStream(modelFile);
	    
	    DataInputStream in = new DataInputStream(fstream);
	    BufferedReader br = new BufferedReader(new InputStreamReader(in));
	    String strLine;
	    
	    int i = 0;
	    boolean reverseWeightSign = false;
	    
	    while ((strLine = br.readLine()) != null) {
		
		if (strLine.startsWith("bias")) {
		    String[] elements = strLine.split(" ", 2);
		    featureIndex_to_weight[i] = Double.parseDouble(elements[1]);
		    i++;
		}
		
		if (strLine.startsWith("label")) {
		    if (strLine.equals("label -1 1")) {
			reverseWeightSign = true;
		    }
		}
		
		
		if (strLine.equals("w")) {
		    break;
		}
	    }
	    
	    
	    while ((strLine = br.readLine()) != null) {
		double weight = Double.parseDouble(strLine);
		if (reverseWeightSign) {
		    featureIndex_to_weight[i] = -weight;
		} else {
		    featureIndex_to_weight[i] = weight;
		}
		
		i++;
	    }
	    br.close();
	    in.close();
	    fstream.close();
	} catch (Exception e) {
	    e.printStackTrace();
	}
	
	return featureIndex_to_weight;
    }
    
    void getWeightsOfTrainExamples(PathConfig pathConfig) {
	HashSet<String> manualIDs = new HashSet<String>();
	RW read = new RW(pathConfig.evaluationFile, 'r');
	try {
	    BufferedReader br = read.br;
	    String strLine;
	    while ((strLine = br.readLine()) != null) {
		String[] elements = strLine.split("\t");
		if (elements.length > 1) {
		    String id = elements[1];
		    manualIDs.add(id);
		}
	    }
	    
	} catch (Exception e) {
	    e.printStackTrace();
	}
	read.close();
	
	read = new RW(pathConfig.trainDataFile, 'r');
	RW write = new RW(pathConfig.svmWeightFile, 'w');
	BufferedWriter out = write.out;
	try {
	    BufferedReader br = read.br;
	    String strLine;
	    while ((strLine = br.readLine()) != null) {
		
		String[] elements = strLine.split("\t");
		if (elements.length > 1) {
		    String id = elements[1];
		    if (manualIDs.contains(id)) {
			out.write(manualTweetWeight + "\n");
		    } else {
			out.write(SSLTweetWeight + "\n");
		    }
		}
	    }
	    
	} catch (Exception e) {
	    e.printStackTrace();
	}
	read.close();
	write.close();
    }
    
    void writeTrainFeature(String inputFeatureFile, String outputFeatureFile) {
	
	RW read = new RW(inputFeatureFile, 'r');
	RW write = new RW(outputFeatureFile, 'w');
	BufferedWriter out = write.out;
	try {
	    BufferedReader br = read.br;
	    String strLine;
	    while ((strLine = br.readLine()) != null) {
		
		String[] elements = strLine.split("\t");
		if (elements.length > 1) {
		    out.write(elements[1] + "\n");
		}
	    }
	    
	} catch (Exception e) {
	    e.printStackTrace();
	}
	read.close();
	write.close();
    }
    
    void writeConfidentAndManualTweetsToTrain(PathConfig pathConfig) {
	
	String trainDataFile = pathConfig.trainDataFile;
	String testDataDir = pathConfig.testDataDir;
	String originalFile = pathConfig.originalFile;
	
	RW read = new RW(originalFile, 'r');
	RW writeTrain = new RW(trainDataFile, 'w');
	BufferedWriter outTrain = writeTrain.out;
	
	RW writeTest = new RW(testDataDir, 'w');
	BufferedWriter outTest = writeTest.out;
	try {
	    BufferedReader br = read.br;
	    String strLine;
	    while ((strLine = br.readLine()) != null) {
		//label: 0: unlabeled; 1: pos; -1: neg
		String[] elements = strLine.split("\t");
		if (elements.length > 1) {
		    long id = Long.parseLong(elements[1]);
		    Integer label = confidentAndManualTweetID_to_label.get(id);
		    if (label == null) {
			outTest.write(strLine + "\n");
		    } else {
			outTrain.write(label + "\t" + elements[1] + "\t" + elements[2] + "\n");
		    }
		}
	    }
	    
	} catch (Exception e) {
	    e.printStackTrace();
	}
	read.close();
	writeTrain.close();
	writeTest.close();
    }
    
    void readPredictFile(PathConfig pathConfig) {
	RW read = new RW(pathConfig.SSLOutputPredictionFile, 'r');
	try {
	    BufferedReader br = read.br;
	    String strLine;
	    while ((strLine = br.readLine()) != null) {
		
		String[] elements = strLine.split("\t");
		if (elements.length > 1) {
		    long id = Long.parseLong(elements[0]);
		    int label = Integer.parseInt(elements[1]);
		    double confidence = Double.parseDouble(elements[2]);
		    if (confidence > confidentTheta || confidence < 1 - confidentTheta) {
			confidentAndManualTweetID_to_label.put(id, label);
		    }
		}
	    }
	    
	} catch (Exception e) {
	    e.printStackTrace();
	}
	read.close();
    }
    
    void callLabelpropagate(PathConfig pathConfig) {
	String cmdPath = GlobalVar.propagate;
        Runtime rt = Runtime.getRuntime();
	String[] cmd = {cmdPath, pathConfig.SSLFeatureMatrixOutPutFile, "2", pathConfig.SSLLabeledFile, pathConfig.SSLOutputPredictionFile};
	Process p;
	try {
	    p = rt.exec(cmd);
	    InputStream in = p.getInputStream();
	    
	    InputStreamReader isr = new InputStreamReader(in);
	    BufferedReader br = new BufferedReader(isr);
	    String line;
	    
	    while ((line = br.readLine()) != null) {
		System.out.println(line);
	    }
	    br.close();
	    isr.close();
	    in.close();
	    p.destroy();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }
    
    void generateLabeledFile(String trainFile, String outputLabeledFile) {
	RW read = new RW(trainFile, 'r');
	RW write = new RW(outputLabeledFile, 'w');
	BufferedWriter out = write.out;
	try {
	    BufferedReader br = read.br;
	    String strLine;
	    while ((strLine = br.readLine()) != null) {
		
		String[] elements = strLine.split("\t");
		if (elements.length > 1) {
		    long id = Long.parseLong(elements[1]);
		    int label = Integer.parseInt(elements[0]);
		    confidentAndManualTweetID_to_label.put(id, label);
		    out.write(elements[1] + "\t" + elements[0] + "\n");
		}
	    }
	    
	} catch (Exception e) {
	    e.printStackTrace();
	}
	read.close();
	write.close();
    }
    
    void callGraphGeneration(String featureFile, int dataNum, int featureNum, String outputPath) {
	String cmdPath = GlobalVar.gen_graph;
        Runtime rt = Runtime.getRuntime();
	String[] cmd = {cmdPath, dataNum + "", featureNum + "", outputPath};
	Process p;
	try {
	    p = rt.exec(cmd);
	    InputStream in = p.getInputStream();
	    
	    InputStreamReader isr = new InputStreamReader(in);
	    BufferedReader br = new BufferedReader(isr);
	    String line;
	    
	    while ((line = br.readLine()) != null) {
		System.out.println(line);
	    }
	    br.close();
	    isr.close();
	    in.close();
	    p.destroy();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }
    
    void generateGraph(PathConfig pathConfig) {
	RW read = new RW(pathConfig.trainDataFile, 'r');
	RW write = new RW(pathConfig.SSLDataAllFile, 'w');
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
	
	read = new RW(pathConfig.testDataDir, 'r');
	try {
	    BufferedReader br = read.br;
	    String strLine;
	    while ((strLine = br.readLine()) != null) {
		out.write(strLine + "\n");
	    }
	    
	} catch (Exception e) {
	    e.printStackTrace();
	}
	
	write.close();
    }
    
    void initQueue(int initialCapacity) {
	Comparator<TweetMeta> comparator = new ConfidencePriorityQueue();
	//initial capacity
	allTweetsQueue = new PriorityQueue<TweetMeta>(initialCapacity, comparator);
    }
    
    class ConfidencePriorityQueue implements Comparator<TweetMeta> {
	
	@Override
	public int compare(TweetMeta x, TweetMeta y) {
	    // Assume neither string is null. Real code should
	    // probably be more robust
	    if (x.confidence < y.confidence) { //larger would be at head
		return 1;
	    }
	    if (x.confidence > y.confidence) {
		return -1;
	    }
	    return 0;
	}
    }
    
    void outputAllTweets(int queryNumReturn, int confidentSampleNumReturn, boolean needAllTweets, 
	    HashMap<Long, Integer> idInEvaFile_to_trueLabel, boolean needShowTopRetriTweets) {
	allTweets = "";
	confidentTweets = "";
	queryTweets = "";
	String confidentNegTweets = "";
	int currentPosNum = 0;
	int currentNegNum = 0;
	String topRetriTweets = "";
	
	double neededTopPosNum = (double) posNum * pickAsConfidentTopPct;
	double samplePosPct = (double) confidentSampleNumReturn / neededTopPosNum;
	
	double neededTopNegNum = (double) negNum * pickAsConfidentTopPct;
	double sampleNegPct = (double) confidentSampleNumReturn / neededTopNegNum;
	
	int fifoQueueSize = queryNumReturn / 2;
	Queue<String> queueForPosQuery = new LinkedList<String>();
	Queue<String> queueForNegQuery = new LinkedList<String>();
	
	while (allTweetsQueue.size() != 0) {
	    TweetMeta tweetMeta = allTweetsQueue.remove();
	    if (local) {
//		RW.pLine(tweetMeta.label + "\t" + tweetMeta.text + "\t" + tweetMeta.confidence);
	    }
	    
	    Integer label = tweetMeta.label;
	    
	    String text = addURLInfo(tweetMeta.text);
	    long id = Long.parseLong(tweetMeta.id);
	    Integer truelabel = idInEvaFile_to_trueLabel.get(id);
	    double timeDiffDbl = queryTimeDbl - computeTime(id, 0, timeStampList.size()-1);
	    String timeDiff = queryTimeStr + ", -" + String.format("%.2f", timeDiffDbl);
	    String notation = "";
	    if (truelabel != null) {
		notation = " <SPAN style='BACKGROUND-COLOR: #E5CCFF'>Manual label: " + truelabel + "</SPAN>";
	    }
	    
	    if (tweetMeta.label > 0) {
		
		if (currentPosNum < neededTopPosNum && Math.random() <= samplePosPct) {
		    confidentTweets += label + "\t" + tweetMeta.id + "\t" + tweetMeta.confidence + " (" + timeDiff + ") " + text + "\n";
		}
		
		currentPosNum++;
		if (needAllTweets) {
		    allTweets += label + "\t" + tweetMeta.id + "\t" + tweetMeta.confidence + "\t" + " (" + timeDiff + ") " + text + "\n";
		}

//		if (allTweetsQueue.size() >= unseenNum && allTweetsQueue.size() < unseenNum + queryNumReturn) {
//		    queryTweets += label + "\t" + tweetMeta.id + "\t" + tweetMeta.confidence + " " + text + "\n";
//		}

		if(needShowTopRetriTweets && topRetriTweetIDs.contains(tweetMeta.id)){
		    topRetriTweets += label + "\t" + tweetMeta.id + "\t" + tweetMeta.confidence + " (" + timeDiff + ") " + text + notation + "\n";
		}else if (allTweetsQueue.size() >= unseenNum) {
		    updateQueue(queueForPosQuery, fifoQueueSize, label + "\t" + tweetMeta.id + "\t" + tweetMeta.confidence + " (" + timeDiff + ") " + text + notation + "\n");
		}
		
	    } else {
		if (currentNegNum < neededTopNegNum && Math.random() <= sampleNegPct) {
		    
		    confidentNegTweets += label + "\t" + tweetMeta.id + "\t-" + tweetMeta.confidence + " (" + timeDiff + ") " + text + "\n";
		}
		
		currentNegNum++;
		
		if (needAllTweets) {
		    allTweets += label + "\t" + tweetMeta.id + "\t-" + tweetMeta.confidence + "\t" + " (" + timeDiff + ") " + text + "\n";
		}

//		if (allTweetsQueue.size() >= unseenNum && allTweetsQueue.size() < unseenNum + queryNumReturn) {
//		    queryTweets += label + "\t" + tweetMeta.id + "\t-" + tweetMeta.confidence + " " + text + "\n";
//		}

		if(needShowTopRetriTweets && topRetriTweetIDs.contains(tweetMeta.id)){
		    topRetriTweets += label + "\t" + tweetMeta.id + "\t-" + tweetMeta.confidence + " (" + timeDiff + ") " + text + notation + "\n";
		}else if (allTweetsQueue.size() >= unseenNum) {
		    updateQueue(queueForNegQuery, fifoQueueSize, label + "\t" + tweetMeta.id + "\t-" + tweetMeta.confidence + " (" + timeDiff + ") " + text + notation + "\n");
		}
	    }
	    
	}
	
	while (queueForPosQuery.size() != 0) {
	    queryTweets += queueForPosQuery.remove();
	}
	while (queueForNegQuery.size() != 0) {
	    queryTweets += queueForNegQuery.remove();
	}
	
	if(needShowTopRetriTweets){
	    queryTweets = topRetriTweets + queryTweets;
	}
	
	confidentTweets += confidentNegTweets;
	negNum = negNum + unseenNum;
	predictStats = "# accumulated tweets predicted as positive: " + posNum + ", # negative: " + negNum;
    }
    
    void updateQueue(Queue<String> fifoQueue, int queueSize, String addElement) {
	if (fifoQueue.size() < queueSize) {
	    fifoQueue.add(addElement);
	} else {
	    fifoQueue.poll();
	    fifoQueue.add(addElement);
	}
    }
    
    double computeConfidence(String featureLine, double[] featureIndex_to_weight) {
	featureLine = featureLine.trim();
	//Note that LIBLINEAR does not use the bias term b by default. 
	//If you observe very different results, try to set -B 1 for LIBLINEAR
	double confidence = 0; //bias

	//throw unseen data away
	if (featureLine.length() == 0) {
//			RW.pLine(featureLine);
	    return throwAwayDataConfidence;
	}
	
	String[] features = featureLine.split(" ");
	
	if (features == null || features.length == 0) {
	    return throwAwayDataConfidence;
	}
	
	for (String feature : features) {
	    String[] eles = feature.split(":");
	    int len = eles.length;
	    int featureIndex = Integer.parseInt(eles[0]);
	    double featureVal = Double.parseDouble(eles[len - 1]);
	    double weight = featureIndex_to_weight[featureIndex];
	    confidence += weight * featureVal;
	}
	
	return confidence;
    }
    
    void getConfidenceOfTest(double[] featureIndex_to_weight, String testFeatureTokenFile) {
	RW read = new RW(testFeatureTokenFile, 'r', "UTF8");
	try {
	    BufferedReader br = read.br;
	    String strLine;
	    while ((strLine = br.readLine()) != null) {
		String[] elements = strLine.split("\t");
		if (elements.length > 1) {
		    String featureLine = elements[2];
		    long id = Long.parseLong(elements[1]);
		    double confidence = computeConfidence(featureLine, featureIndex_to_weight);
		    
		    int label;
		    if (throwAwayDataConfidence == confidence) {
			unseenNum++;
			label = -1;
		    } else if (confidence > 0) {
			posNum++;
			label = 1;
		    } else {
			negNum++;
			label = -1;
		    }
		    
		    confidence = Math.abs(confidence);
		    
		    TweetMeta newPoint = new TweetMeta(confidence, elements[1], elements[3], label);
		    allTweetsQueue.add(newPoint);
		    
		}
	    }
	    
	} catch (Exception e) {
	    e.printStackTrace();
	}
	read.close();
    }
    
    void updateConfidentQueue(PriorityQueue<TweetMeta> confidentQueue, int queueSize, double confidence, String id, String text, int label) {
	
	if (confidentQueue.size() < queueSize) {
	    TweetMeta newPoint = new TweetMeta(confidence, id, text, label);
	    confidentQueue.add(newPoint);
	} else {
	    TweetMeta min = confidentQueue.peek();
	    if (confidence > min.confidence) {
		TweetMeta newPoint = new TweetMeta(confidence, id, text, label);
		confidentQueue.remove();
		confidentQueue.add(newPoint);
	    }
	}
    }
    
    class TweetMeta {
	
	int label;
	double confidence;
	String id;
	String text;
	
	TweetMeta(double confidence, String id, String text, int label) {
	    this.confidence = confidence;
	    this.id = id;
	    this.text = text;
	    this.label = label;
	}
    }
    
    public static void main(String[] args) throws IOException {
    }
}
