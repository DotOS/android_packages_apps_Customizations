package com.android.settings.dotextras.custom.sections.clock.utils

import androidx.exifinterface.media.ExifInterface
import java.io.InputStream

internal class ExifInterfaceCompat(inputStream: InputStream?) {
    private var mSupportExifInterface: ExifInterface? = null
    private var mFrameworkExifInterface: android.media.ExifInterface? = null
    fun getAttributeInt(tag: String?, defaultValue: Int): Int {
        return if (mFrameworkExifInterface != null) mFrameworkExifInterface!!.getAttributeInt(
            tag,
            defaultValue
        ) else mSupportExifInterface!!.getAttributeInt(
            tag!!, defaultValue
        )
    }

    fun getAttribute(tag: String?): String {
        return if (mFrameworkExifInterface != null) mFrameworkExifInterface!!.getAttribute(tag) else mSupportExifInterface!!.getAttribute(
            tag!!
        )!!
    }

    companion object {
        const val TAG_ORIENTATION = ExifInterface.TAG_ORIENTATION
        const val EXIF_ORIENTATION_NORMAL = 1
        const val EXIF_ORIENTATION_UNKNOWN = -1
    }

    /**
     * Reads Exif tags from the specified image input stream. It's the caller's responsibility to
     * close the given InputStream after use.
     * @see ExifInterface.ExifInterface
     */
    init {
        mSupportExifInterface = ExifInterface(inputStream!!)
    }
}