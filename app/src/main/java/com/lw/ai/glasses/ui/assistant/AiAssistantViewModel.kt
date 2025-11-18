package com.lw.ai.glasses.ui.assistant

import BaseViewModel
import android.content.Context
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.LogUtils
import com.fission.wear.glasses.sdk.GlassesManage
import com.fission.wear.glasses.sdk.data.dto.AiChatMessageDTO
import com.fission.wear.glasses.sdk.data.dto.AiContentType
import com.fission.wear.glasses.sdk.events.AiAssistantEvent
import com.fission.wear.glasses.sdk.events.AudioStateEvent
import com.fission.wear.glasses.sdk.events.CmdResultEvent
import com.lw.top.lib_core.data.local.entity.AiAssistantEntity
import com.lw.top.lib_core.data.repository.AiAssistantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class AiAssistantViewModel @Inject constructor(
    private val repository: AiAssistantRepository,
    @ApplicationContext private val context: Context
) : BaseViewModel() {
    private val _uiState = MutableStateFlow(AiAssistantUiState())
    val uiState: StateFlow<AiAssistantUiState> = _uiState

    private var currentMessage: AiAssistantEntity? = null

    init {
        loadHistoryMessages()
        observeGlassesEvents()
    }

    private fun loadHistoryMessages() {
        viewModelScope.launch {
            val history = repository.getAllMessages()
            _uiState.value = _uiState.value.copy(
                messages = history
            )
        }
    }


    fun clearAllMessages() {
        viewModelScope.launch {
            repository.clearAllMessages()
            _uiState.value = _uiState.value.copy(
                messages = emptyList()
            )
        }
    }


    private fun observeGlassesEvents() {
        viewModelScope.launch {
            GlassesManage.eventFlow().collect { event ->
                when (event) {
                    is AiAssistantEvent.AiAssistantResult -> {
                        LogUtils.d("AiAssistantEvent.AiAssistantResult${event.data}")
                        handleStreamingResult(event.data)
                    }

                    is AudioStateEvent.StartRecording -> {//唤醒词后开始录音
                        LogUtils.d("设备开始录音")
                    }

                    is AudioStateEvent.ReceivingAudioData -> {//持续发送给大模型
//                        LogUtils.d("接收录音数据 ${event.byteArray.toByteArray()}")
                    }

                    is AudioStateEvent.CancelRecording -> {
                        LogUtils.d("取消录音")
                    }

                    is AudioStateEvent.StopRecording -> {
                        LogUtils.d("停止录音")
                    }

                    is CmdResultEvent.ImageData -> {
                        LogUtils.d("完整的图片数据：${event.data.toByteArray()}")
                        val bitmap = ConvertUtils.bytes2Bitmap(event.data.toByteArray())
                        val path = "${context.externalCacheDir}/${System.currentTimeMillis()}.jpg"
                        val file = File(path)
                        //调用大模型 识别图片。播报结果
                    }
                    else -> {}
                }
            }
        }
    }


    private suspend fun handleStreamingResult(data: AiChatMessageDTO) {
        val questionText = anyToStringSafe(data.question)
        val answerText = anyToStringSafe(data.answer)
        if (questionText.isEmpty() && answerText.isEmpty() && !data.isFinished) return

        currentMessage = if (currentMessage != null) {
            currentMessage!!.copy(
                question = currentMessage!!.question + questionText,
                answer = currentMessage!!.answer + answerText,
                questionType = mapContentType(data.questionType),
                answerType = mapContentType(data.answerType),
                timestamp = currentMessage!!.timestamp
            )
        } else {
            AiAssistantEntity(
                question = questionText,
                questionType = mapContentType(data.questionType),
                answer = answerText,
                answerType = mapContentType(data.answerType),
                timestamp = System.currentTimeMillis()
            )
        }


        val newList = _uiState.value.messages.toMutableList()
        val index = newList.indexOfFirst { it.timestamp == currentMessage!!.timestamp }
        if (index >= 0) {
            newList[index] = currentMessage!!
        } else if (questionText.isNotEmpty() || answerText.isNotEmpty()) {
            newList.add(0, currentMessage!!)
        }
        _uiState.value = _uiState.value.copy(
            messages = newList,
            streamingMessageId = currentMessage!!.hashCode().toLong()
        )
        if (data.isFinished) {
            if (currentMessage!!.question.isNotEmpty() || currentMessage!!.answer.isNotEmpty()) {
                repository.insertMessage(currentMessage!!)
            }
            _uiState.value = _uiState.value.copy(streamingMessageId = null)
            currentMessage = null
        }
    }

    private fun mapContentType(type: AiContentType): String {
        return when (type) {
            AiContentType.TEXT -> "txt"
            AiContentType.IMAGE_PATH, AiContentType.IMAGE_FILE -> "image"
            else -> ""
        }
    }

    private fun anyToStringSafe(any: Any?): String {
        return when (any) {
            null -> ""
            is String -> any
            is ByteArray -> ""
            is File -> any.absolutePath
            else -> any.toString()
        }
    }

}