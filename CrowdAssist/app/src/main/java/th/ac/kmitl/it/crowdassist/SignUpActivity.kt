package th.ac.kmitl.it.crowdassist

import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import th.ac.kmitl.it.crowdassist.component.TermsAndConditionsDialog
import th.ac.kmitl.it.crowdassist.contract.SignUpContract
import th.ac.kmitl.it.crowdassist.presenter.SignUpPresenter
import kotlin.properties.Delegates

class SignUpActivity : AppCompatActivity(), SignUpContract.View{
    var termsAndConditionDialog : TermsAndConditionsDialog by Delegates.notNull<TermsAndConditionsDialog>()
    var firstTime = true
    val presenter = SignUpPresenter(this, this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        termsAndConditionDialog = TermsAndConditionsDialog(this)
        val signUpButton = findViewById<LinearLayout>(R.id.sign_up_button)
        signUpButton.setOnClickListener({
            presenter.onSignUpClicked()
        })
    }

    override fun showProgressBar() {
        findViewById<ConstraintLayout>(R.id.mainLayout).visibility = View.GONE
        findViewById<LinearLayout>(R.id.progress_layout).visibility = View.VISIBLE
    }

    override fun hideProgressBar() {
        findViewById<ConstraintLayout>(R.id.mainLayout).visibility = View.VISIBLE
        findViewById<LinearLayout>(R.id.progress_layout).visibility = View.GONE
    }

    override fun showTermsAndConditions() {
        termsAndConditionDialog.show()
    }

    override fun getAllEditText(): MutableMap<String, EditText>? {
        val listOfEditText = hashMapOf<String, EditText>()
        listOfEditText.put("username", findViewById(R.id.username_input))
        listOfEditText.put("password", findViewById(R.id.password_input))
        listOfEditText.put("rePassword", findViewById(R.id.confirm_password_input))
        listOfEditText.put("firstName", findViewById(R.id.firstname_input))
        listOfEditText.put("lastName", findViewById(R.id.lastname_input))
        listOfEditText.put("email", findViewById(R.id.email_input))
        listOfEditText.put("idCard", findViewById(R.id.idcard_input))
        return listOfEditText
    }

    override fun showSnackBar(message: String, during: Int) {
        Snackbar.make(findViewById<ConstraintLayout>(R.id.mainLayout), message , during).show()
    }

    override fun onStart() {
        super.onStart()
        if (firstTime) {
            showTermsAndConditions()
            firstTime = false
        }
    }
}
