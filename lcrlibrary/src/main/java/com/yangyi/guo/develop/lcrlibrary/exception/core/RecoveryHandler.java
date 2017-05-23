package com.yangyi.guo.develop.lcrlibrary.exception.core;

import android.content.Intent;
import android.text.TextUtils;

import com.yangyi.guo.develop.lcrlibrary.exception.callback.RecoveryCallback;
import com.yangyi.guo.develop.lcrlibrary.exception.tools.DefaultHandlerUtil;
import com.yangyi.guo.develop.lcrlibrary.exception.tools.RecoverySharedPrefsUtil;
import com.yangyi.guo.develop.lcrlibrary.exception.tools.RecoverySilentSharedPrefsUtil;
import com.yangyi.guo.develop.lcrlibrary.exception.tools.RecoveryUtil;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by GYH on 2017/5/19.
 */

public class RecoveryHandler implements Thread.UncaughtExceptionHandler{

    /**
     * ?? 这里为啥要定义一个新的
     */
    private Thread.UncaughtExceptionHandler mDefaultUncaughtExceptionHandler;

    private RecoveryCallback mCallback;

    private RecoveryStore.ExceptionData mExceptionData;

    private String mStackTrace;

    private String mCause;

    public RecoveryHandler(Thread.UncaughtExceptionHandler handler) {
        this.mDefaultUncaughtExceptionHandler = handler;
    }

    static RecoveryHandler newInstance(Thread.UncaughtExceptionHandler handler){
        return new RecoveryHandler(handler);
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {

        if(Recovery.getInstance().isSilentEnbled()){
            RecoverySilentSharedPrefsUtil.recordCrashData();
        }else {
            RecoverySharedPrefsUtil.recordCrashData();
        }

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        pw.flush();

        String stackTrace = sw.toString();
        String cause = e.getMessage();
        Throwable rootTr = e;
        while (e.getCause() != null){
            e = e.getCause();
            if(e.getStackTrace() != null && e.getStackTrace().length>0){
                rootTr = e;
            }
            String msg = e.getMessage();
            if(!TextUtils.isEmpty(msg)){
                cause = msg;
            }
        }

        String exceptionType = rootTr.getClass().getName();


        String throwClassName;
        String throwMethodName;
        int throwLineNumber;

        if (rootTr.getStackTrace().length > 0) {
            StackTraceElement trace = rootTr.getStackTrace()[0];
            throwClassName = trace.getClassName();
            throwMethodName = trace.getMethodName();
            throwLineNumber = trace.getLineNumber();
        } else {
            throwClassName = "unknown";
            throwMethodName = "unknown";
            throwLineNumber = 0;
        }

        mExceptionData = RecoveryStore.ExceptionData.newInstance()
                .type(exceptionType)
                .className(throwClassName)
                .methodName(throwMethodName)
                .lineNumber(throwLineNumber);

        mStackTrace = stackTrace;
        mCause = cause;

        if (mCallback != null) {
            mCallback.stackTrace(stackTrace);
            mCallback.cause(cause);
            mCallback.exception(exceptionType, throwClassName, throwMethodName, throwLineNumber);
            mCallback.throwable(e);
        }

        if (!DefaultHandlerUtil.isSystemDefaultUncaughtExceptionHandler(mDefaultUncaughtExceptionHandler)) {
            if (mDefaultUncaughtExceptionHandler == null){
                recover();
            }
            mDefaultUncaughtExceptionHandler.uncaughtException(t, e);
        } else {
            recover();
        }
    }

    /**
     * 设置回调
     * @param callback
     * @return
     */
    RecoveryHandler setCallback(RecoveryCallback callback){
        mCallback = callback;
        return this;
    }

    /**
     * 判断回复策略
     */
    private void recover(){

        if(RecoveryUtil.isAppInBackground(Recovery.getInstance().getContext())
                && !Recovery.getInstance().isRecoverInBackground()){
            killProcess();
            return;
        }
        if(Recovery.getInstance().isSilentEnbled()){
            startRecoverService();
        }else {
            startRecoverActivity();
        }

    }

    /**
     * 开启恢复界面
     */
    private void startRecoverActivity(){
        Intent intent = new Intent();
        intent.setClass(Recovery.getInstance().getContext(),RecoveryActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK
                | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        if(RecoveryStore.getInstance().getIntent() != null){
            intent.putExtra(RecoveryStore.RECOVERY_INTENT,RecoveryStore.getInstance().getIntent());
        }
        if(!RecoveryStore.getInstance().getIntents().isEmpty()){
            intent.putParcelableArrayListExtra(RecoveryStore.RECOVERY_INTENTS,RecoveryStore.getInstance().getIntents());
        }
        intent.putExtra(RecoveryStore.RECOVERY_STACK, Recovery.getInstance().isRecoverStack());
        intent.putExtra(RecoveryStore.IS_DEBUG, Recovery.getInstance().isDebug());
        if (mExceptionData != null)
            intent.putExtra(RecoveryStore.EXCEPTION_DATA, mExceptionData);
        if (mStackTrace != null)
            intent.putExtra(RecoveryStore.STACK_TRACE, mStackTrace);
        if (mCause != null)
            intent.putExtra(RecoveryStore.EXCEPTION_CAUSE, mCause);
        Recovery.getInstance().getContext().startActivity(intent);
        killProcess();
    }

    /**
     * 启动服务区恢复应用
     */
    private void startRecoverService() {
        Intent intent = new Intent();
        intent.setClass(Recovery.getInstance().getContext(), RecoveryService.class);
        if (RecoveryStore.getInstance().getIntent() != null)
            intent.putExtra(RecoveryStore.RECOVERY_INTENT, RecoveryStore.getInstance().getIntent());
        if (!RecoveryStore.getInstance().getIntents().isEmpty())
            intent.putParcelableArrayListExtra(RecoveryStore.RECOVERY_INTENTS, RecoveryStore.getInstance().getIntents());
        intent.putExtra(RecoveryService.RECOVERY_SILENT_MODE_VALUE, Recovery.getInstance().getSilentMode().getValue());
        RecoveryService.start(Recovery.getInstance().getContext(), intent);
        killProcess();
    }


    /**
     * 杀死进程
     */
    private void killProcess(){
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(10);
    }


    void register() {
        Thread.setDefaultUncaughtExceptionHandler(this);
    }
}
