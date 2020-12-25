package com.android.settings.dotextras.custom

import android.content.Context

interface SectionInterface {

    fun isAvailable(context: Context): Boolean

}