package com.lw.top.lib_core.data.repository

import ai.instavision.ffmpegkit.FFmpegKit
import ai.instavision.ffmpegkit.FFmpegSession
import ai.instavision.ffmpegkit.ReturnCode
import ai.instavision.ffmpegkit.SessionState
import android.app.Activity
import android.app.Application
import com.blankj.utilcode.util.LogUtils
import com.bytedance.android.openlive.broadcast.DouyinBroadcastApi
import com.bytedance.android.openlive.broadcast.api.AccountAuthCallback
import com.bytedance.android.openlive.broadcast.api.BroadcastInitConfig
import com.bytedance.android.openlive.broadcast.api.IBroadcastAuth
import com.bytedance.android.openlive.broadcast.api.InitBroadcastListener
import com.bytedance.android.openlive.broadcast.api.model.BroadcastPrivacyConfig
import com.bytedance.android.openlive.broadcast.api.model.CamType
import com.bytedance.android.openlive.broadcast.api.model.LiveAngle
import com.bytedance.android.openlive.broadcast.api.model.StartLiveResp
import com.lw.top.lib_core.BuildConfig
import com.lw.top.lib_core.data.repository.base.BaseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject


class DouYinRepository @Inject constructor(
    private val application: Application
) : BaseRepository() {

    private var currentSession: FFmpegSession? = null

    fun initDouyinSdk(activity: Activity) {

        if (DouyinBroadcastApi.isBroadcastInited()) {
            LogUtils.d("clx", "dy直播已初始化")
            return
        }
        val config =
            BroadcastInitConfig.Builder(application, "843349", "FunSeek", "1.0.0", 1)
                .privacyConfig(
                    BroadcastPrivacyConfig.Builder()
                        .isCanUseMac(false)
                        .isCanUseImei(false)
                        .build()
                )
                .isDebug(BuildConfig.DEBUG)
                .setInitBroadcastListener(object : InitBroadcastListener {
                    override fun onInitializeSuccess() {
                        if (DouyinBroadcastApi.isAuthorized()) {
                            LogUtils.d(
                                "clx",
                                "初始化成功 openid: ${DouyinBroadcastApi.getAccessToken()?.openId}"
                            )
                        } else {
                            LogUtils.d("clx", "初始化成功, 未授权")
                        }
                    }

                    override fun onInitializeFail(msg: String?) {
                        LogUtils.d("clx", "初始化失败：$msg")
                    }
                })
        DouyinBroadcastApi.showBroadcastInitLoading(activity)
        DouyinBroadcastApi.init(config.build())
    }


    fun login(activity: Activity) {
        if (!DouyinBroadcastApi.isBroadcastInited()) {
            LogUtils.d("clx", "未初始化")
            return
        }
        DouyinBroadcastApi.login(activity, object : AccountAuthCallback {
            override fun onSuccess() {
                val token = DouyinBroadcastApi.getAccessToken()
                if (token == null) {
                    LogUtils.d("clx", "授权成功，但 token 为空")
                } else {
                    LogUtils.d("clx", "授权成功，openid: ${token.openId}，${token.accessToken}")
                }
            }

            override fun onFailed(errorCode: Int, errorMsg: String?) {
                LogUtils.d("clx", "授权失败：errorCode $errorCode, errorMsg $errorMsg")
            }
        })
    }

    suspend fun getUserInfo(): Result<Any> = withContext(Dispatchers.IO) {
        if (!DouyinBroadcastApi.isBroadcastInited()) {
            return@withContext Result.failure(kotlin.IllegalStateException("SDK 未初始化"))
        }

        if (!DouyinBroadcastApi.isAuthorized()) {
            return@withContext Result.failure(SecurityException("账号未授权"))
        }

        try {
            val response = DouyinBroadcastApi.getAccountInfo()

            if (response == null) {
                Result.failure(kotlin.Exception("获取用户信息失败，网络请求返回 null"))
            } else if (response.statusCode != 0) {
                Result.failure(kotlin.Exception("获取失败 code:${response.statusCode}, msg:${response.prompts}"))
            } else {
                Result.success(response)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun startBroadcast(liveType: Int, camType: Int): Result<StartLiveResp> =
        withContext(Dispatchers.IO) {
            if (!DouyinBroadcastApi.isBroadcastInited()) {
                return@withContext Result.failure(kotlin.IllegalStateException("SDK 未初始化"))
            }
            if (!DouyinBroadcastApi.isAuthorized()) {
                return@withContext Result.failure(SecurityException("未授权"))
            }

            try {
                val resp = DouyinBroadcastApi.startBroadcast(LiveAngle.STANDARD, CamType.APP)
                if (resp == null) {
                    Result.failure(kotlin.Exception("网络请求失败，返回为空"))
                } else if (resp.statusCode != 0) {
                    if (resp.statusCode == IBroadcastAuth.USER_UNAUTHORIZED) {
                        Result.failure(SecurityException("Token失效或未授权"))
                    } else {
                        Result.failure(kotlin.Exception("开启失败 code:${resp.statusCode}, msg:${resp.prompts}"))
                    }
                } else {
                    Result.success(resp)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun closeBroadcast(openRoomId: String?): Result<Unit> = withContext(Dispatchers.IO) {
        if (openRoomId.isNullOrEmpty()) return@withContext Result.success(Unit)

        try {
            val resp = DouyinBroadcastApi.turnOffBroadcast(openRoomId)
            if (resp != null && resp.statusCode == 0) {
                Result.success(Unit)
            } else {
                Result.failure(kotlin.Exception("关闭失败: ${resp?.prompts}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun startFFmpegPush(rtmpPushUrl: String) {
        // 先停止之前的推流（如果有）
        stopFFmpegPush()

        val rtspUrl = "rtsp://"
        LogUtils.d("wl", "准备推流: $rtspUrl -> $rtmpPushUrl")

        // ✅ 变化2: 构建命令字符串 (注意空格)
        // 以前是数组 arrayOf(...)，现在拼成一个长字符串
        val cmdBuilder = kotlin.text.StringBuilder()
        cmdBuilder.append("-i ").append(rtspUrl).append(" ") // 输入
        cmdBuilder.append("-c:v libx264 ").append(" ")       // 视频编码
        cmdBuilder.append("-preset veryfast ").append(" ")   // 编码速度
        cmdBuilder.append("-c:a aac ").append(" ")           // 音频编码
        cmdBuilder.append("-b:v 1500k ").append(" ")         // 视频码率
        cmdBuilder.append("-b:a 128k ").append(" ")          // 音频码率
        cmdBuilder.append("-f flv ").append(" ")             // 封装格式
        cmdBuilder.append(rtmpPushUrl)                       // 输出地址 (RTMP)

        val command = cmdBuilder.toString()

        LogUtils.d("wl", "FFmpeg执行命令: $command")

        // ✅ 变化3: 调用 FFmpegKit.executeAsync
        // 这里的 lambda 就是 FFmpegSessionCompleteCallback
        currentSession = FFmpegKit.executeAsync(command) { session ->
            val state = session.state
            val returnCode = session.returnCode

            if (ReturnCode.isSuccess(returnCode)) {
                LogUtils.d("wl", "FFmpeg 推流结束 (成功)")
            } else if (ReturnCode.isCancel(returnCode)) {
                LogUtils.d("wl", "FFmpeg 推流已手动取消")
            } else {
                // 失败时，可以通过 session.failStackTrace 获取错误日志
                LogUtils.e("wl", "FFmpeg 推流失败, Code: $returnCode")
                LogUtils.e("wl", "错误日志: ${session.failStackTrace}")
            }
        }
    }

    fun stopFFmpegPush() {
        currentSession?.let { session ->
            if (session.state == SessionState.RUNNING || session.state == SessionState.CREATED) {
                session.cancel()
            }
        }
        currentSession = null
    }

}