package com.example.permission_manager

import android.Manifest.permission.READ_MEDIA_IMAGES
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.easypermission.LogE
import com.example.easypermission.PermissionManager

class MainActivity : AppCompatActivity() {

    var permissionManager: PermissionManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        permissionManager = PermissionManager(this@MainActivity)
        /*permissionManager?.changeDialogUIColor(
            "#ffffff",
            "#000000",
            "#000000",
            "#003FFB",
            "#ffffff",
            "#999999"
        )*/
        findViewById<TextView>(R.id.requestPermission).setOnClickListener {
            permissionManager?.requestPermission(READ_MEDIA_IMAGES, "Image",
                { isPermissionAllowed ->
                    LogE("is permission granted:$isPermissionAllowed")

                }, true,
                { isPermissionAllowed ->
                    // in rational permission case
                    LogE("is permission granted:$isPermissionAllowed")
                }
            )
        }

    }


}