package th.ac.kmitl.it.crowdassist

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.properties.Delegates

class ProxyActivity : AppCompatActivity() {
    private val SIGN_IN = 9001
    private val MAIN = 9002
    private var mAuth by Delegates.notNull<FirebaseAuth>()
    private var mUser : FirebaseUser? = null
    private val PROFILE_SP = "profile"
    private var profileSP: SharedPreferences? = null
    private var mDatabase by Delegates.notNull<FirebaseDatabase>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance()
    }

    override fun onStart() {
        super.onStart()
        setVisible(true)
        profileSP = getSharedPreferences(PROFILE_SP, Context.MODE_PRIVATE)
        mUser = mAuth.currentUser
        mUser?.let{
            start()
        } ?:run {
            val intent = Intent(this, SignInActivity::class.java)
            startActivityForResult(intent, SIGN_IN)
        }
        mDatabase = FirebaseDatabase.getInstance()
    }

    private fun start(){
        if (profileSP?.getString("role", null) == null || profileSP?.getString("name", null) == null || !profileSP!!.getBoolean("verify", false) || profileSP?.getString("username", null) == null) {
            mDatabase.getReference("users/" + mUser?.uid).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val editor = profileSP?.edit()
                        editor?.putString("role", dataSnapshot.child("role").getValue(String::class.java))
                        if (dataSnapshot.child("firstName").getValue(String::class.java) != null && dataSnapshot.child("lastName").getValue(String::class.java) != null) {
                            editor?.putString("name", String.format("%s %s", dataSnapshot.child("firstName").getValue(String::class.java), dataSnapshot.child("lastName").getValue(String::class.java)))
                        }
                        if (dataSnapshot.child("verify").getValue(Boolean::class.java)!!) {
                            editor?.putBoolean("verify", true)
                            //mDispatcher.mustSchedule(locationJob)
                        }
                        /*
                        if (dataSnapshot.child("username").getValue(String::class.java) != null) {
                            editor.putString("username", dataSnapshot.child("username").getValue(String::class.java))
                            FirebaseMessaging.getInstance().subscribeToTopic(dataSnapshot.child("username").getValue(String::class.java))
                        }
                        if (sp.getString("device", null) != null) {
                            FirebaseMessaging.getInstance().subscribeToTopic(dataSnapshot.child("username").getValue(String::class.java) + "." + sp.getString("device", null))
                            setUserProperties(dataSnapshot.child("username").getValue(String::class.java), sp.getString("device", null))
                        }*/
                        editor?.apply()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {

                }
            })
        }
        val intent = Intent(this, MainActivity::class.java)
        startActivityForResult(intent, MAIN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == 0) {
            finish()
        }
    }

    infix fun Any?.ifNull(block: () -> Unit) {
        if (this == null) block()
    }
}
