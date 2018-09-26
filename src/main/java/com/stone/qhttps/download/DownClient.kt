package com.stone.qhttps.download

import com.stone.log.Logs
import com.stone.qhttps.OkHttpClientHelper
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/**
 * Created By: sqq
 * Created Time: 8/16/18 7:01 PM.
 * 用以在简单的下载场景快速调用
 *
 * 因下载使用使用场景频次低而且使用场景相对简单，故单独封装出来，用来快速调用
 */
object DownClient {
    private lateinit var url: String
    private var client: OkHttpClient? = null
    private var progress: ((bytesRead: Long, contentLength: Long, done: Boolean) -> Unit)? = null
    private lateinit var dest: File

    /**
     * 下载url
     */
    fun url(url: String): DownClient {
        this.url = url
        return this
    }

    /**
     * 自定义OKHttpClient，一般用以添加自定义header
     *
     * 注意：
     * 1. 当设置了OKHttpClient的值，那么设置的progress进度回调监听就失效；
     * 2. progress可以绑定到自定义的OKHttpClient中
     *
     */
    fun client(client: OkHttpClient): DownClient {
        this.client = client
        return this
    }

    /**
     * IO Thread
     *
     * 下载进度回调
     */
    fun progress(progress: (bytesRead: Long, contentLength: Long, done: Boolean) -> Unit): DownClient {
        this.progress = progress
        return this
    }

    /**
     * 下载的目的地
     */
    fun dest(dest: File): DownClient {
        this.dest = dest
        return this
    }

    /**
     * 执行请求
     */
    fun start(observer: Observer<File>) {
        Retrofit.Builder().baseUrl("https://www.google.com/")
                .client(client ?: OkHttpClientHelper.createDownload(listener = progress))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build().create(DownService::class.java).download(url)
                .subscribeOn(Schedulers.io())//订阅者的执行线程
                .unsubscribeOn(Schedulers.io())//取消订阅的发生线程，即：dispose逻辑的执行线程
                .observeOn(Schedulers.io())
                .map { it.byteStream().saveFile(dest) }
                .observeOn(AndroidSchedulers.mainThread())//观察者的执行线程
                .subscribe(observer)
    }
}

interface DownService {
    @Streaming
    @GET
    fun download(@Url url: String): Observable<ResponseBody>
}

fun InputStream.saveFile(dest: File): File {
    Logs.d("DownClient.saveFile() called with: dest = [${dest.absolutePath}]")
    this.use {
        FileOutputStream(dest).use { itOut ->
            val bufferSize = ByteArray(1024 * 1024)
            var len = it.read(bufferSize)
            while (len != -1) {
                itOut.write(bufferSize, 0, len)
                len = it.read(bufferSize)
            }
            itOut.flush()
        }
    }
    return dest
}