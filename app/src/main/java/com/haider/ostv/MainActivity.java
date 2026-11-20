package com.haider.ostv;
import android.app.*;import android.os.*;import android.content.*;import android.content.pm.*;import android.graphics.Color;import android.graphics.drawable.GradientDrawable;import android.graphics.drawable.Drawable;import android.graphics.Typeface;import android.view.animation.DecelerateInterpolator;import android.net.Uri;import android.net.wifi.WifiManager;import android.provider.Settings;import android.view.*;import android.widget.*;import java.io.*;import java.net.*;import java.security.SecureRandom;import java.util.*;
public class MainActivity extends Activity {
  ArrayList<ArrayList<View>> navRows=new ArrayList<ArrayList<View>>(); int navRow=0,navCol=0; LinearLayout list; ServerSocket listener; volatile boolean enabled=false; String pin; String page=""; int failedAttempts=0; long lockUntil=0L; final int GOLD=Color.rgb(229,181,76);
  @Override public void onCreate(Bundle b){super.onCreate(b);pin=String.format(Locale.US,"%06d",new SecureRandom().nextInt(1000000));setVolumeControlStream(android.media.AudioManager.STREAM_MUSIC);home();
    if(getPreferences(MODE_PRIVATE).getBoolean("show_input_at_start",false))new Handler().postDelayed(()->openVendorInput(),800);}

