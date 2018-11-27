package th.ac.kmitl.it.crowdassist.fragment


import android.Manifest
import android.app.Activity
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.model.Marker

import th.ac.kmitl.it.crowdassist.R
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.support.annotation.NonNull
import th.ac.kmitl.it.crowdassist.model.AssistantModel
import th.ac.kmitl.it.crowdassist.util.DatabaseHelper
import android.support.design.widget.BottomSheetBehavior
import com.google.android.gms.maps.SupportMapFragment
import th.ac.kmitl.it.crowdassist.component.CustomBottomSheetBehavior
import android.support.design.widget.FloatingActionButton
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.widget.*
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import th.ac.kmitl.it.crowdassist.model.DataSnapshotWithTypeModel
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.firebase.database.DataSnapshot
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import org.joda.time.LocalDate
import org.joda.time.Years
import th.ac.kmitl.it.crowdassist.ConfirmActivity
import th.ac.kmitl.it.crowdassist.ManageRequestingActivity
import th.ac.kmitl.it.crowdassist.RequestInformationActivity
import th.ac.kmitl.it.crowdassist.model.Request
import th.ac.kmitl.it.crowdassist.model.UserModel
import th.ac.kmitl.it.crowdassist.util.LocationHelper
import th.ac.kmitl.it.crowdassist.util.SetToRateCallback
import th.ac.kmitl.it.crowdassist.viewmodel.*
import java.util.*


class HelpedFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private val SPREQUEST = "request_information"
    private var markers: MutableMap<String, Marker>? = null
    private var spRequest: SharedPreferences? = null
    private var requestUid: String? = null
    private var type: String? = null
    private var officerList: MutableList<AssistantModel>? = null
    private var volunteerList: MutableList<AssistantModel>? = null
    private var databaseHelper: DatabaseHelper? = null
    private var data: Request? = null
    private var behavior: CustomBottomSheetBehavior<*>? = null
    private var mapFragment: SupportMapFragment? = null
    private var resend: Button? = null
    private var name: TextView? = null
    private var age: TextView? = null
    private var gender: TextView? = null
    private var roleTextView: TextView? = null
    private var timeEstimated: TextView? = null
    private var confirm: FloatingActionButton? = null
    private var progress: LinearLayout? = null
    private var userLayout: LinearLayout? = null
    private var userImage: ImageView? = null
    private var requesterMarker: Marker? = null
    private var assistantViewModel: AssistantInformationViewModel? = null
    private var assistantLiveData: LiveData<DataSnapshotWithTypeModel>? = null
    private var requestStatusViewModel: HandleRequestStatusViewModel? = null
    private var requestStatusLiveData: LiveData<DataSnapshot>? = null
    private var setToRateCallback: SetToRateCallback? = null
    private var mMap: GoogleMap? = null
    private var requestInformationViewModel: RequestInformationViewModel? = null
    private var requestInformationLiveData: LiveData<DataSnapshot>? = null
    private var mStorage: FirebaseStorage? = null
    private var currentLocation: Location? = null
    private var userMarker: Marker? = null
    private var mGoogleApiClient: GoogleApiClient? = null
    private var mLocationRequest: LocationRequest? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        if (mGoogleApiClient == null) {
            mGoogleApiClient = GoogleApiClient.Builder(activity!!)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build()
        }
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?){
                locationResult ?: return
                for (currentLocation in locationResult.locations){
                    handleNewLocation(currentLocation)
                }
            }
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity!!)
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval((10 * 1000).toLong())
                .setFastestInterval((1 * 1000).toLong())
        mStorage = FirebaseStorage.getInstance()
        markers = mutableMapOf()
        spRequest = activity!!.getSharedPreferences(SPREQUEST, Context.MODE_PRIVATE)
        requestUid = spRequest?.getString("request_uid", null)
        type = spRequest?.getString("type", null)
        officerList = mutableListOf()
        volunteerList = mutableListOf()
        databaseHelper = DatabaseHelper(activity!!)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_helped, container, false)
        val includeLayout = view.findViewById<LinearLayout>(R.id.bottom_layout)
        val bottomSheet = includeLayout.findViewById<RelativeLayout>(R.id.design_bottom_sheet)
        behavior = BottomSheetBehavior.from<View>(bottomSheet) as CustomBottomSheetBehavior<*>
        behavior?.state = BottomSheetBehavior.STATE_HIDDEN
        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
        resend = view.findViewById(R.id.resend)
        confirm = view.findViewById(R.id.confirmButton)
        name = view.findViewById(R.id.name)
        age = view.findViewById(R.id.age)
        gender = view.findViewById(R.id.gender)
        roleTextView = view.findViewById(R.id.role)
        timeEstimated = view.findViewById(R.id.timeEstimated)
        progress = view.findViewById(R.id.progress_layout)
        userLayout = view.findViewById(R.id.userLayout)
        userImage = view.findViewById(R.id.userImageView)

        resend?.setOnClickListener{
            val time = spRequest?.getInt("time", 0);
            val emergencyUid = spRequest?.getString("request_uid", null)
            if (time != 0){
                databaseHelper?.updateEmergencyTime(emergencyUid, time!!+1)
            }
            Toast.makeText(activity!!, "ส่งคำร้องใหม่แล้ว", Toast.LENGTH_SHORT).show()
        }
        confirm?.setOnClickListener{
            val intent = Intent(activity!!, ConfirmActivity::class.java)
            activity!!.startActivity(intent)
        }

        behavior?.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(@NonNull bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_DRAGGING -> {
                    }
                    BottomSheetBehavior.STATE_SETTLING -> {
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                    }
                    BottomSheetBehavior.STATE_HIDDEN -> confirm?.show()
                }
            }

            override fun onSlide(@NonNull bottomSheet: View, slideOffset: Float) {

            }
        })

        assistantViewModel = ViewModelProviders.of(this).get(AssistantInformationViewModel::class.java)
        assistantLiveData = assistantViewModel?.liveData
        requestStatusViewModel = ViewModelProviders.of(this).get(HandleRequestStatusViewModel::class.java)
        requestStatusLiveData = requestStatusViewModel?.dataSnapshotLiveData
        requestInformationViewModel = ViewModelProviders.of(this).get(RequestInformationViewModel::class.java)
        requestInformationLiveData = requestInformationViewModel?.dataSnapshotLiveData


        return view
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap
        data?.let{
            requesterMarker.ifNull {
                val options = MarkerOptions()
                    .position(LatLng(data?.lat!!, data?.lng!!))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.me_pin))
                requesterMarker = mMap?.addMarker(options);
                requesterMarker?.title = "คลิกเพื่อดูรายละเอียด"
                requesterMarker?.tag = data?.requesterUid
                mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(data?.lat!!, data?.lng!!), 15.toFloat()))
            }
        }
        mMap?.setOnInfoWindowClickListener(this)
        currentLocation?.let{
            val currentLatitude = currentLocation?.latitude
            val currentLongitude = currentLocation?.longitude
            val latLng = LatLng(currentLatitude!!, currentLongitude!!)
            userMarker?.let{
                it.position = latLng
            }?:run{
                val options = MarkerOptions()
                        .position(latLng)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.me_pin))
                userMarker = mMap?.addMarker(options)
                mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15.toFloat()))
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mGoogleApiClient?.connect()
        assistantLiveData?.observe(this, Observer<DataSnapshotWithTypeModel>{
            data -> kotlin.run {
                when(data?.type){
                    "onChildAdded" -> {
                        val assistantData = data?.dataSnapshot?.getValue(AssistantModel::class.java)
                        assistantData?.let {
                            assistantData.assistantUid = data?.dataSnapshot?.key!!
                            val options = MarkerOptions()
                                    .position(LatLng(assistantData.lat, assistantData.lng))
                                    .title(assistantData.assistantUid)
                             when(assistantData.role){
                                "officer"->{
                                    options.icon(BitmapDescriptorFactory.fromResource(R.drawable.officer_pin))
                                    officerList?.add(assistantData)
                                }
                                "user"->{
                                    options.icon(BitmapDescriptorFactory.fromResource(R.drawable.volunteer_pin))
                                    volunteerList?.add(assistantData)
                                }
                                 else -> {}
                            }
                            val marker = mMap?.addMarker(options)
                            marker?.title = ("คลิกเพื่อดูรายละเอียด")
                            marker?.tag = (assistantData.assistantUid)
                            markers?.put(assistantData.assistantUid, marker!!)
                            val count = spRequest?.getInt("assistance", 0)
                            val editor = spRequest?.edit()
                            editor?.putInt("assistance", count!! + 1)
                            editor?.apply()
                            }
                        }
                    "onChildChanged"->{
                        val assistantData = data?.dataSnapshot?.getValue(AssistantModel::class.java)
                        assistantData?.let{
                            assistantData?.assistantUid = data?.dataSnapshot?.key!!
                            val marker = markers?.get(assistantData.assistantUid)
                            marker?.setPosition(LatLng(assistantData.lat, assistantData.lng))
                        }
                    }
                    else -> {}
                }
            }
        })
        requestInformationLiveData?.observe(this, Observer<DataSnapshot> {
            data: DataSnapshot? -> kotlin.run {
                this.data = data?.getValue(Request::class.java)
            data?.child("timestamp")?.getValue(Long::class.java)?.let{
                this.data?.setTimestamp(data.child("timestamp").getValue(Long::class.java))
                }
                val options = MarkerOptions()
                            .position(LatLng(this.data?.lat!!, this.data?.lng!!))
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.requester_pin))
                mMap?.let{
                    requesterMarker.ifNull {
                        requesterMarker = mMap?.addMarker(options)
                        requesterMarker?.title = "คลิกเพื่อดูรายละเอียด"
                        requesterMarker?.tag = this.data?.requesterUid
                        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(this.data?.lat!!, this.data?.lng!!), 15.toFloat()))
                    }
                }
            }
        })
        if ("Helped" == spRequest?.getString("mode", "")){
            requestStatusLiveData?.observe(this, Observer<DataSnapshot> {
                data: DataSnapshot? ->  kotlin.run {
                data?.let{
                    if ("close" == data.value){
                        showMessageOK("คำร้องได้สิ้นสุดลงแล้ว", DialogInterface.OnClickListener{
                            _, _ -> kotlin.run {
                            val editor:SharedPreferences.Editor? = spRequest?.edit()
                            editor?.putString("mode","Rate")
                            editor?.apply()
                            setToRateCallback?.setToRate()
                        }
                        })
                    }
                }
            }
            })
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is Activity) {
            setToRateCallback = context as SetToRateCallback
        } else {
            throw RuntimeException(context!!.toString() + " must implement GoToCallBack")
        }
    }

    override fun onStop(){
        super.onStop()
        assistantLiveData?.removeObservers(this)
        requestStatusLiveData?.removeObservers(this)
        requestInformationLiveData?.removeObservers(this)
    }

    override fun onDetach() {
        super.onDetach()
        setToRateCallback = null
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun showMessageOK(message: String?, okListener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(activity!!)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .create()
                .show()
    }

    override fun onInfoWindowClick(marker: Marker?) {
        if (marker?.tag == requesterMarker?.tag){
            val intent = Intent(activity!!, ManageRequestingActivity::class.java)
            intent.putExtra("uid", requestUid)
            intent.putExtra("type", type)
            intent.putExtra("officer", officerList?.toTypedArray())
            intent.putExtra("volunteer", volunteerList?.toTypedArray())
            intent.putExtra("data", data)
            intent.putExtra("request_id", requestUid)
            activity!!.startActivityForResult(intent, 5000)
        }else{
            confirm?.hide()
            val uid = marker?.tag as String
            setUser(uid, marker)
            behavior?.setState(BottomSheetBehavior.STATE_COLLAPSED)
        }
    }

    private fun setUser(uid:String?, marker:Marker?) {
        progress?.visibility = View.VISIBLE
        userLayout?.visibility = View.GONE
        val userViewModel = ViewModelProviders.of(this, UserInformationViewModelFactory(this.activity?.application, uid)).get(UserInformationViewModel::class.java)
        val userLiveData = userViewModel.dataSnapshotLiveData
        userLiveData.observe(this, Observer<DataSnapshot> { data ->
            kotlin.run {
                data?.let {
                    val userInfo = data.getValue(UserModel::class.java)
                    val birthOfDate = LocalDate(userInfo?.dateOfBirth)
                    val now = LocalDate(Calendar.getInstance().time)
                    name?.text = String.format("%s %s", userInfo?.firstName, userInfo?.lastName)
                    gender?.text = String.format("%s, ", if ("male" == userInfo?.gender) "ชาย" else "หญิง")
                    age?.text = String.format(Locale.forLanguageTag("th-TH"), "%d", Years.yearsBetween(birthOfDate, now).years)
                    roleTextView?.text = String.format("%s", if ("user" == data.child("role").getValue(String::class.java)) "อาสาสมัคร" else "เจ้าหน้าที่")
                    val userPosition: com.google.maps.model.LatLng = com.google.maps.model.LatLng(marker?.position?.latitude!!, marker?.position?.longitude!!)
                    val requesterPosition: com.google.maps.model.LatLng = com.google.maps.model.LatLng(requesterMarker?.position?.latitude!!, requesterMarker?.position?.longitude!!)
                    timeEstimated?.text = String.format("คาดว่าจะมาถึงในอีก %s", LocationHelper.getEstimateTravelTime(userPosition, requesterPosition))
                    mStorage?.reference?.child("profile")?.child("$uid.jpg")?.downloadUrl?.addOnSuccessListener { uri ->
                        kotlin.run {
                            Picasso.with(activity!!)
                                    .load(uri)
                                    .resize(500, 500)
                                    .into(userImage)
                        }
                    }
                    progress?.visibility = (View.GONE)
                    userLayout?.visibility = (View.VISIBLE)
                }
                userLiveData.removeObservers(this)
            }
        })

    }

    override fun onConnected(p0: Bundle?) {
        if (!hasPermission(Manifest.permission.ACCESS_FINE_LOCATION))
            return
        fusedLocationClient.lastLocation
                .addOnSuccessListener{
                    currentLocation : Location? ->
                    currentLocation?.let {
                        handleNewLocation(currentLocation)
                    }
                    fusedLocationClient.requestLocationUpdates(mLocationRequest, locationCallback, null)
                }
    }

    override fun onConnectionSuspended(p0: Int) {
        Toast.makeText(activity!!, "การเชื่อมต่อถูกตัด กรุณาลองอีกครั้ง", Toast.LENGTH_SHORT).show()
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        Toast.makeText(activity!!, "ไม่สามารถติดต่อ Google ได้ โปรดตรวจสอบการเชื่อมต่อ Internet", Toast.LENGTH_SHORT).show()
    }

    private fun handleNewLocation(location: Location?) {
        currentLocation = location
        databaseHelper?.updateHelperLocation(requestUid, location, type)

        val currentLatitude = location?.latitude
        val currentLongitude = location?.longitude

        val latLng : LatLng? = LatLng(currentLatitude!!, currentLongitude!!)

        mMap?.let {
            userMarker?.let{
                userMarker?.position = latLng
            }?:run{
                val options = MarkerOptions()
                        .position(latLng!!)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.me_pin))
                userMarker = mMap?.addMarker(options)
                mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.toFloat()))
            }
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(activity!!, permission) == PackageManager.PERMISSION_GRANTED
    }

    private infix fun Any?.ifNull(block: () -> Unit) {
        if (this == null) block()
    }
}
