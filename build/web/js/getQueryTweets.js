function sendQueryRequest(query){
    if(query == null || query.length == 0){
	return;
    }
    queryName = query;
    $("#paraSetContainer").animate({ 
	top: "700px"
    }, 1000 );
    getLabeledTweets();
}


function allUnLabeledTweets(query){
    if(query == null || query.length == 0){
	return;
    }
    
    window.open('allTweetsInfo.jsp?paraQueryID='+query+"&query="+query);
}


function getUnLabeledTweets(){
    var queryNumRtn = $('#queryNumRtn').val();
    if(queryNumRtn == null || queryNumRtn.length == 0){
	queryNumRtn = 10;
    }
    
    var confidentNumRtn = $('#confidentNumRtn').val();
    if(confidentNumRtn == null || confidentNumRtn.length == 0){
	confidentNumRtn = 10;
    }
    $.ajax({
	type: "GET",
	url: "SendRecTweets",
	data: "type=getTweets&query="+queryName+"&queryNumRtn="+queryNumRtn
	    +"&confidentNumRtn="+confidentNumRtn+"&needShowTopRetriTweets="+needShowTopRetriTweets,
	success: function(tweets) {
	    
	    needShowTopRetriTweets = "false";
	    $("#one-column-emphasis").html("");
	    $("#one-column-emphasis-rightBottom").html("");
	    
	    if("NoOrginalFile" == tweets){
		alert("Data for " + queryName + " haven't been downloaded yet. Please search via API first.");
		return;
	    }
	    
	    processTweets(tweets);
	    if(tweetArray.length != 0){
		drawTable(tweetArray, tweetLabels, "one-column-emphasis", "checkboxNewData", 1);
	    }
	    
	    if(labeledTweetArray.length != 0){
		drawTable(labeledTweetArray, labeledTweetLabels, "one-column-emphasis-right", "checkboxLabeledData", 0);
	    }
	    
	    $("#bottonDiv").show();

	}
    });
}

function getLabeledTweets(){
    $.ajax({
	type: "GET",
	url: "SendRecTweets",
	data: "type=getLabeledTweets&query="+queryName,
	success: function(tweets) {
	    

	    getUnLabeledTweets();
	    
	    if("null" == tweets){
		labeledTweetIDs = [];
		labeledTweetArray = [];
		labeledTweetLabels = [];
		return;
	    }
	    
	    var allTextLines = tweets.split('\n\n');
	    if(allTextLines[1]!=null && allTextLines[1].length!=0){
		$("#labeledStatsDiv").text("Labeled tweets: " + allTextLines[1]);
	    }
	    
	    $("#one-column-emphasis-right").html("");
	    processLabeledTweets(allTextLines[0]);
	    
	}
    });
}


function sendLabeledConfidentTweetsBack(){
    var i = 0;
    var returnLabels = returnUncertainLabels;
    for(i=0; i<tweetArray.length; i++){
	var label = $("#checkboxNewData"+i).is(':checked') ? 2 : -2;
	returnLabels += label + "_" + tweetIDs[i] + "__";
    }
    
    for(i=0; i<labeledTweetArray.length; i++){
	
	label = $("#checkboxLabeledData"+i).is(':checked') ? 1 : -1;
	returnLabels += label + "_" + labeledTweetIDs[i] + "__";
    }
    
    for(i=0; i<confidTweetArray.length; i++){
	
	label = $("#checkboxConfidentData"+i).is(':checked') ? 2 : -2;
	returnLabels += label + "_" + confidTweetIDs[i] + "__";
    }
    
    $("#bottonDiv").hide();
    $("#bottonRightDiv").hide();
    //    console.log(returnLabels);
    $.ajax({
	type: "POST",
	url: "SendRecTweets",
	data: "type=sendLabels&labels="+returnLabels+"&query="+queryName,
	success: function() {
	    getLabeledTweets();
	}
    });
}


function sendLabeledTweetsBack(){
    var i = 0;
    var returnLabels = returnUncertainLabels;
    for(i=0; i<tweetArray.length; i++){
	var label = $("#checkboxNewData"+i).is(':checked') ? 2 : -2;
	returnLabels += label + "_" + tweetIDs[i] + "__";
    }
    
    for(i=0; i<labeledTweetArray.length; i++){
	
	label = $("#checkboxLabeledData"+i).is(':checked') ? 1 : -1;
	returnLabels += label + "_" + labeledTweetIDs[i] + "__";
    }
    
    $("#bottonDiv").hide();
    $("#bottonRightDiv").hide();
    //    console.log(returnLabels);
    $.ajax({
	type: "POST",
	url: "SendRecTweets",
	data: "type=sendLabels&labels="+returnLabels+"&query="+queryName,
	success: function() {
	    
	    getLabeledTweets();
	}
    });
}

