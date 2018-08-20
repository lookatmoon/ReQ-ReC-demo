package feature;

import fileFetch.RW;
import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.KStemFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;

import global.GlobalVar;

import java.util.*;
import java.io.*;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;

public class Tokenize {

    boolean tokenURL;
    boolean keepPunctuation;
    boolean filterStopWords;
    boolean usingLexiconInWholeTweet;
    boolean removeRT;
    Lexicon lexicon;
    String urlPattern = GlobalVar.urlPattern;
    static Version lucenceVersion = Version.LUCENE_40;
    public HashMap<String, HashSet<String>> token_to_wordSet = new HashMap<String, HashSet<String>>();

    public Tokenize(FeatureConfig config) {
	tokenURL = config.configs.contains("tokenURL");
	keepPunctuation = config.configs.contains("keepPunctuation");
	filterStopWords = config.configs.contains("filterStopWords");
	usingLexiconInWholeTweet = config.configs.contains("usingLexiconInWholeTweet");
	removeRT = config.configs.contains("removeRT");

	if (usingLexiconInWholeTweet) {
	    lexicon = new Lexicon();
	}
//		System.out.println("configure: " + tokenURL + "\t" + keepPunctuation + "\t" + filterStopWords);
    }

    public class CustomAnalyzer extends Analyzer {

	private Version matchVersion;

	public CustomAnalyzer(Version matchVersion) {
	    this.matchVersion = matchVersion;
	}

	@Override
	protected TokenStreamComponents createComponents(String fieldName,
		Reader reader) {
	    Tokenizer source = new StandardTokenizer(matchVersion, reader);
//			Tokenizer source = new WhitespaceTokenizer(matchVersion, reader);
	    TokenStream filter = new LowerCaseFilter(matchVersion, source);
	    if (filterStopWords) {
		System.out.println("filter stop words");
		filter = new StopFilter(matchVersion, filter, StopAnalyzer.ENGLISH_STOP_WORDS_SET);
	    }
	    // Krovetz stemmer
	    filter = new KStemFilter(filter);
	    //Porter stemmer
//			filter = new PorterStemFilter(filter);

	    return new TokenStreamComponents(source, filter);
	}
    }

