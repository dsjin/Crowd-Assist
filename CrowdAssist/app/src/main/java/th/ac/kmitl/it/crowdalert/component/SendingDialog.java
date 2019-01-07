package th.ac.kmitl.it.crowdalert.component;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.InsetDrawable;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import th.ac.kmitl.it.crowdalert.MainActivity;
import th.ac.kmitl.it.crowdalert.R;
import th.ac.kmitl.it.crowdalert.model.Request;
import th.ac.kmitl.it.crowdalert.util.DatabaseHelper;

public class SendingDialog extends Dialog{
    private CountDownTimer timer;
    private TextView countdown;
    private DatabaseHelper helper;
    private Request data;

    public SendingDialog(Context ctx, Request data) {
        super.ctx = ctx;
        this.data = data;
        helper = new DatabaseHelper(this.ctx);
        setupDialog();
    }

    @Override
    void setupDialog(){
        mBuilder = new AlertDialog.Builder(ctx);
        LayoutInflater inflater = LayoutInflater.from(ctx);
        View view = inflater.inflate(R.layout.sending_dialog, null);
        LinearLayout cancel = view.findViewById(R.id.cancel_action);
        countdown = view.findViewById(R.id.countdown);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timer.cancel();
                dialog.cancel();
            }
        });
        mBuilder.setView(view);
    }

    private void timeCounting(int millisec){
        timer = new CountDownTimer(millisec, 1000){
            @Override
            public void onTick(long l) {
                Log.d("Tick", "onTick: "+l);
                countdown.setText(String.format("ใน %d วินาที", l/1000));
            }

            @Override
            public void onFinish() {
                helper.createRequest(data);
                ((MainActivity)ctx).set("Helped");
                dialog.cancel();
            }
        }.start();
    }

    public void show(int millisec){
        dialog = mBuilder.create();
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        ColorDrawable back = new ColorDrawable(Color.TRANSPARENT);
        InsetDrawable inset = new InsetDrawable(back, 60);
        dialog.getWindow().setBackgroundDrawable(inset);
        dialog.show();
        timeCounting(millisec);
    }
}
