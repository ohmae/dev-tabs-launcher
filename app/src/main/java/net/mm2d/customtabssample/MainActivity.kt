/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.customtabssample

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsService
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import net.mm2d.customtabssample.databinding.ActivityMainBinding
import net.mm2d.customtabssample.databinding.ItemPackageBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        binding.toolbar.isFocusable = true
        binding.toolbar.isFocusableInTouchMode = true
        binding.toolbar.requestFocus()
        binding.editText.setText(DEFAULT_URL, TextView.BufferType.NORMAL)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = PackageAdapter(
            layoutInflater,
            createPackageList(),
            this::onClick,
            this::onLongClick
        )
    }

    private fun onClick(info: PackageInfo) {
        when {
            CustomTabsHelper.packageName == null -> {
                CustomTabsHelper.bind(this, info.packageName)
                binding.recyclerView.adapter?.notifyDataSetChanged()
            }
            CustomTabsHelper.packageName != info.packageName -> {
                CustomTabsHelper.unbind(this)
                binding.recyclerView.adapter?.notifyDataSetChanged()
            }
            else -> {
                val customTabsIntent = CustomTabsIntent.Builder(CustomTabsHelper.session)
                    .setShowTitle(true)
                    .build()
                customTabsIntent.intent.setPackage(info.packageName)
                customTabsIntent.launchUrl(this, Uri.parse(binding.editText.text.toString()))
            }
        }
    }

    private fun onLongClick(info: PackageInfo) {
        when {
            CustomTabsHelper.packageName == null -> {
                CustomTabsHelper.bind(this, info.packageName)
                binding.recyclerView.adapter?.notifyDataSetChanged()
            }
            CustomTabsHelper.packageName != info.packageName -> {
                CustomTabsHelper.unbind(this)
                binding.recyclerView.adapter?.notifyDataSetChanged()
            }
            else -> {
                val urlList = listOf(SECOND_URL)
                    .map { Uri.parse(it) }
                    .map { makeOtherLikelyBundle(it) }
                CustomTabsHelper.session?.mayLaunchUrl(Uri.parse(DEFAULT_URL), null, urlList)
            }
        }
    }

    private fun makeOtherLikelyBundle(uri: Uri): Bundle {
        return Bundle().also { it.putParcelable(CustomTabsService.KEY_URL, uri) }
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
        private val onClick: (info: PackageInfo) -> Unit,
        private val onLongClick: (info: PackageInfo) -> Unit
    ) : RecyclerView.Adapter<PackageViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PackageViewHolder =
            PackageViewHolder(ItemPackageBinding.inflate(inflater, parent, false))

        override fun getItemCount(): Int = list.size

        override fun onBindViewHolder(holder: PackageViewHolder, position: Int) {
            val binding: ItemPackageBinding = holder.binding
            val info = list[position]
            if (CustomTabsHelper.packageName == info.packageName) {
                binding.root.setBackgroundColor(COLOR_CONNECTED)
            } else {
                binding.root.background = null
            }
            binding.root.setOnClickListener { onClick(info) }
            binding.root.setOnLongClickListener {
                onLongClick(info)
                true
            }
            binding.icon.setImageDrawable(info.drawable)
            binding.label.text = info.label
            binding.packageName.text = info.packageName
        }
    }

    private class PackageViewHolder(
        val binding: ItemPackageBinding
    ) : RecyclerView.ViewHolder(binding.root)

    companion object {
        private const val DEFAULT_URL = "https://m.yahoo.co.jp/"
        private const val SECOND_URL = "https://news.yahoo.co.jp/"
        private const val ACTION_CUSTOM_TABS_CONNECTION =
            "android.support.customtabs.action.CustomTabsService"
        private val COLOR_CONNECTED = Color.argb(32, 0, 0, 255)
    }
}
