package com.astrolojik_alarm.ozgunesim.astrolojikalarm;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.support.v7.widget.Toolbar;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    TimePicker tp;
    Button sndBtn;
    Uri alarmSound;
    Ringtone ringTone;
    RingtoneManager rManager;
    SeekBar volBar;
    AudioManager aManager;
    int volume;
    MediaPlayer player;
    boolean repeatWeekly = false;
    Button setBtn;
    List<Integer> AlarmDays = new ArrayList<Integer>();
    CheckBox chRepeat;
    Button hUp,hDown,mUp,mDown;
    EditText txtH,txtM,txtMsg;
    int hour, minute;
    Spinner spnSnooze;
    CheckBox chkSnooze;
    LinearLayout lytSnooze;
    public static PendingIntent pendingIntent;
    public static AlarmManager alarmManager;
    //public static List<Alarm> AlarmList = new ArrayList<Alarm>();
    int currentDay;
    Calendar calendar;
    ALARM_TYPE AlarmType;
    Alarm SelectedAlarm = null;
    AlarmDB db;
    String openValue;
    public static Activity mainActivity;
    Toolbar toolbar;
    Horoscope h;


    public enum ALARM_TYPE{
        NEW,
        UPDATE
    }



    ToggleButton tgPzt,tgSal,tgCrs,tgPrs,tgCum,tgCts,tgPzr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainActivity = this;

        db = new AlarmDB(MainActivity.this);

        Intent intent = getIntent();
        openValue = intent.getStringExtra("key");
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setLogo(R.mipmap.ic_launcher);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        h = new Horoscope(this,toolbar);

        /*
        View v = findViewById(R.id.addAlarm);
        v.setVisibility(View.INVISIBLE);
        */

        //tp = (TimePicker)findViewById(R.id.timePicker);
        sndBtn = (Button)findViewById(R.id.sndBtn);
        volBar = (SeekBar)findViewById(R.id.seekVol);
        setBtn = (Button)findViewById(R.id.btnSet);
        chRepeat = (CheckBox)findViewById(R.id.chRepeat);
        txtMsg = (EditText)findViewById(R.id.txtMessage);
        aManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        volBar.setMax(aManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        volBar.setProgress(aManager.getStreamVolume(AudioManager.STREAM_MUSIC));
        volBar.setEnabled(false);


        txtH = (EditText)findViewById(R.id.txtHour);
        txtM = (EditText)findViewById(R.id.txtMin);

        Calendar c = Calendar.getInstance();
        int _hour = c.get(Calendar.HOUR_OF_DAY);
        int _minute = c.get(Calendar.MINUTE);
        hour = _hour;
        minute = _minute;
        txtH.setText(formatTime(_hour));
        txtM.setText(formatTime(_minute));

        tgPzt = (ToggleButton)findViewById(R.id.tglPzt);
        tgSal = (ToggleButton)findViewById(R.id.tglSal);
        tgCrs = (ToggleButton)findViewById(R.id.tglCrs);
        tgPrs = (ToggleButton)findViewById(R.id.tglPrs);
        tgCum = (ToggleButton)findViewById(R.id.tglCum);
        tgCts = (ToggleButton)findViewById(R.id.tglCts);
        tgPzr = (ToggleButton)findViewById(R.id.tglPzr);

        spnSnooze = (Spinner)findViewById(R.id.spnSnooze);
        spnSnooze.setEnabled(false);
        chkSnooze = (CheckBox)findViewById(R.id.chkSnooze);
        lytSnooze = (LinearLayout)findViewById(R.id.contSnooze);

        chkSnooze.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    spnSnooze.setEnabled(true);
                }else{
                    spnSnooze.setEnabled(false);
                }
            }
        });

        ArrayAdapter<CharSequence> snoozeAdapt = ArrayAdapter.createFromResource(
                MainActivity.this,
                R.array.snooze_array,
                R.layout.support_simple_spinner_dropdown_item
            );

        snoozeAdapt.setDropDownViewResource(R.layout.spinner_layout);
        spnSnooze.setAdapter(snoozeAdapt);


        bindToggles();
        setTimeUpdown();

        if(openValue.equals("new")){

            toolbar.setTitle(" Yeni Alarm Kur");
            AlarmType = ALARM_TYPE.NEW;
            String lastSnd = db.GetLastSound();
            if(!lastSnd.equals("bos")){
                alarmSound = Uri.parse(lastSnd);
                sndBtn.setBackgroundResource(R.drawable.snd_selected);
                volBar.setEnabled(true);

                AudioInfo inf = new AudioInfo(alarmSound,MainActivity.this);
                //Toast.makeText(getApplicationContext(), inf.getAllInfo(), Toast.LENGTH_LONG).show();
                AudioInfo.AudioData data = inf.getInfo();
                checkSndBtn(data);

            }
        }else {
            toolbar.setTitle(" Alarmı Düzenle");
            int listIndex = Integer.parseInt(openValue);
            SelectedAlarm = db.GetAlarmbyPosition(listIndex); //db.LoadAlarm(MainActivity.this).get(listIndex);
            AlarmType = ALARM_TYPE.UPDATE;
            fillControls(SelectedAlarm);
        }


        //tp.setIs24HourView(true);


        sndBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, 10);
            }
        });


        volBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                aManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                player = MediaPlayer.create(MainActivity.this, alarmSound);
                player.setLooping(true);
                player.start();
                //ringTone.play();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //ringTone.stop();
                player.stop();
            }
        });


        setBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(alarmSound != null){
                    Alarm alarm = new Alarm();
                    alarm.alarmProp.days = AlarmDays;
                    alarm.alarmProp.isRepeating = chRepeat.isChecked();
                    alarm.alarmProp.soundUri = alarmSound;
                    alarm.alarmProp.volume = volBar.getProgress();
                    alarm.alarmProp.message = txtMsg.getText().toString();
                    alarm.alarmProp.hour = hour;
                    alarm.alarmProp.minute = minute;
                    if(chkSnooze.isChecked()){
                        int snz = 0;
                        if(spnSnooze.getSelectedItemPosition() == 0) snz = 3;
                        else if(spnSnooze.getSelectedItemPosition() == 1) snz = 5;
                        else if(spnSnooze.getSelectedItemPosition() == 2) snz = 10;
                        else if(spnSnooze.getSelectedItemPosition() == 3) snz = 15;
                        else if(spnSnooze.getSelectedItemPosition() == 4) snz = 20;
                        else if(spnSnooze.getSelectedItemPosition() == 5) snz = 30;
                        alarm.alarmProp.snooze = snz;
                    }

                    if(AlarmType == ALARM_TYPE.NEW){
                        int _id = (int)System.currentTimeMillis();
                        alarm.alarmProp.ID = _id;
                        alarm.alarmProp.isActive = true;
                        Boolean isSet =  Alarm.SetAlarm(alarm, MainActivity.this, Alarm.FLAG_NEW_ALARM,true);
                        if(!isSet){
                            Toast.makeText(getApplicationContext(),"BEKLENMEYEN HATA",Toast.LENGTH_LONG).show();
                        }
                    }else if(AlarmType == ALARM_TYPE.UPDATE){
                        alarm.alarmProp.ID = SelectedAlarm.alarmProp.ID;
                        alarm.alarmProp.isActive = SelectedAlarm.alarmProp.isActive;
                        Boolean isSet =  Alarm.SetAlarm(alarm, MainActivity.this, Alarm.FLAG_UPDATE_ALARM, true);
                        if(!isSet){
                            Toast.makeText(getApplicationContext(),"BEKLENMEYEN HATA",Toast.LENGTH_LONG).show();
                        }
                    }
                    finish();

                }else{
                    Toast.makeText(MainActivity.this, "BİR ALARM SESİ SEÇMELİSİNİZ!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    void printDateDifference(long date){
        long diff = date - Calendar.getInstance().getTimeInMillis();
        long days = diff / (24 * 60 * 60 * 1000);
        long hours = (diff / (60 * 60 * 1000));
        long minutes = (diff / (60 * 1000));
        if(hours > 0 )
            hours = hours % 24;
        if(minutes > 0)
            minutes = minutes % 60;

        Toast.makeText(getApplicationContext(),
                "Kalan Süre: " + String.valueOf(days) + " gün, " +
                String.valueOf(hours) + " saat " +
                String.valueOf(minutes) + " dakika."
                , Toast.LENGTH_LONG).show();
    }

    Boolean daySelected(){
        if(AlarmDays.size()>0)
            return true;
        else
            return false;
    }

    void fillControls(Alarm a){
        hour = a.alarmProp.hour;
        minute = a.alarmProp.minute;
        txtH.setText(String.valueOf(a.alarmProp.hour));
        txtM.setText(String.valueOf(a.alarmProp.minute));
        controlTime();
        txtMsg.setText(String.valueOf(a.alarmProp.message));
        alarmSound = a.alarmProp.soundUri;
        //sndBtn.setBackgroundColor(getResources().getColor(R.color.sndGreen));
        sndBtn.setBackgroundResource(R.drawable.snd_selected);
        volBar.setProgress(a.alarmProp.volume);
        volBar.setEnabled(true);
        chRepeat.setChecked(a.alarmProp.isRepeating);
        for(int i=0; i<a.alarmProp.days.size(); i++){
            int _d = a.alarmProp.days.get(i);
            if(_d==1) {tgPzr.setChecked(true);dayToggle(tgPzr);} //tgPzr.setChecked(true);
            if(_d==2) {tgPzt.setChecked(true);dayToggle(tgPzt);} //tgPzt.setChecked(true);
            if(_d==3) {tgSal.setChecked(true);dayToggle(tgSal);} //tgSal.setChecked(true);
            if(_d==4) {tgCrs.setChecked(true);dayToggle(tgCrs);} //tgCrs.setChecked(true);
            if(_d==5) {tgPrs.setChecked(true);dayToggle(tgPrs);} //tgPrs.setChecked(true);
            if(_d==6) {tgCum.setChecked(true);dayToggle(tgCum);} //tgCum.setChecked(true);
            if(_d==7) {tgCts.setChecked(true);dayToggle(tgCts);} //tgCts.setChecked(true);
        }

        if(a.alarmProp.snooze > 0){
            chkSnooze.setChecked(true);
            int snz = a.alarmProp.snooze;
            if(snz == 3) spnSnooze.setSelection(0);
            else if(snz == 5) spnSnooze.setSelection(1);
            else if(snz == 10) spnSnooze.setSelection(2);
            else if(snz == 15) spnSnooze.setSelection(3);
            else if(snz == 20) spnSnooze.setSelection(4);
            else if(snz == 30) spnSnooze.setSelection(5);
            spnSnooze.setEnabled(true);
        }else{
            spnSnooze.setEnabled(false);
        }
    }

    void addDay(int d){
        AlarmDays.add(d);
    }

    void removeDay(int d){
        for(int i=0; i< AlarmDays.size(); i++){
            if(AlarmDays.get(i) == d){
                AlarmDays.remove(i);
            }
        }
    }

    void dayToggle(ToggleButton t){
        if(t.isChecked()){
            //t.setText("⦿" + t.getText());
            int day = Integer.parseInt(t.getTag().toString());
            addDay(day);
        }else{
            //t.setTextAppearance(getApplicationContext(), R.style.normalText);
            //t.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
            String s = t.getText().toString();
            s.replace("✅ ","");
            t.setText(s);
            int day = Integer.parseInt(t.getTag().toString());
            removeDay(day);
        }
    }

    void setTimeUpdown(){
        hUp = (Button)findViewById(R.id.hourUp);
        hDown = (Button)findViewById(R.id.hourDown);
        mUp = (Button)findViewById(R.id.minuteUp);
        mDown = (Button)findViewById(R.id.minuteDown);


        hUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hour++;
                controlTime();
            }
        });

        hDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hour--;
                controlTime();
            }
        });

        mUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                minute++;
                controlTime();
            }
        });

        mDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                minute--;
                controlTime();
            }
        });


        txtM.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String _str = txtM.getText().toString();
                    if(_str.equals(""))
                        txtM.setText("0");
                    minute = Integer.parseInt(txtM.getText().toString());
                    controlTime();
                }
            }
        });

        txtH.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    String _str = txtH.getText().toString();
                    if(_str.equals(""))
                        txtH.setText("0");
                    hour = Integer.parseInt(txtH.getText().toString());
                    controlTime();
                }
            }
        });

    }

    void controlTime(){
        hour = hour%24;
        minute = minute%60;

        if(hour<0)
            hour = 23;
        if(minute<0)
            minute = 59;

        txtH.setText(formatTime(hour));
        txtM.setText(formatTime(minute));
    }

    String formatTime(int time){
        if(time<10)
            return "0" + Integer.toString(time);
        else
            return Integer.toString(time);
    }



    void bindToggles(){
        tgPzt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dayToggle((ToggleButton)v);
            }
        });
        tgSal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dayToggle((ToggleButton)v);
            }
        });
        tgCrs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dayToggle((ToggleButton)v);
            }
        });
        tgPrs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dayToggle((ToggleButton)v);
            }
        });
        tgCum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dayToggle((ToggleButton)v);
            }
        });
        tgCts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dayToggle((ToggleButton)v);
            }
        });
        tgPzr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dayToggle((ToggleButton)v);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(resultCode == RESULT_OK && requestCode == 10){
            alarmSound=data.getData();

            String strUri = alarmSound.toString();
            alarmSound = Uri.parse(strUri);
            db.SaveLastSound(alarmSound);
            AudioInfo inf = new AudioInfo(alarmSound,this);
            AudioInfo.AudioData _d = inf.getInfo();

            checkSndBtn(_d);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu_set, menu);
        return true;
    }

    private void checkSndBtn(AudioInfo.AudioData _d){

        if(_d.title != null && !_d.title.equals("")){
            sndBtn.setText(_d.title);
        }else{
            sndBtn.setText(_d.filename);
        }
        if(sndBtn.getText().length()>20){
            sndBtn.setText(sndBtn.getText().subSequence(0,20) + "...");
        }

        sndBtn.setText("✅ " + sndBtn.getText());
        volBar.setEnabled(true);

        sndBtn.setBackgroundResource(R.drawable.snd_selected);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.settings) {
            return true;
        }*/

        if(id == R.id.chHoroscope){
            h.selectHoroscope();
        }

        if(id == R.id.setAlarm){
            setBtn.performClick();
        }



        return super.onOptionsItemSelected(item);
    }



}
