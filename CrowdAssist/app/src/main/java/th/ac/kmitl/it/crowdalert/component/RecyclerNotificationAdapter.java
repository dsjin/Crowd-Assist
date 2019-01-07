package th.ac.kmitl.it.crowdalert.component;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Locale;

import th.ac.kmitl.it.crowdalert.MainActivity;
import th.ac.kmitl.it.crowdalert.R;
import th.ac.kmitl.it.crowdalert.model.NotificationModel;
import th.ac.kmitl.it.crowdalert.model.UserModel;
import th.ac.kmitl.it.crowdalert.util.ConvertHelper;

public class RecyclerNotificationAdapter extends RecyclerView.Adapter<RecyclerNotificationAdapter.ViewHolder> implements View.OnClickListener{
    private FirebaseDatabase mDatabase;
    private ArrayList<NotificationModel> notification;
    private Context ctx;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    public RecyclerNotificationAdapter(ArrayList<NotificationModel> notification, Context ctx) {
        this.notification = notification;
        this.ctx = ctx;
        init();
    }

    private void init(){
        mDatabase = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.notification_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        NotificationModel dataItem = notification.get(position);
        holder.title.setText(String.format(Locale.forLanguageTag("th_TH"),"มีคำร้องฉุกเฉินจาก %s", dataItem.getUsername()));
        holder.date.setText(String.format("%s", ConvertHelper.ConvertTimestampToDate(dataItem.getTimestamp())));
        holder.distance.setText(String.format(Locale.forLanguageTag("th_TH"), "%.2f กม.", dataItem.getDistance()));
        holder.cardView.setOnClickListener(this);
        holder.cardView.setTag(dataItem);
    }

    @Override
    public int getItemCount() {
        return notification.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        public TextView title;
        public TextView date;
        public TextView distance;
        public CardView cardView;
        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            date = itemView.findViewById(R.id.date);
            distance = itemView.findViewById(R.id.distance);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }

    @Override
    public void onClick(View view) {
        final NotificationModel dataItem = (NotificationModel) view.getTag();
        /*
        mDatabase.getReference("emergency").child(dataItem.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    Request data = dataSnapshot.getValue(Request.class);
                    if ("open".equals(data.getStatus())){
                        //AcceptDialog dialog = new AcceptDialog(ctx, data, dataItem.getUsername(), dataItem.getUid());
                        //dialog.show();
                    }else{
                        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                        builder.setTitle("Alert!");
                        builder.setMessage("คำร้องขอนี้ได้สิ้นสุดลงแล้ว");
                        builder.setPositiveButton("OK", null);
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/
        String uid = dataItem.getUid();
        UserModel user = dataItem.getUser();
        String type = dataItem.getType();
        String requesterUid = dataItem.getRequesterUid();
        user.setUserUid(requesterUid);
        AcceptDialog dialog = new AcceptDialog(ctx, user, uid, type);
        ((MainActivity) ctx).setDialog(dialog);
        dialog.show();
    }
}
