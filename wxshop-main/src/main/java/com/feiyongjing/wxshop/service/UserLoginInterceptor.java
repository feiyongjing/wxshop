package com.feiyongjing.wxshop.service;

import com.feiyongjing.wxshop.generate.User;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

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
                "/api/code",
                "/api/login",
                "/api/status",
                "/api/logout",
                "/error",
                "/",
                "/index.html",
                "/manifest.json"
        ).contains(uri) || uri.startsWith("/static/");
    }
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Object tel = SecurityUtils.getSubject().getPrincipal();
        if (tel != null) {
            User user = userService.getUserByTel(tel.toString());
            UserContext.setCurrentUser(user);
        }
        if (isWhitelist(request)) {
            return true;
        } else if (UserContext.getCurrentUser() == null) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        UserContext.setCurrentUser(null);
    }
}

