package com.example.searchmycarandroid;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.List;


public class ListOfCars extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.listofcars);
        LoadListView loader = new LoadListView();
        Context context = this;
        loader.execute();
    }

    public void onClickStart(View v) {
        startService(new Intent(this, MonitoringService.class));
    }

    class LoadListView extends AsyncTask<Void, Void, Void> {
        String[] textsAndRefs;
        String[] imagesRef;
        Bitmap[] images;
        String s_data = "";
        Bitmap bm;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            ProgressBar pb = (ProgressBar)findViewById(R.id.progressBar);
            pb.setVisibility(View.VISIBLE);
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        protected Void doInBackground(Void... params) {
            SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(ListOfCars.this);
            final String request = sPref.getString("SearchMyCarRequest", "");
            Socket soc = null;
            try {
                soc = new Socket();
                soc.bind(null);
                soc.connect(new InetSocketAddress(InetAddress.getByName("192.168.43.238"), 11111));
                soc.setKeepAlive(true);
                soc.getOutputStream().write(request.getBytes());
                int r = 0;
                byte[] buf = new byte[64 * 1024];
                do {
                    r = soc.getInputStream().read(buf);
                    s_data += new String(buf, 0, r);
                }
                while (r != 0);
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            String[] autoInfoArr = s_data.split("@@@");

            int countOfCars = (autoInfoArr.length-1)/3;
            images = new Bitmap[countOfCars];
            imagesRef = new String[countOfCars];
            textsAndRefs = new String[countOfCars*2];

            Bitmap LoadingImage = BitmapFactory.decodeResource(getResources(), R.drawable.res);
            for(int i=0;i<autoInfoArr.length-1;i+=3)
            {
                textsAndRefs[i/3] = autoInfoArr[i];
                imagesRef[i/3] = autoInfoArr[i+1];
                textsAndRefs[i/3+countOfCars] = autoInfoArr[i+2];
                images[i/3] = LoadingImage;
            }

            SharedPreferences.Editor ed = sPref.edit();
            ed.putString("SearchMyCarLastCarID", autoInfoArr[autoInfoArr.length-1]);
            ed.commit();

            return null;
        }


        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            ProgressBar pb = (ProgressBar)findViewById(R.id.progressBar);
            pb.setVisibility(View.INVISIBLE);

            ListView lv=(ListView)findViewById(R.id.listView);
            lv.setAdapter(new ListViewAdapter(ListOfCars.this, textsAndRefs, images));
            for(int i=0;i<images.length;i++)
            {
                LoadImage li = new LoadImage();
                li.execute(i);
            }


        }

        class LoadImage extends AsyncTask<Integer, Void, Integer> {
            Bitmap bm;

            @Override
            protected void onPreExecute() {
            }

            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            @Override
            protected Integer doInBackground(Integer... params) {
                try {
                    bm = BitmapFactory.decodeStream((InputStream) new URL(imagesRef[params[0]]).getContent());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return params[0];
            }

            @Override
            protected void onPostExecute(Integer result) {
                images[result] = bm;
                ListView lv=(ListView)findViewById(R.id.listView);
                lv.invalidateViews();
            }

        }
    }
}