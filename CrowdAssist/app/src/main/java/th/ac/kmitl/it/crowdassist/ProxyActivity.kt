package th.ac.kmitl.it.crowdassist

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlin.properties.Delegates

class ProxyActivity : AppCompatActivity() {
    private val SIGN_IN = 9001
    private val MAIN = 9002
    private var mAuth by Delegates.notNull<FirebaseAuth>()
    private var mUser : FirebaseUser? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
    }

    override fun onStart() {
        super.onStart()
        setVisible(true)
        mUser = mAuth.currentUser
        mUser?.let{
            start()
        } ?:run {
            val intent = Intent(this, SignInActivity::class.java)
            startActivityForResult(intent, SIGN_IN)
        }
    }

    private fun start(){
        val intent = Intent(this, MainActivity::class.java)
        startActivityForResult(intent, MAIN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == 0) {
            finish()
        }
    }

    infix fun Any?.ifNull(block: () -> Unit) {
        if (this == null) block()
    }
}
