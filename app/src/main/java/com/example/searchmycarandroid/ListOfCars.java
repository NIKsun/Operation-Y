package com.example.searchmycarandroid;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.List;


public class ListOfCars extends Activity {
    Toast toastErrorConnection, toastErrorCarList;
    AlertDialog.Builder ad;
    String lastCarID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.listofcars);
        LoadListView loader = new LoadListView();

        toastErrorConnection = Toast.makeText(getApplicationContext(),
                "Связь с сервером не установлена :(", Toast.LENGTH_SHORT);
        toastErrorCarList = Toast.makeText(getApplicationContext(),
                "По вашему запросу ничего не найдено", Toast.LENGTH_SHORT);

        ad = new AlertDialog.Builder(ListOfCars.this);
        ad.setTitle("Запустить мониторинг?");
        ad.setMessage("Будут приходить уведомления о поступлении новых авто.\nМожно запустить только один мониторинг.");
        ad.setPositiveButton("Запустить новый монитор", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {

                SharedPreferences sPref = getSharedPreferences("SearchMyCarPreferences", Context.MODE_PRIVATE);
                SharedPreferences.Editor ed = sPref.edit();
                stopService(new Intent(ListOfCars.this, MonitoringService.class));
                String request = sPref.getString("SearchMyCarRequest", "");

                ed.putString("SearchMyCarRequestService", request);
                ed.putString("SearchMyCarLastCarID", lastCarID);
                ed.commit();

                startService(new Intent(ListOfCars.this, MonitoringService.class));

                Toast.makeText(ListOfCars.this, "Новый монитор запущен", Toast.LENGTH_SHORT).show();
            }
        });
        ad.setNegativeButton("Остановить старый монитор", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                stopService(new Intent(ListOfCars.this, MonitoringService.class));
                Toast.makeText(ListOfCars.this, "Монитор остановлен.", Toast.LENGTH_SHORT).show();
            }
        });
        ad.setCancelable(true);
        ad.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                Toast.makeText(ListOfCars.this, "Вы не изменили параметры мониторинга", Toast.LENGTH_SHORT).show();
            }
        });

        SharedPreferences sPref = getSharedPreferences("SearchMyCarPreferences", Context.MODE_PRIVATE);
        String request = sPref.getString("SearchMyCarRequest", "");
        loader.execute(request);
    }

    public void onClickStart(View v) {
        ad.show();
    }

    class LoadListView extends AsyncTask<String, Void, Cars> {
        String[] textsAndRefs;
        String[] imagesRef;
        Bitmap[] images;
        String s_data = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            ProgressBar pb = (ProgressBar)findViewById(R.id.progressBar);
            pb.setVisibility(View.VISIBLE);
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        protected Cars doInBackground(String... params) {


            /*Socket soc = new Socket();
            try {
                soc.connect(new InetSocketAddress(InetAddress.getByName("193.124.59.57"), 11111));
                soc.setKeepAlive(true);
                soc.getOutputStream().write(params[0].getBytes());
                int r = 0;
                byte[] buf = new byte[64 * 1024];
                while(true) {
                    r = soc.getInputStream().read(buf);
                    if(r == -1)
                        break;
                    s_data += new String(buf, 0, r);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            if(soc.isConnected() == false)
            {
                toastError.show();
                finish();
                return false;
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
            lastCarID = autoInfoArr[autoInfoArr.length - 1];
            SharedPreferences sPref = getSharedPreferences("SearchMyCarPreferences", Context.MODE_PRIVATE);
            Boolean IsFromService = sPref.getBoolean("SearchMyCarIsFromService", false);
            if(IsFromService)
            {
                SharedPreferences.Editor ed = sPref.edit();
                ed.putString("SearchMyCarLastCarID", lastCarID);
                ed.commit();
            }*/

            Cars cars = null;
            try {
                Document doc;
                doc  = Jsoup.connect(params[0]).userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; ru-RU; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6").get();
                Elements mainElems  =  doc.select("body > div.branding_fix > div.content.content_style > article > div.clearfix > div.b-page-wrapper > div.b-page-content").first().children();


                Log.i("Vizant1", String.valueOf(mainElems.size()));

                Elements listOfCars = null;
                for(int i=0;i<mainElems.size();i++)
                {
                    String className = mainElems.get(i).className();
                    if((className.indexOf("widget widget_theme_white sales-list") == 0) && (className.length() == 36)){
                        Log.i("Vizant34", className);
                        listOfCars = mainElems.get(i).select("div.sales-list-item");
                        break;
                    }
                }
                if(listOfCars == null)
                {
                    Log.i("Vizant1","empty");
                    toastErrorCarList.show();
                    return null;
                }
                Log.i("Vizant2", "Now");

                cars = new Cars(listOfCars.size());
                Bitmap LoadingImage = BitmapFactory.decodeResource(getResources(), R.drawable.res);
                images = new Bitmap[listOfCars.size()];
                for(int i=0;i<listOfCars.size();i++)
                {
                    images[i] = LoadingImage;
                    cars.addFromAutoRu(listOfCars.get(i).select("table > tbody > tr").first());
                }
            } catch (Exception e) {
                e.printStackTrace();
                toastErrorConnection.show();
                return null;
            }
            return cars;
        }
        @Override
        protected void onPostExecute(Cars result) {
            Log.i("Vizant2", "Now2");
            super.onPostExecute(result);
            ProgressBar pb = (ProgressBar) findViewById(R.id.progressBar);
            pb.setVisibility(View.INVISIBLE);

            if(result == null) {
                finish();
                return;
            }

            ListView lv = (ListView) findViewById(R.id.listView);
            SharedPreferences sPref = getSharedPreferences("SearchMyCarPreferences", Context.MODE_PRIVATE);
            lv.setAdapter(new ListViewAdapter(ListOfCars.this, result, images, sPref.getInt("SearchMyCarCountOfNewCars", 0)));

            imagesRef = new String[result.getLenth()];
            for (int i = 0; i < result.getLenth(); i++) {
                imagesRef[i] = result.getImg(i);
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