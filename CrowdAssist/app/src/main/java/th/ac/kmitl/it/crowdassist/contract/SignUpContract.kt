package th.ac.kmitl.it.crowdassist.contract

import android.widget.EditText
import th.ac.kmitl.it.crowdassist.modal.UserSignUpModal

interface SignUpContract{
    interface Presentation{
        fun onSignUpClicked()
        fun getEditTextData() : UserSignUpModal
    }
    interface View{
        fun showProgressBar()
        fun hideProgressBar()
        fun showSnackBar(message : String, during : Int)
        fun showTermsAndConditions()
        fun getAllEditText() : MutableMap<String, EditText>?
        fun finish()
    }
}