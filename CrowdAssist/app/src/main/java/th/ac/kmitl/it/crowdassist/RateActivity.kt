package th.ac.kmitl.it.crowdassist

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.squareup.picasso.Picasso
import th.ac.kmitl.it.crowdassist.contract.RateContract
import th.ac.kmitl.it.crowdassist.model.AssistantModel
import th.ac.kmitl.it.crowdassist.presenter.RatePresenter
import th.ac.kmitl.it.crowdassist.util.AssistantViewModel
import th.ac.kmitl.it.crowdassist.util.DatabaseHelper
import java.util.*

class RateActivity : AppCompatActivity(), RateContract.View{
    private val SP_REQUEST = "request_information"
    private var requestSP: SharedPreferences? = null
    private var requestUid: String? = null
    private var requestType: String? = null
    private var requesterUid: String? = null
    private var userQueue: Queue<AssistantModel>? = null
    private var mUser: FirebaseUser? = null
    private var mAuth: FirebaseAuth? = null
    private var progressLayout: LinearLayout? = null
    private var rateCardLayout: LinearLayout? = null
    private var profileImage: ImageView? = null
    private var ratingBar: RatingBar? = null
    private var nameTextView: TextView? = null
    private var roleTextView: TextView? = null
    private var descriptionEditText: EditText? = null
    private var rateButton: LinearLayout? = null
    private var ignoreButton: LinearLayout? = null
    private var presenter: RatePresenter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rate)
        requestSP = getSharedPreferences(SP_REQUEST, Context.MODE_PRIVATE)
        mAuth = FirebaseAuth.getInstance()
        mUser = mAuth?.currentUser

        requestUid = requestSP?.getString("request_uid", null)
        requestType = requestSP?.getString("type", null)
        requesterUid = requestSP?.getString("requester_uid", null)

        bindView()
        userQueue = LinkedList()
        showProgressBar()
        hideCardView()
        if (mUser?.uid == requesterUid) {
            val viewModel = ViewModelProviders.of(this).get(AssistantViewModel::class.java)
            val liveData = viewModel.dataSnapshotLiveData
            liveData.observe(this, Observer<DataSnapshot>{
                data: DataSnapshot? ->  kotlin.run {
                    data?.let {
                        if (!data.exists()){
                            finish(Activity.RESULT_OK)
                        }
                        if (data.exists()) {
                            for (item in data.children) {
                                val model = AssistantModel().also {
                                    it.assistantUid = item.key!!
                                    it.role = item.child("role").getValue(String::class.java)!!
                                }
                                push(model)
                            }
                        }
                        if (userQueue!!.isEmpty()) {
                            finish(Activity.RESULT_OK)
                        } else {
                            presenter = RatePresenter(DatabaseHelper(this), this, userQueue, requestUid!!)
                            presenter!!.prepareAssistantData()
                        }
                    } ?:run {
                        finish(Activity.RESULT_OK)
                    }
                }
            })
        }else{
            val model = AssistantModel().also {
                it.assistantUid = requesterUid!!
                it.role = "requester"
            }
            push(model)
            presenter!!.prepareAssistantData()
        }
    }

    private fun bindView() {
        progressLayout = findViewById(R.id.progress_layout)
        rateCardLayout = findViewById(R.id.rateCardLayout)
        profileImage = findViewById(R.id.imageView)
        nameTextView = findViewById(R.id.name)
        roleTextView = findViewById(R.id.role)
        ratingBar = findViewById(R.id.ratingBar)
        descriptionEditText = findViewById(R.id.description_text)
        rateButton = findViewById(R.id.rateButton)
        ignoreButton = findViewById(R.id.ignoreButton)
        rateButton!!.setOnClickListener{
            presenter?.onRateClicked()
        }
        ignoreButton!!.setOnClickListener{
            presenter?.onIgnoreClicked()
        }
    }

    override fun finish(resultCode: Int) {
        setResult(resultCode)
        finish()
    }

    override fun setNameCardView(name: String) {
        nameTextView?.text = name
    }

    override fun setRoleTextView(type: String) {
        roleTextView?.text = type
    }

    override fun setImageProfile(uri: Uri) {
        Picasso.with(this)
                .load(uri)
                .resize(500, 500)
                .into(profileImage)
    }

    override fun getRating(): Double {
        return ratingBar?.rating!!.toDouble()
    }

    override fun getDescription(): String {
        return descriptionEditText?.text.toString()
    }

    override fun showCardView() {
        rateCardLayout?.visibility = (View.VISIBLE)
    }

    override fun hideCardView() {
        rateCardLayout?.visibility = (View.GONE)
    }

    override fun showProgressBar() {
        progressLayout?.visibility = (View.VISIBLE)
    }

    override fun hideProgressBar() {
        progressLayout?.visibility = (View.GONE)
    }

    override fun setRating(rating: Double) {
        ratingBar?.rating = rating.toFloat()
    }

    override fun setDescription(description: String) {
        descriptionEditText?.setText(description)
    }

    private fun push(value: AssistantModel) {
        userQueue!!.add(value)
    }

    override fun setImageProfile(resId: Int) {
        profileImage?.setImageResource(resId)
    }
}
