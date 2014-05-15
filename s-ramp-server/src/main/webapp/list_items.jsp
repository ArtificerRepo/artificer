<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@page session="false"%>
<html>
<head>
<title>Index of ${relativePath}</title>
</head>
<body bgcolor="white">

	<h1>Index of ./${relativePath}</h1>
	<hr>
	<pre>
	<c:if test="${not empty  parentPath}">
		<c:url value='/maven/repository${parentPath}' var="urlBack" scope="page"></c:url>
		<a href="${urlBack}">../</a>
	</c:if>
		<c:set var="separator" value=""></c:set>
		<c:if test="${fn:endsWith(relativePath, '/')==false && relativePath!=''}">
			<c:set var="separator" value="/"></c:set>
		</c:if>
		
	<c:forEach items="${items}" var="item"> 	
		<c:url value='/maven/repository/${relativePath}${separator}${item}' var="urlItem" scope="page"></c:url>
		<a href="${urlItem}">${item}</a>
	</c:forEach>
	</pre>
	<hr>
</body>
</html>