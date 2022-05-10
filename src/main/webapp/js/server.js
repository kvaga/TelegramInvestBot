/**
 * 
 */
 
 function loadingStart(){
	document.getElementById('loading').innerHTML='<p>Loading...</p>';
	console.info('Loading started...'+document.getElementById('loading'));
}

function loadingStop(){
	document.getElementById('loading').innerHTML='';
	console.info('Loading finished');
}

function exception(text){
	document.getElementById('exception').innerHTML='<p><font color="red">Exception: ' + text + '</font></p>';
	loadingStop();
	console.error('Exception: ' + text);
}