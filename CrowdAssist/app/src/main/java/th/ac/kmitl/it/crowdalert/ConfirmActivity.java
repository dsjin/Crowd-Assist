package th.ac.kmitl.it.crowdalert;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.chaos.view.PinView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import th.ac.kmitl.it.crowdalert.util.DatabaseHelper;

public class ConfirmActivity extends AppCompatActivity implements View.OnClickListener, OnCompleteListener<Void>{
    private PinView pinView;
    private FirebaseDatabase mDatabase;
    private FirebaseAuth mAuth;
    private SharedPreferences sp;
    private final String SP_REQUEST = "request_information";
    private String requestUid;
    private DatabaseHelper helper;
    private String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);
        Toolbar toolbar = findViewById(R.id.toolbar_top);
        setSupportActionBar(toolbar);
        setTitle(" ");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mDatabase = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        helper = new DatabaseHelper(this);
        sp = getSharedPreferences(SP_REQUEST, Context.MODE_PRIVATE);
        requestUid = sp.getString("request_uid", null);
        type = sp.getString("type", null);
        pinView = findViewById(R.id.pinView);
        pinView.setTextColor(
                ResourcesCompat.getColor(getResources(), R.color.colorAccent, getTheme()));
        pinView.setLineColor(
                ResourcesCompat.getColor(getResources(), R.color.colorTextPrimaryDark, getTheme()));
        pinView.setItemCount(4);
        pinView.setAnimationEnable(true);

        LinearLayout confirm = findViewById(R.id.confirm_button);
        confirm.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        mDatabase.getReference("users").child(mAuth.getCurrentUser().getUid()).child("pin").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.getValue(String.class).equals(pinView.getText().toString())){
                        helper.closeRequest(requestUid, ConfirmActivity.this, type);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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
    public void onComplete(@NonNull Task<Void> task) {
        if (task.isSuccessful()){
            //SharedPreferences.Editor editor = sp.edit();
            //editor.putString("mode", "rate");
            //editor.apply();
            //Intent intent = new Intent();
            //setResult(RESULT_OK, intent);
            finish();
        }else{
            Toast.makeText(this, "มีข้อผิดพลาดเกิดขึ้น กรุณาลองใหม่", Toast.LENGTH_SHORT).show();
        }
    }
}
