package com.dlab.nttcweb;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.Array;
import java.util.List;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainA---";

    // WebView 控件
    private WebView wv_main;

    //定义变量
    private ValueCallback<Uri> uploadMessage;
    private ValueCallback<Uri[]> uploadMessageAboveL;

    /**
     * 选择相片来源的 Dialog
     */
    private HeadDialog headDialog;

    /**
     * 照片保存路径
     */
    private File cameraFile;

    private File file;
    private String path;

    /**
     * 图片后缀
     */
    private static final String TYPE_IMG_UP_PNG = ".PNG";
    private static final String TYPE_IMG_LOW_PNG = ".png";
    private static final String TYPE_IMG_UP_JPG = ".JPG";
    private static final String TYPE_IMG_LOW_JPG = ".jpg";
    private static final String TYPE_IMG_UP_JPEG = ".JPEG";
    private static final String TYPE_IMG_LOW_JPEG = ".jpeg";

    private static final int CHOOSE_PICTURE = 2;
    private static final int CAMERA_REQUEST = 1;

    private ReWebChomeClient mWebChromeClient = new ReWebChomeClient(new ReWebChomeClient.OpenFileChooserCallBack() {
        @Override
        public void openFileChooserCallBack(ValueCallback<Uri> uploadMsg, String acceptType) {//Android >=3.0
            uploadMessage = uploadMsg;
            openImageChooserActivity();
        }

        @Override
        public void showFileChooserCallBack(ValueCallback<Uri[]> filePathCallback) {// Android >= 5.0
            uploadMessageAboveL = filePathCallback;
            openImageChooserActivity();
        }
    });

    /**
     * 正式站
     */
    private static final String WEB_URL = "http://www.naturenode.org/home";

    // JS 调用 Android 方法第一步
//    @SuppressLint("JavascriptInterface")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 找控件
        initView();

        // 初始化选择相片来源的 Dialog
        headDialog = new HeadDialog(this);

        // 初始化 WebView 的一些设置
        initWebViewSettings();

        // 接收所有网站的证书
        wv_main.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                // 一定要移除 super 这句
//                super.onReceivedSslError(view, handler, error);
                // handler.cancel();// Android默认的处理方式
                handler.proceed();// 接受所有网站的证书
                // handleMessage(Message msg);// 进行其他处理
            }
        });

        // 请求权限
        MainActivityPermissionsDispatcher.applyPermissionsWithPermissionCheck(this);

        //设置WebChromeClient
        wv_main.setWebChromeClient(mWebChromeClient);

        // Android 调用 JS 方法
//        wv_main.post(new Runnable() {
//            @Override
//            public void run() {
//                wv_main.loadUrl("javascript:androidCallJS()");
//            }
//        });

        // JS 调用 Android 方法第二步
//        wv_main.addJavascriptInterface(this, "wv");

    }

    // JS 调用 Android 方法第三步
//    @JavascriptInterface
//    public void sayHello(String msg) {
//        Log.i("MainA---", "@JavascriptInterface---" + msg);
//    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebViewSettings() {

        WebSettings webSettings = wv_main.getSettings();
        // 如果访问的页面中要与 Javascript 交互，则 WebView 必须设置支持 Javascript
        webSettings.setJavaScriptEnabled(true);

        // 设置自适应屏幕，两者合用
//        webSettings.setUseWideViewPort(true); // 将图片调整到适合 webview 的大小
//        webSettings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小

//        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN); //支持内容重新布局

        //缩放操作
//        webSettings.setSupportZoom(true); // 支持缩放，默认为 true。是下面那个的前提。
//        webSettings.setBuiltInZoomControls(true); // 设置内置的缩放控件。若为 false，则该 WebView 不可缩放
//        webSettings.setDisplayZoomControls(false); // 隐藏原生的缩放控件
//        webSettings.setTextZoom(2); // 设置文本的缩放倍数，默认为 100
//
//        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);  // 提高渲染的优先级

