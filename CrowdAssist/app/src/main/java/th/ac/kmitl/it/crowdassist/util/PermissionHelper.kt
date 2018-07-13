package th.ac.kmitl.it.crowdassist.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog

class PermissionHelper(val ctx : Context){
    val REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124
    fun setPermission():Boolean{
        val permissionList = mutableListOf<String>()
        val permissionNeeded = mutableListOf<String>()
        if (!addPermission(permissionList, Manifest.permission.ACCESS_FINE_LOCATION)) {
            permissionNeeded.add("GPS")
        }
        if (!addPermission(permissionList, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            permissionNeeded.add("อ่านข้อมูลภายในอุปกรณ์")
        }
        if (!addPermission(permissionList, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            permissionNeeded.add("เขียนข้อมูลภายในอุปกรณ์")
        }
        if (!addPermission(permissionList, Manifest.permission.CALL_PHONE)) {
            permissionNeeded.add("การโทรผ่านแอป")
        }
        if (permissionList.size > 0) {
            if (permissionNeeded.size > 0) {
                var message = "ขออนุญาติการเข้าถึง " + permissionNeeded[0]
                for (i in 1 until permissionNeeded.size)
                    message = message + ", " + permissionNeeded[i]
                showMessageOKCancel(message,
                        DialogInterface.OnClickListener { _ , _ ->
                            ActivityCompat.requestPermissions(ctx as Activity, permissionList.toTypedArray(),
                                    REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS)
                        })
                return false
            }
            ActivityCompat.requestPermissions(ctx as Activity, permissionList.toTypedArray(),
                    REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS)
            return false
        }
        return true
    }

    fun addPermission(permissionsList : MutableList<String>, permission : String) : Boolean{
        if (ContextCompat.checkSelfPermission(ctx, permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission)
            if (!ActivityCompat.shouldShowRequestPermissionRationale(ctx as Activity, permission))
                return false
        }
        return true
    }

    fun showMessageOKCancel(message: String, listener: DialogInterface.OnClickListener){
        AlertDialog.Builder(ctx)
                .setMessage(message)
                .setPositiveButton("OK", listener)
                .setNegativeButton("Cancel", null)
                .create()
                .show()
    }
}
