package th.ac.kmitl.it.crowdalert.util;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusViewModel extends AndroidViewModel {
    private final String SP_REQUEST = "request_information";
    private Context ctx;
    private String requestUid;
    private String type;
    private DatabaseReference STATUS_REF;

    private FirebaseDataReference liveData;

    public StatusViewModel(Application application) {
        super(application);
        ctx = application.getApplicationContext();
        SharedPreferences sp = ctx.getSharedPreferences(SP_REQUEST, Context.MODE_PRIVATE);
        requestUid = sp.getString("request_uid", null);
        type = sp.getString("type", null);
        STATUS_REF =
                FirebaseDatabase.getInstance().getReference().child(type).child(requestUid).child("status");
        liveData = new FirebaseDataReference(STATUS_REF);
    }

    @NonNull
    public LiveData<DataSnapshot> getDataSnapshotLiveData() {
        return liveData;
    }
}
