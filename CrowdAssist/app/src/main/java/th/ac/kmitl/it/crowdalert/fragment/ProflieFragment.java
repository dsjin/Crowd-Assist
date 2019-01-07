package th.ac.kmitl.it.crowdalert.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Locale;

import th.ac.kmitl.it.crowdalert.ProfileListActivity;
import th.ac.kmitl.it.crowdalert.R;

public class ProflieFragment extends Fragment implements View.OnClickListener{
    private CardView emergency;
    private CardView nonEmergency;
    private CardView assistance;
    private TextView emergencyCount;
    private TextView nonEmergencyCount;
    private TextView assistanceCount;
    private ImageView profileImageView;
    private TextView name;
    private RatingBar ratingBar;
    private TextView rating;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private FirebaseUser mUser;

    public ProflieFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_proflie, container, false);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mUser = mAuth.getCurrentUser();
        /*
        Button edit = view.findViewById(R.id.edit);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getActivity() != null) {
                    Intent intent = new Intent(getActivity(), EditProfileActivity.class);
                    getActivity().startActivity(intent);
                }
            }
        });*/
        emergency = view.findViewById(R.id.emergency_count);
        nonEmergency = view.findViewById(R.id.general_count);
        assistance = view.findViewById(R.id.assistance_count);
        emergency.setOnClickListener(this);
        nonEmergency.setOnClickListener(this);
        assistance.setOnClickListener(this);
        emergencyCount = view.findViewById(R.id.count1);
        nonEmergencyCount = view.findViewById(R.id.count2);
        assistanceCount = view.findViewById(R.id.count3);
        profileImageView = view.findViewById(R.id.imageView);
        ratingBar = view.findViewById(R.id.ratingBar);
        name = view.findViewById(R.id.name);
        rating = view.findViewById(R.id.rating);
        Picasso.with(getActivity()).load(mUser.getPhotoUrl())
                .resize(500,500)
                .into(profileImageView);
        mDatabase.getReference().child("users").child(mUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                name.setText(String.format("%s %s", dataSnapshot.child("firstName").getValue(String.class), dataSnapshot.child("lastName").getValue(String.class)));
                Double rate;
                if(dataSnapshot.child("rating").child("scores").getValue(Double.class) != null && dataSnapshot.child("rating").child("counts").getValue(Double.class) != null){
                    if (dataSnapshot.child("rating").child("counts").getValue(Double.class) == 0.0){
                        rate = 0.0;
                    }else{
                        rate = dataSnapshot.child("rating").child("scores").getValue(Double.class)/dataSnapshot.child("rating").child("counts").getValue(Double.class);
                    }
                }else{
                    rate = 0.0;
                }
                rating.setText(String.format("( %.1f )", rate));
                ratingBar.setRating(rate.floatValue());
                Integer emergency = 0;
                Integer general = 0;
                Integer assistance = 0;
                if (dataSnapshot.child("numOfDoc").child("emergency").getValue(Integer.class) != null){
                    emergency = dataSnapshot.child("numOfDoc").child("emergency").getValue(Integer.class);
                }
                if (dataSnapshot.child("numOfDoc").child("general").getValue(Integer.class) != null){
                    general = dataSnapshot.child("numOfDoc").child("general").getValue(Integer.class);
                }
                if (dataSnapshot.child("numOfDoc").child("assistance").getValue(Integer.class) != null){
                    assistance = dataSnapshot.child("numOfDoc").child("assistance").getValue(Integer.class);
                }
                emergencyCount.setText(String.format(Locale.forLanguageTag("Th-th"),"%d", emergency));
                nonEmergencyCount.setText(String.format(Locale.forLanguageTag("Th-th"),"%d", general));
                assistanceCount.setText(String.format(Locale.forLanguageTag("Th-th"), "%d", assistance));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return view;
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(getActivity(), ProfileListActivity.class);
        switch (view.getId()){
            case R.id.emergency_count:
                intent.putExtra("mode", "emergency");
                break;
            case R.id.general_count:
                intent.putExtra("mode", "non-emergency");
                break;
            case R.id.assistance_count:
                intent.putExtra("mode", "assistance");
                break;
        }
        startActivity(intent);
    }
}
