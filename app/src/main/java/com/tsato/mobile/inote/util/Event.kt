package com.tsato.mobile.inote.util

open class Event<out T>(private val content: T) {

    var hasBeenHandled = false
    private set // can be overwritten only from within this class

    fun getContentIfNotHandled() = if (hasBeenHandled) {
        null
    }
    else {
        hasBeenHandled = true
        content
    }

    fun peekContent() = content

}