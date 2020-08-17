package cn.edu.scujcc.workfourweek.okhttp.download;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author Administrator
 */
public class DownloadTask extends AsyncTask<String, Integer, Integer> implements JsResponseBody.JsDownLoadListener {

    private static final String TAG = "DownloadTask";

    private static final int TYPE_SUCCESS = 0;
    private static final int TYPE_FAILED = 1;
    private static final int TYPE_PAUSE = 2;
    private static final int TYPE_CANCELED = 3;

    private DownloadListener mListener;

    private boolean isCanceled = false;
    //private boolean isPaused = false;
    /**
     * 上次下载的进度
     */
    private int lastProgress;
    /**
     * 默认的下载文件名
     */
    private String fileName = "/fyq_new_version.apk";
    private Call call;
    private File file;

    public DownloadTask(DownloadListener listener) {
        mListener = listener;
    }

    @Override
    protected Integer doInBackground(String... strings) {
        InputStream is = null;
        RandomAccessFile savedFile = null;
        file = null;
        try {
            String downloadUrl = strings[0];
            String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
            Log.d(TAG, "doInBackground: start ");
            OkHttpClient client = OkHttpUtil.getClient(this);
            Request request = new Request.Builder()
                    .url(downloadUrl)
                    .build();
            call = client.newCall(request);
            Response response = call.execute();
            if (response.isSuccessful() && response.body() != null) {
                String description = response.header("Content-Disposition");
                if (description != null) {
                    if (description.contains("filename=")) {
                        fileName = "/" + description.substring(description.lastIndexOf("=") + 1);
                    }
                }
                file = new File(directory + fileName);
                Log.d(TAG, "doInBackground: fileName=" + fileName);

                is = response.body().byteStream();
                savedFile = new RandomAccessFile(file, "rw");
                savedFile.seek(0);
                byte[] bytes = new byte[2048];
                int len;
                while ((len = is.read(bytes)) != -1) {
                    savedFile.write(bytes, 0, len);
                }
                response.close();
                return TYPE_SUCCESS;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (savedFile != null) {
                    savedFile.close();
                }
                if (isCanceled && file != null) {
                    file.delete();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (isCanceled) {
            return TYPE_CANCELED;
        }
        return TYPE_FAILED;
    }

    public String getFilePath() {
        return file.getPath();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        int progress = values[0];
        if (progress > lastProgress) {
            if (mListener != null) {
                mListener.onProgress(progress);
            }
            lastProgress = progress;
        }
    }

    @Override
    protected void onPostExecute(Integer integer) {
        switch (integer) {
            case TYPE_SUCCESS:
                mListener.onSuccess();
                break;
            case TYPE_FAILED:
                mListener.onFailed();
                break;
            case TYPE_CANCELED:
                mListener.onCanceled();
                break;
            case TYPE_PAUSE:
                mListener.onCanceled();
                break;
            default:
                break;
        }
    }

    /**
     * 取消下载
     */
    public void cancelDownload() {
        isCanceled = true;
        if (call != null) {
            call.cancel();
        }
    }


    /*
            获取下载文件的大小
             */
//    private long getContentLength(String downloadUrl) throws IOException {
//        OkHttpClient client = OkHttpUtil.getClient(this);
//        Request request = new Request.Builder()
//                .url(downloadUrl)
//                .build();
//        Call call=client.newCall(request);
//        Response response = call.execute();
//        Log.i(TAG, "getContentLength: get");
//        if (response != null && response.isSuccessful()) {
//            call.cancel();
//            if (response.body() != null) {
//                long length = response.body().contentLength();
//                response.body().close();
//                Log.i(TAG, "getContentLength: length="+length);
//                return length;
//            }
//        }
//        return 0;
//    }

    @Override
    public void onProgress(int percent, long downloaded, long contentLength) {
        publishProgress(percent);
    }
}
