package com.astrolojik_alarm.ozgunesim.astrolojikalarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.BoringLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

/**
 * Created by ozgun on 5.3.2016.
 */
class Properties{
    int ID;
    List<Integer> days = new ArrayList<Integer>();
    boolean isRepeating, isActive;
    int hour;
    int minute;
    Uri soundUri;
    int volume;
    int snooze;
    String snoozeTime = "";
    Boolean isSnoozed = false;
    int snoozeHour,snoozeMinute;
    String message;
    AlarmManager alarmManager;
    PendingIntent pendingIntent;
}



public class Alarm {
    public Properties alarmProp = new Properties();
    public static AlarmDB db;

    public static void RemoveAlarm(int id, Context c){
        AlarmManager alarmManager = (AlarmManager)c.getSystemService(c.ALARM_SERVICE);
        Intent myIntent = new Intent(c, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(c,
                id, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        pendingIntent.cancel();
        alarmManager.cancel(pendingIntent);
    }

    //private PendingIntent pendingIntent;

    public String getTime(){
        String h, m;
        h = Integer.toString(alarmProp.hour);
        m = Integer.toString(alarmProp.minute);

        if(alarmProp.hour < 9){
            h = "0" + h;
        }
        if(alarmProp.minute < 9){
            m = "0" + m;
        }

        return h+":"+m;
    }

    public static final int FLAG_NEW_ALARM = 0;
    public static final int FLAG_UPDATE_ALARM = 1;
    public static final int FLAG_REPEAT_ALARM = 2;
    public static final int FLAG_NEXT_ALARM = 3;
    static int alarmFlag;

    public static Boolean SetAlarm(Alarm a, Context c, int flag, Boolean showDiff){

        db = new AlarmDB(c);

        Calendar calendar = Calendar.getInstance();

        calendar.setTimeInMillis(System.currentTimeMillis());

        calendar.set(calendar.HOUR_OF_DAY, a.alarmProp.hour);
        calendar.set(calendar.MINUTE, a.alarmProp.minute);
        calendar.set(calendar.SECOND, 0);

        List<Integer> AlarmDays = a.alarmProp.days;
        if(AlarmDays.size() > 0){
            int currentDay = calendar.get(Calendar.DAY_OF_WEEK);
            Boolean isInWeek = false;
            int index = 0;
            for(int i=currentDay; i<8; i++){
                if(AlarmDays.contains(i)){
                    if(i == currentDay){
                        long alarmDate = calendar.getTimeInMillis();
                        long now = Calendar.getInstance().getTimeInMillis();
                        int _h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                        int _m = Calendar.getInstance().get(Calendar.MINUTE);
                        if(alarmDate<now || (calendar.get(Calendar.HOUR_OF_DAY) == _h && calendar.get(Calendar.MINUTE) == _m)){
                            if(AlarmDays.size() == 1) {
                                calendar.add(Calendar.DATE, 7);
                            }
                            else{
                                index++;
                                continue;
                            }
                        }
                    }
                    isInWeek = true;
                    calendar.add(calendar.DATE,index);
                    break;
                }
                index++;
            }
            if(!isInWeek){
                for(int i=1; i<currentDay; i++){
                    if(AlarmDays.contains(i)){
                        int addDay = ((7 - currentDay)+(i));
                        calendar.add(calendar.DATE,addDay);
                        break;
                    }
                }
            }
        }else{
            long alarmDate = calendar.getTimeInMillis();
            long now = Calendar.getInstance().getTimeInMillis();
            int _h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            int _m = Calendar.getInstance().get(Calendar.MINUTE);

            if(a.alarmProp.isRepeating){
                if(a.alarmProp.days.size() == 0){
                    a.alarmProp.days.add(calendar.get(Calendar.DAY_OF_WEEK));
                }
            }

            if(alarmDate<now || (calendar.get(Calendar.HOUR_OF_DAY) == _h && calendar.get(Calendar.MINUTE) == _m)){
                calendar.add(Calendar.DATE, 7);
            }
        }



        if(showDiff)
            printDateDifference(calendar.getTimeInMillis(), c);


        Intent myIntent = new Intent(c, AlarmReceiver.class);
        myIntent.putExtra("alarm_message", a.alarmProp.message);
        myIntent.putExtra("snd_uri", a.alarmProp.soundUri);
        myIntent.putExtra("alarm_id", a.alarmProp.ID);

        /*if(flag == FLAG_NEW_ALARM)
            alarmFlag = 0;*/
        if(flag == FLAG_UPDATE_ALARM || flag == FLAG_NEXT_ALARM)
            RemoveAlarm(a.alarmProp.ID,c);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(c, a.alarmProp.ID, myIntent, 0);

        AlarmManager alarmManager = (AlarmManager)c.getSystemService(c.ALARM_SERVICE);
        if(a.alarmProp.snooze>0)
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), (1000*60*a.alarmProp.snooze), pendingIntent);
        else
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

        if(flag == FLAG_NEW_ALARM){
            a.alarmProp.isActive = true;
            db.AddAlarmItem(a); //MainActivity.AlarmList.add(a);
        }else if(flag == FLAG_UPDATE_ALARM){
            for(int i=0; i<db.GetCount(); i++){
                if(db.GetAlarmbyPosition(i).alarmProp.ID  == a.alarmProp.ID) {
                    db.SetAlarmItem(a.alarmProp.ID, a);
                    break;
                }
            }
        }
        //db.SaveAlarms();

        return true;
    }

    static void printDateDifference(long date,Context c){
        long diff = date - Calendar.getInstance().getTimeInMillis();
        long days = diff / (24 * 60 * 60 * 1000);
        long hours = (diff / (60 * 60 * 1000));
        long minutes = (diff / (60 * 1000));
        if(hours > 0 )
            hours = hours % 24;
        if(minutes > 0)
            minutes = minutes % 60;

        Toast.makeText(c,
                "Kalan Süre: " + String.valueOf(days) + " gün, " +
                        String.valueOf(hours) + " saat " +
                        String.valueOf(minutes) + " dakika."
                , Toast.LENGTH_LONG).show();
    }

    public void addSnoozeTime(){
        int hour = alarmProp.hour;
        int minute = alarmProp.minute;

        minute += alarmProp.snooze;
        if(minute > 59){
            minute = minute % 60;
            hour++;
        }

        alarmProp.snoozeHour = hour;
        alarmProp.snoozeMinute = minute;
        alarmProp.isSnoozed = true;

        String h = String.valueOf(hour);
        String m = String.valueOf(minute);

        if(hour < 9){
            h = "0" + h;
        }
        if(minute < 9){
            m = "0" + m;
        }
        alarmProp.snoozeTime =  h+":"+m;

    }

}
