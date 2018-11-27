package th.ac.kmitl.it.crowdassist.livedata

import android.arch.lifecycle.LiveData
import android.util.Log
import com.google.firebase.database.*
import th.ac.kmitl.it.crowdassist.model.DataSnapshotWithTypeModel

class FirebaseChildEventLiveData(): LiveData<DataSnapshotWithTypeModel>() {
    var LOG_TAG: String = "FirebaseChildEventLiveData"
    var query: Query? = null
    val listener: ChildEventListener? = handlerChildEvent()

    constructor(query: Query?): this(){
        this.query = query
    }
    constructor(ref: DatabaseReference?): this(){
        this.query = ref
    }

    override fun onActive() {
        Log.d(LOG_TAG, "onActive")
        query?.addChildEventListener(listener!!)
    }

    override fun onInactive() {
        Log.d(LOG_TAG, "onInactive")
        query?.removeEventListener(listener!!)
    }

    private inner class handlerChildEvent: ChildEventListener{
        override fun onCancelled(databaseError: DatabaseError) {
            Log.e(LOG_TAG, "Can't listen to query $query", databaseError.toException())
        }

        override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {
            value = DataSnapshotWithTypeModel(dataSnapshot, "onChildMoved", previousChildName)
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
            value = DataSnapshotWithTypeModel(dataSnapshot, "onChildChanged", previousChildName)
        }

        override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
            value = DataSnapshotWithTypeModel(dataSnapshot, "onChildAdded", previousChildName)
        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {
            value = DataSnapshotWithTypeModel(dataSnapshot, "onChildRemoved")
        }
    }
}