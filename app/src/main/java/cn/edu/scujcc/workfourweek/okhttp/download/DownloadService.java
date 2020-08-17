package cn.edu.scujcc.workfourweek.okhttp.download;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.util.List;

import cn.edu.scujcc.workfourweek.R;

/**
 * @author Administrator
 */
public class DownloadService extends Service {

    private static final String TAG = "DownloadService";

    private static final String CHANNEL_ONE_ID = "download_channel";
    private DownloadTask mDownloadTask;
    private String downloadUrl;
    private DownloadBind mBind = new DownloadBind();
    private String apkFilePath;
    private DownloadListener mDownloadListener = new DownloadListener() {

        @Override
        public void onProgress(int progress) {
            Intent intent = getIntent(OkHttpDownload.class);
            getNotificationManager().notify(1, getNotification("Downloading...", progress, intent));
        }

        @Override
        public void onSuccess() {
            if (mDownloadTask != null) {
                apkFilePath = mDownloadTask.getFilePath();
            }
            mDownloadTask = null;
            // 关闭前台下载进度条，创建一个新的通知
            stopForeground(true);
            Intent intent = getInstallIntent();
            getNotificationManager().notify(1, getNotification("Download success", 0, intent));
            Toast.makeText(DownloadService.this, "success", Toast.LENGTH_SHORT)
                    .show();
        }

        @Override
        public void onFailed() {
            mDownloadTask = null;
            //下载失败，关闭进度条，创建一个失败通知
            stopForeground(true);
            Intent intent = getIntent(OkHttpDownload.class);
            getNotificationManager().notify(1, getNotification("Download Failed", 0, intent));
            Toast.makeText(DownloadService.this, "failed", Toast.LENGTH_SHORT)
                    .show();
        }

        @Override
        public void onPaused() {
            mDownloadTask = null;
            //开启前台服务，通知栏中该通知也会变为不会随着点击或者滑动而删除。除非该service结束停止，这个通知也会随之被删除。
            //这个是结束前台的服务，通知栏中该通知会随着点击或者滑动而删除。参数是：是否删除之前发送的通知，
            // true：删除。false：不删除 （用手滑动或者点击通知会被删除）
            stopForeground(true);
            Toast.makeText(DownloadService.this, "paused", Toast.LENGTH_SHORT)
                    .show();
        }

        @Override
        public void onCanceled() {
            mDownloadTask = null;
            stopForeground(true);
            Toast.makeText(DownloadService.this, "canceled", Toast.LENGTH_SHORT)
                    .show();
        }
    };

    private Notification getNotification(String title, int progress, Intent intent) {
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "success");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ONE_ID, "download", NotificationManager.IMPORTANCE_LOW);
            getNotificationManager().createNotificationChannel(mChannel);
            builder.setChannelId(CHANNEL_ONE_ID);
        }
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        builder.setContentIntent(pendingIntent);
        builder.setContentTitle(title);
        if (progress > 0) {
            //当进度大于0的时候才需要显示进度
            builder.setContentText(progress + "%");
            builder.setProgress(100, progress, false);
        }
        return builder.build();
    }

    public Intent getIntent(Class activityClass) {
        return new Intent(this, activityClass);
    }

    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    private Intent getInstallIntent() {
        Log.i(TAG, "installApk: versionCode=" + Build.VERSION.SDK_INT);
        File apkFile = new File(apkFilePath);
        Intent install = new Intent(Intent.ACTION_VIEW);
        //判读版本是否在7.0以上
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            //在AndroidManifest中的android:authorities值
            Uri apkUri = FileProvider.getUriForFile(this, "cn.edu.scujcc.workfourweek.file_provider", apkFile);
            Log.i(TAG, "installApk: apkUri=" + apkUri);
            install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            //查询所有符合 intent 跳转目标应用类型的应用
            install.setDataAndType(apkUri, "application/vnd.android.package-archive");
            List<ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(install, PackageManager.MATCH_DEFAULT_ONLY);
            //然后全部授权
            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                grantUriPermission(packageName, apkUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
        } else {
            install.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
            install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        return install;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBind;
    }

    public class DownloadBind extends Binder {

        public void startDownload(String url) {
            if (mDownloadTask == null) {
                downloadUrl = url;
                mDownloadTask = new DownloadTask(mDownloadListener);
                mDownloadTask.execute(downloadUrl);
                Intent intent = getIntent(OkHttpDownload.class);
                startForeground(1, getNotification("Downloading...", 0, intent));
                Toast.makeText(DownloadService.this, "start download", Toast.LENGTH_SHORT)
                        .show();
            }
        }

        public void pauseDownload() {
            // if (mDownloadTask!=null){
            //  mDownloadTask.pauseDowload();
            // }
        }

        public void cancelDownload() {
            stopForeground(true);
            if (mDownloadTask != null) {
                mDownloadTask.cancelDownload();
            } else {
                if (downloadUrl != null) {
                    // 取消下载时，删除下载文件，关闭前台通知
                    String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
                    File files=getExternalFilesDir(null);
                    String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
                    File file = new File(directory, fileName);
                    if (file.exists()) {
                        file.delete();
                    }
                    getNotificationManager().cancel(1);
                    stopForeground(true);
                    Toast.makeText(DownloadService.this, "cancel download", Toast.LENGTH_SHORT)
                            .show();
                }
            }
        }
    }
}
