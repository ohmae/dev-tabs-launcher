/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.customtabssample

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        toolbar.isFocusable = true
        toolbar.isFocusableInTouchMode = true
        toolbar.requestFocus()
        editText.setText(DEFAULT_URL, TextView.BufferType.NORMAL)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = PackageAdapter(layoutInflater, createPackageList()) {
            val customTabsIntent = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .build()
            customTabsIntent.intent.setPackage(it.packageName)
            customTabsIntent.launchUrl(this, Uri.parse(editText.text.toString()))
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
        return pm.queryIntentActivities(intent, 0)
            .mapNotNull { it.activityInfo }
            .map { it.packageName }
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
        private const val DEFAULT_URL = "https://m.yahoo.co.jp/"
        private const val ACTION_CUSTOM_TABS_CONNECTION =
            "android.support.customtabs.action.CustomTabsService"
    }
}
