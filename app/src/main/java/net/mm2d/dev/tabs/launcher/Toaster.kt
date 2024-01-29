package net.mm2d.dev.tabs.launcher

import android.app.Application

object Toaster {
    private lateinit var application: Application

    fun initialize(application: Application) {
        this.application = application
    }

    fun show(message: String) {
        android.widget.Toast.makeText(application, message, android.widget.Toast.LENGTH_SHORT).show()
    }
}
