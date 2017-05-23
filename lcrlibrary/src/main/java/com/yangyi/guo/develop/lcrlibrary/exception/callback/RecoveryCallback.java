package com.yangyi.guo.develop.lcrlibrary.exception.callback;

/**
 * Created by GYH on 2017/5/19.
 *
 * 异常回复回调
 */

public interface RecoveryCallback {

    void stackTrace(String stackTrace);

    void cause(String cause);

    void exception(String throwExceptionType,String throwClassName,String throwMethodName, int throwLineNumber);

    void throwable(Throwable throwable);
}
