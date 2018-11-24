package th.ac.kmitl.it.crowdassist.livedata

import android.util.Log
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.Query

class FirebaseValueEventLiveData: FirebaseValueEventListenerWrapper {
    init {
        super.LOG_TAG = "FirebaseValueEventLiveData"
    }
    constructor(query: Query?){
        super.query = query
    }
    constructor(ref: DatabaseReference?){
        super.query = ref
    }

    override fun onActive() {
        Log.d(LOG_TAG, "onActive")
        query?.addListenerForSingleValueEvent(listener!!)
    }

    override fun onInactive() {
        Log.d(LOG_TAG, "onInactive")
        query?.removeEventListener(listener!!)
    }
}