package com.example.namiq.egisterpp.dbo;

/**
 * Created by Namiq on 8/4/2017.
 */

public class UnreadedMessages {
    private String message;
    private String type;

    public UnreadedMessages(String message, String type) {
        this.message = message;
        this.type = type;
    }


    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }
}
