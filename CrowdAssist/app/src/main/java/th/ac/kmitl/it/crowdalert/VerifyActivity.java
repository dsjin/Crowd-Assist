package th.ac.kmitl.it.crowdalert;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import th.ac.kmitl.it.crowdalert.model.VerifyModel;
import th.ac.kmitl.it.crowdalert.util.DatabaseHelper;

public class VerifyActivity extends AppCompatActivity implements View.OnClickListener, DatePickerDialog.OnDateSetListener, OnCompleteListener<Void>{
    private Integer ID_CARD_PICK_IMAGE = 9001;
    private Integer PHOTO_WITH_CARD_PICK_IMAGE = 9002;
    private EditText firstName;
    private EditText lastName;
    private String username;
    private EditText dateOfBirth;
    private Spinner gender;
    private EditText idNumber;
    private RelativeLayout idCardLayout;
    private RelativeLayout photoWithIdCardLayout;
    private ImageView idCardImage;
    private ImageView photoWithIdCardImage;
    private ScrollView mainLayout;
    private LinearLayout progressLayout;
    private ProgressBar circularProgress;
    private ProgressBar horizontalProgress;
    private FirebaseDatabase mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseStorage mStorage;
    private ArrayAdapter<CharSequence> adapterGenderList;
    private Uri idCardURI;
    private Uri photoWithCardURI;
    private Calendar myCalendar;
    private LinearLayout confirmButton;
    private DatabaseHelper helper;
    int themeResId = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);
        Toolbar toolbar = findViewById(R.id.toolbar_top);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        helper = new DatabaseHelper(this);
        myCalendar = Calendar.getInstance();
        firstName = findViewById(R.id.name);
        lastName = findViewById(R.id.lastname);
        dateOfBirth = findViewById(R.id.dateOfBirth);
        dateOfBirth.setOnClickListener(this);
        dateOfBirth.setKeyListener(null);
        dateOfBirth.setFocusable(false);
        gender = findViewById(R.id.gender);
        confirmButton = findViewById(R.id.confirm_button);
        confirmButton.setOnClickListener(this);
        adapterGenderList = ArrayAdapter.createFromResource(this, R.array.gender, R.layout.spinner);
        adapterGenderList.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gender.setAdapter(adapterGenderList);
        idNumber = findViewById(R.id.idNumber);
        idCardLayout = findViewById(R.id.idCardImage);
        idCardLayout.setOnClickListener(this);
        photoWithIdCardLayout = findViewById(R.id.photoWithIdCardImage);
        photoWithIdCardLayout.setOnClickListener(this);
        idCardImage = findViewById(R.id.add_photo);
        photoWithIdCardImage = findViewById(R.id.add_photo1);
        mainLayout = findViewById(R.id.mainLayout);
        progressLayout = findViewById(R.id.progress_layout);
        circularProgress = findViewById(R.id.circularProgress);
        horizontalProgress = findViewById(R.id.horizontalProgress);
        mDatabase = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mStorage = FirebaseStorage.getInstance();
        mainLayout.setVisibility(View.GONE);
        progressLayout.setVisibility(View.VISIBLE);
        mDatabase.getReference("users").child(mUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String genderValue = dataSnapshot.child("gender").getValue(String.class).equals("male")?"ชาย":"หญิง";
                    firstName.setText(dataSnapshot.child("firstName").getValue(String.class));
                    lastName.setText(dataSnapshot.child("lastName").getValue(String.class));
                    idNumber.setText(dataSnapshot.child("idCard").getValue(String.class));
                    myCalendar.setTimeInMillis(dataSnapshot.child("dateOfBirth").getValue(Long.class));
                    dateOfBirth.setText(convertDate(new Date(dataSnapshot.child("dateOfBirth").getValue(Long.class))));
                    gender.setSelection(adapterGenderList.getPosition(genderValue));
                    username = dataSnapshot.child("username").getValue(String.class);
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
    public void onClick(View view) {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");
        Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");
        Intent chooserIntent = Intent.createChooser(getIntent, "เลือกภาพ");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});
        switch (view.getId()){
            case R.id.dateOfBirth:
                DatePickerDialog dialog = new DatePickerDialog(this, themeResId, this, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH));
                dialog.getDatePicker().setMaxDate(Calendar.getInstance().getTime().getTime());
                dialog.show();
                break;
            case R.id.idCardImage:
                startActivityForResult(Intent.createChooser(chooserIntent, "เลือกรูปภาพ"),
                        ID_CARD_PICK_IMAGE);
                break;
            case R.id.photoWithIdCardImage:
                startActivityForResult(Intent.createChooser(chooserIntent, "เลือกรูปภาพ"),
                        PHOTO_WITH_CARD_PICK_IMAGE);
                break;
            case R.id.confirm_button:
                if (idCardURI != null && photoWithCardURI != null && !firstName.getText().toString().isEmpty() && !lastName.getText().toString().isEmpty() && !dateOfBirth.getText().toString().isEmpty() && !idNumber.getText().toString().isEmpty()){
                    mainLayout.setVisibility(View.GONE);
                    progressLayout.setVisibility(View.VISIBLE);
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                    send();
                }
                break;
        }
    }

    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
        myCalendar.set(Calendar.YEAR, i);
        myCalendar.set(Calendar.MONTH, i1);
        myCalendar.set(Calendar.DAY_OF_MONTH, i2);

        dateOfBirth.setText(convertDate(myCalendar.getTime()));
    }

    private String convertDate(Date date) {
        String myFormat = "MM/dd/yy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.forLanguageTag("th-TH"));

        return sdf.format(date);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ID_CARD_PICK_IMAGE){
            if (resultCode == RESULT_OK && data != null){
                idCardURI = data.getData();
                Picasso.with(this)
                        .load(idCardURI)
                        .into(idCardImage);
            }
        }else if (requestCode == PHOTO_WITH_CARD_PICK_IMAGE){
            if (resultCode == RESULT_OK && data != null){
                photoWithCardURI = data.getData();
                Picasso.with(this)
                        .load(photoWithCardURI)
                        .into(photoWithIdCardImage);
            }
        }
    }

    private void send(){
        Observable.zip(uploadObservable("idCard", idCardURI), uploadObservable("photoWithCard", photoWithCardURI), zipFunction())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .take(1)
                .subscribe(action1());
    }

    private Observable<String> uploadObservable(final String name, final Uri uri){
        return Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(final ObservableEmitter<String> emitter) throws Exception {
                mStorage.getReference("verify").child(mUser.getUid()).child(name+".png").putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()){
                            emitter.onNext(""+task.getResult().getDownloadUrl());
                        }
                    }
                });
            }
        });
    }

    private Consumer<HashMap<String, String>> action1(){
        return new Consumer<HashMap<String, String>>() {
            @Override
            public void accept(HashMap<String, String> path) throws Exception {
                //Save In database
                String selectedGender = gender.getSelectedItem().toString().equals("ชาย") ? "male" : "female";
                VerifyModel model = new VerifyModel();
                model.setName(firstName.getText().toString() + " " + lastName.getText().toString());
                model.setFirstName(firstName.getText().toString());
                model.setLastName(lastName.getText().toString());
                model.setDateOfBirth(myCalendar.getTimeInMillis());
                model.setGender(selectedGender);
                model.setRole("user");
                model.setIdCard(idNumber.getText().toString());
                model.setIdCardPath(path.get("idCard"));
                model.setPhotoWithCardPath(path.get("photoWithCard"));
                model.setUsername(username);
                model.setUid(mUser.getUid());
                helper.createVerifyList(model, VerifyActivity.this);
            }
        };
    }

    private BiFunction<String, String, HashMap<String, String>> zipFunction(){
        return new BiFunction<String, String, HashMap<String, String>>() {
            @Override
            public HashMap<String, String> apply(String s, String s2) throws Exception {
                HashMap<String, String> result = new HashMap<>();
                result.put("idCard", s);
                result.put("photoWithCard", s2);
                return result;
            }
        };
    }

    @Override
    public void onComplete(@NonNull Task<Void> task) {
        mainLayout.setVisibility(View.VISIBLE);
        progressLayout.setVisibility(View.GONE);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Snackbar.make(mainLayout, "ส่งเรียบร้อยแล้ว รอการดำเนินการภายใน 1 วัน", Snackbar.LENGTH_SHORT).show();
    }
}
