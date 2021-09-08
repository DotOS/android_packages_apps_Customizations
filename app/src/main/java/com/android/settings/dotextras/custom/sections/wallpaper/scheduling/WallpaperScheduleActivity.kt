package com.android.settings.dotextras.custom.sections.wallpaper.scheduling

import android.app.ActivityThread
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.*
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.sections.wallpaper.TimePickerFragment
import com.android.settings.dotextras.custom.sections.wallpaper.setOnTime24PickedListener
import com.android.settings.dotextras.databinding.ActivityWallpaperScheduleBinding
import com.dot.ui.utils.serialize
import com.google.android.material.tabs.TabLayout
import java.time.Duration
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class WallpaperScheduleActivity : AppCompatActivity(), TabLayout.OnTabSelectedListener {

    private lateinit var workManager: WorkManager
    private lateinit var binding: ActivityWallpaperScheduleBinding
    private var routine = 0
    private var wallpaperUris = ArrayList<String>()
    private var adapter: WallpaperPackAdapter? = null

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            Log.d("DotWorker", "Image Added")
            if (wallpaperUris.add(uri.toString())) {
                binding.schedStart.isEnabled = wallpaperUris.size > 1
                if (adapter != null) {
                    adapter!!.updateWallpapers(wallpaperUris)
                    if (routine == 0 && wallpaperUris.size > 1) {
                        if (removeHeader()) {
                            val singleWallpaper = wallpaperUris[0]
                            wallpaperUris.clear()
                            wallpaperUris.add(singleWallpaper)
                            adapter!!.updateWallpapers(wallpaperUris)
                        }
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWallpaperScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        workManager = WorkManager.getInstance(ActivityThread.currentApplication())
        lifecycleScope.launchWhenCreated {
            with(binding) {
                val context = this@WallpaperScheduleActivity
                wallpaperUris.add("%HEADER%")
                schedWallpaper.canGoBack(context)
                schedStart.isEnabled = wallpaperUris.size > 1
                adapter = WallpaperPackAdapter(
                    wallpaperUris,
                    context,
                    object : WallpaperPackAdapter.Callback {
                        override fun addNewImage(adapter: WallpaperPackAdapter) {
                            getContent.launch("image/*")
                        }
                        override fun onImageDeleted(adapter: WallpaperPackAdapter, uri: Uri) {
                            if (wallpaperUris.removeIf { uri.toString() == it }) {
                                adapter.updateWallpapers(wallpaperUris)
                                schedStart.isEnabled = wallpaperUris.size > 1
                                if (wallpaperUris.isEmpty()) {
                                    addHeader()
                                }
                            }
                        }
                    })
                schedPickedRecycler.adapter = adapter
                schedPickedRecycler.layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                var currentDate = Calendar.getInstance()
                val dueDate = Calendar.getInstance()
                val timePicker = TimePickerFragment.newInstance()
                var timePreview: TextView? = null
                schedTimePref.post {
                    timePreview = schedTimePref.widgetFrame!!.findViewById(R.id.datePreview)
                    timePreview?.text =
                        "${currentDate.get(Calendar.HOUR_OF_DAY)}:${currentDate.get(Calendar.MINUTE)}"
                }
                timePicker.setInitialTime24(
                    currentDate.get(Calendar.HOUR_OF_DAY),
                    currentDate.get(Calendar.MINUTE)
                )
                val timeDiff = dueDate.timeInMillis - currentDate.timeInMillis
                schedDelayInfo.isVisible = timeDiff < standardDelay
                timePicker.setOnTime24PickedListener {
                    dueDate.set(Calendar.HOUR_OF_DAY, it.hour)
                    dueDate.set(Calendar.MINUTE, it.minute)
                    timePreview?.let { preview ->
                        preview.text = "${it.hour}:${it.minute}"
                    }
                }
                schedTimePref.setOnClickPreference {
                    timePicker.show(supportFragmentManager, "timePick")
                }
                dueDate.set(Calendar.SECOND, 0)
                schedRoutine.addOnTabSelectedListener(context)

                schedStart.setOnClickListener {
                    currentDate = Calendar.getInstance()
                    timePicker.setInitialTime24(
                        currentDate.get(Calendar.HOUR_OF_DAY),
                        currentDate.get(Calendar.MINUTE)
                    )
                    if (dueDate.before(currentDate)) {
                        dueDate.add(Calendar.HOUR_OF_DAY, 24)
                    }
                    var timeDiff = dueDate.timeInMillis - currentDate.timeInMillis
                    val wallpaperPack = adapter!!.buildPack()
                    wallpaperPack.scheduledMode = getRoutineMode()
                    wallpaperUris.removeIf { it == "%HEADER%" }
                    wallpaperPack.wallpapers = wallpaperUris
                    wallpaperPack.scheduledTime = WallpaperPack.ScheduledTime(
                        dueDate.get(Calendar.HOUR_OF_DAY),
                        dueDate.get(Calendar.MINUTE)
                    )
                    SchedPrefs.deletePack(context)
                    SchedPrefs.applyPack(context, wallpaperPack.serialize())
                    if (routine == 0) {
                        if (timeDiff < standardDelay) {
                            timeDiff += standardDelay - timeDiff
                        }
                        workManager.enqueue(getOneTimeWorker(timeDiff))
                    } else {
                        workManager.enqueue(getPeriodicTimeWorker(timeDiff, routine.toLong()))
                    }
                    Toast.makeText(context, "Time remaining until first change : ${hms(timeDiff)}", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun addHeader() {
        wallpaperUris.add(0, "%HEADER%")
    }

    private fun removeHeader(): Boolean = wallpaperUris.removeIf { it == "%HEADER%" }

    private fun ArrayList<String>.addIfNew(uri: Uri): Boolean {
        if (!contains(uri.toString())) {
            add(uri.toString())
            return true
        }
        return false
    }

    private fun hms(millis: Long): String = String.format(
        "%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
        TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(
            TimeUnit.MILLISECONDS.toHours(
                millis
            )
        ),
        TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(
            TimeUnit.MILLISECONDS.toMinutes(
                millis
            )
        )
    )

    private fun getPeriodicTimeWorker(timeDiff: Long, interval: Long): PeriodicWorkRequest {
        val work = PeriodicWorkRequestBuilder<WallpaperWorker>(Duration.ofDays(interval))
            .setInitialDelay(Duration.ofMillis(timeDiff))
            .addTag("WallpaperScheduler")
            .setInputData(passWallpapers())
            .build()
        workManager.getWorkInfoByIdLiveData(work.id).observeForever {
            if (it != null) {
                Log.d("DotWorker", "Status changed to ${it.state}")
            }
        }
        return work
    }

    private val standardDelay = 15 * 60000 /* 15 min */

    private fun getOneTimeWorker(timeDiff: Long): OneTimeWorkRequest {
        val work = OneTimeWorkRequestBuilder<WallpaperWorker>()
            .setInitialDelay(Duration.ofMillis(timeDiff))
            .addTag("WallpaperScheduler")
            .setInputData(passWallpapers())
            .build()
        workManager.getWorkInfoByIdLiveData(work.id).observeForever {
            if (it != null) {
                Log.d("DotWorker", "Status changed to ${it.state}")
            }
        }
        return work
    }

    private fun passWallpapers(): Data {
        val builder = Data.Builder()
        builder.putStringArray("wallpapers", wallpaperUris.toTypedArray())
        return builder.build()
    }

    private fun getRoutineMode(): WallpaperPack.Mode {
        return when (routine) {
            0 -> WallpaperPack.Mode.ONCE
            1 -> WallpaperPack.Mode.DAILY
            7 -> WallpaperPack.Mode.WEEKLY
            else -> WallpaperPack.Mode.ONCE
        }
    }

    override fun onTabSelected(tab: TabLayout.Tab) {
        when (tab.position) {
            0 -> {
                routine = 0
                if (wallpaperUris.size > 1) {
                    if (removeHeader()) {
                        val singleWallpaper = wallpaperUris[0]
                        wallpaperUris.clear()
                        wallpaperUris.add(singleWallpaper)
                        adapter!!.updateWallpapers(wallpaperUris)
                    }
                }
            }
            1 -> {
                routine = 1
                if (!wallpaperUris.contains("%HEADER%")) {
                    addHeader()
                    adapter!!.updateWallpapers(wallpaperUris)
                }
            }
            2 -> {
                routine = 7
                if (!wallpaperUris.contains("%HEADER%")) {
                    addHeader()
                    adapter!!.updateWallpapers(wallpaperUris)
                }
            }
        }
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {
    }

    override fun onTabReselected(tab: TabLayout.Tab?) {
    }
}
