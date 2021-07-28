package com.android.settings.dotextras.custom.stats

import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.PUT

interface RequestInterface {
    @Headers("Content-Type: application/json")
    @PUT("device/")
    fun operation(@Body request: ServerRequest?): Observable<ServerResponse?>?
}