package com.example.diox.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.util.List;

/**
 * Created by Diox on 2016/4/15.
 */


public class LocationServer {

    private static final int MIN_LOCATION = -9999;

    private MySQLiteHelper db;

    LocationServer(Context context) {
        db = new MySQLiteHelper(context, "testDB", null, 1);

    }

    public boolean isIdHasAdded(int x_id, int y_id, Orientation orientation) {
        Cursor cursor = db.queryById(x_id, y_id, orientation.toString());
        return cursor.moveToNext();
    }


    public void addLocationToDB(String Txt_x, String Txt_y, List list, Orientation orientation, Way WAY) {
        for (int i = 0; i < list.size(); i++) {
            String tmp = list.get(i).toString();
            ContentValues cv = new ContentValues();
            cv.put("x_id", Integer.parseInt(Txt_x));
            cv.put("y_id", Integer.parseInt(Txt_y));
            cv.put("ori", orientation.toString());
            cv.put("mac", tmp.substring(tmp.indexOf("BSSID: ") + 7, tmp.indexOf("BSSID: ") + 24));
            cv.put("signal", Integer.parseInt(tmp.substring(tmp.indexOf("level: ") + 7, tmp.indexOf(", fre"))));
            if (WAY == Way.UPDATE_DB)
                db.update(cv);
            else if (WAY == Way.INSERT_DB)
                db.insert(cv);
        }
    }

    public String viewAllLocation(int VIEW_LINE) {
        String result = "";
        Cursor cursor = db.queryAll();
        int x = MIN_LOCATION;
        int y = MIN_LOCATION;
        String newOri = "";
        int i = 0;
        while (cursor.moveToNext()) {
            int x_id = cursor.getInt(cursor.getColumnIndex("x_id"));
            int y_id = cursor.getInt(cursor.getColumnIndex("y_id"));
            String ori = cursor.getString(cursor.getColumnIndex("ori"));
            if (x_id != x || y_id != y || !newOri.equals(ori)) {
                x = x_id;
                y = y_id;
                newOri = ori;
                if (i > 0)
                    result = result + "\n\n";
                i = 0;
                result = result + String.format("X: %d Y: %d Ori: %s\n", x_id, y_id, ori);
            }
            if (i > VIEW_LINE) {
                continue;
            }
            String mac = cursor.getString(cursor.getColumnIndex("mac"));
            int signal = cursor.getInt(cursor.getColumnIndex("signal"));
            result = result + String.format("%s, %d\n", mac, signal);
            i++;
        }
        return result;

    }

    public String getLocationNow(List list, Orientation orientation) {
        if (list==null){
            return "";
        }
        String result;
        Location newLocation = new Location(orientation);
        for (int i = 0; i < list.size(); i++) {
            String tmp = list.get(i).toString();
            newLocation.addMac(tmp.substring(tmp.indexOf("BSSID: ") + 7, tmp.indexOf("BSSID: ") + 24),
                    Integer.parseInt(tmp.substring(tmp.indexOf("level: ") + 7, tmp.indexOf(", fre"))));
        }
        SimilarLocation similarLocation = new SimilarLocation(db);
        int[] temp = similarLocation.getMaxSimilarLocation(newLocation);
        result = String.format("X: %d, Y: %d, Ori: %s\n", temp[0], temp[1], orientation.toString());
        for (int j = 0; j < 4; j++) {
            result = ((result + newLocation.getMac(j) + "," + newLocation.getSignal(j) + "\n"));
        }
        return result;
    }

    public MySQLiteHelper getDb() {
        return db;
    }

    public void setDb(MySQLiteHelper db) {
        this.db = db;
    }


}
