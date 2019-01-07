package th.ac.kmitl.it.crowdalert;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import th.ac.kmitl.it.crowdalert.component.EditConfirmDialog;
import th.ac.kmitl.it.crowdalert.component.EditPasswordDialog;
import th.ac.kmitl.it.crowdalert.util.DatabaseHelper;

public class EditFirstTimeActivity extends AppCompatActivity implements View.OnClickListener, DatePickerDialog.OnDateSetListener, OnCompleteListener<Void> {
    private ImageView profile;
    private ArrayAdapter<CharSequence> adapterGenderList;
    private Spinner gender;
    private LinearLayout changePassword;
    private LinearLayout confirmPin;
    private EditText name;
    private EditText lastName;
    private EditText dateOfBirth;
    private EditText tel;
    private Calendar myCalendar;
    private EditText idNumber;
    private LinearLayout confirm;
    private Uri uri;
    private DatabaseHelper helper;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private ScrollView mainLayout;
    private LinearLayout progressLayout;
    private ProgressBar circularProgress;
    private ProgressBar horizontalProgress;
    private FirebaseDatabase mDatabase;
    int themeResId = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_first_time);
        profile = findViewById(R.id.imageView);
        profile.setOnClickListener(this);
        gender = findViewById(R.id.gender);
        myCalendar = Calendar.getInstance();
        helper = new DatabaseHelper(this);
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        adapterGenderList = ArrayAdapter.createFromResource(this, R.array.gender, R.layout.spinner);
        adapterGenderList.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gender.setAdapter(adapterGenderList);
        changePassword = findViewById(R.id.changePasswordLayout);
        confirmPin = findViewById(R.id.confirmLayout);
        changePassword.setOnClickListener(this);
        confirmPin.setOnClickListener(this);
        name = findViewById(R.id.name);
        lastName = findViewById(R.id.lastname);
        dateOfBirth = findViewById(R.id.dateOfBirth);
        dateOfBirth.setOnClickListener(this);
        dateOfBirth.setKeyListener(null);
        dateOfBirth.setFocusable(false);
        idNumber = findViewById(R.id.idNumber);
        idNumber.setEnabled(false);
        tel = findViewById(R.id.tel);
        confirm = findViewById(R.id.confirm_button);
        confirm.setOnClickListener(this);
        mainLayout = findViewById(R.id.mainLayout);
        progressLayout = findViewById(R.id.progress_layout);
        circularProgress = findViewById(R.id.circularProgress);
        horizontalProgress = findViewById(R.id.horizontalProgress);
        mDatabase = FirebaseDatabase.getInstance();
        mainLayout.setVisibility(View.GONE);
        progressLayout.setVisibility(View.VISIBLE);
        mDatabase.getReference("users").child(mUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    name.setText(dataSnapshot.child("firstName").getValue(String.class));
                    lastName.setText(dataSnapshot.child("lastName").getValue(String.class));
                    idNumber.setText(dataSnapshot.child("idCard").getValue(String.class));
                    mainLayout.setVisibility(View.VISIBLE);
                    progressLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.imageView:
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.OFF)
                        .setCropShape(CropImageView.CropShape.RECTANGLE)
                        .setAspectRatio(4,4)
                        .start(EditFirstTimeActivity.this);
                break;
            case R.id.changePasswordLayout:
                EditPasswordDialog editPasswordDialog = new EditPasswordDialog(this);
                editPasswordDialog.show();
                break;
            case R.id.confirmLayout:
                EditConfirmDialog editConfirmDialog = new EditConfirmDialog(this);
                editConfirmDialog.show();
                break;
            case R.id.dateOfBirth:
                DatePickerDialog dialog = new DatePickerDialog(this, themeResId, this, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH));
                dialog.getDatePicker().setMaxDate(Calendar.getInstance().getTime().getTime());
                dialog.show();
                break;
            case R.id.confirm_button:
                if (uri != null && !tel.getText().toString().isEmpty() && !name.getText().toString().isEmpty() && !lastName.getText().toString().isEmpty() && !dateOfBirth.getText().toString().isEmpty()){
                    mainLayout.setVisibility(View.GONE);
                    progressLayout.setVisibility(View.VISIBLE);
                    String selectedGender = gender.getSelectedItem().toString().equals("ชาย") ? "male" : "female";
                    helper.updateProfileFirstTime(mUser.getUid(), name.getText().toString(), lastName.getText().toString(), myCalendar.getTimeInMillis(), selectedGender, tel.getText().toString(),this);
                }else {
                    Toast.makeText(this, "กรุณาใส่ข้อมูลให้ครบ", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
        myCalendar.set(Calendar.YEAR, i);
        myCalendar.set(Calendar.MONTH, i1);
        myCalendar.set(Calendar.DAY_OF_MONTH, i2);
        updateLabel();
    }

    private void updateLabel() {
        String myFormat = "MM/dd/yy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.forLanguageTag("th-TH"));

        dateOfBirth.setText(sdf.format(myCalendar.getTime()));
    }
    @Override
    public void onComplete(@NonNull Task<Void> task) {
        if (task.isSuccessful()){
            circularProgress.setVisibility(View.GONE);
            horizontalProgress.setVisibility(View.VISIBLE);

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference ref = storage.getReference().child("profile").child(mUser.getUid()+".jpg");
            ref.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(name.getText().toString())
                            .setPhotoUri(taskSnapshot.getDownloadUrl())
                            .build();
                    mUser.updateProfile(profileUpdates);
                    setResult(RESULT_OK);
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("EditFirstTime | Upload", e.getMessage());
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    Long progress = (100*taskSnapshot.getBytesTransferred()/taskSnapshot
                            .getTotalByteCount());
                    horizontalProgress.setProgress(progress.intValue());
                }
            });
        }else{
            Toast.makeText(this, "มีข้อผิดพลาดเกิดขึ้น กรุณาลองใหม่", Toast.LENGTH_SHORT).show();
        }
    }
}
