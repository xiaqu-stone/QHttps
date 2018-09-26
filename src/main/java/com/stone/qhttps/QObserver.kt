package com.stone.qhttps

import android.support.annotation.Nullable
import com.stone.log.Logs
import io.reactivex.Observer
import io.reactivex.annotations.NonNull
import io.reactivex.disposables.Disposable
import io.reactivex.internal.util.EndConsumerHelper

/**
 * Created By: sqq
 * Created Time: 17/7/3 下午3:58.
 *
 * Retrofit发起请求的观察者监听
 *
 * 1. 支持Lambda的调用方式
 * 2. 支持内部类继承重写的方式
 */
open class QObserver<T>(private val onSuccess: ((result: T) -> Unit)? = null) : Observer<T> {

    private var onStart: ((disposer: Disposer?) -> Unit)? = null
    private var onError: ((result: T?) -> Unit)? = null
    private var onFinish: (() -> Unit)? = null

    private var disposer: Disposer? = null
    override fun onSubscribe(@NonNull s: Disposable) {
        if (EndConsumerHelper.validate(this.disposer?.s, s, javaClass)) {
            this.disposer = Disposer(s)
            onStart(this.disposer)
        }
    }

    /**
     * Called once the subscription has been set on this observer; override this
     * to perform initialization.
     *
     * 首先执行，线程取决于当前发起调用的线程；
     *
     * 这里的耗时操作会阻塞当前Rx的流程
     */
    fun start(onStart: (disposer: Disposer?) -> Unit): QObserver<T> {
        this.onStart = onStart
        return this
    }

    /**
     *
     * UI Thread
     *
     * 下载发生错误的时候的回调
     */
    fun error(error: (result: T?) -> Unit): QObserver<T> {
        this.onError = error
        return this
    }

    /**
     * UI Thread
     *
     * 结束回调，不管成功还是错误，最终都会回调
     */
    fun finish(finish: () -> Unit): QObserver<T> {
        this.onFinish = finish
        return this
    }

    /**
     * Cancels the upstream's disposable.
     */
    protected fun cancel() {
        this.disposer?.cancel()
    }

    /**
     * Called once the subscription has been set on this observer; override this
     * to perform initialization.
     *
     * 首先执行，线程取决于当前发起调用的线程；
     *
     * 这里的耗时操作会阻塞当前Rx的流程
     */
    protected open fun onStart(disposer: Disposer?) {
        onStart?.invoke(disposer)
        Logs.d("onStart Thread.currentThread() = ${Thread.currentThread()}")
    }

    /**
     * 一般在网络请求的使用场景中，只有一次回调；在RxJava的使用逻辑中，做flatMap等操作时，会被调用多次（可以理解成：遍历中的每次循环都会执行此回调）。
     *
     * 在此回调中处理自定义code值
     *
     * 注意：onSuccess 需要在onNext中 手动回调
     *
     */
    override fun onNext(@NonNull t: T) {
        Logs.d("QObserver.onNext() called with: t = [$t]")
        try {
            onSuccess(t)
        } catch (e: Exception) {
            Logs.e("Error onSuccess, msg = ${e.message}")
        }
    }

    protected open fun onSuccess(@NonNull t: T) {
        onSuccess?.invoke(t)
    }

    final override fun onError(@NonNull e: Throwable) {
        onError(e, null)
    }

    protected open fun onError(@NonNull e: Throwable, @Nullable result: T?) {
        Logs.w("SimpleObserver.onError() called with: e = [$e]")
        onError?.invoke(result)
        onFinish()
    }

    /**
     * 一般是在onNext 执行结束后，回调此方法方法，仅执行一次
     */
    override fun onComplete() {
        Logs.d("SimpleObserver.onComplete() called with: ")
        onFinish()
    }

    /**
     * 此方法肯定会被回调，
     */
    protected open fun onFinish() {
        Logs.d("onFinish Thread.currentThread() = ${Thread.currentThread()}")
        onFinish?.invoke()
    }
}
