package th.ac.kmitl.it.crowdassist.presenter

import android.content.Context
import android.location.Location
import android.net.Uri
import android.support.design.widget.Snackbar
import android.util.Log
import android.widget.EditText
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import th.ac.kmitl.it.crowdassist.CreateGeneralRequestActivity
import th.ac.kmitl.it.crowdassist.R
import th.ac.kmitl.it.crowdassist.contract.CreateGeneralRequestContract
import th.ac.kmitl.it.crowdassist.modal.GeneralRequestModel
import th.ac.kmitl.it.crowdassist.modal.Request
import th.ac.kmitl.it.crowdassist.util.DatabaseHelper
import th.ac.kmitl.it.crowdassist.util.LocationHelper
import java.io.IOException
import java.util.concurrent.Callable

class CreateGeneralRequestPresenter(val ctx : Context, val view : CreateGeneralRequestContract.View) : CreateGeneralRequestContract.Presenter{
    private val databaseHelper = DatabaseHelper(ctx)
    private val emptyEditText: ((Map.Entry<String, EditText>)) -> Boolean = { it.value.text.isEmpty() }

    override fun onSendButtonClicked(location : Location?, type : String?, uri : Uri?) {
        val editText = view.getAllEditText()
        if (!editText.filter(emptyEditText).isEmpty()){
            view.showSnackBar("กรุณาใส่ข้อมูลให้ครบ", Snackbar.LENGTH_SHORT)
            return
        }
        var requesterTypeString = ""
        when (view.getRequesterTypeRadioGroup().checkedRadioButtonId) {
            R.id.radio_victim -> requesterTypeString = "victim"
            R.id.radio_observer -> requesterTypeString = "observer"
        }
        val data = GeneralRequestModel()
        data.type = type
        data.description = editText.get("description")?.text.toString()
        data.lat = location?.latitude
        data.lng = location?.longitude
        data.time = 1
        data.requesterType = requesterTypeString
        try {
            data.title = LocationHelper.getLocationName( ctx , location)
        } catch (exception: IOException) {
            Log.e("LocationHelper", exception.message)
        }
        data.status = "wait"
        view.hideMainLayout()
        view.showProgreesLayout()
        view.showProgressBar()
        Observable.fromCallable(resolveCallable(location!!))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .take(1)
                .subscribe(action1(data, uri!!))
    }

    override fun onSelectPhotoButtonClicked() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.OFF)
                .setCropShape(CropImageView.CropShape.RECTANGLE)
                .start(ctx as CreateGeneralRequestActivity)
    }

    private fun action1(data: Request, uri : Uri): Consumer<String> {
        return Consumer { s ->
            data.area = s
            databaseHelper.createGeneralRequest(data, uri, view)
        }
    }

    private fun resolveCallable(location : Location): Callable<String> {
        return Callable { LocationHelper.resolveArea(location) }
    }
}