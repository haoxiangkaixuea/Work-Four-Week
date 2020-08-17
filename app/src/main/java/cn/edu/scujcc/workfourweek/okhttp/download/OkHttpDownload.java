package cn.edu.scujcc.workfourweek.okhttp.download;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import cn.edu.scujcc.workfourweek.R;

/**
 * @author Administrator
 */
public class OkHttpDownload extends AppCompatActivity implements View.OnClickListener {

    private DownloadService.DownloadBind mBind;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBind = (DownloadService.DownloadBind) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (mBind != null) {
                mBind.cancelDownload();
            }
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);
        Toolbar toolbar = findViewById(R.id.toolbar);

        Button bStart = findViewById(R.id.bStart);
        Button bPause = findViewById(R.id.bPause);
        Button bCancel = findViewById(R.id.bCancel);
        bStart.setOnClickListener(this);
        bPause.setOnClickListener(this);
        bCancel.setOnClickListener(this);

        Intent intent = new Intent(this, DownloadService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
        bindService(intent, mConnection, BIND_AUTO_CREATE);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bStart:
                String url = "http://fyq.fangyinqing.com/version/downloadFile";
                mBind.startDownload(url);
                break;
            case R.id.bPause:
                mBind.cancelDownload();
                break;
            case R.id.bCancel:
                mBind.cancelDownload();
                break;
            default:
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "拒绝权限，无法下载", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
        if (mBind != null) {
            mBind.cancelDownload();
        }
    }
}
