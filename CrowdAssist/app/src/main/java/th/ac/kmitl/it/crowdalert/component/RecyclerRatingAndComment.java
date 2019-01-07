package th.ac.kmitl.it.crowdalert.component;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import th.ac.kmitl.it.crowdalert.R;
import th.ac.kmitl.it.crowdalert.model.RatingAndCommentModel;

public class RecyclerRatingAndComment extends RecyclerView.Adapter<RecyclerRatingAndComment.ViewHolder>{
    private ArrayList<RatingAndCommentModel> list;
    private Context ctx;
    private FirebaseStorage mStorage;

    public RecyclerRatingAndComment(ArrayList<RatingAndCommentModel> list, Context ctx) {
        this.list = list;
        this.ctx = ctx;
        this.mStorage = FirebaseStorage.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.rating_and_comment_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        RatingAndCommentModel dataItem = list.get(position);
        mStorage.getReference().child("profile").child(dataItem.getUid()+".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.with(ctx).load(uri).resize(500,500).into(holder.profile);
            }
        });
        holder.name.setText(dataItem.getName());
        holder.ratingBar.setRating(dataItem.getRating().floatValue());
        holder.rating.setText(String.format("( %.1f )", dataItem.getRating()));
        holder.comment.setText(dataItem.getComment());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        public TextView name;
        public ImageView profile;
        public RatingBar ratingBar;
        public TextView rating;
        public TextView comment;
        public ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            profile = itemView.findViewById(R.id.imageView);
            comment = itemView.findViewById(R.id.comment);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            rating = itemView.findViewById(R.id.rating);
        }
    }
}
