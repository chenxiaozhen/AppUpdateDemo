package com.library.appupdate.receiver;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import com.library.appupdate.utils.DownloadAppUtils;
import com.library.appupdate.utils.UpdateAppUtils;


/**
 * 注册
 * <action android:name="android.intent.action.DOWNLOAD_COMPLETE" />
 * <action android:name="android.intent.action.DOWNLOAD_NOTIFICATION_CLICKED"/>
 */
public class UpdateAppReceiver extends BroadcastReceiver {
    public UpdateAppReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // 处理下载完成
        Cursor c = null;

        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(intent.getAction())) {
            if (DownloadAppUtils.downloadUpdateApkId >= 0) {
                long downloadId = DownloadAppUtils.downloadUpdateApkId;
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(downloadId);
                DownloadManager downloadManager = (DownloadManager) context
                        .getSystemService(Context.DOWNLOAD_SERVICE);
                c = downloadManager.query(query);
                if (c.moveToFirst()) {
                    int status = c.getInt(c
                            .getColumnIndex(DownloadManager.COLUMN_STATUS));
                    if (status == DownloadManager.STATUS_FAILED) {
                        downloadManager.remove(downloadId);

                    } else if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        UpdateAppUtils.installApk(context,DownloadAppUtils.downloadUpdateApkFilePath);
                    }
                }
                c.close();
            }
        }
    }
}
