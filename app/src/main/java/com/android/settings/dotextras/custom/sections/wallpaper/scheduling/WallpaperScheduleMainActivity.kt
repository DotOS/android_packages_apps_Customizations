package com.android.settings.dotextras.custom.sections.wallpaper.scheduling

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.WorkManager
import com.android.settings.dotextras.databinding.ActivityWallpaperScheduleListBinding

class WallpaperScheduleMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWallpaperScheduleListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWallpaperScheduleListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        lifecycleScope.launchWhenCreated {
            update()
        }
        lifecycleScope.launchWhenResumed {
            update()
        }
    }

    fun update() {
        with(binding) {
            val context = this@WallpaperScheduleMainActivity
            schedWallpaper.canGoBack(context)
            val schedExists = SchedPrefs.exists(context)
            updateVisibility()
            schedCancel.setOnClickListener {
                WorkManager.getInstance(context).cancelAllWorkByTag("WallpaperScheduler")
                SchedPrefs.deletePack(context)
                updateVisibility()
            }
            fabSchedNew.setOnClickListener {
                startActivity(Intent(context, WallpaperScheduleActivity::class.java))
            }
            schedNew.setOnClickListener {
                startActivity(Intent(context, WallpaperScheduleActivity::class.java))
            }
            if (schedExists) {
                val wallpaperPack = SchedPrefs.getPack(context)!!
                val wallpapers = wallpaperPack.wallpapers
                val adapter = WallpaperPreviewAdapter(wallpapers, context)
                schedPreviewRecycler.adapter = adapter
                schedPreviewRecycler.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                val routineText: String = when(wallpaperPack.scheduledMode) {
                    WallpaperPack.Mode.ONCE -> "One time"
                    WallpaperPack.Mode.DAILY -> "Daily"
                    WallpaperPack.Mode.WEEKLY -> "Weekly"
                    null -> "Once"
                }
                schedInfo.post {
                    schedInfo.title = "$routineText Schedule - ${wallpapers.size} Wallpaper(s)"
                    schedInfo.summary = "Changes at ${wallpaperPack.scheduledTime!!.hour}:${wallpaperPack.scheduledTime!!.minute}"
                }
            }
        }
    }

    private fun updateVisibility() {
        with(binding) {
            val schedExists = SchedPrefs.exists(this@WallpaperScheduleMainActivity)
            schedNewLayout.isVisible = !schedExists
            fabSchedNew.isVisible = schedExists
            schedRoot.isVisible = schedExists
        }
    }

}