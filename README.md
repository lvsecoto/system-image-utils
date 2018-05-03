# About

这个工具类可以方便地调用系统接口来选择和裁切图片

# How To Use it

## 添加依赖

在根目录下的`build.gradle`添加

```gradle
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

在模块目录的`build.gradle`下添加

```
dependencies {
    implementation 'com.github.lvsecoto:system-image-utils:last.version'
}
```

## 从相机中打开一张图片

```java
Uri imageUri = SystemImageUtils.getImageFromCamera(activity, REQUEST_CODE);
```

*这个Uri必须在onActivityResult后才能使用*

## 从相册中打开一张图片

任意地方调用

```java
SystemImageUtils.getImageFromGalley(activity, REQUEST_CODE);
```

然后在onActivityResult中获取图片Uri

```java
Uri imageUri = SystemImageUtils.onGetImageFromGalleyFinish(activity, data);
```

## 从相册中打开多张图片

任意地方调用

```java
SystemImageUtils.getImagesFromGalley(activity, REQUEST_CODE);
```

然后在onActivityResult中获取图片Uri

```java
ArrayList<Uri> imageUris = GetImagesBySystemUtils.onGetImagesFromGalleyFinish(activity, data);
```

## 裁切一张图片

要裁切图片`srcImgUri`成 300 * 300 大小

```java
Uri imgUri = SystemImageUtils.cropImage(activity, srcImgUri, REQUEST_CODE, 300, 300);
```

*这个Uri必须在onActivityResult后才能使用*

## Android 7.0 FileProvider 额外配置

先在`AndroidManifest.xml`添加FileProvider

```xml
        //<activity...
        <provider
            android:name="android.support.v4.content.FileProvider"
            android:authorities="com.lvsecoto.system.image.utils.provider"
            android:exported="false"
            android:grantUriPermissions="true">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/file_paths" />
        </provider>
```

添加`res/xml/file_paths.xml`，在其中定义需要暴露的文件路径

```xml
<?xml version="1.0" encoding="utf-8"?>
<paths>
    <root-path
        name="root_path"
        path="." />
</paths>
```

这里直接用根目录root-path暴露所有位置就行了