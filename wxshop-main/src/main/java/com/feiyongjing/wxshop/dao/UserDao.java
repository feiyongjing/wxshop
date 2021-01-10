package com.feiyongjing.wxshop.dao;

import com.feiyongjing.wxshop.generate.User;
import com.feiyongjing.wxshop.generate.UserExample;
import com.feiyongjing.wxshop.generate.UserMapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserDao {
    private final SqlSessionFactory sqlSessionFactory;

    @Autowired
    public UserDao(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    public void insertUser(User user) {
        try(SqlSession sqlSession=sqlSessionFactory.openSession()){
            UserMapper mapper = sqlSession.getMapper(UserMapper.class);
            mapper.insert(user);
        }
    }

    public User getUserByBel(String tel) {
        try(SqlSession sqlSession=sqlSessionFactory.openSession()){
            UserMapper mapper = sqlSession.getMapper(UserMapper.class);
            UserExample example=new UserExample();
            example.createCriteria().andTelEqualTo(tel);
            return mapper.selectByExample(example).get(0);
        }
    }
}
