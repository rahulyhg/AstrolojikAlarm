package com.astrolojik_alarm.ozgunesim.astrolojikalarm;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.net.URL;

/**
 * Created by ozgun on 25.4.2016.
 */
public class PullComments {
    private Context c;
    String hUrl;
    Document doc;
    WebView wv;
    ProgressDialog progress;

    public PullComments(Context _c){
        c = _c;
        chooseLink();
    }

    public PullComments(Context _c, WebView _wv){
        c = _c;
        wv = _wv;
        chooseLink();
    }

    void chooseLink(){
        String[] links = c.getResources().getStringArray(R.array.links);
        Horoscope h = new Horoscope(c);
        hUrl = links[h.getUserHoroscope().id-1].split(";")[1];
    }

    public void pull(){
        new BackgroundTask().execute();
    }

    public class BackgroundTask extends AsyncTask<Void, Void, Void> {

        String data;
        Boolean err = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress = ProgressDialog.show(c, "Lütfen Bekleyin",
                    "Burç Yorumlarınız Alınıyor...", true);
        }

        @Override
        protected Void doInBackground(Void... params) {
            Document doc;
            try{
                //doc = Jsoup.connect(hUrl).get();
                doc = Jsoup.parse(new URL(hUrl).openStream(), "UTF-8",hUrl);
                Elements temp = doc.select("div[itemprop=articleBody]");
                Elements head = doc.select("head");
                temp.select("div").remove();
                temp.select("a").remove();
                Elements images = temp.select("img");
                images.wrap("<div style='display:block;text-align:center;'></div>");
                temp.select("p").after("<hr />");

                data = "<!DOCTYPE HTML>\n" +
                        "<html lang='tr'>\n" +
                        "<head>" +
                        head.html() +
                        "<style>" +
                        "h1,h2,h3,h4,h5{display:block;text-align:center;}" +
                        "</style>" +
                        "\n</head><body>";


                data += temp.html();

                data += "\n</body>\n</html>";

                data = data.replaceAll("&#304;", "İ");
                data = data.replaceAll("&#305;", "ı");
                data = data.replaceAll("&#214;", "Ö");
                data = data.replaceAll("&#246;", "ö");
                data = data.replaceAll("&#220;", "Ü");
                data = data.replaceAll("&#252;", "ü");
                data = data.replaceAll("&#199;", "Ç");
                data = data.replaceAll("&#231;", "ç");
                data = data.replaceAll("&#286;", "Ğ");
                data = data.replaceAll("&#287;", "ğ");
                data = data.replaceAll("&#350;", "Ş");
                data = data.replaceAll("&#351;", "ş");

                data = data.replaceAll("Ä°", "İ");
                data = data.replaceAll("Ä±", "ı");
                data = data.replaceAll("Ã–", "Ö");
                data = data.replaceAll("Ã¶", "ö");
                data = data.replaceAll("Ãœ", "Ü");
                data = data.replaceAll("Ã¼", "ü");
                data = data.replaceAll("Ã‡", "Ç");
                data = data.replaceAll("Ã§", "ç");
                data = data.replaceAll("ÄŸ", "Ğ");
                data = data.replaceAll("ÄŸ", "ğ");
                data = data.replaceAll("ÅŸ", "Ş");
                data = data.replaceAll("ÅŸ", "ş");



                data = data.replaceAll("&Ouml;", "Ö");
                data = data.replaceAll("&ouml;", "ö");
                data = data.replaceAll("&Uuml;", "Ü");
                data = data.replaceAll("&uuml;", "ü");
                data = data.replaceAll("&Ccedil;", "Ç");
                data = data.replaceAll("&ccedil;", "ç");

                data = data.replaceAll("\\/images\\/","http:\\/\\/www.gunlukburcyorumlari.net\\/images\\/");
                data = data;
                //ata = doc.html();



            }catch (Exception e){
                err = true;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            progress.dismiss();
            if(!err){
                AlarmDB db = new AlarmDB(c);
                db.SaveLastHoroComment(data);
                if(wv != null){
                    WebSettings settings = wv.getSettings();
                    settings.setDefaultTextEncodingName("utf-8");

                    String mime = "text/html; charset=utf-8";
                    String encoding = "UTF-8";
                    wv.loadData(data,mime,encoding);
                    //wv.loadUrl(hUrl);
                }
            }else{
                AlarmDB db = new AlarmDB(c);
                db.SaveLastHoroComment("err");
                if(wv != null){
                    wv.setBackgroundColor(Color.RED);
                    String errData = "<h1 style='text-align:center;color:white;font-size:30px;'>HATA.<br>Internet Ayarlarinizi Kontrol Edin!</h1>";
                    wv.loadData(errData,"text/html","charset=utf-8");
                }
            }


        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onCancelled(Void result) {
            super.onCancelled(result);
            progress.dismiss();
        }

    }


}
