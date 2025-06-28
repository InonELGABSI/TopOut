package com.topout.kmp.data

sealed interface Result<out R, out E> {
    data class Success<R>(val data: R?) : Result<R, Nothing>
    data class Failure<E: Error>(val error: E?) : Result<Nothing, E>
}

interface Error {
    val message: String
}