package com.hakamo.drugs;

import android.app.*;import android.content.*;import android.net.Uri;import android.os.*;import android.view.*;import android.widget.*;import java.io.*;import java.net.*;import java.util.*;import org.json.*;

public class MainActivity extends Activity {
    DrugDbHelper db; ArrayAdapter<Drug> adapter; ArrayList<Drug> shown=new ArrayList<>(); EditText search; Spinner category; TextView count,status; SharedPreferences prefs;
    static final String DB_URL="https://raw.githubusercontent.com/mohamedhakam990/HakamoDrugs/main/database/hakamo_drugs.db";
    static final String EGYPT_CSV="https://raw.githubusercontent.com/karem505/egyptian-drug-database/main/data/egyptian-drugs.csv";
    @Override public void onCreate(Bundle b){prefs=getSharedPreferences("hakamo",0);if(prefs.getBoolean("amoled",false))setTheme(R.style.AppTheme_Amoled);else setTheme(R.style.AppTheme);super.onCreate(b);setContentView(R.layout.activity_main);installBundledDb();db=new DrugDbHelper(this);showSplashThenMain();}

    void showSplashThenMain(){
      setContentView(R.layout.activity_splash);
      new Handler(Looper.getMainLooper()).postDelayed(()->{
        setContentView(R.layout.activity_main);
        installBundledDb(); db=new DrugDbHelper(this); bind();
      }, 1200);
    }

