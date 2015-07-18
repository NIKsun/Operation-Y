package com.example.searchmycarandroid;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

public class MonitoringService extends Service {
    NotificationManager nm;



    public void onCreate() {
        super.onCreate();
        nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
    }
    public int onStartCommand(Intent intent, int flags, int startId) {

        SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(this);
        final String savedText = sPref.getString("SearchMyCarRequest", "");
        Thread t = new Thread(new Runnable() {
            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            public void run() {
                try {
                    someTask(savedText);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
        return START_STICKY;
    }
    public void onDestroy() {
        super.onDestroy();
    }
    public IBinder onBind(Intent intent) {
        return null;
    }


    void someTask(String request) throws InterruptedException {
        for (int i = 1; i<=100; i++) {
            TimeUnit.SECONDS.sleep(60);
            SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(this);
            final String lastCarId = sPref.getString("SearchMyCarLastCarID", "");
            request += "@@@" + lastCarId;
            final String[] answer = {""};
            final byte[] reqByte = request.getBytes();
            Thread t = new Thread(new Runnable() {
                @TargetApi(Build.VERSION_CODES.HONEYCOMB)
                public void run() {
                    try {
                        Socket soc = new Socket();
                        soc.bind(null);
                        soc.connect(new InetSocketAddress(InetAddress.getByName("192.168.43.238"), 11111));
                        soc.setKeepAlive(true);
                        soc.getOutputStream().write(reqByte);
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
            if(Integer.parseInt(answer[0]) != 0)
                sendNotification(answer[0]);
        }
    }

    void sendNotification(String countOfNewCars) {

        Notification notif = new Notification(R.drawable.status_bar, "New auto!",
                System.currentTimeMillis());


        Intent intent = new Intent(this, ListOfCars.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

        notif.setLatestEventInfo(this, "SearchMyAuto", "Find "+countOfNewCars+" new autos", pIntent);
        Uri ringURI = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        notif.sound = ringURI;
        notif.flags |= Notification.FLAG_AUTO_CANCEL | Notification.DEFAULT_SOUND | Notification.FLAG_ONLY_ALERT_ONCE;
        nm.notify(1, notif);
    }
}
