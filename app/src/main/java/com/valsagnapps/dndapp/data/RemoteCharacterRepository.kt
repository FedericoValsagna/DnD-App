package com.valsagnapps.dndapp.data

import com.valsagnapps.dndapp.data.remote.CharacterApi
import com.valsagnapps.dndapp.data.remote.ProblemDetailDto
import com.valsagnapps.dndapp.data.remote.toDomain
import com.valsagnapps.dndapp.data.remote.toRequest
import com.valsagnapps.dndapp.domain.Character
import com.valsagnapps.dndapp.domain.NewCharacter
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException

class RemoteCharacterRepository(
    private val api: CharacterApi,
    private val json: Json,
) : CharacterRepository {

    override suspend fun list(): RepositoryResult<List<Character>> =
        call { api.list().map { it.toDomain() } }

    override suspend fun get(id: String): RepositoryResult<Character> =
        call { api.get(id).toDomain() }

    override suspend fun create(character: NewCharacter): RepositoryResult<Character> =
        call { api.create(character.toRequest()).toDomain() }

    private suspend fun <T> call(block: suspend () -> T): RepositoryResult<T> =
        try {
            RepositoryResult.Success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: HttpException) {
            RepositoryResult.Failure(e.toRepositoryError())
        } catch (e: SerializationException) {
            RepositoryResult.Failure(RepositoryError.InvalidResponse)
        } catch (e: IOException) {
            RepositoryResult.Failure(RepositoryError.Network)
        }

    private fun HttpException.toRepositoryError(): RepositoryError {
        if (code() == 404) return RepositoryError.NotFound
        val detail = response()?.errorBody()?.string()?.let { body ->
            runCatching { json.decodeFromString<ProblemDetailDto>(body).detail }.getOrNull()
        }
        return RepositoryError.Server(status = code(), detail = detail)
    }
}
