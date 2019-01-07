package th.ac.kmitl.it.crowdalert;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.util.LinkedList;
import java.util.Queue;

import th.ac.kmitl.it.crowdalert.model.AssistantModel;
import th.ac.kmitl.it.crowdalert.model.UserModel;
import th.ac.kmitl.it.crowdalert.util.AssistantViewModel;
import th.ac.kmitl.it.crowdalert.util.DatabaseHelper;

public class RateActivity extends AppCompatActivity implements View.OnClickListener, OnCompleteListener<Void> {
    private final String SP_REQUEST = "request_information";
    private FirebaseDatabase mDatabase;
    private FirebaseStorage mStorage;
    private SharedPreferences requestSP;
    private String requestUid;
    private String requestType;
    private String requesterUid;
    private AssistantModel currentUser;
    private DatabaseHelper helper;
    private Queue<AssistantModel> userQueue;
    private FirebaseUser mUser;
    private FirebaseAuth mAuth;
    private LinearLayout progressLayout;
    private LinearLayout rateCardLayout;
    private ImageView profileImage;
    private RatingBar ratingBar;
    private TextView nameTextView;
    private TextView roleTextView;
    private EditText descriptionEditText;
    private LinearLayout rateButton;
    private LinearLayout ignoreButton;
    private UserModel userModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate);
        helper = new DatabaseHelper(this);
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance();
        mStorage = FirebaseStorage.getInstance();
        bindView();
        userQueue = new LinkedList<>();
        requestSP = getSharedPreferences(SP_REQUEST, Context.MODE_PRIVATE);

        requestUid = requestSP.getString("request_uid", null);
        requestType = requestSP.getString("type", null);
        requesterUid = requestSP.getString("requester_uid", null);

        progressLayout.setVisibility(View.VISIBLE);
        rateCardLayout.setVisibility(View.GONE);
        Log.d("Hello", mUser.getUid()+":"+requesterUid);
        if (mUser.getUid().equals(requesterUid)){
            AssistantViewModel viewModel = ViewModelProviders.of(this).get(AssistantViewModel.class);
            final LiveData<DataSnapshot> liveData = viewModel.getDataSnapshotLiveData();
            liveData.observe(this, new Observer<DataSnapshot>() {
                @Override
                public void onChanged(@Nullable DataSnapshot dataSnapshot) {
                    if (dataSnapshot == null){
                        Log.e("Assistant Livedata", "datasnapshot is empty");
                        setResult(RESULT_OK);
                        finish();
                    }
                    if (!dataSnapshot.exists()){
                        setResult(RESULT_OK);
                        finish();
                    }
                    if (dataSnapshot.exists()){
                        for (DataSnapshot data : dataSnapshot.getChildren()){
                            AssistantModel model = new AssistantModel();
                            model.setAssistantUid(data.getKey());
                            model.setRole(data.child("role").getValue(String.class));
                            push(model);
                        }
                    }
                    if (userQueue.isEmpty()) {
                        setResult(RESULT_OK);
                        finish();
                    }else{
                        currentUser = userQueue.remove();
                        liveData.removeObservers(RateActivity.this);
                        prepareCardView();
                    }
                }
            });
        }else{
            AssistantModel model = new AssistantModel();
            model.setAssistantUid(requesterUid);
            model.setRole("requester");
            push(model);
            currentUser = userQueue.remove();
            prepareCardView();
        }
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.rateButton:
                progressLayout.setVisibility(View.VISIBLE);
                rateCardLayout.setVisibility(View.GONE);
                helper.rate(requestUid, currentUser.getAssistantUid(), Double.parseDouble(Float.toString(ratingBar.getRating())), descriptionEditText.getText().toString(), this);
                break;
            case R.id.ignoreButton:
                progressLayout.setVisibility(View.VISIBLE);
                rateCardLayout.setVisibility(View.GONE);
                profileImage.setImageResource(R.drawable.bg_gray);
                if (userQueue.isEmpty()) {
                    setResult(RESULT_OK);
                    finish();
                }else {
                    currentUser = userQueue.remove();
                    prepareCardView();
                }
                break;
        }
        //forDebug();
    }

    private void bindView(){
        progressLayout = findViewById(R.id.progress_layout);
        rateCardLayout = findViewById(R.id.rateCardLayout);
        profileImage = findViewById(R.id.imageView);
        nameTextView = findViewById(R.id.name);
        roleTextView = findViewById(R.id.role);
        ratingBar = findViewById(R.id.ratingBar);
        descriptionEditText = findViewById(R.id.description_text);
        rateButton = findViewById(R.id.rateButton);
        ignoreButton = findViewById(R.id.ignoreButton);
        rateButton.setOnClickListener(this);
        ignoreButton.setOnClickListener(this);
    }

    private void prepareCardView(){
        mDatabase.getReference().child("users").child(currentUser.getAssistantUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    userModel = dataSnapshot.getValue(UserModel.class);
                    nameTextView.setText(String.format("%s %s", userModel.getFirstName(), userModel.getLastName()));
                    if ("requester".equals(currentUser.getRole())){
                        roleTextView.setText("ผู้ร้องขอ");
                    }else{
                        roleTextView.setText(String.format("%s", currentUser.getRole().equals("user") ? "อาสาสมัคร":"เข้าหน้าที่"));
                    }
                    mStorage.getReference().child("profile").child(currentUser.getAssistantUid()+".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Picasso.with(RateActivity.this)
                                    .load(uri)
                                    .resize(500, 500)
                                    .into(profileImage);
                            descriptionEditText.setText("");
                            ratingBar.setRating(3);
                            progressLayout.setVisibility(View.GONE);
                            rateCardLayout.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void push(AssistantModel val){
        userQueue.add(val);
    }

    @Override
    public void onComplete(@NonNull Task<Void> task) {
        if (task.isSuccessful()){
            if (userQueue.isEmpty()) {
                setResult(RESULT_OK);
                finish();
            }else {
                profileImage.setImageResource(R.drawable.bg_gray);
                currentUser = userQueue.remove();
                prepareCardView();
            }
        }
    }

    private void forDebug(){
        if (userQueue.isEmpty()) {
            finish();
        }else {
            profileImage.setImageResource(R.drawable.bg_gray);
            currentUser = userQueue.remove();
            prepareCardView();
        }
    }
}
