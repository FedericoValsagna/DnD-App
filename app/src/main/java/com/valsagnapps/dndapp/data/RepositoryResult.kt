package com.valsagnapps.dndapp.data

/** Outcome of a repository call. Repositories never throw network or server errors. */
sealed interface RepositoryResult<out T> {
    data class Success<T>(val value: T) : RepositoryResult<T>
    data class Failure(val error: RepositoryError) : RepositoryResult<Nothing>
}

sealed interface RepositoryError {
    /** The server couldn't be reached (no connection, timeout, server down). */
    data object Network : RepositoryError

    data object NotFound : RepositoryError

    /** Any other error response. [detail] comes from the problem+json body, if present. */
    data class Server(val status: Int, val detail: String?) : RepositoryError

    /** The server answered successfully but with a body the app can't read. */
    data object InvalidResponse : RepositoryError
}
