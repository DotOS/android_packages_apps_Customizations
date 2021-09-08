package com.android.settings.dotextras.custom.sections.maintainers.adapters

import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.android.settings.dotextras.custom.sections.maintainers.adapters.models.Maintainers
import com.android.settings.dotextras.custom.sections.maintainers.parsers.HttpHandler
import com.android.settings.dotextras.custom.sections.maintainers.parsers.ImageSaver
import com.android.settings.dotextras.R
import com.dot.ui.DotMaterialPreference
import com.dot.ui.utils.toBitmap
import nl.komponents.kovenant.task
import nl.komponents.kovenant.ui.successUi
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.net.URL

class MaintainersAdapter(
    val activity: AppCompatActivity,
    private val items: ArrayList<Maintainers>
) :
    RecyclerView.Adapter<MaintainersAdapter.ViewHolder>() {

    var forceCheck = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_maintainer, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val maintainer: Maintainers = items[position]
        holder.pref.titleView.visibility = View.VISIBLE
        holder.pref.summaryView?.visibility = View.VISIBLE
        holder.pref.iconView.imageTintList = null
        holder.pref.titleView.text = maintainer.name
        holder.pref.setUrl("https://github.com/${maintainer.githubUsername}")
        holder.pref.summaryView?.text = maintainer.deviceName
        getGithubIcon(maintainer.githubUsername!!, holder.pref)
        holder.pref.iconView.visibility = View.VISIBLE
    }

    fun forceLoadImages() {
        forceCheck = true
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return items.size
    }

    private fun getGithubIcon(usernameResId: String, preference: DotMaterialPreference) {
        var image: Drawable? = null
        task {
            var id = ""
            val saved = ImageSaver()
                .setDirectoryName("${preference.context.cacheDir}/ignore/")
                .setFileName("$usernameResId.jpeg")
                .load()
            if (saved == null || forceCheck) {
                forceCheck = false
                val sh = HttpHandler()
                val url = "https://api.github.com/users/$usernameResId"
                val jsonStr: String?
                try {
                    jsonStr = sh.makeServiceCall(url)
                } catch (e: IOException) {
                    return@task null
                }
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
                        if (image != null) {
                            val result = image!!.toBitmap()
                            ImageSaver()
                                .setDirectoryName("${preference.context.cacheDir}/ignore/")
                                .setFileName("$usernameResId.jpeg")
                                .save(result!!)
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
            return@task image
        } successUi {
            if (image != null) {
                activity.runOnUiThread {
                    preference.iconView.load(image) {
                        crossfade(true)
                        transformations(CircleCropTransformation())
                    }
                }
            }
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val pref: DotMaterialPreference = view.findViewById(R.id.maintainerPref)
    }
}