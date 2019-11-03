package com.team09.hwealth;

public class MessageData {

    private String name;
    private String message;
    private String time;
    private String uid;
    private String cid;

    public MessageData() {

    }

    public MessageData(String name, String message, String time, String uid, String cid){
        this.name = name;
        this.message = message;
        this.time = time;
        this.uid = uid;
        this.cid = cid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }
}
