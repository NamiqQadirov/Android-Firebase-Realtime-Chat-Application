package com.example.namiq.egisterpp.dbo;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by Namiq on 2/23/2017.
 */
@IgnoreExtraProperties
public class Message {

    public String name;
    public String url;
    public String type;
    public String sender;
    public String receiver;

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }


    public String getType() {
        return type;
    }


    // Default constructor required for calls to
    // DataSnapshot.getValue(User.class)
    public Message() {
    }

    public Message(String name, String url, String sender, String receiver, String type) {
        this.name = name;
        this.url = url;
        this.type = type;
        this.sender = sender;
        this.receiver = receiver;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }
}