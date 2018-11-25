package th.ac.kmitl.it.crowdassist.fragment


import android.Manifest
import android.app.Activity
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import com.akexorcist.googledirection.DirectionCallback
import com.akexorcist.googledirection.GoogleDirection
import com.akexorcist.googledirection.constant.RequestResult
import com.akexorcist.googledirection.constant.TransportMode
import com.akexorcist.googledirection.model.Direction
import com.akexorcist.googledirection.util.DirectionConverter
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase

import th.ac.kmitl.it.crowdassist.R
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import th.ac.kmitl.it.crowdassist.ConfirmActivity
import th.ac.kmitl.it.crowdassist.RequestInformationActivity
import th.ac.kmitl.it.crowdassist.model.Request
import th.ac.kmitl.it.crowdassist.util.DatabaseHelper
import th.ac.kmitl.it.crowdassist.util.SetButtonListenerCallback
import th.ac.kmitl.it.crowdassist.util.SetToRateCallback
import th.ac.kmitl.it.crowdassist.viewmodel.HandleRequestStatusViewModel
import th.ac.kmitl.it.crowdassist.viewmodel.RequestInformationViewModel
import th.ac.kmitl.it.crowdassist.viewmodel.RequesterPointViewModel

class HelpFragment : Fragment(), OnMapReadyCallback,  GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnInfoWindowClickListener, DirectionCallback {
    val SPREQUEST = "request_information"
    val SPPROFILE = "profile"
    var mDatabase: FirebaseDatabase? = null
    var spRequest: SharedPreferences? = null
    var requestUid: String? = null
    var type: String? = null
    var mMap: GoogleMap? = null
    var requesterMarker: Marker? = null
    var requesterLocation: LatLng? = null
    var requesterPointViewModel: RequesterPointViewModel? = null
    var requesterPointLiveData: LiveData<DataSnapshot>? = null
    var requestInformationViewModel: RequestInformationViewModel? = null
    var requestStatusViewModel: HandleRequestStatusViewModel? = null
    var requestStatusLivedata: LiveData<DataSnapshot>? = null
    var requestMarker: Marker? = null
    var data: Request? = null
    var setToRateCallback: SetToRateCallback? = null
    var currentLocation: Location? = null
    var userMarker: Marker? = null
    var mGoogleApiClient: GoogleApiClient? = null
    var mLocationRequest: LocationRequest? = null
    var mapFragment: SupportMapFragment? = null
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    override fun onCreate(savedInstanceState: Bundle?) {
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
        mDatabase = FirebaseDatabase.getInstance()
        spRequest = activity?.getSharedPreferences(SPREQUEST, Context.MODE_PRIVATE)
        requestUid = spRequest?.getString("request_uid", null)
        type = spRequest?.getString("type", null)
        requestInformationViewModel = ViewModelProviders.of(this).get(RequestInformationViewModel::class.java)
        requestInformationViewModel?.dataSnapshotLiveData?.observe(this, Observer<DataSnapshot>{
            data: DataSnapshot? ->  kotlin.run {
            data?.let {
                this.data = data.getValue(Request::class.java)
                if (mMap != null){
                    val options = MarkerOptions()
                            .position(LatLng(this.data?.lat!!, this.data?.lng!!))
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.requester_pin))
                    requestMarker = mMap?.addMarker(options)
                    requestMarker?.title = "คลิกเพื่อดูรายละเอียด"
                    requestMarker?.tag = this.data?.requesterUid
                }
            }
        }
        })
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity!!)
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval((10 * 1000).toLong())
                .setFastestInterval((1 * 1000).toLong())
        databaseHelper = DatabaseHelper(activity!!)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_help, container, false)
        val profile = activity?.getSharedPreferences(SPPROFILE, Context.MODE_PRIVATE)
        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
        val direction = view.findViewById<FloatingActionButton>(R.id.fab_direction)
        direction.setOnClickListener{
            _ -> kotlin.run{
                val details = arrayOf<CharSequence>("นำทางไปยังจุดแจ้งเหตุ", "นำทางไปยังผู้ร้องขอ")
                val builder = AlertDialog.Builder(activity!!)
                builder.setTitle("เลือกการนำทาง")
                builder.setItems(details) { _ , i ->
                    if (i == 0) {
                        getDirection("request_point")
                    } else {
                        getDirection("requester_point")
                    }
                }
                builder.show()
            }
        }
        val confirm = view.findViewById<Button>(R.id.confirmButton)
        if (profile?.getString("role", "null") == "officer"){
            confirm.visibility = (View.VISIBLE)
            confirm.setOnClickListener{
                _ -> kotlin.run {
                    val intent = Intent(activity, ConfirmActivity::class.java)
                    activity?.startActivity(intent)
                }
            }
        }

        requesterPointViewModel = ViewModelProviders.of(this).get(RequesterPointViewModel::class.java)
        requesterPointLiveData = requesterPointViewModel?.dataSnapshotLiveData
        requestStatusViewModel = ViewModelProviders.of(this).get(HandleRequestStatusViewModel::class.java)
        requestStatusLivedata = requestStatusViewModel?.dataSnapshotLiveData

        return view
    }

    private fun getDirection(type: String?){
        when(type){
            "request_point" -> {
                GoogleDirection.withServerKey(getString(R.string.google_direction_api))
                        .from(LatLng(currentLocation?.latitude!!, currentLocation?.longitude!!))
                .to(LatLng(data?.lat!!, data?.lng!!))
                .transportMode(TransportMode.WALKING)
                        .execute(this)
            }
            "requester_point"->{
                GoogleDirection.withServerKey(getString(R.string.google_direction_api))
                        .from(LatLng(currentLocation?.latitude!!, currentLocation?.longitude!!))
                .to(LatLng(requesterMarker?.position?.latitude!!, requesterMarker?.position?.longitude!!))
                .transportMode(TransportMode.WALKING)
                        .execute(this)
            }
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

    override fun onStart() {
        super.onStart()
        requesterPointLiveData?.observe(this, Observer<DataSnapshot>{
            data: DataSnapshot? ->  kotlin.run {
                data?.let {
                    val lat = data.child("lat").getValue(Double::class.java)
                    val lng = data.child("lng").getValue(Double::class.java)

                    mMap?.let {
                        requesterMarker?.let {
                            requesterMarker?.setPosition(LatLng(lat!!, lng!!))
                        }?:run{
                            val options = MarkerOptions()
                                    .position(LatLng(lat!!, lng!!))
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.runner_pin))
                            requesterMarker = mMap?.addMarker(options)
                        }
                    }?:run {
                        requesterLocation = LatLng(lat!!, lng!!)
                    }
                }
            }
        })
        if ("Help" == spRequest?.getString("mode", "")){
            requestStatusLivedata?.observe(this, Observer<DataSnapshot> {
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
        mGoogleApiClient?.connect()
    }

    override fun onDetach() {
        super.onDetach()
        setToRateCallback = null
    }

    override fun onStop() {
        super.onStop()
        requestStatusLivedata?.removeObservers(this)
        requesterPointLiveData?.removeObservers(this)
        mGoogleApiClient?.disconnect()
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

    private fun handleNewLocation(location : Location?) {
        currentLocation = location
        databaseHelper.updateHelperLocation(requestUid, location, type);

        val currentLatitude = location?.latitude
        val currentLongitude = location?.longitude

        val latLng : LatLng? = LatLng(currentLatitude!!, currentLongitude!!)

        mMap?.let {
            if (userMarker == null){
                val options = MarkerOptions()
                        .position(latLng!!)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.me_pin))
                userMarker = mMap?.addMarker(options)
                mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.toFloat()))
            }else{
                userMarker?.position = latLng
            }
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap
        mMap?.setOnInfoWindowClickListener(this)
        currentLocation?.let{
            val currentLatitude = currentLocation?.latitude
            val currentLongitude = currentLocation?.longitude
            val latLng : LatLng? = LatLng(currentLatitude!!, currentLongitude!!)
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
        requesterLocation?.let{
            requesterMarker?.let {
                requesterMarker?.setPosition(requesterLocation!!)
            }?:run{
                val options = MarkerOptions()
                        .position(requesterLocation!!)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.runner_pin))
                requesterMarker = mMap?.addMarker(options)
            }
        }
    }

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(activity!!, permission) == PackageManager.PERMISSION_GRANTED
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

    override fun onDirectionSuccess(direction: Direction?, rawBody: String?) {
        val status = direction?.status
        if(status == RequestResult.OK) {
            val route = direction?.routeList?.get(0)
            val leg = route?.legList?.get(0)
            val directionPositionList = leg?.directionPoint
            val polylineOptions = DirectionConverter.createPolyline(activity!!, directionPositionList, 5, Color.RED);
            mMap?.addPolyline(polylineOptions)
        }
    }

    override fun onDirectionFailure(t: Throwable?) {
        Log.e("HelpFragment", t?.localizedMessage)
    }

    override fun onInfoWindowClick(marker: Marker?) {
        if (marker?.tag == requestMarker?.tag){
            val intent = Intent(activity!!, RequestInformationActivity::class.java)
            intent.putExtra("uid", requestUid)
            intent.putExtra("type", type)
            intent.putExtra("data", data)
            intent.putExtra("request_id", requestUid)
            activity!!.startActivity(intent)
        }
    }
}
