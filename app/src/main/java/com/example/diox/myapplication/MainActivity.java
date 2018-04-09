package com.example.diox.myapplication;


import android.Manifest;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static int VIEW_LINE = 6;
    private static int FILE_SELECT_CODE = 200;

    private boolean isAutoRefreshOn = false;

    private WifiManager wifiManager;
    private SensorManager sensorManager;
    private SensorEventListener sensorEventListener;
    private double ori;
    private double[] oriTemp;
    private int oriCount = 0;

    LocationServer locationServer;

    private TextView tv, tvGet, tvAutoState, tvOri;
    private EditText Txt_x, Txt_y;
    private View view;
    private SwipeRefreshLayout swipeRefreshLayoutGet;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        Txt_x = (EditText) findViewById(R.id.Txt_x);
        Txt_y = (EditText) findViewById(R.id.Txt_y);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        oriTemp = new double[30];


        wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        locationServer = new LocationServer(this);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!isOrientationFit()) {
                        Snackbar.make(view, "Please turn orientation", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                        return;
                    }
                    MainActivity.this.view = view;
                    String x = Txt_x.getText().toString();
                    String y = Txt_y.getText().toString();
                    try {
                        if (locationServer.isIdHasAdded(Integer.parseInt(x), Integer.parseInt(y), getOrientation())) {
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle("The location has been added")
                                    .setMessage("Would you want to update it?")
                                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (!isWifiEnabled())
                                                return;
                                            wifiManager.startScan();
                                            List list = wifiManager.getScanResults();
                                            locationServer.addLocationToDB(Txt_x.getText().toString(),
                                                    Txt_y.getText().toString(), list, getOrientation(), Way.UPDATE_DB);
                                            tv.setText(locationServer.viewAllLocation(VIEW_LINE));
                                            Snackbar.make(MainActivity.this.view, "The location has been updated.", Snackbar.LENGTH_LONG)
                                                    .setAction("Action", null).show();

                                        }
                                    })
                                    .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Snackbar.make(MainActivity.this.view, "Updating has been canceled .", Snackbar.LENGTH_LONG)
                                                    .setAction("Action", null).show();
                                        }
                                    })
                                    .show();
                            return;

                        }
                    } catch (NumberFormatException e) {
                        Snackbar.make(view, e.getMessage(), Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                        return;
                    }
                    if (!isWifiEnabled())
                        return;
                    wifiManager.startScan();
                    List list = wifiManager.getScanResults();
                    locationServer.addLocationToDB(x, y, list, getOrientation(), Way.INSERT_DB);
                    tv.setText(locationServer.viewAllLocation(VIEW_LINE));
                    Snackbar.make(view, "A new location has been added.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            });
        }


        tv = (TextView) findViewById(R.id.textView);
        tvGet = (TextView) findViewById(R.id.textViewGet);
        tvOri = (TextView) findViewById(R.id.viewOri);
        tvAutoState = (TextView) findViewById(R.id.viewAutoRefreshState);


        swipeRefreshLayoutGet = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshGet);
        swipeRefreshLayoutGet.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        tvAutoState.setText("Auto refresh:" + isAutoRefreshOn);
                        if (!isWifiEnabled()){
                            swipeRefreshLayoutGet.setRefreshing(false);
                            return;
                        }
                        wifiManager.startScan();
                        List list = wifiManager.getScanResults();
                        tvGet.setText(locationServer.getLocationNow(list, getOrientation()));
                        swipeRefreshLayoutGet.setRefreshing(false);
                    }
                }, 0);
            }
        });

        isWifiEnabled();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 0x12345);
            } else {
                getWifiList();
            }
        }
        tv.setText(locationServer.viewAllLocation(VIEW_LINE));
        tvAutoState.setText("Auto refresh:" + isAutoRefreshOn);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);


    }
    private List getWifiList(){
        wifiManager.startScan();
        return wifiManager.getScanResults();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0x12345) {
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
            }
            getWifiList();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(sensorEventListener);

    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorEventListener = new SensorEventListener() {
            float[] accelerometerValues = new float[3];
            float[] magneticFieldValues = new float[3];


            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    accelerometerValues = event.values.clone();
                }
                if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                    magneticFieldValues = event.values.clone();
                }
                float[] R = new float[9];
                float[] values = new float[3];
                SensorManager.getRotationMatrix(R, null, accelerometerValues,
                        magneticFieldValues);
                SensorManager.getOrientation(R, values);

                if (oriCount++ >= 29) {
                    oriCount = 0;
                    for (double i : oriTemp) {
                        ori = ori + i;
                    }
                    ori = ori / 30;
                    tvOri.setText(String.format("Ori: %1.1f", ori));
                } else {
                    oriTemp[oriCount] = Math.toDegrees(values[0]);
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        sensorManager.registerListener(sensorEventListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(sensorEventListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_UI);


    }

    private boolean isWifiEnabled() {
        if (!wifiManager.isWifiEnabled()) {
            new AlertDialog.Builder(this)
                    .setTitle("Turn On Wifi")
                    .setPositiveButton("set Wifi", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent wifiSettingsIntent = new Intent("android.settings.WIFI_SETTINGS");
                            startActivity(wifiSettingsIntent);
                        }
                    })
                    .show();
            return false;
        }
        return true;
    }


    private void copyFile(String oldPath, String newPath) {
        try {
            int byteRead;
            File oldFile = new File(oldPath);
            if (oldFile.exists()) { //文件存在时
                InputStream inStream = new FileInputStream(oldPath); //读入原文件
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                while ((byteRead = inStream.read(buffer)) != -1) {
                    fs.write(buffer, 0, byteRead);
                }
                inStream.close();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    private Orientation getOrientation() {
        if (ori >= -45 && ori < 45) {
            return Orientation.NORTH;
        } else if (ori >= 45 && ori < 135) {
            return Orientation.EAST;
        } else if (ori >= 135 || ori < -135) {
            return Orientation.SOUTH;
        } else if (ori >= -135 && ori < -45) {
            return Orientation.WEST;
        }
        return null;
    }

    private boolean isOrientationFit() {
        if (ori >= -10 && ori < 10) {
            return true;
        } else if (ori >= 80 && ori < 100) {
            return true;
        } else if (ori >= 170 || ori < -170) {
            return true;
        } else if (ori >= -100 && ori < -80) {
            return true;
        }
        return false;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            if (!isWifiEnabled()){
                return true;
            }
            if (!isAutoRefreshOn) {
                isAutoRefreshOn = true;
                new AsyncTask<Void, String, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        while (isAutoRefreshOn) {
                            wifiManager.startScan();
                            List list = wifiManager.getScanResults();
                            publishProgress(new String[]{"Auto refresh:" + isAutoRefreshOn,
                                    locationServer.getLocationNow(list, getOrientation())});
                            try {
                                Thread.sleep(6000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        return null;
                    }

                    @Override
                    protected void onProgressUpdate(String... values) {
                        tvAutoState.setText(values[0]);
                        tvGet.setText(values[1]);
                        super.onProgressUpdate(values);
                    }
                }.execute();
            } else {
                isAutoRefreshOn = false;
                tvAutoState.setText("Auto refresh:" + isAutoRefreshOn);
            }
            return true;
        }

        if (id == R.id.action_export) {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                copyFile(getDatabasePath("testDB").getAbsolutePath(), Environment.getExternalStorageDirectory() + "/DB.data");
            }
            return true;
        }

        if (id == R.id.action_import) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            try {
                startActivityForResult(Intent.createChooser(intent, "请选择一个要导入的文件"), FILE_SELECT_CODE);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, "未找到文件浏览器", Toast.LENGTH_SHORT).show();// 可以连接到下载文件管理器的连接让用户下载文件管理器
            }
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == MainActivity.RESULT_OK && requestCode == FILE_SELECT_CODE) {
            Uri uri = data.getData();
            if (uri != null) {
                String path = uri.toString();
                if (path.toLowerCase().startsWith("file://")) {
                    // Selected file/directory path is below
                    path = (new File(URI.create(path))).getAbsolutePath();
                    locationServer.getDb().close();
                    copyFile(path, getDatabasePath("testDB").getAbsolutePath());
                    locationServer.setDb(new MySQLiteHelper(MainActivity.this, "testDB", null, 1, new DatabaseErrorHandler() {
                        @Override
                        public void onCorruption(SQLiteDatabase dbObj) {
                            Toast.makeText(MainActivity.this, "打不开数据库", Toast.LENGTH_SHORT).show();
                            new File(getDatabasePath("testDB").getAbsolutePath()).delete();
                        }
                    }));
                    tv.setText(locationServer.viewAllLocation(VIEW_LINE));
                }

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
