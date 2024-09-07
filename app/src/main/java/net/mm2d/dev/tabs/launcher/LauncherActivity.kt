/*
 * Copyright (c) 2020 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dev.tabs.launcher

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.RemoteViews
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsService
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.snackbar.Snackbar
import net.mm2d.color.chooser.ColorChooserDialog
import net.mm2d.dev.tabs.launcher.databinding.ActivityLauncherBinding

class LauncherActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLauncherBinding
    private var toolbarColor: Int = Color.WHITE
    private var secondaryToolbarColor: Int = Color.WHITE
    private var navigationBarColor: Int = Color.BLACK
    private var toolbarColorScheme: Int = Color.BLACK
    private var secondaryToolbarColorScheme: Int = Color.BLACK
    private var navigationBarColorScheme: Int = Color.BLACK

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLauncherBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
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
        ColorChooserDialog.registerListener(this, REQUEST_KEY_TOOLBAR, {
            setToolbarColor(it)
        }, null)
        binding.toolbarColorSample.setOnClickListener {
            ColorChooserDialog.show(
                this,
                REQUEST_KEY_TOOLBAR,
                toolbarColor,
            )
        }
        binding.secondaryToolbarColorSample.setBackgroundColor(secondaryToolbarColor)
        ColorChooserDialog.registerListener(this, REQUEST_KEY_SECONDARY_TOOLBAR, {
            secondaryToolbarColor = it
            binding.secondaryToolbarColorSample.setBackgroundColor(it)
        }, null)
        binding.secondaryToolbarColorSample.setOnClickListener {
            ColorChooserDialog.show(
                this,
                REQUEST_KEY_SECONDARY_TOOLBAR,
                secondaryToolbarColor,
            )
        }
        binding.navigationBarColorSample.setBackgroundColor(navigationBarColor)
        ColorChooserDialog.registerListener(this, REQUEST_KEY_NAVIGATION_BAR, {
            navigationBarColor = it
            binding.navigationBarColorSample.setBackgroundColor(it)
        }, null)
        binding.navigationBarColorSample.setOnClickListener {
            ColorChooserDialog.show(
                this,
                REQUEST_KEY_NAVIGATION_BAR,
                navigationBarColor,
            )
        }
        setToolbarColorScheme(toolbarColorScheme)
        ColorChooserDialog.registerListener(this, REQUEST_KEY_TOOLBAR_SCHEME, {
            setToolbarColorScheme(it)
        }, null)
        binding.toolbarColorSchemeSample.setOnClickListener {
            ColorChooserDialog.show(
                this,
                REQUEST_KEY_TOOLBAR_SCHEME,
                toolbarColorScheme,
            )
        }
        binding.secondaryToolbarColorSchemeSample.setBackgroundColor(secondaryToolbarColorScheme)
        ColorChooserDialog.registerListener(this, REQUEST_KEY_SECONDARY_TOOLBAR_SCHEME, {
            secondaryToolbarColorScheme = it
            binding.secondaryToolbarColorSchemeSample.setBackgroundColor(it)
        }, null)
        binding.secondaryToolbarColorSchemeSample.setOnClickListener {
            ColorChooserDialog.show(
                this,
                REQUEST_KEY_SECONDARY_TOOLBAR_SCHEME,
                secondaryToolbarColorScheme,
            )
        }
        binding.navigationBarColorSchemeSample.setBackgroundColor(navigationBarColorScheme)
        ColorChooserDialog.registerListener(this, REQUEST_KEY_NAVIGATION_BAR_SCHEME, {
            navigationBarColorScheme = it
            binding.navigationBarColorSchemeSample.setBackgroundColor(it)
        }, null)
        binding.navigationBarColorSchemeSample.setOnClickListener {
            ColorChooserDialog.show(
                this,
                REQUEST_KEY_NAVIGATION_BAR_SCHEME,
                navigationBarColorScheme,
            )
        }
        binding.partialCustomTabBehavior.adapter = ArrayAdapter.createFromResource(
            this,
            R.array.resize_behavior,
            android.R.layout.simple_spinner_item,
        ).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
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
            "$message clicked\non ${intent.dataString}"
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
        builder.setDefaultColorSchemeParams(
            CustomTabColorSchemeParams.Builder().also {
                if (binding.toolbarColor.isChecked) {
                    it.setToolbarColor(toolbarColor)
                }
                if (binding.secondaryToolbarColor.isChecked) {
                    it.setSecondaryToolbarColor(secondaryToolbarColor)
                }
                if (binding.navigationBarColor.isChecked) {
                    it.setNavigationBarColor(navigationBarColor)
                }
            }.build(),
        )
        builder.setShowTitle(binding.showTitle.isChecked)
        builder.setUrlBarHidingEnabled(binding.urlBarHiding.isChecked)
        if (binding.colorScheme.isChecked) {
            builder.setColorScheme(
                when {
                    binding.colorSchemeLight.isChecked -> CustomTabsIntent.COLOR_SCHEME_LIGHT
                    binding.colorSchemeDark.isChecked -> CustomTabsIntent.COLOR_SCHEME_DARK
                    else -> CustomTabsIntent.COLOR_SCHEME_SYSTEM
                },
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
            builder.setActionButton(
                getBitmap(R.drawable.ic_account),
                "Account",
                PendingIntent.getActivity(
                    this,
                    REQUEST_CODE_ACTION_BUTTON,
                    Intent(this, LauncherActivity::class.java).also {
                        it.putExtra(EXTRA_MESSAGE, "Account")
                    },
                    FLAGS,
                ),
                true,
            )
        }
        if (binding.menuItem.isChecked) {
            builder.addMenuItem(
                "MenuItem",
                PendingIntent.getActivity(
                    this,
                    REQUEST_CODE_MENU_ITEM,
                    Intent(this, LauncherActivity::class.java).also {
                        it.putExtra(EXTRA_MESSAGE, "MenuItem")
                    },
                    FLAGS,
                ),
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
                    FLAGS,
                ),
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
                    FLAGS,
                ),
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
                    FLAGS,
                ),
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
                    FLAGS,
                ),
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
                    FLAGS,
                ),
            )
        }
        if (binding.partialCustomTab.isChecked) {
            builder.setInitialActivityHeightPx(
                resources.getDimensionPixelSize(R.dimen.initial_activity_height),
                binding.partialCustomTabBehavior.selectedItemPosition.coerceIn(0..2),
            )
        }
    }

    private fun getBitmap(drawableRes: Int): Bitmap =
        (ContextCompat.getDrawable(this, drawableRes) as BitmapDrawable).bitmap

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
            urlList,
        )
    }

    private fun makeOtherLikelyBundle(uri: Uri): Bundle =
        Bundle().also { it.putParcelable(CustomTabsService.KEY_URL, uri) }

    companion object {
        private const val PREFIX = "LauncherActivity"
        private const val REQUEST_KEY_TOOLBAR =
            PREFIX + "REQUEST_KEY_TOOLBAR"
        private const val REQUEST_KEY_SECONDARY_TOOLBAR =
            PREFIX + "REQUEST_KEY_SECONDARY_TOOLBAR"
        private const val REQUEST_KEY_NAVIGATION_BAR =
            PREFIX + "REQUEST_KEY_NAVIGATION_BAR"
        private const val REQUEST_KEY_TOOLBAR_SCHEME =
            PREFIX + "REQUEST_KEY_TOOLBAR_SCHEME"
        private const val REQUEST_KEY_SECONDARY_TOOLBAR_SCHEME =
            PREFIX + "REQUEST_KEY_SECONDARY_TOOLBAR_SCHEME"
        private const val REQUEST_KEY_NAVIGATION_BAR_SCHEME =
            PREFIX + "REQUEST_KEY_NAVIGATION_BAR_SCHEME"

        private const val DEFAULT_URL = "https://www.bing.com/"
        private const val SECOND_URL = "https://www.bing.com/"
        private const val EXTRA_PACKAGE_NAME = "EXTRA_PACKAGE_NAME"
        private const val EXTRA_LABEL = "EXTRA_LABEL"
        private const val EXTRA_MESSAGE = "EXTRA_MESSAGE"
        private const val MESSAGE_SECONDARY_TOOLBAR = "SecondaryToolbar"
        private const val REQUEST_CODE_ACTION_BUTTON = 100
        private const val REQUEST_CODE_MENU_ITEM = 101
        private const val REQUEST_CODE_TOOLBAR_ITEM1 = 102
        private const val REQUEST_CODE_TOOLBAR_ITEM2 = 103
        private const val REQUEST_CODE_TOOLBAR_ITEM3 = 104
        private const val REQUEST_CODE_TOOLBAR_ITEM4 = 105
        private const val REQUEST_CODE_SECONDARY_TOOLBAR = 106
        private val FLAGS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

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
