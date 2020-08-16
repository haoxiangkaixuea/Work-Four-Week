package cn.edu.scujcc.workfourweek;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import cn.edu.scujcc.workfourweek.okhttp.upload.OkUpload;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button okUpload;
    private Button okDownload;
    private Button retrofitUpload;
    private Button retrofitDownload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        okUpload = findViewById(R.id.ok_upload);
        okDownload = findViewById(R.id.ok_download);
        retrofitUpload = findViewById(R.id.retrofit_upload);
        retrofitDownload = findViewById(R.id.retrofit_download);

        okUpload.setOnClickListener(this);
        okDownload.setOnClickListener(this);
        retrofitUpload.setOnClickListener(this);
        retrofitDownload.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ok_upload:
                Intent okUpload = new Intent(this, OkUpload.class);
                startActivity(okUpload);
                break;
            case R.id.ok_download:
                Intent okDownload = new Intent(this, OkUpload.class);
                startActivity(okDownload);
                break;
            case R.id.retrofit_upload:
                Intent retrofitUpload = new Intent(this, OkUpload.class);
                startActivity(retrofitUpload);
                break;
            case R.id.retrofit_download:
                Intent retrofitDownload = new Intent(this, OkUpload.class);
                startActivity(retrofitDownload);
                break;
            default:
                break;
        }
    }
}