LinWear Ai Glasses SDK 文档-中文版
1.添加权限
  <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
  <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION " />
  <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
  <uses-permission
        android:name="android.permission.NEARBY_WIFI_DEVICES"
        android:usesPermissionFlags="neverForLocation"
        tools:targetApi="33" />
        
2.添加依赖（必须）可参考demo，demo为toml集成方式，直接集成方式为：implementation("io.reactivex.rxjava3:rxjava:3.1.6")
  -领为眼镜SDK：fissionsdk_glasses-release.aar
  -rxjava3
  -rxandroid
  -rxandroidble
  -okhttp
  -retrofit
  -utilcodex
  
3.SDK初始化（参考Demo 实现方式，包含Rx全局捕获异常，日志可不用保存在本地），所有方法参数，请参考Demo
  -Utils.init()
  -GlassesManage.initialize()
  
4.搜索设备（App可以自己实现搜索，不使用SDK的也可以）
  - GlassesManage.startScanBleDevices()
   
5.连接设备
  - GlassesManage.connect()
    
6.同步文件
  - GlassesManage.syncAllMediaFile()

7.AI助手（Ai对话，Ai识图，Ai翻译，可使用SDK内部Ai大模型，App仅展示结果。 也可实现AudioStateEvent分步获取，App自己实现大模型交互，GlassesManage.initialize()第三个参数为true）
  -SDK内部实现大模型，连接成功后调用GlassesManage.connectAiAssistant() 即可
  -App自己实现大模型，请参考Demo AudioStateEvent实现，AudioStateEvent参数请查看后续文档
  
8.Sdk Flow流监听（Event 参数）
  ①.搜索设备ScanStateEvent
    -DeviceFound 返回 ScanResult，Mac地址，信号值，名称 可参考Demo 获取
    -ScanFinished 扫描完成
    -Error 扫描异常
  ②.连接设备ConnectionStateEvent
    -Connecting 连接中
    -Connected 已连接
    -Disconnected 断开连接
  ③.音频流AudioStateEvent
    -
  ④.同步媒体文件FileSyncEvent
    -ConnectSuccess 连接Wifi成功
    -DownloadProgress 下载进度（progress 当前进度，curFileIndex 当前同步文件角标，totalFileCount 总共文件条数）
    -DownloadSuccess 同步成功（filePath当前文件存放地址）
    -Failed 同步失败（错误码请查看错误码文档）
  ⑤.AI助手AiAssistantEvent
    -AiAssistantResult 大模型返回的结果 （question问题，answer答案，questionType问题类型，answerType答案类型，isFinished 是否结束。问题答案可能是多段流式返回，需要判断是否结束）
    -Failed（参考错误码文档）
  
  
  

