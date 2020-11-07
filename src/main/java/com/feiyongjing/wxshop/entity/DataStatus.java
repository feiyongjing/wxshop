package com.feiyongjing.wxshop.entity;

public enum DataStatus {
    OK(),
    DELETE();

    public String getName(){
        return name().toLowerCase();
    }

}
