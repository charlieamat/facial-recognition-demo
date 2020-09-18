window.URL = window.URL || window.webkitURL;

var identityImage = null;
var video = $('video').get(0);
var canvas = $('canvas').get(0);
var ctx = canvas.getContext('2d');
var constant = .5;

// Legacy support (source: https://developer.mozilla.org/en-US/docs/Web/API/MediaDevices/getUserMedia)
// Old browsers without support for mediaDevices
if (navigator.mediaDevices === undefined) {
    navigator.mediaDevices = {};
}

// Some browsers with only partial support
if (navigator.mediaDevices.getUserMedia === undefined) {
    navigator.mediaDevices.getUserMedia = function(constraints) {

        var getUserMedia = navigator.mediaDevices.webkitGetUserMedia ||
            navigator.mozGetUserMedia || navigator.msGetUserMedia;

        if (!getUserMedia) {
            return Promise.reject(new Error('getUserMedia is not implemented in this browser'));
        }

        // Otherwise, wrap the call to the old navigator.getUserMedia with a Promise
        return new Promise(function(resolve, reject) {
            getUserMedia.call(navigator, constraints, resolve, reject);
        });
    }
}

if (navigator.mediaDevices.getUserMedia) {
    console.log("User media found!");
    navigator.mediaDevices.getUserMedia({video:{mandatory: { maxWidth: 640, maxHeight: 480 }}})
        .then(function (stream) {
            video.srcObject = stream;
        })
        .catch(function(e) {
            $.error('getUserMedia has failed.', e);
        });
}

function snapshot(inPortrait, pWidth, pHeight) {
    if (video.srcObject) {
        ctx.drawImage(video, 0, 0, 1920, 1080, 0, 0, 1920/3, 1080/3);

        identityImage = canvas.toDataURL('image/jpeg');
        identityImage = identityImage.replace('data:image/jpeg;base64,', '');

        constant = 0;
    }
}
