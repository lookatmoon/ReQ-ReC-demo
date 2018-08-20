<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<link rel="stylesheet" type="text/css" href="css/bootstrap-responsive.min.css">
        <link rel="stylesheet" type="text/css" href="css/bootstrap.min.css">
	<link rel="stylesheet" type="text/css" href="css/customize.css">

        <title>DoubleLoop Demo</title>
    </head>
    <body>
        
    
        
    <div class="page-header" align="center">
        <h1>High Recall Search Demo</h1>
    </div>
        
        
    
        
        <div class='container mini-layout'>
	<div class='well' id="inputContainer">
            <input id="paraQuery" type="text" value="" style="width: 800px;">
            <button class="btn btn-primary" 
                    id="queryAPIButton" onclick='searchAPI();' type="button">Search
            </button>
            <br>
            Tips: Use 
            <a id='btn1' onclick="setVisibility1();"> Advanced </a>
            or 
            <a id='btn2' onclick="setVisibility2();"> History </a>

            </div>
            </div>
        
        
        
            
            <div id="div1" style="display: none;">

            <div class='container mini-layout'>
            <div class='well' id="inputContainer">
<!--
	    <select id="paraSetSelecter" name="trecQueries">
		<option id="queryNone"> Enter preset queries...</option>
		<option id="MB111">MB111; water shortages; 317711766815653888</option>
		<option id="MB112">MB112; Florida Derby 2013; 318035260900265985</option>
		<option id="MB113">MB113; Kal Penn; 318263699460747265</option>
		<option id="MB114">MB114; Detroit EFM Undemocratic; 316597818372538369</option>
		<option id="MB115">MB115; memories of Mr. Rogers; 317871313932935168</option>
		<option id="MB116">MB116; Chinese Computer Attacks; 307317484577497090</option>
		<option id="MB117">MB117; marshmallow Peeps dioramas; 317174111543648256</option>
		<option id="MB118">MB118; Israel and Turkey reconcile; 316274907300392960</option>
		<option id="MB119">MB119; colony collapse disorder; 317773674709131265</option>
		<option id="MB120">MB120; Argentina's Inflation; 314038001112076290</option>
		<option id="MB121">MB121; Future of MOOCs; 316919697658503169</option>
		<option id="MB122">MB122; unsuccessful kickstarter applicants; 317689549570596865</option>
		<option id="MB123">MB123; solar flare; 316801464427229185</option>
		<option id="MB124">MB124; celebrity DUI; 317890398028701696</option>
		<option id="MB125">MB125; Oscars snub Affleck; 308486017655136256</option>
		<option id="MB126">MB126; Pitbull rapper; 314683038921523200</option>
		<option id="MB127">MB127; Hagel nomination filibustered; 308745854808883200</option>
		<option id="MB128">MB128; Buying clothes online; 318126981940121600</option>
		<option id="MB129">MB129; Angry Birds cartoon; 317844688482754560</option>
		<option id="MB130">MB130; Lawyer jokes; 315208648123101184</option>
		<option id="MB131">MB131; trash the dress; 316757751386759168</option>
		<option id="MB132">MB132; asteroid hits Russia; 304039501779574785</option>
		<option id="MB133">MB133; cruise ship safety; 312663145997033474</option>
		<option id="MB134">MB134; The Middle TV show; 317067639165964288</option>
		<option id="MB135">MB135; Big Dog terminator robot; 317792851100590080</option>
		<option id="MB136">MB136; Gone Girl reviews; 318171076645502976</option>
		<option id="MB137">MB137; cause of the Super Bowl blackout; 298834781561581569</option>
		<option id="MB138">MB138; New York City soda ban blocked; 312146160284471296</option>
		<option id="MB139">MB139; Artists Against Fracking; 315810006450724866</option>
		<option id="MB140">MB140; Richard III burial dispute; 316917705368293376</option>
		<option id="MB141">MB141; Mila Kunis in Oz movie; 317835075158941697</option>
		<option id="MB142">MB142; Iranian weapons to Syria; 315927753138511872</option>
		<option id="MB143">MB143; Maracana Stadium problems; 318019519664758785</option>
		<option id="MB144">MB144; Downton Abbey actor turnover; 309426510643748866</option>
		<option id="MB145">MB145; National Parks sequestered; 314855890383687681</option>
		<option id="MB146">MB146; GMO labeling; 316035668402909185</option>
		<option id="MB147">MB147; Victoria's Secret commercial; 318350475424567296</option>
		<option id="MB148">MB148; Cyprus Bailout Protests; 318425159210004480</option>
		<option id="MB149">MB149; making football safer; 314366285070864385</option>
		<option id="MB150">MB150; UK wine industry; 318380116566749184</option>
		<option id="MB151">MB151; gun advocates are corrupt; 304404028731846657</option>
		<option id="MB152">MB152; Iceland FBI Wikileaks; 309589325128028160</option>
		<option id="MB153">MB153; lighter bail for Pistorius; 317228415197519872</option>
		<option id="MB154">MB154; anti-aging resveratrol; 310079727366967297</option>
		<option id="MB155">MB155; Obama reaction to Syrian chemical weapons; 314487445926334464</option>
		<option id="MB156">MB156; Bush's dog dies; 299689022853365760</option>
		<option id="MB157">MB157; Kardashian maternity style; 318177602995122176</option>
		<option id="MB158">MB158; hush puppies meal; 315632008560775168</option>
		<option id="MB159">MB159; circular economy initiatives; 315763906851250176</option>
		<option id="MB160">MB160; social media as educational tool; 316749983514771457</option>
		<option id="MB161">MB161; 3D printing for science; 311546769064464384</option>
		<option id="MB162">MB162; DPRK Nuclear Test; 309820213183008768</option>
		<option id="MB163">MB163; virtual currencies regulation; 317889374605950976</option>
		<option id="MB164">MB164; Lindsey Vonn sidelined; 318147869565652992</option>
		<option id="MB165">MB165; ACPT Crossword Tournament; 312319657644285952</option>
		<option id="MB166">MB166; Maryland casino table games; 315805174633492481</option>
		<option id="MB167">MB167; sequestration opinions; 316230208585871361</option>
		<option id="MB168">MB168; US behind Chaevez cancer; 313351968166928385</option>
		<option id="MB169">MB169; Honey Boo Boo Girl Scout cookies; 310613284749447168</option>
		<option id="MB170">MB170; Tony Mendez; 318365281321881600</option>
	    </select>
