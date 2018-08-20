/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package process;

import fileFetch.RW;
import global.GlobalVar;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.Map.Entry;


public class GetFeatureWeiComplete {

    int featureNumSelected = 25;
//    int featureNumSelectForQuery = 6;
//    int featureToRaiseFactor = 4;
    int fstFeatureFactor = 2;
    double fstFeatureWeight;
    int orgFeatureFactor = 3;
    String orgQuery;
    PriorityQueue<WeightMeta> weightQueue;
    PriorityQueue<WeightMeta> weightNegQueue;
//    HashSet<Integer> importantPosFeatureIndexes = new HashSet<Integer>();
    HashMap<Integer, TopFeatureMeta> importantPosFeatureIndexe_to_rank = new HashMap<Integer, TopFeatureMeta>();
//    HashSet<Integer> topPosFeatureIndexes = new HashSet<Integer>();
//    HashSet<Integer> selectedPosFeatureIndexes = new HashSet<Integer>();
//    HashSet<Integer> importantNegFeatureIndexes = new HashSet<Integer>();
    HashMap<Integer, TopFeatureMeta> importantNegFeatureIndexe_to_rank = new HashMap<Integer, TopFeatureMeta>();
    String formulatedQuery = "";
    String diplayQuery = "";
    String ignoredWordsInQuery = "";
    String querySummary;
    HashSet<String> stopWordSet = GlobalVar.stopWordSet;
    boolean testing = GlobalVar.local;
    String posWeights = "";
    String negWeights = "";
    String posWordsString = "";
    String negWordsString = "";

    GetFeatureWeiComplete(PathConfig pathConfig, String orgQuery) {
	this.orgQuery = orgQuery;

	initQueue(featureNumSelected);
	getImportantFeatureWeights(pathConfig.modelFile + ".com");

	int featureNum = readFeatureIndexInTraining(pathConfig.featureToIndexDir + "wordFeature_to_index", 0);
	readFeatureIndexInTraining(pathConfig.featureToIndexDir + "nonWordFeature_to_index", featureNum);

	addOrgQuery();

	sortByComparator(importantPosFeatureIndexe_to_rank, true);
	sortByComparator(importantNegFeatureIndexe_to_rank, false);

	querySummary = "Query: " + diplayQuery + "\n\nIgnored words: " + ignoredWordsInQuery;
    }

    GetFeatureWeiComplete(PathConfig pathConfig, HashMap<String, HashSet<String>> token_to_wordSet, String orgQuery) {
	this.orgQuery = orgQuery;

	initQueue(featureNumSelected);
	getImportantFeatureWeights(pathConfig.modelFile + ".com");

	int featureNum = readFeatureIndexInTraining(pathConfig.featureToIndexDir + "wordFeature_to_index", 0);
	readFeatureIndexInTraining(pathConfig.featureToIndexDir + "nonWordFeature_to_index", featureNum);

	addOrgQuery();

	sortByComparator(importantPosFeatureIndexe_to_rank, true);
	sortByComparator(importantNegFeatureIndexe_to_rank, false);

	getWordStringOfTokens(token_to_wordSet);

	querySummary = diplayQuery + "\n\n" + ignoredWordsInQuery;
    }

    void addOrgQuery() {
	String[] unigrams = orgQuery.split(" ");
	for (String unigram : unigrams) {
	    String feature = unigram + "^" + orgFeatureFactor;
	    formulatedQuery += " " + feature;
	    diplayQuery += feature + ", ";
	}

    }

    void getWordStringOfTokens(HashMap<String, HashSet<String>> token_to_wordSet) {
	HashSet<String> words = new HashSet<String>();
	String[] features = posWeights.split(", ");
	words.addAll(Arrays.asList(features));

	for (String word : words) {
	    HashSet<String> wordSet = token_to_wordSet.get(word);
	    if (wordSet != null) {
		for (String wordPos : wordSet) {
		    posWordsString += wordPos + " ";
		}
	    } else if (word.contains("_")) {
		String[] unigrams = word.split("_");
		if (unigrams[1] == null || unigrams[1].length() == 0) {
		    continue;
		}

		HashSet<String> wordSet0 = token_to_wordSet.get(unigrams[0]);
		HashSet<String> wordSet1 = token_to_wordSet.get(unigrams[1]);

		if (wordSet0 != null && wordSet1 != null) {
		    for (String wordPos0 : wordSet0) {
			for (String wordPos1 : wordSet1) {
			    posWordsString += wordPos0 + "_" + wordPos1 + " ";
			}
		    }
		}
	    }
	}


	words.clear();
	features = negWeights.split(", ");
	words.addAll(Arrays.asList(features));

	for (String word : words) {
	    HashSet<String> wordSet = token_to_wordSet.get(word);
	    if (wordSet != null) {
		for (String wordPos : wordSet) {
		    negWordsString += wordPos + " ";
		}
	    } else if (word.contains("_")) {
		String[] unigrams = word.split("_");
		if (unigrams[1] == null || unigrams[1].length() == 0) {
		    continue;
		}

		HashSet<String> wordSet0 = token_to_wordSet.get(unigrams[0]);
		HashSet<String> wordSet1 = token_to_wordSet.get(unigrams[1]);

		if (wordSet0 != null && wordSet1 != null) {
		    for (String wordPos0 : wordSet0) {
			for (String wordPos1 : wordSet1) {
			    negWordsString += wordPos0 + "_" + wordPos1 + " ";
			}
		    }
		}
	    }
	}

    }

