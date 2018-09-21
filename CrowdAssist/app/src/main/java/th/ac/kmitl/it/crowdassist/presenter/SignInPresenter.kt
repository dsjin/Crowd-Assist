package th.ac.kmitl.it.crowdassist.presenter

import th.ac.kmitl.it.crowdassist.SignUpActivity
import th.ac.kmitl.it.crowdassist.contract.SignInContract
import th.ac.kmitl.it.crowdassist.util.DatabaseHelper

class SignInPresenter(val databaseHelper : DatabaseHelper, val view : SignInContract.View) : SignInContract.Presenter {
    override fun onSignInButtonClicked() {
        val textFill = view.getAllTextFill()
        view.showProgressBar()
        databaseHelper.signIn(textFill?.get("username")!!, textFill?.get("password")!!, view)
    }

    override fun onSignUpButtonClicked() {
        view.startActivity(SignUpActivity::class.java)
    }
}