package th.ac.kmitl.it.crowdalert.component;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import th.ac.kmitl.it.crowdalert.ManageRequestActivity;
import th.ac.kmitl.it.crowdalert.R;
import th.ac.kmitl.it.crowdalert.model.Item;

public class ExpandableListView extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    public static final int HEADER = 0;
    public static final int CHILD = 1;

    List<Item> data;
    Context ctx;

    public ExpandableListView(List<Item> data, Context ctx) {
        this.data = data;
        this.ctx = ctx;
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        switch(viewType){
            case HEADER:
                view = inflater.inflate(R.layout.header, parent, false);
                ViewHolder header = new ViewHolder(view);
                return header;
            case CHILD:
                view = inflater.inflate(R.layout.child, parent, false);
                ChildViewHolder child = new ChildViewHolder(view);
                return child;
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final Item item = data.get(position);
        float dp = ctx.getResources().getDisplayMetrics().density;
        final int margin = (int) (5 * dp);
        switch (item.type) {
            case HEADER:
                final ViewHolder itemController = (ViewHolder) holder;
                itemController.refferalItem = item;
                itemController.header_title.setText(item.text);
                if (item.invisibleChildren == null) {
                    itemController.btn_expand_toggle.setImageResource(R.drawable.up);
                } else {
                    itemController.btn_expand_toggle.setImageResource(R.drawable.down);
                }
                itemController.btn_expand_toggle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (item.invisibleChildren == null) {
                            item.invisibleChildren = new ArrayList<Item>();
                            int count = 0;
                            int pos = data.indexOf(itemController.refferalItem);
                            while (data.size() > pos + 1 && data.get(pos + 1).type == CHILD) {
                                item.invisibleChildren.add(data.remove(pos + 1));
                                count++;
                            }
                            notifyItemRangeRemoved(pos + 1, count);
                            itemController.btn_expand_toggle.setImageResource(R.drawable.down);
                        } else {
                            int pos = data.indexOf(itemController.refferalItem);
                            int index = pos + 1;
                            for (Item i : item.invisibleChildren) {
                                data.add(index, i);
                                index++;
                            }
                            notifyItemRangeInserted(pos + 1, index - pos - 1);
                            item.invisibleChildren = null;
                            itemController.btn_expand_toggle.setImageResource(R.drawable.up);
                        }
                    }
                });
                break;
            case CHILD:
                final ChildViewHolder childItemController = (ChildViewHolder) holder;
                childItemController.lable.setText(data.get(position).text);
                childItemController.layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(ctx, ManageRequestActivity.class);
                        intent.putExtra("uid", data.get(position).uid);
                        ctx.startActivity(intent);
                    }
                });
                break;
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        public TextView header_title;
        public ImageView btn_expand_toggle;
        public Item refferalItem;
        public ViewHolder(View itemView) {
            super(itemView);
            header_title = itemView.findViewById(R.id.header_title);
            btn_expand_toggle = itemView.findViewById(R.id.btn_expand_toggle);
        }
    }
    class ChildViewHolder extends RecyclerView.ViewHolder{
        public TextView lable;
        public LinearLayout layout;
        public ChildViewHolder(View itemView){
            super(itemView);
            lable = itemView.findViewById(R.id.lable);
            layout = itemView.findViewById(R.id.layout);
        }
    }
    @Override
    public int getItemCount() {
        return data.size();
    }
    @Override
    public int getItemViewType(int position) {
        return data.get(position).type;
    }
}
