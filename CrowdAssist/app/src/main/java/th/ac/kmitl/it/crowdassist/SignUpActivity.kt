package th.ac.kmitl.it.crowdassist

import android.app.Activity
import android.content.Intent
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
import th.ac.kmitl.it.crowdassist.util.DatabaseHelper
import kotlin.properties.Delegates

class SignUpActivity : AppCompatActivity(), SignUpContract.View{
    var termsAndConditionDialog : TermsAndConditionsDialog by Delegates.notNull<TermsAndConditionsDialog>()
    var firstTime = true
    val presenter = SignUpPresenter(DatabaseHelper(this), this)
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

    override fun getAllTextFill(): MutableMap<String, String>? {
        val textFills = hashMapOf<String, String>()
        textFills.put("username", (findViewById<EditText>(R.id.username_input)).text.toString())
        textFills.put("password", (findViewById<EditText>(R.id.password_input)).text.toString())
        textFills.put("rePassword", (findViewById<EditText>(R.id.confirm_password_input)).text.toString())
        textFills.put("firstName", (findViewById<EditText>(R.id.firstname_input)).text.toString())
        textFills.put("lastName", (findViewById<EditText>(R.id.lastname_input)).text.toString())
        textFills.put("email", (findViewById<EditText>(R.id.email_input)).text.toString())
        textFills.put("idCard", (findViewById<EditText>(R.id.idcard_input)).text.toString())
        return textFills
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

    override fun startActivity(activity : Class<out Activity>){
        val intent = Intent(this, activity)
        startActivity(intent)
    }
}
