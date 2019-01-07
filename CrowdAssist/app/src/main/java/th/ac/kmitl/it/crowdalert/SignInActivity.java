package th.ac.kmitl.it.crowdalert;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import th.ac.kmitl.it.crowdalert.util.PermissionHepler;

public class SignInActivity extends AppCompatActivity implements View.OnClickListener {
    final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mRef;

    private EditText username;
    private EditText password;
    private LinearLayout signin;
    private LinearLayout signup;
    private ConstraintLayout constraintLayout;
    private LinearLayout progress;
    private View primaryView;
    private PermissionHepler permissionHepler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN );
        constraintLayout = findViewById(R.id.mainLayout);
        progress = findViewById(R.id.progress_layout);
        primaryView = findViewById(R.id.primary_view);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance();

        username = findViewById(R.id.inputUsername);
        password = findViewById(R.id.inputPassword);
        signin = findViewById(R.id.sign_in_button);
        signup = findViewById(R.id.sign_up_button);

        signin.setOnClickListener(this);
        signup.setOnClickListener(this);

        permissionHepler = new PermissionHepler(this);
    }

    @Override
    protected void onStart() {
        if (mUser != null){
            finish();
        }
        permissionHepler.setPermission();
        super.onStart();
    }

    @Override
    public void onClick(View view) {
        LinearLayout button = (LinearLayout) view;
        switch (button.getId()){
            case R.id.sign_in_button:
                constraintLayout.setVisibility(View.GONE);
                progress.setVisibility(View.VISIBLE);
                resolveEmail();
                break;
            case R.id.sign_up_button:
                Intent intent = new Intent(this, SignUpActivity.class);
                startActivity(intent);
                break;
        }
    }

    private void resolveEmail(){
        mRef = mDatabase.getReference("user_id").child(username.getText().toString());
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    signIn(dataSnapshot.getValue(String.class));
                }else{
                    constraintLayout.setVisibility(View.VISIBLE);
                    progress.setVisibility(View.GONE);
                    Snackbar.make(primaryView, "Username or Password incorrect.", Snackbar.LENGTH_SHORT)
                            .show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private void signIn(String email){
        String userPassword = password.getText().toString();
        mAuth.signInWithEmailAndPassword(email, userPassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            Intent intent = new Intent();
                            setResult(Activity.RESULT_OK, intent);
                            finish();
                        }else{
                            constraintLayout.setVisibility(View.VISIBLE);
                            progress.setVisibility(View.GONE);
                            Snackbar.make(primaryView, "Username or Password incorrect.", Snackbar.LENGTH_SHORT)
                                    .show();
                        }
                    }
                });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS:
                Map<String, Integer> perms = new HashMap<>();
                perms.put(android.Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);
                if (!(perms.get(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)){
                    Toast.makeText(this, "การอนุญาติไม่สมบูณณ์ กรุณารันแอพลิเคชั่นใหม่อีกครั้ง", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("คุณต้องการออกจากแอพพลิเคชันใช่หรือไม่")
                .setCancelable(false)
                .setPositiveButton("ใช่", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SignInActivity.this.finish();
                    }
                })
                .setNegativeButton("ไม่ใช่", null)
                .show();
    }
}
