<%@ page import="com.force5solutions.demo.Person" contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>Face Enrollment Form</title>
</head>

<body>
<h2>Face Enrollment Form</h2>
<g:uploadForm name="enrollmentUpload" action="enrollFromLoadedImage">
    <input type="file" name="faceImage"/><br>
    <g:select name="personId" from="${Person.list()}" noSelection="['':'Select an Identity']" optionValue="fullName" optionKey="id"/><br>
    <g:submitButton name="Submit"/>
</g:uploadForm>
<g:if test="${statusMessage}">
    <p>${statusMessage}</p>
</g:if>
</body>
</html>