package com.haider.remote;
import android.app.*;import android.os.*;import android.content.*;import android.graphics.*;import android.graphics.drawable.*;import android.view.*;import android.widget.*;import java.net.*;import java.io.*;
public class RemoteActivity extends Activity {
 int black=Color.rgb(16,17,21),gold=Color.rgb(211,174,108);
 EditText ip,pin;TextView status;android.content.SharedPreferences prefs;
 int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+0.5f);}
 GradientDrawable bg(int c){GradientDrawable g=new GradientDrawable();g.setColor(c);g.setCornerRadius(dp(17));return g;}
 TextView label(String s,int size){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(gold);t.setGravity(Gravity.CENTER);return t;}
 LinearLayout row(LinearLayout parent){LinearLayout r=new LinearLayout(this);r.setOrientation(LinearLayout.HORIZONTAL);parent.addView(r,new LinearLayout.LayoutParams(-1,dp(70)));return r;}
 void key(LinearLayout r,String title,String cmd){TextView b=label(title,23);b.setBackground(bg(black));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-1,1);lp.setMargins(dp(4),dp(4),dp(4),dp(4));r.addView(b,lp);b.setOnClickListener(v->send(cmd));}
 @Override public void onCreate(Bundle saved){super.onCreate(saved);prefs=getSharedPreferences("haider_remote",0);
 ScrollView scroll=new ScrollView(this);LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setPadding(dp(18),dp(15),dp(18),dp(15));root.setBackgroundColor(Color.rgb(246,243,235));scroll.addView(root);setContentView(scroll);
 TextView title=label("♛ HAIDER REMOTE",30);title.setTextColor(black);root.addView(title);
 TextView helper=label("Wi-Fi • HAIDER OS TV • 6-digit pairing",15);helper.setTextColor(Color.DKGRAY);root.addView(helper);
 ip=new EditText(this);ip.setSingleLine(true);ip.setHint("TV IP — e.g. 192.168.100.63");ip.setText(prefs.getString("ip",""));root.addView(ip);
 pin=new EditText(this);pin.setSingleLine(true);pin.setHint("Six-digit pairing code");pin.setInputType(2);pin.setText(prefs.getString("pin",""));root.addView(pin);
 TextView save=label("SAVE CONNECTION",18);save.setBackground(bg(black));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(54));p.setMargins(0,dp(9),0,dp(9));root.addView(save,p);save.setOnClickListener(v->save());
 status=label("Open Phone Remote on TV and enable it",14);status.setTextColor(black);root.addView(status);
 LinearLayout a=row(root);key(a,"HOME","HOME");key(a,"BACK","BACK");
 LinearLayout b=row(root);key(b," ","NOOP");key(b,"▲","UP");key(b," ","NOOP");
 LinearLayout c=row(root);key(c,"◀","LEFT");key(c,"OK","OK");key(c,"▶","RIGHT");
 LinearLayout d=row(root);key(d," ","NOOP");key(d,"▼","DOWN");key(d," ","NOOP");
 LinearLayout e=row(root);key(e,"VOL +","VOLUME_UP");key(e,"VOL −","VOLUME_DOWN");
 TextView note=label("Only controls HAIDER OS TV app; not protected Haier HDMI hardware settings.",13);note.setTextColor(Color.DKGRAY);root.addView(note);
 }
 void save(){String host=ip.getText().toString().trim(),code=pin.getText().toString().trim();
 if(!host.matches("(\\d{1,3}\\.){3}\\d{1,3}")||!code.matches("\\d{6}")){status.setText("Check IP and 6-digit code");return;}
 for(String piece:host.split("\\."))if(Integer.parseInt(piece)>255){status.setText("Invalid IP");return;}
 prefs.edit().putString("ip",host).putString("pin",code).apply();status.setText("Connection saved");
 }
 void send(String command){if(command.equals("NOOP"))return;String host=ip.getText().toString().trim(),code=pin.getText().toString().trim();
 if(!host.matches("(\\d{1,3}\\.){3}\\d{1,3}")||!code.matches("\\d{6}")){status.setText("Enter the TV IP and pairing code");return;}
 save();status.setText("Sending "+command);
 new Thread(()->{HttpURLConnection connection=null;String msg;
 try{URL url=new URL("http://"+host+":8765/command");connection=(HttpURLConnection)url.openConnection();
 connection.setRequestMethod("POST");connection.setDoOutput(true);connection.setConnectTimeout(3000);connection.setReadTimeout(3000);
 connection.setRequestProperty("X-Haider-Token",code);connection.setRequestProperty("Content-Type","application/json");
 byte[] bytes=("{\"key\":\""+command+"\"}").getBytes("UTF-8");connection.setFixedLengthStreamingMode(bytes.length);
 OutputStream out=connection.getOutputStream();out.write(bytes);out.close();
 int response=connection.getResponseCode();msg=response==200?"✓ "+command+" sent":response==401?"Pairing code rejected":"TV error "+response;
 }catch(Exception ex){msg="Connection failed: "+ex.getClass().getSimpleName();}finally{if(connection!=null)connection.disconnect();}
 final String value=msg;runOnUiThread(()->status.setText(value));
 }).start();
 }
}