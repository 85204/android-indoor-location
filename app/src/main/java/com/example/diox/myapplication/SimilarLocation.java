package com.example.diox.myapplication;

import android.database.Cursor;

/**
 * Created by Diox on 2016/4/11.
 */
public class SimilarLocation {
    private MySQLiteHelper db;

    public SimilarLocation(MySQLiteHelper db) {

        this.db = db;
    }

    public int[] getMaxSimilarLocation(Location location) {
        SimilarDegree similarDegree = new SimilarDegree();
        for (int i = 0; i < 4; i++) {
            Cursor cursor = db.queryByMac(location.getMac(i),location.getOrientation().toString());
            int signal = location.getSignal(i);
            while (cursor.moveToNext()) {
                int x = cursor.getInt(cursor.getColumnIndex("x_id"));
                int y = cursor.getInt(cursor.getColumnIndex("y_id"));
                int similar = Math.abs(signal - cursor.getInt(cursor.getColumnIndex("signal")));
                try {
                    similarDegree.addSimilar(x, y, similar,144/(i+1)/(i+1));
                } catch (Exception e) {
                    e.getStackTrace();
                }
            }
        }
        return similarDegree.getMaxSimilarLocation();
    }
}

class SimilarDegree {
    static private int LENGTH = 50;
    private int[] similar = new int[LENGTH];
    private int[] x = new int[LENGTH];
    private int[] y = new int[LENGTH];
    private int count;


    SimilarDegree() {
        for (int i =0; i<LENGTH;i++){
            x[i]=-200;
            y[i]=-200;
        }
        count = 0;
    }

    public void addSimilar(int x, int y, int similar, int salt) throws Exception {
        for (int i = 0; i < this.count; i++) {
            if (x == this.x[i] && y == this.y[i]) {
                this.similar[i] -= similar * salt;
                return;
            }
        }
        this.x[count]=x;
        this.y[count]=y;
        this.similar[count] -= similar*salt;
        count++;
        if (count > 50) {
            throw new Exception("超过长度上限");
        }
    }

    public int[] getMaxSimilarLocation() {
        int maxSimilar = -2000;
        int count = 0;
        for (int i = 0; i < this.count; i++) {
            if (maxSimilar < this.similar[i]) {
                maxSimilar = this.similar[i];
                count = i;
            }
        }
        return new int[]{this.x[count], this.y[count]};
    }
}