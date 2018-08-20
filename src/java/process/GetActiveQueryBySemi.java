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

public class GetActiveQueryBySemi {

    double sumAccuracy = 0;
    HashMap<Double, String> confidence_to_testData = new HashMap<Double, String>();
    PriorityQueue<TweetMeta> queryQueue;
//    PriorityQueue<TweetMeta> posConfidentQueue;
//    PriorityQueue<TweetMeta> negConfidentQueue;
    String queryTweets;
    String confidentTweets;
    String predictStats = "";
    int posNum = 0;
    int negNum = 0;
    int unseenNum = 0;
    boolean local = GlobalVar.local;
    HashMap<String, HashSet<String>> token_to_wordSet;
    HashMap<Long, Double> tweetID_to_posConfidence = new HashMap<Long, Double>();
    HashMap<Long, Double> tweetID_to_negConfidence = new HashMap<Long, Double>();
    HashMap<Long, Integer> confidentAndManualTweetID_to_label = new HashMap<Long, Integer>();
    double throwAwayDataConfidence = -20;
    double confidentTheta = 0.9;
    double pickAsConfidentTopPct = 0.4; //smaller would be more confident
    int manualTweetWeight = 3;
    int SSLTweetWeight = 1;
    HashMap<String, URLMeta> url_to_URLMeta = new HashMap<String, URLMeta>();
    Pattern urlPattern = Pattern.compile(GlobalVar.urlPattern);

