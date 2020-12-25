package com.android.settings.dotextras.custom.sections.clock

import android.content.ContentResolver
import android.content.Context
import android.content.pm.ProviderInfo
import android.net.Uri
import android.os.AsyncTask
import android.util.Log
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.sections.clock.utils.ContentUriAsset
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

typealias OptionsFetchedListener = ((options: ArrayList<Clockface>?) -> Unit)?

class ContentProviderClockProvider(private val mContext: Context) : ClockProvider {

    private var mProviderInfo: ProviderInfo? = null
    private var mClocks: ArrayList<Clockface>? = null
    override val isAvailable: Boolean
        get() = (mProviderInfo != null && (mClocks == null || mClocks!!.isNotEmpty()))

    init {
        val providerAuthority = mContext.resources.getString(R.string.clocks_provider_authority)
        mProviderInfo = mContext.packageManager.resolveContentProvider(providerAuthority, 0)
    }

    override fun fetch(callback: OptionsFetchedListener, reload: Boolean) {
        if (mClocks != null && !reload) {
            if (callback != null) {
                if (mClocks!!.isNotEmpty()) {
                    callback.invoke(mClocks)
                } else {
                    callback.invoke(null)
                }
            }
            return
        }
        try {
            ClocksFetchTask(mContext, mProviderInfo) { options ->
                run {
                    mClocks = options;
                    if (callback != null) {
                        if (mClocks?.isNotEmpty()!!) {
                            callback.invoke(mClocks);
                        } else {
                            callback.invoke(null)
                        }
                    }
                }
            }.execute()
        } catch (e: NullPointerException) {
            Log.e("Exception", e.stackTraceToString())
        }
    }

    private class ClocksFetchTask(
        private var mContext: Context, private val mProviderInfo: ProviderInfo?,
        callback: OptionsFetchedListener,
    ) : AsyncTask<Void?, Void?, ArrayList<Clockface>?>() {
        private val mCallback: OptionsFetchedListener = callback
        override fun doInBackground(vararg voids: Void?): ArrayList<Clockface> {
            val resolver = mContext.applicationContext.contentResolver
            val clockfaces: ArrayList<Clockface> = ArrayList()
            val optionsUri = Uri.Builder()
                .scheme(ContentResolver.SCHEME_CONTENT)
                .authority(mProviderInfo!!.authority)
                .appendPath(LIST_OPTIONS)
                .build()
            val c = resolver.query(optionsUri, null, null, null, null)
            try {
                while (c.moveToNext()) {
                    val id = c.getString(c.getColumnIndex(COL_ID))
                    val title = c.getString(c.getColumnIndex(COL_TITLE))
                    val thumbnailUri = c.getString(c.getColumnIndex(COL_THUMBNAIL))
                    val previewUri = c.getString(c.getColumnIndex(COL_PREVIEW))
                    val thumbnail = Uri.parse(thumbnailUri)
                    val preview = Uri.parse(previewUri)
                    val builder: Clockface.Builder = Clockface.Builder()
                    builder.setId(id).setTitle(title)
                        .setThumbnail(ContentUriAsset(mContext, thumbnail,
                            RequestOptions.centerInsideTransform()))
                        .setPreview(ContentUriAsset(mContext, preview,
                            RequestOptions.fitCenterTransform()))
                    clockfaces.add(builder.build())
                }
                Glide.get(mContext).clearDiskCache()
            } catch (e: Exception) {
                Log.e("Clockfaces", e.message)
            } finally {
                c.close()
            }
            return clockfaces
        }

        override fun onPostExecute(clockfaces: ArrayList<Clockface>?) {
            super.onPostExecute(clockfaces)
            mCallback?.invoke(clockfaces)
        }

        companion object {
            private const val LIST_OPTIONS = "list_options"
            private const val COL_TITLE = "title"
            private const val COL_ID = "id"
            private const val COL_THUMBNAIL = "thumbnail"
            private const val COL_PREVIEW = "preview"
        }

    }
}