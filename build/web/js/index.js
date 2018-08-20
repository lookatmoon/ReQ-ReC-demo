
$(function(){
	
	$(".labeler").click(function(){
		
    	var index = $(this).parent().index();
    	var sections =  $(this).parents("ul").find("li");
        for (var i = 0; i < sections.length; i++) {
        	   var ob = sections.eq(i).removeClass("active");
        	   //var scheme = ob.find("a").attr("class");
                   
        	    $(".labeler_"+i+"_content").hide();//'fast', function() {
        	}
                
        $(".labeler_"+index+"_content").fadeIn();//"fast",function(){});
        $(this).parent("li").addClass("active");
	});
	
	
	
	});