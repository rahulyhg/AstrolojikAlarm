package com.astrolojik_alarm.ozgunesim.astrolojikalarm;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ozgun on 24.4.2016.
 */
public class Horoscope {

    public class HoroscopeInfo{
        public int id;
        public String name;
        public String startDate;
        public String endDate;
        public String longStartDate;
        public String longEndDate;
    }

    private Context c;
    private List<HoroscopeInfo> HoroscopeList = new ArrayList<>();
    private HoroscopeInfo userHoroscope;
    private Boolean isset = false;
    private String[] mounthArray;
    public static Toolbar toolbar = null;

    private void LoadHoroscopeList(){
        mounthArray = c.getResources().getStringArray(R.array.mouthArray);
        String[] hArray = c.getResources().getStringArray(R.array.horoArray);
        for(int i=0; i<hArray.length; i++){
            String[] temp = hArray[i].split(";");
            if(temp.length<4)
                continue;
            HoroscopeInfo inf = new HoroscopeInfo();
            inf.id = Integer.parseInt(temp[0]);
            inf.name = temp[1];
            inf.startDate = temp[2];
            inf.endDate = temp[3];
            String _sDate[] = inf.startDate.split("\\.");
            String _eDate[] = inf.endDate.split("\\.");
            int sID = Integer.parseInt(_sDate[1]);
            int eID = Integer.parseInt(_eDate[1]);
            inf.longStartDate = _sDate[0] + " " + mounthArray[sID-1];
            inf.longEndDate = _eDate[0] + " " + mounthArray[eID-1];
            HoroscopeList.add(inf);
        }
    }

    public Horoscope(Context _c){
        c = _c;
        LoadHoroscopeList();
        checkUser();
        }

    public Horoscope(Context _c, Toolbar t){
        c = _c;
        toolbar = t;
        LoadHoroscopeList();
        checkUser();
    }

    public void checkUser(){
        SharedPreferences preferences;
        SharedPreferences.Editor editor;
        preferences= PreferenceManager.getDefaultSharedPreferences(c);
        String spHoroscope = preferences.getString("AstroAlarmHoroscope", "bos");
        if(spHoroscope.equals("bos")){
            isset = false;
        }else{
            for(int i=0; i<HoroscopeList.size(); i++){
                if(HoroscopeList.get(i).id == Integer.parseInt(spHoroscope)){
                    userHoroscope = HoroscopeList.get(i);
                    if(toolbar != null)
                        toolbar.setSubtitle("Burcunuz: " + userHoroscope.name);
                }
            }
            isset = true;
        }
    }

    public Boolean isSet(){
        return isset;
    }

    public HoroscopeInfo getUserHoroscope(){
        return userHoroscope;
    }

    public void selectHoroscope(){
        CharSequence[] selectionList = new CharSequence[HoroscopeList.size()];
        for (int i=0; i<HoroscopeList.size(); i++){
            selectionList[i] = HoroscopeList.get(i).name + " ( " +
            HoroscopeList.get(i).longStartDate + " - " + HoroscopeList.get(i).longEndDate + " )";
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setCancelable(false);
        builder.setTitle("Lütfen Burcunuzu Seçin:");
        builder.setItems(selectionList, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences preferences;
                SharedPreferences.Editor editor;
                preferences= PreferenceManager.getDefaultSharedPreferences(c);
                editor = preferences.edit();
                editor.putString("AstroAlarmHoroscope", String.valueOf(HoroscopeList.get(which).id));
                editor.commit();
                checkUser();

            }
        });
        builder.show();
    }
}


