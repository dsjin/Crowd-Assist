package th.ac.kmitl.it.crowdalert.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

import java.util.ArrayList;
import java.util.List;

public class PermissionHepler {
    final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;
    private Context ctx;

    public PermissionHepler(Context ctx) {
        this.ctx = ctx;
    }

    public Boolean setPermission(){
        final ArrayList<String> permissionList = new ArrayList<>();
        ArrayList<String> permissionNeeded = new ArrayList<>();
        if (!addPermission(permissionList, Manifest.permission.ACCESS_FINE_LOCATION)){
            permissionNeeded.add("GPS");
        }
        if (!addPermission(permissionList, Manifest.permission.READ_EXTERNAL_STORAGE)){
            permissionNeeded.add("อ่านข้อมูลภายในอุปกรณ์");
        }
        if (!addPermission(permissionList, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            permissionNeeded.add("เขียนข้อมูลภายในอุปกรณ์");
        }
        if (!addPermission(permissionList, Manifest.permission.CALL_PHONE)){
            permissionNeeded.add("การโทรผ่านแอป");
        }
        if (permissionList.size() > 0){
            if (permissionNeeded.size() > 0){
                String message = "ขออนุญาติการเข้าถึง " + permissionNeeded.get(0);
                for (int i = 1; i < permissionNeeded.size(); i++)
                    message = message + ", " + permissionNeeded.get(i);
                showMessageOKCancel(message,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions((Activity)ctx, permissionList.toArray(new String[permissionList.size()]),
                                        REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
                            }
                        });
                return false;
            }
            ActivityCompat.requestPermissions((Activity)ctx, permissionList.toArray(new String[permissionList.size()]),
                    REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    private boolean addPermission(List<String> permissionsList, String permission) {
        if (ContextCompat.checkSelfPermission(ctx, permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            if (!ActivityCompat.shouldShowRequestPermissionRationale((Activity)ctx, permission))
                return false;
        }
        return true;
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(ctx)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }
}
