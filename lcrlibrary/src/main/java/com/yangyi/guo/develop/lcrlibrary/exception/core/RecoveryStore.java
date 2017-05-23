package com.yangyi.guo.develop.lcrlibrary.exception.core;

import android.app.Activity;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by GYH on 2017/5/19.
 */

public class RecoveryStore {

    static final String RECOVERY_INTENT = "recovery_intent";

    static final String RECOVERY_INTENTS = "recovery_intents";

    static final String RECOVERY_STACK = "recovery_stack";

    static final String IS_DEBUG = "recovery_is_debug";

    static final String STACK_TRACE = "recovery_stack_trace";

    static final String EXCEPTION_DATA = "recovery_exception_data";

    static final String EXCEPTION_CAUSE = "recovery_exception_cause";

    private static final byte[] LOCK = new byte[0];

    private List<WeakReference<Activity>> mRunningActivities;

    private Intent mIntent;//跳转

    private volatile static RecoveryStore sInstance;

    private RecoveryStore(){
        mRunningActivities = new CopyOnWriteArrayList<>();
    }

    public static RecoveryStore getInstance(){
        if(sInstance == null){
            synchronized (LOCK){
                if(sInstance == null){
                    sInstance = new RecoveryStore();
                }
            }
        }
        return sInstance;
    }

    public Intent getIntent() {
        return mIntent;
    }

    /**
     * 设置句柄
     * @param Intent
     */
    public synchronized void setIntent(Intent Intent) {
        this.mIntent = Intent;
    }

    /**
     * 添加
     * @param activity
     */
    public void putActivity(Activity activity) {
        WeakReference<Activity> weakReference = new WeakReference<>(activity);
        mRunningActivities.add(weakReference);
    }

    /**
     * 验证
     * @param activity
     * @return
     */
    public boolean verifyActivity(Activity activity) {
        return activity != null
                && !Recovery.getInstance().getSkipActivities().contains(activity.getClass())
                && !RecoveryActivity.class.isInstance(activity);
    }

    /**
     * 获取跳转句柄
     * @return
     */
    ArrayList<Intent> getIntents(){
        ArrayList<Intent> intentList = new ArrayList<>();
        for (WeakReference<Activity> activityWeakReference: mRunningActivities){
            if(activityWeakReference == null){
                continue;
            }
            Activity tmpActivity = activityWeakReference.get();
            if(tmpActivity == null){
                continue;
            }
            intentList.add((Intent) tmpActivity.getIntent().clone());
        }
        return intentList;
    }

    /***
     * 移除栈中的Activity
     * @param activity
     */
    public void removeActivity(Activity activity) {
        for (WeakReference<Activity> activityWeakReference : mRunningActivities) {
            if (activityWeakReference == null)
                continue;
            Activity tmpActivity = activityWeakReference.get();
            if (tmpActivity == null)
                continue;
            if (tmpActivity == activity) {
                mRunningActivities.remove(activityWeakReference);
                break;
            }
        }
    }

    /**
     * 异常数据封装
     */
    static final class ExceptionData implements Parcelable {

        String type;
        String className;
        String methodName;
        int lineNumber;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.type);
            dest.writeString(this.className);
            dest.writeString(this.methodName);
            dest.writeInt(this.lineNumber);
        }
        ExceptionData(){

        }

        public static ExceptionData newInstance() {
            return new ExceptionData();
        }

        public ExceptionData type(String type){
            this.type = type;
            return this;
        }

        public ExceptionData methodName(String methodName){
            this.methodName = methodName;
            return this;
        }

        public ExceptionData lineNumber(int lineNumber) {
            this.lineNumber = lineNumber;
            return this;
        }
        public ExceptionData className(String className) {
            this.className = className;
            return this;
        }

        protected ExceptionData(Parcel in) {
            this.type = in.readString();
            this.className = in.readString();
            this.methodName = in.readString();
            this.lineNumber = in.readInt();
        }

        public static final Creator<ExceptionData> CREATOR = new Creator<ExceptionData>() {
            @Override
            public ExceptionData createFromParcel(Parcel source) {
                return new ExceptionData(source);
            }

            @Override
            public ExceptionData[] newArray(int size) {
                return new ExceptionData[size];
            }
        };

        @Override
        public String toString() {
            return "ExceptionData{" +
                    "className='" + className + '\'' +
                    ", type='" + type + '\'' +
                    ", methodName='" + methodName + '\'' +
                    ", lineNumber=" + lineNumber +
                    '}';
        }
    }
}
