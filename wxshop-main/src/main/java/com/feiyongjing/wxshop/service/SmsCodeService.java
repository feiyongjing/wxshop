package com.feiyongjing.wxshop.service;

public interface SmsCodeService {
    /**
     * 向一个指定的手机号发送，返回正确答案
     * @param tel 目标手机号
     * @return 正确答案
     */
    String sendSmsCode(String tel);
}
