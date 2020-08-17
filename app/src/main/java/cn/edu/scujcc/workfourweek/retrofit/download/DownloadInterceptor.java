package cn.edu.scujcc.workfourweek.retrofit.download;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * @author Administrator
 */
public class DownloadInterceptor implements Interceptor {

    private DownloadListener listener;

    public DownloadInterceptor(DownloadListener listener) {
        this.listener = listener;
    }

    @NotNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        Response originalResponse = chain.proceed(chain.request());

        return originalResponse.newBuilder()
                .body(new DownloadResponseBody(originalResponse.body(), listener))
                .build();
    }
}
