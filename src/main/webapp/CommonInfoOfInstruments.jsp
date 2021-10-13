<?xml version="1.0" encoding="UTF-8" ?>
<%@page import="ru.kvaga.telegrambot.web.util.ServerUtils"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@page import="ru.kvaga.investments.lib.InstrumentsTrackingLib,
    org.apache.logging.log4j.*,
    ru.kvaga.investments.Instrument,
    ru.kvaga.investments.stocks.StockItem,
    ru.kvaga.investments.bonds.Bond,
    ru.kvaga.investments.etfs.Etf,
    telegrambot.ConfigMap
    "
%>
    
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<script src="sort_table.js"></script> 
<style type="text/css">
	table, th, td {
	    border: 1px solid black;
	}
	th {
	    cursor: pointer;
	}
</style>
<title>Common Information Of Instruments</title>
</head>
<body>

<table id="table" border="1">
<th onclick="sortTable(1)">Name</th><th onclick="sortTable(2)">FullName</th><th>TraceablePrice</th><th>Last Price</th><th onclick="sortTable(4)">Div. % From Traceable Price</th><th onclick="sortTable(5)">Last Updated</th>
<% 
//log.debug("Sorting "+label+"s...");
//Collections.sort(stockItemsForPrinting, new StockItemForPrintingComparatorByPercentFromTrackingPrice());
String instrument = request.getParameter("instrument");
if(instrument!=null && instrument.equals("stocks")){
	for (Instrument si : InstrumentsTrackingLib.getListOfInstruments(new StockItem())){
		out.write("<tr>");
		out.write("<td><a href=\""+String.format(ConfigMap.TEMPLATE_URL_TINKOFF_STOCKS,si.getName())+"\">"+si.getName()+"</a></td>");
		out.write("<td>"+si.getFullName()+"</td>");
		out.write("<td>"+si.getTraceablePrice()+"</td>");
		out.write("<td>"+si.getLastPrice()+"</td>");
		out.write("<td>"+(si.getLastPrice()/si.getTraceablePrice()*100-100)+"</td>");
		out.write("<td>"+si.getLastUpdated()+"</td>");
		out.write("</tr>");
	}
}else if(instrument!=null && instrument.equals("bonds")){
	for (Instrument si : InstrumentsTrackingLib.getListOfInstruments(new Bond())){
		out.write("<tr>");
		out.write("<td><a href=\""+String.format(ConfigMap.TEMPLATE_URL_TINKOFF_BONDS,si.getName())+"\">"+si.getName()+"</a></td>");
		out.write("<td>"+si.getFullName()+"</td>");
		out.write("<td>"+si.getTraceablePrice()+"</td>");
		out.write("<td>"+si.getLastPrice()+"</td>");
		out.write("<td>"+(si.getLastPrice()/si.getTraceablePrice()*100-100)+"</td>");
		out.write("<td>"+si.getLastUpdated()+"</td>");
		out.write("</tr>");
	}
}else if(instrument!=null && instrument.equals("etfs")){
	for (Instrument si : InstrumentsTrackingLib.getListOfInstruments(new Etf())){
		out.write("<tr>");
		out.write("<td><a href=\""+String.format(ConfigMap.TEMPLATE_URL_TINKOFF_ETFS,si.getName())+"\">"+si.getName()+"</a></td>");
		out.write("<td>"+si.getFullName()+"</td>");
		out.write("<td>"+si.getTraceablePrice()+"</td>");
		out.write("<td>"+si.getLastPrice()+"</td>");
		out.write("<td>"+(si.getLastPrice()/si.getTraceablePrice()*100-100)+"</td>");
		out.write("<td>"+si.getLastUpdated()+"</td>");
		out.write("</tr>");
	}
}else{
	out.write(ServerUtils.getHTMLFailText("Unknown value ["+instrument+"] of parameter 'instrument'"));
}

%>
</table>
</body>
</html>