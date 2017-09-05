package com.library.appupdate.customview;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.library.appupdate.R;
import com.library.appupdate.callback.DialogCallback;

/**
 * Created by chenxz on 2017/9/4.
 */

public class ConfirmDialog extends Dialog {

    private DialogCallback mCallback;

    private TextView content;
    private TextView cancelBtn;
    private TextView sureBtn;

    public ConfirmDialog(Context context, DialogCallback callback) {
        super(context, R.style.CustomDialog);
        this.mCallback = callback;
        setCustomDialog();
    }

    private void setCustomDialog() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_confirm,null);
        content = (TextView) view.findViewById(R.id.dialog_confirm_content);
        cancelBtn = (TextView) view.findViewById(R.id.dialog_confirm_cancle);
        sureBtn = (TextView) view.findViewById(R.id.dialog_confirm_sure);

        sureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.callback(1);
                ConfirmDialog.this.cancel();
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.callback(0);
                ConfirmDialog.this.cancel();
            }
        });
        super.setContentView(view);
    }

    public ConfirmDialog setContent(String s){
        content.setText(s);
        return this;
    }

    public ConfirmDialog setLeftBtnText(String s){
        cancelBtn.setText(s);
        return this;
    }

    public ConfirmDialog setRightBtnText(String s){
        sureBtn.setText(s);
        return this;
    }
}
