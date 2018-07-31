package th.ac.kmitl.it.crowdassist.util

import android.content.Context
import android.net.Uri
import android.support.design.widget.Snackbar
import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import th.ac.kmitl.it.crowdassist.R
import th.ac.kmitl.it.crowdassist.contract.CreateGeneralRequestContract
import th.ac.kmitl.it.crowdassist.contract.SignInContract
import th.ac.kmitl.it.crowdassist.contract.SignUpContract
import th.ac.kmitl.it.crowdassist.modal.Request
import th.ac.kmitl.it.crowdassist.modal.UserSignUpModal
import java.util.*

class DatabaseHelper(val ctx : Context){
    private val mAuth : FirebaseAuth = FirebaseAuth.getInstance()
    private val mDatabase : FirebaseDatabase = FirebaseDatabase.getInstance()
    private val mUser = mAuth.currentUser
    private val mFireStore : FirebaseFirestore = FirebaseFirestore.getInstance()
    private val SP_REQUEST = "request_information"
    private val SP_PROFILE = "profile"
    private val SP_LOCATION = "location_information"

    fun signUp(data : UserSignUpModal, view : SignUpContract.View){
        if (data.password != data.rePassword){
            view.showSnackBar(ctx.getString(R.string.password_not_match), Snackbar.LENGTH_SHORT)
            return
        }
        mDatabase.getReference("user_id").child(data.userName).addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                        Log.d("SignUp","Cancelled by mDatabase Reference \"user_id\" ")
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        if(!p0.exists()){
                            mAuth.createUserWithEmailAndPassword(data.email, data.password).addOnCompleteListener {
                                authResult -> run{
                                    if (authResult.isSuccessful){
                                        val user = mAuth.currentUser
                                        val userHashMap = mutableMapOf<String, Any>()
                                        userHashMap.put("username", data.userName)
                                        userHashMap.put("firstName", data.firstName)
                                        userHashMap.put("lastName", data.lastName)
                                        userHashMap.put("email", data.email)
                                        userHashMap.put("idCard", data.idCard)
                                        userHashMap.put("role", data.role)
                                        userHashMap.put("fistTime", true)
                                        mDatabase.getReference("users").child(user!!.uid).setValue(userHashMap)
                                        mDatabase.getReference("user_id").child(data.userName).setValue(data.email)
                                        view.finish()
                                    }else{
                                        Log.d("SignUp","Error for updated in \"create user with email and password\" ")
                                    }
                                }
                            }
                        }
                    }
                }
        )
    }
    fun signIn(username : String , password : String, view : SignInContract.View){
        mDatabase.getReference("user_id").child(username).addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                        Log.d("SignUp", "Cancelled by mDatabase Reference \"user_id\" ")
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        if (p0.exists()) {
                            val email = p0.getValue(String::class.java)
                            mAuth.signInWithEmailAndPassword(email!!, password).addOnCompleteListener { authResult ->
                                run {
                                    if (authResult.isSuccessful){
                                        view.finishActivity()
                                    }else{
                                        view.hideProgressBar()
                                        view.showSnackBar("ชื่อผู้ใช้หรือรหัสผ่านไม่ถูกต้อง", Snackbar.LENGTH_SHORT)
                                    }
                                }
                            }
                        }else{
                            view.hideProgressBar()
                            view.showSnackBar("ชื่อผู้ใช้หรือรหัสผ่านไม่ถูกต้อง", Snackbar.LENGTH_SHORT)
                        }
                    }
                }
        )
    }
    fun createGeneralRequest(data : Request, uri : Uri, view : CreateGeneralRequestContract.View){
        val ref = mDatabase.reference.child("non_emergency").push()
        val runner = mDatabase.reference.child("emergency_requester_point").child(ref.key!!)
        val fireStoreRef = mFireStore.collection("user").document(mUser?.uid!!).collection("general").document(ref.key!!)
        data.requesterUid = mUser.uid

        val fireStore = HashMap<String, Any>()
        fireStore.put("timestamp", Calendar.getInstance().timeInMillis)
        fireStoreRef.set(fireStore)

        val runnerData = mutableMapOf<String, Any>()
        runnerData.put("lat", data.lat)
        runnerData.put("lng", data.lng)
        runner.setValue(runnerData)

        ref.setValue(data).addOnCompleteListener(OnCompleteListener {
            task -> run {
                if (task.isSuccessful) {
                    view.hideCircularProgressBar()
                    view.showProgressBar()
                    val sp = ctx.getSharedPreferences(SP_REQUEST, Context.MODE_PRIVATE)

                    val storage = FirebaseStorage.getInstance()
                    val ref = storage.reference.child("non_emergency").child(sp.getString("request_uid", "null")!! + ".jpg")
                    ref.putFile(uri).addOnSuccessListener {
                        view.finishActivity()
                    }.addOnFailureListener { e -> Log.e("EditFirstTime | Upload", e.message) }.addOnProgressListener { taskSnapshot ->
                        val progress = 100 * taskSnapshot.bytesTransferred / taskSnapshot
                                .totalByteCount
                        view.setPrograssBarValue(progress.toInt())
                    }
                } else {
                    view.showSnackBar("มีข้อผิดพลาดกรุณาลองใหม่อีกครั้ง", Snackbar.LENGTH_SHORT)
                }
            }
        })
        val sp = ctx.getSharedPreferences(SP_REQUEST, Context.MODE_PRIVATE)
        val editor = sp.edit()
        editor.putString("request_uid", ref.key)
        editor.putString("requester_uid", mUser.uid)
        editor.putInt("time", data.time)
        editor.putString("type", "non_emergency")
        editor.putInt("assistance", 0)
        editor.putString("mode", "Helped")
        editor.apply()
    }
}
