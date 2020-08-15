import com.force5solutions.demo.*

class BootStrap {

    def init = { servletContext ->
        new Person(firstName: 'Charles', lastName: 'Amat').save()
        new Person(firstName: 'Nathan', lastName: 'Harris').save()
        new Person(firstName: 'Robert', lastName: 'Evelyn').save()
        new Person(firstName: 'James', lastName: 'Evelyn').save()
    }
    def destroy = {
    }
}
