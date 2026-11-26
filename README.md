# TestAPP

极简 Android WebView 壳应用。

- 应用名称：TestAPP
- 默认首页：`http://192.168.4.115:5173/demo/`
- 支持 JavaScript、DOM Storage、Cookie
- 支持外部 Scheme：`testapp://client/action/pages?url=https%3A%2F%2Fexample.com`
- 支持通过其他扫码 App 的“分享”功能，将扫描到的网址发送给 TestAPP
- 不申请摄像头等运行时权限；仅声明 Android 普通 `INTERNET` 权限

## 云端编译

GitHub 仓库进入 **Actions**，选择 **Build TestAPP APK**，点击 **Run workflow**。
成功后打开本次运行记录，在 **Artifacts** 下载 `TestAPP-apk`，解压即可得到 `app-debug.apk`。

## 说明

由于不申请 CAMERA 权限，TestAPP 本身不直接调用摄像头扫码。二维码扫描由手机上已有的扫码应用完成，再通过分享网址交给 TestAPP。
