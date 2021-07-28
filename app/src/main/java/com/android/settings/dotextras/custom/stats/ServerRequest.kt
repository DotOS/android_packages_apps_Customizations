package com.android.settings.dotextras.custom.stats

class ServerRequest {
    private var device: StatsData? = null

    fun setStats(stats: StatsData?) {
        this.device = stats
    }
}