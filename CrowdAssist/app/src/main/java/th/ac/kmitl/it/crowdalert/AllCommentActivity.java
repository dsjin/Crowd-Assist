package th.ac.kmitl.it.crowdalert;

import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import th.ac.kmitl.it.crowdalert.component.RecyclerRatingAndComment;
import th.ac.kmitl.it.crowdalert.model.RatingAndCommentModel;

public class AllCommentActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private RecyclerRatingAndComment adapter;
    private ArrayList<RatingAndCommentModel> list;
    private Query query;
    private DocumentSnapshot lastVisible;
    private Boolean isLoading;
    private LinearLayoutManager manager;
    private TextView text;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_comment);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar();
        setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        recyclerView = findViewById(R.id.recyclerView);
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        list = new ArrayList<>();
        manager = new LinearLayoutManager(this);
        adapter = new RecyclerRatingAndComment(list, this);
        text = findViewById(R.id.text);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
        isLoading = false;
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
        query = db.collection("request")
                .document(getIntent().getStringExtra("uid"))
                .collection(mUser.getUid())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(10);
        getDoc();
    }

    private void getDoc(){
        isLoading = true;
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (DocumentSnapshot document : task.getResult()) {
                        String uid = document.getId();
                        String comment = document.getString("comment");
                        Double rating = document.getDouble("rating");
                        String name = document.getString("name");
                        RatingAndCommentModel model = new RatingAndCommentModel();
                        model.setUid(uid);
                        model.setName(name);
                        model.setRating(rating);
                        model.setComment(comment);
                        list.add(model);
                        adapter.notifyDataSetChanged();
                    }

                    if (task.getResult().size() > 0) {
                        lastVisible = task.getResult().getDocuments()
                                .get(task.getResult().size() - 1);
                        query = db.collection("request")
                                .document(getIntent().getStringExtra("uid"))
                                .collection(mUser.getUid())
                                .orderBy("timestamp", Query.Direction.DESCENDING)
                                .startAfter(lastVisible)
                                .limit(10);
                    }else{
                        if (list.isEmpty()){
                            text.setVisibility(View.VISIBLE);
                        }else{
                            Toast.makeText(AllCommentActivity.this, "แสดงคอมเม้นครบแล้ว", Toast.LENGTH_SHORT).show();
                        }
                    }
                    isLoading = false;
                }
            }
        });
    }
}
