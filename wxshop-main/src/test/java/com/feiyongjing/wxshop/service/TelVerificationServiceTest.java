package com.feiyongjing.wxshop.service;

import com.feiyongjing.wxshop.entity.TelAndCode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class TelVerificationServiceTest {
    public static TelAndCode VALID_PARAMETER = new TelAndCode("13800000000", null);
    public static TelAndCode VALID_PARAMETER_CODE = new TelAndCode("13800000000", "000000");
    public static TelAndCode EMPTY_PARAMETER = new TelAndCode(null, null);

    @Test
    void returnTrueIfValid() {
        Assertions.assertTrue(new TelVerificationService().verifyTelParameter(VALID_PARAMETER));
    }

    @Test
    void returnFalseNoTel() {
        Assertions.assertFalse(new TelVerificationService().verifyTelParameter(null));
        Assertions.assertFalse(new TelVerificationService().verifyTelParameter(EMPTY_PARAMETER));
    }
}
