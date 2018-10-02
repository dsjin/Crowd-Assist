package th.ac.kmitl.it.crowdassist

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import th.ac.kmitl.it.crowdassist.component.RecycleProfileList
import th.ac.kmitl.it.crowdassist.model.ProfileListModel
import th.ac.kmitl.it.crowdassist.util.ConvertHelper



class ProfileListActivity : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null
    private var mUser: FirebaseUser? = null
    private var mFireStore: FirebaseFirestore? = null
    private var MODE: String? = null
    private var REF: Query? = null
    private var list: MutableList<ProfileListModel>? = null
    private var recyclerView: RecyclerView? = null
    private var adapter: RecycleProfileList? = null
    private var isLoading: Boolean? = null
    private var lastVisible: DocumentSnapshot? = null
    private var snackbar: Snackbar? = null
    private var manager: LinearLayoutManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_list)
        val toolbar = findViewById<Toolbar>(R.id.toolbar_top)
        setSupportActionBar(toolbar)
        val title = toolbar.findViewById<TextView>(R.id.toolbar_title)
        supportActionBar!!.title = ""
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener(View.OnClickListener { finish() })
        val intent = intent
        MODE = intent.getStringExtra("mode")
        mAuth = FirebaseAuth.getInstance()
        mUser = mAuth?.currentUser
        isLoading = false
        mFireStore = FirebaseFirestore.getInstance()
        list = mutableListOf()
        recyclerView = findViewById(R.id.list)
        adapter = RecycleProfileList(list, this, MODE, recyclerView?.context)
        manager = LinearLayoutManager(this)
        snackbar = Snackbar.make(recyclerView!!, "กำลังดึงข้อมูล", Snackbar.LENGTH_INDEFINITE)
        recyclerView?.layoutManager = manager
        recyclerView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    if (!recyclerView!!.canScrollVertically(RecyclerView.FOCUS_DOWN)) {
                        if (!isLoading!!) {
                            getDoc()
                        }
                    }
                }
            }
        })
        recyclerView?.adapter = adapter
        when (MODE) {
            "emergency" -> {
                REF = mFireStore!!.collection("user").document(mUser!!.uid).collection("emergency")
                        .orderBy("timestamp", Query.Direction.DESCENDING)
                        .limit(10)
                title.text = String.format("%s", "รายการคำร้องขอฉุกเฉิน")
            }
            "non-emergency" -> {
                REF = mFireStore!!.collection("user").document(mUser!!.uid).collection("general")
                        .orderBy("timestamp", Query.Direction.DESCENDING)
                        .limit(10)
                title.text = String.format("%s", "รายการคำร้องขอทั่วไป")
            }
            "assistance" -> {
                REF = mFireStore!!.collection("user").document(mUser!!.uid).collection("assistance")
                        .orderBy("timestamp", Query.Direction.DESCENDING)
                        .limit(10)
                title.text = String.format("%s", "รายการการช่วยเหลือ")
            }
        }
        getDoc()
    }

    private fun getDoc(){
        isLoading = true
        snackbar!!.show()
        REF!!.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                for (document in task.result) {
                    val uid = document.id
                    val model = ProfileListModel()
                    model.title = ConvertHelper.ConvertTimestampToDate(document.getLong("timestamp"))
                    model.uid = uid
                    model.timestamp = document.getLong("timestamp")
                    if (MODE == "assistance") {
                        model.type = document.getString("mode")
                    }
                    list?.add(model)
                    //runLayoutAnimation(recyclerView!!)
                    adapter?.notifyDataSetChanged()
                }

                if (task.result.size() > 0) {
                    lastVisible = task.result.documents[task.result.size() - 1]
                    when (MODE) {
                        "emergency" -> REF = mFireStore!!.collection("user").document(mUser!!.uid).collection("emergency")
                                .orderBy("timestamp", Query.Direction.DESCENDING)
                                .startAfter(lastVisible!!)
                                .limit(10)
                        "non-emergency" -> REF = mFireStore!!.collection("user").document(mUser!!.uid).collection("general")
                                .orderBy("timestamp", Query.Direction.DESCENDING)
                                .startAfter(lastVisible!!)
                                .limit(10)
                        "assistance" -> REF = mFireStore!!.collection("user").document(mUser!!.uid).collection("assistance")
                                .orderBy("timestamp", Query.Direction.DESCENDING)
                                .startAfter(lastVisible!!)
                                .limit(10)
                    }
                } else {
                    if (list!!.isEmpty()) {
                        val text = findViewById<TextView>(R.id.text)
                        text.visibility = View.VISIBLE
                    } else {
                        Toast.makeText(this, "แสดงรายการครบแล้ว", Toast.LENGTH_SHORT).show()
                    }
                }
                isLoading = false
                snackbar!!.dismiss()
            }
        }
    }

    private fun runLayoutAnimation(recyclerView: RecyclerView) {
        val context = recyclerView.context
        val controller = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_fall_down)

        recyclerView.layoutAnimation = controller
        recyclerView.adapter.notifyDataSetChanged()
        recyclerView.scheduleLayoutAnimation()
    }
}
