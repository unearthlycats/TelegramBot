package net.unearthly.telegramBot

fun catch(condition: () -> Boolean): Boolean {
    return condition()
}

//ai, I don't now will it work correctly
inline fun <reified T> Any?.cast(): T? {
    return this as? T
}