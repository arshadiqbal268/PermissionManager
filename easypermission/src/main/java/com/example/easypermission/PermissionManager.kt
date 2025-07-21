package com.example.easypermission

import android.Manifest.permission.POST_NOTIFICATIONS
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_AUDIO
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_MEDIA_VIDEO
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionManager constructor(
    private val activity: AppCompatActivity,
    private val mInterface: AllFileResultInterface? = null
) {
    //  (private val mInterface: AllFileResultInterface? = null)
    // for specifically all file permission, this could be called in case if all file process end functionality needs to be handled


    var allFileResultInterface: AllFileResultInterface? = mInterface
    private var permissionExplanationTitle = "Access Required"
    private var permissionExplanation =
        "permission is essential to show the Media, Please enable it by clicking allow button."
    private var permissionExplanationSetting =
        "permission is essential, but you’ve denied it multiple times. Please enable it from settings by clicking allow button."

    private var permissionExplanationManageAllFile =
        "Manage All file permission is essential to Show the Documents, Please enable it by clicking allow button."

    private var dialogBgColor = "#ffffff"
    private var dialogTitleTextColor = "#000000"
    private var dialogDescriptionTextColor = "#000000"
    private var dialogButtonBgColor = "#003FFB"
    private var dialogButtonTextColor = "#ffffff"
    private var dialogCancelButtonColor = "#999999"

    private var permissionViewModel: PermissionViewModel? = null

    fun isPermissionsGranted(permission: String): Boolean {

        if (permission == READ_MEDIA_IMAGES || permission == READ_MEDIA_VIDEO || permission == READ_MEDIA_AUDIO) {
            return isReadMediaPermissionGranted(permission)
        } else {
            return (activity.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED)
        }
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
            false
        }
    }

    /**
     * in case of greater than Android R (API level 30) check all file permission and on lower than check simple storage permission
     * @author Arshad Iqbal
     */
    fun isStoragePermissionAllowed(): Boolean {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            return isAllFilesPermissionAllowed()

        } else {

            if (isPermissionsGranted(WRITE_EXTERNAL_STORAGE) ||
                isPermissionsGranted(READ_EXTERNAL_STORAGE)
            ) {
                return true
            } else
                return false
        }
    }

    private fun openPermissionSettings(
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
     * @param permission the permission that we need to ask runtime
     * @param permissionName name of permission to inject in description on the dialog
     * @param onPermissionResult result of particular permission whether it has been denied or granted already
     * @param showExplanationDialog dialog before asking the permission to explain to the USER
     * @param showSettingDialog setting dialog in case if permission denied twice and setting dialog show true/false
     * @author Arshad Iqbal
     */
    fun checkPermissionAndRequestIfNeeded(

        permission: String,
        permissionName: String,
        onPermissionResult: (Boolean) -> Unit,
        showExplanationDialog: Boolean,
        onRationalPermissionResultCallback: (Boolean) -> Unit,

        dialogPermissionExplanationTitle: String = permissionExplanationTitle,
        dialogPermissionExplanationDescription: String = "$permissionName $permissionExplanation",

        explanationDialogBgColor: String = "#ffffff",
        explanationDialogPositiveButtonBgColor: String = "#003FFB",
        explanationDialogPositiveButtonTxtColor: String = "#ffffff",

        dialogTitleRational: String = permissionExplanationTitle,
        dialogDescriptionRational: String = "$permissionName $permissionExplanationSetting",

        onDeniedButtonOfExplanationDialog: (() -> Unit)? = null,
        showSettingDialog: Boolean = true

    ) {

        this.dialogBgColor = explanationDialogBgColor
        this.dialogButtonBgColor = explanationDialogPositiveButtonBgColor
        this.dialogButtonTextColor = explanationDialogPositiveButtonTxtColor

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
            if (isAllFilesPermissionAllowed()) {
                onPermissionResult.invoke(true)
                return
            }

            if (isReadMediaPermissionGranted(mPermission)) {
                onPermissionResult.invoke(true)
                return
            }
        } else {
            if (permission == WRITE_EXTERNAL_STORAGE || permission == READ_EXTERNAL_STORAGE) {
                if (isAllFilesPermissionAllowed()) {
                    onPermissionResult.invoke(true)
                    return
                }
            }

            if (permission == POST_NOTIFICATIONS) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                    onPermissionResult.invoke(true)
                    return
                }
            }

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
                    LogE("ask permission, dialog positive button clicked for permission:$permission")

                    startRequestingPermissionFlow(
                        permission, dialogTitleRational,
                        dialogDescriptionRational, onPermissionResult,
                        onRationalPermissionResultCallback, onDeniedButtonOfExplanationDialog,
                        showSettingDialog
                    )
                },
                {
                    //don't ask permission negative button clicked
                    LogE("permission dialog negative button clicked")
                    onDeniedButtonOfExplanationDialog?.invoke()
                })

        } else {

            LogE("ask permission, dialog positive button clicked")

            startRequestingPermissionFlow(
                permission, dialogTitleRational,
                dialogDescriptionRational, onPermissionResult,
                onRationalPermissionResultCallback, onDeniedButtonOfExplanationDialog,
                showSettingDialog
            )

        }

    }

    private fun startRequestingPermissionFlow(
        permission: String,
        dialogTitleRational: String,
        dialogDescriptionRational: String,
        onPermissionResult: (Boolean) -> Unit,
        onRationalPermissionResultCallback: (Boolean) -> Unit,
        onDeniedButtonOfExplanationDialog: (() -> Unit)?,
        showSettingDialog: Boolean
    ) {
        startRequestingPermission(
            permission
        ) { isGranted, isPermissionRational ->

            if (!isPermissionRational && !isPermissionsGranted(permission)) {
                LogE("isPermissionRational:$isPermissionRational")
                if (showSettingDialog) {
                    showRationalPermissionDialog(dialogTitleRational,
                        dialogDescriptionRational,
                        {
                            LogE("go to setting")

                            openPermissionSettings({

                                if (isPermissionsGranted(permission)) {
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
                            onDeniedButtonOfExplanationDialog?.invoke()
                        })
                } else {
//                    LogE("no rational dialog is going to show")
//                    onDeniedButtonOfExplanationDialog?.invoke()
                }
            } else {
                LogE("Normal flow of permissions")
                onPermissionResult.invoke(isGranted)
            }
        }
    }

    private fun startRequestingPermission(
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

    fun checkManageAllFilePermissionAndRequestIfNeeded(
        onPermissionResult: (Boolean) -> Unit,
        showExplanationDialog: Boolean,
        dialogPermissionExplanationTitle: String = permissionExplanationTitle,
        dialogPermissionExplanationDescription: String = "$permissionExplanationManageAllFile",

        onDeniedButtonOfExplanationDialog: (() -> Unit)? = null,

        ) {

        //in case of android 11 or above starting all file access permission code
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            if (isAllFilesPermissionAllowed()) {
                onPermissionResult.invoke(true)
                return
            }

            if (showExplanationDialog) {

                showPermissionExplanationDialog(
                    dialogPermissionExplanationTitle,
                    dialogPermissionExplanationDescription,
                    positiveBtnClick = {
                        //ask permission and proceed
                        LogE("ask permission, dialog positive button clicked")

                        startRequestingAllFilePermissionFlow(onPermissionResult)

                    },
                    negativeBtnClick = {
                        //don't ask permission negative button clicked
                        LogE("permission dialog negative button clicked")
                        onDeniedButtonOfExplanationDialog?.invoke()
                    })


            } else {
                LogE("else permission asking without explanation dialog")
                startRequestingAllFilePermissionFlow(onPermissionResult)
            }

        } else {

            //in case of less than android 11, starting storage permission code
            if (showExplanationDialog) {

            }

            checkPermissionAndRequestIfNeeded(WRITE_EXTERNAL_STORAGE, "Storage",
                { isPermissionAllowed ->
                    LogE("is permission granted:$isPermissionAllowed")
                    if (isPermissionAllowed) {
                        onPermissionResult.invoke(true)
                    } else {
                        LogE("permission not granted:$isPermissionAllowed")
                        onPermissionResult.invoke(false)
                    }

                }, true,
                { isPermissionAllowed ->
                    // in rational permission case
                    LogE("is permission granted:$isPermissionAllowed")
                    if (isPermissionAllowed) {
                        onPermissionResult.invoke(true)
                    } else {
                        LogE("permission not granted:$isPermissionAllowed")
                        onPermissionResult.invoke(false)
                    }
                },
                onDeniedButtonOfExplanationDialog = {
                    onDeniedButtonOfExplanationDialog?.invoke()
                }

            )

        }


    }

    private fun startRequestingAllFilePermissionFlow(onPermissionResult: (Boolean) -> Unit) {
        requestManageAllFilePermission(

            {
                LogE("Manage all file permission callBack")
                if (isAllFilesPermissionAllowed()) {
                    LogE("Manage all file permission is granted")
                    onPermissionResult.invoke(true)
                } else {
                    onPermissionResult.invoke(false)
                }
            })
    }

    private fun requestManageAllFilePermission(onPermissionResultCallBack: (Boolean) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            if (!Environment.isExternalStorageManager()) {
                val uri = Uri.parse("package:${activity.packageName}")
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri)
//                activity.startActivityForResult(intent, requestCode)
                manageAllFilePermissionCallback =
                    onPermissionResultCallBack
                manageAllFilePermissionLauncher.launch(intent)

            } else {
                LogE("permission already granted")
                manageAllFilePermissionCallback?.invoke(true)
            }

        } else {
            LogE("Don't need all file permission instead asking storage permission")
        }
    }

    fun changeDialogUIColor(
        dialogBgColor: String = "#ffffff",
        dialogTitleTextColor: String = "#000000",
        dialogDescriptionTextColor: String = "#000000",
        dialogButtonBgColor: String = "#003FFB",
        dialogButtonTextColor: String = "#ffffff",
        dialogCancelButtonColor: String = "#999999",
    ) {
        this.dialogBgColor = dialogBgColor
        this.dialogTitleTextColor = dialogTitleTextColor
        this.dialogDescriptionTextColor = dialogDescriptionTextColor
        this.dialogButtonBgColor = dialogButtonBgColor
        this.dialogButtonTextColor = dialogButtonTextColor
        this.dialogCancelButtonColor = dialogCancelButtonColor
    }

    private fun showPermissionExplanationDialog(
        title: String,
        description: String,
        positiveBtnClick: () -> Unit,
        negativeBtnClick: () -> Unit

    ): AlertDialog {
        val dialogView = LayoutInflater.from(activity).inflate(R.layout.permission_dialog, null)
        val btnAllow: Button = dialogView.findViewById(R.id.btnAllow)

        val bgLayout: ConstraintLayout = dialogView.findViewById(R.id.bgLayout)
        val tvTitleDialog: TextView = dialogView.findViewById(R.id.tvTitleDialog)
        val tvDesDialog: TextView = dialogView.findViewById(R.id.tvDesDialog)

        tvTitleDialog.text = title
        tvDesDialog.text = description
        val btnClose: ImageView = dialogView.findViewById(R.id.btnClose)
        val builder = AlertDialog.Builder(activity)

        bgLayout.backgroundTintList = ColorStateList.valueOf(Color.parseColor(dialogBgColor))
        tvTitleDialog.setTextColor(ColorStateList.valueOf(Color.parseColor(dialogTitleTextColor)))
        tvDesDialog.setTextColor(ColorStateList.valueOf(Color.parseColor(dialogDescriptionTextColor)))
        btnAllow.backgroundTintList = ColorStateList.valueOf(Color.parseColor(dialogButtonBgColor))
        btnAllow.setTextColor(ColorStateList.valueOf(Color.parseColor(dialogButtonTextColor)))
        btnClose.imageTintList = ColorStateList.valueOf(Color.parseColor(dialogCancelButtonColor))



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


    private fun showRationalPermissionDialog(
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
        val bgLayout: ConstraintLayout = dialogView.findViewById(R.id.bgLayout)

        bgLayout.backgroundTintList = ColorStateList.valueOf(Color.parseColor(dialogBgColor))
        tvTitleDialog.setTextColor(ColorStateList.valueOf(Color.parseColor(dialogTitleTextColor)))
        tvDesDialog.setTextColor(ColorStateList.valueOf(Color.parseColor(dialogDescriptionTextColor)))
        btnAllow.backgroundTintList = ColorStateList.valueOf(Color.parseColor(dialogButtonBgColor))
        btnAllow.setTextColor(ColorStateList.valueOf(Color.parseColor(dialogButtonTextColor)))
        btnClose.imageTintList = ColorStateList.valueOf(Color.parseColor(dialogCancelButtonColor))


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


    private fun requestPermissions(
        permissions: Array<String>,
        callback: (Boolean, Boolean) -> Unit
    ) {
        permissionCallback = callback
        permissionLauncher.launch(permissions) // Launch the permission request
    }

    private var permissionCallback: ((Boolean, Boolean) -> Unit)? = null
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


    private var permissionRationalCallback: (() -> Unit)? = null
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


    private var manageAllFilePermissionCallback: ((Boolean) -> Unit)? = null
    private val manageAllFilePermissionLauncher =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            LogE("result.resultCode: ${result.resultCode}")
            LogE("result.resultCode2 ${manageAllFilePermissionCallback}")
            manageAllFilePermissionCallback?.invoke(true)
            allFileResultInterface?.onPermissionDenied()
        }

}

