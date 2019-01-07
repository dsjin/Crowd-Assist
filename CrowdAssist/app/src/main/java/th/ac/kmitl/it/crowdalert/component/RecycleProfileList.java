package th.ac.kmitl.it.crowdalert.component;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import th.ac.kmitl.it.crowdalert.ProfileRequestInfoActivity;
import th.ac.kmitl.it.crowdalert.R;
import th.ac.kmitl.it.crowdalert.model.ProfileListModel;

public class RecycleProfileList extends RecyclerView.Adapter<RecycleProfileList.ViewHolder> implements View.OnClickListener{
    private ArrayList<ProfileListModel> list;
    private Context ctx;
    private String mode;

    public RecycleProfileList(ArrayList<ProfileListModel> list, Context ctx, String mode) {
        this.list = list;
        this.ctx = ctx;
        this.mode = mode;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.simple_listview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProfileListModel dataItem = list.get(position);
        holder.title.setText(dataItem.getTitle());
        holder.cardView.setOnClickListener(this);
        holder.cardView.setTag(dataItem);
    }

    @Override
    public void onClick(View view) {
        ProfileListModel dataItem = (ProfileListModel) view.getTag();
        Intent intent = new Intent(ctx, ProfileRequestInfoActivity.class);
        intent.putExtra("uid", dataItem.getUid());

        switch (mode){
            case "assistance":
                intent.putExtra("type", dataItem.getType());
                break;
            case "emergency":
                intent.putExtra("type", "emergency");
                break;
            case "general":
                intent.putExtra("type", "non_emergency");
                break;
        }
        ctx.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        public TextView title;
        public CardView cardView;
        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }
}
