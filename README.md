


### gradle

```
    debugImplementation "com.sqq.xiaqu:q-https:1.0.2"
    releaseImplementation "com.sqq.xiaqu:q-https-no-log:1.0.2"
    
    //内部依赖，建议在主工程中添加
    implementation "com.squareup.okhttp3:okhttp:$okhttp"

    implementation "com.squareup.retrofit2:retrofit:$retrofit"
    implementation "com.squareup.retrofit2:adapter-rxjava2:$retrofit"

    implementation "io.reactivex.rxjava2:rxjava:$rxJava"
    implementation "io.reactivex.rxjava2:rxandroid:$rxAndroid"
```


### 说明

#### get & post 


推荐使用：

```
Retrofit.Builder().baseUrl("host")
        .client(OkHttpClientHelper.create { "add header"})
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build().create(ApiService::class.java).loanIndex().httpScheduler()
        .subscribe(QObserver<Result<LoanIndex>> { t -> Logs.d("the result is $t, \n the amount is ${t.data.amount_info.amount},, ${t.data.amount_info.max_amount}") })
```
在主工程中使用：可以将ApiService对象做一个全局的封装，最终形成如下的调用代码：

```
//此处可以在主工程中单独封装，将apiService做全局处理
val apiService = Retrofit.Builder().baseUrl("host")
        .client(OkHttpClientHelper.create { "add header"})
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build().create(ApiService::class.java)

//如下代码:每次在具体业务场景中，一句话调用即可发起请求                
apiService.loanIndex().httpScheduler()
        .subscribe(QObserver<Result<LoanIndex>> { t -> Logs.d("the result is $t, \n the amount is ${t.data.amount_info.amount},, ${t.data.amount_info.max_amount}") })
```

子库中快速发起请求：

```
QClient.url("完整URL")
        .headers("add Header")
        .get(QObserver {
            //data层的json
            val data = GsonUtils.getGson().fromJson(it.data, LoanIndex::class.java)
        })
```


#### download

```
DownClient.url("url")
        .dest(File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "test.apk"))
        .progress { bytesRead, contentLength, done -> Logs.d("MainActivity.download() called with: bytesRead = [$bytesRead], contentLength = [$contentLength], done = [$done]") }
        .start(QObserver<File> { toast("下载完成, 存储路径${it.path}") }
                .start { toast("start request") }.finish { toast("the download is finish") }
                .error { toast("the error is ${it.message ?: "null"}") })
```