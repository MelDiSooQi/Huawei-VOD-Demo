package com.voddemo.huawei;

import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;

import com.huawei.hms.support.hwid.result.AuthHuaweiId;

import androidx.appcompat.app.AlertDialog;

/**
 * Created by MelDiSooQi on 1/18/2020.
 */
public class BaseApplication extends Application {
    public static final String TAG = BaseApplication.class.getSimpleName();

    private static BaseApplication mInstance;

    public AuthHuaweiId huaweiAccount;

    public BaseApplication() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public static synchronized BaseApplication getInstance() {
        return mInstance;
    }

    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    public void DialogHandler(Context context, String title, String message,
                         String button1Description, DialogInterface.OnClickListener onClickButton1Listener) {
        AlertDialog alertDialog;

        alertDialog = new AlertDialog.Builder(context).create();

        alertDialog.setTitle(title);

        alertDialog.setMessage(message);

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, button1Description, onClickButton1Listener);

        alertDialog.setCancelable(false);
        alertDialog.show();
    }
}
