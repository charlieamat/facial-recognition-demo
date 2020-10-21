import Luxand.FSDK
import com.force5solutions.demo.*
import org.codehaus.groovy.grails.io.support.ResourceLoader

class BootStrap {

    static String license = "RCGsrGMPo9+uzNiNbWSa53486Bi9Kdnkn7i9+WyXcv+CbJche4iKq3ZM1qAReKzy2inJA2RclGHRdWFUZCaQzEwgUDjPtFynkbNdw1wce0YJdCc4hst+9Y0FoylVjWIPJFHBhEF7kEJrpo2AWzhrDFJLHuuAsNYoSlkDK/EPBdc="

    def init = { servletContext ->
        new Person(firstName: 'Charles', lastName: 'Amat').save()
        new Person(firstName: 'Nathan', lastName: 'Harris').save()
        new Person(firstName: 'Robert', lastName: 'Evelyn').save()
        new Person(firstName: 'James', lastName: 'Evelyn').save()

        log.debug("Activating FSDK library.")

        def facesdkLibrary = ResourceLoader.getResource("lib/facesdk.dll").getFile()
        ResourceLoader.getResource("lib/facesdk.dll").path
        //println(facesdkLibrary)
        System.load("C:\\Program Files (x86)\\Luxand\\FaceSDK 7.2.0\\bin\\win64\\facesdk.dll")
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
