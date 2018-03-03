package com.example.namiq.egisterpp.dbo;

/**
 * Created by Namiq on 8/1/2017.
 */

public class User {
    private String name;
    private String email;
    private String url;
    private String status;
    private long createTime;


    private String description;

    public User() {

    }


    public User(String name, String email, String url, String status, long createTime, String description) {
        this.name = name;
        this.email = email;
        this.url = url;
        this.status = status;
        this.createTime = createTime;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getUrl() {
        return url;
    }

    public String getStatus() {
        return status;
    }

    public long getCreateTime() {
        return createTime;
    }

    public String getDescription() {
        return description;
    }

}
