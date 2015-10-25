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
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        // Display a notification about us starting.  We put an icon in the status bar.
        showNotification();
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("GeolocateService", "Received start id " + startId + ": " + intent);

        LocationManager mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        LocationListener mlocListener = new MyLocationListener();

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return 0;
        }
        mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mlocListener);

        //Intent GPS_intent = new Intent(this, GeolocateIntentService.class);
        //this.startService(GPS_intent); //need to think
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
        /*
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = "Voluntezy";

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, Geolocate.class), 0);

        // Set the info for the views that show in the notification panel.
        Notification notification = new Notification.Builder(this)
                //.setSmallIcon()  // the status icon
                .setTicker(text)  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle("Voluntezy")  // the label of the entry
                .setContentText(text)  // the contents of the entry
                .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                .build();

        // Send the notification.
        mNM.notify(NOTIFICATION, notification);
        */
    }

    /* Class My Location Listener */
    public class MyLocationListener implements LocationListener
    {

        @Override
        public void onLocationChanged(Location loc)
        {

            String Text = "Current location: " +
                    "Latitude = " + loc.getLatitude() +
                    "Longitude = " + loc.getLongitude();

            new SendLocation().execute("Shane",
                    Double.toString(loc.getLatitude()),
                    Double.toString(loc.getLongitude()));

            Toast.makeText(getApplicationContext(), Text, Toast.LENGTH_SHORT).show();


        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.i("GeolocateIntentService", "GPS state change");
        }

        @Override
        public void onProviderDisabled(String provider)
        {
            Toast.makeText( getApplicationContext(), "Gps Disabled", Toast.LENGTH_SHORT ).show();
        }

        @Override
        public void onProviderEnabled(String provider)
        {
            Toast.makeText( getApplicationContext(), "Gps Enabled", Toast.LENGTH_SHORT).show();
        }

    }
}

class SendLocation extends AsyncTask<String, Void, Void> {

    private Exception exception;

    protected Void doInBackground(String ... params) {
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
