<%@ page import="com.force5solutions.demo.Person" contentType="text/html;charset=UTF-8" %>
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
<script src="https://github.com/justadudewhohacks/face-api.js/raw/master/dist/face-api.js"></script>

<canvas id="identityImage" style="width: 1920px; height: 1080px;" />

<g:form name="enrollForm" action="save" params="${params}" method="post">
    <div class="row">
        <div id="webcam" class="span4 offset4">
            <h1 id="countdown" style="display: none;"></h1>

            <div class="video-crop">
                <video autoplay width="1920" height="1080"></video>
            </div>
        </div>
    </div>
</g:form>
<g:if test="${statusMessage}">
    <p>${statusMessage}</p>
</g:if>

<g:javascript src="takePicture.js"/>
<g:javascript src="face-api.js" />
<script type="text/javascript">

    function takePicture() {
        var pictureWidth = $('#identityImage').width();
        var pictureHeight = $('#identityImage').height();

        snapshot(false, pictureWidth, pictureHeight );
        //console.log(identityImage)
        // $('#identityImage').show();
    }

    function uploadImage() {
        $.ajax({
            type:"POST",
            url:"${createLink(controller: 'detection', action: 'findIdentityPost')}",
            data:{faceImage:identityImage, isBase64: 'true'},
            success:function () {
                console.log("Image sent.");
            }
        });
    }

    const MODEL_URL = "https://raw.githubusercontent.com/justadudewhohacks/face-api.js/master/weights"


    $( document ).ready(async function() {
        await faceapi.loadSsdMobilenetv1Model(MODEL_URL);
        await faceapi.loadFaceLandmarkModel(MODEL_URL);
        await faceapi.loadFaceRecognitionModel(MODEL_URL);
        await monitor();
    });


    async function facePresent() {

        const img = document.getElementById('identityImage')
        let fullFaceDescriptions = await faceapi.detectAllFaces(img).withFaceLandmarks().withFaceDescriptors()
        if (fullFaceDescriptions.length > 0) {
            console.log("Found a face.")
            uploadImage();
        } else {
            console.log("No face.");
        }
    }

    async function monitor() {
        takePicture();
        const response = await facePresent();
        setTimeout(function() {
            const response2 = monitor();
        }, 3000);
    }
</script>
