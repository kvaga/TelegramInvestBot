<%@taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>

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

</head>
<body>
<b style='color:red;'>${requestScope.exception}</b>

<form action="Settings" id="settingsForm">
	<table id="table">
		<tr>
			<th onclick="sortTable(1)"><span class="glyphicon glyphicon-sort"></span>&nbsp&nbspSetting</th>
			<th colspan="2" onclick="sortTable(2)"><span class="glyphicon glyphicon-sort"></span>&nbsp&nbspValue</th>	
	    </tr>
	    <tr>
	    	<td>Telegram Notifications <br/>Top Count For Sending</td>
	    	<td colspan="2">
	    		<input type="text" size="6" name="telegramNotificationsTopCountForSending" value="<c:out value="${requestScope.resp.telegramNotificationsTopCountForSending}"></c:out>"></input>	
	    	</td>
	    </tr>
	    <tr>
	    	<td>Working Days</td>
	    	<td colspan="2">
	    		<c:forEach var="workingDay" items="${requestScope.resp.workingDays}">
	    			<input type="checkbox" name="${workingDay.name}" <c:if test = "${workingDay.workingDayBol == 'true' }">checked</c:if> />${workingDay.name}<br/>
			    </c:forEach>
	    	</td>
	    </tr>
	    <tr>
	    	<td>Working Hours</td>
	    	<td>From<br/>To</td>
	    	<td><input type="text" name="hoursFrom" size="2" value="${requestScope.resp.workingHours.hoursFrom}"/>:<input type="text" name="minsFrom" size="2" value="${requestScope.resp.workingHours.minsFrom}"/><br/><input name="hoursTo" size="2" type="text" value="${requestScope.resp.workingHours.hoursTo}"/>:<input type="text" name="minsTo" size="2" value="${requestScope.resp.workingHours.minsTo}"/></td>
	    </tr>
</table>


		<input type="hidden" name="command" value="savesettings"/>
		<input type="hidden" name="redirectTo" value="SettingsNew.jsp?command=getsettings"/>
	    <input type="submit"/>
</form>

</body>
</html>