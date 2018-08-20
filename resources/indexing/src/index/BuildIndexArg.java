package index;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;

import fileFetch.*;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;



public class BuildIndexArg {

	String dataPath;
	
	int indexedDocNum = 0;
	
	void readIndexData(IndexWriter writer) {
		Collection<File> allFiles = new ArrayList<File>();
		new GetAllFiles(dataPath, allFiles);

		for (File file : allFiles) {
			indexedDocNum = 0;
			String fileName = file.toString();
			System.err.println("Indexing " + file.getName() + " ...");
			if (fileName.endsWith(".gz")) {
				readGZIPFile(fileName, writer);
			} else {
				readFile(fileName, writer);
			}
		}

		System.err.println("Total indexed documents: " + indexedDocNum);
	}

	void readGZIPFile(String fileName, IndexWriter writer) {
		// use BufferedReader to get one line at a time
		BufferedReader gzipReader = null;
		try {
			// simple loop to dump the contents to the console
			gzipReader = new BufferedReader(new InputStreamReader(
					new GZIPInputStream(new FileInputStream(fileName))));
			while (gzipReader.ready()) {
				String strLine = gzipReader.readLine();
				processText(strLine, writer);
			}
			gzipReader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void readFile(String fileName, IndexWriter writer) {
		try {
			FileInputStream fstream = new FileInputStream(fileName);

			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			while ((strLine = br.readLine()) != null) {
				processText(strLine, writer);
			}

			br.close();
			in.close();
			fstream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void addDoc(String text, String docID, IndexWriter writer) throws IOException {
		Document doc = new Document();

		doc.add(new VecTextField("text", text, Field.Store.YES));
		doc.add(new VecTextField("docID", docID, Field.Store.YES));
		
		writer.addDocument(doc);
	}

	void processText(String strLine, IndexWriter writer) {

		try {
			String[] elements = strLine.split("\t");
			if(elements.length > 1){
				String id = elements[0];
				String text = elements[1];
				addDoc(text, id, writer);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		indexedDocNum++;
		if(indexedDocNum % 1000000 == 0){
			System.err.println("Indexed documents: " + indexedDocNum);
		}
	}

	void createIndex(String indexPath) {
		File dirs = new File(indexPath);
		dirs.mkdirs();
	}
	


	public BuildIndexArg(String dataPath, String indexPath) {
		
		File file = new File(dataPath);
		if(!file.exists()){
			System.err.println("Data do not exist: " + dataPath);
			System.exit(1);
		}
		
		createIndex(indexPath);
		this.dataPath = dataPath;
		
		try {
			Directory dir = FSDirectory.open(new File(indexPath));

			Map<String, Analyzer> analyzerPerField = new HashMap<String, Analyzer>();
			analyzerPerField.put("text", new TextAnalyzer("text"));
			analyzerPerField.put("docID", new TextAnalyzer("docID"));

			PerFieldAnalyzerWrapper aWrapper = new PerFieldAnalyzerWrapper(
					new StandardAnalyzer(Version.LUCENE_40), analyzerPerField);

			IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_40, aWrapper);

			iwc.setOpenMode(OpenMode.CREATE);
			iwc.setRAMBufferSizeMB(1000.0);
			iwc.setMaxThreadStates(20);

			IndexWriter writer = new IndexWriter(dir, iwc);

			readIndexData(writer);

			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
//		new BuildIndexArg(dataPath, indexPath);
		
		if (args == null || args.length < 2) {
			System.err.println("Usage: java -jar index.jar DATA_PATH INDEX_PATH");
			System.exit(1);
		}else{
			new BuildIndexArg(args[0], args[1]);
		}
	}

}
