package com.example.navigationlab

import android.app.Application
import com.example.navigationlab.di.labModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class NavigationLabApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@NavigationLabApp)
            modules(labModule)
        }
    }
}
