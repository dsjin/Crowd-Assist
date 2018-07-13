package th.ac.kmitl.it.crowdassist.presenter

import android.content.Context
import android.content.Intent
import th.ac.kmitl.it.crowdassist.SignUpActivity
import th.ac.kmitl.it.crowdassist.contract.SignInContract
import th.ac.kmitl.it.crowdassist.util.DatabaseHelper

class SignInPresenter(val ctx : Context, val view : SignInContract.View) : SignInContract.Presenter {
    private val databaseHelper = DatabaseHelper(ctx)
    override fun onSignInButtonClicked() {
        val editText = view.getAllEditText()
        view.showProgressBar()
        databaseHelper.signIn(editText?.get("userName")?.text.toString(), editText?.get("password")?.text.toString(), view)
    }

    override fun onSignUpButtonClick() {
        val intent = Intent(ctx, SignUpActivity::class.java)
        ctx.startActivity(intent)
    }
}