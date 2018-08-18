package th.ac.kmitl.it.crowdassist

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.NavigationView
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.transition.Fade
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.squareup.picasso.Picasso
import th.ac.kmitl.it.crowdassist.fragment.MainFragment
import th.ac.kmitl.it.crowdassist.fragment.ProfileFragment
import th.ac.kmitl.it.crowdassist.fragment.SettingFragment
import th.ac.kmitl.it.crowdassist.util.GoToCallback
import th.ac.kmitl.it.crowdassist.util.LogoutCallback
import th.ac.kmitl.it.crowdassist.util.SetButtonListenerCallback
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity(), GoToCallback , SetButtonListenerCallback.LongClickListener , SetButtonListenerCallback.ClickListener, LogoutCallback, SharedPreferences.OnSharedPreferenceChangeListener, NavigationView.OnNavigationItemSelectedListener {

    private var requestingFab : FloatingActionButton? = null
    private var toolbar : Toolbar? = null
    private var isHome : Boolean? = null
    private val FADE_DEFAULT_TIME: Long = 300
    private var navigationView : NavigationView? = null
    private var profileImage : ImageView? = null
    private var email : TextView? = null
    private var verifyLayout : LinearLayout? = null
    private var mAuth by Delegates.notNull<FirebaseAuth>()
    private var mUser : FirebaseUser? = null
    private var title : TextView? = null
    private var logo : ImageView? = null
    private val PROFILE_SP = "profile"
    private var profileSP: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mAuth = FirebaseAuth.getInstance()
        mUser = mAuth.currentUser
        profileSP = getSharedPreferences(PROFILE_SP, Context.MODE_PRIVATE)
        isHome = true

        requestingFab = findViewById(R.id.requestingButton)
        toolbar = findViewById(R.id.toolbar)
        setupToolbar()

        savedInstanceState ifNull {
            getFragment(MainFragment())
        }


    }

    override fun onStart() {
        super.onStart()
        profileSP?.registerOnSharedPreferenceChangeListener(this)
        if (profileSP!!.getBoolean("verify", false)) {
            verifyLayout?.setBackgroundColor(ContextCompat.getColor(this, R.color.verify))
        }
        Log.d("isHome", isHome.toString())
    }

    private fun setupToolbar(){
        toolbar?.title = ""
        setSupportActionBar(toolbar)
        title = toolbar?.findViewById(R.id.title_text)
        logo = toolbar?.findViewById(R.id.logo)
        val drawer : DrawerLayout = findViewById(R.id.drawer_layout)
        val toggle = object : ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            private var close: Boolean? = false

            override fun onDrawerClosed(v: View) {
                super.onDrawerClosed(v)
                close = true
                if (isHome!!) {
                    requestingFab!!.show()
                }
            }

            override fun onDrawerOpened(v: View) {
                super.onDrawerOpened(v)
                close = false
            }

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                super.onDrawerSlide(drawerView, slideOffset)
                requestingFab!!.hide()
            }

            override fun onDrawerStateChanged(newState: Int) {
                super.onDrawerStateChanged(newState)
                if (close!! && !requestingFab!!.isActivated && isHome!!) {
                    requestingFab!!.show()
                }
            }
        }
        drawer.addDrawerListener(toggle)
        toggle.syncState()
        navigationView = findViewById(R.id.nav_view)
        val header = navigationView?.getHeaderView(0)
        profileImage = header?.findViewById(R.id.imageView)
        email = header?.findViewById(R.id.email)
        email?.setText(mUser?.email)
        verifyLayout = header?.findViewById(R.id.verifyLayout)
        Picasso.with(this).load(mUser?.photoUrl).resize(500, 500).into(profileImage)

        navigationView?.setCheckedItem(R.id.nav_home)
        navigationView?.setNavigationItemSelectedListener(this)
    }

    override fun onStop() {
        super.onStop()
        profileSP?.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun goTo(fragmentName: String) {
        when (fragmentName) {
            "Setting" -> {
                requestingFab?.hide()
                isHome = false
                getFragment(SettingFragment())
                title?.setText("ตั้งค่า")
                title?.setVisibility(View.VISIBLE)
                logo?.setVisibility(View.GONE)
                navigationView?.setCheckedItem(R.id.nav_setting)
            }
        }
    }

    override fun setClick(listener: View.OnClickListener) {
        requestingFab?.setOnClickListener(listener)
    }

    override fun setLongClick(listener: View.OnLongClickListener) {
        requestingFab?.setOnLongClickListener(listener)
    }

    private infix fun Any?.ifNull(block: () -> Unit) {
        if (this == null) block()
    }

    private fun getFragment(mFragment: android.support.v4.app.Fragment) {
        val fragmentManager = supportFragmentManager
        val enterFade = Fade()
        enterFade.duration = FADE_DEFAULT_TIME
        mFragment.enterTransition = enterFade

        fragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, mFragment)
                .commit()
    }

    override fun logout(){
        mAuth.signOut()
        setResult(Activity.RESULT_OK)
        finish()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, s: String) {
        if ("verify" == s) {
            if (sharedPreferences.getBoolean("verify", false)) {
                verifyLayout?.setBackgroundColor(ContextCompat.getColor(this, R.color.verify))
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        when (id) {
            R.id.nav_home -> if (!isHome!!) {
                isHome = true
                getFragment(MainFragment())
                title?.text = getString(R.string.app_name)
                title?.visibility = (View.GONE)
                logo?.visibility = (View.VISIBLE)
            }
            R.id.nav_profile -> {
                requestingFab?.hide()
                isHome = false
                getFragment(ProfileFragment())
                title?.text = ("โปรไฟล์")
                title?.visibility = (View.VISIBLE)
                logo?.visibility = (View.GONE)
            }
            R.id.nav_setting -> {
                requestingFab?.hide()
                isHome = false
                getFragment(SettingFragment())
                title?.setText("ตั้งค่า")
                title?.setVisibility(View.VISIBLE)
                logo?.setVisibility(View.GONE)
            }
            R.id.nav_notification -> {
                /*
                requestingFab.hide()
                isHome = false
                getFragment(NotificationFragment())
                title.setText("การแจ้งเตือน")*/
            }
        }

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putBoolean("isHome", isHome!!)
        outState?.putCharSequence("title", title?.text)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        savedInstanceState?.let{
            isHome = savedInstanceState.getBoolean("isHome")
            isHome?.let{
                if (!isHome!!){
                    requestingFab?.hide()
                    logo?.visibility = View.GONE
                    title?.text = savedInstanceState.getCharSequence("title")
                    title?.visibility = View.VISIBLE
                }
            }
        }
        super.onRestoreInstanceState(savedInstanceState)
    }
}
