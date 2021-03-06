package com.feiyongjing.wxshop.service;

import com.feiyongjing.wxshop.dao.UserDao;
import com.feiyongjing.wxshop.generate.User;
import org.apache.ibatis.exceptions.PersistenceException;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class UserService {

    private final UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public User createUserIfNotExist(String tel) {
        User user=new User();
        user.setTel(tel);
        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());
        try {
            userDao.insertUser(user);
        }catch (PersistenceException e){
            return userDao.getUserByBel(tel);
        }
        return user;
    }

    public User getUserByTel(String tel) {
        return userDao.getUserByBel(tel);
    }
}
