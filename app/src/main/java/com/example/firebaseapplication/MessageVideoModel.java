package com.example.firebaseapplication;

import java.io.Serializable;
import java.util.List;

public class MessageVideoModel implements Serializable {
    private boolean success;
    private String message;
    private List<Video1Model> result;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Video1Model> getResult() {
        return result;
    }

    public void setResult(List<Video1Model> result) {
        this.result = result;
    }

    public MessageVideoModel(boolean success, String message, List<Video1Model> result) {
        this.success = success;
        this.message = message;
        this.result = result;
    }
}
