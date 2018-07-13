package th.ac.kmitl.it.crowdassist.presenter

import android.content.Context
import th.ac.kmitl.it.crowdassist.contract.SignUpContract
import th.ac.kmitl.it.crowdassist.modal.UserSignUpModal
import th.ac.kmitl.it.crowdassist.util.DatabaseHelper

class SignUpPresenter(val ctx : Context, val view : SignUpContract.View) : SignUpContract.Presentation{

    private val databaseHelper = DatabaseHelper(ctx)

    override fun onSignUpClicked() {
        view.showProgressBar()
        databaseHelper.signUp(getEditTextData(), view)
    }

    override fun getEditTextData(): UserSignUpModal {
        val editText = view.getAllEditText()
        return UserSignUpModal(editText?.get("username")?.text.toString(), editText?.get("email")?.text.toString(), editText?.get("firstName")?.text.toString(), editText?.get("lastName")?.text.toString(), editText?.get("idCard")?.text.toString(), "user", editText?.get("password").toString(), editText?.get("rePassword").toString())
    }
}