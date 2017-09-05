package com.library.appupdate.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;

import com.library.appupdate.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownLoadService extends IntentService {

    private NotificationManager mNoticeManager;
    private static final int NID = 0x111;
    public static final String M_ACTION = "com.hhwy.updateApp.downloadComplete";
    private NotificationCompat.Builder mBuilder;

    private long lastTime = 0;

    private String mTempPath;
    private String mRealPath;

    public static boolean isFirst = false;

    public DownLoadService() {
        super("DownLoadService");
    }

    protected void onHandleIntent(Intent intent) {
        Bundle bundle = intent.getExtras();
        // 获得下载文件的url
        String downloadUrl = bundle.getString("url");
        mTempPath = bundle.getString("temp");
        mRealPath = bundle.getString("path");
        // 设置文件下载后的保存路径，保存在SD卡根目录的Download文件夹
        File file = new File(mTempPath);
        File dirs = file.getParentFile();
        boolean isDirectoryCreated = dirs.exists();
        if (!isDirectoryCreated) {
            isDirectoryCreated = dirs.mkdirs();
        }
        if (isDirectoryCreated)
            // 开始下载
            downloadFile(downloadUrl, file);
    }

    private void downloadFile(String downloadUrl, File file) {
        isFirst = true;
        try {
            HttpURLConnection huc = (HttpURLConnection) new URL(downloadUrl).openConnection();
            huc.setRequestMethod("GET");
            long current = 0;
            if (file.exists()) {
                current = file.length();
                huc.setRequestProperty("Range", "bytes=" + file.length() + "-");
            }
            int code = huc.getResponseCode();
            if (code == 200 || code == 206) {
                int sum = huc.getContentLength();
                sum += current;
                InputStream is = huc.getInputStream();
                FileOutputStream fos = new FileOutputStream(file, true);
                byte[] buffer = new byte[2048];
                int length;
                while ((length = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, length);
                    current += length;
                    showDownNotice(current, sum, false);
                }
                downSuccess();
            }
        } catch (IOException e) {
            e.printStackTrace();
            isFirst = false;
            showDownNotice(0, 100, true);
        }
    }

    //下载成功
    private void downSuccess() {
        // 重命名临时文件成正式文件
        new File(mTempPath).renameTo(new File(mRealPath));
        //发送下载完成的通知
        Intent sendIntent = new Intent(M_ACTION);
        // 把下载好的文件的保存地址加进Intent
        sendIntent.putExtra("downloadFile", new File(mRealPath).getPath());
        sendBroadcast(sendIntent);
    }

    /**
     * 下载发送通知栏进度条更新
     *
     * @param current 当前
     * @param total   大小
     * @param isStop  是否停止了
     */
    private void showDownNotice(long current, long total, boolean isStop) {
        if (total == 0)
            return;
        if (mBuilder == null) {
            mBuilder = new NotificationCompat.Builder(this);
            // TODO: 2017/9/5 获取应用的图标
            int id = R.drawable.ic_launcher;
            mBuilder.setSmallIcon(id)
                    .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), id))
                    .setAutoCancel(false)
                    .setWhen(System.currentTimeMillis());
        }
        if (!isStop) {
            double progress = current * 1.0d / total * 100;
            if (current < total) {
                mBuilder.setProgress(100, (int) progress, false);
                mBuilder.setContentTitle("正在下载");
                mBuilder.setContentText("已下载" + String.format("%.1f", progress) + "%，请稍等");
                Intent intent = new Intent(this, this.getClass());
                PendingIntent pi1 = PendingIntent.getActivity(this, 0, intent, 0);
                mBuilder.setContentIntent(pi1);
            } else if (current == total) {
                mBuilder.setProgress(0, 0, false);
                mBuilder.setContentTitle("下载完成");
                mBuilder.setContentText("已下载完成，点击开始安装");
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setDataAndType(Uri.fromFile(new File(mRealPath)),
                        "application/vnd.android.package-archive");
                PendingIntent pi2 = PendingIntent.getActivity(this, 0, intent, 0);
                mBuilder.setContentIntent(pi2);
            }
        } else {
            mBuilder.setContentTitle("暂停下载");
            mBuilder.setContentText("请先检查网络，保证网络畅通！");
        }
        Notification notification = mBuilder.build();
        if (mNoticeManager == null)
            mNoticeManager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
        if (System.currentTimeMillis() - lastTime > 1000 || current == total || isStop) {
            mNoticeManager.notify(NID, notification);
            lastTime = System.currentTimeMillis();
        }
    }

}
