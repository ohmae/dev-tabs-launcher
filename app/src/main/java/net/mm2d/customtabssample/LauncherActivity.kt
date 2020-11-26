package net.mm2d.customtabssample

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsService
import androidx.core.content.ContextCompat
import net.mm2d.color.chooser.ColorChooserDialog
import net.mm2d.customtabssample.databinding.ActivityLauncherBinding

class LauncherActivity : AppCompatActivity(), ColorChooserDialog.Callback {
    private lateinit var binding: ActivityLauncherBinding
    private lateinit var browserPackage: String
    private var toolbarColor: Int = Color.WHITE
    private var secondaryToolbarColor: Int = Color.WHITE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLauncherBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        browserPackage = intent.getStringExtra(EXTRA_PACKAGE_NAME)!!
        supportActionBar?.let {
            it.title = intent.getStringExtra(EXTRA_LABEL)
            it.subtitle = browserPackage
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
        setSecondaryToolbarColor(secondaryToolbarColor)
        binding.secondaryToolbarColorSample.setOnClickListener {
            ColorChooserDialog.show(this, REQUEST_CODE_SECONDARY_TOOLBAR, secondaryToolbarColor)
        }
        CustomTabsHelper.bind(this, browserPackage)
    }

    override fun onDestroy() {
        super.onDestroy()
        CustomTabsHelper.unbind(this)
    }

    private fun launch() {
        val customTabsIntent = CustomTabsIntent.Builder(CustomTabsHelper.session).also {
            if (binding.toolbarColorSwitch.isChecked) {
                it.setToolbarColor(toolbarColor)
            }
            if (binding.secondaryToolbarColorSwitch.isChecked) {
                it.setSecondaryToolbarColor(secondaryToolbarColor)
            }
            it.setShowTitle(binding.showTitleSwitch.isChecked)
        }.build()
        customTabsIntent.intent.setPackage(browserPackage)
        customTabsIntent.launchUrl(this, Uri.parse(binding.editText.text.toString()))
    }

    override fun onColorChooserResult(requestCode: Int, resultCode: Int, color: Int) {
        if (resultCode != RESULT_OK) return
        if (requestCode == REQUEST_CODE_TOOLBAR) {
            setToolbarColor(color)
        } else if (requestCode == REQUEST_CODE_SECONDARY_TOOLBAR) {
            setSecondaryToolbarColor(color)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setToolbarColor(color: Int) {
        this.toolbarColor = color
        binding.toolbarColorSample.setBackgroundColor(color)
        binding.toolbarColorDescription.text =
            "W: " + "%.2f".format(color.contrastToWhiteForeground()) + "\n" +
                    "B: " + "%.2f".format(color.contrastToBlackForeground())
    }

    @SuppressLint("SetTextI18n")
    private fun setSecondaryToolbarColor(color: Int) {
        this.secondaryToolbarColor = color
        binding.secondaryToolbarColorSample.setBackgroundColor(color)
        binding.secondaryToolbarColorDescription.text =
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
        private const val DEFAULT_URL = "https://m.yahoo.co.jp/"
        private const val SECOND_URL = "https://news.yahoo.co.jp/"
        private const val EXTRA_PACKAGE_NAME = "EXTRA_PACKAGE_NAME"
        private const val EXTRA_LABEL = "EXTRA_LABEL"

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
