package th.ac.kmitl.it.crowdassist.contract

import android.widget.EditText

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
        fun getAllEditText() : MutableMap<String, EditText>?
    }
}
