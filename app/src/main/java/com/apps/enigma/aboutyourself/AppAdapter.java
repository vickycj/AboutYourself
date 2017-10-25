package com.apps.enigma.aboutyourself;

import android.app.usage.UsageStats;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Vicky cj on 26-10-2017.
 */

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.MyViewHolder>{


    List<String> topFiveKey;
    HashMap<String,String> topFiveMap;
    Context context;
    PackageManager packageManager;



    public AppAdapter(HashMap<String, String> topFiveMap, Context context){
        topFiveKey=new ArrayList<>(topFiveMap.keySet());
        this.topFiveMap=topFiveMap;
        this.context=context;
        packageManager=context.getPackageManager();

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.apps_list, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
            String packageName=topFiveKey.get(position);
            String time= topFiveMap.get(packageName);
            String appName=GeneralUtils.getAppName(packageName,packageManager);

            holder.mAppName.setText(appName);
            holder.mAppUsageTime.setText(time);
            Drawable icon;
            icon=GeneralUtils.getAppIcon(packageName,packageManager);
            if(icon!=null){
                holder.mAppIcon.setImageDrawable(icon);
            }

    }

    @Override
    public int getItemCount() {
        return topFiveKey.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView mAppName,mAppUsageTime;

        ImageView mAppIcon;

        public MyViewHolder(View itemView) {
            super(itemView);
            mAppName= itemView.findViewById(R.id.app_name);
            mAppUsageTime= itemView.findViewById(R.id.app_usage_time);
            mAppIcon= itemView.findViewById(R.id.app_icon);


        }
    }
}
