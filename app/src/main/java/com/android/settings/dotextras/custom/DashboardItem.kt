package com.android.settings.dotextras.custom

import androidx.fragment.app.Fragment

class DashboardItem(val parent: Fragment, val card_title: String, val target_fragment: Fragment, val display_fragment: Fragment) {
    var longCard: Boolean = false
}