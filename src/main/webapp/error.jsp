<%@ page isErrorPage="true" %>
<html>
<head>
<title>Show Error</title>
</head>
<body>
<h3>Exception was occured</h3>
<p>Exception stack trace:<% exception.printStackTrace(response.getWriter()); %>
</p>
</body>
</html>
