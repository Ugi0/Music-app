package com.tsevaj.musicapp;

public class MyList {
    private final String head;
    private final String desc;
    private final String location;
    private final int duration;

    //constructor initializing values
    public MyList(String head, String desc, String location, int duration) {
        this.head = head;
        this.desc = desc;
        this.location = location;
        this.duration = duration;
    }

    //getters
    public String getHead() {
        return head;
    }

    public String getDesc() {
        return desc;
    }

    public String getLocation() { return location; }

    public int getDuration() { return duration; }

}