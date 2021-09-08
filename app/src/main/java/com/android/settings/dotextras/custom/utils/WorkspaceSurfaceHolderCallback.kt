/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.settings.dotextras.custom.utils

import android.content.Context
import android.os.Bundle
import android.os.Message
import android.os.RemoteException
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.utils.SurfaceViewUtils.createSurfaceViewRequest
import com.android.settings.dotextras.custom.utils.SurfaceViewUtils.getCallback
import com.android.settings.dotextras.custom.utils.SurfaceViewUtils.getSurfacePackage

/** A surface holder callback that renders user's workspace on the passed in surface view.  */
open class WorkspaceSurfaceHolderCallback(
    private val mWorkspaceSurface: SurfaceView,
    context: Context,
) : SurfaceHolder.Callback {
    private val mPreviewUtils: PreviewUtils = PreviewUtils(
        context,
        context.getString(R.string.grid_control_metadata_name)
    )
    private var mLastSurface: Surface? = null
    private var mCallback: Message? = null
    private var mNeedsToCleanUp = false
    override fun surfaceCreated(holder: SurfaceHolder) {
        if (mPreviewUtils.supportsPreview() && mLastSurface !== holder.surface) {
            mLastSurface = holder.surface
            val result = renderPreview(mWorkspaceSurface)
            if (result != null) {
                mWorkspaceSurface.setChildSurfacePackage(
                    getSurfacePackage(result)!!
                )
                mCallback = getCallback(result)
                if (mNeedsToCleanUp) {
                    cleanUp()
                }
            }
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
    override fun surfaceDestroyed(holder: SurfaceHolder) {}
    fun cleanUp() {
        if (mCallback != null) {
            try {
                mCallback!!.replyTo.send(mCallback)
            } catch (e: RemoteException) {
                e.printStackTrace()
            } finally {
                mCallback = null
            }
        } else {
            mNeedsToCleanUp = true
        }
    }

    fun resetLastSurface() {
        mLastSurface = null
    }

    protected open fun renderPreview(workspaceSurface: SurfaceView?): Bundle? {
        return mPreviewUtils.renderPreview(
            createSurfaceViewRequest(workspaceSurface!!)
        )
    }

}