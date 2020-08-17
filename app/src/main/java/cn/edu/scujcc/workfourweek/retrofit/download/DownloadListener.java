package cn.edu.scujcc.workfourweek.retrofit.download;

import java.io.File;

/**
 * @author Administrator
 */
public interface DownloadListener {
    void onFinish(File file);

    void onProgress(int progress, long downloadedLengthKb, long totalLengthKb);

    void onFailed(String errMsg);
}