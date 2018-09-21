package th.ac.kmitl.it.crowdassist.contract

import android.app.Activity

interface SignInContract{
    interface Presenter{
        fun onSignInButtonClicked()
        fun onSignUpButtonClicked()
    }
    interface View{
        fun showProgressBar()
        fun hideProgressBar()
        fun finishActivity()
        fun showSnackBar(message : String, during : Int)
        fun getAllTextFill() : MutableMap<String, String>?
        fun startActivity(activity : Class<out Activity>)
    }
}
