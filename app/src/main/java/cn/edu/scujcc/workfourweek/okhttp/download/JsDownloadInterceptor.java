package cn.edu.scujcc.workfourweek.okhttp.download;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * @author Administrator
 */
public class JsDownloadInterceptor implements Interceptor {

    private static final String TAG = "LogUtil.JsInterceptor";
    private JsResponseBody.JsDownLoadListener mListener;

    public JsDownloadInterceptor(JsResponseBody.JsDownLoadListener mListener) {
        this.mListener = mListener;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());
        return response.newBuilder().body(new JsResponseBody(response.body(), mListener)).build();
    }
}
