package com.dot.applock

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class AppLockActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_applock)
        supportFragmentManager.beginTransaction()
            .replace(R.id.appLockFragmentContainer, AppLockFragment(), "applock")
            .commit()
    }
}