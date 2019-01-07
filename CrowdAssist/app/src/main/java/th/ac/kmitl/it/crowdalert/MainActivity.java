package th.ac.kmitl.it.crowdalert;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.geofire.GeoFire;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import th.ac.kmitl.it.crowdalert.component.AcceptDialog;
import th.ac.kmitl.it.crowdalert.component.GeneralSendingDialog;
import th.ac.kmitl.it.crowdalert.component.VerifyAlertDialog;
import th.ac.kmitl.it.crowdalert.fragment.HelpFragment;
import th.ac.kmitl.it.crowdalert.fragment.HelpedFragment;
import th.ac.kmitl.it.crowdalert.fragment.MainFragment;
import th.ac.kmitl.it.crowdalert.fragment.NotificationFragment;
import th.ac.kmitl.it.crowdalert.fragment.ProflieFragment;
import th.ac.kmitl.it.crowdalert.fragment.SettingFragment;
import th.ac.kmitl.it.crowdalert.model.UserModel;
import th.ac.kmitl.it.crowdalert.service.ReceivingMessengerService;
import th.ac.kmitl.it.crowdalert.util.DatabaseHelper;
import th.ac.kmitl.it.crowdalert.util.GoToCallback;
import th.ac.kmitl.it.crowdalert.util.LogoutCallback;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, LogoutCallback, ValueEventListener, SharedPreferences.OnSharedPreferenceChangeListener, GoToCallback{
    private static final long FADE_DEFAULT_TIME = 300;
    private final String SP_REQUEST = "request_information";
    private final String SP_PROFILE = "profile";
    private final String SP_LOCATION = "location_information";
    private FloatingActionButton requestingFab;
    private Toolbar toolbar;
    private Boolean start = true;
    private Boolean isHome = true;
    private TextView title;
    private ImageView logo;
    private SharedPreferences sp;
    private SharedPreferences profileSp;
    private SharedPreferences locationSp;
    private FirebaseUser mUser;
    private FirebaseDatabase mDatabase;
    private FirebaseAuth mAuth;
    private ImageView imageView;
    private AcceptDialog dialog;
    private GeneralSendingDialog generalSendingDialog;
    private Boolean firstTime = true;
    private BroadcastReceiver receiver;
    private Boolean inNotification = false;
    private LinearLayout verifyLayout;
    private NavigationView navigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        title = toolbar.findViewById(R.id.title_text);
        logo = toolbar.findViewById(R.id.logo);
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance();
        requestingFab = findViewById(R.id.requestingButton);
        requestingFab.setOnClickListener(this);
        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        sp = getSharedPreferences(SP_REQUEST, Context.MODE_PRIVATE);
        profileSp = getSharedPreferences(SP_PROFILE, Context.MODE_PRIVATE);
        locationSp = getSharedPreferences(SP_LOCATION, Context.MODE_PRIVATE);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){
                private Boolean close = false;
                @Override
                public void onDrawerClosed(View v){
                    super.onDrawerClosed(v);
                    close = true;
                    if (isHome){
                        requestingFab.show();
                    }
                }

                @Override
                public void onDrawerOpened(View v) {
                    super.onDrawerOpened(v);
                    close = false;
                }
                @Override
                public void onDrawerSlide(View drawerView, float slideOffset) {
                    super.onDrawerSlide(drawerView, slideOffset);
                    requestingFab.hide();
                };

                @Override
                public void onDrawerStateChanged(int newState) {
                    super.onDrawerStateChanged(newState);
                    if (close && !requestingFab.isActivated() &&isHome){
                        requestingFab.show();
                    }
                }
        };
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);
        imageView = header.findViewById(R.id.imageView);
        TextView email = header.findViewById(R.id.email);
        email.setText(mUser.getEmail());
        verifyLayout = header.findViewById(R.id.verifyLayout);
        Picasso.with(this).load(mUser.getPhotoUrl()).resize(500, 500).into(imageView);

        navigationView.setCheckedItem(R.id.nav_home);

        //mDatabase.getReference("notification").child(mUser.getUid()).orderByChild("timestamp_sort").limitToFirst(1).addChildEventListener(this);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getBooleanExtra("verify", false)){
                    if (!profileSp.getBoolean("verify", false)){
                        VerifyAlertDialog dialog = new VerifyAlertDialog(MainActivity.this);
                        dialog.show();
                    }
                }else if(intent.getBooleanExtra("request", false)){
                    if (dialog != null){
                        if (!dialog.isShowing() && !inNotification){
                            mDatabase.getReference("notification").child(mUser.getUid()).child(intent.getStringExtra("request_uid")).addListenerForSingleValueEvent(MainActivity.this);
                        }
                    }else{
                        if (!inNotification){
                            mDatabase.getReference("notification").child(mUser.getUid()).child(intent.getStringExtra("request_uid")).addListenerForSingleValueEvent(MainActivity.this);
                        }
                    }
                }
            }
        };
        if (getIntent().getBooleanExtra("request", false)){
            if (dialog != null){
                if (!dialog.isShowing() && !inNotification){
                    mDatabase.getReference("notification").child(mUser.getUid()).child(getIntent().getStringExtra("request_uid")).addListenerForSingleValueEvent(MainActivity.this);
                }
            }else{
                if (!inNotification){
                    mDatabase.getReference("notification").child(mUser.getUid()).child(getIntent().getStringExtra("request_uid")).addListenerForSingleValueEvent(MainActivity.this);
                }
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        profileSp.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onStart() {
        if (start && sp.getString("request_uid", null) == null){
            getFragment(new MainFragment());
            start = !start;
        }else if (start && sp.getString("request_uid", null) != null && !"Rate".equals(sp.getString("mode",""))){
            set(sp.getString("mode", null));
            start = !start;
        }else if("Rate".equals(sp.getString("mode",""))){
            getFragment(new MainFragment());
            Intent intent = new Intent(this, RateActivity.class);
            startActivityForResult(intent, 9002);
        }

        if (profileSp.getBoolean("firstTime", true)){
            mDatabase.getReference("users/"+mUser.getUid()+"/firstTime").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        if (dataSnapshot.getValue(Boolean.class)){
                            Intent editProfileFirstTime = new Intent(MainActivity.this, EditFirstTimeActivity.class);
                            startActivityForResult(editProfileFirstTime, 1003);
                        }else{
                            SharedPreferences.Editor editor = profileSp.edit();
                            editor.putBoolean("firstTime", false);
                            editor.apply();
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        if (profileSp.getBoolean("verify", false)){
            verifyLayout.setBackgroundColor(getResources().getColor(R.color.verify));
        }
        LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
                new IntentFilter(ReceivingMessengerService.REQUEST_ACCEPT)
        );
        profileSp.registerOnSharedPreferenceChangeListener(this);
        /*
        if (start){
            getFragment(new MainFragment());
            start = !start;
            Intent intent = new Intent(this, ManageRequestActivity.class);
            startActivity(intent);
        }*/
        super.onStart();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.nav_home:
                if (!isHome){
                    isHome = true;
                    getFragment(new MainFragment());
                    title.setText(R.string.app_name);
                    title.setVisibility(View.GONE);
                    logo.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.nav_profile:
                requestingFab.hide();
                isHome = false;
                getFragment(new ProflieFragment());
                title.setText("โปรไฟล์");
                title.setVisibility(View.VISIBLE);
                logo.setVisibility(View.GONE);
                break;
            case R.id.nav_setting:
                requestingFab.hide();
                isHome = false;
                getFragment(new SettingFragment());
                title.setText("ตั้งค่า");
                title.setVisibility(View.VISIBLE);
                logo.setVisibility(View.GONE);
                break;
            case R.id.nav_notification:
                requestingFab.hide();
                isHome = false;
                getFragment(new NotificationFragment());
                title.setText("การแจ้งเตือน");
                break;
            /*
            case R.id.nav_remove_all_jobs:
                FirebaseJobDispatcher mDispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));
                mDispatcher.cancelAll();
                break;
            case R.id.nav_test:
                requestingFab.hide();
                isHome = false;
                getFragment(new ManageRequestFragment());
                title.setText("จัดการคำร้อง");
                break;
            case R.id.nav_notification:
                requestingFab.hide();
                isHome = false;
                getFragment(new NotificationFragment());
                title.setText("การแจ้งเตือน");
                break;*/
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    protected void getFragment(Fragment mFragment) {
        FragmentManager fragmentManager = getFragmentManager();

        Fade enterFade = new Fade();
        enterFade.setDuration(FADE_DEFAULT_TIME);
        mFragment.setEnterTransition(enterFade);

        fragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, mFragment)
                .commit();
    }
    protected void getFragment(android.support.v4.app.Fragment mFragment) {
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();

        Fade enterFade = new Fade();
        enterFade.setDuration(FADE_DEFAULT_TIME);
        mFragment.setEnterTransition(enterFade);

        fragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, mFragment)
                .commit();
    }
    protected void getFragment(android.support.v4.app.Fragment mFragment, String tag) {
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();

        Fade enterFade = new Fade();
        enterFade.setDuration(FADE_DEFAULT_TIME);
        mFragment.setEnterTransition(enterFade);

        fragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, mFragment, tag)
                .commitAllowingStateLoss();
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            //super.onBackPressed();
            new AlertDialog.Builder(this)
                    .setMessage("คุณต้องการออกจากแอพพลิเคชันใช่หรือไม่")
                    .setCancelable(false)
                    .setPositiveButton("ใช่", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            MainActivity.this.finish();
                        }
                    })
                    .setNegativeButton("ไม่ใช่", null)
                    .show();
        }
    }

    public void set(String set){
        requestingFab.hide();
        toolbar.setVisibility(View.GONE);
        switch (set){
            case "Helped":
                getFragment(new HelpedFragment(), "working");
                break;
            case "Help":
                getFragment(new HelpFragment(), "working");
        }
    }

    public void setToHome(){
        toolbar.setVisibility(View.VISIBLE);
        getFragment(new MainFragment(), "Home");
        requestingFab.show();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 9001){
            if (resultCode == RESULT_OK){
                setToHome();
                Intent intent = new Intent(this, RateActivity.class);
                startActivityForResult(intent, 9002);
            }
        }else if (requestCode == 9002){
            if (resultCode == RESULT_OK){
                SharedPreferences.Editor editor = sp.edit();
                editor.clear();
                editor.apply();
                //setToHome();
            }
        }else if(requestCode == 1003){
            if (resultCode == RESULT_OK){
                SharedPreferences.Editor editor = profileSp.edit();
                editor.putBoolean("firstTime", false);
                editor.apply();
                mUser = FirebaseAuth.getInstance().getCurrentUser();
                Picasso.with(this).load(mUser.getPhotoUrl()).resize(500, 500).into(imageView);
            }
            else if (resultCode == RESULT_CANCELED){
                finish();
            }
        }else if (requestCode == 4000){
            if (resultCode == RESULT_OK){
                dialog.cancle();
                DatabaseHelper helper = new DatabaseHelper(this);
                switch (data.getStringExtra("type")){
                    case "emergency":
                        helper.acceptEmergencyRequest(data.getStringExtra("request_uid"), (UserModel) data.getSerializableExtra("user_information"));
                        break;
                    case "non_emergency":
                        helper.acceptNonEmergencyRequest(data.getStringExtra("request_uid"), (UserModel) data.getSerializableExtra("user_information"));
                }
                set("Help");
            }else if (resultCode == RESULT_CANCELED){
                dialog.cancle();
            }
        }else if(requestCode == 9015){
            if (resultCode == RESULT_OK) {
                generalSendingDialog.dismissAllowingStateLoss();
                generalSendingDialog = null;
                set("Helped");
            }
        }
    }

    public void setListener(View.OnLongClickListener longClickListener){
        requestingFab.setOnLongClickListener(longClickListener);
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        if (sp.getString("request_uid", null) != null){
            return;
        }
        if (dataSnapshot.child("user").getValue(UserModel.class) == null ){
            return;
        }
        String uid = dataSnapshot.getKey();
        UserModel user = dataSnapshot.child("user").getValue(UserModel.class);
        String type = dataSnapshot.child("type").getValue(String.class);
        String requesterUid = dataSnapshot.child("requesterUid").getValue(String.class);
        user.setUserUid(requesterUid);
        dialog = new AcceptDialog(MainActivity.this, user, uid, type);
        dialog.show();
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }

    @Override
    public void onClick(View view) {
        if (profileSp.getBoolean("verify", false)){
            generalSendingDialog = new GeneralSendingDialog();
            generalSendingDialog.show(getSupportFragmentManager(), generalSendingDialog.getTag());
        }else{
            Snackbar.make(findViewById(R.id.fragmentContainer), "กรุณายืนยันตัวตนก่อนการใช้งาน", Snackbar.LENGTH_SHORT).setAction("ไปยังหน้ายืนยัน", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    goTo("Setting");
                }
            }).show();
        }
    }

    @Override
    public void logout(){
        FirebaseJobDispatcher mDispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));
        mDispatcher.cancelAll();
        DatabaseReference ref = mDatabase.getReference("location");
        GeoFire geoFire = new GeoFire(ref);
        if (profileSp.getString("device", null) != null){
            geoFire.removeLocation(mUser.getUid()+"@"+profileSp.getString("device", ""));
        }else{
            geoFire.removeLocation(mUser.getUid());
        }
        SharedPreferences.Editor editLocation = locationSp.edit();
        editLocation.clear();
        editLocation.apply();
        SharedPreferences.Editor editProfile = profileSp.edit();
        editProfile.clear();
        editProfile.apply();
        mAuth.signOut();
        setResult(RESULT_OK);
        finish();
    }

    public void setDialog(AcceptDialog dialog){
        this.dialog = dialog;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if ("verify".equals(s)){
            if (sharedPreferences.getBoolean("verify", false)){
                verifyLayout.setBackgroundColor(getResources().getColor(R.color.verify));
            }
        }
    }

    @Override
    public void goTo(String fragmentName) {
        switch (fragmentName){
            case "Setting":
                requestingFab.hide();
                isHome = false;
                getFragment(new SettingFragment());
                title.setText("ตั้งค่า");
                title.setVisibility(View.VISIBLE);
                logo.setVisibility(View.GONE);
                navigationView.setCheckedItem(R.id.nav_setting);
                break;
        }
    }
    public void setToRate(){
        toolbar.setVisibility(View.VISIBLE);
        getFragment(new MainFragment(), "Home");
        requestingFab.show();
        isHome = true;
        Intent intent = new Intent(this, RateActivity.class);
        startActivityForResult(intent, 9002);
    }
}
