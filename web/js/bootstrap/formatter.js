function setVisibility1() {
    
            if(document.getElementById('btn1').innerHTML == 'Hide') {
                document.getElementById('div1').style.display = 'none';
                document.getElementById('btn1').innerHTML = 'Advanced';
            } else {
                
                document.getElementById('div1').style.display = 'inline';
                document.getElementById('btn1').innerHTML = 'Hide';
                if(document.getElementById('btn2').innerHTML == 'Hide') {
                    document.getElementById('div2').style.display = 'none';
                    document.getElementById('btn2').innerHTML = 'History';
                }
            }
        } 
        

function setVisibility2(){
    
            if(document.getElementById('btn2').innerHTML == 'Hide') {
                document.getElementById('div2').style.display = 'none';
                document.getElementById('btn2').innerHTML = 'History';
            } else {
                
                document.getElementById('div2').style.display = 'inline';
                document.getElementById('btn2').innerHTML = 'Hide';
                if(document.getElementById('btn1').innerHTML == 'Hide') {
                    document.getElementById('div1').style.display = 'none';
                    document.getElementById('btn1').innerHTML = 'Advanced';
                }
            }
          
}

function setVisibility3(btn,div) {
    
        if(document.getElementById(btn).innerHTML == 'Hide') {
            document.getElementById(btn).innerHTML = 'Edit query';
            document.getElementById(div).style.display = 'none';

        } else {
            document.getElementById(btn).innerHTML = 'Hide';
            document.getElementById(div).style.display = 'inline';

        }
        
        
    
}


