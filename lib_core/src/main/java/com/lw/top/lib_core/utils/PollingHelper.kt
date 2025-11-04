package com.lw.top.lib_core.utils

import com.blankj.utilcode.util.LogUtils
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private const val DEFAULT_POLL_TAG = "PollingHelper"
private const val DEFAULT_POLL_INTERVAL = 30_000L

/**
 * 一个用于管理轮询任务的辅助类。
 *
 * @param scope 轮询任务将运行的协程作用域 (CoroutineScope)。
 * @param pollIntervalMillis 后续任务执行之间的时间间隔（毫秒）。
 *                           如果 `executeImmediately` 为 false，则这也是初始延迟时间。
 * @param taskName (可选) 协程的名称，主要用于调试目的 (例如，在协程调试器中显示)。
 * @param executeImmediately (可选, 默认为 true) 是否立即执行任务。
 *                           如果为 true (默认值)，任务在启动时立即执行一次，然后每隔 `pollIntervalMillis` 重复执行。
 *                           如果为 false，任务将首先等待 `pollIntervalMillis`，然后执行，之后再按间隔重复。
 * @param task 需要被执行的挂起函数。
 * @param onError (可选) 用于处理任务执行期间抛出的异常的回调函数。
 *                如果未提供，异常将向上冒泡，可能会导致作用域崩溃（除非作用域本身有例如 CoroutineExceptionHandler 的处理机制）。
 */
class PollingHelper(
    private val scope: CoroutineScope,
    private val pollIntervalMillis: Long = DEFAULT_POLL_INTERVAL,
    private val taskName: String = DEFAULT_POLL_TAG,
    private val executeImmediately: Boolean = true,
    private val task: suspend () -> Unit,
    private val onError: ((Throwable) -> Unit)? = null
) {
    private var pollingJob: Job? = null

    /**
     * 检查轮询任务当前是否处于活动状态。
     * @return 如果任务正在运行，则为 true；否则为 false。
     */
    val isActive: Boolean
        get() = pollingJob?.isActive == true

    /**
     * 根据 `executeImmediately` 的配置启动轮询任务。
     * 如果任务已处于活动状态，它将首先停止当前的轮询，然后再启动新的轮询。
     */
    fun start() {
        stop() // 确保此助手的任何现有作业都已停止
        LogUtils.dTag(taskName, "开始执行协程")
        pollingJob = scope.launch(CoroutineName(taskName)) {
            if (executeImmediately) {
                try {
                    task() // 立即执行
                } catch (e: Throwable) {
                    handleError(e, "立即执行时发生错误")
                    // 如果没有自定义错误处理器且协程仍活动，则重新抛出异常
                    if (onError == null && this.isActive) throw e
                }
            }

            // 主要轮询循环
            while (this.isActive) { // 使用 this.isActive 检查当前启动的协程作用域是否活动
                delay(pollIntervalMillis) // 在下一次执行（或非立即执行时的第一次执行）之前始终延迟

                if (this.isActive) { // 延迟后再次检查协程是否仍然活动
                    try {
                        task() // 执行轮询任务
                    } catch (e: Throwable) {
                        handleError(e, "轮询执行时发生错误")
                        // 如果没有自定义错误处理器且协程仍活动，则重新抛出异常
                        if (onError == null && this.isActive) throw e
                    }
                } else {
                    // 如果在延迟期间协程变为非活动状态，则跳出循环
                    break
                }
            }
        }
    }

    /**
     * 内部错误处理函数。
     * @param e 捕获到的异常。
     * @param executionPhase 发生错误的执行阶段描述。
     */
    private fun handleError(e: Throwable, executionPhase: String) {
        LogUtils.dTag(taskName, "协程异常：${e.message}，执行阶段：${executionPhase}")
        onError?.invoke(e)
    }

    /**
     * 停止轮询任务。
     * 如果任务正在运行，它将被取消。
     */
    fun stop() {
        if (pollingJob?.isActive == true) {
            pollingJob?.cancel()
        }
        pollingJob = null
        LogUtils.dTag(taskName, "停止执行协程")
    }
}