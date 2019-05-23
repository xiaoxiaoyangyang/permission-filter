package com.permission.pojo;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

/**
 * @Author: guozhiyang_vendor
 * @Date: 2019/5/23 11:28
 * @Version 1.0
 */
@Data
@Builder
public class ExceptionBody {
    private String code;
    private String path;
    private String methodType;
    private String message;
}
