package com.force5solutions.demo

import Luxand.*
import org.springframework.web.multipart.commons.CommonsMultipartFile

class EnrollmentController {


    def index() {}

    def enroll() {
        def person = Person.findById(params.long('personId'))
        if (person == null) {
            log.error("The facial enrollment form was submitted without a valid person selected.")
            render view: 'index', model: [statusMessage: "The facial enrollment form was submitted without a valid person selected."]
            return
        }

        def faceImage = params.faceImage as CommonsMultipartFile
        if (faceImage == null) {
            log.error("The facial enrollment form was submitted without a file.")
            render view: 'index', model: [statusMessage: "The facial enrollment form was submitted without a file."]
            return
        }

        log.debug("Enrolling a facial identifier for $person.fullName.")

        def hImage = new FSDK.HImage()
        def loadStatus = FSDK.LoadImageFromJpegBuffer(hImage, faceImage.bytes, faceImage.bytes.size())

        if (loadStatus != FSDK.FSDKE_OK) {
            log.warn("There was an error loading an image from the JPEG buffer.")
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

        log.debug("Successfully created a face template from the image.")

        def facialIdentifier = new FacialIdentifier(template: faceTemplate.template).save(flush: true, failOnError: true)
        person.facialIdentifier = facialIdentifier
        person.save(flush: true, failOnError: true)

        render view: 'index', model: [statusMessage: "Successfully enrolled a facial identifier for $person.fullName."]
    }
}
