package com.example.diox.myapplication;

/**
 * Created by Diox on 2016/4/16.
 */
public enum  Orientation {
    NORTH("NORTH"),SOUTH("SOUTH"),WEST("WEST"),EAST("EAST");
    private String value;

    Orientation(String value){
        this.value=value;
    }

    @Override
    public String toString() {
        return value;
    }
}
