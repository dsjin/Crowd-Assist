package th.ac.kmitl.it.crowdassist.presenter

import android.app.Activity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import th.ac.kmitl.it.crowdassist.R
import th.ac.kmitl.it.crowdassist.contract.RateContract
import th.ac.kmitl.it.crowdassist.model.AssistantModel
import th.ac.kmitl.it.crowdassist.model.UserModel
import th.ac.kmitl.it.crowdassist.util.DatabaseHelper
import java.util.*

class RatePresenter(val databaseHelper : DatabaseHelper, val view : RateContract.View, val userQueue : Queue<AssistantModel>?, val requestUid : String) : RateContract.Presenter{

    private var mDatabase: FirebaseDatabase? = null
    private var mStorage: FirebaseStorage? = null
    private var currentUser: AssistantModel? = null
    private var userModel: UserModel? = null

    init{
        mDatabase = FirebaseDatabase.getInstance()
        mStorage = FirebaseStorage.getInstance()
    }

    override fun prepareAssistantData() {
        view.showProgressBar()
        view.hideCardView()
        currentUser = userQueue?.remove()
        mDatabase?.reference?.child("users")?.child(currentUser?.assistantUid!!)?.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()){
                    userModel = dataSnapshot.getValue(UserModel::class.java)
                    view.setNameCardView(String.format("%s %s", userModel?.firstName, userModel?.lastName))
                    if ("requester" == currentUser?.role) {
                        view.setRoleTextView("ผู้ร้องขอ")
                    } else {
                        view.setRoleTextView(String.format("%s", if (currentUser?.role.equals("user")) "อาสาสมัคร" else "เข้าหน้าที่"))
                    }
                    mStorage?.reference?.child("profile")?.child(currentUser?.assistantUid + ".jpg")?.downloadUrl?.addOnSuccessListener{
                        uri -> kotlin.run {
                            view.setImageProfile(uri)
                            view.setDescription("")
                            view.setRating(3.0)
                            view.hideProgressBar()
                            view.showCardView()
                        }
                    }
                }
            }
        })
    }

    override fun onRateClicked() {
        view.showProgressBar()
        view.hideCardView()
        databaseHelper.rate(requestUid, currentUser?.assistantUid!!, view.getRating(), view.getDescription(), OnCompleteListener{
            task -> kotlin.run {
                if (task.isSuccessful){
                    if (userQueue!!.isEmpty()) {
                        view.finish(Activity.RESULT_OK)
                    }else{
                        view.setImageProfile(R.drawable.bg_gray)
                        currentUser = userQueue.remove()
                        prepareAssistantData()
                    }
                }
            }
        })
    }

    override fun onIgnoreClicked() {
        view.showProgressBar()
        view.hideCardView()
        view.setImageProfile(R.drawable.bg_gray)
        if (userQueue!!.isEmpty()) {
            view.finish(Activity.RESULT_OK)
        }else{
            currentUser = userQueue.remove()
            prepareAssistantData()
        }
    }
}
