package com.valsagnapps.dndapp.data

import com.valsagnapps.dndapp.data.remote.CharacterApi
import com.valsagnapps.dndapp.data.remote.ProblemDetailDto
import com.valsagnapps.dndapp.data.remote.UpdateHitPointsRequest
import com.valsagnapps.dndapp.data.remote.UpdateSubclassRequest
import com.valsagnapps.dndapp.data.remote.toDomain
import com.valsagnapps.dndapp.data.remote.toRequest
import com.valsagnapps.dndapp.data.remote.toUpdateClassesRequest
import com.valsagnapps.dndapp.data.remote.toUpdateSkillsRequest
import com.valsagnapps.dndapp.domain.Character
import com.valsagnapps.dndapp.domain.CharacterClass
import com.valsagnapps.dndapp.domain.ClassInfo
import com.valsagnapps.dndapp.domain.ClassLevel
import com.valsagnapps.dndapp.domain.NewCharacter
import com.valsagnapps.dndapp.domain.Proficiency
import com.valsagnapps.dndapp.domain.Skill
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException

/**
 * @param logFailure receives every exception turned into a [RepositoryError], so it isn't lost
 * (the app sends it to Logcat).
 */
class RemoteCharacterRepository(
    private val api: CharacterApi,
    private val json: Json,
    private val logFailure: (Throwable) -> Unit = {},
) : CharacterRepository {

    override suspend fun list(): RepositoryResult<List<Character>> = call { api.list().map { it.toDomain() } }

    override suspend fun get(id: String): RepositoryResult<Character> = call { api.get(id).toDomain() }

    override suspend fun create(character: NewCharacter): RepositoryResult<Character> =
        call { api.create(character.toRequest()).toDomain() }

    override suspend fun updateSkills(id: String, skills: Map<Skill, Proficiency>): RepositoryResult<Character> =
        call { api.updateSkills(id, skills.toUpdateSkillsRequest()).toDomain() }

    override suspend fun updateClasses(id: String, classes: List<ClassLevel>): RepositoryResult<Character> =
        call { api.updateClasses(id, classes.toUpdateClassesRequest()).toDomain() }

    override suspend fun updateSubclass(
        id: String,
        characterClass: CharacterClass,
        subclassId: String?,
    ): RepositoryResult<Character> =
        call { api.updateSubclass(id, characterClass.name, UpdateSubclassRequest(subclassId)).toDomain() }

    override suspend fun updateMaxHitPoints(id: String, maxHitPoints: Int): RepositoryResult<Character> =
        call { api.updateHitPoints(id, UpdateHitPointsRequest(maxHitPoints)).toDomain() }

    override suspend fun listClasses(): RepositoryResult<List<ClassInfo>> = call { api.listClasses().toDomain() }

    private suspend fun <T> call(block: suspend () -> T): RepositoryResult<T> = try {
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
