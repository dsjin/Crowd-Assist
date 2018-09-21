package th.ac.kmitl.it.crowdassist

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import th.ac.kmitl.it.crowdassist.contract.CreateGeneralRequestContract
import th.ac.kmitl.it.crowdassist.presenter.CreateGeneralRequestPresenter
import th.ac.kmitl.it.crowdassist.util.DatabaseHelper
import th.ac.kmitl.it.crowdassist.util.LocationHelper

class CreateGeneralRequestActivity : AppCompatActivity(), CreateGeneralRequestContract.View , OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private var progressLayout: LinearLayout? = null
    private var circularProgress: ProgressBar? = null
    private var horizontalProgress: ProgressBar? = null
    private var addPhoto: ImageView? = null
    private var mainLayout: android.support.constraint.ConstraintLayout? = null
    private var imageLayout: RelativeLayout? = null
    private var mapType : MutableMap<String, String>? = null
    private val presenter = CreateGeneralRequestPresenter(DatabaseHelper(this), this, Schedulers.io(), AndroidSchedulers.mainThread())
    private var mMap: GoogleMap? = null
    private var mapFragment: SupportMapFragment? = null
    private var location: Location? = null
    private var mLocationRequest: LocationRequest? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var mGoogleApiClient: GoogleApiClient? = null
    private var type: String? = null
    private var typeTitle: TextView? = null
    private var confirmButton: LinearLayout? = null
    private var description: EditText? = null
    private var requesterType: RadioGroup? = null
    private var uri : Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_general_request)
        val toolbar = findViewById<Toolbar>(R.id.toolbar_top)
        setSupportActionBar(toolbar)
        title = ""
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        typeTitle = findViewById(R.id.type_title)
        confirmButton = findViewById(R.id.sendButton)
        description = findViewById(R.id.description_text)
        imageLayout = findViewById(R.id.imageLayout)
        addPhoto = findViewById(R.id.add_photo)
        mainLayout = findViewById(R.id.mainLayout)
        progressLayout = findViewById(R.id.progress_layout)
        circularProgress = findViewById(R.id.circularProgress)
        horizontalProgress = findViewById(R.id.horizontalProgress)
        requesterType = findViewById(R.id.requesterType)

        mapType = mutableMapOf()
        setupMapType()
        val intent = intent
        type = intent.getStringExtra("type")
        if (mGoogleApiClient == null) {
            mGoogleApiClient = GoogleApiClient.Builder(this)
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
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval((10 * 1000).toLong())
                .setFastestInterval((1 * 1000).toLong())
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
        type?.let {
            typeTitle?.text = (String.format("หมวดหมู่คำร้อง : %s", type))
        }

        imageLayout?.setOnClickListener{
            presenter.onSelectPhotoButtonClicked()
        }
        confirmButton?.setOnClickListener{
            presenter.onSendButtonClicked(location, type, uri)
        }
    }

    override fun showCircularProgressBar() {
        circularProgress?.visibility = View.VISIBLE
        horizontalProgress?.visibility = View.GONE
    }

    override fun hideCircularProgressBar() {
        circularProgress?.visibility = View.GONE
        horizontalProgress?.visibility = View.GONE
    }

    override fun showProgressBar() {
        circularProgress?.visibility = View.GONE
        horizontalProgress?.visibility = View.VISIBLE
    }

    override fun hideProgressBar() {
        circularProgress?.visibility = View.GONE
        horizontalProgress?.visibility = View.GONE
    }

    override fun showProgreesLayout() {
        progressLayout?.visibility = View.VISIBLE
    }

    override fun hideProgressLayout() {
        progressLayout?.visibility = View.GONE
    }

    override fun setPrograssBarValue(percent: Int) {
        horizontalProgress?.progress = percent
    }

    override fun showMainLayout() {
        mainLayout?.visibility = View.VISIBLE
    }

    override fun hideMainLayout() {
        mainLayout?.visibility = View.GONE
    }

    override fun finishActivity() {
        setResult(Activity.RESULT_OK)
        finish()
    }

    override fun getAllTextFill(): MutableMap<String, String> {
        val textFills = mutableMapOf<String, String>()
        textFills.put("description", description?.text.toString())
        return textFills
    }

    override fun getRequesterTypeRadioGroup(): RadioGroup {
        return requesterType!!
    }

    override fun showSnackBar(message: String, during: Int) {
        Snackbar.make(findViewById<ConstraintLayout>(R.id.mainLayout), message , during).show()
    }

    private fun setupMapType() {
        mapType?.put("อัคคีภัย", "type1")
        mapType?.put("อุบัติเหตุทางถนน", "type2")
        mapType?.put("อุบัติเหตุทางน้ำ", "type3")
        mapType?.put("อื่นๆ", "type4")
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap
        mMap?.setMyLocationEnabled(true)
    }

    override fun onConnected(p0: Bundle?) {
        if (!hasPermission(Manifest.permission.ACCESS_FINE_LOCATION))
            return
        fusedLocationClient.lastLocation
                .addOnSuccessListener({
                    currentLocation : Location? ->
                    currentLocation?.let {
                        this.location = currentLocation
                        if (mMap != null) {
                            mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(this.location!!.getLatitude(), this.location!!.getLongitude()), 15f))
                        }
                    } ?:run {
                        fusedLocationClient.requestLocationUpdates(mLocationRequest, locationCallback, null)
                    }
                })
    }

    override fun onConnectionSuspended(p0: Int) {
        Toast.makeText(this, "การเชื่อมต่อถูกตัด กรุณาลองอีกครั้ง", Toast.LENGTH_SHORT).show()
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        Toast.makeText(this, "ไม่สามารถติดต่อ Google ได้ โปรดตรวจสอบการเชื่อมต่อ Internet", Toast.LENGTH_SHORT).show()
    }

    infix fun Any?.ifNull(block: () -> Unit) {
        if (this == null) block()
    }

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                val resultUri = result.uri
                Picasso.with(this)
                        .load(resultUri)
                        .into(addPhoto)
                this.uri = resultUri
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
                Log.e("Activity Result", error.message)
            }
        }
    }

    override fun getLocationName(location : Location?) : String{
        return LocationHelper.getLocationName( this , location)
    }

    override fun startCropImage() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.OFF)
                .setCropShape(CropImageView.CropShape.RECTANGLE)
                .start(this)
    }
}
