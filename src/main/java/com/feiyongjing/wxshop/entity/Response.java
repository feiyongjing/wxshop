package com.feiyongjing.wxshop.entity;

public class Response<T> {
    private T data;
    private String massage;

    public static <T> Response<T> of(T data){
        return new Response<>(null, data);
    }
    public static <T> Response<T> of(String massage, T data){
        return new Response<>(massage, null);
    }
    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    private Response(String massage, T data) {
        this.data = data;
        this.massage = massage;
    }

    public Response() {
    }

    public String getMassage() {
        return massage;
    }

    public void setMassage(String massage) {
        this.massage = massage;
    }
}
