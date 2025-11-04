# **LinWear Ai Glasses SDK 文档（中文版）**

---

## **1. 添加权限**

```xml
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission
    android:name="android.permission.NEARBY_WIFI_DEVICES"
    android:usesPermissionFlags="neverForLocation"
    tools:targetApi="33" />
```

---

## **2. 添加依赖（必须）**

可参考 Demo，Demo 为 `toml` 集成方式。  
直接集成方式：

```gradle
implementation("io.reactivex.rxjava3:rxjava:3.1.6")
```

必需依赖项：
- 领为眼镜 SDK：`fissionsdk_glasses-release.aar`
- RxJava3
- RxAndroid
- RxAndroidBle
- OkHttp
- Retrofit
- UtilCodex

---

## **3. SDK 初始化**

参考 Demo 实现方式（包含 Rx 全局异常捕获）。日志可不用保存在本地。

主要方法：
- `Utils.init()`  
- `GlassesManage.initialize()`

---

## **4. 搜索设备**

App 可以自行实现搜索逻辑，也可以使用 SDK 自带方法：

```kotlin
GlassesManage.startScanBleDevices()
```

---

## **5. 连接设备**

```kotlin
GlassesManage.connect()
```

---

## **6. 同步文件**

```kotlin
GlassesManage.syncAllMediaFile()
```

---

## **7. AI 助手功能**

AI 功能包括 **语音对话、图像识别、翻译** 等。可选择两种方式：

### ✅ SDK 内部大模型
连接成功后调用：
```kotlin
GlassesManage.connectAiAssistant()
```

### ✅ 自定义大模型（App 自己实现）
请参考 Demo 中的 `AudioStateEvent` 实现。  
`GlassesManage.initialize()` 的第三个参数传入 `true` 即可启用自定义模式。

---

## **8. SDK Flow 流监听（Event 参数）**

### **① 搜索设备 - ScanStateEvent**
- `DeviceFound`：返回 `ScanResult`，包含 **Mac 地址、信号值、名称**  
- `ScanFinished`：扫描完成  
- `Error`：扫描异常  

### **② 连接设备 - ConnectionStateEvent**
- `Connecting`：连接中  
- `Connected`：已连接  
- `Disconnected`：断开连接  

### **③ 音频流 - AudioStateEvent**
- （详细内容请参考 Demo 示例）

### **④ 同步媒体文件 - FileSyncEvent**
- `ConnectSuccess`：连接 Wi-Fi 成功  
- `DownloadProgress`：下载进度  
  - `progress`：当前进度  
  - `curFileIndex`：当前文件序号  
  - `totalFileCount`：总文件数  
- `DownloadSuccess`：同步成功  
  - `filePath`：文件保存路径  
- `Failed`：同步失败（错误码请参考错误码文档）  

### **⑤ AI 助手 - AiAssistantEvent**
- `AiAssistantResult`：大模型返回结果  
  - `question`：问题  
  - `answer`：答案  
  - `questionType`：问题类型  
  - `answerType`：答案类型  
  - `isFinished`：是否结束（可能多段流式返回）  
- `Failed`：错误（参考错误码文档）

---

> 📘 **提示：** 所有接口与事件回调请参考 Demo 中的完整示例代码。
