package th.ac.kmitl.it.crowdassist.model

import com.google.firebase.database.DataSnapshot

data class DataSnapshotWithTypeModel(var dataSnapshot: DataSnapshot?, var type: String?, var previousChildName: String? = null)