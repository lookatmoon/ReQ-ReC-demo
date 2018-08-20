package feature;

import java.util.*;

public class FeatureConfig {
	
	public Set<String> configs;

	//can take values:
	////////////////////////////////////////
	//tokenize: //note: features in tokenize will only affect word features, not affect nlp and twitter
	//text input to nlp and twitter is the original text
	//keepPunctuation,tokenURL,filterStopWords
	//usingNgramFreq,usingWordFeature
	//usingLexiconInWholeTweet
	//removeRT
	////////////////////////////////////////
	//nlp:
	//usingLexiconInWindow, usingLexiconWithDepencency, usingLexiconWithPOS
	//considerWordShape, considerNumSentences
	//doTagging, doParsing, taggingWholeTweet
	//puncBigram,addPronounExists
	//pathLenBetweenTargetAndSentiment
	//numNameEntity, nameEntityBigram
	//twitterMention
	//chunking
	////////////////////////////////////////
	//Twitter:
	//usingMentionPosition,addURLToken (only work when !usingWordFeature)
	//hashtagScore
	////////////////////////////////////////
	public FeatureConfig(String configStr){
		String[] configArray = configStr.split(",");
		configs = new HashSet<String>(Arrays.asList(configArray));
	}
	
	public static int getTokenIndex(Hashtable<String, Integer> feature_to_index, String feature, boolean isTraining){
		Integer featureIndex = feature_to_index.get(feature);
		if(featureIndex == null){
			if(isTraining){
				//index start from 1
				featureIndex = feature_to_index.size() + 1;
				feature_to_index.put(feature, featureIndex);
			}else{ //for test data, don't add new features
				featureIndex = -1;
			}
		}
		return featureIndex;
	}
}
