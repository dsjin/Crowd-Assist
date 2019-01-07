package th.ac.kmitl.it.crowdalert;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import org.joda.time.LocalDate;
import org.joda.time.Years;

import java.util.Calendar;
import java.util.Locale;

import th.ac.kmitl.it.crowdalert.model.EmergencyRequestModel;
import th.ac.kmitl.it.crowdalert.model.GeneralRequestModel;
import th.ac.kmitl.it.crowdalert.model.UserModel;
import th.ac.kmitl.it.crowdalert.util.ConvertHelper;

public class AcceptActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener{
    private GoogleMap mMap;
    private DatabaseReference requestInformation;
    private FirebaseDatabase mDatabase;
    private TextView date;
    private TextView status;
    private TextView officerAccepted;
    private TextView voluteerAccepted;
    private FirebaseStorage mStorage;
    private ImageView imageView;
    private UserModel user;
    private LinearLayout buttonWrapper;
    private LinearLayout close;
    private RatingBar ratingBar;
    private TextView rating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accept);
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        TextView emergencyTitle = findViewById(R.id.emergency_title);
        LinearLayout informationDetail = findViewById(R.id.information_layout);
        user = (UserModel) getIntent().getSerializableExtra("user_information");
        mStorage = FirebaseStorage.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        requestInformation = mDatabase.getReference();
        date = findViewById(R.id.date);
        status = findViewById(R.id.status);
        imageView = findViewById(R.id.imageView);
        buttonWrapper = findViewById(R.id.button_wrapper);
        close = findViewById(R.id.close);
        close.setOnClickListener(this);
        ratingBar = findViewById(R.id.ratingBar);
        rating = findViewById(R.id.rating);

        if ("emergency".equals(getIntent().getStringExtra("type"))){
            emergencyTitle.setVisibility(View.VISIBLE);
            informationDetail.setVisibility(View.GONE);
            requestInformation.child("emergency").child(getIntent().getStringExtra("request_id")).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    EmergencyRequestModel information = dataSnapshot.getValue(EmergencyRequestModel.class);
                    date.setText(String.format("เวลาส่งคำร้องขอ : %s", ConvertHelper.ConvertTimestampToDate(information.getTimestamp())));
                    status.setText(String.format("สถานะคำร้องขอ : %s", ConvertHelper.ConvertStatusToThai(information.getStatus())));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(information.getLat(), information.getLng()), 15));
                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(information.getLat(), information.getLng())));
                    if ("close".equals(information.getStatus())){
                        buttonWrapper.setVisibility(View.GONE);
                        close.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }else{
            informationDetail.setVisibility(View.VISIBLE);
            emergencyTitle.setVisibility(View.GONE);
            requestInformation.child("non_emergency").child(getIntent().getStringExtra("request_id")).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    GeneralRequestModel information = dataSnapshot.getValue(GeneralRequestModel.class);
                    TextView typeTitle = AcceptActivity.this.findViewById(R.id.type_non_emergency);
                    typeTitle.setText(String.format("หมวดหมู่ : %s", ConvertHelper.ConvertTypeToThai(information.getType())));
                    TextView description = AcceptActivity.this.findViewById(R.id.description);
                    description.setText(information.getDescription());
                    TextView statusOfRequester = findViewById(R.id.statusOfRequester);
                    statusOfRequester.setText(String.format("สถานะผู้ร้องขอ : %s", information.getRequesterType().equals("victim")? "ผู้ประสบเหตุ":"ผู้เห็นเหตุการณ์"));
                    date.setText(String.format("เวลาส่งคำร้องขอ : %s", ConvertHelper.ConvertTimestampToDate(information.getTimestamp())));
                    status.setText(String.format("สถานะคำร้องขอ : %s", ConvertHelper.ConvertStatusToThai(information.getStatus())));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(information.getLat(), information.getLng()), 15));
                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(information.getLat(), information.getLng())));
                    final ImageView cover = findViewById(R.id.coverImage);
                    mStorage.getReference().child("non_emergency").child(getIntent().getStringExtra("request_id")+".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Picasso.with(AcceptActivity.this)
                                    .load(uri)
                                    .into(cover);
                        }
                    });
                    if ("close".equals(information.getStatus())){
                        buttonWrapper.setVisibility(View.GONE);
                        close.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        officerAccepted = findViewById(R.id.officer_accept);
        voluteerAccepted = findViewById(R.id.user_accept);
        mDatabase.getReference().child(getIntent().getStringExtra("type")+"_assistance").child(getIntent().getStringExtra("request_id")).child("officerCount").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    officerAccepted.setText(String.format("%d",dataSnapshot.getValue(Integer.class)));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mDatabase.getReference().child(getIntent().getStringExtra("type")+"_assistance").child(getIntent().getStringExtra("request_id")).child("userCount").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    voluteerAccepted.setText(String.format("%d",dataSnapshot.getValue(Integer.class)));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        setUserInfo(user);
        LinearLayout decline = findViewById(R.id.decline);
        LinearLayout accept = findViewById(R.id.accept);
        decline.setOnClickListener(this);
        accept.setOnClickListener(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setAllGesturesEnabled(false);
    }
    private void setUserInfo(UserModel user){
        TextView name = findViewById(R.id.name);
        TextView gender = findViewById(R.id.gender);
        TextView age = findViewById(R.id.age);
        LocalDate birthOfDate = new LocalDate(user.getDateOfBirth());
        LocalDate now = new LocalDate(Calendar.getInstance().getTime().getTime());
        name.setText(String.format("%s %s", user.getFirstName(), user.getLastName()));
        gender.setText(String.format("%s, ", "male".equals(user.getGender())? "ชาย" : "หญิง"));
        age.setText(String.format(Locale.forLanguageTag("th-TH"), "%d", Years.yearsBetween(birthOfDate, now).getYears()));
        mStorage.getReference().child("profile").child(user.getUserUid()+".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.with(AcceptActivity.this)
                        .load(uri)
                        .resize(500, 500)
                        .into(imageView);
            }
        });
        mDatabase.getReference().child("users").child(user.getUserUid()).child("rating").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Double rate;
                if(dataSnapshot.child("scores").getValue(Double.class) != null && dataSnapshot.child("counts").getValue(Double.class) != null){
                    if (dataSnapshot.child("counts").getValue(Double.class) == 0.0){
                        rate = 0.0;
                    }else{
                        rate = dataSnapshot.child("scores").getValue(Double.class)/dataSnapshot.child("counts").getValue(Double.class);
                    }
                }else{
                    rate = 0.0;
                }
                rating.setText(String.format("( %.1f )", rate));
                ratingBar.setRating(rate.floatValue());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onClick(View view) {
        //TODO Wait for final flow
        switch (view.getId()) {
            case R.id.decline:
                setResult(RESULT_CANCELED);
                finish();
                break;
            case R.id.accept:
                Intent intent = new Intent();
                intent.putExtra("request_uid", getIntent().getStringExtra("request_id"));
                intent.putExtra("type", getIntent().getStringExtra("type"));
                intent.putExtra("user_information", user);
                setResult(RESULT_OK, intent);
                finish();
                break;
            case R.id.close:
                setResult(RESULT_CANCELED);
                finish();
                break;
        }
    }
}
