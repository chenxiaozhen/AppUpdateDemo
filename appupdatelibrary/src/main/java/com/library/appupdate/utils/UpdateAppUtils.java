package com.library.appupdate.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.library.appupdate.customview.ConfirmDialog;

import java.io.File;

/**
 * Created by chenxz on 2017/9/4.
 */
public class UpdateAppUtils {

    private final String TAG = "UpdateAppUtils";
    public static final int CHECK_BY_VERSION_NAME = 1001;
    public static final int CHECK_BY_VERSION_CODE = 1002;
    public static final int DOWNLOAD_BY_APP = 1003;
    public static final int DOWNLOAD_BY_BROWSER = 1004;

    private Activity activity;
    private int checkBy = CHECK_BY_VERSION_NAME;
    private int downloadBy = DOWNLOAD_BY_APP;
    private int serverVersionCode = 1;
    private String serverVersionName = "1.0";
    private String apkUrl = "";// apk下载的链接
    private String filePath = "";// 下载apk的路径
    private String apkName = "app" + serverVersionName + ".apk";
    private boolean isForce = false; //是否强制更新
    private int localVersionCode = 0;
    private String localVersionName = "";
    public static boolean showNotification = true;
    private String updateInfo = "";// 更新提示的内容
    private int tipCount = -1;// 提示更新的次数， -1 表示强制提示
    private String updateLeftText = "暂不更新";
    private String updateRightText = "立即更新";

    private String TIP_COUNT = "tip_count" + serverVersionName;

    private UpdateAppUtils(Activity activity) {
        this.activity = activity;
        getAPPLocalVersion(activity);
    }

    public static UpdateAppUtils from(Activity activity) {
        return new UpdateAppUtils(activity);
    }

    public UpdateAppUtils checkBy(int checkBy) {
        if (checkBy == CHECK_BY_VERSION_CODE || checkBy == CHECK_BY_VERSION_NAME)
            this.checkBy = checkBy;
        return this;
    }

    public UpdateAppUtils apkUrl(String apkUrl) {
        this.apkUrl = apkUrl;
        return this;
    }

    public UpdateAppUtils filePath(String filePath) {
        if (!TextUtils.isEmpty(filePath))
            this.filePath = filePath;
        return this;
    }

    public UpdateAppUtils apkName(String apkName) {
        if (!TextUtils.isEmpty(apkName))
            this.apkName = apkName;
        return this;
    }

    public UpdateAppUtils updateLeftText(String updateLeftText) {
        if (!TextUtils.isEmpty(updateLeftText))
            this.updateLeftText = updateLeftText;
        return this;
    }

    public UpdateAppUtils updateRightText(String updateRightText) {
        if (!TextUtils.isEmpty(updateRightText))
            this.updateLeftText = updateRightText;
        return this;
    }

    public UpdateAppUtils downloadBy(int downloadBy) {
        if (downloadBy == DOWNLOAD_BY_APP || downloadBy == DOWNLOAD_BY_BROWSER)
            this.downloadBy = downloadBy;
        return this;
    }

    public UpdateAppUtils showNotification(boolean showNotification) {
        this.showNotification = showNotification;
        return this;
    }

    public UpdateAppUtils updateInfo(String updateInfo) {
        this.updateInfo = updateInfo;
        return this;
    }


    public UpdateAppUtils serverVersionCode(int serverVersionCode) {
        this.serverVersionCode = serverVersionCode;
        return this;
    }

    public UpdateAppUtils serverVersionName(String serverVersionName) {
        this.serverVersionName = serverVersionName;
        return this;
    }

    public UpdateAppUtils isForce(boolean isForce) {
        this.isForce = isForce;
        return this;
    }

    public UpdateAppUtils tipCount(int tipCount) {
        this.tipCount = tipCount;
        return this;
    }

