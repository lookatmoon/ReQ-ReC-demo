package process;

import feature.FeatureConfig;
import feature.Tokenize;
import fileFetch.RW;
import global.GlobalVar;
import svm.LIBlinearTestLabelUnknown;

import java.io.*;
import java.util.*;

public class PipelinePredictActiveQuery {

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
    double throwAwayDataConfidence = -20;
    double pickAsConfidentTopPct = 0.4; //smaller would be more confident

    PipelinePredictActiveQuery(PathConfig pathConfig, int queryNumReturn, int confidentNumReturn, String methodName, FeatureConfig config, BufferedWriter out, int posHalfWindowSize, int ngramPOS,
	    int ngramDependency) throws IOException {

	out.write(methodName + "\n");

	int idIndex = 1;
	int textIndex = 2;
	int targetIndex = 0;
	int labelIndex = 0;
	int nGram = 2;//n-gram

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
	TrainingDataToFeature trainingDataToFeature = new TrainingDataToFeature(trainDataFile,
		trainFeatureFile, trainFeatureTokenFile, featureToIndexDir, tokenize,
		nGram, config, labelIndex, textIndex, idIndex, targetIndex, posHalfWindowSize, ngramPOS, ngramDependency);
	new TestDataToFeature(testDataDir, testFeatureFile, testFeatureTokenFile,
		featureToIndexDir, tokenize, nGram, config, labelIndex, textIndex,
		idIndex, targetIndex, posHalfWindowSize, ngramPOS, ngramDependency);

	token_to_wordSet = tokenize.token_to_wordSet;

	writeTrainFeature(trainFeatureFile, trainingDataFile);

	new LIBlinearTestLabelUnknown(trainingDataFile, testDataFile, modelFile, resultOutputPath);

	int featureNum = trainingDataToFeature.vocSize + trainingDataToFeature.nonWordFeature_to_index.size();
	double[] featureIndex_to_weight = getPredictedFeatureWeights(featureNum, modelFile);


	initQueue(queryNumReturn, confidentNumReturn);
	getConfidenceOfTest(featureIndex_to_weight, testFeatureTokenFile, queryNumReturn, confidentNumReturn);

	outputQueries();
	confidentTweets = "";
	HashMap<Long, Double> neededPosTweetIDs = sortByComparator(tweetID_to_posConfidence, confidentNumReturn);
	HashMap<Long, Double> neededNegTweetIDs = sortByComparator(tweetID_to_negConfidence, confidentNumReturn);
	outputConfidentTweets(neededPosTweetIDs, neededNegTweetIDs, testFeatureTokenFile);
//		RW.pLine(queryTweets);
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
		    if (confidence != null) {
			confidentTweets += "1\t" + id + "\t" + confidence + " " + elements[3] + "\n";
		    } else {
			confidence = neededNegTweetIDs.get(id);
			if (confidence != null) {
			    confidentNeg += "-1\t" + id + "\t-" + confidence + " " + elements[3] + "\n";
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

    void outputQueries() {
	queryTweets = "";

	while (queryQueue.size() != 0) {
	    TweetMeta tweetMeta = queryQueue.remove();
	    if (local) {
//		RW.pLine(tweetMeta.label + "\t" + tweetMeta.text + "\t" + tweetMeta.confidence);
	    }


	    if (tweetMeta.label > 0) {
		queryTweets += tweetMeta.label + "\t" + tweetMeta.id + "\t" + tweetMeta.confidence + " " + tweetMeta.text + "\n";
	    } else {
		queryTweets += tweetMeta.label + "\t" + tweetMeta.id + "\t-" + tweetMeta.confidence + " " + tweetMeta.text + "\n";
	    }

	}

	predictStats = "# tweets predicted as positive: " + posNum + "; # tweets predicted as negative: " + negNum + "; # tweets unseen in training data (predicted as negative by classifier): " + unseenNum;
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

    double[] getPredictedFeatureWeights(int featureNum, String modelFile) {
	//index start from 1
	double[] featureIndex_to_weight = new double[featureNum + 1];

	try {

	    FileInputStream fstream = new FileInputStream(modelFile);

	    DataInputStream in = new DataInputStream(fstream);
	    BufferedReader br = new BufferedReader(new InputStreamReader(in));
	    String strLine;

	    int i = 0;

	    while ((strLine = br.readLine()) != null) {

		if (strLine.startsWith("bias")) {
		    String[] elements = strLine.split(" ", 2);
		    featureIndex_to_weight[i] = Double.parseDouble(elements[1]);
		    i++;
		}
		if (strLine.equals("w")) {
		    break;
		}
	    }


	    while ((strLine = br.readLine()) != null) {
		double weight = Double.parseDouble(strLine);
		featureIndex_to_weight[i] = weight;
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
