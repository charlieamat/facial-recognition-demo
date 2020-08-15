package com.force5solutions.demo

class FacialIdentifier {

    byte[] template

    static constraints = {
        template maxSize: 10 * 1024 * 1024
    }
}
