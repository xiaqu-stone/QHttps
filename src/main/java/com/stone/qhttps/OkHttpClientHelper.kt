package com.stone.qhttps

import com.stone.qhttps.download.DownloadProgressInterceptor
import com.stone.qhttps.download.DownloadProgressListener
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.net.HttpURLConnection
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created By: sqq
 * Created Time: 8/14/18 4:31 PM.
 */
object OkHttpClientHelper {
    const val HTTP_RESPONSE_DISK_CACHE_MAX_SIZE = 10 * 1024 * 1024L
    const val CONTENT_TYPE = "application/json"
    const val USER_AGENT = "android"
    const val TIME_OUT_NORMAL = 30 * 1000L
    /**
     * 启动页超时设定
     */
    const val TIME_OUT_START_PAGE = 5 * 1000

    /**
     * 基本请求（get/post）使用的OkHttpClient,满足大多数的使用场景
     */
    @JvmOverloads
    fun create(timeout: Long = TIME_OUT_NORMAL, cacheDir: File? = null, headersBuilder: ((Request.Builder) -> Request.Builder)? = null): OkHttpClient {
        val okHttpBuilder = createDefaultBuilder(timeout)
                .retryOnConnectionFailure(false)
                .addInterceptor {
                    if (headersBuilder == null) it.proceed(it.request()) else it.proceed(headersBuilder(it.request().newBuilder()).build())
                }
                .addInterceptor { it ->
                    val builder = it.request().newBuilder()
                            .addHeader("Content-Type", CONTENT_TYPE)
                            .addHeader("User-Agent", USER_AGENT)
                            .addHeader("Cache-Control", String.format(Locale.CHINA, "max-age=%d, no-cache, max-stale=%d", 10, 0))
                    val response = it.proceed(builder.build())
                    if (response.code() == HttpURLConnection.HTTP_OK) {
                        response.header("Set-Cookie")
                    }
                    response
                }
        if (cacheDir != null) {
            okHttpBuilder.cache(Cache(cacheDir, HTTP_RESPONSE_DISK_CACHE_MAX_SIZE))
        }
        return okHttpBuilder.build()
    }

    /**
     * 下载
     */
    @JvmOverloads
    fun createDownload(headersBuilder: ((Request.Builder) -> Request.Builder)? = null, timeout: Long = TIME_OUT_NORMAL, listener: ((bytesRead: Long, contentLength: Long, done: Boolean) -> Unit)? = null): OkHttpClient {
        return createDefaultBuilder(timeout)
                .addInterceptor(DownloadProgressInterceptor(object : DownloadProgressListener {
                    override fun update(bytesRead: Long, contentLength: Long, done: Boolean) {
                        listener?.invoke(bytesRead, contentLength, done)
                    }
                }))
                .addInterceptor {
                    if (headersBuilder == null) it.proceed(it.request()) else it.proceed(headersBuilder(it.request().newBuilder()).build())
                }
                .addInterceptor {
                    val builder = it.request().newBuilder()
                            .addHeader("Cache-Control", String.format(Locale.CHINA, "max-age=%d, no-cache, max-stale=%d", 0, 0))
                    it.proceed(builder.build())
                }
                .build()
    }

    /**
     * 上传
     */
    @JvmOverloads
    fun createUpload(timeout: Long = TIME_OUT_NORMAL, headersBuilder: ((Request.Builder) -> Request.Builder)? = null): OkHttpClient {
        return createDefaultBuilder(timeout)
                .addInterceptor {
                    if (headersBuilder == null) it.proceed(it.request()) else it.proceed(headersBuilder(it.request().newBuilder()).build())
                }
                .build()
    }

    /**
     * 配置Glide的网络请求Client
     *
     * @param listener 可以设置 Glide 加载图片的进度监听
     */
    @JvmOverloads
    fun createGlide(listener: DownloadProgressListener? = null): OkHttpClient {
        return createDefaultBuilder(TIME_OUT_NORMAL)
                .addInterceptor(DownloadProgressInterceptor(listener))
                .build()
    }

    fun createDefaultBuilder(timeout: Long = TIME_OUT_NORMAL): OkHttpClient.Builder {
        val ssl = HttpsUtils.getSslSocketFactory()
        return OkHttpClient.Builder()
                .connectTimeout(timeout, TimeUnit.MILLISECONDS)
                .readTimeout(timeout, TimeUnit.MILLISECONDS)
                .writeTimeout(timeout, TimeUnit.MILLISECONDS)
                .sslSocketFactory(ssl.sSLSocketFactory, ssl.trustManager)
                .retryOnConnectionFailure(true)

    }


}