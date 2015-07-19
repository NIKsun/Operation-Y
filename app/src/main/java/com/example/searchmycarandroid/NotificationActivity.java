package com.example.searchmycarandroid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by Никита on 19.07.2015.
 */

public class NotificationActivity extends Activity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(this, ListOfCars.class);
        SharedPreferences sPref = getSharedPreferences("SearchMyCarPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        String request = sPref.getString("SearchMyCarRequestService", "");
        Log.d("Service:NotificationActivity",request);
        ed.putString("SearchMyCarRequest", request);
        ed.putBoolean("SearchMyCarIsFromService", true);
        ed.commit();
        startActivity(intent);
        finish();
    }
}
