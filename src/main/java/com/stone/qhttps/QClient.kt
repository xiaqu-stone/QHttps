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
    private var body: RequestBody? = null

    private var partMap: Map<String, RequestBody>? = null
    private var file: Array<MultipartBody.Part>? = null
    private var fileList: List<MultipartBody.Part>? = null

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

    fun body(body: RequestBody): QClient {
        this.body = body
        return this
    }

    fun partMap(partMap: Map<String, RequestBody>): QClient {
        this.partMap = partMap
        return this
    }

    fun filePart(file: Array<MultipartBody.Part>): QClient {
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
        create().get(url, headers ?: mapOf(), params ?: mapOf()).httpScheduler()
            .subscribe(observer)
    }

    fun get2(observer: Observer<String>) {
        create().get2(url, headers ?: mapOf(), params ?: mapOf()).httpScheduler()
            .subscribe(observer)
    }

    fun post(observer: Observer<Result<String>>) {
        create().post(url, headers ?: mapOf(), params ?: mapOf()).httpScheduler()
            .subscribe(observer)
    }

    fun post2(observer: Observer<String>) {
        create().post2(url, headers ?: mapOf(), params ?: mapOf()).httpScheduler()
            .subscribe(observer)
    }

    fun upload(observer: Observer<Result<String>>) {
        if (file == null || file!!.isEmpty()) {
            throw Throwable("QHttps: the filePart can't be nullOrEmpty when request upload")
        }
        client(OkHttpClientHelper.createUpload())
        create().upload(url, headers ?: mapOf(), *file!!).httpScheduler()
            .subscribe(observer)
    }

    fun upload2(observer: Observer<String>) {
        if (file == null || file!!.isEmpty()) {
            throw Throwable("QHttps: the filePart can't be nullOrEmpty when request upload")
        }
        client(OkHttpClientHelper.createUpload())
        create().upload2(url, headers ?: mapOf(), *file!!).httpScheduler()
            .subscribe(observer)
    }

    fun uploadMap(observer: Observer<Result<String>>) {
        if (partMap == null || partMap!!.isEmpty()) {
            throw Throwable("QHttps: the partMap can't be nullOrEmpty when request upload")
        }
        client(OkHttpClientHelper.createUpload())
        create().upload(url, headers, partMap!!).httpScheduler()
            .subscribe(observer)
    }

    fun uploadMap2(observer: Observer<String>) {
        if (partMap == null || partMap!!.isEmpty()) {
            throw Throwable("QHttps: the partMap can't be nullOrEmpty when request upload")
        }
        client(OkHttpClientHelper.createUpload())
        create().upload2(url, headers ?: mapOf(), partMap!!).httpScheduler()
            .subscribe(observer)
    }

    fun uploadBody(observer: Observer<Result<String>>) {
        client(OkHttpClientHelper.createUpload())
        create().upload(url, headers ?: mapOf(), body!!).httpScheduler()
            .subscribe(observer)
    }

    fun uploadBody2(observer: Observer<String>) {
        client(OkHttpClientHelper.createUpload())
        create().upload2(url, headers ?: mapOf(), body!!).httpScheduler()
            .subscribe(observer)
    }
}

fun <T> Observable<T>.httpScheduler(): Observable<T> {
    return this.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
}

interface QService {

    @GET
    fun get(@Url url: String, @HeaderMap headers: Map<String, String>? = null, @QueryMap(encoded = true) params: Map<String, String>): Observable<Result<String>>

    @GET
    fun get2(@Url url: String, @HeaderMap headers: Map<String, String>? = null, @QueryMap(encoded = true) params: Map<String, String>): Observable<String>

    @FormUrlEncoded
    @POST
    fun post(@Url url: String, @HeaderMap headers: Map<String, String>? = null, @FieldMap(encoded = true) params: Map<String, String>): Observable<Result<String>>

    @FormUrlEncoded
    @POST
    fun post2(@Url url: String, @HeaderMap headers: Map<String, String>? = null, @FieldMap(encoded = true) params: Map<String, String>): Observable<String>

    /**
     * Part
     *
     * val parts = new MultipartBody.Builder().addFormDataPart...
     *  .build().parts().toTypeArray
     */
    @Multipart
    @POST
    fun upload(@Url url: String, @HeaderMap headers: Map<String, String>? = null, @Part vararg file: MultipartBody.Part): Observable<Result<String>>

    @Multipart
    @POST
    fun upload2(@Url url: String, @HeaderMap headers: Map<String, String>? = null, @Part vararg file: MultipartBody.Part): Observable<String>

    /**
     * 多个参数时可以采用此方法
     *
     * 注意：当在这里上传文件时，注意 key 的写法
     *
     * val map = mapOf("file\"; filename=\"${fileImg.name}" to requestBody, "type" to RequestBodyUtil.str2Body(type))
     *
     * 在组装Map时，对于file需要在后端约定的正常 key值 “file” 的基础上 加上 filename 注意上述写法，其中包含 双引号的转义
     */
    @Multipart
    @POST
    fun upload(@Url url: String, @HeaderMap headers: Map<String, String>? = null, @PartMap partMap: Map<String, @JvmSuppressWildcards RequestBody>): Observable<Result<String>>

    @Multipart
    @POST
    fun upload2(@Url url: String, @HeaderMap headers: Map<String, String>? = null, @PartMap partMap: Map<String, @JvmSuppressWildcards RequestBody>): Observable<String>

    /**
     * 通用的方式
     *
     * 外面全部利用 MultipartBody 来组装成一个 RequestBody
     *
     * example:
     *
    RequestBody body=new MultipartBody.Builder()
    .addFormDataPart("name","stone")
    .addFormDataPart("token","1234567890")
    .addFormDataPart("file",file.getName(),file)
    .build();
     */
    @POST
    fun upload(@Url url: String, @HeaderMap headers: Map<String, String>? = null, @Body body: RequestBody): Observable<Result<String>>

    @POST
    fun upload2(@Url url: String, @HeaderMap headers: Map<String, String>? = null, @Body body: RequestBody): Observable<String>
}


