package index;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.en.KStemFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.util.Version;

import java.io.*;
import java.util.HashMap;

public class TextAnalyzer extends Analyzer {
	
	HashMap<String, String> fieldName_to_type = new HashMap<String, String>();

	private Version matchVersion = Version.LUCENE_40;
	String field;
	public TextAnalyzer(String field){
		this.field = field;
		buildMap();
	}
	
	void buildMap(){
		fieldName_to_type.put("text", "text");
	}

	@Override
	protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
		String fieldType = fieldName_to_type.get(field);
		
		if("text".equals(fieldType)){ 
			Tokenizer source = new StandardTokenizer(matchVersion, reader);

			TokenStream filter = new LowerCaseFilter(matchVersion, source);
//			filter = new StopFilter(matchVersion, filter, StopAnalyzer.ENGLISH_STOP_WORDS_SET);
			// Krovetz stemmer
			filter = new KStemFilter(filter);

			return new TokenStreamComponents(source, filter);

		}else{ //default for metadata, e.g., userName
			Tokenizer source = new WhitespaceTokenizer(matchVersion, reader);

			TokenStream filter = new LowerCaseFilter(matchVersion, source);
			return new TokenStreamComponents(source, filter);
		}

	}

}
