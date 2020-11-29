/*
 * Copyright (c) 2019 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.customtabssample

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.browser.customtabs.CustomTabsCallback
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.browser.customtabs.CustomTabsSession

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
object CustomTabsHelper : CustomTabsServiceConnection() {
    private var bound: Boolean = false
    var session: CustomTabsSession? = null
        private set
    var packageName: String? = null
        private set

    internal fun bind(context: Context, packageName: String) {
        if (!bound) {
            this.packageName = packageName
            bound = CustomTabsClient.bindCustomTabsService(
                context.applicationContext,
                packageName,
                this
            )
        }
    }

    internal fun unbind(context: Context) {
        if (bound) {
            context.applicationContext.unbindService(this)
            bound = false
            session = null
            packageName = null
        }
    }

    override fun onCustomTabsServiceConnected(name: ComponentName, client: CustomTabsClient) {
        client.warmup(0)
        session = client.newSession(object : CustomTabsCallback() {
            override fun onNavigationEvent(navigationEvent: Int, extras: Bundle?) {
                extras?.containsKey("")
                Log.e("XXXX", "onNavigationEvent: $navigationEvent $extras")
            }

            override fun extraCallback(callbackName: String, args: Bundle?) {
                args?.containsKey("")
                Log.e("XXXX", "extraCallback: $callbackName $args")
            }

            override fun extraCallbackWithResult(callbackName: String, args: Bundle?): Bundle? {
                args?.containsKey("")
                Log.e("XXXX", "extraCallbackWithResult: $callbackName $args")
                return null
            }

            override fun onMessageChannelReady(extras: Bundle?) {
                extras?.containsKey("")
                Log.e("XXXX", "onMessageChannelReady: $extras")
            }

            override fun onPostMessage(message: String, extras: Bundle?) {
                extras?.containsKey("")
                Log.e("XXXX", "onPostMessage: $message $extras")
            }

            override fun onRelationshipValidationResult(
                relation: Int,
                requestedOrigin: Uri,
                result: Boolean,
                extras: Bundle?
            ) {
                extras?.containsKey("")
                Log.e("XXXX", "onRelationshipValidationResult: $relation $requestedOrigin $result $extras")
            }
        })
    }

    override fun onServiceDisconnected(name: ComponentName) {
        session = null
    }
}
