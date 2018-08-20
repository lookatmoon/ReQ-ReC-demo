/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package process;

import global.GlobalVar;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class CallAPI {

    String apiPath = GlobalVar.search_api;
    String clusterPath = GlobalVar.clustering;
    
    String paraQueryID;
    String quotedQuery;
//    String paraQuery;
    String paraQueryTime;
    String paraQueryTweetNum;
    String paraClusterNum;
    String paraTopTweetNum;

    CallAPI(String paraQueryID, String paraQuery, String paraQueryTime, String paraQueryTweetNum, String paraClusterNum, String paraTopTweetNum, boolean doClustering) {
	this.paraQueryID = paraQueryID;
//	this.paraQuery = paraQuery;
//	paraQuery = paraQuery.replace("'", "\'");
	quotedQuery = "\" " + paraQuery + " \"";
	this.paraQueryTime = paraQueryTime;
	this.paraQueryTweetNum = paraQueryTweetNum;
	this.paraClusterNum = paraClusterNum;
	this.paraTopTweetNum = paraTopTweetNum;
	apiCall();
	if(doClustering){
	    clusteringCall();
	}
	
    }

    void apiCall() {
	Runtime rt = Runtime.getRuntime();
	//String[] cmd = {apiPath, paraQueryID, quotedQuery, paraQueryTime, paraQueryTweetNum};
        String[] cmd = {apiPath, paraQueryID, quotedQuery, paraQueryTweetNum};

        Process p;
	try {
	    p = rt.exec(cmd);
	    InputStream in = p.getInputStream();

	    InputStreamReader isr = new InputStreamReader(in);
	    BufferedReader br = new BufferedReader(isr);
	    String line;

	    while ((line = br.readLine()) != null) {
		System.out.println(line);
	    }
	    br.close();
	    isr.close();
	    in.close();
	    p.destroy();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }
    
    void clusteringCall() {
	Runtime rt = Runtime.getRuntime();
	String[] cmd = {clusterPath, paraQueryID, paraClusterNum, paraTopTweetNum};
	Process p;
	try {
	    p = rt.exec(cmd);
	    InputStream in = p.getInputStream();

	    InputStreamReader isr = new InputStreamReader(in);
	    BufferedReader br = new BufferedReader(isr);
	    String line;

	    while ((line = br.readLine()) != null) {
//		System.out.println(line);
	    }
	    br.close();
	    isr.close();
	    in.close();
	    p.destroy();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }
}
