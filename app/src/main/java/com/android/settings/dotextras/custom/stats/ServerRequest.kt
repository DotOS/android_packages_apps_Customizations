package com.android.settings.dotextras.custom.stats

class ServerRequest {
    private var operation: String? = null
    private var stats: StatsData? = null
    fun setOperation(operation: String?) {
        this.operation = operation
    }

    fun setStats(stats: StatsData?) {
        this.stats = stats
    }
}