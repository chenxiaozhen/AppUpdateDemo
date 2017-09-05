package com.library.appupdate.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.library.appupdate.service.DownLoadService;

/**
 * Created by chenxz on 2017/9/4.
 */
public class DownloadAppUtils {

    /**
     * 通过浏览器下载APK包
     *
     * @param context
     * @param url
     */
    public static void downloadForWebView(Context context, String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * 下载apk
     *
     * @param context
     * @param url      apk下载地址
     * @param filePath apk存放路径
     * @param fileName apk名称
     */
    public static void downloadApk(Context context, String url, String filePath, String fileName) {
        String path = filePath + "/" + fileName;
        Intent intent = new Intent(context, DownLoadService.class);
        intent.putExtra("url", url);
        intent.putExtra("path", path);
        intent.putExtra("temp", path + "_temp");
        context.startService(intent);
    }

}
