

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<link rel="stylesheet" type="text/css" href="js/library/style.css">
	<script src="js/library/jquery-1.6.4.js"></script>
	<script src="js/library/jquery.tablesorter.js"></script>
	<script src="js/library/jquery.tablesorter.pager.js"></script>
	<script src="js/getQueryTweets.js"></script>
	<script src="js/draw.js"></script>
	<script src="js/data.js"></script>
	<script src="js/allTweetsInfo.js"></script>
	<script src="js/globalVar.js"></script>
        <title>All tweets ranking</title>
    </head>
    <body>
	<div id="infoContainer" style="margin: 0 auto; position: absolute; height: 4%; width: 84%; left: 2%; top: 1%;">
	    <div id="queryText" style="margin: 0 auto; width: 95%; left: 2%; "></div>
	    <div id="classiferStatsDiv" style="margin: 0 auto; width: 95%; left: 2%; "></div>
	</div>

	<div id="tableContainerAll" style="margin: 0 auto; position: absolute; width: 95%; left: 0%; top: 14%; height: 90%; ">
	    <table id="one-column-emphasis-all"  class="one_column_emphasis" width="95%"></table>
	</div>
	<script type="text/javascript">
	    $(function(){
		getParas();
	    });

	</script>
    </body>
</html>
