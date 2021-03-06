package com.example.shaneegan.backgroundgeolocate;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class GeolocateService extends Service {
    public GeolocateService() {
    }

    private NotificationManager mNM;

    private int NOTIFICATION = 1;

    @Override
    public void onCreate() {
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Display a notification about us starting.  We put an icon in the status bar.
        showNotification();
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("GeolocateService", "Received start id " + startId + ": " + intent);

        LocationManager mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        LocationListener mlocListener = new MyLocationListener();

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

            Log.i("GeolocateService", "Cannot listen for location updates as no permissions");
            return 0;
        }

        int MIN_TIME_IN_MINS_BETWEEN_UPDATES = 1;
        int MIN_DISTANCE_IN_METERS = 50;

        mlocManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                (MIN_TIME_IN_MINS_BETWEEN_UPDATES * 60 * 100),
                MIN_DISTANCE_IN_METERS,
                mlocListener);


        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        Log.i("GeolocateService", "destroyed GeolocateService");

        mNM.cancel(NOTIFICATION);

        // Tell the user we stopped.
        Toast.makeText(this, "Destroyed Geolocate Service", Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Show a notification while this service is running.
     */
    private void showNotification() {

        return;
    }

    /* Class My Location Listener */
    public class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location loc) {

            String Text = "Current location: " +
                    "Latitude = " + loc.getLatitude() +
                    ", Longitude = " + loc.getLongitude();

            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            String imei = tm.getDeviceId();

            new SendLocation().execute(imei,
                    Double.toString(loc.getLatitude()),
                    Double.toString(loc.getLongitude()));

            Toast.makeText(getApplicationContext(), Text, Toast.LENGTH_SHORT).show();


        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.i("GeolocateService", "GPS state change");
        }

        @Override
        public void onProviderDisabled(String provider) {
            Toast.makeText(getApplicationContext(), "Gps Disabled", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderEnabled(String provider) {
            Toast.makeText(getApplicationContext(), "Gps Enabled", Toast.LENGTH_SHORT).show();
        }

    }
}

class SendLocation extends AsyncTask<String, Void, Void> {

    private Exception exception;

    protected Void doInBackground(String... params) {
        try {

            Map<String, String> data = new HashMap<String, String>();
            data.put("appbundle_history[user]", params[0]);
            data.put("appbundle_history[lat]", params[1]);
            data.put("appbundle_history[lon]", params[2]);
            if (HttpRequest.post("http://voluntezy.com/history/set").form(data).created())
                Log.i("HTTP_BACKGROUND", "Sent Location to Voluntezy");

        } catch (Exception e) {
            this.exception = e;
            return null;
        }
        return null;
    }

}
