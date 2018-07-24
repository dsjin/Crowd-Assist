package th.ac.kmitl.it.crowdassist.fragment


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat
import th.ac.kmitl.it.crowdassist.R
import th.ac.kmitl.it.crowdassist.util.LogoutCallback

class SettingFragment : PreferenceFragmentCompat(), Preference.OnPreferenceClickListener {
    private var logout: Preference? = null
    private var verify: Preference? = null
    private var termsAndCondition: Preference? = null
    private var editProfile: Preference? = null
    private var developer: Preference? = null
    private var callback: LogoutCallback? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.fragment_setting)
        logout = findPreference("logout")
        logout?.setOnPreferenceClickListener(this)
        verify = findPreference("verify_user_preference")
        verify?.setOnPreferenceClickListener(this)
        termsAndCondition = findPreference("terms_and_condition")
        termsAndCondition?.setOnPreferenceClickListener(this)
        editProfile = findPreference("edit_profile")
        editProfile?.setOnPreferenceClickListener(this)
        developer = findPreference("ีdeveloper_information")
        developer?.setOnPreferenceClickListener(this)
    }

    override fun onPreferenceClick(preference: Preference?): Boolean {
        when (preference?.getKey()) {
            "verify_user_preference" -> {
                //val intent = Intent(activity, VerifyActivity::class.java)
                //activity!!.startActivity(intent)
                return true
            }
            "logout" -> {
                callback?.logout()
                return true
            }
            "terms_and_condition" -> {
                //TermsAndConditionsSettingDialog dialog = new TermsAndConditionsSettingDialog(getActivity());
                //dialog.show();
                val websiteIntent = Intent(Intent.ACTION_VIEW)
                websiteIntent.data = Uri.parse("http://www.zp10656.tld.122.155.18.100.no-domain.name/crowdassist/termsandconditions/")
                activity!!.startActivity(websiteIntent)
                return true
            }
            "edit_profile" -> {
                //val editProfileIntent = Intent(activity, EditProfileActivity::class.java)
                //activity!!.startActivity(editProfileIntent)
                return true
            }
            "ีdeveloper_information" -> {
                //val developerInformationDialog = DeveloperInformationDialog(activity)
                //developerInformationDialog.show()
                return true
            }
        }
        return false
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        try {
            if (context is Activity) {
                callback = context as LogoutCallback?
            }
        } catch (e: ClassCastException) {
            throw ClassCastException(context!!.toString() + " must implement Callback")
        }

    }

    override fun onDetach() {
        super.onDetach()
        callback = null
    }
}
