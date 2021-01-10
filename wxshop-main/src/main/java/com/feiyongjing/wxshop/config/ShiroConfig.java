package com.feiyongjing.wxshop.config;

import com.feiyongjing.wxshop.service.ShiroRealm;
import com.feiyongjing.wxshop.service.UserLoginInterceptor;
import com.feiyongjing.wxshop.service.VerificationCodeCheckService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.crazycake.shiro.RedisCacheManager;
import org.crazycake.shiro.RedisManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.sql.DataSource;


@Configuration
@EnableTransactionManagement
public class ShiroConfig implements WebMvcConfigurer {
    private static final String COOKIE_NAME = "rememberMe"; //  cookie name

    private static final int EXPIRY_TIME = 86400;
    @Value("${wxshop.redis.host}")
    String redisHost;
    @Value("${wxshop.redis.port}")
    int redisPort;

    private UserLoginInterceptor userLoginInterceptor;

    @Autowired
    public ShiroConfig(UserLoginInterceptor userLoginInterceptor, ShiroLoginFilter shiroLoginFilter) {
        this.userLoginInterceptor = userLoginInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userLoginInterceptor);
    }

    @Bean
    public ShiroFilterFactoryBean shiroFilter(SecurityManager securityManager) {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        shiroFilterFactoryBean.setSecurityManager(securityManager);
//        Map<String, String> pattern = new HashMap<>();
//
//        pattern.put("/api/code", "anon");
//        pattern.put("/api/login", "anon");
//        pattern.put("/api/status", "anon");
//        pattern.put("/api/logout", "anon");
//        pattern.put("/**", "authc");
//        Map<String, Filter> filterMap = new LinkedHashMap<>();
//        filterMap.put("shiroLoginFilter", shiroLoginFilter);
//        shiroFilterFactoryBean.setFilters(filterMap);
//        shiroFilterFactoryBean.setFilterChainDefinitionMap(pattern);
        return shiroFilterFactoryBean;
    }

    @Bean
    public SecurityManager securityManager(ShiroRealm shiroRealm, RedisCacheManager redisCacheManager) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();

        securityManager.setRealm(shiroRealm);
        securityManager.setCacheManager(redisCacheManager);
        securityManager.setSessionManager(new DefaultWebSessionManager());
        securityManager.setRememberMeManager(rememberMeManager());
        SecurityUtils.setSecurityManager(securityManager);
        return securityManager;
    }
    public CookieRememberMeManager rememberMeManager() {
        SimpleCookie cookie = new SimpleCookie(COOKIE_NAME);
        cookie.setMaxAge(EXPIRY_TIME);
        CookieRememberMeManager cookieRememberMeManager = new CookieRememberMeManager();
        cookieRememberMeManager.setCookie(cookie);
        cookieRememberMeManager.setCipherKey(Base64.decode("3AvVhmFLUs0KTA3KaTHGFg=="));  // RememberMe cookie encryption key default AES algorithm of key length (128, 256, 512)
        return cookieRememberMeManager;
    }
    @Bean
    public ShiroRealm myShiroRealm(VerificationCodeCheckService verificationCodeCheckService) {
        return new ShiroRealm(verificationCodeCheckService);
    }
    @Bean
    public DataSourceTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
    @Bean
    public RedisCacheManager redisCacheManager(){
        RedisCacheManager redisCacheManager=new RedisCacheManager();
        RedisManager redisManager=new RedisManager();
        redisManager.setHost(redisHost+":"+redisPort);
        redisCacheManager.setRedisManager(redisManager);
        return redisCacheManager;
    }
}
