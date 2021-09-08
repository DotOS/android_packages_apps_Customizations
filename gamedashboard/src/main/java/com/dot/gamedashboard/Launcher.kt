package com.dot.gamedashboard

import android.annotation.SuppressLint
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dot.gamedashboard.databinding.GamingDashboardLayoutBinding
import com.dot.gamedashboard.fragments.AddAppSheet
import com.dot.gamedashboard.fragments.DeleteAppSheet
import com.dot.ui.utils.ResourceHelper
import com.google.android.material.tabs.TabLayout
import java.io.Serializable
import com.dot.gamedashboard.bubble.Settings as BubbleSettings

class Launcher : AppCompatActivity(), DeleteAppSheet.Callback {

    private var mGamingPackages: MutableMap<String, Package>? = null
    private var mGamingPackagesArray = ArrayList<Package>()
    private var mGamingPackageList: String? = null
    private lateinit var binding: GamingDashboardLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mGamingPackages = HashMap()
        binding = GamingDashboardLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        lifecycleScope.launchWhenCreated {
            with(binding) {
                gameToolbar.canGoBack(this@Launcher)
                gamesPicker.setOnClickPreference {
                    AddAppSheet.newInstance(object : AddAppSheet.Callback {
                        override fun onAdd(items: ArrayList<Package>) {
                            for (pkg in items) {
                                mGamingPackages?.put(pkg.name!!, pkg)
                                savePackageList(mGamingPackages!!)
                            }
                        }
                        override fun onDismiss() {
                            updateGames()
                        }
                    }).show(supportFragmentManager, "addGames")
                }
                if (!ResourceHelper.hasHardwareKeys(this@Launcher)) gHardwareKeys.visibility = View.GONE

                var showScreenshot = BubbleSettings().isEnabled(this@Launcher, BubbleSettings.PREF_SHOW_SCREENSHOT)
                qcScreenshotTitle.isSelected = showScreenshot
                qcScreenshotImg.isSelected = showScreenshot
                qcActionScreenshot.setOnClickListener {
                    BubbleSettings().apply(this@Launcher, BubbleSettings.PREF_SHOW_SCREENSHOT, !showScreenshot)
                    showScreenshot = BubbleSettings().isEnabled(this@Launcher, BubbleSettings.PREF_SHOW_SCREENSHOT)
                    qcScreenshotTitle.isSelected = showScreenshot
                    qcScreenshotImg.isSelected = showScreenshot
                }
                var showRecord = BubbleSettings().isEnabled(this@Launcher, BubbleSettings.PREF_SHOW_SCREENRECORD)
                qcRecordTitle.isSelected = showRecord
                qcRecordImg.isSelected = showRecord
                qcActionRecord.setOnClickListener {
                    BubbleSettings().apply(this@Launcher, BubbleSettings.PREF_SHOW_SCREENRECORD, !showRecord)
                    showRecord = BubbleSettings().isEnabled(this@Launcher, BubbleSettings.PREF_SHOW_SCREENRECORD)
                    qcRecordTitle.isSelected = showRecord
                    qcRecordImg.isSelected = showRecord
                }
                var showDnd = BubbleSettings().isEnabled(this@Launcher, BubbleSettings.PREF_SHOW_DND)
                qcDndTitle.isSelected = showDnd
                qcDndImg.isSelected = showDnd
                qcActionDnd.setOnClickListener {
                    BubbleSettings().apply(this@Launcher, BubbleSettings.PREF_SHOW_DND, !showDnd)
                    showDnd = BubbleSettings().isEnabled(this@Launcher, BubbleSettings.PREF_SHOW_DND)
                    qcDndTitle.isSelected = showDnd
                    qcDndImg.isSelected = showDnd
                }
                for (i in 0 until ringerModeSelector.tabCount)
                    if (Settings.System.getInt(contentResolver, "gaming_mode_ringer_mode", 0) == i) {
                        ringerModeSelector.selectTab(ringerModeSelector.getTabAt(i))
                    }
                ringerModeSelector.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
                    override fun onTabSelected(tab: TabLayout.Tab) {
                        Settings.System.putInt(contentResolver, "gaming_mode_ringer_mode", tab.position)
                    }
                    override fun onTabUnselected(tab: TabLayout.Tab?) {}
                    override fun onTabReselected(tab: TabLayout.Tab?) {}
                })
                for (i in 0 until notificationModeSelector.tabCount)
                    if (Settings.System.getInt(contentResolver, "gaming_mode_notifications", 3) == i) {
                        notificationModeSelector.selectTab(notificationModeSelector.getTabAt(i))
                    }
                notificationModeSelector.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
                    override fun onTabSelected(tab: TabLayout.Tab) {
                        Settings.System.putInt(contentResolver, "gaming_mode_notifications", tab.position)
                    }
                    override fun onTabUnselected(tab: TabLayout.Tab?) {}
                    override fun onTabReselected(tab: TabLayout.Tab?) {}
                })
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateGames()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateGames() {
        binding.root.post {
            if (parsePackageList()) {
                if (binding.gamesRecycler.adapter == null) {
                    binding.gamesRecycler.adapter =
                        GamingAppsAdapter(mGamingPackagesArray, this, this)
                    binding.gamesRecycler.layoutManager = LinearLayoutManager(this)
                } else
                    binding.gamesRecycler.adapter!!.notifyDataSetChanged()
            }
        }
    }

    private fun parsePackageList(): Boolean {
        var parsed = false
        var gamingModeString: String? =
            Settings.System.getString(contentResolver, "gaming_mode_values")
        if (gamingModeString == null) gamingModeString = ""
        if (!TextUtils.equals(mGamingPackageList, gamingModeString)) {
            mGamingPackageList = gamingModeString
            mGamingPackages?.clear()
            mGamingPackages?.let { parseAndAddToMap(gamingModeString, it) }
            parsed = true
        }
        return parsed
    }

    private fun parseAndAddToMap(baseString: String?, map: MutableMap<String, Package>) {
        if (baseString == null) {
            return
        }
        mGamingPackagesArray.clear()
        val array = TextUtils.split(baseString, "\\|")
        for (item in array) {
            if (TextUtils.isEmpty(item)) {
                continue
            }
            val pkg: Package? = Package.fromString(item)
            if (pkg != null) {
                map[pkg.name!!] = pkg
                mGamingPackagesArray.add(pkg)
            }
        }
    }

    private fun savePackageList(map: Map<String, Package>) {
        val setting: String =
            if (map === mGamingPackages) "gaming_mode_values" else "gaming_mode_dummy"
        val settings: MutableList<String?> = ArrayList()
        for (app in map.values) settings.add(app.toString())
        val value = TextUtils.join("|", settings)
        Settings.System.putString(contentResolver, setting, value)
    }

    class Package(var name: String?) : Serializable {

        override fun toString(): String {
            val builder = StringBuilder()
            builder.append(name)
            return builder.toString()
        }

        companion object {
            fun fromString(value: String?): Package? {
                return if (TextUtils.isEmpty(value)) {
                    null
                } else try {
                    Package(value)
                } catch (e: NumberFormatException) {
                    null
                }
            }
        }
    }

    override fun onRemove(pkg: Package) {
        mGamingPackages?.let {
            if (it.remove(pkg.name) != null) {
                mGamingPackagesArray.remove(pkg)
                savePackageList(it)
            }
        }
    }

    override fun onDismiss() {
        updateGames()
    }

}