    /**
     * 获取apk的版本号 currentVersionCode
     */
    private void getAPPLocalVersion(Context ctx) {
        PackageManager manager = ctx.getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(ctx.getPackageName(), 0);
            localVersionName = info.versionName; // 版本名
            localVersionCode = info.versionCode; // 版本号
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void update() {
        switch (checkBy) {
            case CHECK_BY_VERSION_CODE:
                if (serverVersionCode > localVersionCode) {
                    toUpdate();
                } else {
                    Log.i(TAG, "当前版本是最新版本" + serverVersionCode + "/" + serverVersionName);
                    Toast.makeText(activity, "当前已是最新版本", Toast.LENGTH_LONG).show();
                }
                break;

            case CHECK_BY_VERSION_NAME:
                if (!serverVersionName.equals(localVersionName)) {
                    toUpdate();
                } else {
                    Log.i(TAG, "当前版本是最新版本" + serverVersionCode + "/" + serverVersionName);
                    Toast.makeText(activity, "当前已是最新版本", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private void toUpdate() {
        if (TextUtils.isEmpty(filePath)) {
            filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download";
        }
        String filePa = filePath + "/" + apkName;
        if (new File(filePa).exists()) {
            // apk已下载，直接安装
            install(filePa);
        } else {
            if (tipCount == -1) {
                realUpdate();
            } else if (tipCount > 0) {
                int count = (int) SPUtils.get(activity, TIP_COUNT, 1);
                if (tipCount >= count) {
                    realUpdate();
                }
            }
        }
    }

    /**
     * 已下载apk，提示安装
     */
    private void install(final String path) {
        ConfirmDialog dialog = new ConfirmDialog(activity, new ConfirmDialog.Callback() {
            @Override
            public void callback(int position) {
                switch (position) {
                    case 0:  // leftBtn
                        if (isForce) {
                            System.exit(0);
                        } else {
                            // TODO: 2017/9/4
                        }
                        break;

                    case 1:  // rightBtn
                        installApk(activity, path);
                        break;
                }
            }
        });

        String content = "安装包已下载，请安装！";
        dialog.setTitle("新版本" + serverVersionName)
                .setContent(content)
                .setLeftBtnText("稍后安装")
                .setRightBtnText("立即安装");
        dialog.setCancelable(false);
        dialog.show();
    }

    /**
     * 下载apk
     */
    private void realUpdate() {
        ConfirmDialog dialog = new ConfirmDialog(activity, new ConfirmDialog.Callback() {
            @Override
            public void callback(int position) {
                switch (position) {
                    case 0:  // leftBtn
                        if (isForce) {
                            System.exit(0);
                        } else {
                            // TODO: 2017/9/4 记录提示框弹出次数
                            if (tipCount > 0) {
                                int count = (int) SPUtils.get(activity, TIP_COUNT, 1);
                                count++;
                                SPUtils.put(activity, TIP_COUNT, count);
                            }
                        }
                        break;

                    case 1:  // rightBtn
                        if (downloadBy == DOWNLOAD_BY_APP) {
                            if (isWifiConnected(activity)) {
                                DownloadAppUtils.downloadApk(activity, apkUrl, filePath, apkName);
                            } else {
                                new ConfirmDialog(activity, new ConfirmDialog.Callback() {
                                    @Override
                                    public void callback(int position) {
                                        if (position == 1) {
                                            DownloadAppUtils.downloadApk(activity, apkUrl, filePath, apkName);
                                        } else {
                                            if (isForce) activity.finish();
                                        }
                                    }
                                }).setContent("目前手机不是WiFi状态\n确认是否继续下载更新？").show();
                            }

                        } else if (downloadBy == DOWNLOAD_BY_BROWSER) {
                            DownloadAppUtils.downloadForWebView(activity, apkUrl);
                        }
                        break;
                }
            }
        });

        String content = ""; //"发现新版本:" + serverVersionName + "\n是否下载更新?";
        if (!TextUtils.isEmpty(updateInfo)) {
            content = updateInfo; //"发现新版本:" + serverVersionName + "是否下载更新?\n\n" + updateInfo;
        }
        dialog.setTitle("新版本" + serverVersionName)
                .setContent(content)
                .setLeftBtnText(updateLeftText)
                .setRightBtnText(updateRightText);
        dialog.setCancelable(false);
        dialog.show();
    }

    /**
     * 检测wifi是否连接
     */
    public static boolean isWifiConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                return true;
            }
        }
        return false;
    }


    /**
     * 调用系统安装apk
     *
     * @param context
     * @param filePath apk文件路径
     */
    public static void installApk(Context context, String filePath) {
        if (!TextUtils.isEmpty(filePath)) {
            Intent i = new Intent(Intent.ACTION_VIEW);
            File apkFile = new File(filePath);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                i.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Uri contentUri = FileProvider.getUriForFile(
                        context, context.getPackageName() + ".fileprovider", apkFile);
                i.setDataAndType(contentUri, "application/vnd.android.package-archive");
            } else {
                i.setDataAndType(Uri.fromFile(apkFile),
                        "application/vnd.android.package-archive");
            }
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    }

}
