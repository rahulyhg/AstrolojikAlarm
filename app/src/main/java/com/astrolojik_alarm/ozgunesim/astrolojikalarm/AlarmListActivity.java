package com.astrolojik_alarm.ozgunesim.astrolojikalarm;

import android.app.ListActivity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.text.method.HideReturnsTransformationMethod;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class AlarmListActivity extends AppCompatActivity{

    ListView lsAlarm;
    public static CustomListAdaptor adaptor;
    //List<Alarm> alarms = new ArrayList<Alarm>();
    Toolbar toolbar;
    AlarmDB db;
    Horoscope h;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_list);

        db = new AlarmDB(AlarmListActivity.this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        //toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setLogo(R.mipmap.ic_launcher);
        //toolbar.setNavigationIcon(R.drawable.leftarrow);
        setSupportActionBar(toolbar);

        //getAlarms();

        lsAlarm = (ListView) findViewById(R.id.lstAlarm);
        adaptor = new CustomListAdaptor(this);
        adaptor.setContext(AlarmListActivity.this);
        lsAlarm.setAdapter(adaptor);
        registerForContextMenu(lsAlarm);

        lsAlarm.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openAlarmActivity(String.valueOf(position));
            }
        });

        h = new Horoscope(AlarmListActivity.this,toolbar);
        if(!h.isSet())
            h.selectHoroscope();
    }

    void checkNotification(){
        NotificationSystem.checkNotifications(this);
    }

    void updateList(){
        //db.SaveAlarms();
        ((BaseAdapter)adaptor).notifyDataSetChanged();
        checkNotification();
    }


    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        updateList();
        /*
        adaptor.notifyDataSetChanged();
        checkNotification();
*/
        h.checkUser();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    String[] LongClickMenu = {"Sil","Değiştir"};
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId()==R.id.lstAlarm) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
            menu.setHeaderTitle("İşlem Seçin:");
            int pos = info.position;
            //String[] menuItems = {"Sil"};
            for (int i = 0; i<LongClickMenu.length; i++) {
                menu.add(Menu.NONE, i, i, LongClickMenu[i]);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int menuItemIndex = item.getItemId();
        if (menuItemIndex == 0) {
            int position = info.position;
            Alarm.RemoveAlarm(db.GetAlarmbyPosition(position).alarmProp.ID, AlarmListActivity.this);
            db.RemoveAlarmbyPosition(position); //MainActivity.AlarmList.remove(position);
            updateList();
            checkNotification();
        }else if(menuItemIndex == 1){
            int position = info.position;
            openAlarmActivity(String.valueOf(position));
        }
        //Toast.makeText(getApplicationContext(), "pozisyon :" + info.position + " itemindex: " + menuItemIndex, Toast.LENGTH_LONG).show();
        return true;
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

        if(id == R.id.addAlarm){
            openAlarmActivity("new");
        }

        if(id == R.id.chHoroscope){
            h.selectHoroscope();
        }

        if(id == R.id.readComments){
            Intent intent = new Intent(AlarmListActivity.this, HoroViewActivity.class);
            startActivity(intent);
        }



        return super.onOptionsItemSelected(item);
    }

    void openAlarmActivity(String value){
        Intent myIntent = new Intent(this, MainActivity.class);
        myIntent.putExtra("key", value); //Optional parameters
        this.startActivity(myIntent);
        this.overridePendingTransition(R.anim.slide_out, R.anim.slide_in);
    }



}
