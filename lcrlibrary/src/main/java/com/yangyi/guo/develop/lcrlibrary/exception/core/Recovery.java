package com.yangyi.guo.develop.lcrlibrary.exception.core;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.yangyi.guo.develop.lcrlibrary.exception.callback.RecoveryActivityLifecycleCallback;
import com.yangyi.guo.develop.lcrlibrary.exception.callback.RecoveryCallback;
import com.yangyi.guo.develop.lcrlibrary.exception.rexce.RecoveryException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by GYH on 2017/5/19.
 *
 * (1)零长度的byte数组对象创建起来将比任何对象都经济――查看编译后的字节码：生成零长度的byte[]对象只需3条操作码，而Object lock = new Object()则需要7行操作码。
 */

public class Recovery {

    private volatile static Recovery sInstance;
//    private static final Object LOCK = new Object();
    private static final byte[] LOCK = new byte[0];

    private Context mContext;

    private boolean isDebug = false;

    private boolean isRecoverInBackground = false;//是否在后台恢复

    private boolean isSilentEnbled = false;//是否是默认模式

    private boolean isRecoverStack = true;//是否在默认栈中恢复

    private SilentMode mSilentMode = SilentMode.RECOVER_ACTIVITY_STACK;

    private RecoveryCallback mCallback;//恢复回调

    private Class<? extends Activity> mMainPageClass;

    //记录跳过的activity
    private List<Class<? extends Activity>> mSkipActivities = new ArrayList<>();

    public static Recovery getInstance(){
        if(sInstance == null){
            synchronized (LOCK){
                if(sInstance == null){
                    sInstance = new Recovery();
                }
            }
        }
        return sInstance;
    }

    /**
     * init recovery
     * @param context
     */
    public void init(Context context){
        if(context == null){
            throw new RecoveryException("Context can not be null");
        }
        if(!(context instanceof Application)){
            context = context.getApplicationContext();
        }
        mContext = context;
        registerRecoveryHandler();
        registerRecoveryLifecycleCallback();
    }

    public Recovery debug(boolean isDebug){
        this.isDebug =isDebug;
        return this;
    }

    public boolean isDebug() {
        return isDebug;
    }

    /**
     * reagister 恢复 handle
     */
    private void registerRecoveryHandler(){
        RecoveryHandler.newInstance(Thread.getDefaultUncaughtExceptionHandler()).setCallback(mCallback).register();
    }

    private void registerRecoveryLifecycleCallback() {
        ((Application) mContext).registerActivityLifecycleCallbacks(new RecoveryActivityLifecycleCallback());
    }


    /**
     * 获取跳过的activity
     * @return
     */
    public List<Class<? extends Activity>> getSkipActivities() {
        return mSkipActivities;
    }

    public Context getContext() {
        return mContext;
    }

    /**
     * 设置是否在后台恢复
     * @param isRecoverInBackground
     * @return
     */
    public Recovery recoverInBackground(boolean isRecoverInBackground){
        this.isRecoverInBackground = isRecoverInBackground;
        return this;
    }

    /**
     * 返回是否在后台恢复
     * @return
     */
    public boolean isRecoverInBackground() {
        return isRecoverInBackground;
    }

    /**
     * 设置默认模式
     * @param enabled
     * @param mode
     * @return
     */
    public Recovery silent(boolean enabled, SilentMode mode){
        this.isSilentEnbled = enabled;
        this.mSilentMode = mode == null ? SilentMode.RECOVER_ACTIVITY_STACK:mode;
        return this;
    }

    /**
     * 恢复回调
     * @param callback
     * @return
     */
    public Recovery callback(RecoveryCallback callback) {
        this.mCallback = callback;
        return this;
    }

    /**
     * 设置恢复界面
     * @param clazz
     * @return
     */
    public Recovery mainPage(Class<? extends Activity> clazz) {
        this.mMainPageClass = clazz;
        return this;
    }

    /**
     * 是否启动模式
     * @return
     */
    public boolean isSilentEnbled(){
        return isSilentEnbled;
    }


    public Recovery recoverStack(boolean isRecoverStack){
        this.isRecoverStack = isRecoverStack;
        return this;
    }

    public boolean isRecoverStack() {
        return isRecoverStack;
    }

    /**
     * 获取当前模式
     * @return
     */
    public SilentMode getSilentMode() {
        return mSilentMode;
    }

    /**
     * 模式
     */
    public  enum SilentMode{
        RESTART(1),
        RECOVER_ACTIVITY_STACK(2),
        RECOVER_TOP_ACTVITY(3),
        RESTART_AND_CLEAR(4),;

        int value;

        SilentMode(int value){
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static SilentMode getMode(int value){
            switch (value){
                case 1:
                    return RESTART;
                case 2:
                    return RECOVER_ACTIVITY_STACK;
                case 3:
                    return RECOVER_TOP_ACTVITY;
                case 4:
                    return RESTART_AND_CLEAR;
                default:
                    return RECOVER_ACTIVITY_STACK;
            }
        }
    }
}
