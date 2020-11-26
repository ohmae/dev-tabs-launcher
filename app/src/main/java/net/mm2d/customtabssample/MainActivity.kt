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
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
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
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = PackageAdapter(layoutInflater, createPackageList()) {
            LauncherActivity.start(this, it.packageName, it.label)
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
            PackageViewHolder(ItemPackageBinding.inflate(inflater, parent, false))

        override fun getItemCount(): Int = list.size

        override fun onBindViewHolder(holder: PackageViewHolder, position: Int) {
            val binding: ItemPackageBinding = holder.binding
            val info = list[position]
            binding.root.setOnClickListener { onClick(info) }
            binding.icon.setImageDrawable(info.drawable)
            binding.label.text = info.label
            binding.packageName.text = info.packageName
        }
    }

    private class PackageViewHolder(
        val binding: ItemPackageBinding
    ) : RecyclerView.ViewHolder(binding.root)

    companion object {
        private const val ACTION_CUSTOM_TABS_CONNECTION =
            "android.support.customtabs.action.CustomTabsService"
    }
}
