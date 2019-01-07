package th.ac.kmitl.it.crowdalert.component;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.InsetDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;

import th.ac.kmitl.it.crowdalert.R;

public class TermsAndConditionsDialog extends Dialog implements View.OnClickListener{
    public TermsAndConditionsDialog(Context ctx) {
        super.ctx = ctx;
        setupDialog();
    }
    @Override
    void setupDialog() {
        mBuilder = new AlertDialog.Builder(ctx);
        LayoutInflater inflater = LayoutInflater.from(ctx);
        View view = inflater.inflate(R.layout.terms_and_conditions_dialog, null);
        LinearLayout decline = view.findViewById(R.id.decline);
        LinearLayout accept = view.findViewById(R.id.accept);
        decline.setOnClickListener(this);
        accept.setOnClickListener(this);
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
            case R.id.decline:
                dialog.cancel();
                ((Activity)ctx).finish();
                break;
            case R.id.accept:
                dialog.cancel();
                break;
        }
    }
}
