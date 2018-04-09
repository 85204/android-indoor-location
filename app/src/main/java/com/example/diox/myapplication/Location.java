package com.example.diox.myapplication;


/**
 * Created by Diox on 2016/4/5.
 */

public class Location {
    private String[] mac = new String[4];
    private int[] signal = new int[4];
    private Orientation orientation;

    Location(Orientation orientation) {
        for (int i = 0; i < 4; i++) {
            mac[i] = null;
            signal[i] = -200;
        }
        this.orientation = orientation;
    }

    public void addMac(String mac, int signal) {
        for (int i = 0; i < 4; i++) {
            if (signal > this.signal[i]) {
                for (int j = 3; j > i; j--) {
                    this.signal[j] = this.signal[j - 1];
                    this.mac[j] = this.mac[j - 1];
                }
                this.signal[i] = signal;
                this.mac[i] = mac;
                return;
            }
        }
    }

    public String getMac(int val) {
        return mac[val];
    }

    public int getSignal(int val) {
        return signal[val];
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
    }
}

