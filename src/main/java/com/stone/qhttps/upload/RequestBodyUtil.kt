package com.stone.qhttps.upload

import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

/**
 * Created By: sqq
 * Created Time: 17/11/20 下午4:28.
 */

object RequestBodyUtil {
    const val MULTIPART_FORM_DATA = "multipart/form-data"

    fun file2Part(key: String, file: File): MultipartBody.Part {
        val requestBody = RequestBody.create(MediaType.parse(MULTIPART_FORM_DATA), file)
        return MultipartBody.Part.createFormData(key, file.name, requestBody)
    }

    fun str2Body(value: String): RequestBody {
        return RequestBody.create(MediaType.parse(MULTIPART_FORM_DATA), value)
    }

}
