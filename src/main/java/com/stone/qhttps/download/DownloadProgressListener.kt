package com.stone.qhttps.download

/**
 * Created By: sqq
 * Created Time: 17/6/12 下午1:40.
 */
interface DownloadProgressListener {
    fun update(bytesRead: Long, contentLength: Long, done: Boolean)
}
