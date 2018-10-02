package th.ac.kmitl.it.crowdassist.util;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AssistantViewModel extends AndroidViewModel {
    private final String SP_REQUEST = "request_information";
    private Context ctx;
    private String requestUid;
    private String type;
    private DatabaseReference ASSISTANT_REF;

    private FirebaseSingleValueDataReference liveData;

    public AssistantViewModel(Application application) {
        super(application);
        ctx = application.getApplicationContext();
        SharedPreferences sp = ctx.getSharedPreferences(SP_REQUEST, Context.MODE_PRIVATE);
        requestUid = sp.getString("request_uid", null);
        type = sp.getString("type", null);
        ASSISTANT_REF =
                FirebaseDatabase.getInstance().getReference().child(type+"_assistance").child(requestUid).child("user");
        liveData = new FirebaseSingleValueDataReference(ASSISTANT_REF);
    }

    @NonNull
    public LiveData<DataSnapshot> getDataSnapshotLiveData() {
        return liveData;
    }
}
