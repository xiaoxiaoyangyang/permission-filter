package com.permission.pojo;

import lombok.Data;

import java.util.List;

/**
 * @Author: guozhiyang_vendor
 * @Date: 2019/5/23 11:56
 * @Version 1.0
 */
@Data
public class User {
    private Integer userId;
    private String name;
    private String password;
    private List<PermissionUrl> permissionList;
}