-->
	    <br>
	    Query ID: <input id="paraQueryID" type="text" value="test" ><br>
	    Maximum # of docs: <input id="paraQueryTweetNum" type="text"  value="500" ><br>
	    # of clusters: <input id="paraClusterNum" type="text"  value="2" ><br>
	    # of top docs selected in first retrieval: <input id="paraTopTweetNum" type="text"  value="2"><br>
            <!-- Query doc time:--> <input id="paraQueryTime" type="hidden"  value="234" ><br>
            <div>
		# of docs to be labeled: <input id="queryNumRtn" type="text"  value="10">
	    </div>
	    <div>
		# of predicted docs: <input id="confidentNumRtn" type="text"  value="10">
	    </div>
	
           
                </div>
        </div>
        </div>
        
        <div id="div2" style="display: none;">

            <div class='container mini-layout'>
            <div class='well' id="inputContainer">
                <input id="query" type="text" value="politics" >
    <!--	    <input type="submit" id="queryButton"
                       value="Continue by Query ID" onclick='sendQueryRequest($("#query").val());'>-->
                <button class="btn btn-primary" 
                        id="queryButton" onclick='sendQueryRequest($("#query").val());' 
                        type="button">Continue by Query ID</button>


    <!--	    <input type="submit" id="allTweetsButton" style="position: relative; "
                       value="All unlabeled tweets" onclick='allUnLabeledTweets($("#query").val());'>-->
