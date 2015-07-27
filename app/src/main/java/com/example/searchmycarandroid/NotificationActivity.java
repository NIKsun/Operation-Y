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
import android.text.BoringLayout;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.util.List;


public class NotificationActivity extends Activity {
    Toast toastErrorConnection, toastErrorCarList;
    String lastCarDate;
    int monitorNumber;

    LoadListViewMonitor loader = new LoadListViewMonitor();

    @Override
    protected void onDestroy() {
        loader.cancel(true);
        super.onDestroy();
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.monitor_list_of_cars);


        toastErrorConnection = Toast.makeText(getApplicationContext(),
                "Связь с сервером не установлена :(", Toast.LENGTH_SHORT);
        toastErrorCarList = Toast.makeText(getApplicationContext(),
                "По вашему запросу ничего не найдено", Toast.LENGTH_SHORT);

        monitorNumber = getIntent().getIntExtra("MonitorNumber", 0);
        if(monitorNumber==0) {
            Log.i("Service", "herovo");return;
        }

        SharedPreferences sPref = getSharedPreferences("SearchMyCarPreferences", Context.MODE_PRIVATE);
        String requestAuto = sPref.getString("SearchMyCarServiceRequestAuto" + monitorNumber, "");
        String requestAvito = sPref.getString("SearchMyCarServiceRequestAvito" + monitorNumber, "");
        lastCarDate = sPref.getString("SearchMyCarServiceLastCarDate" + monitorNumber, "");
        loader.execute(requestAuto, requestAvito);
    }


    public void onClickStart(View v) {

    }

    class LoadListViewMonitor extends AsyncTask<String, Void, Cars> {
        String[] imagesRef;
        Bitmap[] images;
        final Cars[] carsAvto = new Cars[1], carsAvito = new Cars[1];

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ProgressBar pb = (ProgressBar)findViewById(R.id.progressBarMonitor);
            pb.setVisibility(View.VISIBLE);
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        protected Cars doInBackground(final String... params) {

            final Boolean[] bulAvto = {true}, bulAvito = {true}, connectionSuccess = {true};
            Thread threadAvto = new Thread(new Runnable() {
                @TargetApi(Build.VERSION_CODES.HONEYCOMB)
                public void run() {
                    Document doc;
                    try {
                        doc  = Jsoup.connect(params[0]).userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; ru-RU; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6").get();
                    }
                    catch (IOException e)
                    {
                        connectionSuccess[0] = false;
                        return;
                    }
                    Elements mainElems  =  doc.select("body > div.branding_fix > div.content.content_style > article > div.clearfix > div.b-page-wrapper > div.b-page-content").first().children();

                    Elements listOfCars = null;
                    for(int i=0;i<mainElems.size();i++)
                    {
                        String className = mainElems.get(i).className();
                        if((className.indexOf("widget widget_theme_white sales-list") == 0) && (className.length() == 36)){
                            listOfCars = mainElems.get(i).select("div.sales-list-item");
                            break;
                        }
                    }
                    if(listOfCars == null)
                    {
                        bulAvto[0] = false;
                        return;
                    }

                    carsAvto[0] = new Cars(listOfCars.size());
                    for(int i=0;i<listOfCars.size();i++)
                        carsAvto[0].addFromAutoRu(listOfCars.get(i).select("table > tbody > tr").first());
                }
            });
            Thread threadAvito = new Thread(new Runnable() {
                @TargetApi(Build.VERSION_CODES.HONEYCOMB)
                public void run() {
                    Document doc;
                    try {
                        doc = Jsoup.connect(params[1]).userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; ru-RU; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6").get();
                    }
                    catch (HttpStatusException e)
                    {
                        bulAvito[0]=false;
                        return;
                    }
                    catch (IOException e)
                    {
                        connectionSuccess[0] = false;
                        return;
                    }
                    Elements mainElems = doc.select("#catalog > div.layout-internal.col-12.js-autosuggest__search-list-container > div.l-content.clearfix > div.clearfix > div.catalog.catalog_table > div.catalog-list.clearfix").first().children();
                    int length = 0;
                    for (int i = 0; i < mainElems.size(); i++)
                        length += mainElems.get(i).children().size();

                    carsAvito[0] = new Cars(length);
                    for (int i = 0; i < mainElems.size(); i++)
                        for (int j = 0; j < mainElems.get(i).children().size(); j++) {
                            carsAvito[0].addFromAvito(mainElems.get(i).children().get(j));
                        }
                    carsAvito[0].sortByDateAvito();

                }
            });
            if(!params[0].equals("###"))
                threadAvto.start();
            else
                bulAvto[0] = false;
            if(!params[1].equals("###"))
                threadAvito.start();
            else
                bulAvito[0] = false;

            while (threadAvto.isAlive() || threadAvito.isAlive()); //waiting



            if(!connectionSuccess[0]) {
                toastErrorConnection.show();
                return null;
            }
            if(!bulAvito[0] && !bulAvto[0])
            {
                toastErrorCarList.show();
                return null;
            }
            if(!bulAvito[0])
                carsAvito[0] = new Cars(0);
            if(!bulAvto[0])
                carsAvto[0] = new Cars(0);

            Cars cars = Cars.merge(carsAvto[0],carsAvito[0]);
            Bitmap LoadingImage = BitmapFactory.decodeResource(getResources(), R.drawable.res);
            images = new Bitmap[cars.getLenth()];
            for(int i=0;i<cars.getLenth();i++)
                images[i] = LoadingImage;

            SharedPreferences sPref = getSharedPreferences("SearchMyCarPreferences", Context.MODE_PRIVATE);
            sPref.edit().putString("SearchMyCarServiceLastCarDate" + monitorNumber, cars.getLastCarDate()).commit();
            return cars;
        }
        @Override
        protected void onPostExecute(Cars result) {
            super.onPostExecute(result);
            ProgressBar pb = (ProgressBar) findViewById(R.id.progressBarMonitor);
            pb.setVisibility(View.INVISIBLE);

            if(result == null) {
                finish();
                return;
            }

            Toast.makeText(NotificationActivity.this, "Найдено " + carsAvto[0].getLenth() + " ПОСЛЕДНИХ объявлений на Auto.ru и "
                    + carsAvito[0].getLenth() + " на Avito.ru, отсортировано по дате", Toast.LENGTH_LONG).show();

            ListView lv = (ListView) findViewById(R.id.listViewMonitor);
            lv.setAdapter(new ListViewAdapter(NotificationActivity.this, result, images, lastCarDate));


            imagesRef = new String[result.getLenth()];
            for (int i = 0; i < result.getLenth(); i++) {
                imagesRef[i] = result.getImg(i);
                LoadImageMonitor li = new LoadImageMonitor();
                li.execute(i);
            }
        }

        class LoadImageMonitor extends AsyncTask<Integer, Void, Integer> {
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
                ListView lv=(ListView)findViewById(R.id.listViewMonitor);
                lv.invalidateViews();
            }

        }
    }
}