    public static String stemOneWord(String word, char stemmerType) {

	TokenStream stream = new StandardTokenizer(lucenceVersion, new StringReader(word));
	stream = new LowerCaseFilter(lucenceVersion, stream);

	if (stemmerType == 'p') {
	    stream = new PorterStemFilter(stream);
	} else {
	    stream = new KStemFilter(stream);
	}

	try {
	    stream.reset();
	    CharTermAttribute term = stream.addAttribute(CharTermAttribute.class);

	    if (stream.incrementToken()) {
		word = term.toString();
	    }
//			System.out.println();
	    stream.end();
	    stream.close();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	return word;
    }

    String processPunctuation(String text) {

	String processed = text.replaceAll("\\?\\?|\\?", " QuestionMark ").replaceAll("!", " ExclamMark ")
		.replaceAll("@", "AT").replaceAll("#", "Sharp")
		.replaceAll(":", " ColonMark ").replaceAll("--|-", " DashMark ").replaceAll("\\.\\.\\.|\\.\\.", " EllipsisMark ")
		.replaceAll("\"", " QuoteMark ").replaceAll("( ')|(' )", " QuoteMark ");

//		if(!processed.equals(text)){
//			System.out.println(text);
//			System.out.println(processed);
//		}

	return processed;
    }

    public static List<String> tokenizeWithoutLowercase(String text) {
	TokenStream stream = new StandardTokenizer(lucenceVersion, new StringReader(text));
	List<String> tokenList = new LinkedList<String>();
	try {
	    // first argument: which field to index; null: index all
	    CharTermAttribute cattr = stream
		    .addAttribute(CharTermAttribute.class);
	    while (stream.incrementToken()) {

		String token = cattr.toString();

		tokenList.add(token);
	    }
	    stream.end();
	    stream.close();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	return tokenList;
    }

    String tokenOneWord(String word) {
	String token = null;
	Analyzer analyzer = new CustomAnalyzer(lucenceVersion);
	TokenStream stream;
	try {
	    // first argument: which field to index; null: index all
	    stream = analyzer.tokenStream(null, new StringReader(word));
	    CharTermAttribute cattr = stream
		    .addAttribute(CharTermAttribute.class);
	    if (stream.incrementToken()) {

		token = cattr.toString();

	    }
	    stream.end();
	    stream.close();
	    analyzer.close();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	return token;
    }

    public List<String> tokenizeTextAndLexicon(String text, List<String> tokenList) {
	List<String> lexiconList = new ArrayList<String>(20);
	if (tokenURL) {
	    text = text.replaceAll(urlPattern, "http");
	}

	if (keepPunctuation) {
	    text = processPunctuation(text);
	}

	tokenList = new LinkedList<String>();
	Analyzer analyzer = new CustomAnalyzer(lucenceVersion);
	TokenStream stream;
	try {
	    // first argument: which field to index; null: index all
	    stream = analyzer.tokenStream(null, new StringReader(text));
	    CharTermAttribute cattr = stream
		    .addAttribute(CharTermAttribute.class);
	    while (stream.incrementToken()) {

		String token = cattr.toString();
		if (usingLexiconInWholeTweet) {
		    String kStemmedWord = Tokenize.stemOneWord(token, 'k');
		    String sentiTypeWord = lexicon.orgWord_sentiment.get(kStemmedWord);
		    lexiconList.add(sentiTypeWord);
		}
		tokenList.add(token);
	    }
	    stream.end();
	    stream.close();
	    analyzer.close();
	} catch (IOException e) {
	    e.printStackTrace();
	}

	return lexiconList;
    }
    

    public List<String> tokenizeText(String text) {
	
	if(removeRT){
//	    String org = text;
	    text = text.replaceAll("^RT", "");
//	    if(!org.equals(text)){
//		RW.pLine(org);
//		RW.pLine(text);
//	    }
	}
	
	text = text.replace("@", "mentiontoken").replace("#", "hashtagtoken");

	if (tokenURL) {
	    text = text.replaceAll(urlPattern, "http" + " $1");
	}

	if (keepPunctuation) {
	    text = processPunctuation(text);
	}

	List<String> tokenList = new ArrayList<String>(20);
	Analyzer analyzer = new CustomAnalyzer(lucenceVersion);
	TokenStream stream;
	try {
	    // first argument: which field to index; null: index all
	    stream = analyzer.tokenStream(null, new StringReader(text));
	    CharTermAttribute cattr = stream.addAttribute(CharTermAttribute.class);
	    OffsetAttribute offsetAttribute = stream.addAttribute(OffsetAttribute.class);
	    
	    String textLower = text.toLowerCase();
	    while (stream.incrementToken()) {

		String token = cattr.toString();
		token = token.replace("mentiontoken", "@").replace("hashtagtoken", "#");

		int startOffset = offsetAttribute.startOffset();
		int endOffset = offsetAttribute.endOffset();
		String word = textLower.substring(startOffset, endOffset);
		word = word.replace("mentiontoken", "@").replace("hashtagtoken", "#");
//			    RW.pLine(token + "\t" + word);
		tokenList.add(token);
		
		HashSet<String> wordSet = token_to_wordSet.get(token);
		if(wordSet == null){
		    wordSet = new HashSet<String>();
		    wordSet.add(word);
		    token_to_wordSet.put(token, wordSet);
		}else{
		    wordSet.add(word);
		}
	    }
	    stream.end();
	    stream.close();
	    analyzer.close();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

	return tokenList;
    }

    public static void main(String[] args) {

	String[] strings = {"Obama Kills 11 Million - Not A Tear Was Shed, Not Even A Fake One - http://t.co/w35atDJn",
	    "Obama has reportedly skipped HALF of the national security briefings.   http://t.co/I0jLmCT7", "@BarackObama: Our destiny is not written for us, it's written by us. And we can write that next chapter together.\" -President Obama\"", "Better off?? Not so much --&gt;&gt; Franchisors warn #Obamacare will halve profits | http://t.co/7CBi3OxS: http://t.co/toVMTwBO..."};

	//doTagging, posHalfWindowSize, usingLexiconInWholeTweet, usingLexiconInWindow, keepPunctuation
	FeatureConfig config = new FeatureConfig("tokenURL,keepPunctuation");
	Tokenize tokenize = new Tokenize(config);
	for (int i = 0; i < strings.length; i++) {
	    System.out.println("Analzying: " + strings[i]);
	    List<String> tokenList = tokenize.tokenizeText(strings[i]);

	    System.out.println(tokenList.toString());
	}

	System.out.println(Tokenize.stemOneWord("works", 'p'));
	System.out.println(Tokenize.stemOneWord("runs", 'k'));
    }
}