    class WeightMeta {

	int index;
	double weight;

	WeightMeta(double weight, int index) {
	    this.weight = weight;
	    this.index = index;
	}
    }

    void initQueue(int queueCapacity) {
	Comparator<WeightMeta> weightComparator = new WeightPriorityQueue();
	weightQueue = new PriorityQueue<WeightMeta>(queueCapacity, weightComparator);

	Comparator<WeightMeta> weightNegComparator = new WeightPriorityNegQueue();
	weightNegQueue = new PriorityQueue<WeightMeta>(queueCapacity, weightNegComparator);
    }

    class WeightPriorityNegQueue implements Comparator<WeightMeta> {

	@Override
	public int compare(WeightMeta x, WeightMeta y) {
	    // Assume neither string is null. Real code should
	    // probably be more robust
	    if (x.weight < y.weight) { //larger would be at head
		return 1;
	    }
	    if (x.weight > y.weight) {
		return -1;
	    }
	    return 0;
	}
    }

    class WeightPriorityQueue implements Comparator<WeightMeta> {

	@Override
	public int compare(WeightMeta x, WeightMeta y) {
	    // Assume neither string is null. Real code should
	    // probably be more robust
	    if (x.weight > y.weight) { //smaller would be at head
		return 1;
	    }
	    if (x.weight < y.weight) {
		return -1;
	    }
	    return 0;
	}
    }

    void updateConfidentQueue(int queueSize, double weight, int index) {

	if (weightQueue.size() < queueSize) {
	    WeightMeta newPoint = new WeightMeta(weight, index);
	    weightQueue.add(newPoint);
	} else {
	    WeightMeta min = weightQueue.peek();
	    if (weight > min.weight) {
		WeightMeta newPoint = new WeightMeta(weight, index);
		weightQueue.remove();
		weightQueue.add(newPoint);
	    }
	}

	if (weightNegQueue.size() < queueSize) {
	    WeightMeta newPoint = new WeightMeta(weight, index);
	    weightNegQueue.add(newPoint);
	} else {
	    WeightMeta max = weightNegQueue.peek();
	    if (weight < max.weight) {
		WeightMeta newPoint = new WeightMeta(weight, index);
		weightNegQueue.remove();
		weightNegQueue.add(newPoint);
	    }
	}
    }

