package com.example.firebaseapplication;

import java.io.Serializable;

public class Video1Model implements Serializable {
    private int id;
    private String desc;
    private String url;
    private String title;
    // Constructor rỗng (bắt buộc)
    public Video1Model() {
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Video1Model(int id, String desc, String url, String title) {
        this.id = id;
        this.desc = desc;
        this.url = url;
        this.title = title;
    }
}
