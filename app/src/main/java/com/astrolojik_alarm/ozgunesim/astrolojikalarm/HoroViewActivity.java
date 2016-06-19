package com.astrolojik_alarm.ozgunesim.astrolojikalarm;

import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.TextView;

public class HoroViewActivity extends AppCompatActivity {

    WebView wv;
    TextView lblErr;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu_horo_view, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.closeHoroView){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_horo_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        toolbar.setLogo(R.mipmap.ic_launcher);
        toolbar.setTitle("Günlük Burç Yorumunuz");

        setSupportActionBar(toolbar);

        wv = (WebView)findViewById(R.id.horoWebView);
        /*
        AlarmDB db = new AlarmDB(this);
        String html = db.GetLastHoroComment();
        */

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "AstrolojikAlarm");
        wl.acquire();
        wl.release();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);

        PullComments pc = new PullComments(this,wv);
        pc.pull();
        /*
        if(!html.equals("err")){
            String mime = "text/html";
            String encoding = "utf-8";

            wv.loadData(html,mime,encoding);
        }else{
            lblErr = (TextView)findViewById(R.id.lblErr);
            lblErr.setVisibility(View.VISIBLE);
            lblErr.setText(lblErr.getText() + "\nSP: " + html);
        }
        */

    }

}
