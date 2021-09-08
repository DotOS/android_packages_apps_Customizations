package com.android.settings.dotextras.custom.sections.maintainers

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.settings.dotextras.custom.sections.maintainers.adapters.MaintainersAdapter
import com.android.settings.dotextras.custom.sections.maintainers.adapters.models.Maintainers
import com.android.settings.dotextras.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.activity_maintainers.*
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class MaintainersActivity : AppCompatActivity() {

    val jsonUrl = "https://raw.githubusercontent.com/DotOS/services_apps_ota/dot11/maintainers.json"

    val client = OkHttpClient()
    var maintainers: ArrayList<Maintainers> = ArrayList()
    lateinit var adapter: MaintainersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maintainers)
        maintainerToolbar.canGoBack(this)
        maintainersRefresh.setOnRefreshListener { fetchMaintainers() }
        fetchMaintainers()
        val forceReload: FloatingActionButton = findViewById(R.id.forceReload)
        forceReload.setOnClickListener {
            adapter.forceLoadImages()
        }
        val scrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                when (newState) {
                    RecyclerView.SCROLL_STATE_IDLE -> forceReload.show()
                    RecyclerView.SCROLL_STATE_DRAGGING -> forceReload.hide()
                }
                super.onScrollStateChanged(recyclerView, newState)
            }
        }
        maintainersRecycler.clearOnScrollListeners()
        maintainersRecycler.addOnScrollListener(scrollListener)
    }

    private fun fetchMaintainers() {
        maintainersRefresh.isRefreshing = true
        val request = Request.Builder()
            .url(jsonUrl)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                maintainersRefresh.isRefreshing = false
            }

            override fun onResponse(call: Call, response: Response) {
                val mainJson = JSONObject(response.body()!!.string())
                val jsonArray: JSONArray = mainJson.getJSONArray("devs")
                maintainers = ArrayList()
                for (i in 0 until jsonArray.length()) {
                    val jsonObject: JSONObject = jsonArray.getJSONObject(i)
                    val model = Maintainers()
                    model.name = jsonObject.getString("name")
                    model.githubUsername = jsonObject.getString("github")
                    model.deviceName = jsonObject.getString("device")
                    maintainers.add(model)
                }

                runOnUiThread {
                    maintainers.sortBy(Maintainers::deviceName)
                    adapter = MaintainersAdapter(this@MaintainersActivity, maintainers)
                    maintainersRecycler.adapter = adapter
                    maintainersRecycler.setHasFixedSize(true)
                    maintainersRecycler.isNestedScrollingEnabled = true
                    maintainersRecycler.layoutManager = LinearLayoutManager(this@MaintainersActivity)
                    maintainersRefresh.isRefreshing = false
                }
            }
        })
    }
}