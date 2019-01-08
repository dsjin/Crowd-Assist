package th.ac.kmitl.it.crowdassist

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.ImageView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.FirebaseAuth
import android.widget.TextView
import com.google.firebase.storage.FirebaseStorage
import th.ac.kmitl.it.crowdassist.model.Request
import android.widget.LinearLayout
import com.google.android.gms.maps.MapFragment
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import th.ac.kmitl.it.crowdassist.util.ConvertHelper
import java.util.*
import android.widget.Toast
import android.content.Intent
import android.net.Uri
import android.util.Log
import org.joda.time.Years
import android.widget.RatingBar
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.joda.time.LocalDate


class RequestInformationActivity : AppCompatActivity(), OnMapReadyCallback {

    private var mAuth: FirebaseAuth? = null
    private var mUser: FirebaseUser? = null
    private var mDatabase: FirebaseDatabase? = null
    private var mStorage: FirebaseStorage? = null
    private var gMap: GoogleMap? = null
    private var requestInformation: DatabaseReference? = null
    private var date: TextView? = null
    private var status: TextView? = null
    private var officerAccepted: TextView? = null
    private var volunteerAccepted: TextView? = null
    private var imageView: ImageView? = null
    private var information: Request? = null
    private var number: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_information)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = ("")
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        mAuth = FirebaseAuth.getInstance()
        mUser = mAuth?.currentUser
        mDatabase = FirebaseDatabase.getInstance()

        bindView()
    }

    private fun bindView() {
        val mapFragment = fragmentManager
                .findFragmentById(R.id.map) as MapFragment
        mapFragment.getMapAsync(this)
        val emergencyTitle = findViewById<TextView>(R.id.emergency_title)
        val informationDetail = findViewById<LinearLayout>(R.id.information_layout)
        mStorage = FirebaseStorage.getInstance()
        mDatabase = FirebaseDatabase.getInstance()
        requestInformation = mDatabase?.reference
        date = findViewById(R.id.date)
        status = findViewById(R.id.status)
        imageView = findViewById(R.id.imageView)
        information = intent.getSerializableExtra("data") as Request
        if ("emergency" == intent.getStringExtra("type")) {
            emergencyTitle.visibility = View.VISIBLE
            informationDetail.visibility = View.GONE
            setUser(information!!.requesterUid)
            date?.text = String.format("เวลาส่งคำร้องขอ : %s", ConvertHelper.ConvertTimestampToDate(information!!.timestamp))
            status?.text = String.format("สถานะคำร้องขอ : %s", ConvertHelper.ConvertStatusToThai(information!!.status))
        } else {
            informationDetail.visibility = View.VISIBLE
            emergencyTitle.visibility = View.GONE
            val typeTitle = findViewById<TextView>(R.id.type_non_emergency)
            setUser(information!!.requesterUid)
            typeTitle.text = String.format("หมวดหมู่ : %s", ConvertHelper.ConvertTypeToThai(information!!.type))
            val statusOfRequester = findViewById<TextView>(R.id.statusOfRequester)
            statusOfRequester.text = String.format("สถานะผู้ร้องขอ : %s", if (information!!.requesterType == "victim") "ผู้ประสบเหตุ" else "ผู้เห็นเหตุการณ์")
            val description = findViewById<TextView>(R.id.description)
            description.text = information!!.description
            val cover = findViewById<ImageView>(R.id.coverImage)
            date?.text = String.format("เวลาส่งคำร้องขอ : %s", ConvertHelper.ConvertTimestampToDate(information!!.timestamp))
            status?.text = String.format("สถานะคำร้องขอ : %s", ConvertHelper.ConvertStatusToThai(information!!.status))
            mStorage?.reference?.child("non_emergency")?.child(intent.getStringExtra("request_id") + ".jpg")?.downloadUrl?.addOnSuccessListener { uri ->
                Picasso.with(this@RequestInformationActivity)
                        .load(uri)
                        .into(cover)
            }
        }
        officerAccepted = findViewById(R.id.officer_accept)
        volunteerAccepted = findViewById(R.id.user_accept)

        mDatabase?.reference?.child(intent.getStringExtra("type")+"_assistance")?.child(intent.getStringExtra("uid"))?.child("officerCount")?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Log.e("mDatabase", error.message)
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.exists()){
                    officerAccepted?.text = String.format("%d",dataSnapshot.getValue(Integer::class.java))
                }
            }
        })

        mDatabase?.reference?.child(intent.getStringExtra("type")+"_assistance")?.child(intent.getStringExtra("uid"))?.child("userCount")?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Log.e("mDatabase", error.message)
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.exists()){
                    volunteerAccepted?.text = String.format("%d",dataSnapshot.getValue(Integer::class.java))
                }
            }
        })

    }

    override fun onMapReady(googleMap: GoogleMap?) {
        gMap = googleMap
        gMap?.uiSettings?.setAllGesturesEnabled(false)
        gMap?.addMarker(MarkerOptions()
                .position(LatLng(information!!.lat, information!!.lng)))
        gMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(information!!.lat, information!!.lng), 15.toFloat()))
    }

    private fun setUser(uid : String) {
        mDatabase?.reference?.child("users")?.child(uid)?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Log.e("mDatabase", error.message)
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val name = findViewById<TextView>(R.id.name)
                val gender = findViewById<TextView>(R.id.gender)
                val age = findViewById<TextView>(R.id.age)
                val ratingBar = findViewById<RatingBar>(R.id.ratingBar)
                val rating = findViewById<TextView>(R.id.rating)
                val tel = findViewById<LinearLayout>(R.id.tel)
                val birthOfDate = LocalDate(dataSnapshot.child("dateOfBirth").getValue(Long::class.java))
                val now = LocalDate(Calendar.getInstance().time.time)
                name.text = String.format("%s %s", dataSnapshot.child("firstName").getValue(String::class.java), dataSnapshot.child("lastName").getValue(String::class.java))
                gender.text = String.format("%s, ", if ("male" == dataSnapshot.child("gender").getValue(String::class.java)) "ชาย" else "หญิง")
                age.text = String.format(Locale.forLanguageTag("th-TH"), "%d", Years.yearsBetween(birthOfDate, now).years)
                mStorage?.reference?.child("profile")?.child("$uid.jpg")?.downloadUrl?.addOnSuccessListener { uri ->
                    Picasso.with(this@RequestInformationActivity)
                            .load(uri)
                            .resize(500, 500)
                            .into(imageView)
                }
                val rate: Double?
                if (dataSnapshot.child("rating").child("scores").getValue(Double::class.java) != null && dataSnapshot.child("rating").child("counts").getValue(Double::class.java) != null) {
                    if (dataSnapshot.child("rating").child("counts").getValue(Double::class.java) == 0.0) {
                        rate = 0.0
                    } else {
                        rate = dataSnapshot.child("rating").child("scores").getValue(Float::class.java)!!.toDouble() / dataSnapshot.child("rating").child("counts").getValue(Float::class.java)!!.toDouble()
                    }
                } else {
                    rate = 0.0
                }
                rating.text = String.format("( %.1f )", rate)
                ratingBar.rating = rate.toFloat()
                number = dataSnapshot.child("tel").getValue(String::class.java)
                tel.setOnClickListener {
                    if (number != null) {
                        val makeCall = Intent(Intent.ACTION_CALL, Uri.parse("tel:$number"))
                        try {
                            startActivity(makeCall)
                        } catch (e: SecurityException) {
                            Log.e("Permission Error:", e.message)
                        }

                    } else {
                        Toast.makeText(this@RequestInformationActivity, "ผู้ร้องขอไม่ได้กำหนดเบอร์โทรศัพท์", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
