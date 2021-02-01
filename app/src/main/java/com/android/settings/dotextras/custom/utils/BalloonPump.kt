package com.android.settings.dotextras.custom.utils

import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.view.View
import com.android.settings.dotextras.R
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.BalloonSizeSpec
import com.skydoves.balloon.createBalloon
import kotlin.math.roundToInt

class BalloonPump(val context: Context, val preferences: SharedPreferences) {

    lateinit var balloon: Balloon

    fun create(resID: Int) {
        balloon = createBalloon(context) {
            setArrowSize(0)
            setWidth(BalloonSizeSpec.WRAP)
            setHeight(BalloonSizeSpec.WRAP)
            setCornerRadiusResource(R.dimen.default_dialog_radius)
            setTextResource(resID)
            setTextSize(16f)
            setPadding(14)
            setTextColor(ResourceHelper.getTextColor(context))
            setBackgroundColorResource(R.color.colorAccent)
            setBalloonAnimation(BalloonAnimation.FADE)
            setLifecycleOwner(lifecycleOwner)
        }
    }

    fun create(string: String) {
        balloon = createBalloon(context) {
            setArrowSize(0)
            setWidth(BalloonSizeSpec.WRAP)
            setHeight(BalloonSizeSpec.WRAP)
            setCornerRadiusResource(R.dimen.default_dialog_radius)
            setText(string)
            setTextSize(16f)
            setPadding(14)
            setTextColor(ResourceHelper.getTextColor(context))
            setBackgroundColorResource(R.color.colorAccent)
            setBalloonAnimation(BalloonAnimation.FADE)
            setLifecycleOwner(lifecycleOwner)
        }
    }

    fun show(target: View) {
        if (preferences.getBoolean(SettingsConstants.SHOW_BALLOONS, true)) {
            Handler().postDelayed({
                balloon.showAlignTop(target,
                    0,
                    -context.resources.getDimension(R.dimen.default_dialog_radius).roundToInt() / 2)
            }, 400)
        }
    }

}