    void showDeveloperDialog(){LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(28,8,28,4);TextView intro=new TextView(this);intro.setText("👨‍💻 مطور التطبيق: Hakamo\n\nكل ما تحتاجه من تحديثات ومحتوى وشرح ستجده عبر قنواتي وصفحاتي الرسمية.");intro.setTextSize(16);intro.setTextColor(getResources().getColor(R.color.hakamo_text));box.addView(intro);addSocialButton(box,"📢 قناة التليجرام","https://t.me/Hakamo99");addSocialButton(box,"🎵 تيك توك","https://www.tiktok.com/@hakamo99?_r=1&_t=ZS-91x1jX1y20z");addSocialButton(box,"📘 فيسبوك","https://www.facebook.com/share/19QwF7nvvf/");addSocialButton(box,"▶️ يوتيوب","https://youtube.com/@hakamo99");addSocialButton(box,"📚 كل ما تحتاجه في القناة","https://t.me/Hakamo99/1978");TextView dev=new TextView(this);dev.setText("\nHakamo Drugs • جميع الحقوق محفوظة للمطور");dev.setTextSize(13);dev.setTextColor(getResources().getColor(R.color.hakamo_muted));box.addView(dev);new AlertDialog.Builder(this).setTitle("مرحبًا بك في Hakamo Drugs").setView(box).setPositiveButton("دخول للتطبيق",null).setCancelable(false).show();}
    void addSocialButton(LinearLayout box,String title,String url){Button b=new Button(this);b.setText(title);b.setAllCaps(false);b.setOnClickListener(v->{try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url)));}catch(Exception ignored){}});box.addView(b,new LinearLayout.LayoutParams(-1,LinearLayout.LayoutParams.WRAP_CONTENT));}

    void bind(){
      search=findViewById(R.id.search);category=findViewById(R.id.category);count=findViewById(R.id.count);status=findViewById(R.id.status);ListView list=findViewById(R.id.list);
      findViewById(R.id.btnHeaderMenu).setOnClickListener(v->showMoreMenu()); findViewById(R.id.btnHeaderFav).setOnClickListener(v->showFavorites());
      Button amoled=findViewById(R.id.btnAmoled);amoled.setText(prefs.getBoolean("amoled",false)?"☀ فاتح":"🌑 AMOLED");amoled.setOnClickListener(v->{boolean on=!prefs.getBoolean("amoled",false);prefs.edit().putBoolean("amoled",on).apply();recreate();});
      findViewById(R.id.showcase).setOnClickListener(v->showShowcase());
      adapter=new ArrayAdapter<Drug>(this,R.layout.item_drug,shown){public View getView(int p,View v,ViewGroup parent){if(v==null)v=getLayoutInflater().inflate(R.layout.item_drug,parent,false);Drug d=getItem(p);ImageLoader.load(d.imageUrl, (ImageView)v.findViewById(R.id.drugImage), R.drawable.ic_drug_placeholder);((TextView)v.findViewById(R.id.name)).setText(d.displayName());((TextView)v.findViewById(R.id.generic)).setText((d.genericAr==null||d.genericAr.isEmpty()?d.generic:d.genericAr)+(d.generic==null||d.generic.isEmpty()?"":" • "+d.generic));((TextView)v.findViewById(R.id.active)).setText("المادة الفعالة: "+d.active);String price=(d.priceEgp==null||d.priceEgp.isEmpty())?"السعر: غير متاح":"السعر: "+d.priceEgp+" جنيه";((TextView)v.findViewById(R.id.price)).setText(price);((TextView)v.findViewById(R.id.category)).setText(d.category+"  |  "+d.form);TextView f=v.findViewById(R.id.favorite);f.setText(prefs.getBoolean("fav_"+d.id,false)?"★":"☆");f.setOnClickListener(x->{prefs.edit().putBoolean("fav_"+d.id,!prefs.getBoolean("fav_"+d.id,false)).apply();notifyDataSetChanged();});return v;}};list.setAdapter(adapter);list.setOnItemClickListener((p,v,pos,id)->openDrugDetails(shown.get(pos)));
      loadCats();category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){public void onNothingSelected(AdapterView<?> p){}public void onItemSelected(AdapterView<?> p,View v,int pos,long id){filter();}});search.addTextChangedListener(new android.text.TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int c,int d){}public void onTextChanged(CharSequence s,int a,int b,int c){filter();}public void afterTextChanged(android.text.Editable e){}});filter();
      findViewById(R.id.btnDownload).setOnClickListener(v->downloadEgyptCatalog());findViewById(R.id.btnPharmacies).setOnClickListener(v->openNearbyPharmacies());findViewById(R.id.btnIndications).setOnClickListener(v->showIndications());findViewById(R.id.btnCategories).setOnClickListener(v->showCategories());findViewById(R.id.btnIngredients).setOnClickListener(v->showIngredients());findViewById(R.id.btnFav).setOnClickListener(v->showFavorites());findViewById(R.id.btnOnline).setOnClickListener(v->openOnline()); setupBottomNavigation();
    }
    void setupBottomNavigation(){
      TextView home=findViewById(R.id.navHome), drugs=findViewById(R.id.navDrugs), cats=findViewById(R.id.navCategories), ingredients=findViewById(R.id.navIngredients), fav=findViewById(R.id.navFav), more=findViewById(R.id.navMore);
      View.OnClickListener reset=v->{home.setSelected(false);drugs.setSelected(false);cats.setSelected(false);ingredients.setSelected(false);fav.setSelected(false);more.setSelected(false);v.setSelected(true);};
      home.setSelected(true);
      home.setOnClickListener(v->{reset.onClick(v); ((ScrollView)findViewById(R.id.contentScroll)).smoothScrollTo(0,0);});
      drugs.setOnClickListener(v->{reset.onClick(v); ((ScrollView)findViewById(R.id.contentScroll)).smoothScrollTo(0,((View)findViewById(R.id.drugsHeader)).getTop()); search.requestFocus();});
      cats.setOnClickListener(v->{reset.onClick(v);showCategories();});
      ingredients.setOnClickListener(v->{reset.onClick(v);showIngredients();});
      fav.setOnClickListener(v->{reset.onClick(v);showFavorites();});
      more.setOnClickListener(v->{reset.onClick(v);showMoreMenu();});
    }
    void showMoreMenu(){
      String[] items={"🏛 المصادر الرسمية للدواء","⌖ صيدليات قريبة","↻ تحديث البيانات","☀ / 🌑 تغيير المظهر","ℹ عن التطبيق"};
      new AlertDialog.Builder(this).setTitle("المزيد").setItems(items,(d,w)->{if(w==0)openOnline();else if(w==1)openNearbyPharmacies();else if(w==2)downloadEgyptCatalog();else if(w==3){boolean on=!prefs.getBoolean("amoled",false);prefs.edit().putBoolean("amoled",on).apply();recreate();}else showShowcase();}).setNegativeButton("إغلاق",null).show();
    }

    void installBundledDb(){File out=getDatabasePath(DrugDbHelper.DB_NAME);if(out.exists()&&out.length()>1000)return;try{out.getParentFile().mkdirs();InputStream in=getAssets().open("hakamo_drugs.db");FileOutputStream os=new FileOutputStream(out);byte[]buf=new byte[16384];int n;while((n=in.read(buf))>0)os.write(buf,0,n);os.flush();os.close();in.close();}catch(Exception e){statusSafe("تعذر تجهيز قاعدة البيانات المحلية");}}
    void loadCats(){ArrayList<String>a=new ArrayList<>();a.add("كل التصنيفات");a.addAll(db.categories());category.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,a));}
    void filter(){if(search==null)return;shown.clear();shown.addAll(db.search(search.getText().toString(),category==null||category.getSelectedItem()==null?"كل التصنيفات":category.getSelectedItem().toString()));if(adapter!=null)adapter.notifyDataSetChanged();count.setText("النتائج: "+shown.size()+" • إجمالي قاعدة البيانات: "+db.count());}

    void openDrugDetails(Drug d){
      Intent i=new Intent(this,DrugDetailsActivity.class);
      i.putExtra("id",d.id); i.putExtra("trade",d.trade); i.putExtra("tradeAr",d.tradeAr);
      i.putExtra("generic",d.generic); i.putExtra("genericAr",d.genericAr); i.putExtra("active",d.active);
      i.putExtra("form",d.form); i.putExtra("company",d.company); i.putExtra("category",d.category);
      i.putExtra("indications",d.indications); i.putExtra("source",d.source); i.putExtra("updatedAt",d.updatedAt);
      i.putExtra("price",d.priceEgp); i.putExtra("priceUpdatedAt",d.priceUpdatedAt); i.putExtra("imageUrl",d.imageUrl);
      startActivity(i);
    }

    void showShowcase(){new AlertDialog.Builder(this).setTitle("Hakamo Drugs").setMessage("دليل دوائي منظم للبحث والاستكشاف.\n\nابحث بالاسم التجاري أو العلمي أو المادة الفعالة، واستعرض السعر والمعلومات والخدمات المرتبطة بالدواء.\n\nالمعلومات مرجعية وتعليمية، ويجب الرجوع للمصدر الرسمي والطبيب أو الصيدلي عند الحاجة.").setPositiveButton("حسنًا",null).show();}
    void openNearbyPharmacies(){try{Intent i=new Intent(Intent.ACTION_VIEW,Uri.parse("geo:0,0?q=pharmacy"));startActivity(i);}catch(Exception e){startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://www.google.com/maps/search/pharmacy")));}}
    void showCategories(){final ArrayList<String>a=db.categories();new AlertDialog.Builder(this).setTitle("التصنيفات").setItems(a.toArray(new String[0]),(d,w)->{search.setText("");category.setSelection(w+1);}).setNegativeButton("إغلاق",null).show();}
    void showIngredients(){final ArrayList<String>a=db.ingredients();new AlertDialog.Builder(this).setTitle("المواد الفعالة").setItems(a.toArray(new String[0]),(d,w)->search.setText(a.get(w))).setNegativeButton("إغلاق",null).show();}
    void showIndications(){final EditText e=new EditText(this);e.setHint("مثال: pain أو ضغط أو التهاب");new AlertDialog.Builder(this).setTitle("🔎 البحث في دواعي الاستخدام").setMessage("ابحث داخل وصف الاستخدامات المسجل في قاعدة البيانات.").setView(e).setPositiveButton("بحث",(d,w)->search.setText(e.getText().toString())).setNegativeButton("إغلاق",null).show();}
    void showFavorites(){ArrayList<Drug>a=new ArrayList<>();for(Drug d:db.search("","كل التصنيفات"))if(prefs.getBoolean("fav_"+d.id,false))a.add(d);String[]r=new String[a.size()];for(int i=0;i<a.size();i++)r[i]=a.get(i).displayName();new AlertDialog.Builder(this).setTitle("⭐ المفضلة").setItems(r,(d,w)->openDrugDetails(a.get(w))).setNegativeButton("إغلاق",null).show();}
    void openOnline(){startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://eservices.edaegypt.gov.eg/EDASearch/SearchRegDrugs.aspx")));}

    void downloadEgyptCatalog(){statusSafe("جاري تنزيل قاعدة الأدوية المصرية وتحديث الأسعار…");new Thread(()->{HttpURLConnection c=null;File tmp=null;try{c=(HttpURLConnection)new URL(EGYPT_CSV).openConnection();c.setConnectTimeout(20000);c.setReadTimeout(120000);c.setRequestProperty("User-Agent","HakamoDrugs/6.0");if(c.getResponseCode()!=200)throw new IOException("HTTP "+c.getResponseCode());tmp=new File(getCacheDir(),"egyptian-drugs.csv");InputStream in=c.getInputStream();FileOutputStream os=new FileOutputStream(tmp);byte[]buf=new byte[32768];int n;while((n=in.read(buf))>0)os.write(buf,0,n);os.close();in.close();c.disconnect();int nrows=importCsv(tmp);int total=db.count();runOnUiThread(()->{loadCats();filter();status.setText("تم تحديث البيانات المصرية • "+total+" سجل");new AlertDialog.Builder(this).setTitle("تم تحديث قاعدة البيانات").setMessage("تم استيراد "+nrows+" سجل دوائي من قاعدة البيانات المصرية.\n\nالسعر المعروض هو السعر الموجود في مصدر البيانات وقت آخر تحديث، وقد يتغير في السوق.").setPositiveButton("حسنًا",null).show();});}catch(Exception e){if(c!=null)c.disconnect();runOnUiThread(()->new AlertDialog.Builder(this).setTitle("تعذر التحديث").setMessage("تأكد من الإنترنت وحاول مرة أخرى.\n\n"+e.getMessage()).setPositiveButton("حسنًا",null).show());}}).start();}

    int importCsv(File file)throws Exception{BufferedReader r=new BufferedReader(new InputStreamReader(new FileInputStream(file),"UTF-8"));String header=r.readLine();if(header==null)throw new IOException("CSV فارغ");String[] h=parseCsv(header);HashMap<String,Integer> idx=new HashMap<>();for(int i=0;i<h.length;i++)idx.put(h[i].trim(),i);int inserted=0;String line;android.database.sqlite.SQLiteDatabase sql=db.getWritableDatabase();sql.beginTransaction();try{while((line=r.readLine())!=null){String[] v=parseCsv(line);String trade=csv(v,idx,"commercial_name_en","trade_name_en","trade","brand_name");String tradeAr=csv(v,idx,"commercial_name_ar","trade_name_ar","trade_ar");String generic=csv(v,idx,"scientific_name","generic_name","generic");String active=csv(v,idx,"active_ingredient","active_ingredients","substance_name");if(active.isEmpty())active=generic;String company=csv(v,idx,"manufacturer","company","manufacturer_name");String form=csv(v,idx,"dosage_form","form","dosage");String category=csv(v,idx,"drug_class","category","therapeutic_class","pharmacological_class");if(category.isEmpty())category="غير مصنف";String indications=csv(v,idx,"indications","indications_and_usage","uses","purpose");String price=csv(v,idx,"price_egp","price","price_egp_current","current_price");String updated=csv(v,idx,"updated_at","last_updated","date");if(trade.isEmpty()&&generic.isEmpty()&&active.isEmpty())continue;String id="eg-"+Integer.toHexString((trade+"|"+tradeAr+"|"+generic+"|"+active+"|"+company+"|"+form).hashCode());String imageUrl=csv(v,idx,"image_url","image","image_link","product_image","photo_url");db.upsert(new Drug(id,trade,tradeAr,generic,"",active,form,company,category,indications,"Egyptian Drug Database",updated,price,updated,imageUrl));inserted++;} sql.setTransactionSuccessful();}finally{sql.endTransaction();r.close();}return inserted;}
    String[] parseCsv(String line){ArrayList<String>a=new ArrayList<>();StringBuilder b=new StringBuilder();boolean q=false;for(int i=0;i<line.length();i++){char c=line.charAt(i);if(c=='"'){if(q&&i+1<line.length()&&line.charAt(i+1)=='"'){b.append('"');i++;}else q=!q;}else if(c==','&&!q){a.add(b.toString());b.setLength(0);}else b.append(c);}a.add(b.toString());return a.toArray(new String[0]);}
    String csv(String[]v,HashMap<String,Integer>idx,String...names){for(String n:names){Integer i=idx.get(n);if(i!=null&&i<v.length&&v[i]!=null&&!v[i].trim().isEmpty())return v[i].trim();}return "";}
    void statusSafe(String s){if(status!=null)status.setText(s);}
}
