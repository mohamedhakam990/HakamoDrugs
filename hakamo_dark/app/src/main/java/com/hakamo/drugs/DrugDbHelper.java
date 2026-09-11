package com.hakamo.drugs;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.*;
import java.util.*;

public class DrugDbHelper extends SQLiteOpenHelper {
    public static final String DB_NAME="hakamo_drugs.db";
    public static final int DB_VERSION=3;
    private final Context context;
    public DrugDbHelper(Context c){super(c,DB_NAME,null,DB_VERSION);context=c.getApplicationContext();}
    @Override public void onCreate(SQLiteDatabase db){createSchema(db);}
    @Override public void onUpgrade(SQLiteDatabase db,int oldV,int newV){
        createSchema(db);
        try{db.execSQL("ALTER TABLE drugs ADD COLUMN price_egp TEXT");}catch(Exception ignored){}
        try{db.execSQL("ALTER TABLE drugs ADD COLUMN price_updated_at TEXT");}catch(Exception ignored){}
        try{db.execSQL("ALTER TABLE drugs ADD COLUMN image_url TEXT");}catch(Exception ignored){}
    }
    private void createSchema(SQLiteDatabase db){db.execSQL("CREATE TABLE IF NOT EXISTS drugs (id TEXT PRIMARY KEY, trade TEXT, trade_ar TEXT, generic TEXT, generic_ar TEXT, active TEXT, form TEXT, company TEXT, category TEXT, indications TEXT, source TEXT, updated_at TEXT, price_egp TEXT, price_updated_at TEXT, image_url TEXT)");}
    public void upsert(Drug d){SQLiteDatabase db=getWritableDatabase();ContentValues v=new ContentValues();v.put("id",d.id);v.put("trade",d.trade);v.put("trade_ar",d.tradeAr);v.put("generic",d.generic);v.put("generic_ar",d.genericAr);v.put("active",d.active);v.put("form",d.form);v.put("company",d.company);v.put("category",d.category);v.put("indications",d.indications);v.put("source",d.source);v.put("updated_at",d.updatedAt);v.put("price_egp",d.priceEgp);v.put("price_updated_at",d.priceUpdatedAt);v.put("image_url",d.imageUrl);db.insertWithOnConflict("drugs",null,v,SQLiteDatabase.CONFLICT_REPLACE);}
    public void clear(){getWritableDatabase().delete("drugs",null,null);}
    public ArrayList<Drug> search(String q,String cat){
        ArrayList<Drug> out=new ArrayList<>();SQLiteDatabase db=getReadableDatabase();String where="1=1";ArrayList<String> args=new ArrayList<>();
        if(q!=null&&!q.trim().isEmpty()){where+=" AND (trade LIKE ? OR trade_ar LIKE ? OR generic LIKE ? OR generic_ar LIKE ? OR active LIKE ? OR company LIKE ? OR indications LIKE ?)";String x="%"+q.trim()+"%";for(int i=0;i<7;i++)args.add(x);}
        if(cat!=null&&!cat.equals("كل التصنيفات")){where+=" AND category=?";args.add(cat);}
        Cursor c=db.query("drugs",null,where,args.toArray(new String[0]),null,null,"trade COLLATE NOCASE ASC","500");while(c.moveToNext())out.add(from(c));c.close();return out;
    }
    public ArrayList<String> categories(){ArrayList<String>a=new ArrayList<>();Cursor c=getReadableDatabase().rawQuery("SELECT DISTINCT category FROM drugs WHERE category<>'' ORDER BY category",null);while(c.moveToNext())a.add(c.getString(0));c.close();return a;}
    public ArrayList<String> ingredients(){ArrayList<String>a=new ArrayList<>();Cursor c=getReadableDatabase().rawQuery("SELECT DISTINCT active FROM drugs WHERE active<>'' ORDER BY active LIMIT 500",null);while(c.moveToNext())a.add(c.getString(0));c.close();return a;}
    public int count(){Cursor c=getReadableDatabase().rawQuery("SELECT COUNT(*) FROM drugs",null);int n=c.moveToFirst()?c.getInt(0):0;c.close();return n;}
    private String s(Cursor c,String n){int i=c.getColumnIndex(n);return i>=0&&!c.isNull(i)?c.getString(i):"";}
    private Drug from(Cursor c){return new Drug(s(c,"id"),s(c,"trade"),s(c,"trade_ar"),s(c,"generic"),s(c,"generic_ar"),s(c,"active"),s(c,"form"),s(c,"company"),s(c,"category"),s(c,"indications"),s(c,"source"),s(c,"updated_at"),s(c,"price_egp"),s(c,"price_updated_at"),s(c,"image_url"));}
}
