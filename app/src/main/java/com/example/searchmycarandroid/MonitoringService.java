package com.example.searchmycarandroid;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class MonitoringService extends Service {
    NotificationManager nm;

    public class ServiceThread implements Runnable {
        public int serviceId;
        public String requestAvito, requestAuto;
        public ServiceThread(int Id, String requestAvito, String requestAuto) {
            this.serviceId=Id;
            this.requestAuto = requestAuto;
            this.requestAvito = requestAvito;
        }
        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        public void run() {
            try {
                ServiceProcess(serviceId, requestAvito, requestAuto);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void onCreate() {
        super.onCreate();
        nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
    }
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences sPref = getSharedPreferences("SearchMyCarPreferences", Context.MODE_PRIVATE);
        String[] status = getSharedPreferences("SearchMyCarPreferences", Context.MODE_PRIVATE).getString("SearchMyCarService_status", "").split(";");
        for(int i=1;i<=3;i++) {
            if(status[i-1].equals("true")) {
                String requestAuto = sPref.getString("SearchMyCarServiceRequestAuto" + i, "");
                String requestAvito = sPref.getString("SearchMyCarServiceRequestAvito" + i, "");
                Runnable st = new ServiceThread(i, requestAvito, requestAuto);
                new Thread(st).start();
            }
        }
        return START_STICKY;
    }

    public void onDestroy() {
        super.onDestroy();
    }
    public IBinder onBind(Intent intent) {
        return null;
    }


    void ServiceProcess(int serviceID, final String requestAvito, final String requestAuto) throws InterruptedException {
        for (int i = 1; i<=100; i++) {
            SharedPreferences sPref = getSharedPreferences("SearchMyCarPreferences", Context.MODE_PRIVATE);
            String status = sPref.getString("SearchMyCarService_status", "");
            String[] stat;
            if (status.equals(""))
                return;
            else
                stat = status.split(";");
            if(stat[serviceID-1].equals("false"))
                return;

            int period = sPref.getInt("SearchMyCarService_period"+serviceID,6)+4;
            Log.i("Bar2", String.valueOf(period));
            TimeUnit.SECONDS.sleep(60 * period);

            String lastCarID = sPref.getString("SearchMyCarServiceLastCarDate" + serviceID, "");
            final long lastCarDateinMs = Date.parse(lastCarID);
            final int[] counter = {0};

            Thread t = new Thread(new Runnable() {
                @TargetApi(Build.VERSION_CODES.HONEYCOMB)
                public void run() {
                    Document doc;
                    Elements mainElems;
                    if(!requestAuto.equals("###")) {
                        try {
                            doc = Jsoup.connect(requestAuto).userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; ru-RU; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6").get();
                        } catch (IOException e) {
                            return;
                        }
                        mainElems = doc.select("body > div.branding_fix > div.content.content_style > article > div.clearfix > div.b-page-wrapper > div.b-page-content").first().children();

                        Elements listOfCars = null;
                        for (int i = 0; i < mainElems.size(); i++) {
                            String className = mainElems.get(i).className();
                            if ((className.indexOf("widget widget_theme_white sales-list") == 0) && (className.length() == 36)) {
                                listOfCars = mainElems.get(i).select("div.sales-list-item");
                                break;
                            }
                        }
                        if (listOfCars == null) {
                            return;
                        }
                        Date buf;
                        for (int i = 0; i < listOfCars.size(); i++) {
                            buf = Cars.getDateAuto(listOfCars.get(i).select("table > tbody > tr").first());
                            if(buf != null && lastCarDateinMs < Date.parse(buf.toString()))
                                counter[0]++;
                        }
                    }
                    if(!requestAvito.equals("###")) {
                        try {
                            doc = Jsoup.connect(requestAvito).userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; ru-RU; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6").get();
                        } catch (Exception e) {
                            return;
                        }
                        mainElems = doc.select("#catalog > div.layout-internal.col-12.js-autosuggest__search-list-container > div.l-content.clearfix > div.clearfix > div.catalog.catalog_table > div.catalog-list.clearfix").first().children();

                        for (int i = 0; i < mainElems.size(); i++)
                            for (int j = 0; j < mainElems.get(i).children().size(); j++) {
                                if (lastCarDateinMs < Date.parse(Cars.getDateAvito(mainElems.get(i).children().get(j)).toString()))
                                    counter[0]++;
                            }
                    }
                }
            });
            t.start();
            while (t.isAlive());
            if(counter[0] != 0) {
                sendNotification(counter[0], serviceID);
            }

        }
    }

    void sendNotification(int countOfNewCars, int serviceID) {

        Notification notif = new Notification(R.drawable.status_bar, "Новое авто!",
                System.currentTimeMillis());

        Intent intent = new Intent(this, NotificationActivity.class);
        intent.putExtra("MonitorNumber", 1);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

        /*SharedPreferences sPref = getSharedPreferences("SearchMyCarPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putInt("SearchMyCarServiceOutput", serviceID);
        ed.commit();*/

        if(countOfNewCars == 1)
            notif.setLatestEventInfo(this, "Монитор "+serviceID, "Найден "+countOfNewCars+" новый авто", pIntent);
        else
            notif.setLatestEventInfo(this, "Монитор "+serviceID, "Найдено "+countOfNewCars+" новых авто", pIntent);
        Uri ringURI = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        notif.number = countOfNewCars;
        notif.sound = ringURI;
        notif.flags |= Notification.FLAG_AUTO_CANCEL | Notification.DEFAULT_SOUND | Notification.FLAG_ONLY_ALERT_ONCE;
        nm.notify(1, notif);
    }
}
