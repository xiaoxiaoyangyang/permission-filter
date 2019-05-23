package com.permission.constant;

/**
 * @Author: guozhiyang_vendor
 * @Date: 2019/5/23 11:38
 * @Version 1.0
 */
public enum ErrorMessageEnum {
    UNAUTHENCATION("4444","token invalid");
    private String code;
    private String errorMessage;

    ErrorMessageEnum(String code, String errorMessage) {
        this.code = code;
        this.errorMessage = errorMessage;
    }

    public String getCode() {
        return code;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
