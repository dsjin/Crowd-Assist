package th.ac.kmitl.it.crowdassist.presenter

import th.ac.kmitl.it.crowdassist.contract.SignUpContract
import th.ac.kmitl.it.crowdassist.modal.UserSignUpModal
import th.ac.kmitl.it.crowdassist.util.DatabaseHelper

class SignUpPresenter(val databaseHelper : DatabaseHelper, val view : SignUpContract.View) : SignUpContract.Presentation{

    override fun onSignUpClicked() {
        view.showProgressBar()
        databaseHelper.signUp(getTextFillData(), view)
    }

    override fun getTextFillData(): UserSignUpModal {
        val textFills = view.getAllTextFill()
        return UserSignUpModal(textFills?.get("username")!!, textFills?.get("email")!!, textFills?.get("firstName")!!, textFills?.get("lastName")!!, textFills?.get("idCard")!!, "user", textFills?.get("password")!!, textFills?.get("rePassword")!!)
    }
}