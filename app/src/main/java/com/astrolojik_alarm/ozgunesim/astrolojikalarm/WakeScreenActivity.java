package com.astrolojik_alarm.ozgunesim.astrolojikalarm;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

public class WakeScreenActivity extends AppCompatActivity {


    MediaPlayer player;
    String message;
    Uri snd;
    SeekBar sClose, sSnooze;
    TextView txtTime;
    Alarm alarm;
    AlarmDB db;
    Boolean horoOpened = false;


    public void closeAlarm(){
        if(player != null)
            player.stop();
        if(CustomListAdaptor.currentDialog != null)
            CustomListAdaptor.currentDialog.dismiss();
        finish();

    }

    public void clearAlarm(){
        Alarm.RemoveAlarm(alarm.alarmProp.ID, WakeScreenActivity.this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_wake_screen);

        if(MainActivity.mainActivity != null)
            MainActivity.mainActivity.finish();

        Bundle b = getIntent().getExtras();
        alarm = new Alarm();
        AlarmDB db = new AlarmDB(WakeScreenActivity.this);

        alarm = db.GetAlarmItem((int)b.get("alarm_id"));
        snd = alarm.alarmProp.soundUri; //(Uri)b.get("alarm_sound");
        message = alarm.alarmProp.message; //(String)b.get("alarm_message");
        //alarmID = alarm.alarmProp.ID; //(int)b.get("alarm_id");
        player = MediaPlayer.create(this, snd);
        player.setLooping(true);

        if(player != null)
            player.start();
/*
        PullComments pc = new PullComments(this);
        pc.pull();
*/
        sClose = (SeekBar)findViewById(R.id.seekBar_close);
        sSnooze = (SeekBar)findViewById(R.id.seekBar_snooze);
        txtTime = (TextView)findViewById(R.id.txtTime);

        if(alarm.alarmProp.snooze == 0)
            sSnooze.setVisibility(View.INVISIBLE);

        Typeface typeFace= Typeface.createFromAsset(getAssets(), "fonts/digit.ttf");
        txtTime.setTypeface(typeFace);

        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int min = cal.get(Calendar.MINUTE);
        String _h = String.valueOf(hour);
        String _m = String.valueOf(min);
        if(hour < 10)
            _h = "0" + _h;
        if(min < 10)
            _m = "0" + _m;

        txtTime.setText(_h + ":" + _m);

        if(!alarm.alarmProp.isRepeating){
            if(alarm.alarmProp.days.size() > 0){
                for(int i=0; i<alarm.alarmProp.days.size(); i++){
                    if(cal.get(Calendar.DAY_OF_WEEK) == alarm.alarmProp.days.get(i))
                        alarm.alarmProp.days.remove(i);
                }
                if(alarm.alarmProp.days.size() > 0){
                    db.SetAlarmItem(alarm.alarmProp.ID,alarm);
                    Alarm.SetAlarm(alarm,WakeScreenActivity.this, Alarm.FLAG_NEXT_ALARM, false);
                }else{
                    if(alarm.alarmProp.snooze == 0)
                        db.RemoveAlarmItem(alarm.alarmProp.ID);
                }
            }else{
                if(alarm.alarmProp.snooze == 0)
                    db.RemoveAlarmItem(alarm.alarmProp.ID);
            }
        }else{
            Alarm.SetAlarm(alarm,WakeScreenActivity.this, Alarm.FLAG_NEXT_ALARM, false);
        }

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "AstrolojikAlarm");
        wl.acquire();
        wl.release();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);


        if(db.GetCount() == 0){
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(0);
        }


        final Handler h = new Handler();
        h.postDelayed(new Runnable()
        {
            private long time = 0;

            @Override
            public void run()
            {
                closeAlarm();
                //h.postDelayed(this, 1000);
            }
        }, 120000);


        sClose.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress >= 90) {
                    if (!horoOpened) {
                        closeAlarm();
                        if (!alarm.alarmProp.isRepeating && alarm.alarmProp.days.size() == 0) {
                            Alarm.RemoveAlarm(alarm.alarmProp.ID, WakeScreenActivity.this);
                            AlarmDB db = new AlarmDB(WakeScreenActivity.this);
                            db.RemoveAlarmItem(alarm.alarmProp.ID);

                            if (alarm.alarmProp.days.size() > 0) {
                                Calendar cal = Calendar.getInstance();
                                for (int i = 0; i < alarm.alarmProp.days.size(); i++) {
                                    if (cal.get(Calendar.DAY_OF_WEEK) == alarm.alarmProp.days.get(i))
                                        alarm.alarmProp.days.remove(i);
                                }
                            }

                        }
                        NotificationSystem.checkNotifications(WakeScreenActivity.this);

                        Intent intent = new Intent(WakeScreenActivity.this, HoroViewActivity.class);
                        startActivity(intent);
                        horoOpened = true;
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (seekBar.getProgress() < 90)
                    seekBar.setProgress(0);
            }
        });

        sSnooze.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(progress>=90) {
                    closeAlarm();
                    AlarmDB db = new AlarmDB(WakeScreenActivity.this);
                    alarm.addSnoozeTime();
                    db.SetAlarmItem(alarm.alarmProp.ID, alarm);
                    AlarmListActivity.adaptor.notifyDataSetChanged();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (seekBar.getProgress() < 90)
                    seekBar.setProgress(0);
            }
        });


    }

}
