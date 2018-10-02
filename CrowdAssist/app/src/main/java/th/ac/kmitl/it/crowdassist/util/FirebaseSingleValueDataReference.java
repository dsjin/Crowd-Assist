package th.ac.kmitl.it.crowdassist.util;

import android.arch.lifecycle.LiveData;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class FirebaseSingleValueDataReference extends LiveData<DataSnapshot> {
    private static final String LOG_TAG = "FirebaseLiveData";

    private final Query query;
    private final FirebaseSingleValueDataReference.MyValueEventListener listener = new FirebaseSingleValueDataReference.MyValueEventListener();

    public FirebaseSingleValueDataReference(Query query) {
        this.query = query;
    }

    public FirebaseSingleValueDataReference(DatabaseReference ref) {
        this.query = ref;
    }

    @Override
    protected void onActive() {
        Log.d(LOG_TAG, "onActive");
        query.addListenerForSingleValueEvent(listener);
    }

    @Override
    protected void onInactive() {
        Log.d(LOG_TAG, "onInactive");
        query.removeEventListener(listener);
    }

    private class MyValueEventListener implements ValueEventListener {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            setValue(dataSnapshot);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.e(LOG_TAG, "Can't listen to query " + query, databaseError.toException());
        }
    }
}
