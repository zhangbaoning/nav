package com.example.nav;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.*;
import android.os.Build;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import java.text.SimpleDateFormat;
import java.util.Date;

import static android.hardware.SensorManager.SENSOR_DELAY_UI;

public class MainActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        sensorManager.registerListener(new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                TextView textView = findViewById(R.id.pressTextView);
                float pressure = event.values[0];
                double height = 44300 * (1 - Math.pow((pressure * 100 / 101325), (1 / 5.256)));
                textView.setText("大气压：" + pressure / 10 + "kPa，海拔高度：" + height + "m");
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        }, pressureSensor, SENSOR_DELAY_UI);
        final LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        }
        final StringBuffer stringBuffer = new StringBuffer();
        locationManager.registerGnssStatusCallback(new GnssStatus.Callback() {
            boolean isFix = false;
            @Override
            public void onStarted() {
                super.onStarted();
            }

            @Override
            public void onStopped() {
                super.onStopped();
            }

            @Override
            public void onFirstFix(int ttffMillis) {
                isFix = true;
                super.onFirstFix(ttffMillis);
            }

            @Override
            public void onSatelliteStatusChanged(GnssStatus status) {
                if (!isFix){


                TextView textView = findViewById(R.id.gpsTextView);
                textView.setMovementMethod(ScrollingMovementMethod.getInstance());
                StringBuffer stringBuffer=new StringBuffer();
                for (int i = 0; i < status.getSatelliteCount(); i++) {
                    if (status.getConstellationType(i)==GnssStatus.CONSTELLATION_BEIDOU){
                        stringBuffer.append("北斗 "+status.getSvid(i)+"\n");
                    }
                    if (status.getConstellationType(i)==GnssStatus.CONSTELLATION_GPS){
                        stringBuffer.append("GPS "+status.getSvid(i)+"\n");
                    }
                    textView.setText(stringBuffer.toString());
                }
                super.onSatelliteStatusChanged(status);
            }
            }
        }) ;           /*    for (GpsSatellite satellite : locationManager.getGpsStatus(null).getSatellites()) {

            locationManager.addGpsStatusListener(
        new GpsStatus.Listener() {

            @Override
            public void onGpsStatusChanged(int event) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    Activity#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for Activity#requestPermissions for more details.
                    return;
                }
                  // stringBuffer.append("Azimuth "+satellite.getAzimuth()) ;
                    //stringBuffer.append("Elevation "+satellite.getElevation());
                    stringBuffer.append("Prn "+satellite.getPrn());
                    //stringBuffer.append("Snr "+satellite.getSnr());
                    stringBuffer.append("\n");
                    TextView textView = findViewById(R.id.gpsTextView);
                    textView.setText(stringBuffer.toString());
                }
            }
        });*/
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 1, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
               TextView textView =  findViewById(R.id.gpsTextView);
                Date date = new Date(location.getTime());
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyMMdd HH:mm:ss");
                StringBuffer stringBuffer1 = new StringBuffer();
                stringBuffer.append("时间："+simpleDateFormat.format(date));
                stringBuffer.append("gps海拔高度："+location.getAltitude());
               textView.setText(stringBuffer1.toString());
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {
                System.out.println(provider);
            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        });
    }
}
