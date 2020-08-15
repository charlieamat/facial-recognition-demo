package com.force5solutions.demo

class Person {
    static transients = ['fullName']

    String firstName
    String lastName
    FacialIdentifier facialIdentifier

    static constraints = {
        facialIdentifier nullable: true
    }

    String getFullName() {
        "$firstName $lastName"
    }
}
