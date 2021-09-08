package com.android.settings.dotextras.custom.sections.wallpaper

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.sections.wallpaper.scheduling.WallpaperScheduleActivity
import com.android.settings.dotextras.custom.sections.wallpaper.scheduling.WallpaperScheduleMainActivity
import com.android.settings.dotextras.custom.utils.internetAvailable
import com.android.settings.dotextras.custom.views.WallpaperPreviewSystem
import com.android.settings.dotextras.databinding.ActivityWallpapersBinding

class WallpapersActivity: AppCompatActivity() {

    private lateinit var binding: ActivityWallpapersBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWallpapersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        lifecycleScope.launchWhenCreated {
            with(binding) {
                val context = this@WallpapersActivity
                wallToolbar.canGoBack(context)
                schedulingLaunch.setOnClickPreference {
                    startActivity(Intent(context, WallpaperScheduleMainActivity::class.java))
                }
                wpGallery.setOnClickPreference {
                    getContent.launch("image/*")
                }
                wpBuiltin.setOnClickPreference {
                    val intent = Intent(context, WallpaperPickActivity::class.java)
                    intent.putExtra("TYPE", WallpaperPickActivity.Types.TYPE_INCLUDED)
                    startActivity(intent)
                }
                wpLive.setOnClickPreference {
                    val intent = Intent(context, WallpaperPickActivity::class.java)
                    intent.putExtra("TYPE", WallpaperPickActivity.Types.TYPE_LIVE)
                    startActivity(intent)
                }
                wpDot.setOnClickPreference {
                    val intent = Intent(context, WallpaperPickActivity::class.java)
                    intent.putExtra("TYPE", WallpaperPickActivity.Types.TYPE_EXCLUSIVES)
                    if (context.internetAvailable())
                        startActivity(intent)
                    else
                        Toast.makeText(
                            context,
                            getString(R.string.no_internet),
                            Toast.LENGTH_SHORT
                        ).show()
                }
                WallpaperPreviewSystem(context,
                    previewContainerLauncher,
                    previewSurfaceLockscreen,
                    previewImageLockscreen)
            }
        }
    }

    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                val wallpaperGallery = Wallpaper()
                wallpaperGallery.uri = uri.toString()
                val intent = Intent(this, WallpaperApplyActivity::class.java)
                intent.putExtra("wallpaperObject", wallpaperGallery)
                startActivity(intent)
            }
        }
}