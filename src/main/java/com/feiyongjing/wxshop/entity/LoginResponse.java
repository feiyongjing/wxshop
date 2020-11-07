package com.feiyongjing.wxshop.entity;

import com.feiyongjing.wxshop.generate.User;

public class LoginResponse {
    boolean login;
    User user;

    public LoginResponse() {
    }

    public static LoginResponse notLogin(){
        return new LoginResponse(false, null);
    }

    public static LoginResponse login(User user){
        return new LoginResponse(true, user);
    }

    private LoginResponse(boolean login, User user) {
        this.login = login;
        this.user = user;
    }

    public boolean isLogin() {
        return login;
    }

    public void setLogin(boolean login) {
        this.login = login;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
