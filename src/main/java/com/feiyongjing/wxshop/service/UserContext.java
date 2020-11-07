package com.feiyongjing.wxshop.service;

import com.feiyongjing.wxshop.generate.User;

public class UserContext {
   public static ThreadLocal<User> currentUser= new ThreadLocal<>();
   public static User getCurrentUser(){
       return currentUser.get();
   }
   public static void setCurrentUser(User user){
       currentUser.set(user);
   }
}
