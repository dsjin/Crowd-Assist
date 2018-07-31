package th.ac.kmitl.it.crowdassist.fragment


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.CardView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

import th.ac.kmitl.it.crowdassist.R
import java.util.*
import kotlin.properties.Delegates


class ProfileFragment : Fragment() {
    private var emergency: CardView? = null
    private var nonEmergency: CardView? = null
    private var assistance: CardView? = null
    private var emergencyCount: TextView? = null
    private var nonEmergencyCount: TextView? = null
    private var assistanceCount: TextView? = null
    private var profileImageView: ImageView? = null
    private var name: TextView? = null
    private var ratingBar: RatingBar? = null
    private var rating: TextView? = null
    private var mAuth by Delegates.notNull<FirebaseAuth>()
    private var mDatabase by Delegates.notNull<FirebaseDatabase>()
    private var mUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance()
        mUser = mAuth.currentUser
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        emergency = view?.findViewById(R.id.emergency_count)
        nonEmergency = view?.findViewById(R.id.general_count)
        assistance = view?.findViewById(R.id.assistance_count)
        emergency?.setOnClickListener(View.OnClickListener {

        })
        nonEmergency?.setOnClickListener(View.OnClickListener {

        })
        assistance?.setOnClickListener(View.OnClickListener {

        })
        emergencyCount = view?.findViewById(R.id.count1)
        nonEmergencyCount = view?.findViewById(R.id.count2)
        assistanceCount = view?.findViewById(R.id.count3)
        profileImageView = view?.findViewById(R.id.imageView)
        ratingBar = view?.findViewById(R.id.ratingBar)
        name = view?.findViewById(R.id.name)
        rating = view?.findViewById(R.id.rating)
        Picasso.with(activity).load(mUser?.photoUrl)
                .resize(500, 500)
                .into(profileImageView)
        mDatabase.getReference().child("users").child(mUser!!.uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                name?.text = (String.format("%s %s", dataSnapshot.child("firstName").getValue(String::class.java), dataSnapshot.child("lastName").getValue(String::class.java)))
                val rate: Double?
                if (dataSnapshot.child("rating").child("scores").getValue(Double::class.java) != null && dataSnapshot.child("rating").child("counts").getValue(Double::class.java) != null) {
                    if (dataSnapshot.child("rating").child("counts").getValue(Double::class.java) == 0.0) {
                        rate = 0.0
                    } else {
                        rate = dataSnapshot.child("rating").child("scores").getValue(Double::class.java)!! / dataSnapshot.child("rating").child("counts").getValue(Double::class.java)!!
                    }
                } else {
                    rate = 0.0
                }
                rating?.text = (String.format("( %.1f )", rate))
                ratingBar?.rating = (rate.toFloat())
                var emergency: Int? = 0
                var general: Int? = 0
                var assistance: Int? = 0
                if (dataSnapshot.child("numOfDoc").child("emergency").getValue(Int::class.java) != null) {
                    emergency = dataSnapshot.child("numOfDoc").child("emergency").getValue(Int::class.java)
                }
                if (dataSnapshot.child("numOfDoc").child("general").getValue(Int::class.java) != null) {
                    general = dataSnapshot.child("numOfDoc").child("general").getValue(Int::class.java)
                }
                if (dataSnapshot.child("numOfDoc").child("assistance").getValue(Int::class.java) != null) {
                    assistance = dataSnapshot.child("numOfDoc").child("assistance").getValue(Int::class.java)
                }
                emergencyCount?.text = (String.format(Locale.forLanguageTag("Th-th"), "%d", emergency))
                nonEmergencyCount?.text = (String.format(Locale.forLanguageTag("Th-th"), "%d", general))
                assistanceCount?.text = (String.format(Locale.forLanguageTag("Th-th"), "%d", assistance))
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
        return view
    }

}
