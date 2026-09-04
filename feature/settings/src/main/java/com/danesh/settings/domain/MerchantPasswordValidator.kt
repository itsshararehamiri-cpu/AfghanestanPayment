package com.danesh.settings.domain

object MerchantPasswordValidator {

    private val blockedPasswords = setOf("1234", "4321")

    fun isSecure(password: String): Boolean {
        if (password.length != 4 || password.any { !it.isDigit() }) return false
        if (password.all { it == password.first() }) return false
        if (password in blockedPasswords) return false
        return true
    }
}
