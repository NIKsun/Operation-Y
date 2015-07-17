package com.example.searchmycarandroid;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.List;


public class ListOfCars extends Activity {
    ListOfCars loc = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.listofcars);
        MyTask f=new MyTask();
        Context context = this;
        f.execute();
    }
    class MyTask extends AsyncTask<Void, Void, Void> {
        String[] texts;
        String[] images;
        String s_data = "", request = getIntent().getStringExtra("request");
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

            images = new String[autoInfoArr.length/2];
            texts = new String[autoInfoArr.length/2];
            for(int i=0;i<autoInfoArr.length;i+=2)
            {
                images[i/2] = autoInfoArr[i];
                texts[i/2] = autoInfoArr[i+1];
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            ProgressBar pb = (ProgressBar)findViewById(R.id.progressBar);
            pb.setVisibility(View.INVISIBLE);

            ListView lv=(ListView)findViewById(R.id.listView);
            lv.setAdapter(new ListViewAdapter(loc, texts, images));


        }
    }
}