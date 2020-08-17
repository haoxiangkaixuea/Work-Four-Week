package cn.edu.scujcc.workfourweek.okhttp.download;

/**
 * @author Administrator
 */
public interface DownloadListener {

    void onProgress(int progress);

    void onSuccess();

    void onFailed();

    void onPaused();

    void onCanceled();
}
