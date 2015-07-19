package com.example.searchmycarandroid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;


public class CreateRequestActivity extends Activity implements View.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_create_request);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonSearch:
                Intent intent = new Intent(this, ListOfCars.class);
                TextView request = (TextView) findViewById(R.id.editTextRequest);

                SharedPreferences sPref = getSharedPreferences("SearchMyCarPreferences", Context.MODE_PRIVATE);
                SharedPreferences.Editor ed = sPref.edit();
                ed.putString("SearchMyCarRequest", request.getText().toString());
                ed.putBoolean("SearchMyCarIsFromService", false);
                ed.putInt("SearchMyCarCountOfNewCars", 0);
                ed.commit();
                startActivity(intent);

                break;
            default:
                break;
        }
    }
}
