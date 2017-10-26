package com.apps.enigma.aboutyourself;

import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

public class DisplayApps extends AppCompatActivity {


    RecyclerView mRecyclerView;

    AppAdapter appAdapter;



    public static final String INTENT_MAP="map-data";

    public static final String INTENT_TITLE="title";
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_display_apps);

        Intent intent=getIntent();

        String title=intent.getStringExtra(INTENT_TITLE);

        if(title !=null){
            textView=(TextView) findViewById(R.id.tv_display_app_text);
            textView.setText(title);
        }





        HashMap<String,Long> totalMap;
        HashMap<String,String> allMap;
        Map<String,Long> sortedMap;

        totalMap=(HashMap<String,Long>) intent.getSerializableExtra(INTENT_MAP);


        if(totalMap!=null){
            sortedMap=GeneralUtils.sortMapByValue(totalMap);

            allMap=GeneralUtils.convertMapTimeTOString(sortedMap);
            mRecyclerView=(RecyclerView) findViewById(R.id.recycler_view_main);
            appAdapter=new AppAdapter(allMap,this);
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
            mRecyclerView.setLayoutManager(mLayoutManager);
            mRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mRecyclerView.setAdapter(appAdapter);
        }


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
               onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
