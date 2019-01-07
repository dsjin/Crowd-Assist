package th.ac.kmitl.it.crowdalert.component;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
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

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import org.joda.time.LocalDate;
import org.joda.time.Years;

import java.util.Calendar;
import java.util.Locale;

import th.ac.kmitl.it.crowdalert.AcceptActivity;
import th.ac.kmitl.it.crowdalert.R;
import th.ac.kmitl.it.crowdalert.model.UserModel;
import th.ac.kmitl.it.crowdalert.util.DatabaseHelper;

public class AcceptDialog extends Dialog implements View.OnClickListener{
    //EmergencyRequestModel data;
    UserModel userModel;
    DatabaseHelper helper;
    String requestId;
    String type;
    FirebaseStorage mStorage;
    public AcceptDialog(Context ctx, UserModel userModel, String requestId, String type) {
        super.ctx = ctx;
        //this.data = data;
        this.userModel = userModel;
        this.requestId = requestId;
        this.helper = new DatabaseHelper(ctx);
        this.type = type;
        this.mStorage = FirebaseStorage.getInstance();
        setupDialog();
    }

    @Override
    void setupDialog() {
        mBuilder = new AlertDialog.Builder(ctx);
        LayoutInflater inflater = LayoutInflater.from(ctx);
        View view = inflater.inflate(R.layout.accept_dialog, null);
        LinearLayout detail = view.findViewById(R.id.detail);
        ImageView attention = view.findViewById(R.id.attention);
        final ImageView imageView = view.findViewById(R.id.imageView);
        TextView alert = view.findViewById(R.id.type);
        if (type.equals("emergency")){
            alert.setText("ต้องการความช่วยเหลือฉุกเฉิน");
        }else{
            alert.setText("ต้องการความช่วยเหลือ");
            attention.setVisibility(View.GONE);
        }
        TextView name = view.findViewById(R.id.name);
        TextView gender = view.findViewById(R.id.gender);
        TextView age = view.findViewById(R.id.age);
        LocalDate birthOfDate = new LocalDate(userModel.getDateOfBirth());
        LocalDate now = new LocalDate(Calendar.getInstance().getTime().getTime());
        name.setText(String.format("%s %s", userModel.getFirstName(), userModel.getLastName()));
        gender.setText(String.format("%s, ", "male".equals(userModel.getGender())? "ชาย" : "หญิง"));
        age.setText(String.format(Locale.forLanguageTag("th-TH"), "%d", Years.yearsBetween(birthOfDate, now).getYears()));
        mStorage.getReference().child("profile").child(userModel.getUserUid()+".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.with(ctx)
                        .load(uri)
                        .resize(500, 500)
                        .into(imageView);
            }
        });
        detail.setOnClickListener(this);
        mBuilder.setView(view);
    }
    public void show(){
        dialog = mBuilder.create();
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        ColorDrawable back = new ColorDrawable(Color.TRANSPARENT);
        InsetDrawable inset = new InsetDrawable(back, 60);
        dialog.getWindow().setBackgroundDrawable(inset);
        dialog.show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.detail:
                Intent intent = new Intent(ctx, AcceptActivity.class);
                intent.putExtra("type", type);
                intent.putExtra("user_information", userModel);
                intent.putExtra("request_id", requestId);
                ((Activity)ctx).startActivityForResult(intent, 4000);
                break;
        }
    }
    public void cancle(){
        dialog.cancel();
    }

    public Boolean isShowing(){
        return dialog.isShowing();
    }

}
