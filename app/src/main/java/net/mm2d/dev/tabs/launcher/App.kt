package net.mm2d.dev.tabs.launcher

import android.app.Application

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Toaster.initialize(this)
    }
}
