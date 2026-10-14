package com.example.tysyacha_calc

import android.app.Application

class TysyachaApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
