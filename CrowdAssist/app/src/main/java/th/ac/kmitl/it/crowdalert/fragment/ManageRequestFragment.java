package th.ac.kmitl.it.crowdalert.fragment;


import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import th.ac.kmitl.it.crowdalert.R;
import th.ac.kmitl.it.crowdalert.component.ExpandableListView;
import th.ac.kmitl.it.crowdalert.model.Item;
import th.ac.kmitl.it.crowdalert.util.ConvertHelper;

public class ManageRequestFragment extends Fragment implements ChildEventListener {
    List<Item> list;
    List<Item> child1;
    List<Item> child2;
    List<Item> child3;
    List<Item> child4;
    private String defaultRef = "https://senior-project-it.firebaseio.com/profile/non_emergency/";
    private FirebaseDatabase mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private RecyclerView recyclerView;
    private ExpandableListView adapter;
    private LinearLayout progreesBar;

    public ManageRequestFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        list = new ArrayList<>();
        child1 = new ArrayList<>();
        child2 = new ArrayList<>();
        child3 = new ArrayList<>();
        child4 = new ArrayList<>();
        setupList();
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance();
        mDatabase.getReference("/users/"+mUser.getUid()+"/non_emergency/"+"type1/").addChildEventListener(this);
        mDatabase.getReference("/users/"+mUser.getUid()+"/non_emergency/"+"type2/").addChildEventListener(this);
        mDatabase.getReference("/users/"+mUser.getUid()+"/non_emergency/"+"type3/").addChildEventListener(this);
        /*
        mDatabase.getReference("/users/"+mUser.getUid()+"/non_emergency/"+"type1/").addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("TAG","We're done loading the initial "+dataSnapshot.getChildrenCount()+" items");
                finish1 = true;
                if (finish1 && finish2 && finish3){
                    progreesBar.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }
            public void onCancelled(DatabaseError firebaseError) { }
        });
        mDatabase.getReference("/users/"+mUser.getUid()+"/non_emergency/"+"type2/").addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("TAG","We're done loading the initial "+dataSnapshot.getChildrenCount()+" items");
                finish2 = true;
                if (finish1 && finish2 && finish3){
                    progreesBar.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }
            public void onCancelled(DatabaseError firebaseError) { }
        });
        mDatabase.getReference("/users/"+mUser.getUid()+"/non_emergency/"+"type3/").addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("TAG","We're done loading the initial "+dataSnapshot.getChildrenCount()+" items");
                finish3 = true;
                if (finish1 && finish2 && finish3){
                    progreesBar.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }
            public void onCancelled(DatabaseError firebaseError) { }
        });*/
        mDatabase.getReference("/users/"+mUser.getUid()+"/non_emergency/").addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("TAG","We're done loading the initial "+dataSnapshot.getChildrenCount()+" items");
                progreesBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
            public void onCancelled(DatabaseError firebaseError) { }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage_request, container, false);
        progreesBar = view.findViewById(R.id.progress_layout);
        recyclerView = view.findViewById(R.id.requestList);
        progreesBar.setVisibility(View.VISIBLE);
        adapter = new ExpandableListView(list, getActivity());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false){
            @Override
            public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
                LinearSmoothScroller smoothScroller = new LinearSmoothScroller(getActivity()) {

                    private final float SPEED = 50000f;// Change this value (default=25f)

                    @Override
                    protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                        return SPEED / displayMetrics.densityDpi;
                    }

                };
                smoothScroller.setTargetPosition(position);
                startSmoothScroll(smoothScroller);
            }
        });
        return view;
    }

    private void setupMock(){
        list.add(new Item(ExpandableListView.HEADER, "อุบัติเหตุ 1", child1));
        list.add(new Item(ExpandableListView.HEADER, "อุบัติเหตุ 2", child2));
        list.add(new Item(ExpandableListView.HEADER, "อุบัติเหตุ 3", child3));
        /*
        child1.add(new Item(ExpandableListView.CHILD, "1234"));
        child2.add(new Item(ExpandableListView.CHILD, "1234"));
        child2.add(new Item(ExpandableListView.CHILD, "1234"));
        child3.add(new Item(ExpandableListView.CHILD, "1234"));
        child3.add(new Item(ExpandableListView.CHILD, "1234"));
        child3.add(new Item(ExpandableListView.CHILD, "1234"));*/
    }

    private void setupList(){
        //TODO set list
        list.add(new Item(ExpandableListView.HEADER, "อัคคีภัย", child1));
        list.add(new Item(ExpandableListView.HEADER, "อุบัติเหตุทางถนน", child2));
        list.add(new Item(ExpandableListView.HEADER, "อุบัติเหตุทางน้ำ", child3));
        list.add(new Item(ExpandableListView.HEADER, "อื่นๆ", child4));
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        if (progreesBar.getVisibility() == View.GONE){
            recyclerView.setVisibility(View.GONE);
            progreesBar.setVisibility(View.VISIBLE);
        }
        String dataRef = dataSnapshot.getRef().toString().replace(defaultRef,"");
        String[] listOfType = new String[]{"type1","type2","type3","type4"};
        String type = "";
        Long timestamp;
        String head;
        String title;
        Date dateTime;
        for(String temp : listOfType){
            if (dataRef.contains(temp)){
                type = temp;
                break;
            }
        }
        switch(type){
            case "type1":
                timestamp = dataSnapshot.child("timestamp").getValue(Long.class);
                head = dataSnapshot.getKey();
                title = dataSnapshot.child("title").getValue(String.class);
                child1.add(new Item(ExpandableListView.CHILD, ConvertHelper.ConvertTimestampToDate(timestamp), head, timestamp));
                adapter.notifyItemInserted(list.size());
                break;
            case "type2":
                timestamp = dataSnapshot.child("timestamp").getValue(Long.class);
                head = dataSnapshot.getKey();
                child2.add(new Item(ExpandableListView.CHILD, ConvertHelper.ConvertTimestampToDate(timestamp), head, timestamp));
                adapter.notifyItemInserted(list.size());
                break;
            case "type3":
                timestamp = dataSnapshot.child("timestamp").getValue(Long.class);
                head = dataSnapshot.getKey();
                child3.add(new Item(ExpandableListView.CHILD, ConvertHelper.ConvertTimestampToDate(timestamp), head, timestamp));
                adapter.notifyItemInserted(list.size());
                break;
            case "type4":
                timestamp = dataSnapshot.child("timestamp").getValue(Long.class);
                head = dataSnapshot.getKey();
                child3.add(new Item(ExpandableListView.CHILD, ConvertHelper.ConvertTimestampToDate(timestamp), head, timestamp));
                adapter.notifyItemInserted(list.size());
                break;
        }
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {
    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }
}
