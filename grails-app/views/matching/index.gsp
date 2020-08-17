<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>Facial Recognition Matching</title>
</head>

<body>
<h2>Facial Recognition Matching</h2>
<g:uploadForm name="findIdentity" action="findIdentity">
    <input type="file" name="faceImage"/><br>
    <g:submitButton name="Submit"/>
</g:uploadForm>
<g:if test="${statusMessage}">
    <p>${statusMessage}</p>
</g:if>
</body>
</html>