package com.android.settings.dotextras.custom.stats

import android.app.Activity
import android.content.SharedPreferences
import android.os.SystemProperties
import android.text.TextUtils
import android.util.Log
import com.google.gson.GsonBuilder
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class StatsBuilder(private val pref: SharedPreferences) {

    private val mCompositeDisposable = CompositeDisposable()

    fun push(activity: Activity) {
        if (pref.getBoolean(Constants.ALLOW_STATS, true) && pref.getBoolean(Constants.IS_FIRST_LAUNCH, true)
        ) {
            if (!TextUtils.isEmpty(SystemProperties.get(Constants.KEY_DEVICE))) {
                val requestInterface = Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL)
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(
                        GsonConverterFactory.create(
                            GsonBuilder().setLenient().create()
                        )
                    )
                    .build().create(RequestInterface::class.java)
                val stats = StatsData()
                stats.codeName = stats.codeName
                stats.setVersion(stats.getVersion())
                stats.buildType = stats.buildType
                stats.setCountryCode(stats.getCountryCode(activity))
                stats.buildDate = stats.buildDate
                val request = ServerRequest()
                request.setStats(stats)
                mCompositeDisposable.add(
                    requestInterface.operation(request)
                    !!.observeOn(AndroidSchedulers.mainThread(), false, 100)
                        .subscribeOn(Schedulers.io())
                        .subscribe({ resp: ServerResponse? ->
                            handleResponse(resp!!)
                        }, { error: Throwable ->
                            handleError(error)
                        })
                )
            } else {
                Log.d(Constants.TAG, "Unsupported ROM")
            }
        }
    }

    private fun handleResponse(resp: ServerResponse) {
        if (resp.success!!) {
            val editor: SharedPreferences.Editor = pref.edit()
            editor.putBoolean(Constants.IS_FIRST_LAUNCH, false)
            editor.putString(
                Constants.LAST_BUILD_DATE,
                SystemProperties.get(Constants.KEY_BUILD_DATE)
            )
            editor.apply()
            Log.d(Constants.TAG, "Stats pushed")
        } else {
            resp.message?.let { Log.d(Constants.TAG, it) }
        }
    }

    private fun handleError(error: Throwable) {
        Log.d(Constants.TAG, error.stackTraceToString())
    }

    fun clearComposite() {
        mCompositeDisposable.clear()
    }
}