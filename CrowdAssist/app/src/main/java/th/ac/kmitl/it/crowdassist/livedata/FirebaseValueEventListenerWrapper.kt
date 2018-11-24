package th.ac.kmitl.it.crowdassist.livedata

import android.arch.lifecycle.LiveData
import android.util.Log
import com.google.firebase.database.*

open class FirebaseValueEventListenerWrapper() : LiveData<DataSnapshot>() {
    var LOG_TAG: String = "FirebaseValueEventListenerWraper"
    var query: Query? = null
    val listener:ValueEventListener? = handlerEventValue()

    private inner class handlerEventValue : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            value = dataSnapshot
        }

        override fun onCancelled(databaseError: DatabaseError) {
            Log.e(LOG_TAG, "Can't listen to query $query", databaseError.toException())
        }
    }
}