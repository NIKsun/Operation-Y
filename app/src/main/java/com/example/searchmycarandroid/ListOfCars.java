package com.example.searchmycarandroid;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.NetworkOnMainThreadException;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;


public class ListOfCars extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.listofcars);
        MyTask f=new MyTask();
        f.execute();
    }
    class MyTask extends AsyncTask<Void, Void, Void> {

        String s_data, request = getIntent().getStringExtra("request");
        TextView text = (TextView) findViewById(R.id.textView1);
        ImageView image = (ImageView) findViewById(R.id.imageView1);
        Bitmap bm;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            ProgressBar pb = (ProgressBar)findViewById(R.id.progressBar);
            pb.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            //TimeUnit.SECONDS.sleep(2);
            Log.d("1",request);



            Thread t = new Thread(new Runnable() {
                @TargetApi(Build.VERSION_CODES.HONEYCOMB)
                public void run() {
                    Socket soc = null;
                    try {
                        soc = new Socket();
                        soc.bind(null);
                        soc.connect(new InetSocketAddress(InetAddress.getByName("193.124.58.92"), 11111));
                        soc.setKeepAlive(true);
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    catch (NetworkOnMainThreadException e) {
                        e.printStackTrace();
                    }

                    try {
                        soc.getOutputStream().write(request.getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    int r = 0;
                    byte[] buf = new byte[64 * 1024];
                    try {
                        r = soc.getInputStream().read(buf);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    s_data = new String(buf, 0, r);
                }
            });

            t.start();
            while(t.isAlive())
                continue;
            try {
                bm = BitmapFactory.decodeStream((InputStream) new URL(s_data.substring(0, s_data.indexOf("@@@"))).getContent());
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d("1111111", String.valueOf(s_data.length()));

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            ProgressBar pb = (ProgressBar)findViewById(R.id.progressBar);
            pb.setVisibility(View.INVISIBLE);

            try{
                text.setText(Html.fromHtml(s_data.substring(s_data.indexOf("@@@") + 3, s_data.length())));
            }
            catch (Exception e){
                e.printStackTrace();
            }

            image.setImageBitmap(bm);
        }
    }
}