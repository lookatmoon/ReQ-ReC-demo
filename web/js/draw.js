

function drawTable(tweetArray, tweetLabels, tableDivID, checkBoxID, startIndex){
    
    //    $('#one-column-emphasis').append('<thead><tr><th>Dimension</th><th>Tweet</th></tr></thead>');
    $('#' + tableDivID).append('<tbody  width="95%"></tbody>');
    var i = 0;
    for(i=0; i<tweetArray.length; i++){
	if(i%2 == 0){ //positive
	    var color = "rgba(135,206,235,0.5)";
	}else{
	    color = "rgba(255,222,173,0.5)";
	}
	
	if(tweetLabels[i] == -1){
	    var check = null;
	}else{
	    check = 'checked="checked"';
	}
	$('#' + tableDivID + ' tbody').append('<tr style="background-color:'+color+';"><td>'+tweetHighLight(tweetArray[i], startIndex)+'</td><td width="12%"><input type="checkbox" name="option' + i + '" id="' + checkBoxID + i + '" style="width:35px; height:35px;" ' + check + ' ></td></tr>');
	$('#' + checkBoxID + i).click(function() {
		
	    $(this).parent().parent().css("background-color", "rgba(255,204,204,0.5)");
		
	});
    }
    
}

function tweetHighLight(tweet, startIndex){
    if(tweet == null || tweet.length == 0){
	return;
    }
    //    var matches = tweet.match(/(.*)(?=\[\d+\])/);
    var tweetWords = tweet.split(" ");
    tweet = highlightWords(tweetWords, startIndex);
    tweet = httpClickable(tweet);
    return tweet;
}

function highlightWords(tweetWords, startIndex){
    var posColor = "#99CCFF";
    var negColor = "#99CC00";
    var posBigram = "<del style='color:black;'>"; //underscore
    var negBigram = "<del style='color:red;'>"; //cross out words
    var posBigramEnd = "</del>"; //underscore
    var negBigramEnd = "</del>"; //cross out words
    
    var i=0;
    var tweet = "";
    var tweetLowerWords = new Array(tweetWords.length);
    var tweetWordColors = new Array(tweetWords.length);
    var tweetWordBigramShapes = new Array(tweetWords.length);
    var tweetWordBigramShapeEnds = new Array(tweetWords.length);
    
    for(i=0; i<tweetWords.length; i++){
	tweetLowerWords[i] = tweetWords[i].toLowerCase().replace(regexNonWords, '');
	tweetWordBigramShapes[i] = "";
	tweetWordBigramShapeEnds[i] = "";
	tweetWordColors[i] = "";
    }
    
    
    for(i=startIndex; i<tweetLowerWords.length; i++){
	if(tweetLowerWords[i] in posWords){
	    tweetWordColors[i] = posColor;
	}else if(tweetLowerWords[i] in negWords){
	    tweetWordColors[i] = negColor;
	}
    }
    
    var start = "_"+tweetWordColors[startIndex];
    if(start in posWords){
	tweetWordBigramShapes[startIndex] = posBigram;
    }else if(start in negWords){
	tweetWordBigramShapes[startIndex] = negBigram;
    }
    
    for(i=startIndex+1; i<tweetLowerWords.length; i++){
	var bigram = tweetLowerWords[i-1] + "_" + tweetLowerWords[i];
	if(bigram in posWords){
	    tweetWordBigramShapes[i-1] = posBigram;
	    tweetWordBigramShapes[i] = posBigram;
	    tweetWordBigramShapeEnds[i-1] = posBigramEnd;
	    tweetWordBigramShapeEnds[i] = posBigramEnd;
	    
	}else if(bigram in negWords){
	    tweetWordBigramShapes[i-1] = negBigram;
	    tweetWordBigramShapes[i] = negBigram;
	    tweetWordBigramShapeEnds[i-1] = negBigramEnd;
	    tweetWordBigramShapeEnds[i] = negBigramEnd;
	}
    }    

    for(i=0; i<tweetWords.length; i++){

	if(tweetWordColors[i] != ""){
	    tweet += "<SPAN style='BACKGROUND-COLOR: " + tweetWordColors[i] + "'>" + tweetWordBigramShapes[i] + tweetWords[i] + tweetWordBigramShapeEnds[i] + "</SPAN> ";
	    
	}else{
	    tweet += tweetWordBigramShapes[i] + tweetWords[i] + tweetWordBigramShapeEnds[i] + " ";
	}
    }


    return tweet;   
}

function httpClickable(tweet){
    tweet = tweet.replace(urlRegex, '<a href="$1">$1</a>');
    
    return tweet;
}

