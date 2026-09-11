package com.hakamo.drugs;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("deprecation")
public class ImageLoader {
    private static final ConcurrentHashMap<String,Bitmap> CACHE=new ConcurrentHashMap<>();
    public static void load(String url, ImageView view, int placeholder){
        view.setImageResource(placeholder);
        if(url==null||url.trim().isEmpty()) return;
        final String key=url.trim(); Bitmap cached=CACHE.get(key);
        if(cached!=null){view.setImageBitmap(cached);return;}
        new AsyncTask<Void,Void,Bitmap>(){
            protected Bitmap doInBackground(Void... v){
                HttpURLConnection c=null; InputStream in=null;
                try{c=(HttpURLConnection)new URL(key).openConnection();c.setConnectTimeout(8000);c.setReadTimeout(12000);c.setInstanceFollowRedirects(true);c.setRequestProperty("User-Agent","HakamoDrugs/6.5");c.connect();if(c.getResponseCode()>=200&&c.getResponseCode()<300){in=c.getInputStream();return BitmapFactory.decodeStream(in);}}catch(Exception ignored){}finally{try{if(in!=null)in.close();}catch(Exception ignored){}if(c!=null)c.disconnect();}return null;
            }
            protected void onPostExecute(Bitmap b){if(b!=null){CACHE.put(key,b);view.setImageBitmap(b);}}
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
