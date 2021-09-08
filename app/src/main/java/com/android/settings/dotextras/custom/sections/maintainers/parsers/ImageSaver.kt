package com.android.settings.dotextras.custom.sections.maintainers.parsers

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class ImageSaver {
    private var directoryName = "images"
    private var fileName = "image.jpeg"
    fun setFileName(fileName: String): ImageSaver {
        this.fileName = fileName
        return this
    }

    fun setDirectoryName(directoryName: String): ImageSaver {
        this.directoryName = directoryName
        return this
    }

    fun save(bitmapImage: Bitmap) {
        var fileOutputStream: FileOutputStream? = null
        try {
            fileOutputStream = FileOutputStream(createFile())
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                fileOutputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun createFile(): File {
        val dir = File(directoryName)
        if (!dir.exists()) dir.mkdir()
        return File(dir, fileName)
    }

    fun load(): Bitmap? {
        var inputStream: FileInputStream? = null
        val file = createFile()
        if (file.exists()) {
            try {
                inputStream = FileInputStream(createFile())
                return BitmapFactory.decodeStream(inputStream)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    inputStream?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        } else return null
        return null
    }
}