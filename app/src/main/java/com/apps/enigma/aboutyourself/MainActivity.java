package com.apps.enigma.aboutyourself;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static com.apps.enigma.aboutyourself.MainActivity.MONTHLY_TAG;
import static com.apps.enigma.aboutyourself.MainActivity.TODAY_TAG;
import static com.apps.enigma.aboutyourself.MainActivity.WEEKLY_TAG;

public class MainActivity extends AppCompatActivity implements AsyncTaskPass {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;


    ///UsageStatsManager usageStatsManager;

    PackageManager packageManager;

    HashMap<String,Long> totalMap;

    public static final String TODAY_TAG="Today";
    public static final String WEEKLY_TAG="Last 7 days";
    public static final String MONTHLY_TAG="Last month";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        toolbar.setTitle(getString(R.string.app_name));

        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);



        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        //usageStatsManager=(UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);

        packageManager = getPackageManager();


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

    @Override
    public void loadUsageData(Map<String, UsageStats> map) {

        int id = mViewPager.getCurrentItem();

        String param=GeneralUtils.getCurrentItemString(id);

        totalMap=GeneralUtils.populateTheMap(map,packageManager);
        Intent intent=new Intent(this,DisplayApps.class);
        intent.putExtra(DisplayApps.INTENT_MAP,totalMap);
        intent.putExtra(DisplayApps.INTENT_TITLE,param);
        startActivity(intent);

    }


    public void clickFab(View v) {

        int id = mViewPager.getCurrentItem();

            String param=GeneralUtils.getCurrentItemString(id);

            new BackGroundTask(this,this).execute(param);
        }

    }

    class BackGroundTask extends AsyncTask<String,Void,Map<String, UsageStats>>{

        AsyncTaskPass passInterface;
        Context context;

        public BackGroundTask(AsyncTaskPass context,Context mainContext){
            passInterface=context;
            this.context=mainContext;
        }

        @Override
        protected Map<String, UsageStats> doInBackground(String... params) {

            Calendar cal ;
            cal=Calendar.getInstance();
            switch(params[0]){
                case TODAY_TAG:{

                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 1);
                    cal.set(Calendar.SECOND, 1);
                    cal.set(Calendar.MILLISECOND, 1);
                    break;
                }case WEEKLY_TAG:{
                    cal.add(Calendar.DATE, -7);
                    break;
                }case MONTHLY_TAG:{
                    cal.add(Calendar.MONTH, -1);
                    break;
                }default:{

                }
            }



            UsageStatsManager usageStatsManager=(UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);;
            return usageStatsManager.queryAndAggregateUsageStats(cal.getTimeInMillis(),Calendar.getInstance().getTimeInMillis());
        }

        @Override
        protected void onPostExecute(Map<String, UsageStats> stringUsageStatsMap) {
            passInterface.loadUsageData(stringUsageStatsMap);
        }
    }


    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/

    /**
     * A placeholder fragment containing a simple view.
     */


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position){
                case 0:{


                    return  TodayFragment.newInstance("","");
                }
                case 1:{


                    return YesterdayFragment.newInstance("","");
                }
                case 2:{
                    return TomorrowFragment.newInstance("","");
                }
                default:{
                    return null;
                }
            }



        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Today";
                case 1:
                    return "Last 7 days";
                case 2:
                    return "Last month";
            }
            return null;
        }




    }





