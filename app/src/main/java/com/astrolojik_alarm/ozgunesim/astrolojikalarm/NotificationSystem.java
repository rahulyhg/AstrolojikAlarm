package com.astrolojik_alarm.ozgunesim.astrolojikalarm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

/**
 * Created by ozgun on 25.4.2016.
 */
public class NotificationSystem {

    public static void checkNotifications(Context c){
        AlarmDB db = new AlarmDB(c);
        Boolean isSet = false;
        for(int i=0; i<db.GetCount(); i++){
            if(db.GetAlarmbyPosition(i).alarmProp.isActive){
                isSet = true;
                break;
            }
        }
        if(isSet){
            final Intent emptyIntent = new Intent();
            PendingIntent pendingIntent = PendingIntent.getActivity(c, 0, emptyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Bitmap lIcon = BitmapFactory.decodeResource(c.getResources(), R.mipmap.ic_launcher);

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(c)
                            .setSmallIcon(R.drawable.ic_notification)
                            .setLargeIcon(lIcon)
                            .setTicker("Alarm Kuruldu.")
                            .setContentTitle("Alarm Aktif.")
                            .setOngoing(true)
                            .setWhen(0)
                            .setContentText("Astrolojik Alarm")
                            .setContentIntent(pendingIntent); //Required on Gingerbread and below

            NotificationManager notificationManager = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(0, mBuilder.build());
        }else{
            NotificationManager notificationManager = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(0);
        }
    }
}
