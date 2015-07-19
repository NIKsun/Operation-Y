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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

public class MonitoringService extends Service {
    NotificationManager nm;
    String request;
    String lastCarId;
    byte[] reqByte;

    Thread t = new Thread(new Runnable() {
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
        request = sPref.getString("SearchMyCarRequestService", "");
        t.start();

        return START_STICKY;
    }

    public void onDestroy() {
        request=null;
        super.onDestroy();
        Log.d("BugWithService:ServiceDestroy", "ServiceDestroy");
    }
    public IBinder onBind(Intent intent) {
        return null;
    }


    void ServiceProcess() throws InterruptedException {
        for (int i = 1; i<=100; i++) {
            TimeUnit.SECONDS.sleep(10);
            if(request == null)
                return;
            SharedPreferences sPref = getSharedPreferences("SearchMyCarPreferences", Context.MODE_PRIVATE);
            lastCarId = sPref.getString("SearchMyCarLastCarID","");
            final String complexRequest =  request + "@@@" + lastCarId;
            final String[] answer = {""};            
            Thread t = new Thread(new Runnable() {
                @TargetApi(Build.VERSION_CODES.HONEYCOMB)
                public void run() {
                    try {
                        Socket soc = new Socket();
                        soc.bind(null);
                        soc.connect(new InetSocketAddress(InetAddress.getByName("193.124.59.57"), 11111));
                        soc.setKeepAlive(true);
                        soc.getOutputStream().write(complexRequest.getBytes());
                        int r = 0;
                        byte[] buf = new byte[64 * 1024];
                        do {
                            r = soc.getInputStream().read(buf);
                            answer[0] += new String(buf, 0, r);
                        }
                        while (r != 0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            t.start();
            while (t.isAlive())
                continue;
            Log.d("BugWithService:ServiceProcess2",answer[0]);
            if(Integer.parseInt(answer[0]) != 0) {
                sendNotification(answer[0]);
            }
        }
    }

    void sendNotification(String countOfNewCars) {

        Notification notif = new Notification(R.drawable.status_bar, "Новое авто!",
                System.currentTimeMillis());
        Intent intent = new Intent(this, ListOfCars.class);
        intent.putExtra("ServiceRequest", request);
        Log.d("BugWithService:sendNotification", request);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

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
