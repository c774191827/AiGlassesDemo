import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.LogUtils
import com.lw.top.lib_core.data.model.response.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

const val TAG = "BaseViewModel"

abstract class BaseViewModel : ViewModel() {

    protected val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    protected val _error = MutableStateFlow<ApiResult.Error?>(null)
    val error: StateFlow<ApiResult.Error?> = _error.asStateFlow()

    protected val _isEmpty = MutableStateFlow(false) // 用于表示成功但数据为空的状态
    val isEmpty: StateFlow<Boolean> = _isEmpty.asStateFlow()


    protected fun <T> launchOperation(
        operationBlock: suspend () -> ApiResult<T?>,
        onSuccess: (data: T?) -> Unit = {},
        onEmpty: () -> Unit = {},
        onError: (errorResult: ApiResult.Error) -> Unit = {},
        checkEmptyCondition: (T?) -> Boolean = { data -> data == null || (data is Collection<*> && data.isEmpty()) || (data is Map<*, *> && data.isEmpty()) },
        showLoading: Boolean = true
    ) {
        viewModelScope.launch {
            if (showLoading) {
                _isLoading.value = true
            }
            // 清除之前的状态
            _error.value = null
            _isEmpty.value = false

            try {
                when (val result = operationBlock()) {
                    is ApiResult.Success -> {
                        if (checkEmptyCondition(result.data)) {
                            _isEmpty.value = true
                            onEmpty()
                        } else {
                            // 确保 result.data 不是 null 才调用 onSuccess
                            // (checkEmptyCondition 通常会处理 null，但这里多一层保障)
                            result.data?.let { onSuccess(it) }
                                ?: run { // 如果 T 不是可空，但 data 是 null 且 checkEmptyCondition 允许了
                                    _isEmpty.value = true
                                    onEmpty()
                                    LogUtils.dTag(
                                        TAG,
                                        "Data was null in Success but not considered empty by checkEmptyCondition."
                                    )
                                }
                        }
                    }

                    is ApiResult.Error -> {
                        _error.value = result
                        onError(result)
                    }

                    is ApiResult.Empty -> { // 直接处理 ApiResult.Empty
                        _isEmpty.value = true
                        onEmpty()
                    }

                    is ApiResult.Loading -> {
                        // 如果 operationBlock 自身可以返回 Loading (不常见，因为 safeApiCall 通常不返回这个)
                        // 通常 _isLoading 由 launchOperation 开始时设置。
                        // 如果 operationBlock 返回 Loading，我们可能不需要在这里再次设置 _isLoading，
                        // 或者根据具体语义决定。目前假设 safeApiCall 不会返回 Loading。
                        if (!showLoading) _isLoading.value = true // 如果外部禁用了自动loading，但内部返回了loading
                    }
                }
            } catch (e: Exception) { // 捕获 operationBlock 自身抛出的未被 ApiResult.Error 包装的异常
                LogUtils.eTag(TAG, e)
                val errorResult = ApiResult.Error(
                    e,
                    e.message ?: "An unexpected error occurred in operationBlock."
                )
                _error.value = errorResult
                onError(errorResult)
            } finally {
                if (showLoading) {
                    _isLoading.value = false
                }
            }
        }
    }


    fun clearError() {
        _error.value = null
    }

}