package th.ac.kmitl.it.crowdassist

import android.app.Activity
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val button = findViewById<Button>(R.id.logoutButton)
        button.setOnClickListener({
            FirebaseAuth.getInstance().signOut()
            setResult(Activity.RESULT_OK)
            finish()
        })
    }
}
