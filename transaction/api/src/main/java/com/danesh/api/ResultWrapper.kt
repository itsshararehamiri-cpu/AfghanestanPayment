package com.danesh.api
sealed class ResultWrapper<out T> {
    data class Success<out T>(
        val data: T
    ) : ResultWrapper<T>()
    data class Failed<out T>(
        val message: String,    val data: T
    ) : ResultWrapper<T>()
    data class UnSuccess<out T>(
        val message: String,    val data: T
    ) : ResultWrapper<T>()
}