package com.force5solutions.demo

import Luxand.FSDK
import Luxand.FSDKCam
import org.springframework.web.multipart.commons.CommonsMultipartFile
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.*
import java.awt.image.BufferedImage
import com.force5solutions.demo.EnrollmentController
import java.io.FileOutputStream;

class DetectionController {

    static String license = "F3pXg0l9T9uc33EtbkrnzuFktKpcb9fwDdJlQeP+tkK9ofZ7oCmI6vL0Q2BOQMSDNzlWYvVaBqPyY1ZzoEvAcVYVGIyP47GBBe2xcJt63VyHysiZULQ8xAp7KfolGQolISsm/VdXMubw7hDWZiQ9Ui9ntZoiJFOecfPOhjWPHSI="

    private String TrackerMemoryFile = "C:\\Users\\Nate\\Desktop\\tracker71.dat";
    FSDK.HTracker tracker = new FSDK.HTracker();

    private String unknownImagePath = "web-app\\images\\unknown_face.jpg"

    boolean tracking = false

    def index() {
        if (FSDK.ActivateLibrary(license) == FSDK.FSDKE_OK) {
            log.debug("Successfully activated the FSDK library.")

            if (FSDK.Initialize() == FSDK.FSDKE_OK) {
                log.debug("Successfully initialized the FSDK library.")
            } else {
                log.warn("There was an error initializing the FSDK library.")
            }
        } else {
            log.warn("There was an error activating the FSDK library.")
        }
    }

    def findIdentityPost() {
        def tempPhotoLoc = 'snapshot.jpg'
        FileOutputStream imageOutFile = new FileOutputStream(tempPhotoLoc)
        byte[] imageByteArray = Base64.getDecoder().decode(params.faceImage)
        imageOutFile.write(imageByteArray);

        log.debug("Finding an identity by their face.")

        def hImage = new FSDK.HImage()
        def loadStatus = FSDK.LoadImageFromJpegBuffer(hImage, imageByteArray, imageByteArray.size())

        if (loadStatus != FSDK.FSDKE_OK) {
            log.error("There was an error loading an image from the JPEG buffer.")
            render view: 'index', model: [statusMessage: "There was an error loading an image from the JPEG buffer."]
            return
        }

        log.debug("Successfully loaded image from the JPEG buffer.")

        def faceTemplate = new FSDK.FSDK_FaceTemplate.ByReference()
        def faceTemplateStatus = FSDK.GetFaceTemplate(hImage, faceTemplate)

        if (faceTemplateStatus != FSDK.FSDKE_OK) {
            log.warn("There was an error creating a face template from the image.")
            render view: 'index', model: [statusMessage: "There was an error creating a face template from the image."]
            return
        }

        log.debug("There are ${Person.countByFacialIdentifierIsNotNull()} with a facial identifier.")

        def person = Person.findAllByFacialIdentifierIsNotNull().find {
            log.debug("Attempting to match with $it.fullName")

            def faceTemplate2 = new FSDK.FSDK_FaceTemplate.ByReference()
            faceTemplate2.template = it.facialIdentifier.template

            def similarity = new float[1]
            def tolerance = new float[1]

            FSDK.GetMatchingThresholdAtFAR(0.02, tolerance)

            def matchStatus = FSDK.MatchFaces(faceTemplate, faceTemplate2, similarity)
            if (matchStatus != FSDK.FSDKE_OK) {
                log.error("An error occurred while matching templates.")
                return false
            }

            log.debug("Processing $it.fullName\nSimiliarity: ${similarity[0]}, Tolerance: ${tolerance[0]}")

            similarity[0] > tolerance[0]
        }

        def statusMessage
        if (person) {
            statusMessage = "This face belongs to $person.fullName"
            log.debug("This face belongs to $person.fullName")
        } else {
            statusMessage = "No matches found"
            log.debug("No matches found")
        }

        render(view: 'index', model: [statusMessage: statusMessage])
    }


    def identifyUnknown() {
        def person = Person.findById(params.long('personId'))
        if (person == null) {
            log.error("The facial enrollment form was submitted without a valid person selected.")
            render view: 'index', model: [statusMessage: "The facial enrollment form was submitted without a valid person selected."]
            return
        }
        FSDK.HImage hImage = new FSDK.HImage();
        if (FSDK.LoadImageFromFile(hImage, unknownImagePath) == FSDK.FSDKE_OK){
            EnrollmentController enrollmentController = new EnrollmentController()
            return enrollmentController.enroll(hImage, person)
        }
        return
    }

    def findIdentity(FSDK.HImage hImage) {
        def faceTemplate = new FSDK.FSDK_FaceTemplate.ByReference()
        def faceTemplateStatus = FSDK.GetFaceTemplate(hImage, faceTemplate)

        if (faceTemplateStatus != FSDK.FSDKE_OK) {
            log.warn("No face detected or there was an error creating a face template from the image.")
            render view: 'index', model: [monitoringStatus: "Either no face was detected or there was an error creating a face template from the image."]
            return 400
        }

        log.debug("There are ${Person.countByFacialIdentifierIsNotNull()} with a facial identifier.")
        if (Person.countByFacialIdentifierIsNotNull() == 0) {
            return 200
        }

        def person = Person.findAllByFacialIdentifierIsNotNull().find {
            log.debug("Attempting to match with $it.fullName")

            def faceTemplate2 = new FSDK.FSDK_FaceTemplate.ByReference()
            faceTemplate2.template = it.facialIdentifier.template

            def similarity = new float[1]
            def tolerance = new float[1]

            FSDK.GetMatchingThresholdAtFAR(0.02, tolerance)

            def matchStatus = FSDK.MatchFaces(faceTemplate, faceTemplate2, similarity)
            if (matchStatus != FSDK.FSDKE_OK) {
                log.debug("An error occurred while matching templates.")
                return 404
            }

            log.debug("Processing $it.fullName\nSimiliarity: ${similarity[0]}, Tolerance: ${tolerance[0]}")

            similarity[0] > tolerance[0]
        }
        if (person) {
            return person
        } else {
            return 200
        }
    }

