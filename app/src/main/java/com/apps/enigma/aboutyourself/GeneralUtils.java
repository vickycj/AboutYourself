package com.apps.enigma.aboutyourself;

import android.app.usage.UsageStats;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by Vicky cj on 21-10-2017.
 */

public class GeneralUtils {


    public static HashMap<String,Long> populateTheMap(Map<String, UsageStats> map,PackageManager packageManager) {


        HashMap<String,Long> mapNew=new HashMap<String,Long>();


        String packageName;
        String appName;
        long usageTime;
        for ( Map.Entry<String, UsageStats> entry : map.entrySet()) {
            packageName=entry.getKey();

            if(isPackageInstalled(packageName,packageManager)){
                usageTime=entry.getValue().getTotalTimeInForeground();
                appName=getAppName(packageName,packageManager);

                mapNew.put(appName,usageTime);
            }
        }


        return mapNew;
    }

    public static List<Map<String, String>> topFiveConvert(HashMap<String,Long> map){

        List<Map<String, String>> data = new ArrayList<Map<String, String>>();
        for (Map.Entry<String, Long> entry : map.entrySet()) {

            Map<String, String> datum = new HashMap<String, String>(2);
            datum.put("title", entry.getKey());
            datum.put("time", convertLongTime(entry.getValue()));
            data.add(datum);
        }


        return data;
    }


    private static String getAppName(String packagename, PackageManager packageManager){

        ApplicationInfo ai;
        try {
            ai = packageManager.getApplicationInfo( packagename, 0);
        } catch (final PackageManager.NameNotFoundException e) {
            ai = null;
        }
        final String applicationName = (String) (ai != null ? packageManager.getApplicationLabel(ai) : "(unknown)");
        return applicationName;
    }

    private static boolean isPackageInstalled(String packagename, PackageManager packageManager) {

        try {
            packageManager.getPackageInfo(packagename, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }


    public static HashMap<String,String>  takeTopFive(Map<String,Long> map) {

        HashMap<String,String> mapNew=new HashMap<>();
        int count=0;
        for (Map.Entry<String, Long> entry : map.entrySet()) {

            if(count>4){break;}
            mapNew.put(entry.getKey(),convertLongTime(entry.getValue()));
            count++;
        }


        return mapNew;
    }


    public static String convertLongTime(long l){

        String time="";

        String days=getDaysFromLong(l);

        String hours=getHoursFromLong(l);

        String minutes=getMinutesFromLong(l);

        if(!days.equals("00")){
            time+=days+"d";
        }
        if(!hours.equals("00")){
            time+=hours+"h";
        }

        if(!minutes.equals("00")){
            time+=minutes+"m";
        }

        return  time;

    }


    public static String getDaysFromLong(long l){
        long days=TimeUnit.MILLISECONDS.toDays(l);
        if(days<0){
            return "00";
        }else if(days<=9){
            return "0"+days;
        }

        return days+"";
    }

    public static String getHoursFromLong(long l){
        long days=TimeUnit.MILLISECONDS.toDays(l);
        if(days>0){
            l -= TimeUnit.DAYS.toMillis(days);
        }
        long hours = TimeUnit.MILLISECONDS.toHours(l);

        if(hours<0){
            return "00";
        }else if(hours<=9){
            return "0"+hours;
        }

        return hours+"";
    }


    public static String getMinutesFromLong(long l){
        long days=TimeUnit.MILLISECONDS.toDays(l);
        if(days>0){
            l -= TimeUnit.DAYS.toMillis(days);
        }
        long hours = TimeUnit.MILLISECONDS.toHours(l);

        if(hours>0){
            l -= TimeUnit.HOURS.toMillis(hours);
        }

        long minutes = TimeUnit.MILLISECONDS.toMinutes(l);

        if(minutes<0){
            return "00";
        }else if(minutes<=9){
            return "0"+minutes;
        }

        return minutes+"";
    }


    public static double calculatePercent(long j){


        long time = 86400000;


        double percentage=((double)j/(double)time)*100;

        double finalValue = Math.round( percentage * 100.0 ) / 100.0;

        return finalValue;
    }

    public static long calculateTotalTime(Map<String,Long> map){


        long totalTime=0;
        for (Map.Entry<String, Long> entry : map.entrySet()) {

            totalTime+=entry.getValue();

        }
        return totalTime;
    }

    public static Map<String, Long> sortMapByValue(HashMap<String, Long> map){
        Comparator<String> compar = new ValueComparator(map);

        Map<String, Long> result = new TreeMap<String, Long>(compar);
        result.putAll(map);
        return result;
    }


    static class ValueComparator implements Comparator<String>{

        HashMap<String, Long> map = new HashMap<String, Long>();

        public ValueComparator(HashMap<String, Long> map){
            this.map.putAll(map);
        }

        @Override
        public int compare(String s1, String s2) {
            if(map.get(s1) >= map.get(s2)){
                return -1;
            }else{
                return 1;
            }
        }
    }

}
