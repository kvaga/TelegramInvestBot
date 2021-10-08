<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@page import="
	ru.kvaga.investments.lib.InstrumentsTrackingLib,
    org.apache.logging.log4j.*,
    ru.kvaga.investments.Instrument,
    ru.kvaga.investments.stocks.StockItem
    "%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />

<title>Stocks</title>
</head>
<body>
<jsp:include page="Header.jsp"></jsp:include>
<h3>Stocks</h3>
<jsp:include page="CommonInfoOfInstruments.jsp">
	<jsp:param name="instrument" value="stocks" />
</jsp:include>

</table>
</body>
</html>