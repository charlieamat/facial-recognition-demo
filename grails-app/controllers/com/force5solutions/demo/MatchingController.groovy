package com.force5solutions.demo

import Luxand.*
import org.springframework.web.multipart.commons.CommonsMultipartFile

class MatchingController {

    static String license = "F3pXg0l9T9uc33EtbkrnzuFktKpcb9fwDdJlQeP+tkK9ofZ7oCmI6vL0Q2BOQMSDNzlWYvVaBqPyY1ZzoEvAcVYVGIyP47GBBe2xcJt63VyHysiZULQ8xAp7KfolGQolISsm/VdXMubw7hDWZiQ9Ui9ntZoiJFOecfPOhjWPHSI="

    def index() {
        [person: null]
    }

    def findIdentity() {
        def faceImage = params.faceImage as CommonsMultipartFile
        if (faceImage == null) {
            log.error("The facial enrollment form was submitted without a file.")
            render view: 'index', model: [statusMessage: "The facial enrollment form was submitted without a file."]
            return
        }

        log.debug("Finding an identity by their face.")

        def hImage = new FSDK.HImage()
        def loadStatus = FSDK.LoadImageFromJpegBuffer(hImage, faceImage.bytes, faceImage.bytes.size())

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
}
