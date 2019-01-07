package th.ac.kmitl.it.crowdalert.fragment;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import th.ac.kmitl.it.crowdalert.EditProfileActivity;
import th.ac.kmitl.it.crowdalert.R;
import th.ac.kmitl.it.crowdalert.VerifyActivity;
import th.ac.kmitl.it.crowdalert.component.DeveloperInformationDialog;
import th.ac.kmitl.it.crowdalert.util.LogoutCallback;

public class SettingFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener{
    private Preference logout;
    private Preference verify;
    private Preference termsAndCondition;
    private Preference editProfile;
    private Preference developer;
    private LogoutCallback callback;

    public SettingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            if (context instanceof Activity){
                callback = (LogoutCallback) context;
            }
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement Callback");
        }
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.fragment_setting);
        logout = findPreference("logout");
        logout.setOnPreferenceClickListener(this);
        verify = findPreference("verify_user_preference");
        verify.setOnPreferenceClickListener(this);
        termsAndCondition = findPreference("terms_and_condition");
        termsAndCondition.setOnPreferenceClickListener(this);
        editProfile = findPreference("edit_profile");
        editProfile.setOnPreferenceClickListener(this);
        developer = findPreference("ีdeveloper_information");
        developer.setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        switch (preference.getKey()){
            case "verify_user_preference":
                Intent intent = new Intent(getActivity(), VerifyActivity.class);
                getActivity().startActivity(intent);
                return true;
            case "logout":
                callback.logout();
                return true;
            case "terms_and_condition":
                //TermsAndConditionsSettingDialog dialog = new TermsAndConditionsSettingDialog(getActivity());
                //dialog.show();
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW);
                websiteIntent.setData(Uri.parse("http://www.zp10656.tld.122.155.18.100.no-domain.name/crowdassist/termsandconditions/"));
                getActivity().startActivity(websiteIntent);
                return true;
            case "edit_profile":
                Intent editProfileIntent = new Intent(getActivity(), EditProfileActivity.class);
                getActivity().startActivity(editProfileIntent);
                return true;
            case "ีdeveloper_information":
                DeveloperInformationDialog developerInformationDialog = new DeveloperInformationDialog(getActivity());
                developerInformationDialog.show();
                return true;
        }
        return false;
    }
}
