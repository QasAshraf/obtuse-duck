package com.example.shaneegan.backgroundgeolocate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class BootupReceiver extends BroadcastReceiver {
    public BootupReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // an Intent broadcast.
        Log.i("BackgroundGeolocate", "Got intent to start on bootup");

        Intent service = new Intent(context, GeolocateService.class);
        context.startService(service);
    }
}
