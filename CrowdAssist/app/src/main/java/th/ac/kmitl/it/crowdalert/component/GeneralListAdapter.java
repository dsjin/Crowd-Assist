package th.ac.kmitl.it.crowdalert.component;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import th.ac.kmitl.it.crowdalert.R;
import th.ac.kmitl.it.crowdalert.model.GeneralBottomSheetModel;

public class GeneralListAdapter extends BaseAdapter{
    private List<GeneralBottomSheetModel> list;
    private Context ctx;
    public GeneralListAdapter(Context ctx, List<GeneralBottomSheetModel> list) {
        this.list = list;
        this.ctx = ctx;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(ctx).inflate(R.layout.general_dialog_list_view, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.text.setText(list.get(i).getName());
        viewHolder.icon.setImageResource(list.get(i).getIcon());
        return convertView;
    }
    private class ViewHolder {
        public TextView text;
        public ImageView icon;

        public ViewHolder(View convertView) {
            text  = convertView.findViewById(R.id.text);
            icon = convertView.findViewById(R.id.icon);
        }
    }
}
