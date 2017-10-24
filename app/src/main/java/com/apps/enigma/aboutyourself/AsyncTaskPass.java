package com.apps.enigma.aboutyourself;

import android.app.usage.UsageStats;

import java.util.Map;

/**
 * Created by Vicky cj on 21-10-2017.
 */

public interface AsyncTaskPass {

    void loadUsageData(Map<String, UsageStats> map);
}
