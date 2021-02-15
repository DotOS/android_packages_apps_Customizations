package com.android.dot.extra.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.dot.extra.adapters.models.Maintainers
import com.android.dot.extra.parsers.HttpHandler
import com.android.dot.extra.parsers.ImageSaver
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.views.DotMaterialPreference
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.lang.NullPointerException
import java.net.URL


class MaintainersAdapter(private val items: ArrayList<Maintainers>) : RecyclerView.Adapter<MaintainersAdapter.ViewHolder>() {

    var forceCheck = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_maintainer, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val maintainer: Maintainers = items[position]
        holder.pref.titleView!!.visibility = View.VISIBLE
        holder.pref.summaryView!!.visibility = View.VISIBLE
        holder.pref.iconView!!.imageTintList = null
        holder.pref.titleView!!.text = maintainer.name
        holder.pref.summaryView!!.text = maintainer.deviceName
        getGithubIcon(maintainer.githubUsername!!, holder.pref)
    }

    fun forceLoadImages() {
        forceCheck = true
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return items.size
    }

    private fun getGithubIcon(usernameResId: String, preference: DotMaterialPreference) {
        parseGitIcon().execute(usernameResId, preference)
    }

    @SuppressLint("StaticFieldLeak")
    private inner class parseGitIcon : AsyncTask<Any, DotMaterialPreference?, String?>() {
        private var id: String? = null
        private var image: Drawable? = null
        private lateinit var preference: DotMaterialPreference

        override fun doInBackground(vararg arg0: Any): String? {
            preference = arg0[1] as DotMaterialPreference
            val saved = ImageSaver()
                .setDirectoryName("${preference.context.cacheDir}/ignore/")
                .setFileName(arg0[0].toString() + ".jpeg")
                .load()
            if (saved == null || forceCheck) {
                forceCheck = false
                val sh = HttpHandler()
                val url = "https://api.github.com/users/" + arg0[0].toString()
                val jsonStr: String? = sh.makeServiceCall(url)
                if (jsonStr != null) {
                    try {
                        val jsonObj = JSONObject(jsonStr)
                        id = jsonObj.getString("id")
                    } catch (ignored: JSONException) {
                    }
                }
                try {
                    val `is`: InputStream =
                        URL("https://avatars2.githubusercontent.com/u/$id?v=4").content as InputStream
                    try {
                        image = Drawable.createFromStream(`is`, "src name")
                        val result = getCircularImage(image!!)
                        if (result != null) {
                            ImageSaver()
                                .setDirectoryName("${preference.context.cacheDir}/ignore/")
                                .setFileName(arg0[0].toString() + ".jpeg")
                                .save(result)
                            image = BitmapDrawable(preference.resources, result)
                        }
                    } catch (e: NullPointerException) {
                        Log.d("GithubAPI", "Rate limit exceeded")
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } else
                image = BitmapDrawable(preference.resources, saved)
            return null
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            preference.iconView!!.setImageDrawable(image)
        }
    }

    private fun getCircularImage(drawable: Drawable): Bitmap? {
        var srcBitmap: Bitmap?
        if (drawable is BitmapDrawable) {
            if (drawable.bitmap != null) {
                srcBitmap = drawable.bitmap
            }
        }
        val bitmap: Bitmap? = if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
            Bitmap.createBitmap(1,
                1,
                Bitmap.Config.ARGB_8888) // Single color bitmap will be created of 1x1 pixel
        } else {
            Bitmap.createBitmap(drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888)
        }
        val canvasF = Canvas(bitmap)
        drawable.setBounds(0, 0, canvasF.width, canvasF.height)
        drawable.draw(canvasF)
        srcBitmap = bitmap
        val squareBitmapWidth = srcBitmap!!.width.coerceAtMost(srcBitmap.height)
        val dstBitmap = Bitmap.createBitmap(
            squareBitmapWidth,
            squareBitmapWidth,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(dstBitmap)
        val paint = Paint()
        paint.isAntiAlias = true
        val rect = Rect(0, 0, squareBitmapWidth, squareBitmapWidth)
        val rectF = RectF(rect)
        canvas.drawOval(rectF, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        val left = ((squareBitmapWidth - srcBitmap.width) / 2).toFloat()
        val top = ((squareBitmapWidth - srcBitmap.height) / 2).toFloat()
        canvas.drawBitmap(srcBitmap, left, top, paint)
        srcBitmap.recycle()
        return dstBitmap
    }

    private fun isOnline(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.activeNetworkInfo
        return netInfo != null && netInfo.isConnectedOrConnecting
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val pref: DotMaterialPreference = view.findViewById(R.id.maintainerPref)
    }
}