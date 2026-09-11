package com.hakamo.drugs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import java.util.ArrayList;

public class DrugDetailsActivity extends Activity {
    android.content.SharedPreferences prefs;
    String id, trade, tradeAr, generic, genericAr, active, form, company, category, indications, source, updatedAt, price, priceUpdatedAt, imageUrl;

    @Override public void onCreate(Bundle b){
        prefs=getSharedPreferences("hakamo",0);
        setTheme(prefs.getBoolean("amoled",false)?R.style.AppTheme_Amoled:R.style.AppTheme);
        super.onCreate(b); setContentView(R.layout.activity_drug_details);
        readExtras(getIntent()); bind();
    }
    void readExtras(Intent i){
        id=i.getStringExtra("id"); trade=i.getStringExtra("trade"); tradeAr=i.getStringExtra("tradeAr"); generic=i.getStringExtra("generic"); genericAr=i.getStringExtra("genericAr");
        active=i.getStringExtra("active"); form=i.getStringExtra("form"); company=i.getStringExtra("company"); category=i.getStringExtra("category"); indications=i.getStringExtra("indications");
        source=i.getStringExtra("source"); updatedAt=i.getStringExtra("updatedAt"); price=i.getStringExtra("price"); priceUpdatedAt=i.getStringExtra("priceUpdatedAt"); imageUrl=i.getStringExtra("imageUrl");
    }
    String nz(String s){return s==null||s.trim().isEmpty()?"غير متاح":s.trim();}
    String title(){String a=nz(tradeAr);return !a.equals("غير متاح")?a: nz(trade);}
    TextView tv(int id){return findViewById(id);}
    void bind(){
        findViewById(R.id.btnBack).setOnClickListener(v->finish());
        tv(R.id.detailTitle).setText("💊 "+title());
        ImageLoader.load(imageUrl, (ImageView)findViewById(R.id.detailImage), R.drawable.ic_drug_placeholder);
        tv(R.id.imageStatus).setText(imageUrl==null||imageUrl.trim().isEmpty()?"لا توجد صورة موثقة لهذا المنتج حاليًا":"صورة المنتج من المصدر المرتبط بالبيانات");
        tv(R.id.detailSubtitle).setText(nz(genericAr)+(nz(generic).equals("غير متاح")?"":" • "+generic));
        tv(R.id.detailActive).setText(nz(active));
        tv(R.id.detailMeta).setText(nz(form)+"  •  "+nz(company)+"  •  "+nz(category));
        tv(R.id.detailWhat).setText(whatItDoes());
        tv(R.id.detailIndications).setText(nz(indications));
        tv(R.id.detailPrice).setText(price==null||price.trim().isEmpty()?"غير متاح":""+price+" جنيه");
        tv(R.id.detailPriceDate).setText("آخر تحديث للسعر: "+nz(priceUpdatedAt));
        tv(R.id.detailSource).setText(nz(source)+"\nآخر تحديث للبيانات: "+nz(updatedAt));
        Button fav=findViewById(R.id.btnDetailFav); updateFav(fav); fav.setOnClickListener(v->{boolean n=!prefs.getBoolean("fav_"+id,false);prefs.edit().putBoolean("fav_"+id,n).apply();updateFav(fav);});
        findViewById(R.id.btnAlternativesDetail).setOnClickListener(v->showAlternatives());
        findViewById(R.id.btnPharmaciesDetail).setOnClickListener(v->openPharmacies());
        findViewById(R.id.btnEdaDetail).setOnClickListener(v->openUrl("https://eservices.edaegypt.gov.eg/EDASearch/SearchRegDrugs.aspx"));
        findViewById(R.id.btnShareDetail).setOnClickListener(v->share());
    }
    void updateFav(Button b){b.setText(prefs.getBoolean("fav_"+id,false)?"★ إزالة من المفضلة":"☆ إضافة للمفضلة");}
    String whatItDoes(){
        if(indications!=null&&!indications.trim().isEmpty()) return indications.trim();
        return "لا توجد بيانات كافية في السجل المحلي لشرح الاستخدام. راجع النشرة الرسمية أو اسأل الطبيب/الصيدلي.";
    }
    void showAlternatives(){
        DrugDbHelper helper=new DrugDbHelper(this);
        ArrayList<Drug> a=helper.search(active==null?"":active,"كل التصنيفات");
        ArrayList<String> names=new ArrayList<>();
        for(Drug d:a){ if(d.id!=null && !d.id.equals(id)) names.add(d.displayName()); }
        if(names.isEmpty()) names.add("لا توجد بدائل مسجلة بنفس المادة في قاعدة البيانات المحلية.");
        new AlertDialog.Builder(this).setTitle("🔄 بدائل بنفس المادة الفعالة").setItems(names.toArray(new String[0]),null).setNegativeButton("إغلاق",null).show();
    }
    void openPharmacies(){try{startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=pharmacy")));}catch(Exception e){openUrl("https://www.google.com/maps/search/pharmacy");}}
    void openUrl(String u){try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(u)));}catch(Exception ignored){}}
    void share(){String text=title()+"\nالمادة الفعالة: "+nz(active)+"\nالسعر المسجل: "+(price==null||price.isEmpty()?"غير متاح":price+" جنيه")+"\nالمصدر: "+nz(source);startActivity(Intent.createChooser(new Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT,text),"مشاركة الدواء"));}
}
