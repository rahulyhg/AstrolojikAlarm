package com.astrolojik_alarm.ozgunesim.astrolojikalarm;

import android.content.Context;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;

/**
 * Created by ozgun on 20.4.2016.
 */
public class AudioInfo {
    private Uri uri;
    private Context c;
    private AudioData data;

    public AudioInfo(Uri u, Context _c){
        uri = u;
        c= _c;

        data = new AudioData();
        MediaMetadataRetriever mediaMetadataRetriever = (MediaMetadataRetriever) new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(c, uri);
        //String title = (String) mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        data.title = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        data.album = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
        data.albumArtist = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST);
        data.artist = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        data.genre = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE);
        data.duration = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        data.path = uri.getPath();
        data.realPath = getRealPathFromURI();
        if(data.realPath != null){
            String filename=data.realPath.substring(data.realPath.lastIndexOf("/")+1);
            data.filename = filename;
        }
    }

    public AudioData getInfo(){
        return  data;
    }

    public String getAllInfo(){
        String info =
                "Album: " + data.album +
                ". Duration: " + data.duration +
                ". AlbumArtist: " + data.albumArtist +
                ". Title: " + data.title +
                ". Artist: " + data.artist +
                ". Genre: " + data.genre +
                ". Path: " + data.path +
                ". RealPath: " + data.realPath;
        return info;
    }

    public class AudioData{
        public String title = "";
        public String album = "";
        public String albumArtist = "";
        public String artist = "";
        public String genre = "";
        public String duration = "";
        public String path = "";
        public String realPath = "";
        public String filename = "";
    }


    public String getRealPathFromURI() {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = c.getContentResolver().query(uri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

}
