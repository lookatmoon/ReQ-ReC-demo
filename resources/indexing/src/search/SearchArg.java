package search;


import index.TextAnalyzer;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;



public class SearchArg {
	
	String[] indexPathes;
	int indexCount = 0;
	HashSet<String> addedIndexes = new HashSet<String>();
	int maxRtnResults = 1000000;
	String outputFields = null;
	String textQuery = null;
	String numericQuery = null;
	int offsetResult = 0;
	double randomPct = 1;
	
	SearchArg(String[] args){
		
		String indexes = null;
		int maxNumResults = 10;
		
		for (int i = 0; i < args.length; i++) {
			if ("-index".equals(args[i])) {
				indexes = args[i + 1];
				i++;
			} else if ("-query".equals(args[i])) {
				textQuery = args[i + 1];
				i++;
			} else if ("-nq".equals(args[i])) {
				numericQuery = args[i + 1];
				i++;
			} else if ("-max".equals(args[i])) {
				maxNumResults = Integer.parseInt(args[i + 1]);
				i++;
			} else if ("-out".equals(args[i])) {
				outputFields = args[i + 1];
				i++;
			} else if ("-offset".equals(args[i])) {
				offsetResult = Integer.parseInt(args[i + 1]);
				i++;
			} else if ("-random".equals(args[i])) {
				randomPct = Double.parseDouble(args[i + 1]);
				i++;
			}
		}
	    
		if(indexes == null){
			System.out.println("Please provide the index path.");
			System.exit(1);
		}
		
		if(textQuery == null && numericQuery == null){
			System.out.println("Please provide at least one of \"query\" and \"numericQuery\".");
			System.exit(1);
		}

		
		maxRtnResults = Math.min(maxRtnResults, maxNumResults + offsetResult);
		String[] indexArray = indexes.split(";");
		indexPathes = new String[indexArray.length];
		for(String index : indexArray){
			String indexPath = getIndexPath(index);
			if(indexPath != null ){
				indexPathes[indexCount] = indexPath;
				indexCount++;
			}
		}
		searchIndex(textQuery, numericQuery);
	}
	
	String getIndexPath(String index){
		if(index == null){
			return null;
		}
		
		index = index.trim();
		if(index.length() == 0){
			return null;
		}
		
		String indexPath = index;
		File file = new File(indexPath);
		if(!file.exists()){
			System.err.println("Index: '" + indexPath + "' does not exist.");
			System.exit(1);
			return null;
		}
		
		if(addedIndexes.contains(indexPath)){
			return null;
		}
		
		addedIndexes.add(indexPath);		
		return indexPath;
	}
	
	String getColumnVal(String field, Document doc){
		if("query".equals(field)){
			return textQuery;
		}
		
		if("numericQuery".equals(field)){
			return numericQuery;
		}
		
		String fieldVal = doc.get(field);
		if(fieldVal != null){
			return fieldVal;
		}
		
		return field;
	}
	
	void doPagingSearch(IndexSearcher searcher, Query query) throws IOException {

		// Collect enough docs to show 5 pages
		TopDocs results = searcher.search(query, maxRtnResults);
		//The top hits/docs for the query. hits.length is min(5 * hitsPerPage, maximum total results)
		ScoreDoc[] hits = results.scoreDocs; 
		System.err.println("Number of retrieved documents: " + hits.length);

		int numTotalHits = results.totalHits;
		System.err.println("Number of matching documents: " + numTotalHits);

		int start = offsetResult;
		int end = Math.min(numTotalHits, maxRtnResults);
		
		String[] outputFieldArray = null;
		if(outputFields != null){
			outputFieldArray = outputFields.split(";");
		}
		
		for (int i = start; i < end; i++) {
//			if (raw) { // output raw format
//				System.out.println("doc=" + hits[i].doc + " score="
//						+ hits[i].score);
//			}

			Document doc = searcher.doc(hits[i].doc);
			
			if(Math.random() > randomPct){
				continue;
			}
			
			if(outputFields == null){
				//null if TYPE_STORED.setStored(false);
				System.out.println(doc.get("text"));
			}else{
				int j;
				for(j=0; j<outputFieldArray.length-1; j++){
					System.out.print(getColumnVal(outputFieldArray[j], doc) + "\t");
				}
				System.out.println(getColumnVal(outputFieldArray[j], doc));
			}
			
		}
		
	}

	void searchIndex(String queryString, String numericQueries) {

		try {
			IndexReader[] indexReader = new IndexReader[indexCount];

			for (int i = 0; i < indexCount; i++) {
				indexReader[i] = DirectoryReader.open(FSDirectory.open(new File(indexPathes[i])));    
			}
			
			IndexSearcher searcher = new IndexSearcher(new MultiReader(indexReader));
			searcher.setSimilarity(new LMDirichletSimilarity());
			
			TextAnalyzer analyzer = new TextAnalyzer("text");
			QueryParser parser = new QueryParser(Version.LUCENE_40, "text", analyzer);
			
//			Query queryNum = NumericRangeQuery.newLongRange("docID", 0L, 404817801300942850L, true, true);
//			Query queryNum = NumericRangeQuery.newLongRange("time", 20130222000009L, 20130222000011L, true, true);
			BooleanQuery booleanQuery = new BooleanQuery();
			
			if(queryString!=null && queryString.trim().length()>0){
				Query queryText = parser.parse(queryString);
				booleanQuery.add(queryText, BooleanClause.Occur.MUST);
			}
			
			if(numericQueries!=null && numericQueries.trim().length()>0){
				String[] numericQueryArray = numericQueries.split(";");
				for(String numericQuery : numericQueryArray){
					if(numericQuery!=null && numericQuery.trim().length()>0){
						String[] eles = numericQuery.split(",");
						if(eles.length == 3){
							long lowerBound = Long.parseLong(eles[1]);
							long upperBound = Long.parseLong(eles[2]);
							Query queryNum = NumericRangeQuery.newLongRange(eles[0], lowerBound, upperBound, true, true);
							booleanQuery.add(queryNum, BooleanClause.Occur.MUST);
						}
					}
				}
			}
			
			doPagingSearch(searcher, booleanQuery);
			
			for (int i = 0; i < indexCount; i++) {
				indexReader[i].close();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public static void main(String[] args) {

		if (args == null || args.length < 4) {
			System.err.println("Usage: java -jar search.jar -index INDEX_PATH -query QUERY [-max MAX_RESULT_NUM_RETURNED] [-out OUTPUT_FORMAT]");
			System.exit(1);
		}else{
			new SearchArg(args);
		}
	}
}
