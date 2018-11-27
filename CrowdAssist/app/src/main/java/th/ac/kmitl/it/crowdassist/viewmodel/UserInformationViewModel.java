package th.ac.kmitl.it.crowdassist.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.Marker;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import th.ac.kmitl.it.crowdassist.util.FirebaseSingleValueDataReference;

public class UserInformationViewModel extends AndroidViewModel {
    private DatabaseReference USER_REF;
    private FirebaseSingleValueDataReference liveData;
    public UserInformationViewModel(Application application, String uid){
        super(application);
        USER_REF = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
        liveData = new FirebaseSingleValueDataReference(USER_REF);
    }

    @NonNull
    public LiveData<DataSnapshot> getDataSnapshotLiveData() {
        return liveData;
    }
}
