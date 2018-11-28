package th.ac.kmitl.it.crowdassist.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import th.ac.kmitl.it.crowdassist.util.FirebaseSingleValueDataReference;

public class RequestInformationViewModel extends AndroidViewModel {
    private final String SP_REQUEST = "request_information";
    private Context ctx;
    private String requestUid;
    private String type;
    private DatabaseReference REQUEST_REF;
    private FirebaseSingleValueDataReference liveData;
    public RequestInformationViewModel(Application application){
        super(application);
        ctx = application.getApplicationContext();
        SharedPreferences sp = ctx.getSharedPreferences(SP_REQUEST, Context.MODE_PRIVATE);
        requestUid = sp.getString("request_uid", null);
        type = sp.getString("type", null);
        REQUEST_REF = FirebaseDatabase.getInstance().getReference().child(type).child(requestUid);
        liveData = new FirebaseSingleValueDataReference(REQUEST_REF);
    }

    @NonNull
    public LiveData<DataSnapshot> getDataSnapshotLiveData() {
        return liveData;
    }
}
