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

import java.util.concurrent.TimeUnit;

public class MonitoringService extends Service {
    NotificationManager nm;
    String requestAvito, requestAuto;
    String lastCarId;

    Thread mainThread = new Thread(new Runnable() {
        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        public void run() {
            try {
                ServiceProcess();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    });

    public void onCreate() {
        super.onCreate();
        nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
    }
    public int onStartCommand(Intent intent, int flags, int startId) {

        SharedPreferences sPref = getSharedPreferences("SearchMyCarPreferences", Context.MODE_PRIVATE);
        requestAuto = sPref.getString("SearchMyCarServiceRequestAuto", "");
        requestAvito = sPref.getString("SearchMyCarServiceRequestAvito", "");
        mainThread.start();

        return START_STICKY;
    }

    public void onDestroy() {
        requestAuto=null;
        requestAvito=null;
        super.onDestroy();
    }
    public IBinder onBind(Intent intent) {
        return null;
    }


    void ServiceProcess() throws InterruptedException {
        for (int i = 1; i<=100; i++) {
            TimeUnit.SECONDS.sleep(120);
            if(requestAuto == null && requestAvito == null)
                return;
            SharedPreferences sPref = getSharedPreferences("SearchMyCarPreferences", Context.MODE_PRIVATE);
            lastCarId = sPref.getString("SearchMyCarLastCarDate","");
            Thread t = new Thread(new Runnable() {
                @TargetApi(Build.VERSION_CODES.HONEYCOMB)
                public void run() {
                    try {




                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            t.start();
            while (t.isAlive());
            if(Integer.parseInt(answer[0]) != 0) {
                sendNotification(answer[0]);
            }
        }
    }

    void sendNotification(String countOfNewCars) {

        Notification notif = new Notification(R.drawable.status_bar, "Новое авто!",
                System.currentTimeMillis());

        Intent intent = new Intent(this, NotificationActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

        SharedPreferences sPref = getSharedPreferences("SearchMyCarPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putInt("SearchMyCarCountOfNewCars", Integer.parseInt(countOfNewCars));
        ed.commit();

        if(countOfNewCars == "1")
            notif.setLatestEventInfo(this, "SearchMyAuto", "Найдено "+countOfNewCars+" новый авто", pIntent);
        else
            notif.setLatestEventInfo(this, "SearchMyAuto", "Найдено "+countOfNewCars+" новых авто", pIntent);
        Uri ringURI = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        notif.number = Integer.parseInt(countOfNewCars);
        notif.sound = ringURI;
        notif.flags |= Notification.FLAG_AUTO_CANCEL | Notification.DEFAULT_SOUND | Notification.FLAG_ONLY_ALERT_ONCE;
        nm.notify(1, notif);
    }
}
