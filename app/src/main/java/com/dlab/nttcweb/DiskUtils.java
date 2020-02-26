package com.dlab.nttcweb;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author corey
 * @date 2016/8/20
 */
public class DiskUtils {

    private static final String CROPIMAGEPATH = "cropImage";


    public static String generatePhotoPath(Context context){
        File file = getDiskCacheDir(context);

        String cameraImgSavePath = file.getPath() + File.separator
                + getPhotoFileName();
        return cameraImgSavePath;
    }

    public static File generatePhotoFile(Context context){
        File file = getDiskCacheDir(context);

        String cameraImgSavePath = file.getPath() + File.separator
                + getPhotoFileName();
        return new File(cameraImgSavePath);
    }


    public static File getDiskCacheDir(Context context) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
            Log.i("DiskUtils---1---", "cachePath = " + cachePath);
        } else {
            cachePath = context.getCacheDir().getPath();
            Log.i("DiskUtils---2---", "cachePath = " + cachePath);
        }
        File file = new File(cachePath + File.separator + CROPIMAGEPATH);
        if (!file.exists()) {
            file.mkdirs();
        }

        return file;
    }

    private static String getPhotoFileName() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "'IMG'_yyyyMMdd_HHmmss");
        return dateFormat.format(date) + ".jpg";
    }

}
