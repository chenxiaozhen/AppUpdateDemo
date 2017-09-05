package com.library.appupdate.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.library.appupdate.utils.UpdateAppUtils;

public class UpdateAppReceiver extends BroadcastReceiver {
    public UpdateAppReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String pathString = intent.getStringExtra("downloadFile");
        UpdateAppUtils.installApk(context, pathString);
    }
}
