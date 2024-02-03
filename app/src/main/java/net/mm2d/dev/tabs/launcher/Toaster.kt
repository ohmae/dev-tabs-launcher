package net.mm2d.dev.tabs.launcher

import android.app.Application
import android.widget.Toast

object Toaster {
    private lateinit var application: Application

    fun initialize(application: Application) {
        this.application = application
    }

    fun show(message: String) {
        Toast.makeText(application, message, Toast.LENGTH_SHORT).show()
    }
}
