package com.trophate.redisinaction.dto;

public class Page {

    /**
     * 当前页
     */
    private Integer current;
    /**
     * 每页条数
     */
    private Integer size;

    public Integer getCurrent() {
        return current;
    }

    public void setCurrent(Integer current) {
        this.current = current;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Integer getStartIndex() {
        if (current == null || size == null) {
            return null;
        }
        return (current - 1) * size;
    }

    public Integer getEndIndex() {
        if (current == null || size == null) {
            return null;
        }
        return getStartIndex() + size - 1;
    }
}
