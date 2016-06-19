package com.astrolojik_alarm.ozgunesim.astrolojikalarm;

import android.app.AlarmManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

/**
 * Created by ozgun on 15.4.2016.
 */
public class AlarmDB {
    private String SaveString,LoadString;
    String[] temp,_days;
    public static List<Alarm> AlarmList;
    Context c;


    public AlarmDB(Context _c){
        c = _c;
        AlarmList = LoadAlarms();
    }

    public void RemoveAlarmItem(int _id){
        for(int i=0; i<AlarmList.size(); i++){
            if(AlarmList.get(i).alarmProp.ID == _id) {
                AlarmList.remove(i);
            }
        }
        SaveAlarms();
    }

    public Alarm GetAlarmItem(int _id){
        for(int i=0; i<AlarmList.size(); i++){
            if(AlarmList.get(i).alarmProp.ID == _id) {
                return AlarmList.get(i);
            }
        }
        return null;
    }

    public Alarm GetAlarmbyPosition(int pos){
        return AlarmList.get(pos);
    }

    public void RemoveAlarmbyPosition(int pos){
        AlarmList.remove(pos);
        SaveAlarms();
    }

    public void SetAlarmItem(int _id, Alarm a){
        for(int i=0; i<AlarmList.size(); i++){
            if(AlarmList.get(i).alarmProp.ID == _id) {
                AlarmList.set(i,a);
            }
        }
        SaveAlarms();
    }

    public void AddAlarmItem(Alarm a){
        AlarmList.add(a);
        SaveAlarms();
    }

    public int GetCount(){
        return AlarmList.size();
    }

    public List<Alarm> GetList(){
        return AlarmList;
    }

    public void SaveAlarms(){
        AlarmDB.AlarmList = AlarmList;
        SaveString = "";
        for (int i=0; i<AlarmList.size(); i++){
            Alarm a = AlarmList.get(i);
            SaveString += String.valueOf(a.alarmProp.ID) + ",";
            SaveString += String.valueOf(a.alarmProp.hour) + ",";
            SaveString += String.valueOf(a.alarmProp.minute) + ",";
            SaveString += a.alarmProp.message + ",";
            SaveString += a.alarmProp.soundUri.toString() + ",";
            SaveString += String.valueOf(a.alarmProp.volume) + ",";
            SaveString += (a.alarmProp.isRepeating) ? "1," : "0,";
            SaveString += String.valueOf(a.alarmProp.snooze) + ",";
            SaveString += (a.alarmProp.isActive) ? "1" : "0";

            if(a.alarmProp.days.size()>0)
                SaveString += ",";
            for(int j=0; j<a.alarmProp.days.size(); j++){
                String day = String.valueOf(a.alarmProp.days.get(j));
                SaveString += day;
                if(j != a.alarmProp.days.size()-1)
                    SaveString += ":";
            }
            if(i != AlarmList.size()-1)
                SaveString += ";";
        }

        SharedPreferences preferences;
        SharedPreferences.Editor editor;
        preferences= PreferenceManager.getDefaultSharedPreferences(c);
        editor = preferences.edit();
        editor.putString("AstroAlarmList", SaveString);
        editor.commit();

        LoadAlarms();
    }


    List<Alarm> returnList = new ArrayList<Alarm>();

    public List<Alarm> LoadAlarms(){
        SharedPreferences preferences;
        SharedPreferences.Editor editor;
        preferences= PreferenceManager.getDefaultSharedPreferences(c);

        LoadString = preferences.getString("AstroAlarmList","bos");
        if(LoadString == "bos")
            return new ArrayList<Alarm>();

        returnList.clear();
        String[] alarms = LoadString.split(";");
        for(int i=0; i< alarms.length; i++){
            if(alarms[i] != null && alarms[i] != ""){
                temp = null;
                temp = alarms[i].split(",");
                Alarm alarm = new Alarm();
                alarm.alarmProp.ID = Integer.parseInt(temp[0]);
                alarm.alarmProp.hour = Integer.parseInt(temp[1]);
                alarm.alarmProp.minute = Integer.parseInt(temp[2]);
                alarm.alarmProp.message = temp[3];
                alarm.alarmProp.soundUri = Uri.parse(temp[4]);
                alarm.alarmProp.volume = Integer.parseInt(temp[5]);
                alarm.alarmProp.isRepeating = (temp[6].equals("1")) ? true : false;
                alarm.alarmProp.snooze = Integer.parseInt(temp[7]);
                alarm.alarmProp.isActive = (temp[8].equals("1")) ? true : false;
                if(temp.length>9){
                    List<Integer> returnDays = new ArrayList<Integer>();
                    _days = null;
                    _days = temp[9].split(":");
                    for(int j=0; j<_days.length; j++){
                        returnDays.add(Integer.parseInt(_days[j]));
                    }
                    alarm.alarmProp.days = returnDays;
                }
                returnList.add(alarm);
            }

        }
        AlarmList = returnList;
        return returnList;
    }

    public void SaveLastSound(Uri u){
        SharedPreferences preferences;
        SharedPreferences.Editor editor;
        preferences= PreferenceManager.getDefaultSharedPreferences(c);
        editor = preferences.edit();
        editor.putString("AstroAlarmSound" , u.toString());
        editor.commit();
    }

    public String GetLastSound(){
        SharedPreferences preferences;
        SharedPreferences.Editor editor;
        preferences= PreferenceManager.getDefaultSharedPreferences(c);
        editor = preferences.edit();
        String sndString = preferences.getString("AstroAlarmSound","bos");
        return sndString;
    }


    public void SaveLastHoroComment(String comment){
        SharedPreferences preferences;
        SharedPreferences.Editor editor;
        preferences= PreferenceManager.getDefaultSharedPreferences(c);
        editor = preferences.edit();
        editor.putString("AstroAlarmComment" , comment);
        editor.commit();
    }

    public String GetLastHoroComment(){
        SharedPreferences preferences;
        SharedPreferences.Editor editor;
        preferences= PreferenceManager.getDefaultSharedPreferences(c);
        editor = preferences.edit();
        String sndString = preferences.getString("AstroAlarmComment","bos");
        return sndString;
    }





}
