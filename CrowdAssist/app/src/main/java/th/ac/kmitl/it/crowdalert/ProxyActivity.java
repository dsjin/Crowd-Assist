package th.ac.kmitl.it.crowdalert;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.UUID;

import th.ac.kmitl.it.crowdalert.service.LocationJobDispatcher;

public class ProxyActivity extends Activity {
    private final String PROFILE_SP = "profile";
    private SharedPreferences sp;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private static Integer SIGN_IN = 9001;
    private static Integer MAIN = 9002;
    private FirebaseJobDispatcher mDispatcher;
    private Job locationJob;
    private FirebaseDatabase mDatabase;
    private FirebaseAnalytics mFirebaseAnalytics;
    private Bundle bundle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mDispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));
        sp = getSharedPreferences(PROFILE_SP, Context.MODE_PRIVATE);
        if (sp.getString("device", null) == null){
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("device", UUID.randomUUID().toString());
            editor.apply();
        }
        locationJob = mDispatcher.newJobBuilder()
                .setService(LocationJobDispatcher.class)
                .setTag("locationJob")
                .setRecurring(true)
                .setTrigger(Trigger.executionWindow(40, 60))
                //TODO Set Lifetime mai na ja
                .setLifetime(Lifetime.UNTIL_NEXT_BOOT)
                .setReplaceCurrent(false)
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                .build();
        bundle = getIntent().getExtras();
    }

    @Override
    protected void onStart() {
        super.onStart();
        setVisible(true);
        mUser = mAuth.getCurrentUser();
        if (mUser == null){
            Intent intent = new Intent(this, SignInActivity.class);
            startActivityForResult(intent, SIGN_IN);
        }else{
            start();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("Result Code", resultCode+"");
        if (resultCode == 0){
            finish();
        }
    }

    private void start(){
        if(sp.getString("role", null) == null || sp.getString("name", null) == null || !sp.getBoolean("verify", false) || sp.getString("username", null) == null){
            mDatabase.getReference("users/"+mUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()){
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("role", dataSnapshot.child("role").getValue(String.class));
                        if (dataSnapshot.child("firstName").getValue(String.class) != null && dataSnapshot.child("lastName").getValue(String.class) != null){
                            editor.putString("name", String.format("%s %s", dataSnapshot.child("firstName").getValue(String.class), dataSnapshot.child("lastName").getValue(String.class)));
                        }
                        if (dataSnapshot.child("verify").getValue(Boolean.class)){
                            editor.putBoolean("verify", true);
                            mDispatcher.mustSchedule(locationJob);
                        }
                        if(dataSnapshot.child("username").getValue(String.class) != null){
                            editor.putString("username", dataSnapshot.child("username").getValue(String.class));
                            FirebaseMessaging.getInstance().subscribeToTopic(dataSnapshot.child("username").getValue(String.class));
                        }
                        if (sp.getString("device", null) != null){
                            FirebaseMessaging.getInstance().subscribeToTopic(dataSnapshot.child("username").getValue(String.class)+"."+sp.getString("device", null));
                            setUserProperties(dataSnapshot.child("username").getValue(String.class), sp.getString("device", null));
                        }
                        //editor.putBoolean("firstTime", dataSnapshot.child("firstTime").getValue(Boolean.class));
                        editor.apply();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        if (sp.getBoolean("verify", false)){
            mDispatcher.mustSchedule(locationJob);
        }
        if (sp.getString("username", null) != null){
            FirebaseMessaging.getInstance().subscribeToTopic(sp.getString("username", null));
        }
        if (sp.getString("device", null) != null && sp.getString("username", null) != null){
            FirebaseMessaging.getInstance().subscribeToTopic(sp.getString("username", null)+"."+sp.getString("device", null));
            setUserProperties(sp.getString("username", null), sp.getString("device", null));
        }
        Intent intent = new Intent(this, MainActivity.class);

        if (bundle != null){
            if ("true".equals(bundle.getString("request"))){
                Log.d("Hello", "bundle");
                intent.putExtra("request", true);
                intent.putExtra("request_uid", bundle.getString("request_uid"));
            }
        }
        startActivityForResult(intent, MAIN);
    }

    private void setUserProperties(String username, String token){
        mFirebaseAnalytics.setUserProperty("username", username);
        mFirebaseAnalytics.setUserProperty("token", token);
    }
}
