package com.feiyongjing.wxshop.service;

import com.feiyongjing.wxshop.generate.User;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserService userService;
    private final VerificationCodeCheckService verificationCodeCheckService;
    private final SmsCodeService smsCodeService;

    @Autowired
    public AuthService(UserService userService,
                       VerificationCodeCheckService verificationCodeCheckService,
                       SmsCodeService smsCodeService) {
        this.userService = userService;
        this.verificationCodeCheckService = verificationCodeCheckService;
        this.smsCodeService = smsCodeService;
    }

    @SuppressFBWarnings("DLS_DEAD_LOCAL_STORE")
    public void sendVerificationCode(String tel) {
        User user = userService.createUserIfNotExist(tel);
        String correctCode = smsCodeService.sendSmsCode(tel);
        verificationCodeCheckService.addCode(tel, correctCode);
    }

}
