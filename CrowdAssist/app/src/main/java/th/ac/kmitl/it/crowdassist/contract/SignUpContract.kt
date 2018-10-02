package th.ac.kmitl.it.crowdassist.contract

import android.app.Activity
import th.ac.kmitl.it.crowdassist.model.UserSignUpModel

interface SignUpContract{
    interface Presentation{
        fun onSignUpClicked()
        fun getTextFillData() : UserSignUpModel
    }
    interface View{
        fun showProgressBar()
        fun hideProgressBar()
        fun showSnackBar(message : String, during : Int)
        fun showTermsAndConditions()
        fun getAllTextFill() : MutableMap<String, String>?
        fun finish()
        fun startActivity(activity : Class<out Activity>)
    }
}