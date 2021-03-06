package th.ac.kmitl.it.crowdassist.contract


import android.location.Location
import android.net.Uri
import android.widget.RadioGroup

interface CreateGeneralRequestContract{
    interface View{
        fun showCircularProgressBar()
        fun hideCircularProgressBar()
        fun showProgressBar()
        fun hideProgressBar()
        fun setPrograssBarValue(percent : Int)
        fun showProgreesLayout()
        fun hideProgressLayout()
        fun showMainLayout()
        fun hideMainLayout()
        fun finishActivity()
        fun getAllTextFill() : MutableMap<String, String>
        fun getRequesterTypeRadioGroup() : RadioGroup
        fun showSnackBar(message : String, during : Int)
        fun getLocationName(location : Location?) : String
        fun startCropImage()
    }

    interface Presenter{
        fun onSendButtonClicked(location : Location?, type : String?, uri : Uri?)
        fun onSelectPhotoButtonClicked()
    }
}