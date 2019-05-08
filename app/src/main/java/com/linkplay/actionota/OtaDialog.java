package com.linkplay.actionota;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public abstract class OtaDialog extends Dialog {


    private View btCan;
    private Button btStart;
    private ProgressBar progressBar;
    private TextView textPath;

    public OtaDialog(Context context) {
        super(context);
        init();
    }

    public OtaDialog(Context context, int themeResId) {
        super(context, themeResId);
        init();
    }

    protected OtaDialog(Context context, boolean cancelable, DialogInterface.OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        init();
    }

    private void init() {

        setCancelable(false);
        setCanceledOnTouchOutside(false);
        setContentView(R.layout.ota_dialog);

        btCan = findViewById(R.id.btn_can);
        btStart = findViewById(R.id.btn_start);

        progressBar = findViewById(R.id.progress_bar_h);
        textPath = findViewById(R.id.file_path);

        btStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ("开始".equals(btStart.getText())){
                    onStartOta();
                    btStart.setEnabled(false);
                    btStart.setText("升级中");
                }else{
                    cancel();
                }
            }
        });
        btCan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel();
                onStopOta();
            }
        });
    }

    public abstract void onStartOta();
    public abstract void onStopOta();

    public void setProgress(float progress){
        progressBar.setProgress((int) progress);
    }

    @Override
    public void show() {
        super.show();
    }

    @Override
    public void cancel() {
        super.cancel();
    }

    @SuppressLint("SetTextI18n")
    public void setOtaPath(String otaPath) {
        textPath.setText(" 请把文件放入： "+otaPath + "\n 命名： action.OTA");
    }

    public void otaEnd(){
        btStart.setEnabled(true);
        btStart.setText("升级完成");
    }

    public void otaError() {
        btStart.setEnabled(true);
        btStart.setText("升级失败");
    }
}