    GetActiveQueryBySemi(PathConfig pathConfig, int queryNumReturn, int confidentSampleNumReturn, String methodName, FeatureConfig config, BufferedWriter out, int posHalfWindowSize, int ngramPOS,
	    int ngramDependency, HashMap<Long, Integer> idInEvaFile_to_trueLabel) throws IOException {

	out.write(methodName + "\n");

	int idIndex = 1;
	int textIndex = 2;
	int targetIndex = 0;
	int labelIndex = 0;
	int nGram = 3;//n-gram

	String trainDataFile = pathConfig.trainDataFile;
	String trainFeatureFile = pathConfig.trainFeatureFile;
	String trainFeatureTokenFile = pathConfig.trainFeatureTokenFile;
	String testDataDir = pathConfig.testDataDir;
	String testFeatureFile = pathConfig.testFeatureFile;
	String testFeatureTokenFile = pathConfig.testFeatureTokenFile;
	String featureToIndexDir = pathConfig.featureToIndexDir;
	//path for svm
	String modelFile = pathConfig.modelFile;
	String trainingDataFile = pathConfig.trainingDataFile;
	String testDataFile = pathConfig.testDataFile;
	String resultOutputPath = pathConfig.resultOutputPath;
	String resultFile = pathConfig.resultFile;


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


	initQueue(queryNumReturn, confidentSampleNumReturn);
	getConfidenceOfTest(featureIndex_to_weight, testFeatureTokenFile, queryNumReturn, confidentSampleNumReturn);

	outputQueries(idInEvaFile_to_trueLabel);
	confidentTweets = "";
	HashMap<Long, Double> neededPosTweetIDs = sortByComparator(tweetID_to_posConfidence, confidentSampleNumReturn);
	HashMap<Long, Double> neededNegTweetIDs = sortByComparator(tweetID_to_negConfidence, confidentSampleNumReturn);
	outputConfidentTweets(neededPosTweetIDs, neededNegTweetIDs, testFeatureTokenFile);
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
		    if(strLine.equals("label -1 1")){
			reverseWeightSign = true;
		    }
		}
		
		
		if (strLine.equals("w")) {
		    break;
		}
	    }


	    while ((strLine = br.readLine()) != null) {
		double weight = Double.parseDouble(strLine);
		if(reverseWeightSign){
		    featureIndex_to_weight[i] = -weight;
		}else{
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

    @SuppressWarnings({"unchecked", "rawtypes"})
    HashMap<Long, Double> sortByComparator(Map unsortMap, int confidentNumReturn) {

	List list = new LinkedList(unsortMap.entrySet());
	HashMap<Long, Double> neededTweetIDs = new HashMap<Long, Double>();
	// sort list based on comparator, order from large -> small
	Collections.sort(list, new Comparator() {
	    public int compare(Object o1, Object o2) {
		return ((Comparable) ((Map.Entry) (o2)).getValue())
			.compareTo(((Map.Entry) (o1)).getValue());
	    }
	});

	int num = list.size();
	double neededTopNum = (double) num * pickAsConfidentTopPct;
	double samplePct = confidentNumReturn / neededTopNum;

	// tranverse
	int i = 0;
	for (Iterator it = list.iterator(); it.hasNext();) {
	    if (i > neededTopNum) {
		break;
	    }

	    Map.Entry entry = (Map.Entry) it.next();

	    if (Math.random() <= samplePct) {
		Long id = (Long) entry.getKey();
		Double confidence = (Double) entry.getValue();
		neededTweetIDs.put(id, confidence);
	    }

	    i++;
	}

	return neededTweetIDs;
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

    void outputConfidentTweets(HashMap<Long, Double> neededPosTweetIDs, HashMap<Long, Double> neededNegTweetIDs, String testFeatureTokenFile) {
	RW read = new RW(testFeatureTokenFile, 'r', "UTF8");
	try {
	    BufferedReader br = read.br;
	    String strLine;
	    String confidentNeg = "";
	    while ((strLine = br.readLine()) != null) {
		String[] elements = strLine.split("\t");
		if (elements.length > 1) {
		    long id = Long.parseLong(elements[1]);
		    Double confidence = neededPosTweetIDs.get(id);
		    
		    String text = addURLInfo(elements[3]);
		    
		    if (confidence != null) {
			confidentTweets += "1\t" + id + "\t" + confidence + " " + text + "\n";
		    } else {
			confidence = neededNegTweetIDs.get(id);
			if (confidence != null) {
			    confidentNeg += "-1\t" + id + "\t-" + confidence + " " + text + "\n";
			}
		    }

		}
	    }

	    confidentTweets += confidentNeg;
	} catch (Exception e) {
	    e.printStackTrace();
	}
	read.close();
    }

    void initQueue(int initialCapacity, int confidentQueueCapacity) {
	Comparator<TweetMeta> comparator = new QueryPriorityQueue();
	//initial capacity
	queryQueue = new PriorityQueue<TweetMeta>(initialCapacity, comparator);
    }

    class QueryPriorityQueue implements Comparator<TweetMeta> {

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

    void outputQueries(HashMap<Long, Integer> idInEvaFile_to_trueLabel) {
	queryTweets = "";

	while (queryQueue.size() != 0) {
	    TweetMeta tweetMeta = queryQueue.remove();
	    if (local) {
//		RW.pLine(tweetMeta.label + "\t" + tweetMeta.text + "\t" + tweetMeta.confidence);
	    }

	    long id = Long.parseLong(tweetMeta.id);
	    Integer label = idInEvaFile_to_trueLabel.get(id);
	    if (label == null) {
		label = tweetMeta.label;
	    }

	    String text = addURLInfo(tweetMeta.text);

	    if (tweetMeta.label > 0) {
		queryTweets += label + "\t" + tweetMeta.id + "\t" + tweetMeta.confidence + " " + text + "\n";
	    } else {
		queryTweets += label + "\t" + tweetMeta.id + "\t-" + tweetMeta.confidence + " " + text + "\n";
	    }

	}
	
	negNum = negNum + unseenNum;

	predictStats = "# tweets predicted as positive in current outer loop: " + posNum + ", # negative: " + negNum;
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

    void updateConfidneceHashMap(long id, int label, double confidence) {
	if (label > 0) {
	    tweetID_to_posConfidence.put(id, confidence);
	} else {
	    tweetID_to_negConfidence.put(id, confidence);
	}
    }

    void getConfidenceOfTest(double[] featureIndex_to_weight, String testFeatureTokenFile, int queueSize, int confidentNumReturn) {
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

		    if (throwAwayDataConfidence == confidence) {
			unseenNum++;
			continue;
		    }

		    int label;
		    if (confidence > 0) {
			posNum++;
			label = 1;
		    } else {
			negNum++;
			label = -1;
		    }

		    confidence = Math.abs(confidence);
		    updateQueryQueue(queueSize, confidence, elements[1], elements[3], label);
		    updateConfidneceHashMap(id, label, confidence);

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

    void updateQueryQueue(int queueSize, double confidence, String id, String text, int label) {

	if (queryQueue.size() < queueSize) {
	    TweetMeta newPoint = new TweetMeta(confidence, id, text, label);
	    queryQueue.add(newPoint);
	} else {
	    TweetMeta max = queryQueue.peek();
	    if (confidence < max.confidence) {
		TweetMeta newPoint = new TweetMeta(confidence, id, text, label);
		queryQueue.remove();
		queryQueue.add(newPoint);
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
