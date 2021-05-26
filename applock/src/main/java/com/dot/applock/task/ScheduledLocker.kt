package com.dot.applock.task

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.dot.applock.ResourceHelper

class ScheduledLocker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    override fun doWork(): Result {
        val applockManager = ResourceHelper.getAppLockManager(applicationContext)
        val packages = inputData.getStringArray("PACKAGES")
        return try {
            if (packages != null) {
                for (pckg in packages) {
                    if (!applockManager.lockedPackages.contains(pckg))
                        applockManager.addAppToList(pckg)
                }
            }
            Result.success()
        } catch (throwable: Throwable) {
            Result.failure()
        }
    }
}