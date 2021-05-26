package com.dot.applock.model

import android.app.AppLockManager
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable

class AppModel(info: ResolveInfo, pm: PackageManager, am: AppLockManager) {

    var mLabel: String? = null
    var mPackageName: String? = null
    var mIcon: Drawable? = null
    var mAppLocked = false

    init {
        mLabel = info.loadLabel(pm).toString()
        mIcon = info.loadIcon(pm)
        mPackageName = info.activityInfo.packageName
        mAppLocked = am.isAppLocked(mPackageName)
    }

}