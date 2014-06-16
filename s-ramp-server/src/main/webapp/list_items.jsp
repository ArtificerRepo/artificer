<%@taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@page session="false"%>
<html>
<head>
<title>Index of ${relativePath}</title>
</head>
<body bgcolor="white">
    <h1>Index of ./${relativePath}</h1>
    <hr>
    <table width="100%" border="0" cellpadding="2" cellspacing="2">
    <c:if test="${not empty  parentPath}">
        <c:url value='/maven/repository${parentPath}' var="urlBack" scope="page"></c:url>
        <tr><td><a href="${urlBack}">../</a></td></tr>
    </c:if>
    <c:forEach items="${items}" var="item">
        <c:url value='/maven/repository/${relativePath}${item}' var="urlItem" scope="page"></c:url>
        <tr><td><a href="${urlItem}">${item}</a></td></tr>
    </c:forEach>
    </table>
    <hr>
</body>
</html>