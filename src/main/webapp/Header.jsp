<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>TelegramBot</title>

<style type="text/css">
	.div-table {
	  display:  table;
        width:auto;
        background-color:#eee;
        border:1px solid  #666666;
        border-spacing:5px;/*cellspacing:poor IE support for  this*/
       /* border-collapse:separate;*/
	}
	.div-table-row {
		display:table-row;
		width:auto;
	}
	.div-table-row {
		display:table-row;
		width:auto;
	}
	.div-table-col {
		float:left;/*fix for  buggy browsers*/
        display:table-column;
        width:200px;
        background-color:#ccc;
	}
</style>

</head>
<body>
	<div id="exception"></div>
	<div id="loading"></div>
	<script src="js/server.js"></script>
<h3>Telegram Bot</h3>
<div class="div-table">
	<div class="div-table-head-row">
		<div class="div-table-col">Instruments</div>
		<div class="div-table-col">Settings</div>
	</div>
	<div class="div-table-row">
		<div class="div-table-col">
			<div class="div-table-col"><a href="Stocks.jsp">Stocks</a></div>
			<div class="div-table-col"><a href="Bonds">Bonds</a></div>
			<div class="div-table-col"><a href="Etfs.jsp">Etfs.jsp</a></div>
		</div>
		<div class="div-table-col"><a href="Settings.jsp">Settings.jsp</a></div>
	</div>
</div>

<hr />
</body>
</html>