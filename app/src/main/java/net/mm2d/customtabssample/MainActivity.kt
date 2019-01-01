/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.customtabssample

import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var helper: CustomTabsHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        helper = CustomTabsHelper(this)
        toolbar.isFocusable = true
        toolbar.isFocusableInTouchMode = true
        toolbar.requestFocus()
        editText.setText(DEFAULT_URL, TextView.BufferType.NORMAL)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = PackageAdapter(layoutInflater, createPackageList()) {
            startCustomTabs(it.packageName)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        helper.unbind()
    }

    private fun startCustomTabs(packageName: String) {
        helper.bind(packageName) {
            val customTabsIntent = CustomTabsIntent.Builder(it)
                .setShowTitle(true)
                .setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setCloseButtonIcon(AppCompatResources.getDrawable(this, R.drawable.ic_test)!!.toBitmap())
//                .enableUrlBarHiding()
//                .setStartAnimations(this, R.anim.slide_in_right, R.anim.slide_out_left)
//                .setExitAnimations(this, R.anim.slide_in_left, R.anim.slide_out_right)
//                .setSecondaryToolbarViews(
//                    RemoteViews(this.packageName, R.layout.layout_toolbar),
//                    intArrayOf(R.id.button1, R.id.button2, R.id.button3),
//                    PendingIntent.getActivity(
//                        this, 1,
//                        Intent(this, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT
//                    )
//                )
//                .setActionButton(
//                    AppCompatResources.getDrawable(this, R.drawable.ic_test)!!.toBitmap(),
//                    "test",
//                    PendingIntent.getActivity(
//                        this, 1,
//                        Intent(this, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT
//                    ), true
//                )
//                .addDefaultShareMenuItem()
                .addMenuItem("テスト",
                    PendingIntent.getActivity(
                        this, 1,
                        Intent(this, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT
                    ))
                .build()
            customTabsIntent.intent.setPackage(packageName)
            customTabsIntent.launchUrl(this, Uri.parse(editText.text.toString()))
        }
    }

    private fun Drawable.toBitmap(): Bitmap {
        if (this is BitmapDrawable) return bitmap
        return Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888).also {
            val canvas = Canvas(it)
            setBounds(0, 0, canvas.width, canvas.height)
            draw(canvas)
        }
    }

    private fun createPackageList(): List<PackageInfo> {
        val pm = packageManager
        val browsers = getBrowserPackages(pm)
        return pm.queryIntentServices(Intent(ACTION_CUSTOM_TABS_CONNECTION), 0)
            .mapNotNull { it.serviceInfo }
            .filter { browsers.contains(it.packageName) }
            .map {
                PackageInfo(
                    it.loadIcon(pm),
                    it.loadLabel(pm).toString(),
                    it.packageName
                )
            }
    }

    private fun getBrowserPackages(pm: PackageManager): Set<String> {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.example.com/"))
        intent.addCategory(Intent.CATEGORY_BROWSABLE)
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PackageManager.MATCH_ALL
        } else {
            0
        }
        return pm.queryIntentActivities(intent, flags)
            .mapNotNull { it.activityInfo?.packageName }
            .toSet()
    }

    private data class PackageInfo(
        val drawable: Drawable?,
        val label: String,
        val packageName: String
    )

    private class PackageAdapter(
        private val inflater: LayoutInflater,
        private val list: List<PackageInfo>,
        private val onClick: (info: PackageInfo) -> Unit
    ) : RecyclerView.Adapter<PackageViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PackageViewHolder =
            PackageViewHolder(inflater.inflate(R.layout.li_package, parent, false))

        override fun getItemCount(): Int = list.size

        override fun onBindViewHolder(holder: PackageViewHolder, position: Int) {
            val info = list[position]
            holder.itemView.setOnClickListener { onClick(info) }
            holder.icon.setImageDrawable(info.drawable)
            holder.label.text = info.label
            holder.packageName.text = info.packageName
        }
    }

    private class PackageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.icon)
        val label: TextView = itemView.findViewById(R.id.label)
        val packageName: TextView = itemView.findViewById(R.id.packageName)
    }

    companion object {
        private const val DEFAULT_URL = "https://droidkaigi.jp/2019/"
        private const val ACTION_CUSTOM_TABS_CONNECTION =
            "android.support.customtabs.action.CustomTabsService"
    }
}
