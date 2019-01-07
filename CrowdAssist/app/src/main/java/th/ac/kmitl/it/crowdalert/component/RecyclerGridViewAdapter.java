package th.ac.kmitl.it.crowdalert.component;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.util.List;

import th.ac.kmitl.it.crowdalert.R;
import th.ac.kmitl.it.crowdalert.model.AssistantModel;
import th.ac.kmitl.it.crowdalert.model.Request;

public class RecyclerGridViewAdapter extends RecyclerView.Adapter<RecyclerGridViewAdapter.ViewHolder>{
    private Context ctx;
    private List<AssistantModel> list;
    private FirebaseStorage mStorage;
    private Request request;

    public RecyclerGridViewAdapter(Context ctx, List<AssistantModel> list, Request request) {
        this.ctx = ctx;
        this.list = list;
        mStorage = FirebaseStorage.getInstance();
        this.request = request;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView = LayoutInflater.from(ctx).inflate(R.layout.user_recyclerview_layout, parent, false);
        return new ViewHolder(convertView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final AssistantModel assistant = list.get(position);
        mStorage.getReference().child("profile").child(assistant.getAssistantUid()+".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.with(ctx)
                        .load(uri)
                        .resize(500, 500)
                        .into(holder.profileImage);
                holder.layout.setTag(uri);
            }
        });
        holder.icon.setImageResource("user".equals(assistant.getRole())? R.drawable.user_icon: R.drawable.officer_icon);
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = (Uri) view.getTag();
                UserDialog userDialog = new UserDialog(ctx, assistant, request, uri);
                userDialog.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        ImageView profileImage;
        ImageView icon;
        CardView layout;
        public ViewHolder(View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profileImage);
            icon = itemView.findViewById(R.id.icon);
            layout = itemView.findViewById(R.id.userLayout);
        }
    }
}
