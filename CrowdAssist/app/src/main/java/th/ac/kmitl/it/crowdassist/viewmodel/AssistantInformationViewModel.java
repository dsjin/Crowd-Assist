package th.ac.kmitl.it.crowdassist.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import th.ac.kmitl.it.crowdassist.livedata.FirebaseChildEventLiveData;
import th.ac.kmitl.it.crowdassist.model.DataSnapshotWithTypeModel;

public class AssistantInformationViewModel extends AndroidViewModel {
    private final String SP_REQUEST = "request_information";
    private Context ctx;
    private String requestUid;
    private String type;
    private Query ASSISTANT_INFORMATION_REF;
    private FirebaseChildEventLiveData liveData;

    public AssistantInformationViewModel(Application application){
        super(application);
        ctx = application.getApplicationContext();
        SharedPreferences sp = ctx.getSharedPreferences(SP_REQUEST, Context.MODE_PRIVATE);
        requestUid = sp.getString("request_uid", null);
        type = sp.getString("type", null);
        ASSISTANT_INFORMATION_REF = FirebaseDatabase.getInstance().getReference().child(type+"_assistance").child(requestUid).child("user");
        liveData = new FirebaseChildEventLiveData(ASSISTANT_INFORMATION_REF);
    }

    @NonNull
    public LiveData<DataSnapshotWithTypeModel> getLiveData() {
        return liveData;
    }
}
