package com.permission.filter;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.permission.filterealm.AuthenAuthorRealm;
import com.permission.filterealm.Mytest;
import com.permission.pojo.ExceptionBody;
import com.permission.pojo.PermissionUrl;
import com.permission.pojo.User;
import com.permission.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.asm.Advice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.permission.constant.ErrorMessageEnum.UNAUTHENCATION;

/**
 * @Author: guozhiyang_vendor
 * @Date: 2019/5/23 10:34
 * @Version 1.0
 */
@Slf4j
@WebFilter(urlPatterns = "/*",filterName = "permissionFilter")
public class PermissionFilter implements Filter {

    private static final String OPTION="option";
    @Value("${oauth2.whitelist}")
    private String whiteList;
    @Value("{oauth2.permission.url}")
    private String permissionUrlList;
    @Autowired
    private RedisTemplate<String,String> redisTemplate;
    @Autowired
    private RestTemplate restTemplate;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("权限过滤器，开始进行初始化");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        //直接判断是否是option的请求，如果是，则直接放行
        String methodType = request.getMethod();
        if(methodType.equalsIgnoreCase(OPTION)){
            filterChain.doFilter(servletRequest,servletResponse);
        }
        //获取请求路径
        String uri = request.getRequestURI();
        String ur = uri.replaceAll(request.getContextPath(), "");
        log.info("请求的路径------------->{}",ur);
        //判断请求路径是否在白名单里,如果在白名单中直接进行放行
        List<String> list = Arrays.asList(whiteList.split(","));
        boolean flag = list.stream().anyMatch(s -> ur.equals(s));
        if(flag){
            log.info("白名单进行了比中,过滤器进行放行");
            filterChain.doFilter(servletRequest,servletResponse);
        }else{
            //先进行认证
            String accessToken = request.getHeader("accessToken");
            String username = JwtUtils.getUsername(accessToken);
            String s = redisTemplate.opsForValue().get(username);
            if(username==null && !s.equals(accessToken)){
                ExceptionBody exceptionBody = errorResult(UNAUTHENCATION.getCode(), uri, UNAUTHENCATION.getErrorMessage(), methodType);
                printErrorResult(servletResponse,exceptionBody);
            }
            //通过名字获取相对应的对象的信息
            ResponseEntity<User> forEntity = restTemplate.getForEntity(permissionUrlList, User.class);
            User body = forEntity.getBody();
            if(body==null){
                ExceptionBody exceptionBody = errorResult(UNAUTHENCATION.getCode(), uri, UNAUTHENCATION.getErrorMessage(), methodType);
                printErrorResult(servletResponse,exceptionBody);
            }
            if (!JwtUtils.verify(accessToken, username, body.getPassword())) {
                ExceptionBody exceptionBody = errorResult(UNAUTHENCATION.getCode(), uri, UNAUTHENCATION.getErrorMessage(), methodType);
                printErrorResult(servletResponse,exceptionBody);
            }
            log.info("认证通过-------------------->");
            //进行鉴权
            List<PermissionUrl> permissionList = body.getPermissionList();
            boolean permissionFlag = permissionList.stream().anyMatch(permissionUrl -> (permissionUrl.equals(uri) && permissionUrl.equals(methodType)));
            if(permissionFlag){
                String sign = JwtUtils.sign(username, body.getPassword());
                redisTemplate.opsForValue().set(username,sign,30,TimeUnit.MINUTES);
                request.setAttribute("accessToken",sign);
                filterChain.doFilter(request,servletResponse);
            }else {
                ExceptionBody exceptionBody = errorResult(UNAUTHENCATION.getCode(), uri, UNAUTHENCATION.getErrorMessage(), methodType);
                printErrorResult(servletResponse,exceptionBody);
            }
        }
    }

    @Override
    public void destroy() {
        log.info("权限过滤器，已经销毁");
    }


    /**
     * 打印错误结果
     * @param response
     * @param exceptionBody
     * @throws IOException
     * @throws JsonProcessingException
     */
    private void printErrorResult(ServletResponse response, ExceptionBody exceptionBody)
            throws IOException, JsonProcessingException {
        HttpServletResponse res = (HttpServletResponse) response;
        res.setCharacterEncoding("UTF-8");
        res.setContentType("application/json;charset=UTF-8");
        res.setStatus(HttpStatus.UNAUTHORIZED.value());
        String s = JSON.toJSONString(exceptionBody);
        OutputStream writer = response.getOutputStream();
        writer.write(s.getBytes("UTF-8"));
        writer.flush();
        writer.close();
    }

    /**
     * 封装错误的信息
     * @param code
     * @param uri
     * @param message
     * @param mothedType
     * @return
     */
    private ExceptionBody errorResult(String code,String uri,String message,String mothedType){
        ExceptionBody build = ExceptionBody.builder().code(code)
                .path(uri)
                .methodType(mothedType)
                .message(message).build();
        return build;
    }
}
