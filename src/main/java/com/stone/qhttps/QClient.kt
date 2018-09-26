package com.stone.qhttps

import com.stone.retrofit2_gson_convert.GsonConverterFactory
import com.stone.retrofit2_gson_convert.Result
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.http.*

/**
 * Created By: sqq
 * Created Time: 8/17/18 2:24 PM.
 */
object QClient {

    private lateinit var url: String
    private var client: OkHttpClient? = null

    private var headers: Map<String, String>? = null
    private var params: Map<String, String>? = null

    private var partMap: Map<String, RequestBody>? = null
    private var file: Array<out MultipartBody.Part>? = null

    /**
     * 下载url
     */
    fun url(url: String): QClient {
        this.url = url
        return this
    }

    /**
     * 自定义OKHttpClient
     *
     */
    fun client(client: OkHttpClient): QClient {
        this.client = client
        return this
    }

    fun headers(headers: Map<String, String>): QClient {
        this.headers = headers
        return this
    }

    fun params(params: Map<String, String>): QClient {
        this.params = params
        return this
    }

    fun partMap(partMap: Map<String, RequestBody>): QClient {
        this.partMap = partMap
        return this
    }

    fun filePart(file: Array<out MultipartBody.Part>): QClient {
        this.file = file
        return this
    }

    private fun create(): QService {
        return Retrofit.Builder().baseUrl("https://www.google.com/")
                .client(this.client ?: OkHttpClientHelper.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
//                .addConverterFactory(StringConverterFactory)
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(QService::class.java)
    }

    fun get(observer: Observer<Result<String>>) {
        create().get(url, headers, params ?: mapOf()).httpScheduler()
                .subscribe(observer)
    }

    fun post(observer: Observer<Result<String>>) {
        create().post(url, headers, params ?: mapOf()).httpScheduler()
                .subscribe(observer)
    }

    fun upload(observer: Observer<Result<String>>) {
        if (file == null || file!!.isEmpty()) {
            throw Throwable("QHttps: the filePart can't be nullOrEmpty when request upload")
        }
        client(OkHttpClientHelper.createUpload())
        create().upload(url, headers, partMap, *file!!).httpScheduler()
                .subscribe(observer)
    }
}

fun <T> Observable<T>.httpScheduler(): Observable<T> {
    return this.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
}

interface QService {

    @GET
    fun get(@Url url: String, @HeaderMap headers: Map<String, String>? = null, @QueryMap(encoded = true) params: Map<String, String>): Observable<Result<String>>

    @FormUrlEncoded
    @POST
    fun post(@Url url: String, @HeaderMap headers: Map<String, String>? = null, @FieldMap(encoded = true) params: Map<String, String>): Observable<Result<String>>

    /**
     * @param partMap : 用以多参数传值
     * @param file : 用以多文件上传
     *
     * example:
     *
     */
    @Multipart
    @POST
    fun upload(@Url url: String, @HeaderMap headers: Map<String, String>? = null, @PartMap partMap: Map<String, RequestBody>? = null, @Part vararg file: MultipartBody.Part): Observable<Result<String>>
}


