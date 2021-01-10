package com.feiyongjing.wxshop.api.data;

import java.io.Serializable;
import java.util.List;

public class PageResponse<T> implements Serializable {
    Integer pageNum;
    Integer pageSize;
    Integer totalPage;
    List<T> data;

    public PageResponse() {
    }

    public static <T> PageResponse<T> of(Integer pageNum, Integer pageSize, Integer totalPage, List<T> data){
        return new PageResponse<>(pageNum, pageSize, totalPage, data);
    }

    private PageResponse(Integer pageNum, Integer pageSize, Integer totalPage, List<T> data) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.totalPage = totalPage;
        this.data = data;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(Integer totalPage) {
        this.totalPage = totalPage;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }
}
