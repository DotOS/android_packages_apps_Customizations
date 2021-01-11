package com.android.settings.dotextras.custom.utils

import android.app.Service
import android.content.Intent
import android.os.IBinder
import java.io.File

class MaidService : Service() {

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        println("onTaskRemoved called")
        super.onTaskRemoved(rootIntent)
        val dir = applicationContext.cacheDir
        val children: Array<String>? = dir.list()
        if (children != null) {
            for (child in children) {
                if (child.endsWith(".jpeg") || child.endsWith(".jpg")) File(dir, child).delete()
            }
        }
        this.stopSelf()
    }

}