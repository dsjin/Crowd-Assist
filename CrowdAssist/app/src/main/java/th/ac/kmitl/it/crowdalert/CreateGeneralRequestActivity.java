package th.ac.kmitl.it.crowdalert;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import th.ac.kmitl.it.crowdalert.model.GeneralRequestModel;
import th.ac.kmitl.it.crowdalert.model.Request;
import th.ac.kmitl.it.crowdalert.util.DatabaseHelper;
import th.ac.kmitl.it.crowdalert.util.LocationHelper;

public class CreateGeneralRequestActivity extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, OnCompleteListener<Void> {
    private final String SP_REQUEST = "request_information";
    private String type;
    private DatabaseHelper helper;
    private TextView typeTitle;
    private LinearLayout confirmButton;
    private EditText description;
    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mMap;
    private Location location;
    private HashMap<String, String> mapType;
    private RadioGroup requesterType;
    private RelativeLayout imageLayout;
    private Uri uri;
    private ImageView addPhoto;
    private android.support.constraint.ConstraintLayout mainLayout;
    private LinearLayout progressLayout;
    private ProgressBar circularProgress;
    private ProgressBar horizontalProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_general_request);
        Toolbar toolbar = findViewById(R.id.toolbar_top);
        setSupportActionBar(toolbar);
        setTitle(" ");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mapType = new HashMap<>();
        setupMapType();

        helper = new DatabaseHelper(this);
        Intent intent = getIntent();
        type = intent.getStringExtra("type");

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        //TODO Bind Widgets with variables
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        typeTitle = findViewById(R.id.type_title);
        confirmButton = findViewById(R.id.sendButton);
        description = findViewById(R.id.description_text);
        confirmButton.setOnClickListener(this);
        requesterType = findViewById(R.id.requesterType);
        imageLayout = findViewById(R.id.imageLayout);
        addPhoto = findViewById(R.id.add_photo);
        mainLayout = findViewById(R.id.mainLayout);
        progressLayout = findViewById(R.id.progress_layout);
        circularProgress = findViewById(R.id.circularProgress);
        horizontalProgress = findViewById(R.id.horizontalProgress);
        imageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.OFF)
                        .setCropShape(CropImageView.CropShape.RECTANGLE)
                        .start(CreateGeneralRequestActivity.this);
            }
        });
        if (type != null){
            typeTitle.setText(String.format("หมวดหมู่คำร้อง : %s", type));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        if (description.getText().toString().isEmpty() && uri == null){
            Toast.makeText(this, "กรุณาใส่ข้อมูลให้ครบ", Toast.LENGTH_SHORT).show();
            return;
        }
        String requesterTypeString = "";
        switch (requesterType.getCheckedRadioButtonId()){
            case R.id.radio_victim:
                requesterTypeString = "victim";
                break;
            case R.id.radio_observer:
                requesterTypeString = "observer";
        }
        GeneralRequestModel data = new GeneralRequestModel();
        data.setType(mapType.get(type));
        data.setDescription(description.getText().toString());
        data.setLat(location.getLatitude());
        data.setLng(location.getLongitude());
        data.setTime(1);
        data.setRequesterType(requesterTypeString);
        try{
            data.setTitle(LocationHelper.getLocationName(this, location));
        }catch (IOException exception){
            Log.e("LocationHelper", exception.getMessage());
        }
        data.setStatus("wait");
        mainLayout.setVisibility(View.GONE);
        progressLayout.setVisibility(View.VISIBLE);
        Observable.fromCallable(resolveCallable())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .take(1)
                .subscribe(action1(data));
    }

    @Override
    public void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (!hasPermission(android.Manifest.permission.ACCESS_FINE_LOCATION))
            return;
        if (mMap != null){
            location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()),15));
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if (mGoogleApiClient.isConnected()){
            this.location = location;
        }
    }
    private boolean hasPermission(String permission) {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    private void setupMapType(){
        mapType.put("อัคคีภัย", "type1");
        mapType.put("อุบัติเหตุทางถนน", "type2");
        mapType.put("อุบัติเหตุทางน้ำ", "type3");
        mapType.put("อื่นๆ", "type4");
    }

    @Override
    public void onComplete(@NonNull Task<Void> task) {
        if (task.isSuccessful()){
            circularProgress.setVisibility(View.GONE);
            horizontalProgress.setVisibility(View.VISIBLE);
            SharedPreferences sp = getSharedPreferences(SP_REQUEST, Context.MODE_PRIVATE);

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference ref = storage.getReference().child("non_emergency").child(sp.getString("request_uid", "null")+".jpg");
            ref.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    setResult(RESULT_OK);
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("EditFirstTime | Upload", e.getMessage());
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    Long progress = (100*taskSnapshot.getBytesTransferred()/taskSnapshot
                            .getTotalByteCount());
                    horizontalProgress.setProgress(progress.intValue());
                }
            });
        }else{
            Toast.makeText(this, "มีข้อผิดพลาดกรุณาลองใหม่อีกครั้ง", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                Picasso.with(this)
                        .load(resultUri)
                        .into(addPhoto);
                this.uri = resultUri;
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private Consumer<String> action1(final Request data){
        return new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                data.setArea(s);
                helper.createGeneralRequest(data, CreateGeneralRequestActivity.this);
            }
        };
    }

    private Callable<String> resolveCallable(){
        return new Callable<String>() {
            @Override
            public String call() throws Exception {
                return LocationHelper.resolveArea(location);
            }
        };
    }
}
