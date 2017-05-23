package com.yangyi.guo.develop.lcrlibrary.exception.rexce;

/**
 * Created by GYH on 2017/5/19.
 */

public class RecoveryException extends RuntimeException{

    public RecoveryException(String message) {
        super(message);
    }

    public RecoveryException(String message, Throwable cause) {
        super(message, cause);
    }
}
