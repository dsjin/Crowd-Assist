package th.ac.kmitl.it.crowdalert.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import th.ac.kmitl.it.crowdalert.model.Request;
import th.ac.kmitl.it.crowdalert.model.UserModel;
import th.ac.kmitl.it.crowdalert.model.VerifyModel;

public class DatabaseHelper {
    private final String SP_REQUEST = "request_information";
    private final String SP_PROFILE = "profile";
    private final String SP_LOCATION = "location_information";
    private FirebaseDatabase mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseFirestore mFireStore;
    private Context ctx;

    public DatabaseHelper(Context ctx) {
        this.ctx = ctx;
        init();
    }

    private void init(){
        this.mDatabase = FirebaseDatabase.getInstance();
        this.mAuth = FirebaseAuth.getInstance();
        this.mUser = mAuth.getCurrentUser();
        this.mFireStore = FirebaseFirestore.getInstance();
    }

    public void createRequest(Request data){
        DatabaseReference ref = mDatabase.getReference().child("emergency").push();
        DatabaseReference runner = mDatabase.getReference().child("emergency_requester_point").child(ref.getKey());
        DocumentReference fireStoreRef = mFireStore.collection("user").document(mUser.getUid()).collection("emergency").document(ref.getKey());

        ref.setValue(data);

        Map<String, Object> fireStore = new HashMap<>();
        fireStore.put("timestamp", Calendar.getInstance().getTimeInMillis());
        fireStoreRef.set(fireStore);

        Map<String, Object> runnerData = new HashMap<>();
        runnerData.put("lat", data.getLat());
        runnerData.put("lng", data.getLng());
        runner.setValue(runnerData);

        SharedPreferences sp = ctx.getSharedPreferences(SP_REQUEST, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("request_uid", ref.getKey());
        editor.putString("requester_uid", mUser.getUid());
        editor.putInt("time", data.getTime());
        editor.putString("type", "emergency");
        editor.putInt("assistance", 0);
        editor.putString("mode", "Helped");
        editor.apply();
    }

    public void createGeneralRequest(Request data, OnCompleteListener<Void> listener){
        DatabaseReference ref = mDatabase.getReference().child("non_emergency").push();
        DatabaseReference runner = mDatabase.getReference().child("emergency_requester_point").child(ref.getKey());
        DocumentReference fireStoreRef = mFireStore.collection("user").document(mUser.getUid()).collection("general").document(ref.getKey());
        //DatabaseReference refUser = mDatabase.getReference().child("users/"+mUser.getUid()+"/non-emergency/"+data.getType()+"/"+ref.getKey());
        data.setRequesterUid(mUser.getUid());

        Map<String, Object> fireStore = new HashMap<>();
        fireStore.put("timestamp", Calendar.getInstance().getTimeInMillis());
        fireStoreRef.set(fireStore);

        Map<String, Object> runnerData = new HashMap<>();
        runnerData.put("lat", data.getLat());
        runnerData.put("lng", data.getLng());
        runner.setValue(runnerData);

        ref.setValue(data).addOnCompleteListener(listener);
        SharedPreferences sp = ctx.getSharedPreferences(SP_REQUEST, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("request_uid", ref.getKey());
        editor.putString("requester_uid", mUser.getUid());
        editor.putInt("time", data.getTime());
        editor.putString("type", "non_emergency");
        editor.putInt("assistance", 0);
        editor.putString("mode", "Helped");
        editor.apply();
        //refUser.setValue(data);
    }

    public void updateEmergencyTime(String id, Integer time){
        DatabaseReference ref = mDatabase.getReference().child("emergency").child(id);
        Map<String, Object> childUpdate = new HashMap<>();
        childUpdate.put("time", time);
        ref.updateChildren(childUpdate);
        SharedPreferences sp = ctx.getSharedPreferences(SP_REQUEST, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("time", time);
        editor.apply();
    }

    public void acceptEmergencyRequest(String uid, UserModel userModel){
        DatabaseReference ref = mDatabase.getReference().child("emergency_assistance").child(uid).child("user").child(mUser.getUid());
        DocumentReference userRef = mFireStore.collection("user").document(mUser.getUid()).collection("assistance").document(uid);
        SharedPreferences spEmergency = ctx.getSharedPreferences(SP_REQUEST, Context.MODE_PRIVATE);
        SharedPreferences spProfile = ctx.getSharedPreferences(SP_PROFILE, Context.MODE_PRIVATE);
        SharedPreferences spLocation = ctx.getSharedPreferences(SP_LOCATION, Context.MODE_PRIVATE);
        String role = spProfile.getString("role", null);
        String currentLat = spLocation.getString("location_lat", null);
        String currentLon = spLocation.getString("location_lon", null);
        Map<String, Object> childCreate = new HashMap<>();
        childCreate.put("lat", Double.parseDouble(currentLat));
        childCreate.put("lng", Double.parseDouble(currentLon));
        childCreate.put("role", role);
        ref.setValue(childCreate);


        Map<String, Object> createAcceptItem = new HashMap<>();
        createAcceptItem.put("timestamp", Calendar.getInstance().getTimeInMillis());
        createAcceptItem.put("mode", "emergency");
        userRef.set(createAcceptItem);

        SharedPreferences.Editor editor = spEmergency.edit();
        editor.putString("request_uid", uid);
        editor.putString("mode","Help");
        editor.putString("type", "emergency");
        editor.putString("requester_uid", userModel.getUserUid());
        editor.putLong("requester_date_of_birth", userModel.getDateOfBirth());
        editor.putString("requester_gender", userModel.getGender());
        editor.apply();
    }

    public void acceptNonEmergencyRequest(String uid, UserModel userModel){
        DatabaseReference ref = mDatabase.getReference().child("non_emergency_assistance").child(uid).child("user").child(mUser.getUid());
        DocumentReference userRef = mFireStore.collection("user").document(mUser.getUid()).collection("assistance").document(uid);
        SharedPreferences spEmergency = ctx.getSharedPreferences(SP_REQUEST, Context.MODE_PRIVATE);
        SharedPreferences spProfile = ctx.getSharedPreferences(SP_PROFILE, Context.MODE_PRIVATE);
        SharedPreferences spLocation = ctx.getSharedPreferences(SP_LOCATION, Context.MODE_PRIVATE);
        String role = spProfile.getString("role", null);
        String currentLat = spLocation.getString("location_lat", null);
        String currentLon = spLocation.getString("location_lon", null);
        Map<String, Object> childCreate = new HashMap<>();
        childCreate.put("lat", Double.parseDouble(currentLat));
        childCreate.put("lng", Double.parseDouble(currentLon));
        childCreate.put("role", role);
        ref.setValue(childCreate);

        Map<String, Object> createAcceptItem = new HashMap<>();
        createAcceptItem.put("timestamp", Calendar.getInstance().getTimeInMillis());
        createAcceptItem.put("mode", "non๘emergency");
        userRef.set(createAcceptItem);

        SharedPreferences.Editor editor = spEmergency.edit();
        editor.putString("request_uid", uid);
        editor.putString("mode","Help");
        editor.putString("type", "non_emergency");
        editor.putString("requester_uid", userModel.getUserUid());
        editor.putLong("requester_date_of_birth", userModel.getDateOfBirth());
        editor.putString("requester_gender", userModel.getGender());
        editor.apply();
    }

    public void updateHelperLocation(String uid, Location location, String path){
        DatabaseReference ref = mDatabase.getReference().child(path+"_assistance/"+uid+"/user/"+mUser.getUid());
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/lng", location.getLongitude());
        childUpdates.put("/lat", location.getLatitude());
        ref.updateChildren(childUpdates);
    }

    public void updateRequesterLocation(String uid, Location location, String type){
        DatabaseReference ref = mDatabase.getReference().child(type+"_requester_point").child(uid);
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/lng", location.getLongitude());
        childUpdates.put("/lat", location.getLatitude());
        ref.updateChildren(childUpdates);
    }

    public void closeRequest(String uid, OnCompleteListener<Void> listener, String path){
        DatabaseReference ref = mDatabase.getReference().child(path).child(uid);
        Map<String , Object> childUpdate = new HashMap<>();
        childUpdate.put("/status", "close");
        ref.updateChildren(childUpdate).addOnCompleteListener(listener);
    }

    public void updateProfileFirstTime(String uid, String firstName, String lastName, Long dateOfBarth, String gender, String tel, OnCompleteListener<Void> listener){
        DatabaseReference ref = mDatabase.getReference().child("users").child(uid);
        Map<String , Object> childUpdate = new HashMap<>();
        childUpdate.put("/firstName", firstName);
        childUpdate.put("/lastName", lastName);
        childUpdate.put("/dateOfBirth", dateOfBarth);
        childUpdate.put("/gender", gender);
        childUpdate.put("/firstTime", false);
        childUpdate.put("/tel", tel);
        SharedPreferences spProfile = ctx.getSharedPreferences(SP_PROFILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = spProfile.edit();
        editor.putString("name", String.format("%s %s",firstName, lastName));
        editor.apply();
        ref.updateChildren(childUpdate).addOnCompleteListener(listener);
    }

    public void updatePin(String pin, OnCompleteListener<Void> listener){
        DatabaseReference ref = mDatabase.getReference().child("users").child(mUser.getUid());
        Map<String , Object> childUpdate = new HashMap<>();
        childUpdate.put("/pin", pin);
        ref.updateChildren(childUpdate).addOnCompleteListener(listener);
    }

    public void rate(String uid, String userUid, Double rating, String comment, OnCompleteListener<Void> listener){
        DocumentReference ref = mFireStore.collection("request").document(uid).collection(userUid).document(mUser.getUid());
        SharedPreferences spProfile = ctx.getSharedPreferences(SP_PROFILE, Context.MODE_PRIVATE);
        HashMap<String , Object> data = new HashMap<>();
        data.put("rating", rating);
        data.put("comment", comment);
        data.put("name", spProfile.getString("name", ""));
        data.put("timestamp",  Calendar.getInstance().getTimeInMillis());
        ref.set(data).addOnCompleteListener(listener);
    }

    public void createVerifyList(VerifyModel model, OnCompleteListener<Void> listener){
        DocumentReference ref = mFireStore.collection("verify").document(model.getUid());
        HashMap<String , Object> data = new HashMap<>();
        HashMap<String, Object> subData = new HashMap<>();
        data.put("firstName", model.getFirstName());
        data.put("lastName", model.getLastName());
        data.put("name", model.getName());
        data.put("role", model.getRole());
        data.put("idCard", model.getIdCard());
        data.put("gender", model.getGender());
        data.put("timestamp", Calendar.getInstance().getTimeInMillis());
        data.put("username", model.getUsername());
        data.put("dateOfBirth", model.getDateOfBirth());
        subData.put("idCard", model.getIdCardPath());
        subData.put("photoWithCard", model.getPhotoWithCardPath());
        data.put("referencePath", subData);
        data.put("verify", false);
        ref.set(data).addOnCompleteListener(listener);
    }
}
