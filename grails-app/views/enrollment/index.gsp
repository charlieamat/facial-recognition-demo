<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>Enroll</title>
</head>

<body>
<h2>Face Enrollment Form</h2>
<g:uploadForm name="enrollmentUpload" action="enroll">
    <input type="file" name="faceImage"/>
    <g:submitButton name="Submit"/>
</g:uploadForm>
</body>
</html>