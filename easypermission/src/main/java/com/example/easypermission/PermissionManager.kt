package com.example.easypermission

import android.Manifest.permission.READ_MEDIA_AUDIO
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_MEDIA_VIDEO
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionManager constructor(
    private val activity: ComponentActivity
) {


    private var permissionExplanationTitle = "Access Required"
    private var permissionExplanation =
        "permission is essential, Please enable it from settings by click allow button."
    private var permissionExplanationSetting =
        "permission is essential, but you’ve denied it multiple times. Please enable it from settings by click allow button."


    fun isPermissionsGranted(permissionName: String): Boolean {
        return (activity.checkSelfPermission(permissionName) == PackageManager.PERMISSION_GRANTED)
    }

    fun isReadMediaPermissionGranted(permissionName: String): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                activity, permissionName
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                activity, android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun isAllFilesPermissionAllowed(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            true
        }
    }

    fun openPermissionSettings(
        onRationalPermissionCallBack: () -> Unit,
    ) {


        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri: Uri = Uri.fromParts("package", activity.packageName, null)
        intent.data = uri
//        activity.startActivity(intent)
        permissionRationalCallback = onRationalPermissionCallBack
        permissionRationalLauncher.launch(intent)


    }


    /**
     * This method adds two numbers
     * @param permission the permission that we need to ask runtime
     * @param permissionName name of permission to inject in description on the dialog
     * @param onPermissionResult result of particular permission whether it has been denied or granted already
     * @param showExplanationDialog dialog before asking the permission to explain to the USER
     * @author Arshad Iqbal
     */
    fun requestPermission(

        permission: String,
        permissionName: String,
        onPermissionResult: (Boolean) -> Unit,
        showExplanationDialog: Boolean,
        onRationalPermissionResultCallback: (Boolean) -> Unit,

        dialogPermissionExplanationTitle: String = permissionExplanationTitle,
        dialogPermissionExplanationDescription: String = "$permissionName $permissionExplanation",

        dialogTitleRational: String = permissionExplanationTitle,
        dialogDescriptionRational: String = "$permissionName $permissionExplanationSetting",

        onDeniedButtonOfExplanationDialog: (() -> Unit)? = null,

        ) {

        var mPermission = ""
        if (permission == READ_MEDIA_IMAGES || permission == READ_MEDIA_VIDEO || permission == READ_MEDIA_AUDIO) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                mPermission = permission
            } else {
                mPermission = WRITE_EXTERNAL_STORAGE
            }

        } else {
            mPermission = permission
        }


        LogE("requested permission is:$mPermission")

        if (permission == READ_MEDIA_IMAGES || permission == READ_MEDIA_VIDEO || permission == READ_MEDIA_AUDIO) {
            if (isReadMediaPermissionGranted(mPermission)) {
                onPermissionResult.invoke(true)
                return
            }
        } else {
            if (isPermissionsGranted(mPermission)) {
                onPermissionResult.invoke(true)
                return
            }
        }


        if (showExplanationDialog) {

            showPermissionExplanationDialog(
                dialogPermissionExplanationTitle,
                dialogPermissionExplanationDescription,
                {
                    //ask permission and proceed
                    LogE("ask permission, dialog positive button clicked")

                    startRequestingPermission(
                        permission
                    ) { isGranted, isPermissionRational ->

                        if (!isPermissionRational && !isReadMediaPermissionGranted(permission)) {
                            LogE("isPermissionRational:$isPermissionRational")
                            showRationalPermissionDialog(dialogTitleRational,
                                dialogDescriptionRational,
                                {
                                    LogE("go to setting")

                                    openPermissionSettings({

                                        if (isReadMediaPermissionGranted(permission)) {
                                            LogE("$permission is granted")
                                            onRationalPermissionResultCallback.invoke(true)
                                        } else {
                                            LogE("$permission is not granted")
                                            onRationalPermissionResultCallback.invoke(false)
                                        }
                                    })
                                },
                                {
                                    LogE("Cancel rational")
                                })
                        } else {
                            LogE("Normal flow of permissions")
                            onPermissionResult.invoke(isGranted)
                        }
                    }
                },
                {
                    //don't ask permission negative button clicked
                    LogE("permission dialog negative button clicked")
                    onDeniedButtonOfExplanationDialog?.invoke()
                })

        } else {

            LogE("ask permission, dialog positive button clicked")

            startRequestingPermission(
                permission
            ) { isGranted, isPermissionRational ->

                if (!isPermissionRational && !isReadMediaPermissionGranted(permission)) {
                    LogE("isPermissionRational:$isPermissionRational")
                    showRationalPermissionDialog(dialogTitleRational,
                        dialogDescriptionRational,
                        {
                            LogE("go to setting")

                            openPermissionSettings({

                                if (isReadMediaPermissionGranted(permission)) {
                                    LogE("$permission is granted")
                                    onRationalPermissionResultCallback.invoke(true)
                                } else {
                                    LogE("$permission is not granted")
                                    onRationalPermissionResultCallback.invoke(false)
                                }
                            })
                        },
                        {
                            LogE("Cancel rational")
                        })
                } else {
                    LogE("Normal flow of permissions")
                    onPermissionResult.invoke(isGranted)
                }
            }

        }

    }

    fun startRequestingPermission(
        permission: String,
        onPermissionResult: (Boolean, Boolean) -> Unit
    ) {

        var mPermission = ""

        /*
        checking whether the permission belongs to media, because starting from android 13 (API 33)
        we need to ask these separate media permissions for Video, image or audio. However for lower
        than android 13, normal read external or write eternal storage permission required
        */
        if (permission == READ_MEDIA_IMAGES || permission == READ_MEDIA_VIDEO || permission == READ_MEDIA_AUDIO) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                mPermission = permission
            } else {
                mPermission = WRITE_EXTERNAL_STORAGE
            }

        } else {
            mPermission = permission
        }


        LogE("requested permission is:$mPermission")

        requestPermissions(arrayOf(mPermission), { isGranted, isPermissionRational ->
            onPermissionResult.invoke(isGranted, isPermissionRational)
        })

    }

    fun requestManageAllFilePermission(requestCode: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val uri = Uri.parse("package:${activity.packageName}")
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri)
                activity.startActivityForResult(intent, requestCode)
            }
        }
    }

    fun showPermissionExplanationDialog(
        title: String,
        description: String,
        positiveBtnClick: () -> Unit,
        negativeBtnClick: () -> Unit

    ): AlertDialog {
        val dialogView = LayoutInflater.from(activity).inflate(R.layout.permission_dialog, null)
        val btnAllow: Button = dialogView.findViewById(R.id.btnAllow)

        val tvTitleDialog: TextView = dialogView.findViewById(R.id.tvTitleDialog)
        val tvDesDialog: TextView = dialogView.findViewById(R.id.tvDesDialog)

        tvTitleDialog.text = title
        tvDesDialog.text = description

        val btnClose: ImageView = dialogView.findViewById(R.id.btnClose)
        val builder = AlertDialog.Builder(activity)
        builder.setView(dialogView)

        val alertDialog = builder.create()
        alertDialog.setCancelable(false)

        alertDialog.setOnKeyListener { dialog, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                dialog.dismiss()
                negativeBtnClick.invoke()
                true
            } else {
                false
            }
        }

        btnAllow.setOnClickListener {
            alertDialog.dismiss()
            positiveBtnClick.invoke()
        }

        btnClose.setOnClickListener {
            alertDialog.dismiss()
            negativeBtnClick.invoke()
        }

        alertDialog.show()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )

        return alertDialog
    }


    fun showRationalPermissionDialog(
        title: String,
        description: String,
        positiveBtnClick: () -> Unit,
        negativeBtnClick: () -> Unit

    ): AlertDialog {
        val dialogView = LayoutInflater.from(activity).inflate(R.layout.permission_dialog, null)
        val btnAllow: Button = dialogView.findViewById(R.id.btnAllow)

        val tvTitleDialog: TextView = dialogView.findViewById(R.id.tvTitleDialog)
        val tvDesDialog: TextView = dialogView.findViewById(R.id.tvDesDialog)

        tvTitleDialog.text = title
        tvDesDialog.text = description

        val btnClose: ImageView = dialogView.findViewById(R.id.btnClose)
        val builder = AlertDialog.Builder(activity)
        builder.setView(dialogView)

        val alertDialog = builder.create()
        alertDialog.setCancelable(false)

        alertDialog.setOnKeyListener { dialog, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                dialog.dismiss()
                negativeBtnClick.invoke()
                true
            } else {
                false
            }
        }

        btnAllow.setOnClickListener {
            alertDialog.dismiss()
            positiveBtnClick.invoke()
        }

        btnClose.setOnClickListener {
            alertDialog.dismiss()
            negativeBtnClick.invoke()
        }

        alertDialog.show()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )

        return alertDialog
    }


    fun requestPermissions(
        permissions: Array<String>,
        callback: (Boolean, Boolean) -> Unit
    ) {
        permissionCallback = callback
        permissionLauncher.launch(permissions) // Launch the permission request
    }

    var permissionCallback: ((Boolean, Boolean) -> Unit)? = null
    private val permissionLauncher =
        activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val isGranted = permissions.all { it.value } // Check if all permissions are granted
            LogE("is permission granted: $isGranted")

            val shouldShowRationale = permissions.keys.any { permission ->
                LogE("permission: $permission")
                ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    permission
                )
            }
            LogE("shouldShowRationale: $shouldShowRationale")

            permissionCallback?.invoke(isGranted, shouldShowRationale)
        }


    var permissionRationalCallback: (() -> Unit)? = null
    private val permissionRationalLauncher =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == RESULT_OK) {
                LogE("result.resultCode OKay: ${result.resultCode}")

            } else {
                LogE("result.resultCode Cancel: ${result.resultCode}")
                //permissionRationalCallback?.invoke(false)
            }

            if (isReadMediaPermissionGranted(READ_MEDIA_IMAGES)) {

            }
            permissionRationalCallback?.invoke()
        }
}