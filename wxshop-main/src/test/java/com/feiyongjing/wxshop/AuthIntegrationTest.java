package com.feiyongjing.wxshop;

import com.feiyongjing.wxshop.entity.LoginResponse;
import com.feiyongjing.wxshop.mock.MockOrderRpcService;
import com.feiyongjing.wxshop.service.TelVerificationServiceTest;
import com.github.kevinsawicki.http.HttpRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;

import static com.feiyongjing.wxshop.service.TelVerificationServiceTest.VALID_PARAMETER;
import static com.feiyongjing.wxshop.service.TelVerificationServiceTest.VALID_PARAMETER_CODE;
import static java.net.HttpURLConnection.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = WxshopApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"spring.config.location=classpath:test-application.yml"})
public class AuthIntegrationTest extends AbstractIntegrationTest{
    @Autowired
    MockOrderRpcService mockOrderService;
    @Test
    public void loginLogoutTest() throws IOException {
        // 最开始默认情况下，访问/api/status 处于未登录状态
        // 发送验证码
        // 带着验证码登录, 得到Cookie
        String sessionId=loginAndGetCookie().cookie;
        // 带着Cookie访问 /api/status 应该处于登录状态
        String statusResponse = doHttpResponse("/api/v1/status", "GET", null, sessionId).body;
        LoginResponse response = objectMapper.readValue(statusResponse, LoginResponse.class);
        Assertions.assertTrue(response.isLogin());
        Assertions.assertEquals(VALID_PARAMETER_CODE.getTel(), response.getUser().getTel());

        // 调用/api/logout
        //注销登录使Cookie失效, 注意注意登录也需要Cookie
        HttpResponse httpResponse = doHttpResponse("/api/v1/logout", "POST", null, sessionId);

        // 再次带着Cookie访问/api/status 恢复成为未登录状态
        statusResponse = doHttpResponse("/api/v1/status", "GET", null, sessionId).body;
        response = objectMapper.readValue(statusResponse, LoginResponse.class);
        Assertions.assertFalse(response.isLogin());

    }

    @Test
    public void returnHttpOkWhenParameterIsCorrect() throws IOException {
        int requestCode = HttpRequest.post(getUrl("/api/v1/code"))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .send(objectMapper.writeValueAsString(VALID_PARAMETER))
                .code();
        Assertions.assertEquals(HTTP_OK, requestCode);
    }
    @Test
    public void returnUnauthorizedIfNotlogin() throws IOException {
        int requestCode = HttpRequest.post(getUrl("/api/v1/rests"))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .code();
        Assertions.assertEquals(HTTP_UNAUTHORIZED, requestCode);
    }

    @Test
    public void returnHttpBadRequestWhenParameterIsCorrect() throws IOException {
        int requestCode = HttpRequest.post(getUrl("/api/v1/code"))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .send(objectMapper.writeValueAsString(TelVerificationServiceTest.EMPTY_PARAMETER))
                .code();
        Assertions.assertEquals(HTTP_BAD_REQUEST, requestCode);
    }
}
