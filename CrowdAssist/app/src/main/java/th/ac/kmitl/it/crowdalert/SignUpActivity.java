package th.ac.kmitl.it.crowdalert;

import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import th.ac.kmitl.it.crowdalert.component.TermsAndConditionsDialog;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private EditText username;
    private EditText password;
    private EditText repeatPassword;
    private EditText email;
    private EditText firstName;
    private EditText lastName;
    private EditText idCard;
    private LinearLayout signUp;
    private Boolean firstTime = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN );
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        username = findViewById(R.id.inputUsername);
        password = findViewById(R.id.inputPassword);
        repeatPassword = findViewById(R.id.inputConfirmPassword);
        email = findViewById(R.id.inputEmail);
        firstName = findViewById(R.id.inputFirstname);
        lastName = findViewById(R.id.inputLastname);
        idCard = findViewById(R.id.inputIDcard);
        signUp = findViewById(R.id.sign_up_button);

        signUp.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (firstTime){
            TermsAndConditionsDialog dialog = new TermsAndConditionsDialog(this);
            dialog.show();
            firstTime = false;
        }
    }

    @Override
    public void onClick(View view) {
        if (!password.getText().toString().equals(repeatPassword.getText().toString())){
            return;
        }
        mDatabase.getReference("user_id").child(username.getText().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    mAuth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                            .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isComplete()){
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        UserSignUp information = new UserSignUp(username.getText().toString(), email.getText().toString(), firstName.getText().toString(), lastName.getText().toString(), idCard.getText().toString(), "user");
                                        mDatabase.getReference("users").child(user.getUid()).setValue(information);
                                        mDatabase.getReference("user_id").child(username.getText().toString()).setValue(email.getText().toString());
                                        finish();
                                    }else{

                                    }
                                }
                            });
                }else{
                    Toast.makeText(SignUpActivity.this, "Username นี้มีคนใช้งานแล้ว", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public static class UserSignUp{
        public String username;
        public String email;
        public String firstName;
        public String lastName;
        public String idCard;
        public String role;
        public Boolean firstTime;
        public Boolean verify;

        public UserSignUp() {
        }

        public UserSignUp(String username, String email, String firstName, String lastName, String idCard, String role) {
            this.username = username;
            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
            this.idCard = idCard;
            this.role = role;
            this.firstTime = true;
            this.verify = false;
        }
    }
}
