package process;

import java.io.*;
import java.util.*;

import feature.Tokenize;
import fileFetch.RW;
import global.GlobalVar;

public class GetVocabulary {
    //tokenize and remove stop words, stemming

    Tokenize tokenize;
    //n gram
    int nGram;
    int textIndex;
    int labelIndex;
    HashSet<String> wordSet = new HashSet<String>();
    HashMap<String, Integer> trigram_to_frequency = new HashMap<String, Integer>();
    int triGramTheta = 2; //trigrams larger than triGramTheta will be included
    HashSet<String> selectedTrigramSet = new HashSet<String>();
    HashSet<String> stopWordSet = GlobalVar.stopWordSet;

    GetVocabulary(Tokenize tokenize, int nGram, String trainingFile, int labelIndex, int textIndex) {
	this.tokenize = tokenize;
	this.nGram = nGram;
	this.labelIndex = labelIndex;
	this.textIndex = textIndex;

	readFile(trainingFile);

	selectTrigrams();

//		wordSet.clear();
//		printWordIndex();
    }

    void selectTrigrams() {
	for (Map.Entry<String, Integer> entry : trigram_to_frequency.entrySet()) {
	    if (entry.getValue() > triGramTheta) {
		selectedTrigramSet.add(entry.getKey());
//		RW.pLine("Selected trigram: " + entry.getKey());
	    } else {
//		RW.pLine("Removed trigram: " + entry.getKey());
	    }
	}
    }

    void readFile(String filePath) {
	try {

	    FileInputStream fstream = new FileInputStream(filePath);

	    DataInputStream in = new DataInputStream(fstream);
	    BufferedReader br = new BufferedReader(new InputStreamReader(in));
	    String strLine;

	    while ((strLine = br.readLine()) != null) {

		String[] elements = strLine.split("\t");
		if (elements.length > 1) {
		    List<String> tokenList = tokenize.tokenizeText(elements[textIndex]);
		    addUnigramToVocabulary(tokenList);
		    if (nGram > 1) {
			addBigramToVocabulary(tokenList);
		    }

		    if (nGram > 2) {
			addTrigramToVocabulary(tokenList);
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

    boolean shouldIgnoreInQuery(String word) {
	if (stopWordSet.contains(word) || word.matches("\\d+")) {
	    return true;
	}
	return false;
    }

    void addTrigram(String triGram) {
	Integer freq = trigram_to_frequency.get(triGram);
	if (freq == null) {
	    trigram_to_frequency.put(triGram, 1);
	} else {
	    freq++;
	    trigram_to_frequency.put(triGram, freq);
	}
    }

    void addTrigramToVocabulary(List<String> tokenList) {
	//insert start and end symbol to the token list
//	tokenList.add(tokenList.size(), "</s>");
	String lastlastToken = "<s>";
	String lastToken = tokenList.get(0);
	for (int i = 1; i < tokenList.size(); i++) {
	    String token = tokenList.get(i);
	    String triGram = lastlastToken + "_" + lastToken + "_" + token;
	    lastlastToken = lastToken;
	    lastToken = token;

	    if (!shouldIgnoreInQuery(lastlastToken) || !shouldIgnoreInQuery(lastToken) || !shouldIgnoreInQuery(token)) {
		addTrigram(triGram);
		//	    System.out.print(triGram + "; ");
		if (lastlastToken.startsWith("#")) {
		    String lastlastTokenRmHash = lastlastToken.substring(1);
		    triGram = lastlastTokenRmHash + "_" + lastToken + "_" + token;
		    addTrigram(triGram);
		}
		
		if (lastToken.startsWith("#")) {
		    String lastTokenRmHash = lastToken.substring(1);
		    triGram = lastlastToken + "_" + lastTokenRmHash + "_" + token;
		    addTrigram(triGram);
		}
		
		if (token.startsWith("#")) {
		    String tokenRmHash = token.substring(1);
		    triGram = lastlastToken + "_" + lastToken + "_" + tokenRmHash;
		    addTrigram(triGram);
		}
	    }


	}
//		System.out.println();
    }

    void addBigramToVocabulary(List<String> tokenList) {
	//insert start and end symbol to the token list
	tokenList.add(tokenList.size(), "</s>");
	String lastToken = "<s>";
	for (String token : tokenList) {
	    String biGram = lastToken + "_" + token;
	    wordSet.add(biGram);
//			System.out.print(lastToken + " " + token + "; ");
	    boolean lastIsHash = false;
	    boolean tokenIsHash = false;
	    String lastTokenRmHash = "";
	    String tokenRmHash = "";
	    if (lastToken.startsWith("#")) {
		lastIsHash = true;
		lastTokenRmHash = lastToken.substring(1);
	    }
	    if (token.startsWith("#")) {
		tokenIsHash = true;
		tokenRmHash = token.substring(1);
	    }


	    if (lastIsHash && !tokenIsHash) {
		biGram = lastTokenRmHash + "_" + token;
		wordSet.add(biGram);
	    } else if (!lastIsHash && tokenIsHash) {
		biGram = lastToken + "_" + tokenRmHash;
		wordSet.add(biGram);
	    } else if (lastIsHash && tokenIsHash) {
		biGram = lastToken + "_" + tokenRmHash;
		wordSet.add(biGram);
		biGram = lastTokenRmHash + "_" + token;
		wordSet.add(biGram);
		biGram = lastTokenRmHash + "_" + tokenRmHash;
		wordSet.add(biGram);
	    }

	    lastToken = token;
	}
//		System.out.println();
    }

    void addUnigramToVocabulary(List<String> tokenList) {
	for (String token : tokenList) {
	    wordSet.add(token);
	    if (token.startsWith("#")) {
		token = token.substring(1);
		wordSet.add(token);
	    }
	}
    }
}
