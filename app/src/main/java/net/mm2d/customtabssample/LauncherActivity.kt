/*
 * Copyright (c) 2020 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.customtabssample

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsService
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import net.mm2d.color.chooser.ColorChooserDialog
import net.mm2d.customtabssample.databinding.ActivityLauncherBinding

class LauncherActivity : AppCompatActivity(), ColorChooserDialog.Callback {
    private lateinit var binding: ActivityLauncherBinding
    private var toolbarColor: Int = Color.WHITE
    private var secondaryToolbarColor: Int = Color.WHITE
    private var navigationBarColor: Int = Color.BLACK
    private var toolbarColorScheme: Int = Color.BLACK
    private var secondaryToolbarColorScheme: Int = Color.BLACK
    private var navigationBarColorScheme: Int = Color.BLACK

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLauncherBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME)!!
        supportActionBar?.let {
            it.title = intent.getStringExtra(EXTRA_LABEL)
            it.subtitle = packageName
        }
        binding.toolbar.isFocusable = true
        binding.toolbar.isFocusableInTouchMode = true
        binding.toolbar.requestFocus()
        binding.editText.setText(DEFAULT_URL, TextView.BufferType.NORMAL)
        binding.launchButton.setOnClickListener { launch() }
        toolbarColor = ContextCompat.getColor(this, R.color.colorAccent)
        setToolbarColor(toolbarColor)
        binding.toolbarColorSample.setOnClickListener {
            ColorChooserDialog.show(this, REQUEST_CODE_TOOLBAR, toolbarColor)
        }
        binding.secondaryToolbarColorSample.setBackgroundColor(secondaryToolbarColor)
        binding.secondaryToolbarColorSample.setOnClickListener {
            ColorChooserDialog.show(this, REQUEST_CODE_SECONDARY_TOOLBAR, secondaryToolbarColor)
        }
        binding.navigationBarColorSample.setBackgroundColor(navigationBarColor)
        binding.navigationBarColorSample.setOnClickListener {
            ColorChooserDialog.show(this, REQUEST_CODE_NAVIGATION_BAR, navigationBarColor)
        }
        setToolbarColorScheme(toolbarColorScheme)
        binding.toolbarColorSchemeSample.setOnClickListener {
            ColorChooserDialog.show(this, REQUEST_CODE_TOOLBAR_SCHEME, toolbarColorScheme)
        }
        binding.secondaryToolbarColorSchemeSample.setBackgroundColor(secondaryToolbarColorScheme)
        binding.secondaryToolbarColorSchemeSample.setOnClickListener {
            ColorChooserDialog.show(
                this,
                REQUEST_CODE_SECONDARY_TOOLBAR_SCHEME,
                secondaryToolbarColorScheme
            )
        }
        binding.navigationBarColorSchemeSample.setBackgroundColor(navigationBarColorScheme)
        binding.navigationBarColorSchemeSample.setOnClickListener {
            ColorChooserDialog.show(
                this,
                REQUEST_CODE_NAVIGATION_BAR_SCHEME,
                navigationBarColorScheme
            )
        }
        CustomTabsHelper.bind(this, packageName)
    }

    override fun onDestroy() {
        super.onDestroy()
        CustomTabsHelper.unbind(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val message = intent.getStringExtra(EXTRA_MESSAGE)
        if (message.isNullOrEmpty()) return
        Snackbar.make(binding.root, message, Snackbar.LENGTH_INDEFINITE).also { bar ->
            bar.setAction("OK") { bar.dismiss() }
        }.show()
    }

    private fun launch() {
        val customTabsIntent = CustomTabsIntent.Builder(CustomTabsHelper.session).also {
            if (binding.toolbarColor.isChecked) {
                it.setToolbarColor(toolbarColor)
            }
            if (binding.secondaryToolbarColor.isChecked) {
                it.setSecondaryToolbarColor(secondaryToolbarColor)
            }
            if (binding.navigationBarColor.isChecked) {
                it.setNavigationBarColor(navigationBarColor)
            }
            it.setShowTitle(binding.showTitle.isChecked)
            if (binding.urlBarHiding.isChecked) {
                it.enableUrlBarHiding()
            }
            if (binding.colorScheme.isChecked) {
                it.setColorScheme(
                    when {
                        binding.colorSchemeLight.isChecked -> CustomTabsIntent.COLOR_SCHEME_LIGHT
                        binding.colorSchemeDark.isChecked -> CustomTabsIntent.COLOR_SCHEME_DARK
                        else -> CustomTabsIntent.COLOR_SCHEME_SYSTEM
                    }
                )
            }
            if (binding.colorSchemeParams.isChecked) {
                val scheme =
                    if (binding.colorSchemeParamsLight.isChecked) CustomTabsIntent.COLOR_SCHEME_LIGHT else CustomTabsIntent.COLOR_SCHEME_DARK
                val params = CustomTabColorSchemeParams.Builder().also { builder ->
                    if (binding.toolbarColorScheme.isChecked) {
                        builder.setToolbarColor(toolbarColorScheme)
                    }
                    if (binding.secondaryToolbarColorScheme.isChecked) {
                        builder.setSecondaryToolbarColor(secondaryToolbarColorScheme)
                    }
                    if (binding.navigationBarColorScheme.isChecked) {
                        builder.setNavigationBarColor(navigationBarColorScheme)
                    }
                }.build()
                it.setColorSchemeParams(scheme, params)
            }
            if (binding.closeButtonIcon.isChecked) {
                it.setCloseButtonIcon(getBitmap(R.drawable.ic_back))
            }
            if (binding.defaultShareMenuItem.isChecked) {
                it.addDefaultShareMenuItem()
            }
            if (binding.actionButton.isChecked) {
                val intent = Intent(this, LauncherActivity::class.java)
                intent.putExtra(EXTRA_MESSAGE, "Account clicked")
                val pendingIntent =
                    PendingIntent.getActivity(this, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                it.setActionButton(getBitmap(R.drawable.ic_account), "Account", pendingIntent, true)
            }
        }.build()
        customTabsIntent.intent.setPackage(intent.getStringExtra(EXTRA_PACKAGE_NAME))
        customTabsIntent.launchUrl(this, Uri.parse(binding.editText.text.toString()))
    }

    private fun getBitmap(drawableRes: Int): Bitmap =
        (ContextCompat.getDrawable(this, drawableRes) as BitmapDrawable).bitmap

    override fun onColorChooserResult(requestCode: Int, resultCode: Int, color: Int) {
        if (resultCode != RESULT_OK) return
        when (requestCode) {
            REQUEST_CODE_TOOLBAR ->
                setToolbarColor(color)
            REQUEST_CODE_SECONDARY_TOOLBAR -> {
                secondaryToolbarColor = color
                binding.secondaryToolbarColorSample.setBackgroundColor(color)
            }
            REQUEST_CODE_NAVIGATION_BAR -> {
                navigationBarColor = color
                binding.navigationBarColorSample.setBackgroundColor(color)
            }
            REQUEST_CODE_TOOLBAR_SCHEME ->
                setToolbarColorScheme(color)
            REQUEST_CODE_SECONDARY_TOOLBAR_SCHEME -> {
                secondaryToolbarColorScheme = color
                binding.secondaryToolbarColorSchemeSample.setBackgroundColor(color)
            }
            REQUEST_CODE_NAVIGATION_BAR_SCHEME -> {
                navigationBarColorScheme = color
                binding.navigationBarColorSchemeSample.setBackgroundColor(color)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setToolbarColor(color: Int) {
        toolbarColor = color
        binding.toolbarColorSample.setBackgroundColor(color)
        binding.toolbarColorDescription.text =
            "W: " + "%.2f".format(color.contrastToWhiteForeground()) + "\n" +
                    "B: " + "%.2f".format(color.contrastToBlackForeground())
    }

    @SuppressLint("SetTextI18n")
    private fun setToolbarColorScheme(color: Int) {
        toolbarColorScheme = color
        binding.toolbarColorSchemeSample.setBackgroundColor(color)
        binding.toolbarColorSchemeDescription.text =
            "W: " + "%.2f".format(color.contrastToWhiteForeground()) + "\n" +
                    "B: " + "%.2f".format(color.contrastToBlackForeground())
    }

    private fun mayLaunchUrl() {
        val urlList = listOf(SECOND_URL)
            .map { Uri.parse(it) }
            .map { makeOtherLikelyBundle(it) }
        CustomTabsHelper.session?.mayLaunchUrl(
            Uri.parse(binding.editText.text.toString()),
            null,
            urlList
        )
    }

    private fun makeOtherLikelyBundle(uri: Uri): Bundle =
        Bundle().also { it.putParcelable(CustomTabsService.KEY_URL, uri) }

    companion object {
        private const val REQUEST_CODE_TOOLBAR = 1
        private const val REQUEST_CODE_SECONDARY_TOOLBAR = 2
        private const val REQUEST_CODE_NAVIGATION_BAR = 3
        private const val REQUEST_CODE_TOOLBAR_SCHEME = 4
        private const val REQUEST_CODE_SECONDARY_TOOLBAR_SCHEME = 5
        private const val REQUEST_CODE_NAVIGATION_BAR_SCHEME = 6
        private const val DEFAULT_URL = "https://cs.android.com/"
        private const val SECOND_URL = "https://news.yahoo.co.jp/"
        private const val EXTRA_PACKAGE_NAME = "EXTRA_PACKAGE_NAME"
        private const val EXTRA_LABEL = "EXTRA_LABEL"
        private const val EXTRA_MESSAGE = "EXTRA_MESSAGE"

        fun start(context: Context, packageName: String, label: String) {
            Intent(context, LauncherActivity::class.java).also {
                it.putExtra(EXTRA_PACKAGE_NAME, packageName)
                it.putExtra(EXTRA_LABEL, label)
            }.let {
                context.startActivity(it)
            }
        }
    }
}
