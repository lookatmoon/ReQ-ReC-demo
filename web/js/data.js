function processLabeledTweets(tweets) {

    var allTextLines = tweets.split(/\r\n|\n/);
    var rowNum = allTextLines.length;
    if(allTextLines[rowNum-1] == null || allTextLines[rowNum-1].length == 0){
	rowNum = rowNum - 1;
    }
    
    labeledTweetIDs = new Array(rowNum);
    labeledTweetArray = new Array(rowNum);
    labeledTweetLabels = new Array(rowNum);
    
    var i = 0;
    var j = 0;
    for(i=0; i<rowNum; i++){

	var entries = allTextLines[i].split('\t');
	labeledTweetLabels[i] = entries[0];
	labeledTweetIDs[i] = entries[1];
	labeledTweetArray[i] = entries[2];
    }
    

    //        console.log(labeledTweetLabels);
    //        console.log(labeledTweetIDs);
    //	console.log(labeledTweetArray);
    tweets = null;
    allTextLines = null;
}

//uncertain and confident tweets by SVM
function processTweets(tweets) {

    var allTextLines = tweets.split('\n\n');
    processUncertainTweets(allTextLines[0]);

    if(allTextLines[2]!=null && allTextLines[2].length!=0){
	$("#classiferStatsDiv").text("Tweets the classifier are confident in. " + allTextLines[2]);
    }
    
    if(allTextLines[3]!=null && allTextLines[3].length!=0){
	$("#newQueryText").html(allTextLines[3] + "<br><br>" + allTextLines[4]);
    }
    
    if(allTextLines[5]!=null && allTextLines[5].length!=0){
	processWordsHighlight(allTextLines[5], allTextLines[6]);
    }else{
	posWords = {};
	negWords = {};
    }

    
    if(allTextLines[7]!=null && allTextLines[7].length!=0){
//	console.log(allTextLines[7] + "\t" + allTextLines[8]);
	$("#newQueryToSubmitInput").val(allTextLines[7]);
	$("#newQueryIgnoreText").text(allTextLines[8]);
    }


    if(allTextLines[1]!=null && allTextLines[1].length!=0){
	processConfidentTweets(allTextLines[1]);
	$("#bottonRightDiv").show();
	if(confidTweetArray.length != 0){
	    drawTable(confidTweetArray, confidTweetLabels, "one-column-emphasis-rightBottom", "checkboxConfidentData", 1);
	}
	
    }
    
}

function processWordsHighlight(posString, negString){
//    console.log(posString);
//    console.log(negString);
    var allTextLines = posString.split(" ");
    var rowNum = allTextLines.length;
    if(allTextLines[rowNum-1] == null || allTextLines[rowNum-1].length == 0){
	rowNum = rowNum - 1;
    }
    
    posWords = {};
    var i = 0;
    for(i=0; i<rowNum; i++){
	posWords[allTextLines[i]] = 1;
    }
    
    allTextLines = negString.split(" ");
    rowNum = allTextLines.length;
    if(allTextLines[rowNum-1] == null || allTextLines[rowNum-1].length == 0){
	rowNum = rowNum - 1;
    }
    
    negWords = {};
    for(i=0; i<rowNum; i++){
	negWords[allTextLines[i]] = 1;
    }  
    
//    console.log(posWords);
//    console.log(negWords);
}

function processUncertainTweets(tweets) {

    var allTextLines = tweets.split(/\r\n|\n/);
    var rowNum = allTextLines.length;
    if(allTextLines[rowNum-1] == null || allTextLines[rowNum-1].length == 0){
	rowNum = rowNum - 1;
    }
    
    returnUncertainLabels = "";
    tweetIDs = [];
    tweetArray = [];
    tweetLabels = [];
    
    var i = 0;
    var j = 0;
    for(i=0; i<rowNum; i++){

	var entries = allTextLines[i].split('\t');
	var label = +entries[0];
	if(label > 1){
	    label = label - 3;
	    returnUncertainLabels += label + "_" + entries[1] + "__";
	}else{
	    tweetLabels.push(entries[0]);
	    tweetIDs.push(entries[1]);
	    tweetArray.push(entries[2]);
	}
    }
    
    if(tweetArray.length == 0){
	$("#one-column-emphasis").text('No tweets shown means all tweets have been previously labeled. Click "Submit" to continue.');
    }
    

    //    console.log(tweetIDs);
    //    console.log(tweetArray);
    tweets = null;
    allTextLines = null;
}

function processConfidentTweets(tweets) {

    var allTextLines = tweets.split(/\r\n|\n/);
    var rowNum = allTextLines.length;
    if(allTextLines[rowNum-1] == null || allTextLines[rowNum-1].length == 0){
	rowNum = rowNum - 1;
    }
    
    confidTweetIDs = new Array(rowNum);
    confidTweetArray = new Array(rowNum);
    confidTweetLabels = new Array(rowNum);
    
    var i = 0;
    var j = 0;
    for(i=0; i<rowNum; i++){

	var entries = allTextLines[i].split('\t');
	confidTweetLabels[i] = entries[0];
	confidTweetIDs[i] = entries[1];
	confidTweetArray[i] = entries[2];
    }
    

    //        console.log(confidTweetIDs);
    //        console.log(confidTweetArray);
    tweets = null;
    allTextLines = null;
}

