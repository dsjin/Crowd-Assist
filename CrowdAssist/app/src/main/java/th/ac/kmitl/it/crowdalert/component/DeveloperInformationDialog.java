package th.ac.kmitl.it.crowdalert.component;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.InsetDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.squareup.picasso.Picasso;

import th.ac.kmitl.it.crowdalert.R;

public class DeveloperInformationDialog extends Dialog implements View.OnClickListener {
    public DeveloperInformationDialog(Context ctx) {
        super.ctx = ctx;
        setupDialog();
    }

    @Override
    void setupDialog() {
        mBuilder = new AlertDialog.Builder(ctx);
        LayoutInflater inflater = LayoutInflater.from(ctx);
        View view = inflater.inflate(R.layout.developer_information_dialog, null);
        LinearLayout close = view.findViewById(R.id.close);
        close.setOnClickListener(this);
        mBuilder.setView(view);
        ImageView army = view.findViewById(R.id.army);
        ImageView chat = view.findViewById(R.id.chat);
        Picasso.with(ctx).load(R.drawable.army).into(army);
        Picasso.with(ctx).load(R.drawable.chat).into(chat);
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
            case R.id.close:
                dialog.cancel();
                break;
        }
    }
}
