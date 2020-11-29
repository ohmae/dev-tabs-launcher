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
import android.widget.RemoteViews
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
            ColorChooserDialog.show(
                this,
                COLOR_REQUEST_CODE_TOOLBAR,
                toolbarColor
            )
        }
        binding.secondaryToolbarColorSample.setBackgroundColor(secondaryToolbarColor)
        binding.secondaryToolbarColorSample.setOnClickListener {
            ColorChooserDialog.show(
                this,
                COLOR_REQUEST_CODE_SECONDARY_TOOLBAR,
                secondaryToolbarColor
            )
        }
        binding.navigationBarColorSample.setBackgroundColor(navigationBarColor)
        binding.navigationBarColorSample.setOnClickListener {
            ColorChooserDialog.show(
                this,
                COLOR_REQUEST_CODE_NAVIGATION_BAR,
                navigationBarColor
            )
        }
        setToolbarColorScheme(toolbarColorScheme)
        binding.toolbarColorSchemeSample.setOnClickListener {
            ColorChooserDialog.show(
                this,
                COLOR_REQUEST_CODE_TOOLBAR_SCHEME,
                toolbarColorScheme
            )
        }
        binding.secondaryToolbarColorSchemeSample.setBackgroundColor(secondaryToolbarColorScheme)
        binding.secondaryToolbarColorSchemeSample.setOnClickListener {
            ColorChooserDialog.show(
                this,
                COLOR_REQUEST_CODE_SECONDARY_TOOLBAR_SCHEME,
                secondaryToolbarColorScheme
            )
        }
        binding.navigationBarColorSchemeSample.setBackgroundColor(navigationBarColorScheme)
        binding.navigationBarColorSchemeSample.setOnClickListener {
            ColorChooserDialog.show(
                this,
                COLOR_REQUEST_CODE_NAVIGATION_BAR_SCHEME,
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
        val text = if (message == MESSAGE_SECONDARY_TOOLBAR) {
            val button =
                when (intent.getIntExtra(CustomTabsIntent.EXTRA_REMOTEVIEWS_CLICKED_ID, 0)) {
                    R.id.button1 -> "button1"
                    R.id.button2 -> "button2"
                    R.id.button3 -> "button3"
                    R.id.button4 -> "button4"
                    else -> "unknown"
                }
            "$message $button clicked\non ${intent.dataString}"
        } else {
            message
        }
        Snackbar.make(binding.root, text, Snackbar.LENGTH_INDEFINITE).also { bar ->
            bar.setAction("OK") { bar.dismiss() }
        }.show()
    }

    private fun launch() {
        val customTabsIntent = CustomTabsIntent.Builder(CustomTabsHelper.session).also {
            setUpCustomTabsIntent(it)
        }.build()
        customTabsIntent.intent.setPackage(intent.getStringExtra(EXTRA_PACKAGE_NAME))
        customTabsIntent.launchUrl(this, Uri.parse(binding.editText.text.toString()))
    }

    private fun setUpCustomTabsIntent(builder: CustomTabsIntent.Builder) {
        if (binding.toolbarColor.isChecked) {
            builder.setToolbarColor(toolbarColor)
        }
        if (binding.secondaryToolbarColor.isChecked) {
            builder.setSecondaryToolbarColor(secondaryToolbarColor)
        }
        if (binding.navigationBarColor.isChecked) {
            builder.setNavigationBarColor(navigationBarColor)
        }
        builder.setShowTitle(binding.showTitle.isChecked)
        if (binding.urlBarHiding.isChecked) {
            builder.enableUrlBarHiding()
        }
        if (binding.colorScheme.isChecked) {
            builder.setColorScheme(
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
            val params = CustomTabColorSchemeParams.Builder().also {
                if (binding.toolbarColorScheme.isChecked) {
                    it.setToolbarColor(toolbarColorScheme)
                }
                if (binding.secondaryToolbarColorScheme.isChecked) {
                    it.setSecondaryToolbarColor(secondaryToolbarColorScheme)
                }
                if (binding.navigationBarColorScheme.isChecked) {
                    it.setNavigationBarColor(navigationBarColorScheme)
                }
            }.build()
            builder.setColorSchemeParams(scheme, params)
        }
        if (binding.closeButtonIcon.isChecked) {
            builder.setCloseButtonIcon(getBitmap(R.drawable.ic_back))
        }
        if (binding.defaultShareMenuItem.isChecked) {
            builder.addDefaultShareMenuItem()
        }
        if (binding.startAnimation.isChecked) {
            builder.setStartAnimations(this, R.anim.slide_in_right, R.anim.slide_out_left)
        }
        if (binding.exitAnimation.isChecked) {
            builder.setExitAnimations(this, R.anim.slide_in_left, R.anim.slide_out_right)
        }
        if (binding.actionButton.isChecked) {
            val intent = Intent(this, LauncherActivity::class.java)
            intent.putExtra(EXTRA_MESSAGE, "Account")
            val pendingIntent =
                PendingIntent.getActivity(
                    this,
                    REQUEST_CODE_ACTION_BUTTON,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            builder.setActionButton(
                getBitmap(R.drawable.ic_account),
                "Account",
                pendingIntent,
                true
            )
        }
        if (binding.toolbarItem.isChecked) {
            builder.addToolbarItem(
                R.id.button1,
                getBitmap(R.drawable.ic_1),
                "button1",
                PendingIntent.getActivity(
                    this,
                    REQUEST_CODE_TOOLBAR_ITEM1,
                    Intent(this, LauncherActivity::class.java).also {
                        it.putExtra(EXTRA_MESSAGE, "button1")
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
            builder.addToolbarItem(
                R.id.button2,
                getBitmap(R.drawable.ic_2),
                "button2",
                PendingIntent.getActivity(
                    this,
                    REQUEST_CODE_TOOLBAR_ITEM2,
                    Intent(this, LauncherActivity::class.java).also {
                        it.putExtra(EXTRA_MESSAGE, "button2")
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
            builder.addToolbarItem(
                R.id.button3,
                getBitmap(R.drawable.ic_3),
                "button3",
                PendingIntent.getActivity(
                    this,
                    REQUEST_CODE_TOOLBAR_ITEM3,
                    Intent(this, LauncherActivity::class.java).also {
                        it.putExtra(EXTRA_MESSAGE, "button3")
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
            builder.addToolbarItem(
                R.id.button4,
                getBitmap(R.drawable.ic_4),
                "button4",
                PendingIntent.getActivity(
                    this,
                    REQUEST_CODE_TOOLBAR_ITEM4,
                    Intent(this, LauncherActivity::class.java).also {
                        it.putExtra(EXTRA_MESSAGE, "button4")
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
        }
        if (binding.secondaryToolbar.isChecked) {
            val remoteViews = RemoteViews(packageName, R.layout.secondary_toolbar)
            builder.setSecondaryToolbarViews(
                remoteViews,
                intArrayOf(R.id.button1, R.id.button2, R.id.button3, R.id.button4),
                PendingIntent.getActivity(
                    this,
                    REQUEST_CODE_SECONDARY_TOOLBAR,
                    Intent(this, LauncherActivity::class.java).also {
                        it.putExtra(EXTRA_MESSAGE, MESSAGE_SECONDARY_TOOLBAR)
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
        }
    }

    private fun getBitmap(drawableRes: Int): Bitmap =
        (ContextCompat.getDrawable(this, drawableRes) as BitmapDrawable).bitmap

    override fun onColorChooserResult(requestCode: Int, resultCode: Int, color: Int) {
        if (resultCode != RESULT_OK) return
        when (requestCode) {
            COLOR_REQUEST_CODE_TOOLBAR ->
                setToolbarColor(color)
            COLOR_REQUEST_CODE_SECONDARY_TOOLBAR -> {
                secondaryToolbarColor = color
                binding.secondaryToolbarColorSample.setBackgroundColor(color)
            }
            COLOR_REQUEST_CODE_NAVIGATION_BAR -> {
                navigationBarColor = color
                binding.navigationBarColorSample.setBackgroundColor(color)
            }
            COLOR_REQUEST_CODE_TOOLBAR_SCHEME ->
                setToolbarColorScheme(color)
            COLOR_REQUEST_CODE_SECONDARY_TOOLBAR_SCHEME -> {
                secondaryToolbarColorScheme = color
                binding.secondaryToolbarColorSchemeSample.setBackgroundColor(color)
            }
            COLOR_REQUEST_CODE_NAVIGATION_BAR_SCHEME -> {
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
        private const val COLOR_REQUEST_CODE_TOOLBAR = 1
        private const val COLOR_REQUEST_CODE_SECONDARY_TOOLBAR = 2
        private const val COLOR_REQUEST_CODE_NAVIGATION_BAR = 3
        private const val COLOR_REQUEST_CODE_TOOLBAR_SCHEME = 4
        private const val COLOR_REQUEST_CODE_SECONDARY_TOOLBAR_SCHEME = 5
        private const val COLOR_REQUEST_CODE_NAVIGATION_BAR_SCHEME = 6
        private const val DEFAULT_URL = "https://cs.android.com/"
        private const val SECOND_URL = "https://news.yahoo.co.jp/"
        private const val EXTRA_PACKAGE_NAME = "EXTRA_PACKAGE_NAME"
        private const val EXTRA_LABEL = "EXTRA_LABEL"
        private const val EXTRA_MESSAGE = "EXTRA_MESSAGE"
        private const val MESSAGE_SECONDARY_TOOLBAR = "SecondaryToolbar"
        private const val REQUEST_CODE_ACTION_BUTTON = 100
        private const val REQUEST_CODE_TOOLBAR_ITEM1 = 101
        private const val REQUEST_CODE_TOOLBAR_ITEM2 = 102
        private const val REQUEST_CODE_TOOLBAR_ITEM3 = 103
        private const val REQUEST_CODE_TOOLBAR_ITEM4 = 104
        private const val REQUEST_CODE_SECONDARY_TOOLBAR = 105

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
