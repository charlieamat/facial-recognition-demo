package com.force5solutions.demo

import Luxand.*
import org.springframework.web.multipart.commons.CommonsMultipartFile

class EnrollmentController {

    static String license = "AigFBWMDQcjyOOoVmuwrByOoZF5CZ7rHROzL372Le7Pwi50U+VpkFcHDp3t/VEAnVqalc9mYqK/k/5CzlqvxBR8vnuUDfBdNExTOal7kmS3MWfXrL+7qedbMqpz4qTcKooxKwmdFSB6Xa5tWp4613SNOt47BmYvSfLH6IIoxLMU="

    def index() {
    }

    def enroll() {
        def faceImage = params.faceImage as CommonsMultipartFile

        if (faceImage == null) {
            log.error("The facial enrollment form was submitted without a file.")
            return
        }

        log.debug("Enrolling a facial identifier for <IDENTITY>.")

        log.debug("Activating FSDK library.")

        def activationStatus = FSDK.ActivateLibrary(license)

        if (activationStatus != FSDK.FSDKE_OK) {
            log.warn("There was an error activating the FSDK library.")
            return
        }

        log.debug("Successfully activated the FSDK library.")

        def initializationStatus = FSDK.Initialize()

        if (initializationStatus != FSDK.FSDKE_OK) {
            log.warn("There was an error initializing the FSDK library.")
            return
        }

        log.debug("Successfully initialized the FSDK library.")

        def hImage = new FSDK.HImage()
        def loadStatus = FSDK.LoadImageFromJpegBuffer(hImage, faceImage.bytes, faceImage.bytes.size())

        if (loadStatus != FSDK.FSDKE_OK) {
            log.warn("There was an error loading an image from the JPEG buffer.")
            return
        }

        log.debug("Successfully loaded image from the JPEG buffer.")

        def faceTemplate = new FSDK.FSDK_FaceTemplate.ByReference()
        def faceTemplateStatus = FSDK.GetFaceTemplate(hImage, faceTemplate)

        if (faceTemplateStatus != FSDK.FSDKE_OK) {
            log.warn("There was an error creating a face template from the image.")
            return
        }

        log.debug("Successfully created a face template from the image.")

        new FacialIdentifier(template: faceTemplate.template).save()

        FSDK.finalize()

        log.debug("Successfully finalized the FSDK library.")
    }

}