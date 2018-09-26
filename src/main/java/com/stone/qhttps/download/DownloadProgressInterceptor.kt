package com.stone.qhttps.download

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * Created By: sqq
 * Created Time: 17/6/12 下午1:39.
 */

class DownloadProgressInterceptor(private val progressListener: DownloadProgressListener?) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalResponse = chain.proceed(chain.request())
        return originalResponse.newBuilder()
                .body(DownloadProgressResponseBody(originalResponse.body(), progressListener))
                .build()
    }
}
