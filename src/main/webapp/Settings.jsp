<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<jsp:include page="Header.jsp"></jsp:include>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>Settings</title>
<style type="text/css">
	table, th, td {
	    border: 1px solid black;
	}
	th {
	    cursor: pointer;
	}
</style>
<script>
try{
	loadingStart();
    var xhr1 = new XMLHttpRequest();
    xhr1.onreadystatechange = function() {
        if (xhr1.readyState == 4) {
        	loadingStop();
            const dataObj = JSON.parse(xhr1.responseText);
           	if(dataObj.message){
        	   exception(dataObj.message);
           	}else{
            	fulfillTable(dataObj);
           	}
        }
    }

    xhr1.open('GET', '${pageContext.request.contextPath}/Settings?command=getSettings', true);
    xhr1.send(null);
}catch(err){
	exception(err.message);
}finally{
	loadingStop();
}

//this function appends the json data to the table 'gable'
function fulfillTable(dataObj){
	var table = document.getElementById('table');
 	console.log(dataObj);
 	var tr = document.createElement('tr');
	table.appendChild(tr);
	tr.innerHTML = '<td>telegramNotificationsTopCountForSending</td>'+
					'<td><input type="text" id="telegramNotificationsTopCountForSending" name="telegramNotificationsTopCountForSending" minlength="1" maxlength="1" size="1" value="'+dataObj.telegramNotificationsTopCountForSending+'"/></td>';
	/*
	 for(var i=0; i<dataObj.length;i++){            
	 	var tr = document.createElement('tr');
		tr.innerHTML = 
			'<td>' + '<a href="${pageContext.request.contextPath}/showFeed?feedId='+dataObj[i].feedId+'">' + dataObj[i].name + '</a>' + '</td>' +
	    	'<td>' + '<a href="${pageContext.request.contextPath}/deleteFeed?redirectTo=/CompositeFeedsListShort.jsp&feedId='+dataObj[i].feedId+'">Delete' + '</a>'+'</td>' +
	    	'<td>' + '<a href="${pageContext.request.contextPath}/mergeRSS.jsp?feedTitle='+fixedEncodeURIComponent(dataObj[i].name)+'&feedId='+dataObj[i].feedId+'"">Edit' + '</a>'+'</td>' +
	    	'<td>' + '<a href="${pageContext.request.contextPath}/moveFeedsFromOneCompositeToAnother.jsp?compositeFeedTitle='+fixedEncodeURIComponent(dataObj[i].name)+'&compositeFeedId='+dataObj[i].feedId+'"">Move' + '</a>'+'</td>' +		
	    	'<td>' + dataObj[i].feedIds.length + '</td>'+
	    	
	    	
	    	'<td>' + dataObj[i].countOfItems + '</td>' + 
	    	'<td>' + dataObj[i].sizeMb + '</td>' +
	    	'<td>' + dataObj[i].newestPubDate + '</td>' +
	    	'<td>' + dataObj[i].oldestPubDate + '</td>' +
	    	
	    	'<td>' + dataObj[i].lastUpdated + '</td>' +
	    	'<td>' + dataObj[i].lastUpdateStatus + '</td>' 
	    	
	    	
	    	;
	    	table.appendChild(tr);
	 }
	*/
	 //sortTable(1);
 
}
function submit(){
	 const form = document.getElementById('subscribe');
	 console.log(document.settingsForm);

}
</script>
</head>
<body>
<form action="Settings" id="settingsForm">
	<table id="table">
		<tr>
			<th onclick="sortTable(1)"><span class="glyphicon glyphicon-sort"></span>&nbsp&nbspSetting</th>
			<th onclick="sortTable(2)"><span class="glyphicon glyphicon-sort"></span>&nbsp&nbspValue</th>	
	    </tr>
</table>
		<input type="hidden" name="command" value="savesettings"></input>
	    <input type="submit"></input>
</form>

</body>
</html>