/*
 * Copyright (c) 2020 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dev.tabs.launcher

import android.app.Application

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Toaster.initialize(this)
    }
}
