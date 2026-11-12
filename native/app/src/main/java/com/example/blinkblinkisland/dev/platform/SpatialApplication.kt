package com.example.blinkblinkisland.dev.platform

import android.app.Application
import com.pico.spatial.ui.foundation.dsl.launch
import com.example.blinkblinkisland.dev.mainApp

class SpatialApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        launch(::mainApp)
    }
}
