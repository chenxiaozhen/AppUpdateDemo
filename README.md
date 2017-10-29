# AppUpdateDemo

- 应用更新使用方法

```
UpdateAppUtils.from(this)
        .checkBy(UpdateAppUtils.CHECK_BY_VERSION_NAME)// 根据VersionName检测是否要更新
        .apkUrl(apkUrl) // 新版本apk的下载地址
        .serverVersionCode(2) // 版本号
        .serverVersionName("2.0")// 版本号
        .updateInfo("1.修复若干bug\n2.美化部分页面\n3.增加微信支付方式") // 更新的内容
        .isForce(false) // 是否强制更新
        .filePath("") // apk存放路径
        .apkName("app.apk") // apk的名称
        .tipCount(1) // 提示的次数
        .updateLeftText("") // 提示框左边的按钮文字
        .updateRightText("") // 提示框右边的按钮文字
        .showNotification(true) // 下载时是否显示进度条
        .update();
```

- 项目应用截图

![](/art/01.png)

