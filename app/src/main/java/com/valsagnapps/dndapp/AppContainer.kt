package com.valsagnapps.dndapp

import com.valsagnapps.dndapp.data.CharacterRepository
import com.valsagnapps.dndapp.data.RemoteCharacterRepository
import com.valsagnapps.dndapp.data.remote.CharacterApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/** Creates the app's long-lived dependencies once (manual dependency injection). */
class AppContainer(baseUrl: String = BuildConfig.BASE_URL) {

    private val json = Json { ignoreUnknownKeys = true }

    private val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    val characterRepository: CharacterRepository =
        RemoteCharacterRepository(retrofit.create(CharacterApi::class.java), json)
}
