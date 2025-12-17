package com.lw.ai.glasses

import android.app.Application
import android.os.Environment
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.Utils
import dagger.hilt.android.HiltAndroidApp
import io.reactivex.rxjava3.exceptions.UndeliverableException
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import java.io.IOException
import java.net.SocketException

@HiltAndroidApp
class LinWearApplication : Application(){
    override fun onCreate() {
        super.onCreate()

        Utils.init(this)

        val logDirPath = externalCacheDir?.absolutePath
            ?: cacheDir.absolutePath

        LogUtils.getConfig().apply {
            setConsoleSwitch(true)
            setLog2FileSwitch(true)
            setDir(logDirPath)
            setSaveDays(7)
            setFilePrefix("AiGlass")
            setDir(Environment.getExternalStorageDirectory())
        }

        setupRxJavaErrorHandler()
    }

    private fun setupRxJavaErrorHandler() {
        RxJavaPlugins.setErrorHandler { throwable ->
            if (throwable is UndeliverableException) {
                val e = throwable.cause
                if (e is IOException || e is SocketException) {
                    LogUtils.w("RxErrorHandler", "Ignoring benign network exception: $e")
                    return@setErrorHandler
                }
                if (e is InterruptedException) {
                    LogUtils.w("RxErrorHandler", "Ignoring InterruptedException: $e")
                    return@setErrorHandler
                }
            }
            LogUtils.e("RxErrorHandler", "Undeliverable exception received", throwable)
        }
    }

}