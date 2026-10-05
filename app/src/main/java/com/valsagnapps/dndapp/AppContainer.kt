package com.valsagnapps.dndapp

import android.util.Log
import com.valsagnapps.dndapp.data.CharacterRepository
import com.valsagnapps.dndapp.data.RemoteCharacterRepository
import com.valsagnapps.dndapp.data.remote.CharacterApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/** Creates the app's long-lived dependencies once (manual dependency injection). */
class AppContainer(baseUrl: String = BuildConfig.BASE_URL) {

    private val json = Json { ignoreUnknownKeys = true }

    // In debug, every request, response and connection error goes to Logcat (filter: tag:DnDHttp).
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(
            HttpLoggingInterceptor { message -> Log.d(HTTP_LOG_TAG, message) }.apply {
                level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                else HttpLoggingInterceptor.Level.NONE
            }
        )
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    val characterRepository: CharacterRepository =
        RemoteCharacterRepository(retrofit.create(CharacterApi::class.java), json)

    private companion object {
        const val HTTP_LOG_TAG = "DnDHttp"
    }
}
