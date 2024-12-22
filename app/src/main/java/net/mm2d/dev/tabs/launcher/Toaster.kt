/*
 * Copyright (c) 2020 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dev.tabs.launcher

import android.app.Application
import android.widget.Toast

object Toaster {
    private lateinit var application: Application

    fun initialize(
        application: Application,
    ) {
        this.application = application
    }

    fun show(
        message: String,
    ) {
        Toast.makeText(application, message, Toast.LENGTH_SHORT).show()
    }
}
