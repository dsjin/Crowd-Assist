package th.ac.kmitl.it.crowdassist.viewmodel;

import android.app.Application;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.Marker;

public class UserInformationViewModelFactory implements ViewModelProvider.Factory {
    private Application mApplication;
    private String uid;

    public UserInformationViewModelFactory(Application application, String uid){
        this.mApplication = application;
        this.uid = uid;
    }
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new UserInformationViewModel(mApplication, uid);
    }
}