//        webSettings.setStandardFontFamily(""); // 设置 WebView 的字体，默认字体为 "sans-serif"
//        webSettings.setDefaultFontSize(20); // 设置 WebView 字体的大小，默认大小为 16
//        webSettings.setMinimumFontSize(12); // 设置 WebView 支持的最小字体大小，默认为 8

        // 5.1 以上默认禁止了 https 和 http 混用，以下方式是开启
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        // 其他操作
//        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK); // 关闭 WebView 中缓存
        webSettings.setAllowFileAccess(true); // 设置可以访问文件
//        webSettings.setJavaScriptCanOpenWindowsAutomatically(true); // 支持通过 JS 打开新窗口
//        webSettings.setLoadsImagesAutomatically(true); // 支持自动加载图片
//        webSettings.setDefaultTextEncodingName("utf-8");// 设置编码格式
//        webSettings.setGeolocationEnabled(true); // 允许网页执行定位操作
//        webSettings.setUserAgentString("Mozilla/5.0 (Windows NT 10.0; WOW64; rv:50.0) Gecko/20100101 Firefox/50.0");//设置User-Agent

        // 不允许访问本地文件（不影响 assets 和 resources 资源的加载）
//        webSettings.setAllowFileAccess(false);
//        webSettings.setAllowFileAccessFromFileURLs(false);
//        webSettings.setAllowUniversalAccessFromFileURLs(false);

    }

    /**
     * 监听Back键按下事件
     * 设置webview的后退，如果后退没有网页了，则onBackPressed
     */
    @Override
    public void onBackPressed() {
        if (wv_main.canGoBack()) {
            wv_main.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {

        uploadMessage = null;
        uploadMessageAboveL = null;
        if (wv_main != null) {
            ((ViewGroup) wv_main.getParent()).removeView(wv_main);
            wv_main.destroy();
            wv_main = null;
        }
        super.onDestroy();
    }

    private void initView() {
        wv_main = findViewById(R.id.wv_main);
    }

    @NeedsPermission({Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void applyPermissions() {
        // 加载
        wv_main.loadUrl(WEB_URL);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @OnPermissionDenied({Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void onPermissionDenied() {
        Toast.makeText(MainActivity.this, "如需进行身份认证，请开启应用存储和相机权限。", Toast.LENGTH_SHORT).show();

        //引导用户至设置页手动授权
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getApplicationContext().getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);

    }

    @OnNeverAskAgain({Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void onNeverAskAgain() {

        Toast.makeText(MainActivity.this, "您已禁止该权限，请手动开启。", Toast.LENGTH_SHORT).show();

        //引导用户至设置页手动授权
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getApplicationContext().getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);

    }

    private void openImageChooserActivity() {

        headDialog.show();
        headDialog.setClickListener(new HeadDialog.ClickListenerInterface() {
            @Override
            public void doGetCamera() {

                // 相机
                headDialog.dismiss();

                // 已经授予权限
                camera();

            }

            @Override
            public void doGetPic() {

                // 图库
                headDialog.dismiss();

                // 已经授予权限
                gallery();

            }

            @Override
            public void doCancel() {

                // 取消
                headDialog.dismiss();

                if (uploadMessageAboveL != null) {
                    uploadMessageAboveL.onReceiveValue(null);
                    uploadMessageAboveL = null;
                } else if (uploadMessage != null) {
                    uploadMessage.onReceiveValue(null);
                    uploadMessage = null;
                }

            }

        });

    }

    /**
     * 相机
     */
    private void camera() {

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraFile = DiskUtils.generatePhotoFile(this);

        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            // 如果版本高于7.0，则需要用FileProvider来实现uri获取
            uri = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".fileprovider", cameraFile);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        } else {

            // 如果版本低于7.0，则传统方法实现本地视频播放
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(cameraFile));

        }

        this.startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }

    /**
     * 图库
     */
    private void gallery() {
        // 选择数据
        Intent intent = new Intent(Intent.ACTION_PICK);
        // 调用图库，获取所有本地图片
//        Intent intent = new Intent((Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, CHOOSE_PICTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            // 图库返回值
            case CHOOSE_PICTURE:

                if (data == null) {
                    if (uploadMessageAboveL != null) {
                        uploadMessageAboveL.onReceiveValue(null);
                        uploadMessageAboveL = null;
                    } else if (uploadMessage != null) {
                        uploadMessage.onReceiveValue(null);
                        uploadMessage = null;
                    }
                    break;
                }

                // 解决小米手机问题的方案
                Uri uri = getUri(data);

                // 图片路径
                path = getRealPathFromURI(uri);
                if (TextUtils.isEmpty(path)) {
                    // 不支持的图片格式
                    Toast.makeText(this, "图片格式不支持", Toast.LENGTH_SHORT).show();
                    if (uploadMessageAboveL != null) {
                        uploadMessageAboveL.onReceiveValue(null);
                        uploadMessageAboveL = null;
                    } else if (uploadMessage != null) {
                        uploadMessage.onReceiveValue(null);
                        uploadMessage = null;
                    }
                    break;
                }

                // 图片格式判断
                if (!path.endsWith(TYPE_IMG_LOW_PNG) && !path.endsWith(TYPE_IMG_UP_PNG)
                        && !path.endsWith(TYPE_IMG_LOW_JPG) && !path.endsWith(TYPE_IMG_UP_JPG)
                        && !path.endsWith(TYPE_IMG_LOW_JPEG) && !path.endsWith(TYPE_IMG_UP_JPEG)) {

                    // 不支持的图片格式
                    Toast.makeText(this, "图片格式不支持", Toast.LENGTH_SHORT).show();
                    if (uploadMessageAboveL != null) {
                        uploadMessageAboveL.onReceiveValue(null);
                        uploadMessageAboveL = null;
                    } else if (uploadMessage != null) {
                        uploadMessage.onReceiveValue(null);
                        uploadMessage = null;
                    }
                    break;
                }

                // 图片地址给 WebView 回调
                getImageWebView(path);

                break;

            // 相机
            case CAMERA_REQUEST:

                if (cameraFile == null) {
                    // 获取图片失败
                    Toast.makeText(MainActivity.this, "获取图片失败", Toast.LENGTH_SHORT).show();
                    if (uploadMessageAboveL != null) {
                        uploadMessageAboveL.onReceiveValue(null);
                        uploadMessageAboveL = null;
                    } else if (uploadMessage != null) {
                        uploadMessage.onReceiveValue(null);
                        uploadMessage = null;
                    }
                    break;
                }

                if (resultCode != RESULT_OK) {
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.dialog_personal_center_logout_cancel), Toast.LENGTH_SHORT).show();
                    if (uploadMessageAboveL != null) {
                        uploadMessageAboveL.onReceiveValue(null);
                        uploadMessageAboveL = null;
                    } else if (uploadMessage != null) {
                        uploadMessage.onReceiveValue(null);
                        uploadMessage = null;
                    }
                    break;
                }

                // 图片路径
                path = cameraFile.getAbsolutePath();
                // 图片路径非空判断
                if (TextUtils.isEmpty(path)) {
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.dialog_personal_center_logout_cancel), Toast.LENGTH_SHORT).show();
                    if (uploadMessageAboveL != null) {
                        uploadMessageAboveL.onReceiveValue(null);
                        uploadMessageAboveL = null;
                    } else if (uploadMessage != null) {
                        uploadMessage.onReceiveValue(null);
                        uploadMessage = null;
                    }
                    break;
                }

                // 图片格式判断
                if (!path.endsWith(TYPE_IMG_LOW_PNG) && !path.endsWith(TYPE_IMG_UP_PNG)
                        && !path.endsWith(TYPE_IMG_LOW_JPG) && !path.endsWith(TYPE_IMG_UP_JPG)
                        && !path.endsWith(TYPE_IMG_LOW_JPEG) && !path.endsWith(TYPE_IMG_UP_JPEG)) {
                    // 不支持的图片格式
                    Toast.makeText(this, "图片格式不支持", Toast.LENGTH_SHORT).show();
                    if (uploadMessageAboveL != null) {
                        uploadMessageAboveL.onReceiveValue(null);
                        uploadMessageAboveL = null;
                    } else if (uploadMessage != null) {
                        uploadMessage.onReceiveValue(null);
                        uploadMessage = null;
                    }
                    break;
                }

                // 图片地址给 WebView 回调
                getImageWebView(path);

                break;

        }

    }

    // 将图片路径返回给 WebView
    private void getImageWebView(String str_image) {
        if (!TextUtils.isEmpty(str_image)) {
            Uri uri = getImageContentUri(this, new File(str_image));
            if (uploadMessageAboveL != null) {
                Uri[] uris = new Uri[]{uri};
                uploadMessageAboveL.onReceiveValue(uris);
                uploadMessageAboveL = null;
            } else if (uploadMessage != null) {
                uploadMessage.onReceiveValue(uri);
                uploadMessage = null;
            }
        } else {
            if (uploadMessageAboveL != null) {
                uploadMessageAboveL.onReceiveValue(null);
                uploadMessageAboveL = null;
            } else if (uploadMessage != null) {
                uploadMessage.onReceiveValue(null);
                uploadMessage = null;
            }
        }
    }

    /**
     * 定义图片类型和 uri 的类型
     */
    private static final String TYPE_IMAGE = "image";
    private static final String TYPE_DUCUMENTS = "com.android.providers.media.documents";

    /**
     * 从媒体库获取图片的uri
     *
     * @param contentUri 传进来的 uri
     * @return 返回的图片 uri
     */
    public String getRealPathFromURI(Uri contentUri) {

        String res = null;

        if (DocumentsContract.isDocumentUri(this, contentUri)) {

            if (TYPE_DUCUMENTS.equals(contentUri.getAuthority())) {

                String docId = DocumentsContract.getDocumentId(contentUri);
                String[] split = docId.split(":");
                String type = split[0];

                Uri tempUri = null;

                if (TYPE_IMAGE.equals(type)) {
                    tempUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                }

                // 非空判断
                if (null == tempUri) {
                    return null;
                }

                String selection = MediaStore.Images.Media._ID + "=?";
                String[] selectionArgs = new String[]{split[1]};

                Cursor cursor = null;
                String column = MediaStore.Images.Media.DATA;
                String[] projection = {column};

                try {

                    cursor = getContentResolver().query(tempUri, projection, selection, selectionArgs, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        int index = cursor.getColumnIndexOrThrow(column);
                        res = cursor.getString(index);
                    }

                } finally {

                    if (cursor != null) {
                        cursor.close();
                    }

                }
            }

        } else {

            String[] project = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(contentUri, project, null, null, null);

            if (cursor != null && cursor.getCount() >= 1) {
                if (cursor.moveToFirst()) {
                    try {
                        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                        res = cursor.getString(columnIndex);
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                        Log.e(TAG, e.toString());
                    }
                }
                cursor.close();
            }

        }

        return res;

    }

    private static final String TYPE_FILE_SCHEME = "file";

    /**
     * 解决小米手机上获取图片路径为null的情况
     *
     * @param intent 传入的意图
     * @return 返回的 uri
     */
    private Uri getUri(android.content.Intent intent) {

        Uri uri = intent.getData();

        /**
         * 从文件选择中选择的话就没有这个type
         * String type = intent.getType();
         * Log.i("getUri---", "type = " + type);
         * if (uri.getScheme().equals("file") && (type.contains("image/"))) {
         */

        // 先做非空判断
        if (null == uri) {
            return null;
        }

        if (TYPE_FILE_SCHEME.equals(uri.getScheme())) {

            String path = uri.getEncodedPath();

            if (path != null) {

                path = Uri.decode(path);
                ContentResolver cr = this.getContentResolver();
                String buff = "(" + MediaStore.Images.ImageColumns.DATA + "=" +
                        "'" + path + "'" + ")";
                Cursor cur = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        new String[]{MediaStore.Images.ImageColumns._ID},
                        buff, null, null);

                int index = 0;

                // 先对 Cursor 做非空判断
                if (null != cur) {

                    for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
                        index = cur.getColumnIndex(MediaStore.Images.ImageColumns._ID);
                        // set _id value
                        index = cur.getInt(index);
                    }

                    if (index != 0) {

                        Uri uriTemp = Uri.parse("content://media/external/images/media/" + index);

                        if (uriTemp != null) {
                            uri = uriTemp;
                        }

                    }

                    // 关闭 Cursor
                    cur.close();

                }

            }

        }

        return uri;

    }

    // 将文件File转成Uri
    public Uri getImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID}, MediaStore.Images.Media.DATA + "=? ",
                new String[]{filePath}, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }

}
