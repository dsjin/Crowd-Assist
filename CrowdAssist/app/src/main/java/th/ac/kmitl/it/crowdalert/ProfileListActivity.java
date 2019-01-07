package th.ac.kmitl.it.crowdalert;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import th.ac.kmitl.it.crowdalert.component.RecycleProfileList;
import th.ac.kmitl.it.crowdalert.model.ProfileListModel;
import th.ac.kmitl.it.crowdalert.util.ConvertHelper;

public class ProfileListActivity extends AppCompatActivity {
    private FirebaseDatabase mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseFirestore mFireStore;
    private String MODE;
    private Query REF;
    private ArrayList<ProfileListModel> list;
    private RecyclerView recyclerView;
    private RecycleProfileList adapter;
    private Boolean isLoading;
    private DocumentSnapshot lastVisible;
    private Snackbar snackbar;
    private LinearLayoutManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_list);
        Toolbar toolbar = findViewById(R.id.toolbar_top);
        setSupportActionBar(toolbar);
        TextView title = toolbar.findViewById(R.id.toolbar_title);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Intent intent = getIntent();
        MODE = intent.getStringExtra("mode");
        mDatabase = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        isLoading = false;
        mFireStore = FirebaseFirestore.getInstance();
        list = new ArrayList<>();
        recyclerView = findViewById(R.id.list);
        adapter = new RecycleProfileList(list, this, MODE);
        manager = new LinearLayoutManager(this);
        snackbar = Snackbar.make(recyclerView, "กำลังดึงข้อมูล", Snackbar.LENGTH_INDEFINITE);
        recyclerView.setLayoutManager(manager);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0){
                    if (!recyclerView.canScrollVertically(RecyclerView.FOCUS_DOWN)){
                        if (!isLoading){
                            getDoc();
                        }
                    }
                }
            }
        });
        recyclerView.setAdapter(adapter);
        switch (MODE){
            case "emergency":
                //REF = mDatabase.getReference().child("emergency").orderByChild("requesterUid").equalTo(mUser.getUid());
                REF = mFireStore.collection("user").document(mUser.getUid()).collection("emergency")
                        .orderBy("timestamp", Query.Direction.DESCENDING)
                        .limit(10);
                title.setText(String.format("%s", "รายการคำร้องขอฉุกเฉิน"));
                break;
            case "non-emergency":
                //REF = mDatabase.getReference().child("non_emergency").orderByChild("requesterUid").equalTo(mUser.getUid());
                REF = mFireStore.collection("user").document(mUser.getUid()).collection("general")
                        .orderBy("timestamp", Query.Direction.DESCENDING)
                        .limit(10);
                title.setText(String.format("%s", "รายการคำร้องขอทั่วไป"));
                break;
            case "assistance":
                /*
                ProfileListModel model = new ProfileListModel();
                model.setTitle(ConvertHelper.ConvertTimestampToDate(Long.parseLong("1518620876069")));
                model.setUid("-L5JqscDBCJb5nPV-1RY");
                model.setTimestamp(Long.parseLong("1518620876069"));
                title.setText(String.format("%s", "รายการการช่วยเหลือ"));
                model.setType("emergency");
                list.add(model);
                adapter.notifyDataSetChanged();*/
                REF = mFireStore.collection("user").document(mUser.getUid()).collection("assistance")
                        .orderBy("timestamp", Query.Direction.DESCENDING)
                        .limit(10);
                title.setText(String.format("%s", "รายการการช่วยเหลือ"));
                break;
        }
        /*
        if (REF != null){
            REF.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot data : dataSnapshot.getChildren()){
                        ProfileListModel model = new ProfileListModel();
                        model.setTitle(ConvertHelper.ConvertTimestampToDate(data.child("timestamp").getValue(Long.class)));
                        model.setUid(data.getKey());
                        model.setTimestamp(data.child("timestamp").getValue(Long.class));
                        list.add(model);
                        adapter.notifyDataSetChanged();
                    }
                    Collections.sort(list, new Comparator<ProfileListModel>() {
                        @Override
                        public int compare(ProfileListModel t0, ProfileListModel t1) {
                            return t0.getTimestamp() > t1.getTimestamp() ? -1 :(t0.getTimestamp() < t1.getTimestamp() ? 1 : 0);
                        }
                    });
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }*/
        getDoc();
    }
    private void getDoc() {
        isLoading = true;
        snackbar.show();
        REF.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        String uid = document.getId();
                        ProfileListModel model = new ProfileListModel();
                        model.setTitle(ConvertHelper.ConvertTimestampToDate(document.getLong("timestamp")));
                        model.setUid(uid);
                        model.setTimestamp(document.getLong("timestamp"));
                        if (MODE.equals("assistance")) {
                            model.setType(document.getString("mode"));
                        }
                        list.add(model);
                        adapter.notifyDataSetChanged();
                    }

                    if (task.getResult().size() > 0) {
                        lastVisible = task.getResult().getDocuments()
                                .get(task.getResult().size() - 1);
                        switch (MODE) {
                            case "emergency":
                                REF = mFireStore.collection("user").document(mUser.getUid()).collection("emergency")
                                        .orderBy("timestamp", Query.Direction.DESCENDING)
                                        .startAfter(lastVisible)
                                        .limit(10);
                                break;
                            case "non-emergency":
                                REF = mFireStore.collection("user").document(mUser.getUid()).collection("general")
                                        .orderBy("timestamp", Query.Direction.DESCENDING)
                                        .startAfter(lastVisible)
                                        .limit(10);
                                break;
                            case "assistance":
                                REF = mFireStore.collection("user").document(mUser.getUid()).collection("assistance")
                                        .orderBy("timestamp", Query.Direction.DESCENDING)
                                        .startAfter(lastVisible)
                                        .limit(10);
                                break;
                        }
                    } else {
                        if (list.isEmpty()){
                            TextView text = findViewById(R.id.text);
                            text.setVisibility(View.VISIBLE);
                        }else{
                            Toast.makeText(ProfileListActivity.this, "แสดงรายการครบแล้ว", Toast.LENGTH_SHORT).show();
                        }
                    }
                    isLoading = false;
                    snackbar.dismiss();
                }
            }
        });
    }
}
