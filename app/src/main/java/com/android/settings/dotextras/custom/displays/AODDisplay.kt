package com.android.settings.dotextras.custom.displays

import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.android.settings.dotextras.R
import java.text.SimpleDateFormat
import java.util.*

class AODDisplay : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.display_aod, container, false)
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val dateText: TextView = view.findViewById(R.id.dateView)
        val formatter = SimpleDateFormat(getString(R.string.lockscreen_date_pattern))
        dateText.text = formatter.format(Calendar.getInstance().time)
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
            requireContext().registerReceiver(null, ifilter)
        }
        val batteryPct: Float? = batteryStatus?.let { intent ->
            val level: Int = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale: Int = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            level * 100 / scale.toFloat()
        }
        val batteryTextView: TextView = view.findViewById(R.id.batteryStatus)
        if (batteryPct != null) {
            batteryTextView.text = "${batteryPct.toInt()}%"
        }
    }
}