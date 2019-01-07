package th.ac.kmitl.it.crowdalert;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import th.ac.kmitl.it.crowdalert.component.RecyclerGridViewAdapter;
import th.ac.kmitl.it.crowdalert.model.AssistantModel;
import th.ac.kmitl.it.crowdalert.model.Request;
import th.ac.kmitl.it.crowdalert.util.ConvertHelper;

public class ManageRequestingActivity extends AppCompatActivity implements OnMapReadyCallback {
    private Request information;
    private String type;
    private GoogleMap mMap;
    private TextView date;
    private TextView status;
    private ArrayList<AssistantModel> officerList;
    private ArrayList<AssistantModel> volunteerList;
    private RecyclerGridViewAdapter adapter;
    private FirebaseStorage mStorage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_requesting);
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        information = (Request)getIntent().getSerializableExtra("data");
        type = getIntent().getStringExtra("type");
        date = findViewById(R.id.date);
        status = findViewById(R.id.status);
        mStorage = FirebaseStorage.getInstance();
        TextView emergencyTitle = findViewById(R.id.emergency_title);
        LinearLayout informationDetail = findViewById(R.id.information_layout);
        if ("emergency".equals(type)){
            emergencyTitle.setVisibility(View.VISIBLE);
            informationDetail.setVisibility(View.GONE);
            date.setText(String.format("เวลาส่งคำร้องขอ : %s", ConvertHelper.ConvertTimestampToDate(information.getTimestamp())));
            status.setText(String.format("สถานะคำร้องขอ : %s", ConvertHelper.ConvertStatusToThai(information.getStatus())));
        }else{
            informationDetail.setVisibility(View.VISIBLE);
            emergencyTitle.setVisibility(View.GONE);
            TextView typeTitle = findViewById(R.id.type_non_emergency);
            typeTitle.setText(String.format("หมวดหมู่ : %s", ConvertHelper.ConvertTypeToThai(information.getType())));
            TextView statusOfRequester = findViewById(R.id.statusOfRequester);
            statusOfRequester.setText(String.format("สถานะผู้ร้องขอ : %s", information.getRequesterType().equals("victim")? "ผู้ประสบเหตุ":"ผู้เห็นเหตุการณ์"));
            TextView description = findViewById(R.id.description);
            description.setText(information.getDescription());
            final ImageView cover = findViewById(R.id.coverImage);
            date.setText(String.format("เวลาส่งคำร้องขอ : %s", ConvertHelper.ConvertTimestampToDate(information.getTimestamp())));
            status.setText(String.format("สถานะคำร้องขอ : %s", ConvertHelper.ConvertStatusToThai(information.getStatus())));
            mStorage.getReference().child("non_emergency").child(getIntent().getStringExtra("request_id")+".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.with(ManageRequestingActivity.this)
                            .load(uri)
                            .into(cover);
                }
            });
        }
        officerList = (ArrayList<AssistantModel>) getIntent().getSerializableExtra("officer");
        volunteerList = (ArrayList<AssistantModel>) getIntent().getSerializableExtra("volunteer");
        TextView officerAccepted = findViewById(R.id.officer_accept);
        TextView voluteerAccepted = findViewById(R.id.user_accept);
        officerAccepted.setText(String.format("%d", officerList.size()));
        voluteerAccepted.setText(String.format("%d", volunteerList.size()));
        ArrayList<AssistantModel> newList = new ArrayList<>(officerList);
        newList.addAll(volunteerList);
        RecyclerView userView = findViewById(R.id.userItem);
        adapter = new RecyclerGridViewAdapter(this, newList, information);
        userView.setLayoutManager(new GridLayoutManager(this, 3));
        userView.setAdapter(adapter);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(information.getLat(), information.getLng()), 15));
        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(information.getLat(), information.getLng())));
    }
}