function getSelectedParameters() {
    var str = "";
    
    $("#paraSetSelecter option:selected").each(function () {
	str += $(this).text();
    });
    var entries = str.split('; ');
    $('#paraQueryID').val(entries[0]);
    $('#paraQuery').val(entries[1]);
    $('#paraQueryTime').val(entries[2]);
}

function searchAPI() {
     $(".main_body_labeler").show();
    var paraQueryID = $('#paraQueryID').val();
    if(paraQueryID == null || paraQueryID.length == 0){
	return;
    }
    
    var paraQuery = $('#paraQuery').val();
    if(paraQuery == null || paraQuery.length == 0){
	return;
    }
    
    var paraQueryTime = $('#paraQueryTime').val();
    if(paraQueryTime == null || paraQueryTime.length == 0){
	return;
    }
    
    var paraQueryTweetNum = $('#paraQueryTweetNum').val();
    if(paraQueryTweetNum == null || paraQueryTweetNum.length == 0){
	return;
    }
    
    var paraClusterNum = $('#paraClusterNum').val();
    if(paraClusterNum == null || paraClusterNum.length == 0){
	return;
    }
    
    
    var paraTopTweetNum = $('#paraTopTweetNum').val();
    if(paraTopTweetNum == null || paraTopTweetNum.length == 0){
	return;
    }
    
    
    $("#one-column-emphasis-right").html("");
    $("#one-column-emphasis").html("");
    $("#one-column-emphasis-rightBottom").html("");
    confidTweetArray = [];
    confidTweetIDs = [];
    confidTweetLabels = [];

    labeledTweetIDs = [];
    labeledTweetArray = [];
    labeledTweetLabels = [];

    tweetIDs = [];
    tweetArray = [];
    tweetLabels = [];

    
    $.ajax({
	type: "GET",
	url: "SendRecTweets",
	data: "type=searchAPI&paraQueryID="+paraQueryID+"&paraQuery="+paraQuery+"&paraTopTweetNum="+paraTopTweetNum
	+"&query="+paraQueryID+"&paraQueryTime="+paraQueryTime+"&paraQueryTweetNum="+paraQueryTweetNum+"&paraClusterNum="+paraClusterNum,
	success: function(tweetNumOuterLoop) {
	    $("#tweetNumOuterLoop").text("New unique, English tweets retrieved via API: " + tweetNumOuterLoop);
	    //alert("New unique, English tweets retrieved via API: " + tweetNumOuterLoop);
	    $("#query").val(paraQueryID);
	    queryName = paraQueryID;
	    sendQueryRequest(paraQueryID);
	}
    });   
    
}

function re_searchAPI() {

    var qid = $('#query').val();
    if(qid == null || qid.length == 0){
	qid = $('#paraQueryID').val();
	if(qid == null || qid.length == 0){
	    alert("Please fill in the query id");
	    return;
	}
    }
    
    var newQuery = $('#newQueryToSubmitInput').val();
    if(newQuery == null || newQuery.length == 0){
	alert("Please fill in the query");
	return;
    }
    
    queryName = qid;
    $("#one-column-emphasis").html("");
    $("#one-column-emphasis-rightBottom").html("");
    
    confidTweetArray = [];
    confidTweetIDs = [];
    confidTweetLabels = [];


    tweetIDs = [];
    tweetArray = [];
    tweetLabels = [];
    
    var paraQueryTweetNum = $('#paraQueryTweetNum2').val();
    $("#oldQueryDiv").text("Previous query: " + newQuery);
    
    $.ajax({
	type: "GET",
	url: "SendRecTweets",
	data: "type=re_searchAPI&paraQueryID="+qid
	+"&query="+qid+"&paraQueryTweetNum="+paraQueryTweetNum+"&paraQuery="+newQuery,
	success: function(tweetNumOuterLoop) {
	    $("#tweetNumOuterLoop").text("New unique, English tweets retrieved via API: " + tweetNumOuterLoop);
	    alert("New unique, English tweets retrieved via API: " + tweetNumOuterLoop);
	    needShowTopRetriTweets = "true";
	    getUnLabeledTweets();
	}
    });
    
}