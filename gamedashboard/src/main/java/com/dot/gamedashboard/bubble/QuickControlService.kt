package com.dot.gamedashboard.bubble

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.IBinder
import android.view.View
import android.view.WindowManager
import com.dot.gamedashboard.Launcher
import com.dot.gamedashboard.R
import com.dot.gamedashboard.bubble.impl.*
import java.lang.Exception

import android.provider.Settings.Global.ZEN_MODE_IMPORTANT_INTERRUPTIONS
import android.provider.Settings.Global.ZEN_MODE_OFF
import android.widget.ImageView
import android.widget.LinearLayout

open class QuickControlService: Service() {

    // The Window Manager View
    private var windowManager: WindowManager? = null

    // The layout inflater
    var inflater: ViewInflater? = null

    // Window Dimensions
    private var windowSize = Point()

    // The Views
    private var bubbleView: View? = null
    private var removeBubbleView: View? = null
    private var expandableView: View? = null
    private var bubbleParams: WindowManager.LayoutParams? = null
    private var config: FloatingBubbleConfig? = null
    private var physics: FloatingBubblePhysics? = null
    private var touch: QuickControlTouch? = null

    var mNotificationManager: NotificationManager? = null

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        // Remove existing views
        removeAllViews()

        // Load the Window Managers
        setupWindowManager()
        setupViews()
        setTouchListener()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        removeAllViews()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        bubbleView?.requestLayout()
    }

    private fun removeAllViews() {
        if (windowManager == null) {
            return
        }
        if (bubbleView != null) {
            windowManager!!.removeView(bubbleView)
            bubbleView = null
        }
        if (removeBubbleView != null) {
            windowManager!!.removeView(removeBubbleView)
            removeBubbleView = null
        }
        if (expandableView != null) {
            windowManager!!.removeView(expandableView)
            expandableView = null
        }
    }

    private fun setupWindowManager() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        setLayoutInflater()
        windowManager!!.defaultDisplay.getSize(windowSize)
    }

    private fun setLayoutInflater(): ViewInflater? {
        inflater = ViewInflater(context)
        return inflater
    }

    /**
     * Creates the views
     */
    private fun setupViews() {
        config = getConfig()
        config?.let {

            // Setting up view
            bubbleView = inflater!!.inflate(R.layout.quick_control_layout)
            val screenshot = bubbleView!!.findViewById<View>(R.id.qc_action_screenshot)
            val screenrecord = bubbleView!!.findViewById<View>(R.id.qc_action_record)
            val dnd = bubbleView!!.findViewById<ImageView>(R.id.qc_action_dnd)
            val settings = bubbleView!!.findViewById<View>(R.id.qc_action_settings)
            screenshot.visibility =
                if (Settings().isEnabled(context, Settings.PREF_SHOW_SCREENSHOT)) View.VISIBLE
                else View.GONE
            screenrecord.visibility =
                if (Settings().isEnabled(context, Settings.PREF_SHOW_SCREENRECORD)) View.VISIBLE
                else View.GONE
            dnd.visibility =
                if (Settings().isEnabled(context, Settings.PREF_SHOW_DND)) View.VISIBLE
                else View.GONE
            settings.setOnClickListener {
                val intent = Intent(bubbleView!!.context, Launcher::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
            screenrecord.setOnClickListener {
                startActivity(getScreenrecordIntent())
            }
            screenshot.setOnClickListener {
                touch?.forceHide()
                Runtime.getRuntime().exec("input keyevent 120")
            }
            if (getZenMode() == ZEN_MODE_OFF) {
                dnd.setImageResource(R.drawable.ic_dnd_off)
            } else {
                dnd.setImageResource(R.drawable.ic_dnd_on)
            }
            dnd.setOnClickListener {
                if (getZenMode() == ZEN_MODE_OFF) {
                    setZenMode(ZEN_MODE_IMPORTANT_INTERRUPTIONS)
                    dnd.setImageResource(R.drawable.ic_dnd_on)
                } else {
                    setZenMode(ZEN_MODE_OFF)
                    dnd.setImageResource(R.drawable.ic_dnd_off)
                }
            }
            bubbleParams = defaultWindowParams
            bubbleParams!!.gravity = it.gravity
            windowManager!!.addView(bubbleView, bubbleParams)
        }
    }

    private fun getZenMode(): Int {
        if (mNotificationManager == null) {
            mNotificationManager =
                bubbleView!!.context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        }
        return try {
            mNotificationManager!!.getZenMode()
        } catch (e: Exception) {
            -1
        }
    }

    private fun setZenMode(mode: Int) {
        if (mNotificationManager == null) {
            mNotificationManager =
                bubbleView!!.context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        }
        try {
            mNotificationManager!!.setZenMode(mode, null, TAG)
        } catch (e: Exception) {
        }
    }

    /**
     * Get an intent to show screen recording options to the user.
     */
    private val SYSUI_PACKAGE: String = "com.android.systemui"
    private val SYSUI_SCREENRECORD_LAUNCHER = "com.android.systemui.screenrecord.ScreenRecordDialog"
    open fun getScreenrecordIntent(): Intent? {
        val launcherComponent = ComponentName(
            SYSUI_PACKAGE,
            SYSUI_SCREENRECORD_LAUNCHER
        )
        val intent = Intent()
        intent.component = launcherComponent
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        return intent
    }

    /**
     * Get the Bubble config
     *
     * @return the config
     */
    protected open fun getConfig(): FloatingBubbleConfig {
        return FloatingBubbleConfig.getDefault(context)
    }

    /**
     * Sets the touch listener
     */
    @SuppressLint("ClickableViewAccessibility")
    protected fun setTouchListener() {
        physics = FloatingBubblePhysics.Builder()
            .sizeX(windowSize.x)
            .sizeY(windowSize.y)
            .bubbleView(bubbleView)
            .config(config)
            .windowManager(windowManager)
            .build()
        touch = QuickControlTouch.Builder()
            .sizeX(windowSize.x)
            .sizeY(windowSize.y)
            .bubbleView(bubbleView)
            .callback(object : QuickControlTouch.TouchCallback {
                override fun onShow() {
                    val rootLayout = bubbleView!!.findViewById<LinearLayout>(R.id.qc_layout)
                    for (i in 0 until rootLayout.childCount) {
                        val child = rootLayout.getChildAt(i)
                        if (child is ImageView) child.isClickable = true
                    }
                }

                override fun onHide() {
                    val rootLayout = bubbleView!!.findViewById<LinearLayout>(R.id.qc_layout)
                    for (i in 0 until rootLayout.childCount) {
                        val child = rootLayout.getChildAt(i)
                        if (child is ImageView) child.isClickable = false
                    }
                }

            })
            .build()
        bubbleView!!.findViewById<View>(R.id.qc_card).setOnTouchListener(touch)
        bubbleView!!.findViewById<View>(R.id.qc_drag).setOnTouchListener(touch)
    }

    /**
     * Get the default window layout params
     *
     * @return the layout param
     */
    private val defaultWindowParams: WindowManager.LayoutParams
    get() = getDefaultWindowParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT
    )

    /**
     * Get the default window layout params
     *
     * @return the layout param
     */
    private fun getDefaultWindowParams(width: Int, height: Int): WindowManager.LayoutParams {
        return WindowManager.LayoutParams(
            width,
            height,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                    or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )
    }

    /**
     * Get the context for the service
     *
     * @return the context
     */
    protected val context: Context
    get() = applicationContext

    companion object {
        protected val TAG = QuickControlService::class.java.simpleName
    }
}