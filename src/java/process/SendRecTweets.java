/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package process;

import feature.FeatureConfig;
import fileFetch.RW;
import global.GlobalVar;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SendRecTweets extends HttpServlet {

    /**
     * Processes requests for both HTTP
     * <code>GET</code> and
     * <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    //labels of docs in evaluation/human labeled file
    HashMap<Long, Integer> idInEvaFile_to_trueLabel = null;
    String evaFileQueryID = null;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
	    throws ServletException, IOException {
	boolean testing = GlobalVar.local;
	response.setContentType("text/html;charset=UTF-8");
	PrintWriter out = response.getWriter();
	try {
	    String requestType = request.getParameter("type");
	    String queryName = request.getParameter("query");
	    PathConfig pathConfig = new PathConfig(queryName);
            
	    if ("getTweets".equals(requestType)) { //get confident and uncertain tweets from classifer
		int queryNumRtn = Integer.parseInt(request.getParameter("queryNumRtn"));
		int confidentNumReturn = Integer.parseInt(request.getParameter("confidentNumRtn"));
		String needShow = request.getParameter("needShowTopRetriTweets");
		importEvaluationFromFile(queryName, pathConfig);
		boolean needShowTopRetriTweets = "true".equals(needShow);
		String queryTweets = getQueryTweets(pathConfig, queryNumRtn, confidentNumReturn, needShowTopRetriTweets);
		out.print(queryTweets);
	    } else if ("sendLabels".equals(requestType)) { //get labels from the browser
		String labels = request.getParameter("labels");
		updateTrainTestFiles(pathConfig, labels);
	    } else if ("getLabeledTweets".equals(requestType)) { //get labeled tweets from the server
		String trainData = getLabeledTweets(pathConfig);
		out.print(trainData);
	    } else if ("getAllTweetInfo".equals(requestType)) { //get the ranking of all unlabeled tweets in all iterations
//		String paraQueryID = request.getParameter("paraQueryID");
		String queryTweets = getAllTweets(pathConfig);
		out.print(queryTweets);
	    } else if ("searchAPI".equals(requestType)) { //search queries in API and do clustering
		String paraQueryID = request.getParameter("paraQueryID");
		String paraQuery = request.getParameter("paraQuery");
		String paraQueryTime = request.getParameter("paraQueryTime");
		String paraQueryTweetNum = request.getParameter("paraQueryTweetNum");
		String paraClusterNum = request.getParameter("paraClusterNum");
		String paraTopTweetNum = request.getParameter("paraTopTweetNum");

		storeParameters(pathConfig, paraQueryID, paraQuery, paraQueryTime, paraQueryTweetNum, paraClusterNum);
		testing = false;
                if (!testing) {
		    new CallAPI(paraQueryID, paraQuery, paraQueryTime, paraQueryTweetNum, paraClusterNum, paraTopTweetNum, true);
		}
		TransferSearchedFile transferSearchedFile = new TransferSearchedFile(pathConfig, true);

		out.print(transferSearchedFile.tweetGetInOuterLoop);
	    } else if ("re_searchAPI".equals(requestType)) { //inner loop finished and need to formulate query and re_search queries in API and do clustering

		String[] elements = readParameters(pathConfig);
		String paraQueryTweetNum = request.getParameter("paraQueryTweetNum");
		if (paraQueryTweetNum == null || paraQueryTweetNum.length() == 0) {
		    paraQueryTweetNum = elements[3];
		}

		String paraQuery = request.getParameter("paraQuery");
		paraQuery = paraQuery.replaceAll("\n", "").replace(", ", " ").replace(",", " ");

//		GetFeatureWeight getFeatureWeight = new GetFeatureWeight(pathConfig, elements[1]);
//		out.print(getFeatureWeight.querySummary);
		new BackUpPosData(pathConfig, paraQuery);
		//only one cluster
                testing = false;
		if (!testing) {
		    new CallAPI(elements[0], paraQuery, elements[2], paraQueryTweetNum, "0", "1", true);
		}
		TransferSearchedFile transferSearchedFile = new TransferSearchedFile(pathConfig, false);
		out.print(transferSearchedFile.tweetGetInOuterLoop);
	    }

	} finally {
	    out.close();
	}
    }

    void importEvaluationFromFile(String queryID, PathConfig pathConfig) {
	if (queryID.equals(evaFileQueryID)) {
	    return;
	}

	idInEvaFile_to_trueLabel = new HashMap<Long, Integer>();

	File file = new File(pathConfig.evaluationFile);
	if (!file.exists()) {
	    try {
		file.createNewFile();
	    } catch (IOException ex) {
		ex.printStackTrace();
	    }

	    return;
	}

	RW read = new RW(pathConfig.evaluationFile, 'r');
	try {
	    BufferedReader br = read.br;
	    String strLine;
	    while ((strLine = br.readLine()) != null) {


		String[] elements = strLine.split("\t");
		if (elements.length > 1) {
		    long id = Long.parseLong(elements[1]);
		    //-1 -> 2; 1 -> 4
//		    int label = Integer.parseInt(elements[0]) + 3;
		    int label = Integer.parseInt(elements[0]);
		    idInEvaFile_to_trueLabel.put(id, label);
		}
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	}
	read.close();
    }

    String[] readParameters(PathConfig pathConfig) {
	String[] elements = null;
	RW read = new RW(pathConfig.parameterPath, 'r');
	try {
	    BufferedReader br = read.br;
	    String strLine;
	    if ((strLine = br.readLine()) != null) {

		elements = strLine.split("\t");

	    }

	} catch (Exception e) {
	    e.printStackTrace();
	}
	read.close();
	return elements;
    }

    void storeParameters(PathConfig pathConfig, String paraQueryID, String paraQuery, String paraQueryTime, String paraQueryTweetNum, String paraClusterNum) {
	String parameterPath = pathConfig.parameterPath;
	RW write = new RW(parameterPath, 'w');
	write.wLine(paraQueryID + "\t" + paraQuery + "\t" + paraQueryTime + "\t" + paraQueryTweetNum + "\t" + paraClusterNum);
	write.close();
    }
    List<Long> timeStampList = new ArrayList<Long>();

    void readTimeFile(String file) {
	RW read = new RW(file, 'r');
	try {
	    BufferedReader br = read.br;
	    String strLine;
	    while ((strLine = br.readLine()) != null) {

		String[] elements = strLine.split("\t");
		if (elements.length > 1) {
		    long id = Long.parseLong(elements[0]);
		    timeStampList.add(id);
		}
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	}
	read.close();
    }

    double computeTime(long id, int startIndex, int endIndex) {
	long startID = timeStampList.get(startIndex);
	long endID = timeStampList.get(endIndex);
	long firstID = timeStampList.get(0);
	long lastID = timeStampList.get(timeStampList.size() - 1);

	if (id < firstID) {
	    return -1;
	} else if (id > lastID) {
	    return timeStampList.size();
	}

	if (id == startID) {
	    return startIndex;
	}

	if (id == endID) {
	    return endIndex;
	}

	if (endIndex - 1 == startIndex) {
	    double time = startIndex + (double) (id - startID) / (endID - startID);
//	    int timeInt = (int) (time * 100);
//	    time = (double) timeInt / 100;
	    return time;
	}

	int midIndex = (startIndex + endIndex) / 2;
	long midID = timeStampList.get(midIndex);
	if (id == midID) {
	    return midIndex;
	} else if (id < midID) {
	    return computeTime(id, startIndex, midIndex);
	} else { //id>midID
	    return computeTime(id, midIndex, endIndex);
	}
    }

    String getCentroidFromOrigin(PathConfig pathConfig, String tweetQueryTime) {
	if (timeStampList.size() == 0) {
	    readTimeFile(pathConfig.timeFile);
	}
	double queryTimeDbl;
	String queryTimeStr;
	queryTimeDbl = computeTime(Long.parseLong(tweetQueryTime), 0, timeStampList.size() - 1);
	queryTimeStr = String.format("%.2f", queryTimeDbl);

	String originalFile = pathConfig.originalFile;
	StringBuilder initialQueryData = new StringBuilder();

	RW read = new RW(originalFile, 'r');
	try {
	    BufferedReader br = read.br;
	    String strLine;
	    while ((strLine = br.readLine()) != null) {
		//label: 0: unlabeled; 1: pos; -1: neg
		String[] elements = strLine.split("\t");
		if (elements.length > 1) {
		    if ("-2".equals(elements[0])) {
			long id = Long.parseLong(elements[1]);
			Integer label = idInEvaFile_to_trueLabel.get(id);
			double timeDiffDbl = queryTimeDbl - computeTime(id, 0, timeStampList.size() - 1);
			String timeDiff = queryTimeStr + ", -" + String.format("%.2f", timeDiffDbl);

			String notation = "";
			if (label != null) {
			    notation = " <SPAN style='BACKGROUND-COLOR: #E5CCFF'>Manual label: " + label + "</SPAN>";
			} else {
			    label = -1;
			}

			initialQueryData.append(label + "\t" + elements[1] + "\t" + " (" + timeDiff + ") " + elements[2] + notation + "\n");
		    }
		}
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	}
	read.close();
	return initialQueryData.toString();
    }

    String getLabeledTweets(PathConfig pathConfig) {

	StringBuilder trainData = new StringBuilder();
	String trainDataFile = pathConfig.trainDataFile;
	File trainFile = new File(trainDataFile);
	if (!trainFile.exists()) {
	    return "null";
	}

	HashSet<String> preTrainIDs = new HashSet<String>();
	File trainPreFile = new File(pathConfig.trainInPreLoop);
	if (trainPreFile.exists()) {
	    RW read = new RW(pathConfig.trainInPreLoop, 'r');
	    try {
		BufferedReader br = read.br;
		String strLine;
		while ((strLine = br.readLine()) != null) {

		    preTrainIDs.add(strLine);
		}

	    } catch (Exception e) {
		e.printStackTrace();
	    }
	    read.close();
	}

	int posLabelNum = 0;
	int negLabelNum = 0;

	int posCurLabelNum = 0;
	int negCurLabelNum = 0;

	RW read = new RW(trainDataFile, 'r', "UTF8");
	try {
	    BufferedReader br = read.br;
	    String strLine;
	    while ((strLine = br.readLine()) != null) {
		trainData.append(strLine + "\n");
		if (strLine.startsWith("-")) {
		    negLabelNum++;
		} else {
		    posLabelNum++;
		}

		String[] elements = strLine.split("\t");
		if (elements.length > 1) {
		    if (!preTrainIDs.contains(elements[1])) {
			if (strLine.startsWith("-")) {
			    negCurLabelNum++;
			} else {
			    posCurLabelNum++;
			}
		    }
		}
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	}
	read.close();
	String labelStatistics = "# accumulated positve tweets: " + posLabelNum + ", # negative: " + negLabelNum;

	String curLabelStats = "# positve tweets in current outer loop: " + posCurLabelNum + ", # negative: " + negCurLabelNum;

	labelStatistics += ". " + curLabelStats;
	return trainData.toString() + "\n\n" + labelStatistics;
    }

    void labelToHashMap(String labels, HashMap<Long, Integer> id_to_label) {
	String[] labelAndIDs = labels.split("__");
	for (int i = 0; i < labelAndIDs.length; i++) {
	    String[] labelAndID = labelAndIDs[i].split("_");
	    if (labelAndID.length > 1) {
		int label = Integer.parseInt(labelAndID[0]);
		long id = Long.parseLong(labelAndID[1]);
		id_to_label.put(id, label);
	    }
	}
    }

    void updateTrainTestFiles(PathConfig pathConfig, String labels) {
	HashMap<Long, Integer> id_to_label = new HashMap<Long, Integer>();
	labelToHashMap(labels, id_to_label);

	String trainDataFile = pathConfig.trainDataFile;
	String testDataDir = pathConfig.testDataDir;
	String originalFile = pathConfig.originalFile;

	RW read = new RW(originalFile, 'r');
	RW writeTrain = new RW(trainDataFile, 'w');
	BufferedWriter outTrain = writeTrain.out;

	RW writeTest = new RW(testDataDir, 'w');
	BufferedWriter outTest = writeTest.out;

	RW writeEva = new RW(pathConfig.evaluationFile, 'a');
	BufferedWriter outEva = writeEva.out;

	try {
	    BufferedReader br = read.br;
	    String strLine;
	    while ((strLine = br.readLine()) != null) {
		//label: 0: unlabeled; 1: pos; -1: neg
		String[] elements = strLine.split("\t");
		if (elements.length > 1) {
		    long id = Long.parseLong(elements[1]);
		    Integer label = id_to_label.get(id);
		    if (label == null) {
			outTest.write(strLine + "\n");
		    } else {

			if (label > 1 || label < -1) { //new manual labeled
			    label = label / 2;
			    outEva.write(label + "\t" + elements[1] + "\t" + elements[2] + "\n");
			}

			outTrain.write(label + "\t" + elements[1] + "\t" + elements[2] + "\n");

			id_to_label.remove(id);
		    }
		}
	    }

	    read = new RW(pathConfig.completeRetrievedData, 'r');
	    br = read.br;
	    while ((strLine = br.readLine()) != null) {
		//label: 0: unlabeled; 1: pos; -1: neg
		String[] elements = strLine.split("\t");
		if (elements.length > 1) {
		    long id = Long.parseLong(elements[1]);
		    Integer label = id_to_label.get(id);
		    if (label != null) {

			if (label > 1 || label < -1) { //new manual labeled
			    label = label / 2;
			    outEva.write(label + "\t" + elements[1] + "\t" + elements[2] + "\n");
			}

			outTrain.write(label + "\t" + elements[1] + "\t" + elements[2] + "\n");
		    }
		}
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	}
	read.close();
	writeTrain.close();
	writeTest.close();
	writeEva.close();
    }

    String getAllTweets(PathConfig pathConfig) throws IOException {

	String trainDataFile = pathConfig.trainDataFile;
	File trainFile = new File(trainDataFile);
	if (!trainFile.exists()) {
	    return "NoTrainFile";
	}

	FeatureConfig config;
	String methodName;

	int posHalfWindowSize;
	int ngramPOS;
	int ngramDependency;
	posHalfWindowSize = 0;
	ngramPOS = 1;
	ngramDependency = 1;

//		//only word features
	methodName = "!keepPunctuation,tokenURL,addURLToken,usingWordFeature,removeRT";

	config = new FeatureConfig(methodName);

	String[] elements = readParameters(pathConfig);

	PredictCompleteData predictCompleteData = new PredictCompleteData(pathConfig, methodName, 0, 0, config, posHalfWindowSize, ngramPOS, ngramDependency, true, idInEvaFile_to_trueLabel, elements[2], false);
	String queryTweets = predictCompleteData.allTweets + "\n\n" + predictCompleteData.predictStats;

	queryTweets += "\n\nQueryID: " + elements[0] + ". Query: " + elements[1];

	GetFeatureWeiComplete getFeatureWeight = new GetFeatureWeiComplete(pathConfig, predictCompleteData.token_to_wordSet, elements[1]);
	queryTweets += "\n\n" + getFeatureWeight.posWordsString + "\n\n" + getFeatureWeight.negWordsString;

	return queryTweets;

    }

    String getQueryTweets(PathConfig pathConfig, int queryNumRtn, int confidentNumReturn, boolean needShowTopRetriTweets) throws IOException {
	File orgFile = new File(pathConfig.originalFile);
	if (!orgFile.exists()) {
	    return "NoOrginalFile";
	}

	String[] elements = readParameters(pathConfig);

	String trainDataFile = pathConfig.trainDataFile;
	File trainFile = new File(trainDataFile);
	if (!trainFile.exists()) {
	    String initialQueryData = getCentroidFromOrigin(pathConfig, elements[2]);
	    return initialQueryData;
	}

	String resultFile = pathConfig.resultFile;

	int queryNumReturn = queryNumRtn;

	FileWriter fstreamOut = new FileWriter(resultFile);
	BufferedWriter outFile = new BufferedWriter(fstreamOut);

	FeatureConfig config;
	String methodName;

	int posHalfWindowSize;
	int ngramPOS;
	int ngramDependency;
	posHalfWindowSize = 0;
	ngramPOS = 1;
	ngramDependency = 1;

//		//only word features
	methodName = "!keepPunctuation,tokenURL,addURLToken,usingWordFeature,removeRT";

	config = new FeatureConfig(methodName);


	PredictCompleteData predictCompleteData = new PredictCompleteData(pathConfig, methodName, queryNumRtn, confidentNumReturn, config, posHalfWindowSize, ngramPOS, ngramDependency, false, idInEvaFile_to_trueLabel, elements[2], needShowTopRetriTweets);
	String queryTweets = predictCompleteData.queryTweets + "\n" + predictCompleteData.confidentTweets + "\n\n" + predictCompleteData.predictStats;

	GetFeatureWeiComplete getFeatureWeight = new GetFeatureWeiComplete(pathConfig, predictCompleteData.token_to_wordSet, elements[1]);
	String features = "\n\nTop positive features: " + getFeatureWeight.posWeights + "\n\nTop negative features: " + getFeatureWeight.negWeights
		+ "\n\n" + getFeatureWeight.posWordsString + "\n\n" + getFeatureWeight.negWordsString + "\n\n" + getFeatureWeight.querySummary;


	GetActiveQueryBySemi getActiveQueryBySemi = new GetActiveQueryBySemi(pathConfig, queryNumReturn, confidentNumReturn, methodName, config, outFile, posHalfWindowSize, ngramPOS, ngramDependency, idInEvaFile_to_trueLabel);
	queryTweets += ". " + getActiveQueryBySemi.predictStats;

	queryTweets += features;

	outFile.close();
	fstreamOut.close();


	return queryTweets;

    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
	    throws ServletException, IOException {
	processRequest(request, response);
    }

    /**
     * Handles the HTTP
     * <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
	    throws ServletException, IOException {
	processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
	return "Short description";
    }// </editor-fold>
}
