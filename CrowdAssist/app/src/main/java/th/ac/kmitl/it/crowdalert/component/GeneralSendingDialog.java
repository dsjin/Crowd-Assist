package th.ac.kmitl.it.crowdalert.component;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import th.ac.kmitl.it.crowdalert.CreateGeneralRequestActivity;
import th.ac.kmitl.it.crowdalert.R;
import th.ac.kmitl.it.crowdalert.model.GeneralBottomSheetModel;

public class GeneralSendingDialog extends BottomSheetDialogFragment {

    private ArrayList<GeneralBottomSheetModel> list;

    public GeneralSendingDialog() {

        list = new ArrayList<>();
        setUpList();

    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.general_sending_dialog, container, true);
        ListView listView = view.findViewById(R.id.listView);
        listView.setAdapter(new GeneralListAdapter(getActivity(), list));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getActivity(), CreateGeneralRequestActivity.class);
                intent.putExtra("type", list.get(i).getName());
                getActivity().startActivityForResult(intent,9015);
            }
        });
        return view;
    }

    private void setUpList(){
        list.add(new GeneralBottomSheetModel("อัคคีภัย", R.drawable.fire));
        list.add(new GeneralBottomSheetModel("อุบัติเหตุทางถนน", R.drawable.crash));
        list.add(new GeneralBottomSheetModel("อุบัติเหตุทางน้ำ", R.drawable.sea));
        list.add(new GeneralBottomSheetModel("อื่นๆ", R.drawable.other));
    }
}
