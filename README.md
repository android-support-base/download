# android-support-download
* 基于Android API and Google API 开发

## 发布

| groupId | artifactId | version |
| -------- | -------- | -------- |
| com.amlzq.android | download |  |

* 使用
```
dependencies{
    ...
    implementation 'com.amlzq.android:download:latest.integration'
}
```

## 工程结构
* ./download
> download library
```
package: com.amlzq.android.download
```
* ./sample
> sample application
```
package: com.amlzq.asb
appName: Download支持库
applicationId: com.amlzq.asb.download
```

### 理论

### 实践

- [Aspsine/MultiThreadDownload](https://github.com/Aspsine/MultiThreadDownload)
```
Android Multi-Thread Download library
```

- OkDownload
	```
	// 下载库
    // core
    implementation 'com.liulishuo.okdownload:okdownload:1.0.5'
    // provide sqlite to store breakpoints
    implementation 'com.liulishuo.okdownload:sqlite:1.0.5'
    // provide okhttp to connect to backend
    implementation 'com.liulishuo.okdownload:okhttp:1.0.5'
	```
	```
	/**
     * OkDownload类库配置
     */
    private void okDownloadInitialize() {
        // 日志记录器
//        com.liulishuo.okdownload.core.Util.setLogger(new OkDownloadLogger());
//        com.liulishuo.okdownload.core.Util.enableConsoleLog();
        // 全局控制
//        OkDownload.with().setMonitor(monitor);
//        DownloadDispatcher.setMaxParallelRunningCount(1);
//        RemitStoreOnSQLite.setRemitToDBDelayMillis(3000);
//        OkDownload.with().downloadDispatcher().cancelAll();
//        OkDownload.with().breakpointStore().remove(taskId);
        // 注射组件
//        OkDownload.Builder builder = new OkDownload.Builder(mContext)
//                .downloadStore(downloadStore)
//                .callbackDispatcher(callbackDispatcher)
//                .downloadDispatcher(downloadDispatcher)
//                .connectionFactory(connectionFactory)
//                .outputStreamFactory(outputStreamFactory)
//                .downloadStrategy(downloadStrategy)
//                .processFileStrategy(processFileStrategy)
//                .monitor(monitor);
//        OkDownload.setSingletonInstance(builder.build());
    }
	```

- FileDownloader
	```
	implementation 'com.liulishuo.filedownloader:library:1.7.6'
	```

- Download
	```
	小体积
	```

- 