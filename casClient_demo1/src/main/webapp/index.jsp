<%--
  Created by IntelliJ IDEA.
  User: hudongfei
  Date: 2019/1/9
  Time: 21:33
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Demo1</title>
</head>
<body>
    cas1-user:
    <%= request.getRemoteUser()%>
    <a href="http://localhost:9100/cas/logout">登出</a>
</body>
</html>
