package com.apps.enigma.aboutyourself;


import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TodayFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TodayFragment extends Fragment implements AsyncTaskPass {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    private String mParam1;
    private String mParam2;


    UsageStatsManager usageStatsManager;

    private static final int INTERVAL_TIME=UsageStatsManager.INTERVAL_BEST;

    PackageManager packageManager ;
    ApplicationInfo applicationInfo ;
    AppOpsManager appOpsManager ;

    Context context;
    TextView mDays;

    TextView mHours;

    TextView mMinutes;


    TextView percentText;

    ProgressBar mPercentProgress;

    ProgressBar progressBar;

    RecyclerView mRecyclerView;

    AppAdapter appAdapter;

    View mainView;

    Context mainActivityContext;

    HashMap<String,Long> intentMap;



    public TodayFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TodayFragment.
     */

    public static TodayFragment newInstance(String param1, String param2) {
        TodayFragment fragment = new TodayFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=getContext();
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView=inflater.inflate(R.layout.today_layout,container,false);

        mainView=rootView;
        mDays=(TextView) rootView.findViewById(R.id.tv_days);

        mHours=(TextView) rootView.findViewById(R.id.tv_hours);

        mMinutes=(TextView) rootView.findViewById(R.id.tv_minutes);


        percentText=(TextView) rootView.findViewById(R.id.tv_percent_text);

        mPercentProgress=(ProgressBar) rootView.findViewById(R.id.pb_percentage_of_day);

        progressBar=(ProgressBar) rootView.findViewById(R.id.loading_bar);


        mRecyclerView=(RecyclerView) rootView.findViewById(R.id.recycler_view_main);



        usageStatsManager=(UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);

        appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        packageManager = context.getPackageManager();






        if(!isAccessGranted()){
            visiblePermission();
        }else{



            inVisiblePermission();
            startAsyncTask();
        }


        return rootView;
    }



    @Override
    public void onResume() {
        super.onResume();
        if(!isAccessGranted()){
            visiblePermission();
        }else{



            inVisiblePermission();
            startAsyncTask();
        }

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

        intentMap=totalMap;

        topFiveMap=GeneralUtils.takeTopFive(sortedMap);

        long calculateTotalTime=GeneralUtils.calculateTotalTime(totalMap);

        double percent=GeneralUtils.calculatePercent(calculateTotalTime,GeneralUtils.ONE_DAY_MILLIS);



        setTime(calculateTotalTime);

        setProgressBar(percent);

        setTopFive(topFiveMap);


        cancelLoading();


    }


    public void setTopFive( HashMap<String,String> topFiveMap){

        appAdapter=new AppAdapter(topFiveMap,context);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(context);
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




    public class BackGroundTask extends AsyncTask<Void,Void,Map<String, UsageStats>> {



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
            applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
            int mode = 0;
            mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                    applicationInfo.uid, applicationInfo.packageName);

            return (mode == AppOpsManager.MODE_ALLOWED);

        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }



    public void visiblePermission(){
        LinearLayout linearLayout=(LinearLayout) mainView.findViewById(R.id.permission_linear_layout);

        linearLayout.setVisibility(View.VISIBLE);

    }

    public void inVisiblePermission(){
        LinearLayout linearLayout=(LinearLayout) mainView.findViewById(R.id.permission_linear_layout);

        linearLayout.setVisibility(View.GONE);

    }

    public void enablePermission(View v){
        AlertDialog alertDialog = new AlertDialog.Builder(context)
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
