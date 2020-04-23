package com.reactlibrary;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class DeviceLogModule extends ReactContextBaseJavaModule
    implements ActivityEventListener {

    private final int REQUEST_SEND_EMAIL = 0;
    private final int RESULT_SUCCESS = 0;
    private final int RESULT_FAILED = -1;

    private final ReactApplicationContext reactContext;

    private Callback lastCallback;

    public DeviceLogModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "DeviceLog";
    }

    @ReactMethod
    public void emailDeviceLog(String emailAddress, Callback callback) {
        Uri logUri = null;
        try {
            File logFile = generateLog();
            logUri = FileProvider.getUriForFile(
                    reactContext,
                    "com.reactlibrary.devicelog.provider", //(use your app signature + ".provider" )
                    logFile);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(reactContext, "Failed to generate device log", Toast.LENGTH_SHORT).show();
            callback.invoke(RESULT_FAILED);
            return;
        }

        Intent selectorIntent = new Intent(Intent.ACTION_SENDTO);
        selectorIntent.setData(Uri.parse("mailto:"));

        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{emailAddress});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Device Log");
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Device: " + getDeviceInfo());
        emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        emailIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//        emailIntent.setSelector(selectorIntent);
        emailIntent.putExtra(Intent.EXTRA_STREAM, logUri);
        emailIntent.setType("multipart/");

        if (emailIntent.resolveActivity(reactContext.getPackageManager()) != null) {
            reactContext.startActivityForResult(emailIntent, REQUEST_SEND_EMAIL, null);
            lastCallback = callback;
        } else {
            Toast.makeText(reactContext, "Failed to send device log", Toast.LENGTH_SHORT).show();
            callback.invoke(RESULT_FAILED);
        }
    }

    private File generateLog() throws IOException {
//            File logDir = new File(Environment.getExternalStorageDirectory(), "logs");
//            if (!logDir.exists()) {
//                logDir.mkdir();
//            }
//            String filename = "device_log_" + new Date().getTime() + ".log";
//            File logFile = new File(logDir, filename);
            File logFile = File.createTempFile("device-log-", ".log", reactContext.getCacheDir());
            String[] cmd = new String[] { "logcat", "-f", logFile.getAbsolutePath(), "-v", "time", "*:*" };
            Runtime.getRuntime().exec(cmd);
            Toast.makeText(reactContext, "Device log generated", Toast.LENGTH_SHORT).show();
            return logFile;
    }

    private String getDeviceInfo() {
        return Build.MANUFACTURER
                + " " + Build.MODEL + " " + Build.VERSION.RELEASE
                + " " + Build.VERSION_CODES.class.getFields()[android.os.Build.VERSION.SDK_INT].getName();
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SEND_EMAIL && lastCallback != null) {
            if (resultCode == Activity.RESULT_OK) {
                lastCallback.invoke(RESULT_SUCCESS);
            } else {
                lastCallback.invoke(RESULT_FAILED);
            }
        }
    }

    @Override
    public void onNewIntent(Intent intent) {

    }
}
