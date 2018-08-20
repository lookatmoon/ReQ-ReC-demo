package feature;

import fileFetch.RW;
import global.GlobalVar;

import java.io.BufferedReader;
import java.util.Hashtable;

public class TwitterFeature {
	boolean usingMentionPosition;
	boolean addURLToken;
	boolean usingWordFeature;
	boolean puncBigram;
	boolean isTraining;
	boolean hashtagScore;
	
	String hashtagScoreFile = GlobalVar.codePath + "data/featureToIndex/hashtagKLDiv.txt";
	Hashtable<String, Integer> nonWordFeature_to_index;
	String target;
	Hashtable<Integer, FeatureMeta> featureIndex_to_featureMeta = new Hashtable<Integer, FeatureMeta>();
	Hashtable<String, String> hashtag_to_score = new Hashtable<String, String>();
	
	void addIntToFeature_to_index(String featureStr, int val){
		int tokenIndex = FeatureConfig.getTokenIndex(nonWordFeature_to_index, featureStr, isTraining);
		FeatureMeta featureMeta = featureIndex_to_featureMeta.get(tokenIndex);
		if(featureMeta == null){
			featureMeta = new FeatureMeta(val, featureStr, 0, false);
			featureIndex_to_featureMeta.put(tokenIndex, featureMeta);
		}else{
			featureMeta.freq+=val;
		}
	}
	
	String categorizeScore(double score){
		if(score < 0.1){
			return "tag0.1";
		}else if(score < 0.3){
			return "tag0.3";
		}else if(score < 0.5){
			return "tag0.5";
		}else if(score < 0.7){
			return "tag0.7";
		}else{
			return "tagMax";
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
	
	boolean findHashTags(String text, String target){
		String[] words = text.toLowerCase().split(" ");
		String targethashtag = "#" + target;
		for(String word : words){
			if(word != null && word.length() > 2){
				char wordStart = word.charAt(0);
				if(wordStart == '#'){
					if(targethashtag.equals(word)){
						continue;
					}
					word = word.replaceAll("[^#_A-Za-z0-9]", "");
					String score = hashtag_to_score.get(word);
					if(score == null){
						addIntToFeature_to_index("tagRare", 1);
					}else{
						addIntToFeature_to_index(score, 1);
					}
				}
			}
		}
		
		return false;
	}
	
	public TwitterFeature(FeatureConfig config, boolean isTraining){
		this.isTraining = isTraining;
		usingMentionPosition = config.configs.contains("usingMentionPosition");
		addURLToken = config.configs.contains("addURLToken");
		usingWordFeature = config.configs.contains("usingWordFeature");
		puncBigram = config.configs.contains("puncBigram");
		hashtagScore = config.configs.contains("hashtagScore");
		
		if(hashtagScore){
			loadHashtagScore();
		}
	}
	
	public Hashtable<Integer, FeatureMeta> getTwitterFeatures(String text, 
			Hashtable<String, Integer> nonWordFeature_to_index, String target){
		featureIndex_to_featureMeta.clear();
		this.nonWordFeature_to_index = nonWordFeature_to_index;
		this.target = target;
		
		if(!puncBigram && usingMentionPosition){
			if(text.charAt(0) == '@' || text.charAt(1) == '@'){
				addIntToFeature_to_index("<s>@", 1);
//				System.out.println(text);
			}
		}
		
		if(!usingWordFeature && !puncBigram && addURLToken && text.contains("http")){
			addIntToFeature_to_index("http", 1);
		}
		
		if(hashtagScore){
			findHashTags(text, target);
		}
		
		return featureIndex_to_featureMeta;
	}
	
}
