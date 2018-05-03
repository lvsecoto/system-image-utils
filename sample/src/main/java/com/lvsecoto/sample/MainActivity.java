package com.lvsecoto.sample;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.lvsecoto.system.image.utils.SystemImageUtils;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA = 0;

    private static final int REQUEST_GALLEY = 1;

    private static final int REQUEST_MULTIPLE = 2;

    private static final int REQUEST_CROP = 3;

    private static final int REQUEST_NOT_PROCESS = -1;

    private Uri mImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toast.makeText(this,
                android.os.Build.MANUFACTURER, Toast.LENGTH_LONG).show();
    }

    public void camera(View view) {
        mImageUri = SystemImageUtils.getImageFromCamera(this, REQUEST_CAMERA);
    }

    public void galley(View view) {
        SystemImageUtils.getImageFromGalley(this, REQUEST_GALLEY);
    }

    public void multiple(View view) {
        SystemImageUtils.getImagesFromGalley(this, REQUEST_MULTIPLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_CAMERA:
                mImageUri = SystemImageUtils.cropImage(this, mImageUri, REQUEST_CROP, getWidth(), getHeigh());
                break;
            case REQUEST_GALLEY:
                mImageUri = SystemImageUtils.cropImage(
                        this,
                        SystemImageUtils.onGetImageFromGalleyFinish(this, data),
                        REQUEST_CROP,
                        getWidth(), getHeigh());
                break;
            case REQUEST_MULTIPLE:
                ArrayList<Uri> imageUris = SystemImageUtils.onGetImagesFromGalleyFinish(this, data);
                for (Uri uri : imageUris) {
                    SystemImageUtils.cropImage(
                            this,
                            uri,
                            REQUEST_NOT_PROCESS,
                            getWidth(), getHeigh());
                }
                break;

            case REQUEST_CROP:
                setImage(mImageUri);
                setPath(SystemImageUtils.getPathFromUri(this, mImageUri));
                break;
        }

    }

    private int getWidth() {
        try {
            return Math.max(Integer.valueOf(
                    ((EditText) findViewById(R.id.width)).getText().toString()
            ), 100);
        } catch (Exception ignored) {
            return 100;
        }
    }

    private int getHeigh() {
        try {
            return Math.max(Integer.valueOf(
                    ((EditText) findViewById(R.id.height)).getText().toString()
            ), 100);
        } catch (Exception ignored) {
            return 100;
        }
    }

    private void setImage(Uri uri) {
        ((ImageView) findViewById(R.id.img)).setImageURI(uri);
    }

    private void setPath(String path) {
        ((TextView) findViewById((R.id.path))).setText(path);
    }
}
