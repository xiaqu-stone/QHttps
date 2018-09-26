package com.stone.qhttps

import io.reactivex.disposables.Disposable
import io.reactivex.internal.disposables.DisposableHelper

/**
 * 包装cancel方法，提供给外部调用，用以取消请求操作
 */
class Disposer(var s: Disposable) {
    fun cancel() {
        val s = this.s
        this.s = DisposableHelper.DISPOSED
        s.dispose()
    }
}