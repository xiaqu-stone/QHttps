package com.stone.qhttps.download

import okhttp3.MediaType
import okhttp3.ResponseBody
import okio.*
import java.io.IOException

/**
 * Created By: sqq
 * Created Time: 17/6/12 下午1:40.
 */

class DownloadProgressResponseBody(private val responseBody: ResponseBody?,
                                   private val progressListener: DownloadProgressListener?) : ResponseBody() {
    private var bufferedSource: BufferedSource? = null

    override fun contentType(): MediaType? {
        return responseBody?.contentType()
    }

    override fun contentLength(): Long {
        return responseBody?.contentLength() ?: 0L
    }

    override fun source(): BufferedSource? {
        if (bufferedSource == null && responseBody != null) {
            bufferedSource = Okio.buffer(source(responseBody.source()))
        }
        return bufferedSource
    }

    private fun source(source: Source): Source {
        return object : ForwardingSource(source) {
            var totalBytesRead = 0L

            @Throws(IOException::class)
            override fun read(sink: Buffer, byteCount: Long): Long {
                val bytesRead = super.read(sink, byteCount)
                // read() returns the number of bytes read, or -1 if this source is exhausted.
                totalBytesRead += if (bytesRead != -1L) bytesRead else 0

                progressListener?.update(totalBytesRead, contentLength(), bytesRead == -1L)
                return bytesRead
            }
        }
    }
}
