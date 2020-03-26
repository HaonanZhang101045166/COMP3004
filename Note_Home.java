package com.example.zhu.note;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class Note_Home extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener{

    @BindView(R.id.list)ListView listView;
    @BindView(R.id.icon)ImageView icon;
    @BindView(R.id.fab)FloatingActionButton fab;


    private Context mcontext;
    Note_Adapter note_adapter;
    List<Note_table> list = new ArrayList<>();

    //定时变量
    int alarm_hour;
    int alarm_minute;
    int alarm_year;
    int alarm_month;
    int alarm_day;

    String alarm_date = "";
    //这里设置点击回调处理时间
    private BroadcastAlarm broadcastAlarm;
    private MyReceiver myReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_home);

        mcontext = this;
        ButterKnife.bind(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.android.deskclock.ALARM_ALERT");//android alarm broadcast
        filter.addAction("com.sonyericsson.alarm.ALARM_ALERT");//sony alarm broadcast
        filter.addAction("com.samsung.sec.android.clockpackage.alarm.ALARM_ALERT");//samsung alarm broadcast
        filter.addAction("com.cn.google.AlertClock.ALARM_ALERT");//vivo alarm broadcast
        filter.addAction("com.oppo.alarmclock.alarmclock.ALARM_ALERT");//oppo alarm broadcast
        filter.addAction("com.zdworks.android.zdclock.ACTION_ALARM_ALERT");//ZTE alarm broadc
        filter.addAction(getPackageName());

        broadcastAlarm = new BroadcastAlarm();
        //注册本地接收器
        registerReceiver(broadcastAlarm,filter);
        listview_click();  //listview点击事件
        listview_longPress(); //listview长按事件

        IntentFilter intentFilter1 = new IntentFilter();
        intentFilter1.addAction("net.deniro.android.MY_BROADCAST");
        intentFilter1.addAction(getPackageName());
        myReceiver = new MyReceiver();
        registerReceiver(myReceiver, intentFilter1);

        //naozhong_icon();
    }

   static public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("---------","MyReceiver");

            new SweetAlertDialog(context, SweetAlertDialog.NORMAL_TYPE)
                    .setTitleText("Wake up ")
                    .setContentText(" alarm clock goes off")
                    .setConfirmText("OK")
                    .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            sDialog.dismissWithAnimation();
                        }
                    })
                    .show();


        }
    }

    //添加新备忘
    @OnClick(R.id.fab)
    public void fab(View view){
        Intent intent = new Intent(mcontext,Note_Edit.class);
        startActivityForResult(intent,1);
    }
    //天气
    @OnClick(R.id.icon)
    public void icon(View view){
        Intent intent = new Intent(mcontext,Weather_Home.class);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        list.clear();
        Data_loading();
//        Log.d("---------","onStart"+ list.get(0).getTime());

        note_adapter = new Note_Adapter(mcontext,(ArrayList<Note_table>)list);

        listView.setAdapter(note_adapter);
        naozhong_icon();
    }


    //从数据库中加载数据
    private void Data_loading(){
        list = DataSupport.findAll(Note_table.class);
    }

    //listview点击事件
    private void listview_click(){
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Note_Info note_info =  new Note_Info();

                note_info.setId(list.get(position).getId());
                note_info.setColor_key(list.get(position).getColor_key());
                note_info.setContent(list.get(position).getContent());
                note_info.setAlarm_key(list.get(position).getAlarm_key());
                note_info.setTime(list.get(position).getTime());

                Intent intent = new Intent(mcontext,Note_Edit.class);
                intent.putExtra("note_table_data", note_info);
                startActivity(intent);

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == 2) {
            if (requestCode == 1) {

                //定时变量
                int alarm_hour;
                int alarm_minute;
                int alarm_year;
                int alarm_month;
                int alarm_day;
                int i=0, k=0;


                String alarm_date = data.getStringExtra("alarm_date");

                if(alarm_date.length()>1){
                    int id = data.getIntExtra("Id",0);
                    while(i<alarm_date.length()&&alarm_date.charAt(i)!='/') i++;
                    alarm_year=Integer.parseInt(alarm_date.substring(k,i));
                    k=i+1;i++;
                    while(i<alarm_date.length()&&alarm_date.charAt(i)!='/') i++;
                    alarm_month=Integer.parseInt(alarm_date.substring(k,i));
                    k=i+1;i++;
                    while(i<alarm_date.length()&&alarm_date.charAt(i)!=' ') i++;
                    alarm_day=Integer.parseInt(alarm_date.substring(k,i));
                    k=i+1;i++;
                    while(i<alarm_date.length()&&alarm_date.charAt(i)!=':') i++;
                    alarm_hour=Integer.parseInt(alarm_date.substring(k,i));
                    k=i+1;i++;
                    alarm_minute=Integer.parseInt(alarm_date.substring(k));
//                    Log.d("----alarm_hour------","alarm_hour"+alarm_hour);
//                    Log.d("----alarm_minute------","alarm_minute"+alarm_minute);
//                    Log.d("----alarm_year------","alarm_year"+alarm_year);
//                    Log.d("----alarm_month------","alarm_month"+alarm_month);
//                    Log.d("----alarm_day------","alarm_day"+alarm_day);

                    //从数据库中拿到alarm_date数据进行定时操作
                    Intent intent = new Intent(mcontext, BroadcastAlarm.class);
                    intent.putExtra("alarmId",id);
                    PendingIntent sender = PendingIntent.getBroadcast(mcontext,id, intent, 0);

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(System.currentTimeMillis());
                    //calendar.add(Calendar.SECOND, 5);

                    Calendar alarm_time = Calendar.getInstance();
                    calendar.set(Calendar.SECOND, 0);

                    alarm_time.set(alarm_year,alarm_month-1,alarm_day,alarm_hour,alarm_minute,0);

                    // Schedule the alarm!
                    AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);

                    //if(interval==0)
                    am.set(AlarmManager.RTC_WAKEUP, alarm_time.getTimeInMillis(), sender);
                }
            }
        }
    }

    //listview长按事件
    private void listview_longPress(){
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,final int position, long id) {
                AlertDialog alert = null;
                AlertDialog.Builder builder  = new AlertDialog.Builder(mcontext);

                alert = builder.setIcon(R.drawable.jinggao)
                        .setTitle("System Prompt：")
                        .setMessage("Would you like to delete this memo？")
                        .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(mcontext,"cancel～",Toast.LENGTH_SHORT).show();
                            }

                        })
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    cancelAlarm(position);
                                    Note_table note_table = DataSupport.find(Note_table.class,list.get(position).getId());
                                    note_table.delete();
                                    Data_loading();
                                    note_adapter = new Note_Adapter(mcontext,(ArrayList<Note_table>)list);
                                    listView.setAdapter(note_adapter);
                                    Toast.makeText(mcontext,"success ～",Toast.LENGTH_SHORT).show();

                                }
                            }).create();

                alert.show();

                return true;
            }
        });
    }

    //cancel the alarm
    private void cancelAlarm(int num) {


        Intent intent = new Intent(mcontext,BroadcastAlarm.class);
        PendingIntent sender = PendingIntent.getBroadcast(mcontext,list.get(num).getId(),intent, 0);

        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.cancel(sender);
        Log.d("闹钟取消","取消取消");
    }

    public void naozhong_icon(){

        if(alarm_date.length()<=1) {
            //if no alarm clock has been set up before
            //show the current time
            Calendar c=Calendar.getInstance();
            alarm_hour=c.get(Calendar.HOUR_OF_DAY);
            alarm_minute=c.get(Calendar.MINUTE);

            alarm_year=c.get(Calendar.YEAR);
            alarm_month=c.get(Calendar.MONTH)+1;
            alarm_day=c.get(Calendar.DAY_OF_MONTH);
            Log.d("月份",String.valueOf(alarm_month));
        }
        else {
            //show the alarm clock time which has been set up before
            int i=0, k=0;
            while(i<alarm_date.length()&&alarm_date.charAt(i)!='/') i++;
            alarm_year=Integer.parseInt(alarm_date.substring(k,i));
            k=i+1;i++;
            while(i<alarm_date.length()&&alarm_date.charAt(i)!='/') i++;
            alarm_month=Integer.parseInt(alarm_date.substring(k,i));
            k=i+1;i++;
            while(i<alarm_date.length()&&alarm_date.charAt(i)!=' ') i++;
            alarm_day=Integer.parseInt(alarm_date.substring(k,i));
            k=i+1;i++;
            while(i<alarm_date.length()&&alarm_date.charAt(i)!=':') i++;
            alarm_hour=Integer.parseInt(alarm_date.substring(k,i));
            k=i+1;i++;
            alarm_minute=Integer.parseInt(alarm_date.substring(k));
        }

        //new TimePickerDialog(this,this,alarm_hour,alarm_minute,true).show();
        //new DatePickerDialog(this,this,alarm_year,alarm_month-1,alarm_day).show();
        //new customDateDialog(this,this,alarm_year,alarm_month-1,alarm_day).show();

    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        alarm_year=year;
        alarm_month=monthOfYear+1;
        alarm_day=dayOfMonth;
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        alarm_hour=hourOfDay;
        alarm_minute=minute;

        alarm_date=alarm_year+"/"+alarm_month+"/"+alarm_day+" "+alarm_hour+":"+alarm_minute;
//        av.setText("Alert at "+alarm+"!");
//        av.setVisibility(View.VISIBLE);
//        Toast.makeText(this,"",Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(broadcastAlarm);
        super.onDestroy();
    }

//    @OnClick(R.id.icon_fanhui)
//    public void icon_fanhui(View view){
//        finish();
//
//    }
}
