package th.ac.kmitl.it.crowdassist

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import th.ac.kmitl.it.crowdassist.util.DatabaseHelper
import android.content.SharedPreferences
import android.support.v7.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.chaos.view.PinView
import android.widget.LinearLayout
import android.support.v4.content.res.ResourcesCompat
import android.view.MenuItem
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener




class ConfirmActivity : AppCompatActivity() {

    private var pinView: PinView? = null
    private var mDatabase: FirebaseDatabase? = null
    private var mAuth: FirebaseAuth? = null
    private var spRequest: SharedPreferences? = null
    private val SPREQUEST = "request_information"
    private var requestUid: String? = null
    private var databaseHelper: DatabaseHelper? = null
    private var type: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirm)
        val toolbar = findViewById<Toolbar>(R.id.toolbar_top)
        setSupportActionBar(toolbar)
        title = " "
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener{
            finish()
        }
        mDatabase = FirebaseDatabase.getInstance()
        mAuth = FirebaseAuth.getInstance()
        databaseHelper = DatabaseHelper(this)
        spRequest = getSharedPreferences(SPREQUEST, Context.MODE_PRIVATE)
        requestUid = spRequest?.getString("request_uid", null)
        type = spRequest?.getString("type", null)
        pinView = findViewById(R.id.pinView)
        pinView?.setTextColor(
                ResourcesCompat.getColor(resources, R.color.colorAccent, theme))
        pinView?.setLineColor(
                ResourcesCompat.getColor(resources, R.color.colorTextPrimaryDark, theme))
        pinView?.itemCount = 4
        pinView?.setAnimationEnable(true)

        val confirm = findViewById<LinearLayout>(R.id.confirm_button)
        confirm.setOnClickListener{
            mDatabase?.getReference("users")?.child(mAuth?.currentUser?.uid!!)?.child("pin")?.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        if (dataSnapshot.getValue(String::class.java) == pinView?.text.toString()) {
                            databaseHelper?.closeRequest(requestUid, OnCompleteListener{
                                task -> kotlin.run {
                                    if (task.isSuccessful){
                                        finish()
                                    }else{
                                        Toast.makeText(this@ConfirmActivity, "มีข้อผิดพลาดเกิดขึ้น กรุณาลองใหม่", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }, type)
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(this@ConfirmActivity, "มีข้อผิดพลาดเกิดขึ้น กรุณาลองใหม่", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
