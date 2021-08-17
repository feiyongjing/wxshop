package com.feiyongjing.wxshop.service;

import com.feiyongjing.wxshop.generate.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

@Slf4j
@Service
public class UserLoginInterceptor implements HandlerInterceptor {
    private UserService userService;

    @Autowired
    public UserLoginInterceptor(UserService userService) {
        this.userService = userService;
    }

    private boolean isWhitelist(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return Arrays.asList(
                "/api/v1/code",
                "/api/v1/login",
                "/api/v1/status",
                "/api/v1/logout",
                "/error",
                "/",
                "/index.html",
                "/manifest.json"
        ).contains(uri) || uri.startsWith("/static/");
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("当前参数"+request.getQueryString());
        if (SecurityUtils.getSubject().isAuthenticated()) {
            Object tel = SecurityUtils.getSubject().getPrincipal();
            User user = userService.getUserByTel(tel.toString());
            UserContext.setCurrentUser(user);
            return true;
        }
        if (isWhitelist(request)) {
            return true;
        }

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        return false;
    }


    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserContext.setCurrentUser(null);
    }
}

