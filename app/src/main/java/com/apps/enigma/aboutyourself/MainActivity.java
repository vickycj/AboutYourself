package com.apps.enigma.aboutyourself;

import android.app.AppOpsManager;
import android.app.ProgressDialog;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class MainActivity extends AppCompatActivity implements AsyncTaskPass {

    UsageStatsManager usageStatsManager;

    private static final int INTERVAL_TIME=UsageStatsManager.INTERVAL_BEST;

    PackageManager packageManager ;
    ApplicationInfo applicationInfo ;
    AppOpsManager appOpsManager ;


    TextView mDays;

    TextView mHours;

    TextView mMinutes;


    TextView percentText;

    ProgressBar mPercentProgress;

    ProgressBar progressBar;

    RecyclerView mRecyclerView;

    AppAdapter appAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


         mDays=(TextView) findViewById(R.id.tv_days);

        mHours=(TextView) findViewById(R.id.tv_hours);

        mMinutes=(TextView) findViewById(R.id.tv_minutes);


        percentText=(TextView) findViewById(R.id.tv_percent_text);

        mPercentProgress=(ProgressBar) findViewById(R.id.pb_percentage_of_day);

        progressBar=(ProgressBar) findViewById(R.id.loading_bar);


        mRecyclerView=(RecyclerView) findViewById(R.id.recycler_view_main);



        usageStatsManager=(UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);

        appOpsManager = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        packageManager = getPackageManager();







        if(!isAccessGranted()){
            visiblePermission();
        }else{



            inVisiblePermission();
            startAsyncTask();
        }



    }

    @Override
    protected void onResume() {
        super.onResume();


    }

    public void startAsyncTask(){
        showLoading();
        new BackGroundTask(this).execute();
    }


    @Override
    public void loadUsageData(Map<String, UsageStats> map) {
        HashMap<String,Long> totalMap;
        HashMap<String,String> topFiveMap;
        Map<String,Long> sortedMap;

        totalMap=GeneralUtils.populateTheMap(map,packageManager);

        sortedMap=GeneralUtils.sortMapByValue(totalMap);

        topFiveMap=GeneralUtils.takeTopFive(sortedMap);

        long calculateTotalTime=GeneralUtils.calculateTotalTime(totalMap);

        double percent=GeneralUtils.calculatePercent(calculateTotalTime);



        setTime(calculateTotalTime);

        setProgressBar(percent);

        setTopFive(topFiveMap);


        cancelLoading();


    }


    public void setTopFive( HashMap<String,String> topFiveMap){

        appAdapter=new AppAdapter(topFiveMap,this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(appAdapter);
    }

public void setTime(long time){
    mDays.setText(GeneralUtils.getDaysFromLong(time));
    mHours.setText(GeneralUtils.getHoursFromLong(time));
    mMinutes.setText(GeneralUtils.getMinutesFromLong(time));
}

public void setProgressBar(double percent){

    int per= (int) percent;
    mPercentProgress.setProgress(per);
    percentText.setText(per +"% of your total time");
}





    public class BackGroundTask extends AsyncTask<Void,Void,Map<String, UsageStats>>{



        public AsyncTaskPass passInterface;


        public BackGroundTask(AsyncTaskPass context){
            passInterface=context;
        }

        @Override
        protected Map<String, UsageStats> doInBackground(Void... params) {
            Calendar cal ;
            cal=Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 1);
            cal.set(Calendar.SECOND, 1);
            cal.set(Calendar.MILLISECOND, 1);



            return usageStatsManager.queryAndAggregateUsageStats(cal.getTimeInMillis(),Calendar.getInstance().getTimeInMillis());
        }

        @Override
        protected void onPostExecute(Map<String, UsageStats> stringUsageStatsMap) {
            passInterface.loadUsageData(stringUsageStatsMap);
        }
    }

    private boolean isAccessGranted() {
        try {
            applicationInfo = packageManager.getApplicationInfo(getPackageName(), 0);
            int mode = 0;
            mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                        applicationInfo.uid, applicationInfo.packageName);

            return (mode == AppOpsManager.MODE_ALLOWED);

        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }



    public void visiblePermission(){
        LinearLayout linearLayout=(LinearLayout) findViewById(R.id.permission_linear_layout);

        linearLayout.setVisibility(View.VISIBLE);

    }

    public void inVisiblePermission(){
        LinearLayout linearLayout=(LinearLayout) findViewById(R.id.permission_linear_layout);

        linearLayout.setVisibility(View.GONE);

    }

    public void enablePermission(View v){
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Usage Access")
                .setMessage("App will not run without usage access permissions.")
                .setPositiveButton("Settings", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {

                        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                         //intent.setComponent(new ComponentName("com.android.settings","com.android.settings.Settings$SecuritySettingsActivity"));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivityForResult(intent,0);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .create();


        alertDialog.show();
    }



    public void showLoading(){
        progressBar.setVisibility(View.VISIBLE);
    }

    public void cancelLoading(){
        progressBar.setVisibility(View.GONE);
    }
}
