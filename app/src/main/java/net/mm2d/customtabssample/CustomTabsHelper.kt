/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
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
class CustomTabsHelper(private val context: Context) : CustomTabsServiceConnection() {
    private var bound: Boolean = false
    private var onConnect: ((session: CustomTabsSession) -> Unit)? = null

    fun bind(packageName: String, callback: ((session: CustomTabsSession) -> Unit)) {
        if (bound) {
            unbind()
            return
        }
        onConnect = callback
        bound = CustomTabsClient.bindCustomTabsService(context, packageName, this)
    }

    fun unbind() {
        if (!bound) {
            return
        }
        context.unbindService(this)
        bound = false
    }

    override fun onCustomTabsServiceConnected(name: ComponentName, client: CustomTabsClient) {
        client.warmup(0)
        val session = client.newSession(object : CustomTabsCallback() {
            override fun onRelationshipValidationResult(
                relation: Int,
                requestedOrigin: Uri?,
                result: Boolean,
                extras: Bundle?
            ) {
                extras?.getBundle("")
                Log.e("TAG", "onRelationshipValidationResult:$relation $requestedOrigin $result $extras")
            }

            override fun onNavigationEvent(navigationEvent: Int, extras: Bundle?) {
                extras?.getBundle("")
                Log.e("TAG", "onNavigationEvent:$navigationEvent $extras")
            }

            override fun extraCallback(callbackName: String?, args: Bundle?) {
                args?.getBundle("")
                Log.e("TAG", "extraCallback:$callbackName $args")
            }

            override fun onPostMessage(message: String?, extras: Bundle?) {
                extras?.getBundle("")
                Log.e("TAG", "onPostMessage:$message $extras")
            }

            override fun onMessageChannelReady(extras: Bundle?) {
                extras?.getBundle("")
                Log.e("TAG", "onMessageChannelReady:$extras")
            }
        })
        onConnect?.invoke(session)
        onConnect = null
    }

    override fun onServiceDisconnected(name: ComponentName) {}
}
