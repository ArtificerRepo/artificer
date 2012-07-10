<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 3.2 Final//EN">
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="org.overlord.sramp.maven.repo.models.DirectoryEntryType"%>
<%@page import="javax.swing.text.StyledEditorKit.ForegroundAction"%>
<%@page import="java.util.List"%>
<%@page import="org.overlord.sramp.maven.repo.models.DirectoryEntry"%>
<%@page import="org.overlord.sramp.maven.repo.models.DirectoryListing"%>
<%
	DirectoryListing directoryListing = (DirectoryListing) request.getAttribute("model");
	List<DirectoryEntry> entries = directoryListing.getEntries();
%>
<html>
	<head>
		<title>Index of <%=directoryListing.getMavenPath()%></title>
	</head>
<body>
	<h1>Index of <%=directoryListing.getMavenPath()%></h1>
	<table>
		<tr>
			<th>Type</th>
			<th>Name</th>
			<th>Last Modified</th>
			<th>Size</th>
			<th>Description</th>
		</tr>
		<tr>
			<th colspan="5"><hr></th>
		</tr>
		<%
			SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
			for (DirectoryEntry entry : entries) {
				String type = "FILE";
				String href = directoryListing.getUrlPath() + entry.getName();
				String label = entry.getName();
				if (entry.getType() == DirectoryEntryType.directory) {
					type = "DIR";
					href += "/";
					label += "/";
				}
				String formattedDate = format.format(entry.getLastModified());
				String size = "-";
				String description = entry.getDescription();
		%>
		<tr>
			<td valign="top">[<%= type %>]</td>
			<td><a href="<%= href %>"><%= label %></a></td>
			<td align="right"><%= formattedDate %></td>
			<td align="right"><%= size %></td>
			<td align="right"><%= description %></td>
		</tr>
		<%  }  %>
		<tr>
			<th colspan="5"><hr></th>
		</tr>
	</table>
</body>
</html>