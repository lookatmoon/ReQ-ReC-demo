function getParas() {
    var vars = window.location.href.slice(window.location.href.indexOf('?') + 1);
    //    $("#queryID").text(vars);
    $.ajax({
	type: "GET",
	url: "SendRecTweets",
	data: "type=getAllTweetInfo&"+vars,
	success: function(tweets) {
	    //	    console.log(tweets);
	    splitInfo(tweets);
	    
	}
    });   
}

function splitInfo(data){
    var allTextLines = data.split('\n\n');
    
    if(allTextLines[0] == "NoTrainFile"){
	alert("No training data found. Plase label some data first.");
	return;
    }
	    
    processAllTweets(allTextLines[0]);

    if(allTextLines[1]!=null && allTextLines[1].length!=0){
	$("#classiferStatsDiv").html(allTextLines[1] + "<br>"+
	    "<span style='BACKGROUND-COLOR: rgba(135,206,235,0.5)'>Blue background</span>: predicted as positive.<br>"+
	    " <span style='BACKGROUND-COLOR: rgba(255,222,173,0.5)'>Yellow background</span>: predicted as negative. <br>"+
	    "Click table headers to sort.");
    }
    
    if(allTextLines[2]!=null && allTextLines[2].length!=0){
	$("#queryText").text(allTextLines[2]);
    }

    if(allTextLines[3]!=null && allTextLines[3].length!=0){
	processWordsHighlight(allTextLines[3], allTextLines[4]);
    }else{
	posWords = {};
	negWords = {};
    }
    
    drawSortTable(allTweetArray, allTweetLabels, allTweetConfidence, "one-column-emphasis-all", 1);
}

var posWords = {};
var negWords = {};

var allTweetIDs = [];
var allTweetArray = [];
var allTweetLabels = [];
var allTweetConfidence = [];

function processAllTweets(tweets) {

    var allTextLines = tweets.split(/\r\n|\n/);
    var rowNum = allTextLines.length;
    if(allTextLines[rowNum-1] == null || allTextLines[rowNum-1].length == 0){
	rowNum = rowNum - 1;
    }
    
    allTweetIDs = [];
    allTweetArray = [];
    allTweetLabels = [];
    allTweetConfidence = [];
    
    var i = 0;
    var j = 0;
    for(i=0; i<rowNum; i++){

	var entries = allTextLines[i].split('\t');
	allTweetLabels.push(entries[0]);
	allTweetIDs.push(entries[1]);
	allTweetConfidence.push(+entries[2]);
	allTweetArray.push(entries[3]);
    }
    //    console.log(tweetIDs);
    //    console.log(tweetArray);
    tweets = null;
    allTextLines = null;
}


function drawSortTable(tweetArray, tweetLabels, tweetConfidence, tableDivID, startIndex){
    
    $('#' + tableDivID).append('<thead><tr style="background-color: rgba(144,238,144,0.5);"><th>Label</th><th>Confidence</th><th>Tweet</th></tr></thead>');
    $('#' + tableDivID).append('<tbody  width="95%"></tbody>');
    var i = 1;
    for(i=0; i<tweetArray.length; i++){
	if(tweetLabels[i] == "1"){ //positive
	    var color = "rgba(135,206,235,0.5)";
	}else{
	    color = "rgba(255,222,173,0.5)";
	}

	$('#' + tableDivID + ' tbody').append('<tr style="background-color:'+color+';">'
	    +'<td width="5%"><font size="3">'+tweetLabels[i]+'</font></td>'
	    +'<td width="17%"><font size="3">'+tweetConfidence[i]+'</font></td>'
	    +'<td><font size="3">'+tweetHighLight(tweetArray[i], startIndex)+'</font></td></tr>');

    }
    
    $('#' + tableDivID).tablesorter(); 
}