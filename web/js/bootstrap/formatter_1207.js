function setVisibility(div1,div2) {
    
        document.getElementById(div1).style.display = 'inline';
        
            document.getElementById(div2).style.display = 'none';
        
    
}

function setVisibility1(btn,div) {
    
        if(document.getElementById(btn).innerHTML == 'Hide') {
            document.getElementById(btn).innerHTML = 'Edit query';
            document.getElementById(div).style.display = 'none';

        } else {
            document.getElementById(btn).innerHTML = 'Hide';
            document.getElementById(div).style.display = 'inline';

        }
        
        
    
}


