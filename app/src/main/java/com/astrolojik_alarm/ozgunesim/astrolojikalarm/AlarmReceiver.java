package com.astrolojik_alarm.ozgunesim.astrolojikalarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

/**
 * Created by ozgun on 3.4.2016.
 */
public class AlarmReceiver extends BroadcastReceiver{

    MediaPlayer player;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Bundle bundle = intent.getExtras();
        String message = bundle.getString("alarm_message");
        Uri snd = (Uri)bundle.get("snd_uri");
        int _id = (int)bundle.get("alarm_id");

        Intent newIntent = new Intent(context, WakeScreenActivity.class);
        newIntent.putExtra("alarm_message", message);
        newIntent.putExtra("alarm_sound", snd);
        newIntent.putExtra("alarm_id",_id);
        newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(newIntent);

        //Toast.makeText(context, message + intent.getAction(), Toast.LENGTH_LONG).show();
    }
}
