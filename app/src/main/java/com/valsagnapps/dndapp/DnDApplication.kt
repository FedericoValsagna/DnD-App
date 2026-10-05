package com.valsagnapps.dndapp

import android.app.Application

class DnDApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer()
    }
}
