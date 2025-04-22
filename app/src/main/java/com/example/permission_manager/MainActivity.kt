package com.example.permission_manager

import android.Manifest.permission.READ_CONTACTS
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_MEDIA_VIDEO
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.easypermission.AllFileResultInterface
import com.example.easypermission.LogE
import com.example.easypermission.PermissionManager
import com.example.easypermission.PermissionViewModel

class MainActivity : AppCompatActivity() {

    var permissionManager: PermissionManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        LogE("onCreate")

//        permissionManager = PermissionManager(this@MainActivity)
        //for specifically all file permission, this could be called in case if all file process end functionality needs to be handled
        permissionManager = PermissionManager(this@MainActivity, object : AllFileResultInterface {
            override fun onPermissionDenied() {
                LogE("onPermissionDenied kkjkjk")
                findViewById<TextView>(R.id.requestPermission).visibility= View.GONE
            }
        })
//        permissionManager?.initViewMode(permissionViewModel)
        /*permissionManager?.changeDialogUIColor(
            "#ffffff",
            "#000000",
            "#000000",
            "#003FFB",
            "#ffffff",
            "#999999"
        )*/

        findViewById<TextView>(R.id.requestPermission).setOnClickListener {
            permissionManager?.checkPermissionAndRequestIfNeeded(
                READ_CONTACTS, "Image",
                { isPermissionAllowed ->
                    LogE("is permission granted:$isPermissionAllowed")

                }, true,
                { isPermissionAllowed ->
                    // in rational permission case
                    LogE("is permission granted:$isPermissionAllowed")
                }
            )
        }


       /* findViewById<TextView>(R.id.requestPermission).setOnClickListener {
            permissionManager?.checkManageAllFilePermissionAndRequestIfNeeded(
                onPermissionResult = { isPermissionAllowed ->
                    if (isPermissionAllowed) {
                        LogE("permission granted:$isPermissionAllowed")
                    } else {
                        LogE("permission not granted:$isPermissionAllowed")
                    }
                },
                true,
                onDeniedButtonOfExplanationDialog = {
                    LogE("onDeniedButtonOfExplanationDialog")
                }
            )
        }*/

    }

    override fun onResume() {
        super.onResume()
        LogE("onResume")
    }


}