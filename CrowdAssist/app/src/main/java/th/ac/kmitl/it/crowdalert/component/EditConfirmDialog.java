package th.ac.kmitl.it.crowdalert.component;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.InsetDrawable;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.chaos.view.PinView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import th.ac.kmitl.it.crowdalert.R;
import th.ac.kmitl.it.crowdalert.util.DatabaseHelper;

public class EditConfirmDialog extends Dialog implements View.OnClickListener, OnCompleteListener<Void> {
    private PinView pinView;
    private LinearLayout confirm;
    private DatabaseHelper helper;
    public EditConfirmDialog(Context ctx) {
        super.ctx = ctx;
        helper = new DatabaseHelper(ctx);
        setupDialog();
    }

    @Override
    void setupDialog() {
        mBuilder = new AlertDialog.Builder(ctx);
        LayoutInflater inflater = LayoutInflater.from(ctx);
        View view = inflater.inflate(R.layout.edit_confirm_code_dialog, null);
        pinView = view.findViewById(R.id.pinView);
        pinView.setTextColor(
                ResourcesCompat.getColor(ctx.getResources(), R.color.colorAccent, ctx.getTheme()));
        pinView.setLineColor(
                ResourcesCompat.getColor(ctx.getResources(), R.color.colorTextPrimaryDark, ctx.getTheme()));
        pinView.setItemCount(4);
        pinView.setAnimationEnable(true);
        confirm = view.findViewById(R.id.confirm_button);
        confirm.setOnClickListener(this);
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
        if (pinView.getText().toString().length() == 4){
            helper.updatePin(pinView.getText().toString(), this);
        }else {
            Toast.makeText(ctx, "กรุณาใส่รหัสให้ครบ", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onComplete(@NonNull Task<Void> task) {
        if (dialog.isShowing()){
            dialog.cancel();
        }
        Toast.makeText(ctx, "เสร็จแล้ว", Toast.LENGTH_SHORT).show();
    }
}
