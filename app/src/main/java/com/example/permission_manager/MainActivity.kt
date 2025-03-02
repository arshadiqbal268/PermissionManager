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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val permissionManager = PermissionManager(this@MainActivity)

        findViewById<TextView>(R.id.requestPermission).setOnClickListener {
            permissionManager.requestPermission(READ_MEDIA_IMAGES, "Image",
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