//query result
var tweetIDs;
var tweetArray;
var tweetLabels;
var queryName;

//confident tweets
var confidTweetArray = [];
var confidTweetIDs = [];
var confidTweetLabels = [];

var labeledTweetIDs = [];
var labeledTweetArray = [];
var labeledTweetLabels = [];

var urlPattern = "(https?://[a-zA-Z0-9_\\-\\./]+[a-zA-Z0-9_\\-/])";
var urlRegex = new RegExp(urlPattern,"ig");

var posWords = {};
var negWords = {};
var regexNonWords = new RegExp('[^a-z0-9@#]', 'g');

var returnUncertainLabels = "";

var needShowTopRetriTweets = "false";