<!--                <button class="btn btn-primary" 
                        id="allTweetsButton" onclick='allUnLabeledTweets($("#query").val());' 
                        type="button">All unlabeled tweets</button>-->
            </div>      
            </div>
        </div>
        
        <div class="container">
            <div  id="stat" style="display: none">
            <div class='well' id="inputContainer" >
                <div id="labeledStatsDiv" ></div><br>
                <div id="classiferStatsDiv" ></div>            
            </div>
                </div>
        </div>
            
        <!--        Jun's Session-->
      <div class='mini-layout main_body_labeler' style="display: none;">
          <div id='static_btn' class='mini-layout fixed' align='center' style='top:300px;position: fixed;width:150px;'>
        <div class='well' style='width:90px; height:110px'>  
            
            <div class="pull-left" style='margin-bottom: 10px;'><span style='background-color: #99CCFF'>pos unigrams</span></div>
            <div class="pull-left" style='margin-bottom: 10px;'><span style='background-color: #99CC00'>neg unigrams</span></div>
            <div class="pull-left" style='margin-bottom: 10px;'><del style='color:black;'>pos bigrams</del></div>
            <div class="pull-left" style='margin-bottom: 10px;'><del style='color:red;'> neg bigrams</del></div>
            
        </div>
        
    </div>

          

            
          
          
        <div class='container'>
        <div class='span12'>
            <ul class="nav nav-tabs">
               <li class="active">
                   <a class="labeler">Please label</a>
               </li>
               <li><a class="labeler">Your labels</a></li>
               <li><a class="labeler">Predicted labels</a></li>
            </ul>
        </div>
        </div>
       
          
        
        <div style='height:500px;overflow:auto' class='mini-layout-body'>
            
        <div id="tableContainer" class="labeler_0_content container">
            
            <table id="one-column-emphasis" class="one_column_emphasis table table-hover table-bordered"></table>
            <div id="bottonDiv" class="mini-layout pull-right">
                    <button class="btn btn-primary" 
                    id="submitBotton" onclick='sendLabeledTweetsBack();' type="button">Submit</button>
            </div>
        </div>
        
	    <div id="tableContainerRight" class="labeler_1_content container" style="display: none;">
                <table id="one-column-emphasis-right"  class="one_column_emphasis table table-hover table-bordered"></table>
                <div id="bottonDiv" class="mini-layout pull-right">
                        <button class="btn btn-primary" 
                        id="submitBotton" onclick='sendLabeledTweetsBack();' type="button">Submit</button>
                </div>
            </div> 
                
	    <div id="tableContainerRightBottom" class="labeler_2_content container" style="display: none;">
	    <table id="one-column-emphasis-rightBottom"  class="one_column_emphasis table table-hover table-bordered"></table>
	    <div id="bottonRightDiv" style="display: none;" class='mini-layout pull-right'>
                <button class="btn btn-primary" 
                    id="submitConfidentBotton" onclick='sendLabeledConfidentTweetsBack();' 
                    type="button">Submit Confident tweets as well</button>
                </div>
            
            </div>
		<br>
        </div>
          <div class='container mini-layout'>
	<div class='well' id="inputContainer">
            <div>
<!--		<input type="submit" id="stopButton"
		       value="Stop labeling to query API" onclick='re_searchAPI();'>-->
                <button class="btn btn-primary" 
                    id="stopButton" onclick='re_searchAPI();' type="button">Stop labeling and search more</button>
                <a id='btn3' onclick="setVisibility3('btn3','div3');"> Edit query</a>

            </div>
	    <!--<div id="newQueryText"></div>-->
            <div id="div3" style="display: none;">
                <div id="newQueryToSubmitDiv">
                    Query for outer loop: <br>
                    <textarea id="newQueryToSubmitInput" style="width: 600px; height: 80px;"></textarea>
                    <br>
                    Ignored terms in query: <div id="newQueryIgnoreText" ></div>
                </div>
                <div id="tweetNumOuterLoop" ></div>
                <div id="oldQueryDiv" ></div>
                Maximum # of docs: <input id="paraQueryTweetNum2" type="text"  value="1000" ><br>
	    </div>
	</div>      
        </div>
   </div>
	
        <script src="js/jquery.js"></script>
        <script src="js/bootstrap.min.js"></script>
	<script src="js/getQueryTweets.js"></script>
	<script src="js/draw.js"></script>
	<script src="js/data.js"></script>
        <script src="js/index.js"></script>
	<script src="js/globalVar.js"></script> 
        <script src="js/bootstrap/formatter.js"></script>
        <script type="text/javascript">
	    
	    $('#paraSetSelecter').change(function() {
		getSelectedParameters();
	    });
	    
	    $("#query").keyup(function(event) {
		if (event.keyCode==13) {
		    sendQueryRequest($("#query").val());
		}
	    });
	</script>

    </body>
</html>
