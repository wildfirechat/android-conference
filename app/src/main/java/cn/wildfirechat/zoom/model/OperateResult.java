/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfirechat.zoom.model;

public class OperateResult<T> {
    T result;
    int errorCode;

    public OperateResult(T result, int errorCode) {
        this.result = result;
        this.errorCode = errorCode;
    }

    public OperateResult(int errorCode) {
        this.errorCode = errorCode;
    }

    public boolean isSuccess() {
        return errorCode == 0;
    }

    public T getResult() {
        return result;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
