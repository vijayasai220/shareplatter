package com.example.shareplate

import android.app.Application
import com.example.shareplate.utils.AppwriteService

class SharePlateApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppwriteService.initialize(this)
    }
} 