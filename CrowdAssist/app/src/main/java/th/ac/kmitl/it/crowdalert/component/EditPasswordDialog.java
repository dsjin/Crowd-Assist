package th.ac.kmitl.it.crowdalert.component;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.InsetDrawable;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import th.ac.kmitl.it.crowdalert.R;

public class EditPasswordDialog extends Dialog implements View.OnClickListener{
    private EditText oldPassword;
    private EditText newPassword;
    private EditText reNewPassword;
    private LinearLayout confirm;
    private LinearLayout mainLayout;
    private LinearLayout progress;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    public EditPasswordDialog(Context ctx) {
        super.ctx = ctx;
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        setupDialog();
    }

    @Override
    void setupDialog() {
        mBuilder = new AlertDialog.Builder(ctx);
        LayoutInflater inflater = LayoutInflater.from(ctx);
        View view = inflater.inflate(R.layout.edit_password_dialog, null);
        oldPassword = view.findViewById(R.id.oldPassword);
        newPassword = view.findViewById(R.id.newPassword);
        reNewPassword = view.findViewById(R.id.repeatNewPassword);
        confirm = view.findViewById(R.id.confirm_button);
        confirm.setOnClickListener(this);
        progress = view.findViewById(R.id.progress_layout);
        mainLayout = view.findViewById(R.id.mainLayout);
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

    private void updatePassword(){
        mUser.updatePassword(newPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(ctx, "กำหนดรหัสผ่านใหม่เรียบร้อย", Toast.LENGTH_SHORT).show();
                    dialog.cancel();
                }else{
                    Toast.makeText(ctx, "มีข้อผิดพลาดกรุณาลองใหม่", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (!oldPassword.getText().toString().isEmpty() &&
                !newPassword.getText().toString().isEmpty() &&
                !reNewPassword.getText().toString().isEmpty()){
            if (!newPassword.getText().toString().equals(reNewPassword.getText().toString())){
                Toast.makeText(ctx, "รหัสผ่านใหม่ไม่ตรงกัน", Toast.LENGTH_SHORT).show();
                return;
            }
            progress.setVisibility(View.VISIBLE);
            mainLayout.setVisibility(View.GONE);
            dialog.setCancelable(false);
            AuthCredential credential = EmailAuthProvider.getCredential(mUser.getEmail(), oldPassword.getText().toString());
            mUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        updatePassword();
                    }else{
                        Toast.makeText(ctx, "รหัสผ่านเก่าไม่ถูกต้อง", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
