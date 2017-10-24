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

import me.itangqi.waveloadingview.WaveLoadingView;

public class MainActivity extends AppCompatActivity implements AsyncTaskPass {

    UsageStatsManager usageStatsManager;

    private static final int INTERVAL_TIME=UsageStatsManager.INTERVAL_BEST;

    PackageManager packageManager ;
    ApplicationInfo applicationInfo ;
    AppOpsManager appOpsManager ;

    TextView mtotalUsageTime;

    WaveLoadingView mwaveLoadingView;

    ListView listView;

    ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.checking);




       /* usageStatsManager=(UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);

        packageManager = getPackageManager();

        appOpsManager = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);

        mtotalUsageTime=(TextView) findViewById(R.id.tv_total_usage_time);


        mwaveLoadingView =(WaveLoadingView) findViewById(R.id.wave_loading_view);

        listView=(ListView) findViewById(R.id.lv_top5_apps);

        progressBar=(ProgressBar) findViewById(R.id.progress_bar);


        if(!isAccessGranted()){
            visiblePermission();
        }else{
            inVisiblePermission();
            startAsyncTask();
        }*/



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
        HashMap<String,Long> topFiveMap;
        Map<String,Long> sortedMap;

        totalMap=GeneralUtils.populateTheMap(map,packageManager);

        sortedMap=GeneralUtils.sortMapByValue(totalMap);

        topFiveMap=GeneralUtils.takeTopFive(sortedMap);

        long calculateTotalTime=GeneralUtils.calculateTotalTime(totalMap);

        double percent=GeneralUtils.calculatePercent(calculateTotalTime);

        String convertedTime=GeneralUtils.convertLongToHours(calculateTotalTime);

        setTotalTime(convertedTime);

        setPercentage(percent);
        setTopFiveApp(topFiveMap);

        cancelLoading();


    }



    public void setTopFiveApp(HashMap<String,Long> topFiveMap){
        List<Map<String, String>> data;

        data=GeneralUtils.topFiveConvert(topFiveMap);

        SimpleAdapter adapter = new SimpleAdapter(this, data,
                android.R.layout.simple_list_item_2,
                new String[] {"title", "time"},
                new int[] {android.R.id.text1,
                        android.R.id.text2});
        listView.setAdapter(adapter);
    }


    public void setTotalTime(String text){
        mtotalUsageTime.setText(text);
    }

    public void setPercentage(double val){
        int i=(int) val;
        mwaveLoadingView.setProgressValue(i);
        if(i<50){
            mwaveLoadingView.setCenterTitle("");
            mwaveLoadingView.setTopTitle("");
            mwaveLoadingView.setBottomTitle(String.format("%d%%",i));

        }else if(i<80){
            mwaveLoadingView.setCenterTitle(String.format("%d%%",i));
            mwaveLoadingView.setTopTitle("");
            mwaveLoadingView.setBottomTitle("");
        }else{
            mwaveLoadingView.setCenterTitle("");
            mwaveLoadingView.setTopTitle(String.format("%d%%",i));
            mwaveLoadingView.setBottomTitle("");
        }
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
            cal.add(Calendar.DATE, -1);

            return usageStatsManager.queryAndAggregateUsageStats(cal.getTimeInMillis(),System.currentTimeMillis());
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