    void getImportantFeatureWeights(String modelFile) {

	try {

	    FileInputStream fstream = new FileInputStream(modelFile);

	    DataInputStream in = new DataInputStream(fstream);
	    BufferedReader br = new BufferedReader(new InputStreamReader(in));
	    String strLine;

	    int i = 1;
	    boolean reverseWeightSign = false;

	    while ((strLine = br.readLine()) != null) {
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
		    weight = -weight;
		}

		updateConfidentQueue(featureNumSelected, weight, i);
		i++;
	    }
	    br.close();
	    in.close();
	    fstream.close();

	    i = 0;
	    int initialSize = weightQueue.size();
	    while (weightQueue.size() != 0) {
		WeightMeta weightMeta = weightQueue.remove();
		if (testing) {
//		    RW.pLine("weight: " + weightMeta.index + "\t" + weightMeta.weight);
		}
		int rank = initialSize - i;
		fstFeatureWeight = weightMeta.weight;
		TopFeatureMeta topFeatureMeta = new TopFeatureMeta(rank, weightMeta.weight);
		importantPosFeatureIndexe_to_rank.put(weightMeta.index, topFeatureMeta);
		i++;
	    }

	    i = 0;
	    initialSize = weightNegQueue.size();

	    while (weightNegQueue.size() != 0) {
		WeightMeta weightMeta = weightNegQueue.remove();
		if (testing) {
//		    RW.pLine("weight: " + weightMeta.index + "\t" + weightMeta.weight);
		}
		int rank = initialSize - i;
		TopFeatureMeta topFeatureMeta = new TopFeatureMeta(rank, weightMeta.weight);
		importantNegFeatureIndexe_to_rank.put(weightMeta.index, topFeatureMeta);
		i++;
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

    class TopFeatureMeta {

	int rank;
	String featureToken;
	double weight;

	TopFeatureMeta(int rank, double weight) {
	    this.rank = rank;
	    this.weight = weight;
	}
    }

    int readFeatureIndexInTraining(String feature_to_indexFile, int offset) {
	int featureNum = 0;
	try {

	    FileInputStream fstream = new FileInputStream(feature_to_indexFile);

	    DataInputStream in = new DataInputStream(fstream);
	    BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF8"));

	    String strLine;

	    while ((strLine = br.readLine()) != null) {

//				System.out.println(strLine);
		String[] elements = strLine.split("=");

		if (elements.length > 1) {
		    String feature = elements[0];
		    for (int i = 1; i < elements.length - 1; i++) {
			feature += "=" + elements[i];
		    }


		    int index = offset + Integer.parseInt(elements[elements.length - 1]);
		    TopFeatureMeta topFeatureMeta = importantPosFeatureIndexe_to_rank.get(index);
		    if (topFeatureMeta != null) {
			topFeatureMeta.featureToken = feature;
		    } else {
			topFeatureMeta = importantNegFeatureIndexe_to_rank.get(index);
			if (topFeatureMeta != null) {
			    topFeatureMeta.featureToken = feature;
			}
		    }
//					System.out.println(feature + " " + index);
		    featureNum++;
		}
	    }



	    br.close();
	    in.close();
	    fstream.close();
	} catch (Exception e) {
	    e.printStackTrace();
	}

	return featureNum;
    }

    boolean shouldIgnoreInQuery(String word) {
	if (stopWordSet.contains(word) || word.matches("\\d+")) {
	    return true;
	}

	return false;
    }

    boolean shouldIgnoreFeature(String[] unigrams) {
	for (String unigram : unigrams) {
	    if (!shouldIgnoreInQuery(unigram)) {
		return false;
	    }
	}
	return true;
    }

    double getFactorOfFeature(int usedFeatureNum, double weight) {
	if (usedFeatureNum > 0) {
	    double factor = (double) Math.floor(fstFeatureFactor * weight / fstFeatureWeight * 100) / 100;
	    return factor;
	} else {
	    return fstFeatureFactor;
	}
    }

    void sortByComparator(HashMap<Integer, TopFeatureMeta> unsortMap, boolean isPosFeature) {

	List<Map.Entry<Integer, TopFeatureMeta>> list = new LinkedList<Map.Entry<Integer, TopFeatureMeta>>(unsortMap.entrySet());

	// sort list based on comparator, order from small -> large
	Collections.sort(list, new Comparator<Map.Entry<Integer, TopFeatureMeta>>() {
	    @Override
	    public int compare(Entry<Integer, TopFeatureMeta> t1, Entry<Integer, TopFeatureMeta> t2) {
		return t1.getValue().rank - t2.getValue().rank;
	    }
	});

	// tranverse
	int usedFeatureNum = 0;
	HashSet<String> queryFeatures = new HashSet<String>();
	String[] orgQueryUnigrams = orgQuery.toLowerCase().split(" ");
	HashSet<String> orgQuerySet = new HashSet<String>(Arrays.asList(orgQueryUnigrams));

	for (Iterator<Map.Entry<Integer, TopFeatureMeta>> it = list.iterator(); it.hasNext();) {
	    Map.Entry<Integer, TopFeatureMeta> entry = it.next();
	    TopFeatureMeta topFeatureMeta = entry.getValue();
//	    System.out.println(entry.getKey() + "\t" + entry.getValue().rank);
	    String feature = topFeatureMeta.featureToken;
	    String displayFeature = feature.replace("<s>", "BOT").replace("</s>", "EOT");
	    if (isPosFeature) {
		posWeights += displayFeature + ", ";
	    } else {
		negWeights += displayFeature + ", ";
	    }

	    if (isPosFeature && usedFeatureNum < featureNumSelected) {
		feature = feature.replace("<s>_", "");
		feature = feature.replace("_</s>", "");

		if (feature.contains("_")) { //bigrams or trigrams
		    String[] unigrams = feature.split("_");
		    if (!shouldIgnoreFeature(unigrams)) {//ignore stopwords
			feature = feature.replace("_", " ");
			feature = "|" + feature + "|";

			double factor = getFactorOfFeature(usedFeatureNum, topFeatureMeta.weight);
			feature = feature + "^" + factor;

			formulatedQuery += " " + feature;
			diplayQuery += feature + ", ";

			usedFeatureNum++;
		    } else {
			ignoredWordsInQuery += feature + ", ";
		    }

		} else { //unigrams
		    if (shouldIgnoreInQuery(feature)) { //ignore stopwords or numbers
			ignoredWordsInQuery += feature + ", ";
		    } else {
			if (queryFeatures.contains(feature) || orgQuerySet.contains(feature)) {
			    continue;
			}
			queryFeatures.add(feature);
			double factor = getFactorOfFeature(usedFeatureNum, topFeatureMeta.weight);
			feature = feature + "^" + factor;

			formulatedQuery += " " + feature;
			diplayQuery += feature + ", ";
			usedFeatureNum++;
		    }
		}
	    }
	}
    }
}
