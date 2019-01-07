package th.ac.kmitl.it.crowdalert;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import th.ac.kmitl.it.crowdalert.component.EditConfirmDialog;
import th.ac.kmitl.it.crowdalert.component.EditPasswordDialog;

public class EditProfileActivity extends AppCompatActivity implements View.OnClickListener{
    private LinearLayout changePassword;
    private LinearLayout confirmPin;
    private LinearLayout confirm;
    private ImageView profile;
    private Uri uri;
    private LinearLayout progressLayout;
    private ProgressBar circularProgress;
    private ProgressBar horizontalProgress;
    private ScrollView mainLayout;
    private LinearLayout imageLayout;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private Toolbar toolbar;
    private EditText tel;
    private FirebaseDatabase mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_activity);
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance();
        tel = findViewById(R.id.tel);
        mDatabase.getReference().child("users").child(mUser.getUid()).child("tel").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    if (dataSnapshot.getValue(String.class) != null){
                        tel.setText(dataSnapshot.getValue(String.class));
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        setToolbar();
        bindView();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.changePasswordLayout:
                EditPasswordDialog editPasswordDialog = new EditPasswordDialog(this);
                editPasswordDialog.show();
                break;
            case R.id.confirmLayout:
                EditConfirmDialog editConfirmDialog = new EditConfirmDialog(this);
                editConfirmDialog.show();
                break;
            case R.id.imageLayout:
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.OFF)
                        .setCropShape(CropImageView.CropShape.RECTANGLE)
                        .setAspectRatio(4,4)
                        .start(this);
                break;
            case R.id.confirm_button:
                if (uri != null){
                    mainLayout.setVisibility(View.GONE);
                    progressLayout.setVisibility(View.VISIBLE);
                    circularProgress.setVisibility(View.VISIBLE);
                    horizontalProgress.setVisibility(View.GONE);
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                    submit();
                }else if (!tel.getText().toString().isEmpty()){
                    mDatabase.getReference().child("users").child(mUser.getUid()).child("tel").setValue(tel.getText().toString());
                    Snackbar.make(mainLayout, "อัปเดตโปรไฟล์เรียบร้อย", Snackbar.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void bindView(){
        profile = findViewById(R.id.imageView);
        changePassword = findViewById(R.id.changePasswordLayout);
        confirmPin = findViewById(R.id.confirmLayout);
        confirm = findViewById(R.id.confirm_button);
        progressLayout = findViewById(R.id.progress_layout);
        circularProgress = findViewById(R.id.circularProgress);
        horizontalProgress = findViewById(R.id.horizontalProgress);
        mainLayout = findViewById(R.id.mainLayout);
        imageLayout = findViewById(R.id.imageLayout);

        changePassword.setOnClickListener(this);
        confirmPin.setOnClickListener(this);
        imageLayout.setOnClickListener(this);
        confirm.setOnClickListener(this);
        Picasso.with(this)
                .load(mUser.getPhotoUrl())
                .resize(500,500)
                .into(profile);
    }

    private void setToolbar(){
        toolbar = findViewById(R.id.toolbar_top);
        setSupportActionBar(toolbar);
        setTitle(" ");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void submit(){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference ref = storage.getReference().child("profile").child(mUser.getUid()+".jpg");
        mDatabase.getReference().child("users").child(mUser.getUid()).child("tel").setValue(tel.getText());
        ref.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setPhotoUri(taskSnapshot.getDownloadUrl())
                        .build();
                mUser.updateProfile(profileUpdates);
                progressLayout.setVisibility(View.GONE);
                mainLayout.setVisibility(View.VISIBLE);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                Snackbar.make(mainLayout, "อัปเดตโปรไฟล์เรียบร้อย", Snackbar.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("EditProfile | Upload", e.getMessage());
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                Toast.makeText(EditProfileActivity.this, "มีข้อผิดพลาดเกิดขึ้น กรุณาลองใหม่", Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                if (horizontalProgress.getVisibility() == View.GONE){
                    circularProgress.setVisibility(View.GONE);
                    horizontalProgress.setVisibility(View.VISIBLE);
                    horizontalProgress.setProgress(0);
                }
                Long progress = (100*taskSnapshot.getBytesTransferred()/taskSnapshot
                        .getTotalByteCount());
                horizontalProgress.setProgress(progress.intValue());
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                Picasso.with(this)
                        .load(resultUri)
                        .resize(500,500)
                        .into(profile);
                this.uri = resultUri;
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