  final int BG=Color.rgb(12,10,11), PANEL=Color.rgb(34,27,25), MUTED=Color.rgb(189,164,137);
  int dp(float v){return (int)(getResources().getDisplayMetrics().density*v+0.5f);}
  GradientDrawable shape(int top,int bottom,int radius,boolean selected){
    GradientDrawable g=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{top,bottom});
    g.setCornerRadius(dp(radius));g.setStroke(dp(selected?3:1),selected?GOLD:Color.rgb(80,62,44));return g;
  }
  TextView text(String caption,int size,int color,boolean bold){
    TextView t=new TextView(this);t.setText(caption);t.setTextColor(color);t.setTextSize(size);
    if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);t.setGravity(Gravity.CENTER_VERTICAL);return t;
  }
  LinearLayout pageRoot(String title){
    page=title;LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setPadding(dp(28),dp(18),dp(28),dp(12));
    root.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{BG,Color.rgb(36,23,19),Color.rgb(16,13,13)}));
    LinearLayout header=new LinearLayout(this);header.setGravity(Gravity.CENTER_VERTICAL);
    TextView logo=text("♛ HAIDER  OS TV",25,GOLD,true);header.addView(logo,new LinearLayout.LayoutParams(0,dp(45),1));
    TextView version=text("THE FUTURE OF SMART TV     •     "+title,12,MUTED,false);
    header.addView(version);root.addView(header);
    View border=new View(this);border.setBackgroundColor(Color.rgb(101,74,44));
    LinearLayout.LayoutParams borderParams=new LinearLayout.LayoutParams(-1,dp(1));borderParams.setMargins(0,dp(5),0,dp(10));root.addView(border,borderParams);
    return root;
  }
  void screen(String title){
    LinearLayout root=pageRoot(title);ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);
    list=new LinearLayout(this);list.setOrientation(1);scroll.addView(list);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));setContentView(root);
  }
  void button(String label,Runnable action){
    TextView b=text(label,18,Color.WHITE,true);b.setPadding(dp(20),dp(16),dp(20),dp(16));
    b.setFocusable(true);b.setClickable(true);
    b.setBackground(shape(PANEL,Color.rgb(50,37,28),17,false));
    b.setOnFocusChangeListener((v,focused)->{
      v.setBackground(shape(focused?Color.rgb(84,58,31):PANEL,focused?Color.rgb(49,35,25):Color.rgb(50,37,28),17,focused));
      v.animate().scaleX(focused?1.012f:1f).scaleY(focused?1.012f:1f).setDuration(130).start();
    });
    b.setOnClickListener(v->action.run());LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,dp(5),0,dp(5));list.addView(b,p);
  }
  void labelRow(LinearLayout parent,String caption){
    TextView label=text(caption,19,GOLD,true);label.setPadding(dp(5),dp(15),0,dp(9));parent.addView(label);
  }
  LinearLayout horizontalRow(LinearLayout parent){
    HorizontalScrollView scroll=new HorizontalScrollView(this);scroll.setHorizontalScrollBarEnabled(false);
    scroll.setClipToPadding(false);LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);
    row.setPadding(dp(6),dp(5),dp(6),dp(10));scroll.addView(row);
    parent.addView(scroll,new LinearLayout.LayoutParams(-1,-2));navRows.add(new ArrayList<View>());return row;
  }
  View tile(String title,Drawable icon,Runnable open){
    LinearLayout card=new LinearLayout(this);card.setOrientation(1);card.setGravity(Gravity.CENTER);
    card.setFocusable(true);card.setClickable(true);card.setPadding(dp(9),dp(10),dp(9),dp(10));
    card.setBackground(shape(Color.rgb(62,44,33),Color.rgb(28,24,23),20,false));
    LinearLayout.LayoutParams box=new LinearLayout.LayoutParams(dp(144),dp(135));box.setMargins(dp(4),0,dp(10),0);
    ImageView picture=new ImageView(this);picture.setScaleType(ImageView.ScaleType.FIT_CENTER);
    if(icon!=null)picture.setImageDrawable(icon);else picture.setImageResource(android.R.drawable.ic_menu_manage);
    card.addView(picture,new LinearLayout.LayoutParams(dp(61),dp(65)));
    TextView caption=text(title,13,Color.WHITE,true);caption.setGravity(Gravity.CENTER);caption.setMaxLines(2);
    caption.setEllipsize(android.text.TextUtils.TruncateAt.END);
    LinearLayout.LayoutParams cap=new LinearLayout.LayoutParams(-1,dp(40));cap.topMargin=dp(5);card.addView(caption,cap);
    card.setOnFocusChangeListener((v,focused)->{
      v.setBackground(shape(focused?Color.rgb(112,79,38):Color.rgb(62,44,33),focused?Color.rgb(56,38,28):Color.rgb(28,24,23),20,focused));
      v.animate().scaleX(focused?1.055f:1f).scaleY(focused?1.055f:1f).setDuration(140).setInterpolator(new DecelerateInterpolator()).start();
      caption.setTextColor(focused?GOLD:Color.WHITE);
      if(focused){v.bringToFront();updateSelection(v);}
    });
    card.setOnClickListener(v->open.run());card.setLayoutParams(box);return card;
  }
  Drawable symbol(int res){return getResources().getDrawable(res);}
  void quick(LinearLayout row,String title,int icon,Runnable action){View v=tile(title,symbol(icon),action);row.addView(v);navRows.get(navRows.size()-1).add(v);}
  List<ResolveInfo> installed(){
    PackageManager pm=getPackageManager();Intent query=new Intent(Intent.ACTION_MAIN);
    query.addCategory(Intent.CATEGORY_LAUNCHER);
    List<ResolveInfo> all=pm.queryIntentActivities(query,0);
    List<ResolveInfo> visible=new ArrayList<ResolveInfo>();
    for(ResolveInfo r:all){
      String name=String.valueOf(r.loadLabel(pm)).toLowerCase(Locale.US);
      if(name.matches(".*(\\bgames?\\b|ألعاب|لعبة|game center).*"))continue;
      if((r.activityInfo.applicationInfo.flags & 0x02000000)!=0)continue;
      visible.add(r);
    }
    final java.util.List<String> saved=storedOrder();
    Collections.sort(visible,new Comparator<ResolveInfo>(){public int compare(ResolveInfo x,ResolveInfo y){
      int ax=saved.indexOf(x.activityInfo.packageName),by=saved.indexOf(y.activityInfo.packageName);
      if(ax>=0&&by>=0)return Integer.compare(ax,by);
      if(ax>=0)return -1;if(by>=0)return 1;
      return String.valueOf(x.loadLabel(pm)).compareToIgnoreCase(String.valueOf(y.loadLabel(pm)));
    }});
    return visible;
  }
  void appsRow(LinearLayout parent,List<ResolveInfo> all,int first,int last){
    LinearLayout row=horizontalRow(parent);PackageManager pm=getPackageManager();
    for(int i=first;i<Math.min(last,all.size());i++){
      ResolveInfo a=all.get(i);final String pkg=a.activityInfo.packageName;
      Drawable image=null;try{image=a.loadIcon(pm);}catch(Exception ignored){}
      View tileView=tile(String.valueOf(a.loadLabel(pm)),image,()->{
        Intent go=pm.getLaunchIntentForPackage(pkg);
        if(go!=null)launch(go);
      });row.addView(tileView);navRows.get(navRows.size()-1).add(tileView);
    }
  }

  View dockTile(String caption,Drawable icon,Runnable open){
    LinearLayout card=new LinearLayout(this);card.setOrientation(1);card.setGravity(Gravity.CENTER);
    card.setFocusable(true);card.setClickable(true);
    card.setPadding(dp(9),dp(7),dp(9),dp(7));
    card.setBackground(shape(Color.rgb(41,38,40),Color.rgb(19,19,23),18,false));
    ImageView image=new ImageView(this);
    if(icon!=null)image.setImageDrawable(icon);
    card.addView(image,new LinearLayout.LayoutParams(dp(55),dp(55)));
    TextView name=text(caption,13,Color.rgb(231,226,222),true);
    name.setGravity(Gravity.CENTER);name.setSingleLine(true);
    name.setEllipsize(android.text.TextUtils.TruncateAt.END);
    LinearLayout.LayoutParams label=new LinearLayout.LayoutParams(-1,dp(25));label.topMargin=dp(5);card.addView(name,label);
    LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(115),1);
    p.setMargins(dp(6),dp(7),dp(6),dp(7));card.setLayoutParams(p);
    card.setOnClickListener(v->open.run());
    card.setOnFocusChangeListener((v,focused)->{
      card.setBackground(shape(focused?Color.rgb(101,77,45):Color.rgb(41,38,40),focused?Color.rgb(46,38,33):Color.rgb(19,19,23),18,focused));
      name.setTextColor(focused?GOLD:Color.WHITE);
      card.animate().scaleX(focused?1.06f:1f).scaleY(focused?1.06f:1f).setDuration(145).start();
      if(focused)card.bringToFront();
    });return card;
  }
  void dockApp(LinearLayout row,ResolveInfo info){
    PackageManager pm=getPackageManager();String pkg=info.activityInfo.packageName;
    Drawable icon=null;try{icon=info.loadIcon(pm);}catch(Exception ignored){}
    row.addView(dockTile(String.valueOf(info.loadLabel(pm)),icon,()->{
      Intent launch=pm.getLaunchIntentForPackage(pkg);if(launch!=null)launch(launch);
    }));
  }
  void sectionGrid(LinearLayout parent,List<ResolveInfo> apps,int from,int max){
    PackageManager pm=getPackageManager();int cols=4;
    for(int i=from;i<Math.min(max,apps.size());i+=cols){
      LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);
      for(int j=i;j<Math.min(i+cols,Math.min(max,apps.size()));j++){
        ResolveInfo info=apps.get(j);String pkg=info.activityInfo.packageName;
        Drawable icon=null;try{icon=info.loadIcon(pm);}catch(Exception ignored){}
        row.addView(dockTile(String.valueOf(info.loadLabel(pm)),icon,()->{
          Intent go=pm.getLaunchIntentForPackage(pkg);if(go!=null)launch(go);
        }));
      }
      parent.addView(row);
    }
  }
  void home(){
    page="HOME";
    LinearLayout root=new LinearLayout(this);root.setOrientation(1);
    root.setPadding(dp(35),dp(19),dp(35),dp(10));
    root.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,
      new int[]{Color.rgb(4,5,8),Color.rgb(18,14,15),Color.rgb(26,15,10),Color.rgb(5,6,9)}));
    LinearLayout top=new LinearLayout(this);top.setGravity(Gravity.CENTER_VERTICAL);
    TextView logo=text("♛ HAIDER OS TV",19,GOLD,true);top.addView(logo,new LinearLayout.LayoutParams(0,dp(39),1));
    TextView clock=text(new java.text.SimpleDateFormat("HH:mm   •   EEE d MMM",Locale.getDefault()).format(new Date()),15,Color.WHITE,false);
    clock.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);top.addView(clock);
    root.addView(top);
    View line=new View(this);line.setBackgroundColor(Color.rgb(65,51,42));
    root.addView(line,new LinearLayout.LayoutParams(-1,dp(1)));
    ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);
    LinearLayout body=new LinearLayout(this);body.setOrientation(1);body.setGravity(Gravity.TOP);
    TextView greeting=text("Your TV. Your space.",34,Color.WHITE,true);greeting.setPadding(dp(8),dp(24),0,0);body.addView(greeting);
    TextView description=text("Beautifully simple. Made for your screen.",15,MUTED,false);
    description.setPadding(dp(9),0,0,dp(14));body.addView(description);
    TextView dockTitle=text("FAVORITES   •   المفضلة",14,GOLD,true);dockTitle.setPadding(dp(9),dp(8),0,dp(3));body.addView(dockTitle);
    LinearLayout dock=new LinearLayout(this);dock.setOrientation(LinearLayout.HORIZONTAL);
    dock.setPadding(dp(7),dp(7),dp(7),dp(7));
    dock.setBackground(shape(Color.rgb(24,23,27),Color.rgb(15,15,19),24,false));
    List<ResolveInfo> all=installed();
    int favorites=Math.min(4,all.size());
    for(int i=0;i<favorites;i++)dockApp(dock,all.get(i));
    if(favorites==0) {
      dock.addView(dockTile("التطبيقات",symbol(android.R.drawable.ic_menu_view),this::apps));
      dock.addView(dockTile("الملفات",symbol(android.R.drawable.ic_menu_save),()->files(android.os.Environment.getExternalStorageDirectory())));
    }
    body.addView(dock);
    TextView gridTitle=text("ALL APPS   •   التطبيقات",14,GOLD,true);
    gridTitle.setPadding(dp(10),dp(21),0,dp(4));body.addView(gridTitle);
    if(all.isEmpty()){body.addView(text("No installed apps found",16,Color.WHITE,false));}
    else sectionGrid(body,all,0,Math.min(all.size(),20));
    TextView actions=text("TOOLS  •  الأدوات",14,GOLD,true);
    actions.setPadding(dp(10),dp(18),0,dp(4));body.addView(actions);
    LinearLayout tools=new LinearLayout(this);tools.setOrientation(LinearLayout.HORIZONTAL);
    tools.addView(dockTile("ملفات APK",symbol(android.R.drawable.ic_menu_save),()->files(android.os.Environment.getExternalStorageDirectory())));
    tools.addView(dockTile("الإعدادات",symbol(android.R.drawable.ic_menu_preferences),this::settings));
    tools.addView(dockTile("ريموت الجوال",symbol(android.R.drawable.ic_menu_call),this::remote));
    tools.addView(dockTile("فحص الجهاز",symbol(android.R.drawable.ic_menu_info_details),this::diagnostics));
    tools.addView(dockTile("ترتيب التطبيقات",symbol(android.R.drawable.ic_menu_sort_by_size),this::arrangeApps));
    tools.addView(dockTile("HDMI / INPUT",symbol(android.R.drawable.ic_menu_slideshow),this::inputSources));
    body.addView(tools);scroll.addView(body);
    root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));setContentView(root);
    scroll.post(()->{if(dock.getChildCount()>0)dock.getChildAt(0).requestFocus();});
  }
  void apps(){
    navRows.clear();navRow=0;navCol=0;
    LinearLayout root=pageRoot("APPLICATIONS");
    ScrollView sc=new ScrollView(this);LinearLayout body=new LinearLayout(this);body.setOrientation(1);
    List<ResolveInfo> all=installed();labelRow(body,"كل التطبيقات  •  All Apps");
    for(int i=0;i<all.size();i+=8)appsRow(body,all,i,i+8);
    sc.addView(body);root.addView(sc,new LinearLayout.LayoutParams(-1,0,1));setContentView(root);
    if(body.getChildCount()>1){View first=body.getChildAt(1);if(first instanceof HorizontalScrollView){
      HorizontalScrollView h=(HorizontalScrollView)first;h.post(()->{
        LinearLayout row=(LinearLayout)h.getChildAt(0);if(row.getChildCount()>0)row.getChildAt(0).requestFocus();
      });
    }}
  }
  void updateSelection(View focused){
    for(int r=0;r<navRows.size();r++){int c=navRows.get(r).indexOf(focused);
      if(c>=0){navRow=r;navCol=c;return;}}
  }
  boolean moveSelection(int direction){
    if(navRows.size()==0)return false;
    View current=getCurrentFocus();if(current!=null)updateSelection(current);
    if(navRow>=navRows.size())navRow=navRows.size()-1;
    int targetRow=navRow,targetCol=navCol;
    if(direction==View.FOCUS_RIGHT)targetCol++;
    else if(direction==View.FOCUS_LEFT)targetCol--;
    else if(direction==View.FOCUS_DOWN)targetRow++;
    else if(direction==View.FOCUS_UP)targetRow--;
    else return false;
    if(targetRow<0||targetRow>=navRows.size())return false;
    ArrayList<View> destination=navRows.get(targetRow);
    if(destination.isEmpty())return false;
    if(targetCol<0||targetCol>=destination.size()){
      // Stay in the current row at its end rather than jumping unpredictably.
      if(direction==View.FOCUS_LEFT||direction==View.FOCUS_RIGHT)return true;
      targetCol=Math.max(0,Math.min(targetCol,destination.size()-1));
    }
    View target=destination.get(targetCol);navRow=targetRow;navCol=targetCol;
    target.requestFocus();
    // Explicitly scroll newly focused tiles into view on Android 4.4.4 TVs.
    target.post(()->{
      android.graphics.Rect rect=new android.graphics.Rect(0,0,target.getWidth(),target.getHeight());
      target.requestRectangleOnScreen(rect,false);
    });
    return true;
  }
  @Override public boolean onKeyDown(int keyCode,android.view.KeyEvent event){
    if(page.equals("HOME")||page.equals("APPLICATIONS")){
      int d=keyCode==KeyEvent.KEYCODE_DPAD_RIGHT?View.FOCUS_RIGHT:
        keyCode==KeyEvent.KEYCODE_DPAD_LEFT?View.FOCUS_LEFT:
        keyCode==KeyEvent.KEYCODE_DPAD_UP?View.FOCUS_UP:
        keyCode==KeyEvent.KEYCODE_DPAD_DOWN?View.FOCUS_DOWN:0;
      if(d!=0&&moveSelection(d))return true;
    }
    return super.onKeyDown(keyCode,event);
  }

  java.util.List<String> storedOrder(){
    String saved=getPreferences(MODE_PRIVATE).getString("app_order","");
    java.util.List<String> names=new ArrayList<String>();
    if(!saved.isEmpty())for(String p:saved.split(","))if(!p.isEmpty())names.add(p);
    return names;
  }
  void moveApp(String pkg,int direction){
    java.util.List<ResolveInfo> apps=installed();
    java.util.List<String> names=new ArrayList<String>();int index=-1;
    for(int i=0;i<apps.size();i++){String n=apps.get(i).activityInfo.packageName;names.add(n);if(n.equals(pkg))index=i;}
    if(index<0||index+direction<0||index+direction>=names.size())return;
    Collections.swap(names,index,index+direction);
    StringBuilder b=new StringBuilder();for(String n:names){if(b.length()>0)b.append(",");b.append(n);}
    getPreferences(MODE_PRIVATE).edit().putString("app_order",b.toString()).apply();
    arrangeApps();
  }
  void arrangeApps(){
    screen("SORT APPS  •  ترتيب التطبيقات");
    button("↩  Back to HOME",this::home);
    button("↺  Restore alphabetical order",()->{getPreferences(MODE_PRIVATE).edit().remove("app_order").apply();arrangeApps();});
    PackageManager pm=getPackageManager();
    for(ResolveInfo app:installed()){
      String pkg=app.activityInfo.packageName;
      button("▲  "+String.valueOf(app.loadLabel(pm))+"  •  Move earlier",()->moveApp(pkg,-1));
      button("▼  "+String.valueOf(app.loadLabel(pm))+"  •  Move later",()->moveApp(pkg,1));
    }
  }
  void inputSources(){
    screen("HDMI / INPUT  •  المداخل");
    button("SOURCE on Haier physical remote: opens TV source selection",()->{});
    button("Try opening built-in TV input menu",this::openVendorInput);
    button("Start-up input selection: "+(getPreferences(MODE_PRIVATE).getBoolean("show_input_at_start",false)?"ON":"OFF")+" (tap)",()->{
      boolean enabled=getPreferences(MODE_PRIVATE).getBoolean("show_input_at_start",false);
      getPreferences(MODE_PRIVATE).edit().putBoolean("show_input_at_start",!enabled).apply();inputSources();
    });
    button("Note: Detecting active HDMI or switching physical inputs needs Haier firmware support",()->{});
    button("Back to HOME",this::home);
  }
  boolean openVendorInput(){
    PackageManager pm=getPackageManager();
    Intent query=new Intent(Intent.ACTION_MAIN);query.addCategory(Intent.CATEGORY_LAUNCHER);
    java.util.List<ResolveInfo> apps=pm.queryIntentActivities(query,0);
    for(ResolveInfo app:apps){
      String name=String.valueOf(app.loadLabel(pm)).toLowerCase(Locale.US);
      String pkg=app.activityInfo.packageName.toLowerCase(Locale.US);
      if(name.equals("source")||name.equals("input")||name.contains("input source")||name.contains("hdmi")||pkg.endsWith(".tvinput")){
        Intent go=pm.getLaunchIntentForPackage(app.activityInfo.packageName);
        if(go!=null){try{startActivity(go);return true;}catch(Exception ignored){}}
      }
    }
    Toast.makeText(this,"Source selector unavailable — use SOURCE on Haier remote",Toast.LENGTH_LONG).show();
    return false;
  }
  void files(File folder){screen("Files: "+folder.getAbsolutePath());File parent=folder.getParentFile();if(parent!=null)button(".. Back folder",()->files(parent));File[] entries=folder.listFiles();if(entries==null)return;Arrays.sort(entries,(a,b)->a.getName().compareToIgnoreCase(b.getName()));int n=0;for(File f:entries){if(n++>150)break;button((f.isDirectory()?"[DIR] ":"[FILE] ")+f.getName(),()->{if(f.isDirectory())files(f);else{Intent i=new Intent(Intent.ACTION_VIEW);String nm=f.getName().toLowerCase(Locale.US);String mime=nm.endsWith(".apk")?"application/vnd.android.package-archive":nm.endsWith(".mp4")?"video/mp4":nm.endsWith(".mp3")?"audio/mpeg":"*/*";i.setDataAndType(Uri.fromFile(f),mime);try{startActivity(i);}catch(Exception e){Toast.makeText(this,"No compatible app",0).show();}}});}}
  void settings(){screen("Settings");button("Reorder Apps | ترتيب التطبيقات",this::arrangeApps);button("HDMI / Input | المداخل",this::inputSources);button("Wi-Fi",()->launch(new Intent(Settings.ACTION_WIFI_SETTINGS)));button("Display",()->launch(new Intent(Settings.ACTION_DISPLAY_SETTINGS)));button("Sound",()->launch(new Intent(Settings.ACTION_SOUND_SETTINGS)));button("Android Settings",()->launch(new Intent(Settings.ACTION_SETTINGS)));button("Back to HOME",this::home);}
  void launch(Intent i){try{startActivity(i);}catch(Exception e){Toast.makeText(this,"Unavailable",0).show();}}

  String textFile(String path,int maxChars){StringBuilder sb=new StringBuilder();try(BufferedReader reader=new BufferedReader(new FileReader(path))){String line;while((line=reader.readLine())!=null&&sb.length()<maxChars){sb.append(line).append("\n");}}catch(Exception e){return "Unavailable: "+e.getClass().getSimpleName();}return sb.toString().substring(0,Math.min(maxChars,sb.length()));}
  String memorySummary(){String info=textFile("/proc/meminfo",4096);for(String line:info.split("\n"))if(line.startsWith("MemTotal:"))return line.trim();return "Unavailable";}
  String cpuSummary(){String info=textFile("/proc/cpuinfo",6000);StringBuilder sb=new StringBuilder();for(String line:info.split("\n")){String lineLower=line.toLowerCase(Locale.US);if(lineLower.startsWith("processor")||lineLower.startsWith("hardware")||lineLower.startsWith("model name")||lineLower.startsWith("cpu architecture")||lineLower.startsWith("cpu implementer")||lineLower.startsWith("cpu part")||lineLower.startsWith("features")){sb.append(line.trim()).append("\n");if(sb.length()>1300)break;}}return sb.length()>0?sb.toString():"Unavailable";}
  String hardwareReport(){return "HAIDER OS TV 0.3 | Read-only device report\n"
    +"Android: "+Build.VERSION.RELEASE+" (API "+Build.VERSION.SDK_INT+")\n"
    +"Manufacturer: "+Build.MANUFACTURER+"\n"
    +"Brand: "+Build.BRAND+"\n"
    +"Model: "+Build.MODEL+"\n"
    +"Board: "+Build.BOARD+"\n"
    +"Device: "+Build.DEVICE+"\n"
    +"Product: "+Build.PRODUCT+"\n"
    +"Hardware: "+Build.HARDWARE+"\n"
    +"CPU ABI: "+Build.CPU_ABI+"\n"
    +"CPU ABI2: "+Build.CPU_ABI2+"\n"
    +"Kernel: "+System.getProperty("os.version","Unavailable")+"\n"
    +"Build ID: "+Build.ID+"\n"
    +"Build display: "+Build.DISPLAY+"\n"
    +"Fingerprint: "+Build.FINGERPRINT+"\n"
    +"RAM: "+memorySummary()+"\n"
    +"CPU details:\n"+cpuSummary()+"\n"
    +"Note: Hardware properties do not prove firmware compatibility. Do not flash unverified firmware.";
  }
  void diagnostics(){screen("Device Diagnostics 0.2");String report=hardwareReport();button("Copy report to clipboard | نسخ التقرير",()->{android.content.ClipboardManager clipboard=(android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE);clipboard.setPrimaryClip(ClipData.newPlainText("HAIDER TV hardware report",report));Toast.makeText(this,"Report copied",Toast.LENGTH_LONG).show();});for(String line:report.split("\n")){TextView text=new TextView(this);text.setText(line);text.setTextColor(Color.WHITE);text.setTextSize(17);text.setPadding(3,8,3,8);list.addView(text);}button("Back to HOME",this::home);}
  void about(){screen("About");button("Android "+Build.VERSION.RELEASE+" API "+Build.VERSION.SDK_INT,()->{});button("Model: "+Build.MODEL,()->{});button("Hardware: "+Build.HARDWARE,()->{});button("This APK does not update Android firmware",()->{});}
  String ip(){try{WifiManager w=(WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);int a=w.getConnectionInfo().getIpAddress();return (a&255)+"."+((a>>8)&255)+"."+((a>>16)&255)+"."+((a>>24)&255);}catch(Exception e){return "Unknown";}}
  void remote(){screen("Phone Remote");button("TV IP: "+ip()+" Port: 8765",()->{});button("Pairing Token: "+pin,()->{});button("Remote: "+(enabled?"ON":"OFF")+" (tap to toggle)",()->{if(enabled)stopServer();else startServer();remote();});button("Commands: UP DOWN LEFT RIGHT OK BACK HOME",()->{});button("Use X-Haider-Token header and POST /command JSON",()->{});}
  synchronized void startServer(){if(enabled)return;enabled=true;Thread t=new Thread(()->{try(ServerSocket s=new ServerSocket(8765)){listener=s;while(enabled){Socket c=s.accept();try{serve(c);}catch(Exception ignored){}finally{try{c.close();}catch(Exception ignored){}}}}catch(Exception ignored){}finally{enabled=false;listener=null;}});t.setDaemon(true);t.start();}
  synchronized void stopServer(){enabled=false;try{if(listener!=null)listener.close();}catch(Exception ignored){}}
  void serve(Socket c)throws Exception{c.setSoTimeout(2500);if(System.currentTimeMillis()<lockUntil){reply(c,429,"{\"error\":\"try_later\"}");return;}BufferedReader reader=new BufferedReader(new InputStreamReader(c.getInputStream(),"UTF-8"));String first=reader.readLine();if(first==null||first.length()>200)return;boolean authorized=false;int length=0;for(int n=0;n<32;n++){String l=reader.readLine();if(l==null||l.isEmpty())break;if(l.length()>1024)return;String lower=l.toLowerCase(Locale.US);if(lower.startsWith("x-haider-token:"))authorized=l.substring(l.indexOf(':')+1).trim().equals(pin);if(lower.startsWith("content-length:"))try{length=Integer.parseInt(l.substring(l.indexOf(':')+1).trim());}catch(Exception ignored){}}if(!first.startsWith("POST /command ")||!authorized||length<1||length>256){failedAttempts++;if(failedAttempts>=5){lockUntil=System.currentTimeMillis()+60000;failedAttempts=0;}reply(c,401,"{\"error\":\"unauthorized\"}");return;}failedAttempts=0;char[] body=new char[length];int got=0;while(got<length){int k=reader.read(body,got,length-got);if(k<0)break;got+=k;}String input=new String(body,0,got).toUpperCase(Locale.US);String cmd=null;for(String k:new String[]{"UP","DOWN","LEFT","RIGHT","OK","BACK","HOME","VOLUME_UP","VOLUME_DOWN"})if(input.matches("(?s).*\\\"KEY\\\"\\s*:\\s*\\\""+k+"\\\".*")){cmd=k;break;}if(cmd==null){reply(c,400,"{\"error\":\"invalid\"}");return;}final String action=cmd;runOnUiThread(()->perform(action));reply(c,200,"{\"ok\":true}");}
  void reply(Socket c,int status,String data)throws Exception{byte[] b=data.getBytes("UTF-8");c.getOutputStream().write(("HTTP/1.1 "+status+" OK\r\nContent-Type: application/json\r\nContent-Length: "+b.length+"\r\nConnection: close\r\n\r\n").getBytes("US-ASCII"));c.getOutputStream().write(b);}
  // Runs only on the Activity UI thread after an authenticated LAN remote command.
  // Android 4.4 TV devices can route volume to different output streams.
  // A suggested-stream adjustment lets the OS select the active route; STREAM_MUSIC
  // is used as a fallback on old firmwares that do not honor suggested routing.
  void adjustTvVolume(boolean up){
    android.media.AudioManager audio=(android.media.AudioManager)getSystemService(AUDIO_SERVICE);
    if(audio==null){Toast.makeText(this,"TV audio unavailable",Toast.LENGTH_SHORT).show();return;}
    int step=up?android.media.AudioManager.ADJUST_RAISE:android.media.AudioManager.ADJUST_LOWER;
    try{
      int before=audio.getStreamVolume(android.media.AudioManager.STREAM_MUSIC);
      audio.adjustSuggestedStreamVolume(step,android.media.AudioManager.USE_DEFAULT_STREAM_TYPE,android.media.AudioManager.FLAG_SHOW_UI);
      int after=audio.getStreamVolume(android.media.AudioManager.STREAM_MUSIC);
      if(after==before){
        audio.adjustStreamVolume(android.media.AudioManager.STREAM_MUSIC,step,android.media.AudioManager.FLAG_SHOW_UI);
      }
    }catch(SecurityException e){
      Toast.makeText(this,"Volume control restricted by TV firmware",Toast.LENGTH_SHORT).show();
    }catch(RuntimeException e){
      Toast.makeText(this,"Volume control not supported",Toast.LENGTH_SHORT).show();
    }
  }
  void perform(String key){if(key.equals("HOME"))home();else if(key.equals("BACK"))onBackPressed();else if(key.equals("VOLUME_UP")||key.equals("VOLUME_DOWN")){adjustTvVolume(key.equals("VOLUME_UP"));}else if(key.equals("OK")){View v=getCurrentFocus();if(v!=null)v.performClick();}else{int d=key.equals("UP")?View.FOCUS_UP:key.equals("DOWN")?View.FOCUS_DOWN:key.equals("LEFT")?View.FOCUS_LEFT:View.FOCUS_RIGHT;if((page.equals("HOME")||page.equals("APPLICATIONS"))&&moveSelection(d))return;View v=getCurrentFocus();if(v==null){if(list!=null&&list.getChildCount()>0)list.getChildAt(0).requestFocus();}else{View next=v.focusSearch(d);if(next!=null)next.requestFocus();}}}
  @Override public void onBackPressed(){if(!page.equals("HOME"))home();else super.onBackPressed();}
  @Override protected void onDestroy(){stopServer();super.onDestroy();}
}