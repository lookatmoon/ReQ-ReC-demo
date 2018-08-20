package feature;

import fileFetch.RW;
import global.GlobalVar;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;

public class Lexicon {
	String lexiconPath = GlobalVar.sentiLexiconPath + "subjclueslen1-HLTEMNLP05.tff.txt";
	String slangPath = GlobalVar.sentiLexiconPath + "slang/slang_list.txt";
	String emoticonPath = GlobalVar.sentiLexiconPath + "emoticon/utf8/";
	String pronounPath = GlobalVar.sentiLexiconPath + "pronoun.txt";
	String hashtagScoreFile = GlobalVar.codePath + "data/featureToIndex/hashtagKLDiv.txt";
	
	public HashMap<String, String> orgWord_sentiment = new HashMap<String, String>();
	public HashMap<String, String> stemmedWord_sentiment = new HashMap<String, String>();
	public HashSet<String> slangs = new HashSet<String>();
	public List<String> posFaces = new LinkedList<String>();
	public List<String> negFaces = new LinkedList<String>();
	public List<String> neutralFaces = new LinkedList<String>();
	public Hashtable<String, String> hashtag_to_score = new Hashtable<String, String>();
	
	public HashMap<String, String> pronoun_type = new HashMap<String, String>();
	
	Lexicon(){
		readLexicon();
		readSlangs();
		readEmoticon(emoticonPath + "posFace.txt", posFaces);
		readEmoticon(emoticonPath + "negFace.txt", negFaces);
		readEmoticon(emoticonPath + "neutralFace.txt", neutralFaces);
		readPronoun();
		loadHashtagScore();
	}
	
	
	String categorizeScore(double score){
		if(score < 0.1){
			return "#0.1";
		}else if(score < 0.3){
			return "#0.3";
		}else if(score < 0.5){
			return "#0.5";
		}else if(score < 0.7){
			return "#0.7";
		}else{
			return "#Max";
		}
	}
	
	void loadHashtagScore(){
		RW read = new RW(hashtagScoreFile, 'r');
		try {
			BufferedReader br = read.br;
			String strLine;
			while ((strLine = br.readLine()) != null) {
				
				String[] elements = strLine.split("\t");
				if(elements.length > 1){
					double score = Double.parseDouble(elements[1]);
					String scoreType = categorizeScore(score);
					String hashtag = elements[0];
					hashtag_to_score.put(hashtag, scoreType);
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		read.close();
	}
	
	
	void readPronoun(){
		try {

			FileInputStream fstream = new FileInputStream(pronounPath);

			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF8"));

			String strLine;

			while ((strLine = br.readLine()) != null) {
				String [] strs = strLine.split("\t");
				if(strs.length > 1){
					pronoun_type.put(strs[0], strs[1]);
				}
			}
			br.close();
			in.close();
			fstream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	void readEmoticon(String fileName, List<String> faceList){
		try {

			FileInputStream fstream = new FileInputStream(fileName);

			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF8"));

			String strLine;

			while ((strLine = br.readLine()) != null) {

//				System.out.println(strLine);
				faceList.add(strLine);
			}
			br.close();
			in.close();
			fstream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	void readSlangs(){
		try {

			FileInputStream fstream = new FileInputStream(slangPath);

			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			String strLine;

			while ((strLine = br.readLine()) != null) {
//				System.out.println(strLine);
				slangs.add(strLine);
			}
			br.close();
			in.close();
			fstream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
   	void readLexicon(){
		try {

			FileInputStream fstream = new FileInputStream(lexiconPath);

			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			while ((strLine = br.readLine()) != null) {
				String [] strs = strLine.split(" ");
				if(strs.length > 4){
					String word = strs[2].substring(6);
					String stemmedWord = Tokenize.stemOneWord(word, 'p');
					if(strs[5].charAt(14) == 'p'){
						
						if(strs[0].charAt(5) == 'w'){ //weakly subjective
							orgWord_sentiment.put(word, "weakPos"); //weak positive
							stemmedWord_sentiment.put(stemmedWord, "weakPos"); 
						}else{ //strongly subjective
							orgWord_sentiment.put(word, "strongPos"); //strong positive
							stemmedWord_sentiment.put(stemmedWord, "strongPos"); 
						}
						
					}else if(strs[5].charAt(16) == 'g'){
						
						if(strs[0].charAt(5) == 'w'){ //weakly subjective
							orgWord_sentiment.put(word, "weakNeg"); //weak negative
							stemmedWord_sentiment.put(stemmedWord, "weakNeg"); //weak positive
						}else{ //strongly subjective
							orgWord_sentiment.put(word, "strongNeg"); //strong negative
							stemmedWord_sentiment.put(stemmedWord, "strongNeg"); //weak positive
						}
						
					}
				}
			}

			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
