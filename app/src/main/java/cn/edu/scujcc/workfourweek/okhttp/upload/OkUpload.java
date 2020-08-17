package cn.edu.scujcc.workfourweek.okhttp.upload;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.nex3z.flowlayout.FlowLayout;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cn.edu.scujcc.workfourweek.R;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @author Administrator
 */
public class OkUpload extends AppCompatActivity implements View.OnClickListener {
    /**
     * 添加图片
     */
    private LinearLayout llAdd;
    /**
     * 提交
     */
    private Button btCommit;
    private FlowLayout flContent;
    private int dim160;
    /**
     * 选择图片集合
     */
    private List<LocalMedia> selectList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ok_upload);
        initView();
        initListener();
        initDate();
        //判断是否有访问内存的权限
        if (ContextCompat.checkSelfPermission(OkUpload.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(OkUpload.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    public void initView() {
        llAdd = findViewById(R.id.ll_add);
        btCommit = findViewById(R.id.bt_commit);
        flContent = findViewById(R.id.fl_content);
    }

    public void initListener() {
        btCommit.setOnClickListener(this);
        llAdd.setOnClickListener(this);
    }

    public void initDate() {
        selectList = new ArrayList<>();
        dim160 = getResources().getDimensionPixelSize(R.dimen.d_160);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_commit:
                upFile();
                break;
            case R.id.ll_add:
                goSelectPic();
                break;
            default:
                break;
        }
    }

    /**
     * 上传文件
     */
    private void upFile() {
        int size = selectList == null ? 0 : selectList.size();
        if (size >= 1) {
            List<File> files = new ArrayList();
            for (int i = 0; i < selectList.size(); i++) {
                String pathname = selectList.get(i).getCompressPath();
                File file = new File(pathname);
                files.add(file);
            }
            //这里是后台地址
            String filesUrl = "/passPayShop/image/";
            uploadMultiFiles(filesUrl, files);
        } else {
            Toast.makeText(this, "请选择图片再上传", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 单图上传
     */
    private void uploadMultiFile(String url, File file) {
        RequestBody fileBody = RequestBody.create(MediaType.parse("image/*"), file);
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), fileBody)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        final okhttp3.OkHttpClient.Builder httpBuilder = new OkHttpClient.Builder();
        OkHttpClient okHttpClient = httpBuilder
                //设置超时
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Toast.makeText(OkUpload.this, "上传失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                try {
                    String jsonStr = response.body().string();
                    Toast.makeText(OkUpload.this, "上传成功", Toast.LENGTH_SHORT).show();
                    Log.i("EvaluateActivity", "uploadMultiFile() response=" + jsonStr);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 上传多图片
     */
    private void uploadMultiFiles(String url, List<File> files) {
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        for (int i = 0; i < files.size(); i++) {
            File file = files.get(i);
            RequestBody fileBody = RequestBody.create(MediaType.parse("image/*"), file);
            builder.addFormDataPart("file", file.getName(), fileBody);
        }

        MultipartBody multipartBody = builder.build();
        Request request = new Request.Builder()
                .url(url)
                .post(multipartBody)
                .build();

        final okhttp3.OkHttpClient.Builder httpBuilder = new OkHttpClient.Builder();
        OkHttpClient okHttpClient = httpBuilder
                //设置超时
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Toast.makeText(OkUpload.this, "上传失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                try {
                    String jsonStr = response.body().string();
                    Toast.makeText(OkUpload.this, "上传成功", Toast.LENGTH_SHORT).show();
                    Log.i("EvaluateActivity", "uploadMultiFile() response=" + jsonStr);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 选择图片
     */
    private void goSelectPic() {
        selectList.clear();
        flContent.removeAllViews();
        // 进入相册 以下是例子：用不到的api可以不写
        PictureSelector.create(this)
                //全部.PictureMimeType.ofAll()、图片.ofImage()
                .openGallery(PictureMimeType.ofImage())
                // 、视频.ofVideo()、音频.ofAudio()
                //  .theme(R.style.picture_white_style)
                // 主题样式(不设置为默认样式) 也可参考demo values/styles下 例如：R
                // .style.picture.white.style
                // 最大图片选择数量 int
                .maxSelectNum(9)
                // 最小选择数量 int
                .minSelectNum(0)
                // 每行显示个数 int
                .imageSpanCount(4)
                // 多选 or 单选 PictureConfig.MULTIPLE or
                .selectionMode(PictureConfig.MULTIPLE)
                // PictureConfig.SINGLE
                // 是否可预览图片 true or false
                .previewImage(true)
                // 是否可预览视频 true or false
                .previewVideo(false)
                // 是否可播放音频 true or false
                .enablePreviewAudio(false)
                // 是否显示拍照按钮 true or false
                .isCamera(true)
                // 拍照保存图片格式后缀,默认jpeg
                .imageFormat(PictureMimeType.PNG)
                // 图片列表点击 缩放效果 默认true
                .isZoomAnim(true)
                // glide 加载图片大小 0~1之间 如设置 .glideOverride()无效
                .sizeMultiplier(0.5f)
                // 自定义拍照保存路径,可不填
                .setOutputCameraPath("/CustomPath")
                // 是否裁剪 true or false
                .enableCrop(false)
                // 是否压缩 true or false
                .compress(true)
                .glideOverride(160, 160)
                //压缩图片保存地址
                .compressSavePath(getPath())
                // 是否传入已选图片 List<LocalMedia> list
                .selectionMedia(selectList)
                // 预览图片时 是否增强左右滑动图片体验(图片滑动一半即可看到上一张是否选中) true or false
                .previewEggs(true)
                // 小于100kb的图片不压缩
                .minimumCompressSize(100)
                //同步true或异步false 压缩 默认同步
                .synOrAsy(true)
                //结果回调onActivityResult code
                .forResult(PictureConfig.CHOOSE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PictureConfig.CHOOSE_REQUEST:
                    //选择图片后之前的上传的就要清除，重新上传
                    // 图片选择结果回调
                    selectList.clear();
                    selectList.addAll(PictureSelector.obtainMultipleResult(data));
                    // 如果裁剪并压缩了，已取压缩路径为准，因为是先裁剪后压缩的
                    for (int i = 0; i < selectList.size(); i++) {
                        LocalMedia media = selectList.get(i);
                        Log.d("图片", media.getPath());
                        ImageView imageView = new ImageView(this);
                        flContent.addView(imageView);
                        ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
                        layoutParams.width = dim160;
                        layoutParams.height = dim160;
                        RequestOptions options = new RequestOptions()
                                .centerCrop()
                                .placeholder(R.color.main_sel)
                                .diskCacheStrategy(DiskCacheStrategy.ALL);
                        Glide.with(this)
                                .load(media.getCompressPath())
                                .apply(options)
                                .into(imageView);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 请求权限
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "拒绝权限将无法使用程序", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }

    /**
     * 自定义压缩存储地址
     *
     * @return 保存路径
     */
    private String getPath() {
       // File eFile=getExternalFilesDir(null);
        String path = Environment.getExternalStorageDirectory() + "/passPayShop/image/";
        File file = new File(path);
        if (file.mkdirs()) {
            return path;
        }
        return path;
    }
}
