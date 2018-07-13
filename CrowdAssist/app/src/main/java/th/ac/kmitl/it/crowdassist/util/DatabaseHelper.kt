package th.ac.kmitl.it.crowdassist.util

import android.content.Context
import android.support.design.widget.Snackbar
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import th.ac.kmitl.it.crowdassist.R
import th.ac.kmitl.it.crowdassist.contract.SignInContract
import th.ac.kmitl.it.crowdassist.contract.SignUpContract
import th.ac.kmitl.it.crowdassist.modal.UserSignUpModal

class DatabaseHelper(val ctx : Context){
    val mAuth : FirebaseAuth = FirebaseAuth.getInstance()
    val mDatabase : FirebaseDatabase = FirebaseDatabase.getInstance()
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
                        Log.d("SignIp", "Cancelled by mDatabase Reference \"user_id\" ")
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
}
