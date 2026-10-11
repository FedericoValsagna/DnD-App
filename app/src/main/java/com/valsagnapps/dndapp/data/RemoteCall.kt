package com.valsagnapps.dndapp.data

import com.valsagnapps.dndapp.data.remote.ProblemDetailDto
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException

/**
 * Runs a call to the API and turns what Retrofit throws into a [RepositoryResult.Failure].
 *
 * @param logFailure receives every exception turned into a [RepositoryError], so it isn't lost.
 */
internal class RemoteCall(private val json: Json, private val logFailure: (Throwable) -> Unit) {

    suspend operator fun <T> invoke(block: suspend () -> T): RepositoryResult<T> = try {
        RepositoryResult.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: HttpException) {
        logFailure(e)
        RepositoryResult.Failure(e.toRepositoryError())
    } catch (e: SerializationException) {
        logFailure(e)
        RepositoryResult.Failure(RepositoryError.InvalidResponse)
    } catch (e: IOException) {
        logFailure(e)
        RepositoryResult.Failure(RepositoryError.Network)
    }

    private fun HttpException.toRepositoryError(): RepositoryError {
        if (code() == HTTP_NOT_FOUND) return RepositoryError.NotFound
        val detail = response()?.errorBody()?.string()?.let { body ->
            runCatching { json.decodeFromString<ProblemDetailDto>(body).detail }.getOrNull()
        }
        return RepositoryError.Server(status = code(), detail = detail)
    }

    private companion object {
        const val HTTP_NOT_FOUND = 404
    }
}
