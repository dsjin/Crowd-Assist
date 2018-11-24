package th.ac.kmitl.it.crowdassist.fragment

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import th.ac.kmitl.it.crowdassist.R
import th.ac.kmitl.it.crowdassist.model.Request
import th.ac.kmitl.it.crowdassist.util.GoToCallback
import th.ac.kmitl.it.crowdassist.util.LocationHelper
import th.ac.kmitl.it.crowdassist.util.SetButtonListenerCallback
import java.util.concurrent.Callable
import kotlin.properties.Delegates

class MainFragment : Fragment(), OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    private var rootLayout: FrameLayout? = null
    private var onClickCallback: SetButtonListenerCallback.ClickListener? = null
    private var onLongClickCallback : SetButtonListenerCallback.LongClickListener? = null
    private var goToCallback: GoToCallback? = null
    private val PROFILE_SP = "profile"
    private var mGoogleApiClient: GoogleApiClient? = null
    private var sp: SharedPreferences? = null
    private var mAuth by Delegates.notNull<FirebaseAuth>()
    private var mUser: FirebaseUser? = null
    private var location: Location? = null
    private var mLocationRequest: LocationRequest? = null
    private var mMap: GoogleMap? = null
    private var mapFragment: SupportMapFragment? = null
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
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (currentLocation in locationResult.locations){
                    mMap?.let{
                        location?.ifNull{
                            mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(currentLocation!!.latitude, currentLocation!!.longitude), 15f))
                        }
                    }
                    location = currentLocation
                }
            }
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity!!)
        sp = activity!!.getSharedPreferences(PROFILE_SP, Context.MODE_PRIVATE)
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval((10 * 1000).toLong())
                .setFastestInterval((1 * 1000).toLong())
        mAuth = FirebaseAuth.getInstance()
        mUser = mAuth.currentUser
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_main, container, false)
        rootLayout = view.findViewById(R.id.root_view)
        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
        onClickCallback?.let {
            onClickCallback?.setClick(View.OnClickListener {

            })
        }
        onLongClickCallback?.let {
            onLongClickCallback?.setLongClick(View.OnLongClickListener {
                if (sp!!.getBoolean("verify", false)) {
                    val data = getData()
                    val location = Location("")
                    location.latitude = data!!.getLat()
                    location.longitude = data!!.getLng()
                    Observable.fromCallable(resolveCallable(location)).subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .take(1)
                            .subscribe(action1(data))
                } else {
                    Snackbar.make(rootLayout!!, "กรุณายืนยันตัวตนก่อนการใช้งาน", Snackbar.LENGTH_SHORT).setAction("ไปยังหน้ายืนยัน", this).show()
                }
                true
            })
        }
        return view
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is Activity) {
            onClickCallback = context as SetButtonListenerCallback.ClickListener
            onLongClickCallback = context as SetButtonListenerCallback.LongClickListener
        } else {
            throw RuntimeException(context!!.toString() + " must implement GoToCallBack")
        }
    }

    override fun onDetach() {
        super.onDetach()
        onClickCallback = null
        onLongClickCallback = null
    }

    override fun onStart() {
        super.onStart()
        mGoogleApiClient?.connect()
    }

    override fun onStop() {
        super.onStop()
        mGoogleApiClient?.disconnect()
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap
        mMap?.setMyLocationEnabled(true)
        location?.let {
            mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location!!.getLatitude(), location!!.getLongitude()), 15f))
        }
    }

    override fun onConnected(p0: Bundle?) {
        if (!hasPermission(Manifest.permission.ACCESS_FINE_LOCATION))
            return
        fusedLocationClient.lastLocation
                .addOnSuccessListener{
                    currentLocation : Location? ->
                        currentLocation?.let {
                            this.location = currentLocation
                            if (mMap != null) {
                                mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(this.location!!.latitude, this.location!!.longitude), 15f))
                            }
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

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(activity!!, permission) == PackageManager.PERMISSION_GRANTED
    }

    infix fun Any?.ifNull(block: () -> Unit) {
        if (this == null) block()
    }

    private fun action1(data: Request): Consumer<String> {
        return Consumer { s ->
            data.setArea(s)
            //val dialog = SendingDialog(activity, data)
            //dialog.show(10000)
        }
    }

    private fun resolveCallable(location: Location): Callable<String> {
        return Callable { LocationHelper.resolveArea(location) }
    }

    private fun getData(): Request? {
        if (mGoogleApiClient!!.isConnected) {
            val data = Request()
            data.requesterUid = mUser?.uid
            data.lat = location?.latitude
            data.lng = location?.longitude
            data.status = "wait"
            data.time = 1
            return data
        }
        return null
    }

    override fun onClick(view: View) {
        goToCallback?.goTo("Setting")
    }
}
