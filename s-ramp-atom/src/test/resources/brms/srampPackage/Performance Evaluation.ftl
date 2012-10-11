<html>
<body>
<h2>Performance Evaluation</h2>
<hr>
<#if task.descriptions[0]??>
Description: ${task.descriptions[0].text}<BR/>
</#if>
Content: ${Content}<BR/>
<form action="complete" method="POST" enctype="multipart/form-data">
performance: <input type="text" name="performance" /><BR/>
<BR/>
<input type="submit" name="outcome" value="Complete"/>
</form>
</body>
</html>