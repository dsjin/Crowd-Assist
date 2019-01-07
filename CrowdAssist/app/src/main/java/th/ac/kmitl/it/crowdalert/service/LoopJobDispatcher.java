package th.ac.kmitl.it.crowdalert.service;

import android.content.Context;
import android.content.SharedPreferences;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import th.ac.kmitl.it.crowdalert.util.DatabaseHelper;

public class LoopJobDispatcher extends JobService {
    private DatabaseHelper databaseHelper;
    private final String SP_EMERGENCY = "Emergency_information";
    private SharedPreferences sp;
    @Override
    public boolean onStartJob(JobParameters job) {
        init();
        updateDatabase();
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return false;
    }
    private void init(){
        databaseHelper = new DatabaseHelper(LoopJobDispatcher.this);
    }
    private void updateDatabase(){
        sp = getSharedPreferences(SP_EMERGENCY, Context.MODE_PRIVATE);
        int time = sp.getInt("time", 0);
        String emergencyUid = sp.getString("emergency_uid", null);
        if (time != 0){
            databaseHelper.updateEmergencyTime(emergencyUid, time+1);
        }
    }

    //TODO find way to update
}
