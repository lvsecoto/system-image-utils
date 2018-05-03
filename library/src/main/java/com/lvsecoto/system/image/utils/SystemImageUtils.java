package com.lvsecoto.system.image.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

public class SystemImageUtils {

    /**
     * @return 相机获取的图片Uri，在{@link Activity#onActivityResult(int, int, Intent) 中使用}
     */
    public static Uri getImageFromCamera(Activity activity, int requestCode) {
        Uri uri = getProviderUriIfNeed(activity, createFile(activity));

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        activity.startActivityForResult(intent, requestCode);

        return uri;
    }

    private static Uri getProviderUriIfNeed(Context context, File file) {
        Uri uri;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            uri = getUri(file);
        } else {
            uri = getProviderUri(context, file);
        }
        return uri;
    }

    private static File createFile(Context context) {
        return new File(context.getExternalCacheDir().getAbsolutePath() + "/" + UUID.randomUUID().toString() + ".jpg");
    }

    private static Uri getUri(File file) {
        return Uri.fromFile(file);
    }

    private static Uri getProviderUri(Context context, File file) {
        return FileProvider.getUriForFile(
                context,
                context.getPackageName() + ".provider",
                file);
    }

    /**
     * 在@{@link Activity#onActivityResult(int, int, Intent)}中调用
     * {@link #onGetImageFromGalleyFinish(Activity, Intent)}
     * 获取相册图片Uri
     */
    public static void getImageFromGalley(Activity activity, int requestCode) {
        Intent intent = new Intent();
        intent.setType("image/*");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        } else {
            intent.setAction(Intent.ACTION_GET_CONTENT);
        }

        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * 在@{@link Activity#onActivityResult(int, int, Intent)}中调用
     * {@link #onGetImagesFromGalleyFinish(Activity, Intent)}
     * 获取相册多张图片Uri
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static void getImagesFromGalley(Activity activity, int requestCode) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);

        activity.startActivityForResult(Intent.createChooser(intent, "Select Picture"), requestCode);
    }

    /**
     * @see #getImageFromGalley(Activity, int)
     */
    public static Uri onGetImageFromGalleyFinish(Activity activity, Intent data) {
        Uri imgUriSel = data.getData();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //打开相册会返回一个经过图像选择器安全化的Uri，直接放入裁剪程序会不识别，抛出[暂不支持此类型：华为7.0]
            //formatUri会返回根据Uri解析出的真实路径
            String imgPathSel = UriUtils.formatUri(activity, imgUriSel);
            //根据真实路径转成File,然后通过应用程序重新安全化，再放入裁剪程序中才可以识别
            imgUriSel = getProviderUriIfNeed(activity, new File(imgPathSel));
        }

        return imgUriSel;
    }

    /**
     * @see #getImagesFromGalley(Activity, int)
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static ArrayList<Uri> onGetImagesFromGalleyFinish(Activity activity, Intent data) {
        ArrayList<Uri> imgUris = new ArrayList<>();

        if (data.getData() != null) {
            Uri imgUriSel = data.getData();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                //打开相册会返回一个经过图像选择器安全化的Uri，直接放入裁剪程序会不识别，抛出[暂不支持此类型：华为7.0]
                //formatUri会返回根据Uri解析出的真实路径
                String imgPathSel = UriUtils.formatUri(activity, imgUriSel);
                //根据真实路径转成File,然后通过应用程序重新安全化，再放入裁剪程序中才可以识别
                imgUriSel = getProviderUriIfNeed(activity, new File(imgPathSel));
            }

            imgUris.add(imgUriSel);

        } else if (data.getClipData() != null) {
            ClipData clipData = data.getClipData();
            for (int i = 0; i < clipData.getItemCount(); i++) {

                Uri imgUriSel = clipData.getItemAt(i).getUri();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    //打开相册会返回一个经过图像选择器安全化的Uri，直接放入裁剪程序会不识别，抛出[暂不支持此类型：华为7.0]
                    //formatUri会返回根据Uri解析出的真实路径
                    String imgPathSel = UriUtils.formatUri(activity, imgUriSel);
                    //根据真实路径转成File,然后通过应用程序重新安全化，再放入裁剪程序中才可以识别
                    imgUriSel = getProviderUriIfNeed(activity, new File(imgPathSel));
                }

                imgUris.add(imgUriSel);
            }
        }

        return imgUris;
    }

    /**
     * @param width  输出图片宽度
     * @param height 输出图片高度
     * @return 裁剪好的图片Uri，在{@link Activity#onActivityResult(int, int, Intent)}中使用
     */
    public static Uri cropImage(Activity activity,
                                Uri image,
                                int requestCode,
                                int width,
                                int height) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        Uri outputImageUri = getUri(createFile(activity));

        intent.setDataAndType(image, "image/*");
        intent.putExtra("crop", true);

        if (Build.MANUFACTURER.equals("HUAWEI")) {
            setAspectForHuaWei(width, height, intent);
        } else {
            intent.putExtra("aspectX", width);
            intent.putExtra("aspectY", height);
        }
        intent.putExtra("outputX", width);
        intent.putExtra("outputY", height);
        intent.putExtra("return-data", false);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputImageUri);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.putExtra("noFaceDetection", true);
        activity.startActivityForResult(intent, requestCode);

        return outputImageUri;
    }

    /**
     * 在华为手机上，如果裁切框宽高比是1:1裁切框会变成圆形，需要特殊处理
     */
    private static void setAspectForHuaWei(int width, int height, Intent intent) {
        if (width == height) {
            if (width > 10000) {
                intent.putExtra("aspectX", width - 1);
                intent.putExtra("aspectY", height);
            } else {
                intent.putExtra("aspectX", width * 10000 - 1);
                intent.putExtra("aspectY", height * 10000);
            }
        }
    }

    /**
     * 取得图片路径
     */
    public static String getPathFromUri(Context context, Uri uri) {
        return UriUtils.formatUri(context, uri);
    }
}
