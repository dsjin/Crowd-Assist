package th.ac.kmitl.it.crowdassist

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import th.ac.kmitl.it.crowdassist.contract.SignInContract
import th.ac.kmitl.it.crowdassist.presenter.SignInPresenter
import th.ac.kmitl.it.crowdassist.util.PermissionHelper

class SignInActivity : AppCompatActivity(), SignInContract.View {
    private val permissionHelper = PermissionHelper(this)
    private val presenter = SignInPresenter(this, this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        val signInButton = findViewById<LinearLayout>(R.id.sign_in_button)
        val signUpButton = findViewById<LinearLayout>(R.id.sign_up_button)
        signInButton.setOnClickListener({
            presenter.onSignInButtonClicked()
        })
        signUpButton.setOnClickListener({
            presenter.onSignInButtonClicked()
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

    override fun finishActivity() {
        val intent = Intent()
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun getAllEditText(): MutableMap<String, EditText>? {
        val listOfEditText = hashMapOf<String, EditText>()
        listOfEditText.put("username", findViewById(R.id.username_input))
        listOfEditText.put("password", findViewById(R.id.password_input))
        return  listOfEditText
    }

    override fun showSnackBar(message: String, during: Int) {
        Snackbar.make(findViewById<ConstraintLayout>(R.id.mainLayout), message , during).show()
    }

    override fun onBackPressed() {
        AlertDialog.Builder(this)
                .setMessage("คุณต้องการออกจากแอพพลิเคชันใช่หรือไม่")
                .setCancelable(false)
                .setPositiveButton("ใช่", {
                    _ , _ -> this.finish()
                })
                .setNegativeButton("ไม่ใช่", null)
                .show()
    }

    override fun onStart() {
        super.onStart()
        permissionHelper.setPermission()
    }
}
