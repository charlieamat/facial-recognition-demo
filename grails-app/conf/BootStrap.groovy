import Luxand.FSDK
import com.force5solutions.demo.*

class BootStrap {

    static String license = "AigFBWMDQcjyOOoVmuwrByOoZF5CZ7rHROzL372Le7Pwi50U+VpkFcHDp3t/VEAnVqalc9mYqK/k/5CzlqvxBR8vnuUDfBdNExTOal7kmS3MWfXrL+7qedbMqpz4qTcKooxKwmdFSB6Xa5tWp4613SNOt47BmYvSfLH6IIoxLMU="

    def init = { servletContext ->
        new Person(firstName: 'Charles', lastName: 'Amat').save()
        new Person(firstName: 'Nathan', lastName: 'Harris').save()
        new Person(firstName: 'Robert', lastName: 'Evelyn').save()
        new Person(firstName: 'James', lastName: 'Evelyn').save()

        log.debug("Activating FSDK library.")

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
