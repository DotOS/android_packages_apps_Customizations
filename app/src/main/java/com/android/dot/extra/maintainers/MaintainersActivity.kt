package com.android.dot.extra.maintainers

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.dot.extra.maintainers.adapters.MaintainersAdapter
import com.android.dot.extra.maintainers.adapters.models.Maintainers
import com.android.settings.dotextras.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class MaintainersActivity : AppCompatActivity() {

    val jsonUrl = "https://raw.githubusercontent.com/DotOS/official_devices/master/maintainers.json"

    val client = OkHttpClient()
    var maintainers: ArrayList<Maintainers> = ArrayList()
    lateinit var refresh: SwipeRefreshLayout
    lateinit var recycler: RecyclerView
    lateinit var adapter: MaintainersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maintainers)
        recycler = findViewById(R.id.maintainersRecycler)
        refresh = findViewById(R.id.maintainersRefresh)
        refresh.setOnRefreshListener { fetchMaintainers() }
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

        recycler.clearOnScrollListeners()
        recycler.addOnScrollListener(scrollListener)
    }

    private fun fetchMaintainers() {
        refresh.isRefreshing = true
        val request = Request.Builder()
            .url(jsonUrl)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                refresh.isRefreshing = false
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
                    recycler.adapter = adapter
                    recycler.setHasFixedSize(true)
                    recycler.isNestedScrollingEnabled = true
                    recycler.layoutManager = LinearLayoutManager(this@MaintainersActivity)
                    refresh.isRefreshing = false
                }
            }
        })
    }
}
