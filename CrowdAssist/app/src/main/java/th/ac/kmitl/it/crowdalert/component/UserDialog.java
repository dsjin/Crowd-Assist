package th.ac.kmitl.it.crowdalert.component;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.InsetDrawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import org.joda.time.LocalDate;
import org.joda.time.Years;

import java.util.Calendar;
import java.util.Locale;

import th.ac.kmitl.it.crowdalert.R;
import th.ac.kmitl.it.crowdalert.model.AssistantModel;
import th.ac.kmitl.it.crowdalert.model.Request;
import th.ac.kmitl.it.crowdalert.model.UserModel;
import th.ac.kmitl.it.crowdalert.util.LocationHelper;

public class UserDialog extends Dialog implements View.OnClickListener{
    private AssistantModel assistant;
    private TextView name;
    private TextView gender;
    private TextView age;
    private TextView roleTextView;
    private TextView timeEstimated;
    private ImageView userImage;
    private FirebaseStorage mStorage;
    private FirebaseDatabase mDatabase;
    private Request data;
    private LinearLayout close;
    private LinearLayout progress;
    private LinearLayout mainLayout;
    private Uri uri;

    public UserDialog(Context ctx, AssistantModel assistant, Request data, Uri uri) {
        this.assistant = assistant;
        this.data = data;
        super.ctx = ctx;
        mStorage = FirebaseStorage.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        this.uri = uri;
        setupDialog();
    }

    @Override
    void setupDialog() {
        mBuilder = new AlertDialog.Builder(ctx);
        LayoutInflater inflater = LayoutInflater.from(ctx);
        View view = inflater.inflate(R.layout.user_information_dialog, null);
        name = view.findViewById(R.id.name);
        age = view.findViewById(R.id.age);
        gender = view.findViewById(R.id.gender);
        roleTextView = view.findViewById(R.id.role);
        timeEstimated = view.findViewById(R.id.timeEstimated);
        userImage = view.findViewById(R.id.userImageView);
        close = view.findViewById(R.id.close);
        progress = view.findViewById(R.id.progress_layout);
        mainLayout = view.findViewById(R.id.mainLayout);
        progress.setVisibility(View.VISIBLE);
        mainLayout.setVisibility(View.GONE);
        mDatabase.getReference().child("users").child(assistant.getAssistantUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    UserModel userInfo = dataSnapshot.getValue(UserModel.class);
                    LocalDate birthOfDate = new LocalDate(userInfo.getDateOfBirth());
                    LocalDate now = new LocalDate(Calendar.getInstance().getTime().getTime());
                    name.setText(String.format("%s %s", userInfo.getFirstName(), userInfo.getLastName()));
                    gender.setText(String.format("%s, ", "male".equals(userInfo.getGender())? "ชาย" : "หญิง"));
                    age.setText(String.format(Locale.forLanguageTag("th-TH"), "%d", Years.yearsBetween(birthOfDate, now).getYears()));
                    roleTextView.setText(String.format("%s", "user".equals(dataSnapshot.child("role").getValue(String.class))? "อาสาสมัคร":"เจ้าหน้าที่"));
                    com.google.maps.model.LatLng userPosition = new com.google.maps.model.LatLng(assistant.getLat(), assistant.getLng());
                    com.google.maps.model.LatLng requesterPosition = new com.google.maps.model.LatLng(data.getLat(), data.getLng());
                    timeEstimated.setText(String.format("คาดว่าจะมาถึงในอีก %s", LocationHelper.getEstimateTravelTime(userPosition, requesterPosition)));
                    Picasso.with(ctx)
                            .load(uri)
                            .resize(500, 500)
                            .into(userImage);
                    progress.setVisibility(View.GONE);
                    mainLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        close.setOnClickListener(this);
        mBuilder.setView(view);
    }

    public void show(){
        dialog = mBuilder.create();
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        ColorDrawable back = new ColorDrawable(Color.TRANSPARENT);
        InsetDrawable inset = new InsetDrawable(back, 60);
        dialog.getWindow().setBackgroundDrawable(inset);
        dialog.show();
    }

    @Override
    public void onClick(View view) {
        dialog.cancel();
    }
}
