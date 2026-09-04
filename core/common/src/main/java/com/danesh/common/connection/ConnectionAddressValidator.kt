package com.danesh.common.connection

object ConnectionAddressValidator {

    fun ipError(value: String): IpValidationError? {
        val trimmed = value.trim()
        if (trimmed.isEmpty()) return IpValidationError.EMPTY
        if (!isValidIpv4(trimmed)) return IpValidationError.INVALID
        return null
    }

    fun portError(value: String): PortValidationError? {
        val trimmed = value.trim()
        if (trimmed.isEmpty()) return PortValidationError.EMPTY
        if (!trimmed.all { it.isDigit() }) return PortValidationError.INVALID
        val port = trimmed.toIntOrNull() ?: return PortValidationError.INVALID
        if (port !in MIN_PORT..MAX_PORT) return PortValidationError.INVALID
        return null
    }

    fun niiError(value: String): NiiValidationError? {
        val trimmed = value.trim()
        if (trimmed.isEmpty()) return NiiValidationError.EMPTY
        if (!trimmed.all { it.isDigit() }) return NiiValidationError.INVALID
        return null
    }

    fun defaultDepositIdError(value: String): DefaultDepositIdValidationError? {
        val trimmed = value.trim()
        if (trimmed.isEmpty()) return DefaultDepositIdValidationError.EMPTY
        if (!trimmed.all { it.isDigit() }) return DefaultDepositIdValidationError.INVALID
        if (trimmed.length !in MIN_DEFAULT_DEPOSIT_ID_LENGTH..MAX_DEFAULT_DEPOSIT_ID_LENGTH) {
            return DefaultDepositIdValidationError.LENGTH
        }
        return null
    }

    fun isMainServerConfigured(ip: String, port: Int): Boolean =
        ipError(ip) == null && portError(port.toString()) == null

    fun isValidIpv4(value: String): Boolean {
        val parts = value.split('.')
        if (parts.size != 4) return false
        return parts.all { part ->
            part.isNotEmpty() &&
                part.length <= 3 &&
                part.all { it.isDigit() } &&
                part.toIntOrNull() in 0..255
        }
    }

    enum class IpValidationError {
        EMPTY,
        INVALID,
    }

    enum class PortValidationError {
        EMPTY,
        INVALID,
    }

    enum class NiiValidationError {
        EMPTY,
        INVALID,
    }

    enum class DefaultDepositIdValidationError {
        EMPTY,
        INVALID,
        LENGTH,
    }

    private const val MIN_DEFAULT_DEPOSIT_ID_LENGTH = 3
    private const val MAX_DEFAULT_DEPOSIT_ID_LENGTH = 30

    private const val MIN_PORT = 1
    private const val MAX_PORT = 65535
}
