package com.lw.ai.glasses

import android.app.Application
import android.os.Environment
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.Utils
import com.fission.wear.glasses.sdk.GlassesManage
import com.fission.wear.glasses.sdk.constant.GlassesConstant
import com.lw.top.lib_core.data.datastore.AppDataManager
import dagger.hilt.android.HiltAndroidApp
import io.reactivex.rxjava3.exceptions.UndeliverableException
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.SocketException
import javax.inject.Inject

@HiltAndroidApp
class LinWearApplication : Application(){

    @Inject
    lateinit var appDataManager: AppDataManager

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
        initSavedEnvironment()
    }

    private fun initSavedEnvironment() {
        MainScope().launch {
            val savedEnvName = appDataManager.getEnvironment()
            savedEnvName?.let { name ->
                try {
                    val env = GlassesConstant.ServerEnvironment.valueOf(name)
                    GlassesManage.updateEnvironment(env)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
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
