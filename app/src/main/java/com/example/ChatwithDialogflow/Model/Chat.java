package com.example.ChatwithDialogflow.Model;

public class Chat {

//    private String sender;
    private String type;
    private String message;

    public Chat(String type, String message) {
        this.type = type;
        this.message = message;
    }

    public Chat(){

    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
