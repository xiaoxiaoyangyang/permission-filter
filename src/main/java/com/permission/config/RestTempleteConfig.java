package com.permission.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * @Author: guozhiyang_vendor
 * @Date: 2019/5/23 13:22
 * @Version 1.0
 */

public class RestTempleteConfig {
    @Autowired
    private HttpMessageConverters fastJsonHttpMessageConverters;
    @Bean
    public RestTemplate restTemplate(){
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setMessageConverters(fastJsonHttpMessageConverters.getConverters());
        return restTemplate;
    }
}
