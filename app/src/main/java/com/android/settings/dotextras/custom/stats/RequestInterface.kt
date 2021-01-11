package com.android.settings.dotextras.custom.stats

import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.POST

interface RequestInterface {
    @POST("dotstats_api/")
    fun operation(@Body request: ServerRequest?): Observable<ServerResponse?>?
}