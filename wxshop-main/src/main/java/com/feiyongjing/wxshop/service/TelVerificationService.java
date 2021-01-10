package com.feiyongjing.wxshop.service;

import com.feiyongjing.wxshop.entity.TelAndCode;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class TelVerificationService {
    private static Pattern TEL_PATTERN = Pattern.compile("1\\d{10}");

    /**
     * 验证输入的参数是否合法:
     * tel必须存在且为合法的中国大陆手机号
     *
     * @param telAndCode 输入的参数
     * @return true 合法，否则返回 false
     */
    public boolean verifyTelParameter(TelAndCode telAndCode) {
        if (telAndCode != null && telAndCode.getTel() != null) {
            return TEL_PATTERN.matcher(telAndCode.getTel()).find();
        } else {
            return false;
        }
    }

}
