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
        String[] imagesRef;
        Bitmap[] images;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ProgressBar pb = (ProgressBar)findViewById(R.id.progressBar);
            pb.setVisibility(View.VISIBLE);
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        protected Cars doInBackground(final String... params) {

            /*
            lastCarID = autoInfoArr[autoInfoArr.length - 1];
            SharedPreferences sPref = getSharedPreferences("SearchMyCarPreferences", Context.MODE_PRIVATE);
            Boolean IsFromService = sPref.getBoolean("SearchMyCarIsFromService", false);
            if(IsFromService)
            {
                SharedPreferences.Editor ed = sPref.edit();
                ed.putString("SearchMyCarLastCarID", lastCarID);
                ed.commit();
            }*/

            final Cars[] carsAvto = new Cars[1], carsAvito = new Cars[1];
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
                        toastErrorCarList.show();
                        bulAvto[0] = false;
                        return;
                    }

                    carsAvto[0] = new Cars(listOfCars.size());
                    for(int i=0;i<listOfCars.size();i++)
                    {
                        carsAvto[0].addFromAutoRu(listOfCars.get(i).select("table > tbody > tr").first());
                    }
                }
            });
            Thread threadAvito = new Thread(new Runnable() {
                @TargetApi(Build.VERSION_CODES.HONEYCOMB)
                public void run() {
                    Document doc;
                    try {
                        doc = Jsoup.connect("https://www.avito.ru/rossiya/avtomobili/chevrolet/lanos").userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; ru-RU; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6").get();
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
            threadAvito.start();
            threadAvto.start();
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
            return cars;
        }
        @Override
        protected void onPostExecute(Cars result) {
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