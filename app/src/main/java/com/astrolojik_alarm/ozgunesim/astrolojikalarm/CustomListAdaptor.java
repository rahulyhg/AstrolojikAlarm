package com.astrolojik_alarm.ozgunesim.astrolojikalarm;

import android.app.Activity;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.media.Image;
import android.support.annotation.DrawableRes;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by ozgun on 6.3.2016.
 */
public class CustomListAdaptor extends BaseAdapter {
    private LayoutInflater mInflater;
    private Context c;
    public static Dialog currentDialog;
    private Alarm alarm;
    Button btnActive;

    AlarmDB db;

    public CustomListAdaptor(Activity activity){
        mInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        db = new AlarmDB(activity);
    }


    @Override
    public int getCount() {
        if(db != null)
            return db.GetCount();
        else
            return 0;
    }

    public void setContext(Context _c){
        c = _c;
    }

    @Override
    public Alarm getItem(int position) {
        return db.GetAlarmbyPosition(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View rowView;

        rowView = mInflater.inflate(R.layout.row_layout, null);
        TextView txtTime = (TextView) rowView.findViewById(R.id.time);

        alarm = db.GetAlarmbyPosition(position);

        Button delBtn = (Button)rowView.findViewById(R.id.listDel);
        btnActive = (Button) rowView.findViewById(R.id.listActive);

        if(alarm.alarmProp.isActive){
            btnActive.setBackgroundResource(R.drawable.alarm_on);
        }else{
            btnActive.setBackgroundResource(R.drawable.alarm_off);
        }

        btnActive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alarm.alarmProp.isActive = !alarm.alarmProp.isActive;
                if (alarm.alarmProp.isActive) {
                    Alarm.SetAlarm(alarm, c, Alarm.FLAG_UPDATE_ALARM, true);
                } else {
                    Alarm.RemoveAlarm(alarm.alarmProp.ID, c);
                }
                db.SetAlarmItem(db.GetAlarmbyPosition(position).alarmProp.ID, alarm);
                checkIsActive();
            }
        });

        delBtn.setTag(position);
        delBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = Integer.parseInt(((Button) v).getTag().toString());
                showAlert(pos);
            }
        });

        final ImageView iv = (ImageView)rowView.findViewById(R.id.bgView);
        iv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP)
                    iv.setAlpha(1f);
                else if (event.getAction() == MotionEvent.ACTION_DOWN)
                    iv.setAlpha(0.8f);
                else if (event.getAction() == MotionEvent.ACTION_MOVE)
                    iv.setAlpha(1f);

                return false;
            }
        });

        if(!alarm.alarmProp.isSnoozed)
            txtTime.setText(alarm.getTime());
        else
            txtTime.setText(alarm.alarmProp.snoozeTime);

        final ImageView repeatIcon = (ImageView)rowView.findViewById(R.id.imgRepeat);
        if(!alarm.alarmProp.isRepeating) {
            repeatIcon.setVisibility(View.GONE);
        }
        else {
            repeatIcon.setVisibility(View.VISIBLE);
        }

        final ImageView snoozeIcon = (ImageView)rowView.findViewById(R.id.imgSnooze);
        if(alarm.alarmProp.snooze == 0) {
            snoozeIcon.setVisibility(View.GONE);
        }
        else {
            snoozeIcon.setVisibility(View.VISIBLE);
        }

        TextView lblPzt,lblSal,lblCrs,lblPrs,lblCum,lblCts,lblPaz;
        lblPzt = (TextView)rowView.findViewById(R.id.lblPzt);
        lblSal = (TextView)rowView.findViewById(R.id.lblSal);
        lblCrs = (TextView)rowView.findViewById(R.id.lblCrs);
        lblPrs = (TextView)rowView.findViewById(R.id.lblPrs);
        lblCum = (TextView)rowView.findViewById(R.id.lblCum);
        lblCts = (TextView)rowView.findViewById(R.id.lblCts);
        lblPaz = (TextView)rowView.findViewById(R.id.lblPaz);

        if(alarm.alarmProp.days.contains(1))
            lblPaz.setTextColor(Color.rgb(0,255,0));
        if(alarm.alarmProp.days.contains(2))
            lblPzt.setTextColor(Color.rgb(0,255,0));
        if(alarm.alarmProp.days.contains(3))
            lblSal.setTextColor(Color.rgb(0,255,0));
        if(alarm.alarmProp.days.contains(4))
            lblCrs.setTextColor(Color.rgb(0,255,0));
        if(alarm.alarmProp.days.contains(5))
            lblPrs.setTextColor(Color.rgb(0,255,0));
        if(alarm.alarmProp.days.contains(6))
            lblCum.setTextColor(Color.rgb(0,255,0));
        if(alarm.alarmProp.days.contains(7))
            lblCts.setTextColor(Color.rgb(0,255,0));


        return rowView;
    }

    private void checkIsActive(){
        if(alarm.alarmProp.isActive){
            btnActive.setBackgroundResource(R.drawable.alarm_on);
        }else{
            btnActive.setBackgroundResource(R.drawable.alarm_off);
        }
        NotificationSystem.checkNotifications(c);
    }


    private void showAlert(final int pos) {
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle("Silmek İstediğinize Emin Misiniz?");
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                AlarmDB db = new AlarmDB(c);
                Alarm.RemoveAlarm(db.GetAlarmbyPosition(pos).alarmProp.ID, c);
                db.RemoveAlarmbyPosition(pos);
                notifyDataSetChanged();

                NotificationSystem.checkNotifications(c);

                dialog.dismiss();
                currentDialog = null;
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //TODO
                dialog.dismiss();
                currentDialog = null;
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        currentDialog = dialog;
    }





}
