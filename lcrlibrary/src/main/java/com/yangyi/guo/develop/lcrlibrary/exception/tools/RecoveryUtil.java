package com.yangyi.guo.develop.lcrlibrary.exception.tools;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Environment;

import com.yangyi.guo.develop.lcrlibrary.exception.core.Recovery;
import com.yangyi.guo.develop.lcrlibrary.exception.rexce.RecoveryException;

import java.io.File;
import java.util.List;

/**
 * Created by GYH on 2017/5/19.
 */

public class RecoveryUtil {

    private RecoveryUtil(){
        throw  new RecoveryException("Stub!");
    }

    /**
     * 判断 app是否在后台运行
     * @param context
     * @return
     */
    public static boolean isAppInBackground(Context context){
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appPrecesses = activityManager.getRunningAppProcesses();
        if(appPrecesses == null){
            return true;
        }
        for(ActivityManager.RunningAppProcessInfo appProcessInfo: appPrecesses){
            if(appProcessInfo.processName.equals(context.getPackageName())){
                return appProcessInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND;
            }
        }
        return false;
    }


    /**
     * 检查intent是否可用
     * @param context
     * @param intent
     * @return
     */
    public static boolean isIntentAvailable(Context context, Intent intent){
        if(context == null || intent == null){
            return false;
        }
        PackageManager packageManager = context.getApplicationContext().getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent,PackageManager.MATCH_DEFAULT_ONLY);
        return list.size()>0;
    }

    /**
     * 获取app名称
     * @param context
     * @return
     */
    public static String getAppName(Context context){
        PackageManager packageManager = null;
        ApplicationInfo applicationInfo = null;
        if (!(context instanceof Application))
            context = context.getApplicationContext();
        try {
            packageManager = context.getPackageManager();
            applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            applicationInfo = null;
        }
        CharSequence charSequence = packageManager.getApplicationLabel(applicationInfo);
        return charSequence == null ? "" : (String) charSequence;
    }




    /**
     * 清除appdata
     */
    public static void clearApplicationData() {
        clearAppData(getDataDir());
        clearAppData(getExternalDataDir());
    }

    /**
     * 获取内存中目录
     * @return
     */
    private static File getDataDir() {
        return new File(File.separator + "data" + File.separator + "data" + File.separator + Recovery.getInstance().getContext().getPackageName());
    }

    /**
     * 获取sd内缓存目录（一般存放临时缓存数据）
     * @return
     */
    private static File getExternalDataDir() {
        File file = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            file = Recovery.getInstance().getContext().getExternalCacheDir();
        }
        return file == null ? null : file.getParentFile();
    }

    /**
     * 清除目录下的数据
     * @param dir
     * @return
     */
    private static boolean clearAppData(File dir) {
        if (dir == null || !dir.isDirectory() || !dir.exists())
            return false;
        File[] files = dir.listFiles();
        int length = files.length;
        for (int i = 0; i < length; i++) {
            File file = files[i];
            if (file == null)
                continue;
            if (file.isFile() && file.exists()) {
                boolean result = file.delete();
                RecoveryLog.e(file.getName() + (result ? " delete success!" : " delete failed!"));
                continue;
            }
            if (file.isDirectory() && file.exists()) {
                clearAppData(file);
            }
        }
        return true;
    }
}
