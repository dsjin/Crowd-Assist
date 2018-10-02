package th.ac.kmitl.it.crowdassist.presenter

import android.location.Location
import android.net.Uri
import android.support.design.widget.Snackbar
import android.util.Log
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.functions.Consumer
import th.ac.kmitl.it.crowdassist.R
import th.ac.kmitl.it.crowdassist.contract.CreateGeneralRequestContract
import th.ac.kmitl.it.crowdassist.model.GeneralRequestModel
import th.ac.kmitl.it.crowdassist.model.Request
import th.ac.kmitl.it.crowdassist.util.DatabaseHelper
import th.ac.kmitl.it.crowdassist.util.LocationHelper
import java.io.IOException
import java.util.concurrent.Callable

class CreateGeneralRequestPresenter(val databaseHelper: DatabaseHelper, val view : CreateGeneralRequestContract.View, val ioScheduler : Scheduler, val mainScheduler : Scheduler) : CreateGeneralRequestContract.Presenter{
    private val emptyEditText: ((Map.Entry<String, String>)) -> Boolean = { it.value.isEmpty() }

    override fun onSendButtonClicked(location : Location?, type : String?, uri : Uri?) {
        val textFill = view.getAllTextFill()
        if (!textFill.filter(emptyEditText).isEmpty()){
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
        data.description = textFill["description"]
        data.lat = location?.latitude
        data.lng = location?.longitude
        data.time = 1
        data.requesterType = requesterTypeString
        try {
            data.title = view.getLocationName(location)
        } catch (exception: IOException) {
            Log.e("LocationHelper", exception.message)
        }
        data.status = "wait"
        view.hideMainLayout()
        view.showProgreesLayout()
        view.showProgressBar()
        Observable.fromCallable(resolveCallable(location!!))
                .subscribeOn(ioScheduler)
                .observeOn(mainScheduler)
                .take(1)
                .subscribe(action1(data, uri!!))
    }

    override fun onSelectPhotoButtonClicked() {
        view.startCropImage()
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