    def findMatch() {

    }

    def stopMonitoring() {
        tracking = false
        render(view: 'index', model: [monitoringStatus: "Tracking stopped."])
        return
    }

    def monitor() {
        tracking = true
        render(view: 'index', model: [monitoringStatus: "Tracking started."])
        if (FSDK.FSDKE_OK != FSDK.LoadTrackerMemoryFromFile(tracker, TrackerMemoryFile)) // try to load saved tracker state
            log.info("Couldn't find " + TrackerMemoryFile)
            FSDK.CreateTracker(tracker) // if could not be loaded, create a new tracker


        // set realtime face detection parameters
        int[] err = new int[1]
        err[0] = 0
        FSDK.SetTrackerMultipleParameters(tracker, "HandleArbitraryRotations=false; DetermineFaceRotationAngle=false; InternalResizeWidth=100; FaceDetectionThreshold=5;", err)

        FSDKCam.InitializeCapturing()

        FSDKCam.TCameras cameraList = new FSDKCam.TCameras();
        int[] count = new int[1]
        FSDKCam.GetCameraList(cameraList, count)
        if (count[0] == 0){
            log.error("No camera found.")
            render view: 'index', model: [monitoringStatus: "No camera detected."]
            return
        }

        String cameraName = cameraList.cameras[0];

        FSDKCam.FSDK_VideoFormats formatList = new FSDKCam.FSDK_VideoFormats();
        FSDKCam.GetVideoFormatList(cameraName, formatList, count);
        FSDKCam.SetVideoFormat(cameraName, formatList.formats[0]);

        FSDKCam.HCamera cameraHandle = new FSDKCam.HCamera();
        int r = FSDKCam.OpenVideoCamera(cameraName, cameraHandle);
        if (r != FSDK.FSDKE_OK){
            log.error("Error opening camera.")
            render view: 'index', model: [monitoringStatus: "Error opening camera."]
            return
        }

        int reattempt = 0
        while (tracking) {
            log.info("Running loop.")
            FSDK.HImage imageHandle = new FSDK.HImage();
            if (FSDKCam.GrabFrame(cameraHandle, imageHandle) == FSDK.FSDKE_OK){
                Image[] awtImage = new Image[1];
                if (FSDK.SaveImageToAWTImage(imageHandle, awtImage, FSDK.FSDK_IMAGEMODE.FSDK_IMAGE_COLOR_24BIT) == FSDK.FSDKE_OK){

                    FSDK.HImage hImage = new FSDK.HImage()
                    def loadStatus = FSDK.LoadImageFromAWTImage(hImage, awtImage[0], FSDK.FSDK_IMAGEMODE.FSDK_IMAGE_COLOR_24BIT)

                    if (loadStatus != FSDK.FSDKE_OK) {
                        log.warn("There was an error loading an image from the AWT image.")
                        render view: 'index', model: [monitoringStatus: "There was an error loading an image from the AWT image."]
                        return
                    }

                    def person = findIdentity(hImage)
                    def statusMessage
                    if (person == 400) {
                        statusMessage = "No face detected"
                        log.debug("No face detected")
                        render(view: 'index', model: [monitoringStatus: statusMessage])
                    } else if (person == 404) {
                        statusMessage = "An error occurred."
                        log.debug("An error occurred.")
                        render(view: 'index', model: [monitoringStatus: statusMessage])
                    } else if (person == 200) {  // retry matching three times if face is detected, in case bad framing
                        if (reattempt < 5 ) {
                            reattempt = reattempt + 1
                            continue
                        } else {
                            if (FSDK.SaveImageToFile(hImage, unknownImagePath) != FSDK.FSDKE_OK) {
                                statusMessage = "An error occurred when saving the unknown face."
                                log.debug("An error occurred when saving the unknown face.")
                                render(view: 'index', model: [monitoringStatus: statusMessage])
                            }
                            reattempt = 0
                            statusMessage = "No matches found, new face present"
                            log.debug("No matches found, new face present")
                            render(view: 'index', model: [monitoringStatus: statusMessage, imagePath: resource(dir: 'images', file: 'unknown_face.jpg')])
                            // redirect(controller: 'enrollment', action: 'index')
                            return
                        }
                    } else {
                        statusMessage = "This face belongs to $person.fullName"
                        log.debug("This face belongs to $person.fullName")
                        render(view: 'index', model: [monitoringStatus: statusMessage])
                        return
                    }
                }
            }
            FSDK.FreeImage(imageHandle); // delete the FaceSDK image handle
        }
        render(view: 'index', model: [monitoringStatus: "Tracking stopped."])
        return
    }
}