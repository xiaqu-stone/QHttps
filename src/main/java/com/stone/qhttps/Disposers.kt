package com.stone.qhttps

/**
 * Created By: sqq
 * Created Time: 8/16/18 5:01 PM.
 */
object Disposers {
    private val disposers = arrayListOf<Disposer>()
    fun get() {
        disposers
    }

    fun add(disposer: Disposer){
        disposers.add(disposer)
    }
}