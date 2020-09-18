import Luxand.FSDK
import com.force5solutions.demo.*
import org.codehaus.groovy.grails.io.support.ResourceLoader

class BootStrap {

    static String license = "F3pXg0l9T9uc33EtbkrnzuFktKpcb9fwDdJlQeP+tkK9ofZ7oCmI6vL0Q2BOQMSDNzlWYvVaBqPyY1ZzoEvAcVYVGIyP47GBBe2xcJt63VyHysiZULQ8xAp7KfolGQolISsm/VdXMubw7hDWZiQ9Ui9ntZoiJFOecfPOhjWPHSI="

    def init = { servletContext ->
        new Person(firstName: 'Charles', lastName: 'Amat').save()
        new Person(firstName: 'Nathan', lastName: 'Harris').save()
        new Person(firstName: 'Robert', lastName: 'Evelyn').save()
        new Person(firstName: 'James', lastName: 'Evelyn').save()

        log.debug("Activating FSDK library.")

        //def facesdkLibrary = ResourceLoader.getResource("lib/facesdk.dll").getFile()
        //println(facesdkLibrary)
        System.load("C:\\Program Files (x86)\\Luxand\\FaceSDK 7.1.0\\bin\\win64\\facesdk.dll")
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
    def destroy = {
        FSDK.finalize()
        log.debug("Successfully finalized the FSDK library.")
    }
}
